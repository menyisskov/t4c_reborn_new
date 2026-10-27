package com.perso.T4C.server;

import com.google.gson.JsonParser;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import com.perso.T4C.server.persistence.OnlineStore;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Local QA only: real native adapters and PostgreSQL, deterministic HTTP application faults. */
public final class NativeNetworkFaultProbe {
  private static final long CONNECT_SECONDS=10;

  public static void main(String[] args) throws Exception {
    if(args.length>1) throw new IllegalArgumentException("Expected optional local QA port");
    int port=args.length==0 ? 18765 : Integer.parseInt(args[0]);
    if(port<1024 || port>65535) throw new IllegalArgumentException("Invalid local QA port");
    URI endpoint=URI.create("http://127.0.0.1:"+port);
    // JDK HttpServer uses seconds for these request/response limits.
    System.setProperty("sun.net.httpserver.maxReqTime","5");
    System.setProperty("sun.net.httpserver.maxRspTime","5");
    System.setProperty("jdk.httpserver.maxConnections","32");
    System.setProperty("sun.net.httpserver.maxReqHeaders","16");
    System.setProperty("sun.net.httpserver.maxReqHeaderSize","4096");
    try(var store=OnlineStore.fromEnvironment()) {
    store.migrate();
    var npc=NpcInteractionCatalog.loadBundled().require(CrimsonscaleLetterRules.NPC);
    var kit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(196));
    var owner=store.provisionAccount();var other=store.provisionAccount();
    var character=store.create(owner.accountId(),"Fault probe owner",npc.world(),npc.x()+.5,npc.y()+.5,kit.progress(),kit.gold());
    var observer=store.create(other.accountId(),"Fault probe observer",0,2944.5,1059.5,kit.progress(),kit.gold());
    var credentials=new OnlineConnection.Credentials(owner.secret(),character.id());
    try(var shim=new Shim(endpoint);
        var witness=new OnlineConnection(endpoint,new OnlineConnection.Credentials(other.secret(),observer.id()));
        var player=new OnlineConnection(shim.endpoint(),credentials)) {
      connected(player);connected(witness);
      long ownerTick=player.view().snapshot().tick(),observerTick=witness.view().snapshot().tick();
      observe(witness,3000);
      require(player.view()!=null && player.status().equals("connected") && player.view().snapshot().tick()>ownerTick,
          "Delayed native owner did not continue receiving snapshots");
      require(witness.view().snapshot().tick()>observerTick && shim.delayedReads.get()>=5,
          "Delayed reads did not exercise both live adapters");
      shim.dropNextCommittedInteraction.set(true);
      boolean ambiguous=false;
      for(int attempt=0;attempt<5 && !ambiguous;attempt++) {
        try {
          var reply=player.interact(CrimsonscaleLetterRules.NPC).toCompletableFuture().get(6,TimeUnit.SECONDS);
          require(!reply.accepted(),"Fault shim unexpectedly delivered a committed reply");
          observe(witness,150); // A normal stale-revision conflict may be explicitly attempted again.
        } catch(ExecutionException expectedUnknownOutcome) {
          require(shim.discardedReplies.get()==1,"Native command failed before the committed-reply fault");
          ambiguous=true;
        }
      }
      require(ambiguous,"No committed reply was withheld");
      int forwarded=shim.interactions.get();
      observerTick=witness.view().snapshot().tick();
      observe(witness,3000);
      require(player.view()==null && player.status().equals("disconnected"),"Ambiguous mutation did not disconnect owner");
      require(shim.interactions.get()==forwarded && shim.committedInteractions.get()==1,
          "The native adapter automatically replayed an ambiguous mutation");
      require(witness.view().snapshot().tick()>observerTick,"Observer stalled during owner failure");
      var saved=store.load(owner.accountId(),character.id());
      var letters=saved.progress().inventory().stream().filter(i->i.key().equals(CrimsonscaleLetterRules.ITEM)).toList();
      require(letters.size()==1 && letters.getFirst().quantity()==1
          && saved.progress().questFlags().getOrDefault(CrimsonscaleLetterRules.LETTER_FLAG,0)==1,
          "Lost reply did not leave exactly one committed letter and flag");
      UUID letter=letters.getFirst().id();
      player.close();
      // Normally logout has already released the lease. If it was lost, wait for actual expiry;
      // never steal or edit a live lease to manufacture a passing reconnect.
      try(var rejoined=reconnect(endpoint,credentials,witness)) {
        require(rejoined.view().character().progress().equals(saved.progress()),"Reconnect changed committed owner progress");
        var current=store.load(owner.accountId(),character.id());
        require(current.progress().equals(saved.progress()) && current.gold()==saved.gold() && current.experience()==saved.experience(),
            "Reconnect changed committed rewards");
        require(current.progress().inventory().stream().filter(i->i.id().equals(letter)).count()==1,
            "Reconnect replaced or duplicated the committed item identity");
        require(shim.interactions.get()==forwarded,"Reconnect unexpectedly replayed the failed mutation");
        require(store.load(other.accountId(),observer.id()).progress().equals(kit.progress()),"Owner fault altered observer progress");
        observe(witness,500);
      }
      System.out.println("PASS two real OnlineConnection adapters: 150ms delayed reads; one committed interaction reply withheld; owner disconnected without replay; observer kept advancing; reconnect preserved the same single letter UUID and progress. No rendered or TCP packet-loss claim.");
    }
    }
  }

  private static void connected(OnlineConnection connection) throws Exception {
    connected(connection,System.nanoTime()+TimeUnit.SECONDS.toNanos(CONNECT_SECONDS));
  }
  private static void connected(OnlineConnection connection,long deadline) throws Exception {
    while(connection.view()==null && !connection.status().equals("disconnected") && System.nanoTime()<deadline) Thread.sleep(25);
    require(connection.view()!=null,"Native adapter could not connect");
    connection.confirmRegionReady(connection.view().map().locationEpoch());
  }

  private static OnlineConnection reconnect(URI endpoint,OnlineConnection.Credentials credentials,OnlineConnection observer) throws Exception {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(40);
    while(System.nanoTime()<deadline) {
      var candidate=new OnlineConnection(endpoint,credentials);
      try { connected(candidate,Math.min(deadline,System.nanoTime()+TimeUnit.SECONDS.toNanos(CONNECT_SECONDS)));return candidate; }
      catch(AssertionError unavailable) { candidate.close();observe(observer,1000); }
      catch(Exception failure) { candidate.close();throw failure; }
    }
    throw new AssertionError("Owner lease did not permit bounded recovery");
  }

  private static void observe(OnlineConnection observer,long millis) throws Exception {
    long until=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(millis);
    long first=observer.view()==null ? -1 : observer.view().snapshot().tick();
    while(System.nanoTime()<until) {
      require(observer.view()!=null && observer.status().equals("connected"),"Observer disconnected during fault");
      Thread.sleep(50);
    }
    if(millis>=500) require(observer.view().snapshot().tick()>first,"Observer snapshots stopped advancing");
  }

  private static void require(boolean condition,String message) { if(!condition) throw new AssertionError(message); }

  private static final class Shim implements AutoCloseable {
    private static final Set<String> GETS=Set.of("/map","/state","/character","/corpses");
    private static final Set<String> POSTS=Set.of("/join","/input","/region-ready","/interact","/leave");
    final AtomicBoolean dropNextCommittedInteraction=new AtomicBoolean();
    final AtomicInteger delayedReads=new AtomicInteger(),interactions=new AtomicInteger(),
        committedInteractions=new AtomicInteger(),discardedReplies=new AtomicInteger();
    private final URI target;
    private final HttpServer server;
    private final ThreadPoolExecutor workers=new ThreadPoolExecutor(4,4,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(16),
        Thread.ofPlatform().daemon(true).name("fault-shim-",0).factory(),new ThreadPoolExecutor.AbortPolicy());
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2))
        .followRedirects(HttpClient.Redirect.NEVER).build();
    Shim(URI target) throws IOException {
      if(!"http".equals(target.getScheme()) || !"127.0.0.1".equals(target.getHost()) || target.getPort()<1024
          || target.getUserInfo()!=null || target.getRawQuery()!=null || target.getRawFragment()!=null || !target.getRawPath().isEmpty())
        throw new IllegalArgumentException("Fault shim target must be a fixed local QA origin");
      this.target=target;server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),16);
      server.setExecutor(workers);server.createContext("/",this::handle);server.start();
    }
    URI endpoint() { return URI.create("http://127.0.0.1:"+server.getAddress().getPort()); }
    private void handle(HttpExchange exchange) throws IOException {
      try {
        String path=exchange.getRequestURI().getRawPath(),method=exchange.getRequestMethod();
        if(exchange.getRequestURI().getRawQuery()!=null || !endpoint().getRawAuthority().equals(exchange.getRequestHeaders().getFirst("Host"))
            || !("GET".equals(method) && GETS.contains(path) || "POST".equals(method) && POSTS.contains(path))) {
          reject(exchange,400);return;
        }
        if("POST".equals(method) && !endpoint().toString().equals(exchange.getRequestHeaders().getFirst("Origin"))) {
          reject(exchange,403);return;
        }
        byte[] body=exchange.getRequestBody().readNBytes(129);
        if(body.length>128) { reject(exchange,413);return; }
        var request=HttpRequest.newBuilder(target.resolve(path)).timeout(Duration.ofSeconds(2));
        for(String header:List.of("Authorization",OnlineProtocol.VERSION_HEADER)) {
          var values=exchange.getRequestHeaders().get(header);
          if(values!=null) {
            if(values.size()!=1 || values.getFirst().length()>128) { reject(exchange,400);return; }
            request.header(header,values.getFirst());
          }
        }
        if("POST".equals(method)) request.header("Origin",target.toString()).header("Content-Type","text/plain")
            .POST(HttpRequest.BodyPublishers.ofByteArray(body));
        if(path.equals("/interact")) interactions.incrementAndGet();
        var pending=client.sendAsync(request.build(),ignored->new LimitedBody());
        HttpResponse<byte[]> response;
        try { response=pending.get(3,TimeUnit.SECONDS); }
        catch(Exception unavailable) { pending.cancel(true);throw unavailable; }
        if(path.equals("/interact") && response.statusCode()==200
            && JsonParser.parseString(new String(response.body(),java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject().get("changed").getAsBoolean()) {
          committedInteractions.incrementAndGet();
          if(dropNextCommittedInteraction.compareAndSet(true,false)) {
            discardedReplies.incrementAndGet();Thread.sleep(3000);exchange.close();return;
          }
        }
        if("GET".equals(method)) { delayedReads.incrementAndGet();Thread.sleep(150); }
        exchange.getResponseHeaders().set("Content-Type","application/json");
        exchange.sendResponseHeaders(response.statusCode(),response.body().length);
        try(var output=exchange.getResponseBody()) { output.write(response.body()); }
      } catch(InterruptedException interrupted) { Thread.currentThread().interrupt();exchange.close(); }
      catch(Exception unavailable) { try { reject(exchange,502); } catch(IOException closed) { exchange.close(); } }
    }
    private static void reject(HttpExchange exchange,int status) throws IOException {
      exchange.sendResponseHeaders(status,-1);exchange.close();
    }
    @Override public void close() { server.stop(0);workers.shutdownNow();client.shutdownNow(); }
  }

  private static final class LimitedBody implements HttpResponse.BodySubscriber<byte[]> {
    private final HttpResponse.BodySubscriber<byte[]> delegate=HttpResponse.BodySubscribers.ofByteArray();
    private Flow.Subscription subscription;private long received;
    @Override public CompletionStage<byte[]> getBody() { return delegate.getBody(); }
    @Override public void onSubscribe(Flow.Subscription subscription) { this.subscription=subscription;delegate.onSubscribe(subscription); }
    @Override public void onNext(List<ByteBuffer> buffers) {
      for(var buffer:buffers) received+=buffer.remaining();
      if(received>2*1024*1024) { subscription.cancel();delegate.onError(new IOException("Bounded QA reply exceeded")); }
      else delegate.onNext(buffers);
    }
    @Override public void onError(Throwable failure) { delegate.onError(failure); }
    @Override public void onComplete() { delegate.onComplete(); }
  }
}
