package com.perso.T4C.online;

import com.google.gson.Gson;
import com.perso.T4C.online.model.OnlineCharacterView;
import com.perso.T4C.online.model.CreatureView;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Network-only adapter for the local Alpha server; never touches graphics or character files. */
public final class OnlineConnection implements AutoCloseable {
  public record MapInfo(int originX, int originY, int width, int height, boolean persistent) {}

  public record Credentials(String secret, java.util.UUID characterId) {
    public Credentials {
      if (secret == null || !secret.matches("[0-9a-f]{64}") || characterId == null)
        throw new IllegalArgumentException("Invalid online credentials");
    }

    @Override
    public String toString() {
      return "Credentials[characterId=" + characterId + ", secret=REDACTED]";
    }
  }

  public record Actor(long id, double x, double y, long sequence, long appearance) {}

  public record Snapshot(long tick, List<Actor> players, Long characterRevision, List<CreatureView> creatures) {}

  public record View(long ownId, MapInfo map, Snapshot snapshot, OnlineCharacterView character) {}

  private record Joined(String token, long id) {}

  private static final Gson JSON = new Gson();
  private final URI endpoint;
  private final Credentials credentials;
  private final AtomicInteger direction = new AtomicInteger(4);
  private final AtomicInteger pendingTap = new AtomicInteger(-1);
  private final Thread worker;
  private volatile boolean closed;
  private volatile View view;
  private volatile String status = "connecting";

  public OnlineConnection(URI endpoint) {
    this(endpoint, null);
  }

  public OnlineConnection(URI endpoint, Credentials credentials) {
    if (!"http".equals(endpoint.getScheme())
        || !"127.0.0.1".equals(endpoint.getHost())
        || endpoint.getPort() < 1
        || endpoint.getPort() > 65535
        || endpoint.getUserInfo() != null
        || endpoint.getQuery() != null
        || endpoint.getFragment() != null
        || !(endpoint.getPath().isEmpty() || "/".equals(endpoint.getPath())))
      throw new IllegalArgumentException("Local Alpha requires http://127.0.0.1:PORT");
    this.endpoint = URI.create("http://127.0.0.1:" + endpoint.getPort());
    this.credentials = credentials;
    worker = new Thread(this::run, "online-client-network");
    // Keep the process alive for the bounded logout request after the render window closes.
    worker.start();
  }

  public void direction(int dx, int dy) {
    if (dx < -1 || dx > 1 || dy < -1 || dy > 1)
      throw new IllegalArgumentException("Invalid direction");
    direction.set((dx + 1) * 3 + dy + 1);
  }

  /** Preserve a brief key press even if press/release both happen between rendered frames. */
  public void tap(int dx, int dy) {
    if (dx < -1 || dx > 1 || dy < -1 || dy > 1)
      throw new IllegalArgumentException("Invalid direction");
    pendingTap.set((dx + 1) * 3 + dy + 1);
  }

  public void stopInput() {
    direction.set(4);
    pendingTap.set(-1);
  }

  public View view() {
    return view;
  }

  public String status() {
    return status;
  }

  private void run() {
    String token = null;
    try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
      try {
        MapInfo map = JSON.fromJson(send(client, "/map", null, null), MapInfo.class);
        if (map.width() <= 0 || map.height() <= 0 || map.width() > 1024 || map.height() > 1024)
          throw new IllegalStateException("Invalid server map");
        if (map.persistent() != (credentials != null))
          throw new IllegalStateException("Profile and server mode do not match");
        String login =
            credentials == null ? "" : credentials.secret() + " " + credentials.characterId();
        Joined joined = JSON.fromJson(send(client, "/join", login, null), Joined.class);
        if (joined.id() <= 0 || joined.token() == null || !joined.token().matches("[0-9a-f]{64}"))
          throw new IllegalStateException("Invalid server session");
        token = joined.token();
        long sequence = 0;
        OnlineCharacterView character = null;
        while (!closed) {
          int intent = direction.get();
          int tap = pendingTap.getAndSet(-1);
          if (intent == 4 && tap >= 0) intent = tap;
          send(
              client,
              "/input",
              sequence++ + " " + (intent / 3 - 1) + " " + (intent % 3 - 1),
              token);
          Snapshot snapshot = JSON.fromJson(send(client, "/state", null, token), Snapshot.class);
          if (snapshot.players() == null || snapshot.players().size() > 20)
            throw new IllegalStateException("Invalid server snapshot");
          boolean hasSelf = false;
          for (Actor actor : snapshot.players()) {
            if (actor == null
                || actor.id() <= 0
                || !Double.isFinite(actor.x())
                || !Double.isFinite(actor.y())
                || actor.x() < 0
                || actor.y() < 0
                || actor.x() >= map.width()
                || actor.y() >= map.height())
              throw new IllegalStateException("Invalid server position");
            hasSelf |= actor.id() == joined.id();
          }
          if (!hasSelf) throw new IllegalStateException("Session missing from snapshot");
          var creatures = snapshot.creatures() == null ? List.<CreatureView>of() : snapshot.creatures();
          if (creatures.size() > 256) throw new IllegalStateException("Invalid creature count");
          var creatureIds = new java.util.HashSet<Long>();
          for (var creature : creatures) {
            if (creature == null || creature.x() >= map.width() || creature.y() >= map.height()
                || !creatureIds.add(creature.id()))
              throw new IllegalStateException("Invalid creature snapshot");
          }
          if (map.persistent()) {
            if (snapshot.characterRevision() == null || snapshot.characterRevision() < 0)
              throw new IllegalStateException("Missing committed character revision");
            if (character == null || character.revision() < snapshot.characterRevision()) {
              OnlineCharacterView updated =
                  JSON.fromJson(send(client, "/character", null, token), OnlineCharacterView.class);
              if (updated == null || !credentials.characterId().equals(updated.id())
                  || updated.revision() < snapshot.characterRevision())
                throw new IllegalStateException("Invalid private character state");
              character = updated;
            }
          }
          view =
              new View(
                  joined.id(), map,
                  new Snapshot(snapshot.tick(), List.copyOf(snapshot.players()), snapshot.characterRevision(), List.copyOf(creatures)),
                  character);
          status = "connected";
          Thread.sleep(75);
        }
      } finally {
        // Cleanup stays off the render thread. The server expires the session if it is unavailable.
        Thread.interrupted();
        if (token != null) {
          try {
            send(client, "/leave", "", token);
          } catch (Exception ignored) {
          }
        }
      }
    } catch (Exception error) {
      if (!closed) System.err.println("Online connection stopped: " + error.getMessage());
      status = "disconnected";
    } finally {
      if (!closed) status = "disconnected";
      view = null;
    }
  }

  private String send(HttpClient client, String path, String body, String token) throws Exception {
    var request = HttpRequest.newBuilder(endpoint.resolve(path)).timeout(Duration.ofSeconds(2));
    if (token != null) request.header("Authorization", "Bearer " + token);
    if (body != null)
      request
          .header("Origin", endpoint.toString())
          .header("Content-Type", "text/plain")
          .POST(HttpRequest.BodyPublishers.ofString(body));
    var pending = client.sendAsync(request.build(), ignored -> new BoundedBody());
    HttpResponse<byte[]> response;
    try {
      // Bound the whole body transfer too; a headers-only timeout is insufficient for streamed data.
      response = pending.get(2, java.util.concurrent.TimeUnit.SECONDS);
    } catch (java.util.concurrent.TimeoutException | InterruptedException error) {
      pending.cancel(true);
      throw error;
    }
    if (response.statusCode() != 200)
      throw new IllegalStateException("Server rejected request: " + response.statusCode());
    return new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
  }

  /** Bound bytes while receiving, before allocating the complete response. */
  private static final class BoundedBody implements HttpResponse.BodySubscriber<byte[]> {
    // The store caps gameplay JSON at 1 MiB. Allow its envelope without unbounded buffering.
    private static final int LIMIT = 2 * 1024 * 1024;
    private final HttpResponse.BodySubscriber<byte[]> delegate = HttpResponse.BodySubscribers.ofByteArray();
    private java.util.concurrent.Flow.Subscription subscription;
    private long received;

    @Override public java.util.concurrent.CompletionStage<byte[]> getBody() { return delegate.getBody(); }
    @Override public void onSubscribe(java.util.concurrent.Flow.Subscription value) {
      subscription = value; delegate.onSubscribe(value);
    }
    @Override public void onNext(List<java.nio.ByteBuffer> buffers) {
      for (var buffer : buffers) received += buffer.remaining();
      if (received > LIMIT) {
        subscription.cancel();
        delegate.onError(new java.io.IOException("Server response exceeds limit"));
      } else delegate.onNext(buffers);
    }
    @Override public void onError(Throwable error) { delegate.onError(error); }
    @Override public void onComplete() { delegate.onComplete(); }
  }

  @Override
  public void close() {
    closed = true;
    stopInput();
    status = "disconnected";
    view = null;
    worker.interrupt();
  }
}
