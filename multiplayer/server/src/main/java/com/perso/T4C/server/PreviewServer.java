package com.perso.T4C.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.online.model.OnlineCharacterView;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import com.perso.T4C.helper.CharacterCreationRules;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Loopback-only QA transport with optional persistent sessions and no desktop dependencies. */
public final class PreviewServer implements AutoCloseable {
  private record PersistentSnapshot(long tick, java.util.List<PreviewWorld.PlayerView> players,
      long characterRevision, java.util.List<com.perso.T4C.online.model.CreatureView> creatures,com.perso.T4C.online.model.OnlineRegion region,java.util.List<com.perso.T4C.online.model.NpcView> npcs,JsonArray loot) {}
  /** Ground loot shown in one snapshot: at most this many drops, nearest the viewer first. */
  static final int GROUND_LOOT_VIEW_LIMIT = 128;
  private static final Set<String> POST_ROUTES=Set.of("/join","/admin/shutdown","/input","/attack",
      "/recover","/equip","/unequip","/interact","/pickup","/cast","/use","/region-ready","/leave",
      "/account/characters","/shop-offers","/shop-buy","/shop-sell");
  private static final Set<String> GET_ROUTES=Set.of("/","/client.js","/style.css","/strings",
      "/account/characters","/map","/state","/character","/corpses","/health","/admin/metrics");
  private record RegionJoined(String token,long id,com.perso.T4C.online.model.OnlineRegion region) {}
  private static final Gson JSON = new Gson();
  private final com.perso.T4C.online.model.ItemContent items;
  private final HttpServer http;
  private final GameSocketServer gameSocket;
  private final ServerTransport transport;
  private final HttpRequestAdmission admission = new HttpRequestAdmission();
  private final HttpRequestAdmission.ExchangeExecutor requests = new HttpRequestAdmission.ExchangeExecutor();
  private final ArrayBlockingQueue<Runnable> commands = new ArrayBlockingQueue<>(128);
  private final AccountRosterService accounts;
  private final HostedWorlds hosted;
  private final PreviewWorld world;
  private final OnlineStore store;
  private final PersistentSessions persistent;
  private final PersistentCreatures persistentCreatures;
  private final OnlineMelee melee;
  private final OnlineRetaliation retaliation;
  private final String controlToken;
  private final SimulationLoop loop;
  private final HttpTrafficMetrics traffic = new HttpTrafficMetrics();
  private final ServerRuntimeMetrics runtimeMetrics;
  private final Thread simulation;
  private final AtomicBoolean closed = new AtomicBoolean();
  private final AtomicBoolean disposed = new AtomicBoolean();
  private final CountDownLatch terminated = new CountDownLatch(1);
  private final Object lifecycle = new Object();
  private final boolean ownsStore;
  private boolean started;
  private final Map<String, String> strings;
  private final String mapJson;
  private volatile Throwable failure;

  public PreviewServer(PreviewMap map, int port) throws IOException {
    this(map,port,null);
  }

  public PreviewServer(PreviewMap map, int port, OnlineStore store) throws IOException {
    this(map,port,store,null);
  }

  public PreviewServer(PreviewMap map, int port, OnlineStore store, String controlToken) throws IOException {
    this(map,port,store,controlToken,ServerTransport.local());
  }

  public PreviewServer(PreviewMap map, int port, OnlineStore store, String controlToken, ServerTransport transport) throws IOException {
    this(HostedRegionPlan.single(map),port,store,controlToken,transport);
  }

  PreviewServer(HostedRegionPlan plan,int port,OnlineStore store,String controlToken,ServerTransport transport) throws IOException {
    this(plan,port,store,controlToken,transport,false);
  }

  PreviewServer(HostedRegionPlan plan,int port,OnlineStore store,String controlToken,ServerTransport transport,boolean ownsStore) throws IOException {
    this(plan,port,defaultGameSocketPort(port),store,controlToken,transport,ownsStore);
  }

  PreviewServer(HostedRegionPlan plan,int port,int gameSocketPort,OnlineStore store,String controlToken,ServerTransport transport,boolean ownsStore) throws IOException {
    this.ownsStore=ownsStore;
    this.transport=java.util.Objects.requireNonNull(transport);
    if (controlToken!=null && !controlToken.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid control token");
    this.controlToken=controlToken;
    this.store=store;
    strings = loadStrings();
    items = ItemCatalog.loadBundled(); // Validate packaged equipment before accepting sessions.
    try { hosted=new HostedWorlds(plan,store,items,MonsterCatalog.loadBundled()); }
    catch(java.sql.SQLException recovery) { throw new IOException("Could not recover hosted regions",recovery); }
    accounts=store==null ? null : new AccountRosterService(store,plan);
    var initial=hosted.regions.getFirst();world=initial.world;persistent=hosted.sessions;
    persistentCreatures=initial.creatures;melee=initial.melee;retaliation=initial.retaliation;
    try { http = transport.create(port); }
    catch(IOException|RuntimeException failure) { if(accounts!=null) accounts.close();hosted.close();throw failure; }
    try {
      gameSocket = new GameSocketServer(transport.createGameSocket(gameSocketPort), new GameSocketServer.Handler() {
        @Override public GameSocketServer.Result request(com.perso.T4C.online.model.GameSocketProtocol.Request request,
            String sessionToken, InetSocketAddress remote) throws Exception {
          return socketRequest(request,sessionToken,remote);
        }
        @Override public void disconnected(String sessionToken) { socketDisconnected(sessionToken); }
      });
    } catch(IOException|RuntimeException failure) {
      http.stop(0);
      if(accounts!=null) accounts.close();
      hosted.close();
      throw failure;
    }
    mapJson=mapJson(initial,0);
    loop = new SimulationLoop(PreviewWorld.TICK_RATE, tick -> {
      for (int i = 0; i < 128; i++) {
        Runnable command = commands.poll();
        if (command == null) break;
        command.run();
      }
      hosted.step();
    });
    runtimeMetrics = new ServerRuntimeMetrics(loop, traffic);
    simulation = new Thread(() -> {
      try { loop.run(Long.MAX_VALUE); }
      catch (Throwable error) { failure = error; }
      finally {
        requestStop();
        finishShutdown();
      }
    }, "preview-world");
    http.setExecutor(requests);
    http.createContext("/", this::handle);
  }

  /** The reply to a potion or a self-cast of the live allowlist. */
  private static Map<String,Object> abilityJson(PersistentSessions.AbilityReply done) {
    var reply=new java.util.LinkedHashMap<String,Object>();
    reply.put("revision",done.revision());reply.put("healthGained",done.healthGained());
    reply.put("manaSpent",done.manaSpent());reply.put("manaGained",done.manaGained());
    return reply;
  }
  /** The reply to an engine cast: the same fields plus every creature the spell damaged. */
  private static Map<String,Object> spellJson(OnlineSpellStrikes.Reply done) {
    var reply=new java.util.LinkedHashMap<String,Object>();
    reply.put("revision",done.revision());reply.put("healthGained",done.healthGained());
    reply.put("manaSpent",done.manaSpent());reply.put("manaGained",0);
    var hits=new java.util.ArrayList<Map<String,Object>>();
    for (var hit:done.hits()) {
      var entry=new java.util.LinkedHashMap<String,Object>();
      entry.put("target",hit.target());entry.put("damage",hit.damage());entry.put("health",hit.health());entry.put("killed",hit.killed());
      hits.add(entry);
    }
    reply.put("hits",hits);
    var buffs=new java.util.ArrayList<Map<String,Object>>();
    for (var laid:done.buffs()) {
      var entry=new java.util.LinkedHashMap<String,Object>();
      entry.put("spell",laid.spell());entry.put("expiresAt",laid.expiresAt());
      buffs.add(entry);
    }
    reply.put("buffs",buffs);
    return reply;
  }
  private HostedWorlds.Region admit(String token,Long epoch) {
    var context=hosted.actor(token);context.world.admitRequest(token);
    if(epoch!=null) { if(persistent!=null) persistent.requireEpoch(token,epoch);else if(epoch!=0) throw new PreviewWorld.Rejected(409,"region_epoch"); }
    return context;
  }
  private String mapJson(HostedWorlds.Region region,long epoch) {
    var values=new java.util.LinkedHashMap<String,Object>();var map=region.map;
    values.put("width",map.width);values.put("height",map.height);values.put("originX",map.originX);values.put("originY",map.originY);
    values.put("blocked",map.cells());values.put("maxPlayers",PreviewWorld.MAX_PLAYERS);values.put("persistent",store!=null);
    values.put("protocolVersion",OnlineProtocol.VERSION);values.put("gameSocketPort",gameSocket.port());values.put("world",region.spec.world());values.put("key",region.spec.key());values.put("locationEpoch",epoch);
    return JSON.toJson(values);
  }

  private static int defaultGameSocketPort(int httpPort) {
    if (httpPort == 0) return 0;
    if (httpPort >= 65535) throw new IllegalArgumentException("Set a game socket port when the HTTP listener uses 65535");
    return httpPort + 1;
  }
  public String url() { return transport.origin(http.getAddress().getPort()); }

  public void start() {
    synchronized(lifecycle) {
      if(started || closed.get()) throw new IllegalStateException("Server already started or stopped");
      try { simulation.start();http.start();gameSocket.start();started=true; }
      catch(RuntimeException failure) { requestStop();throw failure; }
    }
  }

  public void await() throws InterruptedException {
    simulation.join();
    if (failure != null) throw new IllegalStateException("Preview simulation failed", failure);
  }

  private <T> T onWorld(Callable<T> action) throws Exception {
    var result = new CompletableFuture<T>();
    if (closed.get() || !commands.offer(() -> {
      synchronized (result) {
        if (result.isCancelled()) return;
        try { result.complete(action.call()); }
        catch (Exception error) { result.completeExceptionally(error); }
      }
    })) throw new PreviewWorld.Rejected(503, "busy");
    try { return result.get(2, TimeUnit.SECONDS); }
    catch (TimeoutException | InterruptedException error) {
      // Cancellation and the short, nonblocking world mutation must be atomic. Otherwise a
      // timed-out join could attach an actor while the HTTP handler releases its database lease.
      synchronized (result) {
        if (error instanceof InterruptedException) Thread.currentThread().interrupt();
        if (!result.isDone()) { result.cancel(false); throw new PreviewWorld.Rejected(503,"busy"); }
        try { return result.join(); }
        catch (CompletionException failed) {
          if (failed.getCause() instanceof PreviewWorld.Rejected rejected) throw rejected;
          throw failed;
        }
      }
    }
    catch (ExecutionException error) {
      if (error.getCause() instanceof PreviewWorld.Rejected rejected) throw rejected;
      throw error;
    }
  }

  /** Check header limits before reading; chunked bodies still have the same streaming byte cap. */
  private static void checkDeclaredBodyLength(HttpExchange exchange,int limit) {
    var lengths=exchange.getRequestHeaders().get("Content-Length");
    if(lengths==null) return;
    if(lengths.size()!=1) throw new PreviewWorld.Rejected(400,"input");
    try {
      long length=Long.parseLong(lengths.getFirst());
      if(length<0) throw new PreviewWorld.Rejected(400,"input");
      if(length>limit) throw new PreviewWorld.Rejected(413,"input");
    } catch(NumberFormatException invalid) { throw new PreviewWorld.Rejected(400,"input"); }
  }

  /** A known session check precedes the blocking body read, without spending its existing tick budget. */
  private HttpRequestAdmission.Permit knownSession(HttpExchange exchange) throws Exception {
    String session=token(exchange);
    onWorld(() -> { hosted.actor(session);return null; });
    return admission.session(session);
  }

  private HttpRequestAdmission.Permit admitPost(HttpExchange exchange,String path) throws Exception {
    if("/join".equals(path)) return admission.publicRequest();
    if("/account/characters".equals(path)) {
      if(accounts==null) throw new PreviewWorld.Rejected(404,"route");
      requireAccountProtocol(exchange);
      String secret=accountKey(exchange);
      var permit=admission.rosterRequest();
      try {
        var pending=accounts.authenticate(peer(exchange),secret);
        awaitAccount(pending);
        return permit;
      } catch(Exception|Error failure) {
        permit.close();
        throw failure;
      }
    }
    if("/admin/shutdown".equals(path)) {
      requireLocalAdmin(exchange);
      return admission.session(controlToken);
    }
    if(store==null && Set.of("/attack","/recover","/equip","/unequip","/interact","/pickup","/cast","/use",
        "/shop-offers","/shop-buy","/shop-sell").contains(path))
      throw new PreviewWorld.Rejected(404,"route");
    return knownSession(exchange);
  }

  private HttpRequestAdmission.Permit admitGet(HttpExchange exchange,String path) throws Exception {
    return switch(path) {
      case "/health" -> null; // Cheap local diagnostic must remain available while public bodies wait.
      case "/state" -> knownSession(exchange);
      case "/character","/corpses" -> {
        if(persistent==null) throw new PreviewWorld.Rejected(404,"route");
        yield knownSession(exchange);
      }
      case "/map" -> exchange.getRequestHeaders().getFirst("Authorization")==null
          ? admission.publicRequest() : knownSession(exchange);
      case "/account/characters" -> {
        if(accounts==null) throw new PreviewWorld.Rejected(404,"route");
        requireAccountProtocol(exchange);
        accountKey(exchange);
        yield admission.rosterRequest();
      }
      case "/admin/metrics" -> {
        requireLocalAdmin(exchange);
        yield admission.session(controlToken);
      }
      default -> admission.publicRequest();
    };
  }

  private void handle(HttpExchange exchange) throws IOException {
    var observation=traffic.begin(exchange.getRequestMethod(),exchange.getRequestURI().getRawPath());
    HttpRequestAdmission.Permit permit=null;
    try {
      String host = exchange.getRequestHeaders().getFirst("Host");
      if (!transport.acceptsHost(host,http.getAddress().getPort()))
        throw new PreviewWorld.Rejected(403, "origin");
      String method = exchange.getRequestMethod();
      String path = exchange.getRequestURI().getRawPath();
      if (exchange.getRequestURI().getRawQuery() != null && !("GET".equals(method) && "/account/characters".equals(path)))
        throw new PreviewWorld.Rejected(400, "input");
      if ("POST".equals(method)) {
        if (!POST_ROUTES.contains(path)) throw new PreviewWorld.Rejected(404, "route");
        if (!transport.acceptsOrigin(exchange.getRequestHeaders().getFirst("Origin"),http.getAddress().getPort()))
          throw new PreviewWorld.Rejected(403, "origin");
        var types=exchange.getRequestHeaders().get("Content-Type");
        String type = types==null || types.isEmpty() ? null : types.getFirst();
        if (type == null || ("/account/characters".equals(path)
            ? types.size()!=1 || !type.equalsIgnoreCase("application/json")
            : !type.toLowerCase(java.util.Locale.ROOT).startsWith("text/plain")))
          throw new PreviewWorld.Rejected(415, "input");
        if ("/join".equals(path) || "/pickup".equals(path) || "/cast".equals(path) || "/use".equals(path)
            || path.startsWith("/shop-") || "/account/characters".equals(path)) {
          var versions = exchange.getRequestHeaders().get(OnlineProtocol.VERSION_HEADER);
          if (versions == null || versions.size() != 1
              || !Integer.toString(OnlineProtocol.VERSION).equals(versions.getFirst()))
            throw new PreviewWorld.Rejected(426, "protocol");
        }
        int limit="/account/characters".equals(path) ? 512
            : store!=null && "/join".equals(path) ? 101
            : "/interact".equals(path) ? 128
            : path.startsWith("/shop-") ? 256
            : "/recover".equals(path) || "/cast".equals(path) ? 128
            : ("/equip".equals(path) || "/unequip".equals(path) || "/pickup".equals(path) || "/use".equals(path)) ? 96 : 64;
        checkDeclaredBodyLength(exchange,limit);
        permit=admitPost(exchange,path);
        byte[] bytes = new byte[limit+1];
        int count=0;
        try {
          var input=exchange.getRequestBody();
          while(count<bytes.length) {
            int read=input.read(bytes,count,bytes.length-count);
            if(read<0) break;
            if(read==0) {
              int one=input.read();
              if(one<0) break;
              bytes[count++]=(byte)one;
              observation.read(1);
            } else { count+=read;observation.read(read); }
          }
        } catch(IOException failedRead) { observation.readFailed();throw failedRead; }
        if (count > limit) throw new PreviewWorld.Rejected(413, "input");
        String body;
        if("/account/characters".equals(path)) {
          try {
            body=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes,0,count)).toString();
          } catch(java.nio.charset.CharacterCodingException invalid) {
            throw new PreviewWorld.Rejected(400,"creation_input");
          }
        } else body=new String(bytes,0,count,StandardCharsets.US_ASCII);
        switch (path) {
          case "/account/characters" -> {
            var request=parseCreation(body);
            var pending=accounts.create(peer(exchange),accountKey(exchange),request);
            json(exchange,observation,200,awaitAccount(pending));
          }
          case "/admin/shutdown" -> {
            requireLocalAdmin(exchange);
            if (!body.isEmpty()) throw new PreviewWorld.Rejected(400,"input");
            json(exchange,observation,200,Map.of("stopping",true));
            new Thread(this::close,"online-admin-stop").start();
          }
          case "/join" -> {
            if (store==null) {
              if (!body.isEmpty()) throw new PreviewWorld.Rejected(400, "input");
              json(exchange,observation, 200, onWorld(() -> { var joined=world.join();return new RegionJoined(joined.token(),joined.id(),hosted.regions.getFirst().descriptor(0)); }));
            } else {
              if (!body.matches("[0-9a-f]{64} [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
                throw new PreviewWorld.Rejected(400,"input");
              String[] credentials=body.split(" ");
              long started=System.nanoTime();
              var session=store.acquire(credentials[0],java.util.UUID.fromString(credentials[1]));
              boolean attached=false;
              try {
                var joined=onWorld(() -> persistent.join(session,started));
                attached=true;
                json(exchange,observation,200,onWorld(() -> new RegionJoined(joined.token(),joined.id(),persistent.region(joined.token()))));
              } finally { if (!attached) store.release(session); }
            }
          }
          case "/input" -> {
            String token = token(exchange);
            if (!body.matches("[0-9]{1,19} [0-9]{1,19} -?[01] -?[01]"))
              throw new PreviewWorld.Rejected(400, "input");
            String[] parts = body.split(" ");
            long sequence,epoch;
            try { epoch=Long.parseLong(parts[0]);sequence = Long.parseLong(parts[1]); }
            catch (NumberFormatException e) { throw new PreviewWorld.Rejected(400, "input"); }
            int dx = Integer.parseInt(parts[2]), dy = Integer.parseInt(parts[3]);
            onWorld(() -> {
              var context=admit(token,epoch);
              // Physical exhaustion after a cast or potion forbids movement: stop the actor, consume the
              // sequence and refuse definitely. The player stays connected and moves again once it expires.
              if ((dx!=0 || dy!=0) && persistent!=null && persistent.movementExhausted(token)) {
                context.world.input(token, sequence, 0, 0);
                throw new PreviewWorld.Rejected(409,"exhausted");
              }
              context.world.input(token, sequence, dx, dy); return null;
            });
            json(exchange,observation, 200, Map.of("ok", true));
          }
          case "/attack" -> {
            if (melee==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            if (!body.matches("[0-9]{1,19} [0-9]{1,19} [0-9]{1,19}")) throw new PreviewWorld.Rejected(400,"input");
            String[] parts=body.split(" "); long revision,epoch,target;
            try { revision=Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]); target=Long.parseLong(parts[2]); }
            catch (NumberFormatException bad) { throw new PreviewWorld.Rejected(400,"input"); }
            var pending=onWorld(() -> { return admit(token,epoch).melee.attack(token,revision,target); });
            var committed=pending.toCompletableFuture().get(2,TimeUnit.SECONDS);
            json(exchange,observation,200,Map.of("revision",committed.character().revision()));
          }
          case "/recover" -> {
            if (persistent==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            // Optional fourth token: one corpse stack to take. Absent takes every stack that fits.
            String uuid="[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
            if (!body.matches("[0-9]{1,19} [0-9]{1,19} "+uuid+"( "+uuid+")?"))
              throw new PreviewWorld.Rejected(400,"input");
            String[] parts=body.split(" "); long revision,epoch; java.util.UUID corpse,item;
            try { revision=Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]); corpse=java.util.UUID.fromString(parts[2]);
              item=parts.length>3 ? java.util.UUID.fromString(parts[3]) : null; }
            catch (IllegalArgumentException bad) { throw new PreviewWorld.Rejected(400,"input"); }
            var pending=onWorld(() -> { admit(token,epoch); return persistent.recoverCorpse(token,revision,corpse,item); });
            OnlineStore.RewardResult committed;
            try { committed=pending.toCompletableFuture().get(2,TimeUnit.SECONDS); }
            // A definite corpse refusal (item_unavailable, too_heavy, ...) arrives as the completion's cause.
            catch (ExecutionException error) { if(error.getCause() instanceof PreviewWorld.Rejected rejected) throw rejected; throw error; }
            json(exchange,observation,200,Map.of("revision",committed.character().revision()));
          }
          case "/equip", "/unequip" -> {
            if (persistent == null) throw new PreviewWorld.Rejected(404, "route");
            String token = token(exchange);
            boolean equip = "/equip".equals(path);
            String pattern = equip ? "[0-9]{1,19} [0-9]{1,19} [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12} [A-Z_]{1,16}"
                : "[0-9]{1,19} [0-9]{1,19} [A-Z_]{1,16}";
            if (!body.matches(pattern)) throw new PreviewWorld.Rejected(400, "input");
            String[] parts = body.split(" ");
            long revision,epoch;
            com.perso.T4C.player.BodyPart slot;
            java.util.UUID instance;
            try {
              revision = Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]);
              slot = com.perso.T4C.player.BodyPart.valueOf(parts[equip ? 3 : 2]);
              instance = equip ? java.util.UUID.fromString(parts[2]) : null;
            } catch (IllegalArgumentException error) { throw new PreviewWorld.Rejected(400, "input"); }
            var pending = onWorld(() -> {
              admit(token,epoch);
              var saved = persistent.character(token);
              var changed = equip ? OnlineEquipmentCommands.equip(saved.progress(), items, instance, slot)
                  : OnlineEquipmentCommands.unequip(saved.progress(), slot);
              return persistent.commitGameplayEvent(token, revision, java.util.UUID.randomUUID(), 0, 0, changed);
            });
            var committed = pending.toCompletableFuture().get(2, TimeUnit.SECONDS);
            json(exchange,observation, 200, Map.of("revision", committed.character().revision()));
          }
          case "/interact" -> {
            if(persistent==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            if(!body.matches("[0-9]{1,19} [0-9]{1,19} [A-Za-z][A-Za-z0-9_]{0,63}( [0-9]{1,2})?"))
              throw new PreviewWorld.Rejected(400,"input");
            String[] parts=body.split(" "); long revision,epoch;
            try { revision=Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]); }
            catch(NumberFormatException invalid) { throw new PreviewWorld.Rejected(400,"input"); }
            // Optional authored topic index; the NPC dispatcher rejects an index the NPC does not have.
            var topic=parts.length==4 ? java.util.OptionalInt.of(Integer.parseInt(parts[3])) : java.util.OptionalInt.empty();
            var pending=onWorld(()->{ admit(token,epoch);return persistent.interact(token,revision,parts[2],topic); });
            json(exchange,observation,200,pending.toCompletableFuture().get(2,TimeUnit.SECONDS));
          }
          case "/pickup" -> {
            if (persistent==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            if (!body.matches("[0-9]{1,19} [0-9]{1,19} [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
              throw new PreviewWorld.Rejected(400,"input");
            String[] parts=body.split(" "); long revision,epoch; java.util.UUID loot;
            try { revision=Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]); loot=java.util.UUID.fromString(parts[2]); }
            catch (IllegalArgumentException bad) { throw new PreviewWorld.Rejected(400,"input"); }
            var pending=onWorld(() -> { admit(token,epoch); return persistent.pickup(token,revision,epoch,loot); });
            OnlineStore.RewardResult committed;
            try { committed=pending.toCompletableFuture().get(2,TimeUnit.SECONDS); }
            // Definite refusals arrive as the completion's cause; unwrap them into their 409 code.
            catch (ExecutionException error) { if(error.getCause() instanceof PreviewWorld.Rejected rejected) throw rejected; throw error; }
            json(exchange,observation,200,Map.of("revision",committed.character().revision()));
          }
          case "/shop-offers" -> {
            if (persistent==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            // The revision slot is accepted for command-shape parity but a listing is a read: nothing is fenced on it.
            if (!body.matches("[0-9]{1,19} [0-9]{1,19} [A-Za-z][A-Za-z0-9_]{0,63}")) throw new PreviewWorld.Rejected(400,"input");
            String[] parts=body.split(" "); long epoch;
            try { Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]); }
            catch (NumberFormatException bad) { throw new PreviewWorld.Rejected(400,"input"); }
            json(exchange,observation,200,onWorld(()->{ admit(token,epoch);return persistent.shopOffers(token,epoch,parts[2]); }));
          }
          case "/shop-buy", "/shop-sell" -> {
            if (persistent==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            // Identity and a small count only: the server prices everything from its own catalog.
            if (!body.matches("[0-9]{1,19} [0-9]{1,19} [A-Za-z][A-Za-z0-9_]{0,63} item\\.[a-z0-9_]{1,120} [0-9]{1,2}"))
              throw new PreviewWorld.Rejected(400,"input");
            String[] parts=body.split(" "); long revision,epoch;
            try { revision=Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]); }
            catch (NumberFormatException bad) { throw new PreviewWorld.Rejected(400,"input"); }
            int quantity=Integer.parseInt(parts[4]);
            var side="/shop-buy".equals(path) ? OnlineShopTrades.Side.BUY : OnlineShopTrades.Side.SELL;
            var pending=onWorld(()->{ admit(token,epoch);return persistent.shopTrade(token,revision,epoch,parts[2],side,parts[3],quantity); });
            PersistentSessions.ShopReceipt receipt;
            try { receipt=pending.toCompletableFuture().get(2,TimeUnit.SECONDS); }
            // Definite refusals (insufficient_gold, too_heavy, ...) arrive as the completion's cause; unwrap to their 409.
            catch (ExecutionException error) { if(error.getCause() instanceof PreviewWorld.Rejected rejected) throw rejected; throw error; }
            json(exchange,observation,200,Map.of("revision",receipt.revision(),"quantity",receipt.quantity(),"total",receipt.total()));
          }
          case "/cast", "/use" -> {
            if (persistent==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            boolean cast="/cast".equals(path);
            String uuid="[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
            // Protocol 6: /cast may end with one target token, u:<creature id> or p:<x>,<y>; absent means self.
            String aim="u:[0-9]{1,19}|p:-?[0-9]{1,6},-?[0-9]{1,6}";
            if (!body.matches("[0-9]{1,19} [0-9]{1,19} "+(cast ? "spell\\.[a-z0-9_]{1,64}( "+aim+")?" : uuid)))
              throw new PreviewWorld.Rejected(400,"input");
            String[] parts=body.split(" "); long revision,epoch; java.util.UUID instance=null;
            com.perso.T4C.spell.engine.SpellEngine.Target target=new com.perso.T4C.spell.engine.SpellEngine.Target.Self();
            try { revision=Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]);
              if (!cast) instance=java.util.UUID.fromString(parts[2]);
              else if (parts.length>3) target=parts[3].startsWith("u:")
                  ? new com.perso.T4C.spell.engine.SpellEngine.Target.Unit(Long.parseLong(parts[3].substring(2)))
                  : new com.perso.T4C.spell.engine.SpellEngine.Target.Position(
                      Integer.parseInt(parts[3].substring(2,parts[3].indexOf(','))),
                      Integer.parseInt(parts[3].substring(parts[3].indexOf(',')+1))); }
            catch (IllegalArgumentException bad) { throw new PreviewWorld.Rejected(400,"input"); }
            final var consumable=instance; final var aimed=target;
            // One receipt-bound commit per request: a refusal changes nothing, an ambiguous outcome is never replayed.
            var pending=onWorld(() -> { var region=admit(token,epoch);
              if (!cast) return persistent.useItem(token,revision,consumable).thenApply(PreviewServer::abilityJson);
              // The live self-cast allowlist (spells-v1) is served exactly as before; the engine serves the rest.
              if (aimed instanceof com.perso.T4C.spell.engine.SpellEngine.Target.Self && persistent.legacySelfCast(parts[2]))
                return persistent.cast(token,revision,parts[2]).thenApply(PreviewServer::abilityJson);
              if (region.spells==null) throw new PreviewWorld.Rejected(409,"unsupported_ability");
              return region.spells.cast(token,revision,parts[2],aimed).thenApply(PreviewServer::spellJson); });
            Map<String,Object> done;
            try { done=pending.toCompletableFuture().get(2,TimeUnit.SECONDS); }
            catch (ExecutionException error) { if(error.getCause() instanceof PreviewWorld.Rejected rejected) throw rejected; throw error; }
            json(exchange,observation,200,done);
          }
          case "/region-ready" -> {
            String token=token(exchange);if(!body.matches("[0-9]{1,19}")) throw new PreviewWorld.Rejected(400,"input");
            long epoch;try { epoch=Long.parseLong(body); } catch(NumberFormatException invalid) { throw new PreviewWorld.Rejected(400,"input"); }
            onWorld(() -> { admit(token,epoch);if(persistent!=null) persistent.regionReady(token,epoch);return null; });
            json(exchange,observation,200,Map.of("ok",true));
          }
          case "/leave" -> {
            if (!body.isEmpty()) throw new PreviewWorld.Rejected(400, "input");
            String token = token(exchange);
            if (persistent==null) onWorld(() -> { world.leave(token); return null; });
            else onWorld(() -> persistent.leave(token)).get(2,TimeUnit.SECONDS);
            json(exchange,observation, 200, Map.of("ok", true));
          }
          default -> throw new PreviewWorld.Rejected(404, "route");
        }
      } else if ("GET".equals(method)) {
        if (!GET_ROUTES.contains(path)) throw new PreviewWorld.Rejected(404,"route");
        permit=admitGet(exchange,path);
        switch (path) {
          case "/" -> resource(exchange,observation, "index.html", "text/html; charset=utf-8");
          case "/client.js" -> resource(exchange,observation, "client.js", "text/javascript; charset=utf-8");
          case "/style.css" -> resource(exchange,observation, "style.css", "text/css; charset=utf-8");
          case "/strings" -> json(exchange,observation, 200, strings);
          case "/account/characters" -> {
            if(accounts==null) throw new PreviewWorld.Rejected(404,"route");
            requireAccountProtocol(exchange);
            String query=exchange.getRequestURI().getRawQuery();java.util.UUID after=null;
            if(query!=null) {
              if(!query.matches("after=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) throw new PreviewWorld.Rejected(400,"input");
              after=java.util.UUID.fromString(query.substring(6));
            }
            var pending=accounts.request(peer(exchange),accountKey(exchange),after);
            json(exchange,observation,200,awaitAccount(pending));
          }
          case "/map" -> {
            String authorization=exchange.getRequestHeaders().getFirst("Authorization");
            String description=authorization==null ? mapJson : onWorld(() -> { String token=token(exchange);var context=admit(token,null);
              return mapJson(context,persistent==null ? 0 : persistent.region(token).locationEpoch()); });
            send(exchange,observation,200,"application/json",description.getBytes(StandardCharsets.UTF_8));
          }
          case "/state" -> {
            String token = token(exchange);
            json(exchange,observation, 200, onWorld(() -> {
              var context=admit(token,null);var snapshot=context.world.snapshot(token);
              return new PersistentSnapshot(snapshot.tick(),snapshot.players(),persistent==null ? 0 : persistent.character(token).revision(),
                  snapshot.creatures(),context.descriptor(persistent==null ? 0 : persistent.region(token).locationEpoch()),context.npcs.views(),
                  groundLoot(token,context));
            }));
          }
          case "/character" -> {
            String token=token(exchange);
            if (persistent==null) throw new PreviewWorld.Rejected(404,"route");
            json(exchange,observation,200,onWorld(() -> {
              admit(token,null);
              var saved=persistent.character(token);
              return new OnlineCharacterView(saved.id(),saved.name(),saved.gold(),saved.experience(),
                  saved.revision(),saved.progress(),saved.locationEpoch());
            }));
          }
          case "/corpses" -> {
            if (persistent==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            var pending=onWorld(() -> { admit(token,null); return persistent.corpses(token); });
            var found=pending.toCompletableFuture().get(2,TimeUnit.SECONDS);
            json(exchange,observation,200,found.stream().map(c -> Map.of("id",c.id(),"world",c.world(),"x",c.x(),"y",c.y(),
                "itemCount",c.items().size(),"gold",c.gold())).toList());
          }
          case "/health" -> json(exchange,observation, 200,
              Map.of("tick", loop.completedTicks(), "mode", store==null ? "local-preview" : "local-persistent", "capacity", PreviewWorld.MAX_PLAYERS));
          case "/admin/metrics" -> {
            requireLocalAdmin(exchange);
            json(exchange,observation,200,runtimeMetrics.snapshot());
          }
          default -> throw new PreviewWorld.Rejected(404, "route");
        }
      } else throw new PreviewWorld.Rejected(405, "method");
    } catch (PreviewWorld.Rejected rejected) {
      json(exchange,observation, rejected.status, Map.of("error", rejected.getMessage()));
    } catch (OnlineStore.Conflict rejected) {
      json(exchange,observation,409,Map.of("error","account_or_session"));
    } catch (Exception error) {
      if (error instanceof InterruptedException) Thread.currentThread().interrupt();
      json(exchange,observation, 503, Map.of("error", "busy"));
    } finally {
      try { exchange.close(); }
      finally {
        try { if(permit!=null) permit.close(); }
        finally { observation.finish(); }
      }
    }
  }

  /**
   * Region-local ground loot near the viewer, nearest first. Only this region's cache is read, so drops
   * never cross regions. The owner is the viewer's own public player ID while the viewer holds the owner
   * window; other owners are reported as null. No character or account ID leaves the server.
   */
  private JsonArray groundLoot(String token,HostedWorlds.Region region) {
    var result=new JsonArray();
    if (persistent==null) return result;
    var self=region.world.position(token);
    var character=persistent.character(token).id();
    double viewerX=region.map.originX+self.x(),viewerY=region.map.originY+self.y();
    var nearest=region.loot.snapshot().stream()
        .sorted(java.util.Comparator.comparingDouble(loot -> Math.max(Math.abs(loot.x()-viewerX),Math.abs(loot.y()-viewerY))))
        .limit(GROUND_LOOT_VIEW_LIMIT).toList();
    for (var loot : nearest) {
      var entry=new JsonObject();
      boolean owned=character.equals(loot.ownerCharacter()) && loot.ownerRemainingMillis()>0;
      entry.addProperty("id",loot.id().toString());
      entry.addProperty("x",loot.x()-region.map.originX);
      entry.addProperty("y",loot.y()-region.map.originY);
      entry.addProperty("key",loot.key());
      if (owned) entry.addProperty("ownerPlayerId",self.id()); else entry.add("ownerPlayerId",JsonNull.INSTANCE);
      entry.addProperty("ownerRemainingMs",Math.max(0,loot.ownerRemainingMillis()));
      result.add(entry);
    }
    return result;
  }

  private String token(HttpExchange exchange) {
    String value = exchange.getRequestHeaders().getFirst("Authorization");
    if (value == null || !value.matches("Bearer [0-9a-f]{64}"))
      throw new PreviewWorld.Rejected(401, "session");
    return value.substring(7);
  }

  private static String peer(HttpExchange exchange) {
    return exchange.getRemoteAddress().getAddress().getHostAddress();
  }

  private static void requireAccountProtocol(HttpExchange exchange) {
    var versions=exchange.getRequestHeaders().get(OnlineProtocol.VERSION_HEADER);
    if(versions==null || versions.size()!=1 || !Integer.toString(OnlineProtocol.VERSION).equals(versions.getFirst()))
      throw new PreviewWorld.Rejected(426,"protocol");
  }

  private static String accountKey(HttpExchange exchange) {
    var authorization=exchange.getRequestHeaders().get("Authorization");
    if(authorization==null || authorization.size()!=1 || !authorization.getFirst().matches("Bearer [0-9a-f]{64}"))
      throw new PreviewWorld.Rejected(401,"account");
    return authorization.getFirst().substring(7);
  }

  private static <T> T awaitAccount(CompletableFuture<T> pending) throws Exception {
    try { return pending.get(3,TimeUnit.SECONDS); }
    catch(ExecutionException error) {
      if(error.getCause() instanceof PreviewWorld.Rejected rejected) throw rejected;
      throw error;
    }
    catch(TimeoutException timeout) {
      pending.cancel(false);throw new PreviewWorld.Rejected(503,"account_busy");
    }
  }

  private static OnlineCharacterCreation.Request parseCreation(String body) {
    try(var reader=new JsonReader(new StringReader(body))) {
      reader.setLenient(false);
      reader.beginObject();
      var seen=new java.util.HashSet<String>();
      String requestId=null,name=null,characterClass=null,gender=null;
      Boolean evil=null;
      while(reader.hasNext()) {
        String key=reader.nextName();
        if(!seen.add(key)) throw new IllegalArgumentException("Duplicate field");
        switch(key) {
          case "requestId" -> { if(reader.peek()!=JsonToken.STRING) throw new IllegalArgumentException();requestId=reader.nextString(); }
          case "name" -> { if(reader.peek()!=JsonToken.STRING) throw new IllegalArgumentException();name=reader.nextString(); }
          case "characterClass" -> { if(reader.peek()!=JsonToken.STRING) throw new IllegalArgumentException();characterClass=reader.nextString(); }
          case "gender" -> { if(reader.peek()!=JsonToken.STRING) throw new IllegalArgumentException();gender=reader.nextString(); }
          case "evil" -> { if(reader.peek()!=JsonToken.BOOLEAN) throw new IllegalArgumentException();evil=reader.nextBoolean(); }
          default -> throw new IllegalArgumentException("Unknown field");
        }
      }
      reader.endObject();
      if(reader.peek()!=JsonToken.END_DOCUMENT || seen.size()!=5 || requestId==null || name==null
          || characterClass==null || gender==null || evil==null
          || !requestId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
        throw new IllegalArgumentException("Incomplete creation");
      UUID id=UUID.fromString(requestId);
      if(!id.toString().equals(requestId)) throw new IllegalArgumentException("Noncanonical UUID");
      name=CharacterCreationRules.normalizeName(name);
      if(!CharacterCreationRules.isValidName(name) || !OnlineCharacterCreation.GENDERS.contains(gender))
        throw new IllegalArgumentException("Invalid creation");
      if(!OnlineCharacterCreation.CLASSES.contains(characterClass))
        throw new PreviewWorld.Rejected(400,"unsupported_class");
      return new OnlineCharacterCreation.Request(id,name,characterClass,gender,evil);
    } catch(PreviewWorld.Rejected rejected) { throw rejected; }
      catch(Exception malformed) { throw new PreviewWorld.Rejected(400,"creation_input"); }
  }

  private void requireLocalAdmin(HttpExchange exchange) {
    if (!exchange.getRemoteAddress().getAddress().isLoopbackAddress() || controlToken==null)
      throw new PreviewWorld.Rejected(403,"admin");
    if (!java.security.MessageDigest.isEqual(controlToken.getBytes(StandardCharsets.US_ASCII),
        token(exchange).getBytes(StandardCharsets.US_ASCII)))
      throw new PreviewWorld.Rejected(403,"admin");
  }

  private void resource(HttpExchange exchange, HttpTrafficMetrics.Request observation,
      String name, String type) throws IOException {
    try (var in = PreviewServer.class.getResourceAsStream("/preview/" + name)) {
      if (in == null) throw new IOException("Missing preview resource " + name);
      send(exchange,observation, 200, type, in.readAllBytes());
    }
  }

  private static Map<String, String> loadStrings() throws IOException {
    try (var stream = PreviewServer.class.getResourceAsStream("/i18n/lang.json")) {
      if (stream == null) throw new IOException("Missing English catalogue");
      var catalogue = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
      Map<String, String> result = new TreeMap<>();
      catalogue.entrySet().stream().filter(e -> e.getKey().startsWith("multiplayer.preview."))
          .forEach(e -> result.put(e.getKey().substring("multiplayer.preview.".length()), e.getValue().getAsString()));
      return Map.copyOf(result);
    }
  }

  private void json(HttpExchange exchange, HttpTrafficMetrics.Request observation,
      int status, Object value) throws IOException {
    send(exchange,observation, status, "application/json; charset=utf-8", JSON.toJson(value).getBytes(StandardCharsets.UTF_8));
  }

  private void send(HttpExchange exchange, HttpTrafficMetrics.Request observation,
      int status, String type, byte[] data) throws IOException {
    var headers = exchange.getResponseHeaders();
    headers.set("Content-Type", type);
    headers.set("Cache-Control", "no-store");
    headers.set("X-Content-Type-Options", "nosniff");
    headers.set("Referrer-Policy", "no-referrer");
    headers.set("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self'; connect-src 'self'; frame-ancestors 'none'");
    observation.selected(status);
    try {
      exchange.sendResponseHeaders(status, data.length);
      observation.headersSent();
      exchange.getResponseBody().write(data);
      observation.fullyWritten(data.length);
    } catch(IOException | RuntimeException incomplete) {
      observation.responseFailed();
      throw incomplete;
    }
  }

  private GameSocketServer.Result socketRequest(com.perso.T4C.online.model.GameSocketProtocol.Request request,
      String sessionToken, InetSocketAddress remote) throws Exception {
    String path="/"+request.command();
    boolean routeAllowed = request.method().equals("GET") ? GET_ROUTES.contains(path) : POST_ROUTES.contains(path);
    if (!routeAllowed || path.equals("/account/characters") || path.equals("/admin/shutdown")
        || path.equals("/admin/metrics") || path.equals("/health") || path.equals("/")
        || path.equals("/client.js") || path.equals("/style.css") || path.equals("/strings"))
      return new GameSocketServer.Result(404,"{\"error\":\"route\"}",null,false);
    boolean methodMatches = request.method().equals("POST") == POST_ROUTES.contains(path);
    if (!methodMatches) return new GameSocketServer.Result(405,"{\"error\":\"method\"}",null,false);
    var exchange=new GameSocketExchange(request,sessionToken,remote,
        new InetSocketAddress(http.getAddress().getAddress(),http.getAddress().getPort()),url());
    handle(exchange);
    int status=exchange.responseCode();
    String body=new String(exchange.responseBytes(),StandardCharsets.UTF_8);
    if (path.equals("/join") && status==200) {
      var joined=JSON.fromJson(body,RegionJoined.class);
      if(joined==null || joined.token()==null || !joined.token().matches("[0-9a-f]{64}")
          || joined.id()<=0 || joined.region()==null)
        throw new IOException("Invalid joined game session");
      return new GameSocketServer.Result(200,JSON.toJson(Map.of("id",joined.id(),"region",joined.region())),joined.token(),false);
    }
    return new GameSocketServer.Result(status,body,null,path.equals("/leave") && status==200);
  }

  private void socketDisconnected(String token) {
    if(closed.get()) return;
    try {
      if(persistent==null) onWorld(() -> { world.leave(token); return null; });
      else onWorld(() -> persistent.leave(token)).get(2,TimeUnit.SECONDS);
    } catch(Exception ignored) {
      // The normal shutdown path drains persistent sessions through HostedWorlds.close().
    }
  }
  private void requestStop() {
    boolean withoutSimulation;
    synchronized(lifecycle) {
      if(closed.compareAndSet(false,true)) { loop.close();gameSocket.close();http.stop(0);requests.shutdownNow(); }
      withoutSimulation=!started;
    }
    if(withoutSimulation) finishShutdown();
  }

  private void finishShutdown() {
    if(!disposed.compareAndSet(false,true)) return;
    try {
      try { requests.awaitTermination(2,TimeUnit.SECONDS); }
      catch(InterruptedException interrupted) { Thread.currentThread().interrupt(); }
      try { if(accounts!=null) accounts.close(); }
      finally { hosted.close(); }
    } catch(Throwable shutdownFailure) { if(failure==null) failure=shutdownFailure; }
    finally {
      try {
        if(ownsStore && store!=null) {
          if(failure!=null) System.err.println("Server shutdown failed; closing owned database pool after incomplete teardown. Recover from committed state.");
          store.close();
        }
      } catch(Throwable poolFailure) { if(failure==null) failure=poolFailure; }
      finally { terminated.countDown(); }
    }
  }

  @Override public void close() {
    requestStop();
    if(Thread.currentThread()==simulation) return;
    // Every concurrent closer waits for the same drain, including a JVM shutdown hook racing
    // the admin endpoint. Interruption cannot turn an unfinished flush into apparent success.
    boolean interrupted=false;
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(90);
    try {
      while(terminated.getCount()!=0) {
        long remaining=deadline-System.nanoTime();
        if(remaining<=0) throw new IllegalStateException("Server drain incomplete; owned pool remains open until teardown finishes");
        try { terminated.await(remaining,TimeUnit.NANOSECONDS); }
        catch(InterruptedException signal) { interrupted=true; }
      }
      if(failure!=null) throw new IllegalStateException("Server teardown reported a failure ("+
          com.perso.T4C.server.persistence.PersistenceFailure.describe(failure)+")");
    } finally { if(interrupted) Thread.currentThread().interrupt(); }
  }
}
