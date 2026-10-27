package com.perso.T4C.online;

import com.google.gson.Gson;
import com.perso.T4C.online.model.OnlineCharacterView;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.online.model.OnlineRegion;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.online.model.NpcInteractionReply;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Network-only adapter for the local Alpha server; never touches graphics or character files. */
public final class OnlineConnection implements AutoCloseable {
  public record MapInfo(int originX, int originY, int width, int height, boolean persistent, int protocolVersion,
      int world,String key,long locationEpoch) {
    public OnlineRegion region() { return new OnlineRegion(world,key,originX,originY,width,height,locationEpoch); }
  }

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

  public record Actor(long id, double x, double y, long sequence, long appearance, int hp, int maxHp) {}

  public record Snapshot(long tick, List<Actor> players, Long characterRevision, List<CreatureView> creatures,
      OnlineRegion region, List<NpcView> npcs) {}

  public record Corpse(java.util.UUID id, int world, double x, double y, int itemCount, long gold) {}

  public record View(long ownId, MapInfo map, Snapshot snapshot, OnlineCharacterView character,
      List<Corpse> corpses) {}

  private record Joined(String token, long id, OnlineRegion region) {}

  public record CommandResult(boolean accepted, OnlineCharacterView character, String messageKey) {
    public CommandResult(boolean accepted, OnlineCharacterView character) { this(accepted,character,null); }
  }
  private record GameplayCommand(String path, String body, long revision,
      java.util.concurrent.CompletableFuture<CommandResult> result) {}
  private record Committed(Long revision) {}
  private static final class Rejected extends IllegalStateException {
    private final int code;
    Rejected(int code) { super("Server rejected request: " + code); this.code = code; }
  }

  private static final Gson JSON = new Gson();
  private final URI endpoint;
  private final Credentials credentials;
  private final AtomicInteger direction = new AtomicInteger(4);
  private final AtomicInteger pendingTap = new AtomicInteger(-1);
  private final java.util.concurrent.atomic.AtomicLong readyRegion = new java.util.concurrent.atomic.AtomicLong(-1);
  private volatile long acknowledgedRegion = -1;
  private final java.util.concurrent.atomic.AtomicReference<GameplayCommand> gameplay =
      new java.util.concurrent.atomic.AtomicReference<>();
  private final Thread worker;
  private volatile boolean closed;
  private volatile View view;
  private volatile String status = "connecting";

  public OnlineConnection(URI endpoint) {
    this(endpoint, null);
  }

  public OnlineConnection(URI endpoint, Credentials credentials) {
    this.endpoint = OnlineEndpoint.validate(endpoint);
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

  /** The renderer acknowledges only after the matching map assets are installed. */
  public void confirmRegionReady(long locationEpoch) {
    var current=view;
    if (current!=null && current.map().locationEpoch()==locationEpoch && acknowledgedRegion!=locationEpoch)
      readyRegion.set(locationEpoch);
  }

  public String status() {
    return status;
  }

  /** Nonblocking, single-flight command. No local equipment prediction or automatic replay. */
  public java.util.concurrent.CompletionStage<CommandResult> equip(
      java.util.UUID instance, com.perso.T4C.player.BodyPart slot) {
    java.util.Objects.requireNonNull(instance);
    java.util.Objects.requireNonNull(slot);
    return gameplayCommand("/equip", instance + " " + slot.name());
  }

  public java.util.concurrent.CompletionStage<CommandResult> unequip(com.perso.T4C.player.BodyPart slot) {
    java.util.Objects.requireNonNull(slot);
    return gameplayCommand("/unequip", slot.name());
  }

  /** The client supplies only a target identity and committed revision, never damage or rewards. */
  public java.util.concurrent.CompletionStage<CommandResult> attack(long target) {
    if (target<=0) throw new IllegalArgumentException("Invalid target");
    return gameplayCommand("/attack",Long.toString(target));
  }

  /** Reference-only recovery; ownership, range and contents remain server decisions. */
  public java.util.concurrent.CompletionStage<CommandResult> recover(java.util.UUID corpse) {
    return gameplayCommand("/recover",java.util.Objects.requireNonNull(corpse).toString());
  }

  /** Authored identity only; the server resolves dialogue, proximity and any item grant. */
  public java.util.concurrent.CompletionStage<CommandResult> interact(String npc) {
    if(npc==null || !npc.matches("[A-Za-z][A-Za-z0-9_]{0,63}"))
      throw new IllegalArgumentException("Invalid NPC identity");
    return gameplayCommand("/interact",npc);
  }

  private java.util.concurrent.CompletionStage<CommandResult> gameplayCommand(String path, String arguments) {
    View current = view;
    if (closed || current == null || current.character() == null || !status.equals("connected"))
      throw new IllegalStateException("Persistent session is not connected");
    long revision = current.character().revision();
    var command = new GameplayCommand(path, revision + " " + current.map().locationEpoch() + " " + arguments, revision,
        new java.util.concurrent.CompletableFuture<>());
    if (!gameplay.compareAndSet(null, command)) throw new IllegalStateException("Gameplay command pending");
    // Close/disconnect may race publication of the command after the initial state check.
    if (closed || !status.equals("connected")) failCommand();
    return command.result().minimalCompletionStage();
  }

  private void failCommand() {
    var command = gameplay.getAndSet(null);
    if (command != null) command.result().completeExceptionally(
        new IllegalStateException("Gameplay result unavailable; reconnect and reload committed state"));
  }

  private void run() {
    String token = null;
    try (var client = OnlineEndpoint.client()) {
      try {
        MapInfo map = JSON.fromJson(send(client, "/map", null, null), MapInfo.class);
        if (map == null || map.protocolVersion() != OnlineProtocol.VERSION)
          throw new IllegalStateException("Incompatible online protocol; update client and server together");
        if (map.width() <= 0 || map.height() <= 0 || map.width() > 1024 || map.height() > 1024)
          throw new IllegalStateException("Invalid server map");
        if (map.persistent() != (credentials != null))
          throw new IllegalStateException("Profile and server mode do not match");
        map.region();
        String login =
            credentials == null ? "" : credentials.secret() + " " + credentials.characterId();
        Joined joined = JSON.fromJson(send(client, "/join", login, null), Joined.class);
        if (joined.id() <= 0 || joined.token() == null || !joined.token().matches("[0-9a-f]{64}"))
          throw new IllegalStateException("Invalid server session");
        token = joined.token();
        if (joined.region()==null) throw new IllegalStateException("Missing joined region");
        map=readMap(client,token,map.persistent());
        if (!map.region().equals(joined.region())) throw new IllegalStateException("Joined map changed unexpectedly");
        long sequence = 0;
        OnlineCharacterView character = null;
        List<Corpse> corpses = List.of();
        while (!closed) {
          long ready=readyRegion.getAndSet(-1);
          if (ready==map.locationEpoch() && acknowledgedRegion!=ready) {
            try { send(client,"/region-ready",Long.toString(ready),token); acknowledgedRegion=ready; }
            catch (Rejected rejected) { if (rejected.code!=409) throw rejected; }
          }
          var command = gameplay.get();
          boolean accepted = false;
          String messageKey=null;
          long requiredRevision = character == null ? 0 : character.revision();
          if (command != null) {
            try {
              var reply=send(client, command.path(), command.body(), token);
              if(command.path().equals("/interact")) {
                var dialogue=JSON.fromJson(reply,NpcInteractionReply.class);
                if(dialogue==null || dialogue.revision()<command.revision()
                    || dialogue.changed() && dialogue.revision()==command.revision())
                  throw new IllegalStateException("Invalid dialogue acknowledgement");
                requiredRevision=dialogue.revision();messageKey=dialogue.messageKey();
              } else {
                var committed = JSON.fromJson(reply, Committed.class);
                if (committed == null || committed.revision() == null || committed.revision() <= command.revision())
                  throw new IllegalStateException("Invalid gameplay acknowledgement");
                requiredRevision = committed.revision();
              }
              accepted = true;
            } catch (Rejected rejected) {
              if (rejected.code != 409) throw rejected;
              // A normal conflict leaves the session usable. Refresh, but never replay stale intent.
            }
          }
          int intent = direction.get();
          int tap = pendingTap.getAndSet(-1);
          if (intent == 4 && tap >= 0) intent = tap;
          if (acknowledgedRegion!=map.locationEpoch()) intent=4;
          try { send(
              client,
              "/input",
              map.locationEpoch()+" "+sequence++ + " " + (intent / 3 - 1) + " " + (intent % 3 - 1),
              token); }
          catch (Rejected rejected) { if (rejected.code!=409) throw rejected; }
          int regionRetries=0;
          while (true) {
          Snapshot snapshot = JSON.fromJson(send(client, "/state", null, token), Snapshot.class);
          if (snapshot==null || snapshot.region()==null) throw new IllegalStateException("Missing region state");
          if (!map.region().equals(snapshot.region())) {
            stopInput(); acknowledgedRegion=-1; readyRegion.set(-1);
            map=readMap(client,token,map.persistent());
            snapshot=JSON.fromJson(send(client,"/state",null,token),Snapshot.class);
            if (snapshot==null || !map.region().equals(snapshot.region()))
              throw new IllegalStateException("Region state did not stabilize");
          }
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
                || actor.maxHp() <= 0 || actor.hp() < 0 || actor.hp() > actor.maxHp()
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
          var npcs=snapshot.npcs()==null ? List.<NpcView>of() : snapshot.npcs();
          if(npcs.size()>64) throw new IllegalStateException("Invalid NPC count");
          var npcIds=new java.util.HashSet<String>();
          for(var npc:npcs)
            if(npc==null || npc.x()>=map.width() || npc.y()>=map.height() || !npcIds.add(npc.id()))
              throw new IllegalStateException("Invalid NPC snapshot");
          if (map.persistent()) {
            if (snapshot.characterRevision() == null || snapshot.characterRevision() < 0)
              throw new IllegalStateException("Missing committed character revision");
            if (command != null || character == null || character.revision() < snapshot.characterRevision()
                || character.locationEpoch()!=map.locationEpoch()) {
              OnlineCharacterView updated =
                  JSON.fromJson(send(client, "/character", null, token), OnlineCharacterView.class);
              if (updated == null || !credentials.characterId().equals(updated.id())
                  || updated.revision() < Math.max(requiredRevision, snapshot.characterRevision()))
                throw new IllegalStateException("Invalid private character state");
              character = updated;
              var found = JSON.fromJson(send(client,"/corpses",null,token),Corpse[].class);
              if (found==null || found.length>256) throw new IllegalStateException("Invalid corpse list");
              var identities=new java.util.HashSet<java.util.UUID>();
              for (var corpse:found) {
                if (corpse==null || corpse.id()==null || !identities.add(corpse.id()) || corpse.world()<0
                    || !Double.isFinite(corpse.x()) || !Double.isFinite(corpse.y()) || corpse.x()<0 || corpse.y()<0
                    || corpse.itemCount()<0 || corpse.gold()<0)
                  throw new IllegalStateException("Invalid corpse state");
              }
              corpses=List.of(found);
            }
            if (character.locationEpoch()!=map.locationEpoch()) {
              stopInput(); acknowledgedRegion=-1; readyRegion.set(-1);
              map=readMap(client,token,map.persistent()); character=null; corpses=List.of();
              if (++regionRetries>=3) throw new IllegalStateException("Private region state did not stabilize");
              continue; // Refresh reads only. Never replay the gameplay mutation already sent above.
            }
          }
          synchronized (this) {
            if (closed) break;
            view =
              new View(
                  joined.id(), map,
                  new Snapshot(snapshot.tick(), List.copyOf(snapshot.players()), snapshot.characterRevision(), List.copyOf(creatures),snapshot.region(),List.copyOf(npcs)),
                  character,corpses);
            status = "connected";
          }
          break;
          }
          if (command != null && gameplay.compareAndSet(command, null))
            command.result().complete(new CommandResult(accepted, character,messageKey));
          Thread.sleep(75);
        }
      } finally {
        status = "disconnected";
        view = null;
        failCommand();
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
      failCommand();
    }
  }

  private MapInfo readMap(HttpClient client,String token,boolean persistent) throws Exception {
    var loaded=JSON.fromJson(send(client,"/map",null,token),MapInfo.class);
    if (loaded==null || loaded.protocolVersion()!=OnlineProtocol.VERSION || loaded.persistent()!=persistent)
      throw new IllegalStateException("Invalid region map");
    loaded.region(); // Validate the bounded descriptor before publishing actor coordinates.
    return loaded;
  }

  private String send(HttpClient client, String path, String body, String token) throws Exception {
    var request = HttpRequest.newBuilder(endpoint.resolve(path)).timeout(Duration.ofSeconds(2));
    if ("/join".equals(path)) request.header(OnlineProtocol.VERSION_HEADER, Integer.toString(OnlineProtocol.VERSION));
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
      throw new Rejected(response.statusCode());
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
  public synchronized void close() {
    closed = true;
    stopInput();
    status = "disconnected";
    view = null;
    failCommand();
    // Requests already have a whole-transfer deadline. Do not interrupt a logout that
    // may be running after a failed command; let the worker finish its bounded cleanup.
  }
}
