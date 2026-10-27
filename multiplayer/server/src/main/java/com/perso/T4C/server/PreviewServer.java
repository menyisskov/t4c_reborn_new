package com.perso.T4C.server;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.online.model.OnlineCharacterView;
import com.perso.T4C.online.model.OnlineProtocol;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Loopback-only QA transport with optional persistent sessions and no desktop dependencies. */
public final class PreviewServer implements AutoCloseable {
  private record PersistentSnapshot(long tick, java.util.List<PreviewWorld.PlayerView> players,
      long characterRevision, java.util.List<com.perso.T4C.online.model.CreatureView> creatures,com.perso.T4C.online.model.OnlineRegion region,java.util.List<com.perso.T4C.online.model.NpcView> npcs) {}
  private record RegionJoined(String token,long id,com.perso.T4C.online.model.OnlineRegion region) {}
  private static final Gson JSON = new Gson();
  private final com.perso.T4C.online.model.ItemContent items;
  private final HttpServer http;
  private final ServerTransport transport;
  private final ThreadPoolExecutor requests = new ThreadPoolExecutor(
      8, 8, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(128),
      Thread.ofPlatform().name("preview-http-", 0).factory(), new ThreadPoolExecutor.AbortPolicy());
  private final ArrayBlockingQueue<Runnable> commands = new ArrayBlockingQueue<>(128);
  private final HostedWorlds hosted;
  private final PreviewWorld world;
  private final OnlineStore store;
  private final PersistentSessions persistent;
  private final PersistentCreatures persistentCreatures;
  private final OnlineMelee melee;
  private final OnlineRetaliation retaliation;
  private final String controlToken;
  private final SimulationLoop loop;
  private final Thread simulation;
  private final AtomicBoolean closed = new AtomicBoolean();
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
    this.transport=java.util.Objects.requireNonNull(transport);
    if (controlToken!=null && !controlToken.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid control token");
    this.controlToken=controlToken;
    this.store=store;
    strings = loadStrings();
    items = ItemCatalog.loadBundled(); // Validate packaged equipment before accepting sessions.
    try { hosted=new HostedWorlds(plan,store,items,MonsterCatalog.loadBundled()); }
    catch(java.sql.SQLException recovery) { throw new IOException("Could not recover hosted regions",recovery); }
    var initial=hosted.regions.getFirst();world=initial.world;persistent=hosted.sessions;
    persistentCreatures=initial.creatures;melee=initial.melee;retaliation=initial.retaliation;
    mapJson=mapJson(initial,0);
    try { http = transport.create(port); }
    catch(IOException|RuntimeException failure) { hosted.close();throw failure; }
    loop = new SimulationLoop(PreviewWorld.TICK_RATE, tick -> {
      for (int i = 0; i < 128; i++) {
        Runnable command = commands.poll();
        if (command == null) break;
        command.run();
      }
      hosted.step();
    });
    simulation = new Thread(() -> {
      try { loop.run(Long.MAX_VALUE); }
      catch (Throwable error) { failure = error; }
      finally {
        close();
        hosted.close();
      }
    }, "preview-world");
    http.setExecutor(requests);
    http.createContext("/", this::handle);
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
    values.put("protocolVersion",OnlineProtocol.VERSION);values.put("world",region.spec.world());values.put("key",region.spec.key());values.put("locationEpoch",epoch);
    return JSON.toJson(values);
  }

  public String url() { return transport.origin(http.getAddress().getPort()); }

  public void start() { http.start(); simulation.start(); }

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

  private void handle(HttpExchange exchange) throws IOException {
    try {
      String host = exchange.getRequestHeaders().getFirst("Host");
      if (!transport.acceptsHost(host,http.getAddress().getPort()))
        throw new PreviewWorld.Rejected(403, "origin");
      String method = exchange.getRequestMethod();
      String path = exchange.getRequestURI().getRawPath();
      if (exchange.getRequestURI().getRawQuery() != null)
        throw new PreviewWorld.Rejected(400, "input");
      if ("POST".equals(method)) {
        if (!transport.acceptsOrigin(exchange.getRequestHeaders().getFirst("Origin"),http.getAddress().getPort()))
          throw new PreviewWorld.Rejected(403, "origin");
        String type = exchange.getRequestHeaders().getFirst("Content-Type");
        if (type == null || !type.toLowerCase(java.util.Locale.ROOT).startsWith("text/plain"))
          throw new PreviewWorld.Rejected(415, "input");
        if ("/join".equals(path)) {
          var versions = exchange.getRequestHeaders().get(OnlineProtocol.VERSION_HEADER);
          if (versions == null || versions.size() != 1
              || !Integer.toString(OnlineProtocol.VERSION).equals(versions.getFirst()))
            throw new PreviewWorld.Rejected(426, "protocol");
        }
        int limit=store!=null && "/join".equals(path) ? 101
            : "/interact".equals(path) ? 128
            : ("/equip".equals(path) || "/unequip".equals(path) || "/recover".equals(path)) ? 96 : 64;
        byte[] bytes = exchange.getRequestBody().readNBytes(limit+1);
        if (bytes.length > limit) throw new PreviewWorld.Rejected(413, "input");
        String body = new String(bytes, StandardCharsets.US_ASCII);
        switch (path) {
          case "/admin/shutdown" -> {
            if (!exchange.getRemoteAddress().getAddress().isLoopbackAddress()
                || controlToken==null || !java.security.MessageDigest.isEqual(
                controlToken.getBytes(StandardCharsets.US_ASCII),token(exchange).getBytes(StandardCharsets.US_ASCII)))
              throw new PreviewWorld.Rejected(403,"admin");
            if (!body.isEmpty()) throw new PreviewWorld.Rejected(400,"input");
            json(exchange,200,Map.of("stopping",true));
            new Thread(this::close,"online-admin-stop").start();
          }
          case "/join" -> {
            if (store==null) {
              if (!body.isEmpty()) throw new PreviewWorld.Rejected(400, "input");
              json(exchange, 200, onWorld(() -> { var joined=world.join();return new RegionJoined(joined.token(),joined.id(),hosted.regions.getFirst().descriptor(0)); }));
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
                json(exchange,200,onWorld(() -> new RegionJoined(joined.token(),joined.id(),persistent.region(joined.token()))));
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
            onWorld(() -> { var context=admit(token,epoch);context.world.input(token, sequence, dx, dy); return null; });
            json(exchange, 200, Map.of("ok", true));
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
            json(exchange,200,Map.of("revision",committed.character().revision()));
          }
          case "/recover" -> {
            if (persistent==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            if (!body.matches("[0-9]{1,19} [0-9]{1,19} [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
              throw new PreviewWorld.Rejected(400,"input");
            String[] parts=body.split(" "); long revision,epoch; java.util.UUID corpse;
            try { revision=Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]); corpse=java.util.UUID.fromString(parts[2]); }
            catch (IllegalArgumentException bad) { throw new PreviewWorld.Rejected(400,"input"); }
            var pending=onWorld(() -> { admit(token,epoch); return persistent.recoverCorpse(token,revision,corpse); });
            var committed=pending.toCompletableFuture().get(2,TimeUnit.SECONDS);
            json(exchange,200,Map.of("revision",committed.character().revision()));
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
            json(exchange, 200, Map.of("revision", committed.character().revision()));
          }
          case "/interact" -> {
            if(persistent==null) throw new PreviewWorld.Rejected(404,"route");
            String token=token(exchange);
            if(!body.matches("[0-9]{1,19} [0-9]{1,19} [A-Za-z][A-Za-z0-9_]{0,63}"))
              throw new PreviewWorld.Rejected(400,"input");
            String[] parts=body.split(" "); long revision,epoch;
            try { revision=Long.parseLong(parts[0]);epoch=Long.parseLong(parts[1]); }
            catch(NumberFormatException invalid) { throw new PreviewWorld.Rejected(400,"input"); }
            var pending=onWorld(()->{ admit(token,epoch);return persistent.interact(token,revision,parts[2]); });
            json(exchange,200,pending.toCompletableFuture().get(2,TimeUnit.SECONDS));
          }
          case "/region-ready" -> {
            String token=token(exchange);if(!body.matches("[0-9]{1,19}")) throw new PreviewWorld.Rejected(400,"input");
            long epoch;try { epoch=Long.parseLong(body); } catch(NumberFormatException invalid) { throw new PreviewWorld.Rejected(400,"input"); }
            onWorld(() -> { admit(token,epoch);if(persistent!=null) persistent.regionReady(token,epoch);return null; });
            json(exchange,200,Map.of("ok",true));
          }
          case "/leave" -> {
            if (!body.isEmpty()) throw new PreviewWorld.Rejected(400, "input");
            String token = token(exchange);
            if (persistent==null) onWorld(() -> { world.leave(token); return null; });
            else onWorld(() -> persistent.leave(token)).get(2,TimeUnit.SECONDS);
            json(exchange, 200, Map.of("ok", true));
          }
          default -> throw new PreviewWorld.Rejected(404, "route");
        }
      } else if ("GET".equals(method)) {
        switch (path) {
          case "/" -> resource(exchange, "index.html", "text/html; charset=utf-8");
          case "/client.js" -> resource(exchange, "client.js", "text/javascript; charset=utf-8");
          case "/style.css" -> resource(exchange, "style.css", "text/css; charset=utf-8");
          case "/strings" -> json(exchange, 200, strings);
          case "/map" -> {
            String authorization=exchange.getRequestHeaders().getFirst("Authorization");
            String description=authorization==null ? mapJson : onWorld(() -> { String token=token(exchange);var context=admit(token,null);
              return mapJson(context,persistent==null ? 0 : persistent.region(token).locationEpoch()); });
            send(exchange,200,"application/json",description.getBytes(StandardCharsets.UTF_8));
          }
          case "/state" -> {
            String token = token(exchange);
            json(exchange, 200, onWorld(() -> {
              var context=admit(token,null);var snapshot=context.world.snapshot(token);
              return new PersistentSnapshot(snapshot.tick(),snapshot.players(),persistent==null ? 0 : persistent.character(token).revision(),
                  snapshot.creatures(),context.descriptor(persistent==null ? 0 : persistent.region(token).locationEpoch()),context.npcs.views());
            }));
          }
          case "/character" -> {
            String token=token(exchange);
            if (persistent==null) throw new PreviewWorld.Rejected(404,"route");
            json(exchange,200,onWorld(() -> {
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
            json(exchange,200,found.stream().map(c -> Map.of("id",c.id(),"world",c.world(),"x",c.x(),"y",c.y(),
                "itemCount",c.items().size(),"gold",c.gold())).toList());
          }
          case "/health" -> json(exchange, 200,
              Map.of("tick", loop.completedTicks(), "mode", store==null ? "local-preview" : "local-persistent", "capacity", PreviewWorld.MAX_PLAYERS));
          default -> throw new PreviewWorld.Rejected(404, "route");
        }
      } else throw new PreviewWorld.Rejected(405, "method");
    } catch (PreviewWorld.Rejected rejected) {
      json(exchange, rejected.status, Map.of("error", rejected.getMessage()));
    } catch (OnlineStore.Conflict rejected) {
      json(exchange,409,Map.of("error","account_or_session"));
    } catch (Exception error) {
      if (error instanceof InterruptedException) Thread.currentThread().interrupt();
      json(exchange, 503, Map.of("error", "busy"));
    } finally { exchange.close(); }
  }

  private String token(HttpExchange exchange) {
    String value = exchange.getRequestHeaders().getFirst("Authorization");
    if (value == null || !value.matches("Bearer [0-9a-f]{64}"))
      throw new PreviewWorld.Rejected(401, "session");
    return value.substring(7);
  }

  private void resource(HttpExchange exchange, String name, String type) throws IOException {
    try (var in = PreviewServer.class.getResourceAsStream("/preview/" + name)) {
      if (in == null) throw new IOException("Missing preview resource " + name);
      send(exchange, 200, type, in.readAllBytes());
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

  private void json(HttpExchange exchange, int status, Object value) throws IOException {
    send(exchange, status, "application/json; charset=utf-8", JSON.toJson(value).getBytes(StandardCharsets.UTF_8));
  }

  private void send(HttpExchange exchange, int status, String type, byte[] data) throws IOException {
    var headers = exchange.getResponseHeaders();
    headers.set("Content-Type", type);
    headers.set("Cache-Control", "no-store");
    headers.set("X-Content-Type-Options", "nosniff");
    headers.set("Referrer-Policy", "no-referrer");
    headers.set("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self'; connect-src 'self'; frame-ancestors 'none'");
    exchange.sendResponseHeaders(status, data.length);
    exchange.getResponseBody().write(data);
  }

  @Override public void close() {
    if (!closed.compareAndSet(false, true)) return;
    loop.close();
    http.stop(0);
    requests.shutdownNow();
    if (Thread.currentThread() != simulation) {
      try { simulation.join(15000); }
      catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
  }
}
