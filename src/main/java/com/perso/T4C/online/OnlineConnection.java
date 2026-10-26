package com.perso.T4C.online;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Network-only adapter for the local Alpha server; never touches graphics or character files. */
public final class OnlineConnection implements AutoCloseable {
  public record MapInfo(int originX, int originY, int width, int height) {}

  public record Actor(long id, double x, double y, long sequence) {}

  public record Snapshot(long tick, List<Actor> players) {}

  public record View(long ownId, MapInfo map, Snapshot snapshot) {}

  private record Joined(String token, long id) {}

  private static final Gson JSON = new Gson();
  private final URI endpoint;
  private final AtomicInteger direction = new AtomicInteger(4);
  private final Thread worker;
  private volatile boolean closed;
  private volatile View view;
  private volatile String status = "connecting";

  public OnlineConnection(URI endpoint) {
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
    worker = new Thread(this::run, "online-client-network");
    worker.setDaemon(true);
    worker.start();
  }

  public void direction(int dx, int dy) {
    if (dx < -1 || dx > 1 || dy < -1 || dy > 1)
      throw new IllegalArgumentException("Invalid direction");
    direction.set((dx + 1) * 3 + dy + 1);
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
        Joined joined = JSON.fromJson(send(client, "/join", "", null), Joined.class);
        if (joined.id() <= 0 || joined.token() == null || !joined.token().matches("[0-9a-f]{64}"))
          throw new IllegalStateException("Invalid server session");
        token = joined.token();
        long sequence = 0;
        while (!closed) {
          int intent = direction.get();
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
          view =
              new View(
                  joined.id(), map, new Snapshot(snapshot.tick(), List.copyOf(snapshot.players())));
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
    var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 200)
      throw new IllegalStateException("Server rejected request: " + response.statusCode());
    return response.body();
  }

  @Override
  public void close() {
    closed = true;
    direction.set(4);
    status = "disconnected";
    view = null;
    worker.interrupt();
  }
}
