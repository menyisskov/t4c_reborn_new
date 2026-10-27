package com.perso.T4C.online;

import com.google.gson.Gson;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.online.model.OnlineRoster;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/** Read-only account discovery. Call on a worker, never the render thread; acquires no character lease. */
public final class OnlineAccountClient implements AutoCloseable {
  private static final Gson JSON=new Gson();
  private record Hello(int protocolVersion,boolean persistent) {}
  private final URI endpoint;
  private final String accountKey;
  private final HttpClient client;
  private volatile boolean closed;

  public static final class Rejected extends IOException {
    private final int statusCode;
    private Rejected(int statusCode) {
      super("Account request rejected (HTTP "+statusCode+")");this.statusCode=statusCode;
    }
    public int statusCode() { return statusCode; }
  }

  public OnlineAccountClient(URI endpoint,String accountKey) {
    this.endpoint=OnlineEndpoint.validate(endpoint);
    if(accountKey==null || !accountKey.matches("[0-9a-f]{64}"))
      throw new IllegalArgumentException("Invalid account key");
    this.accountKey=accountKey;client=OnlineEndpoint.client();
  }

  /** Explicit bounded page; the caller chooses whether to request the next page. */
  public synchronized OnlineRoster page(UUID after) throws IOException,InterruptedException {
    if(closed) throw new IOException("Account client is closed");
    // Recheck after every restart/page. Never send the account key before this anonymous gate.
    var hello=parse(read("/map",false,1024*1024),Hello.class);
    if(hello==null || hello.protocolVersion()!=OnlineProtocol.VERSION || !hello.persistent())
      throw new IOException("Incompatible account server; update client and server together");
    var roster=parse(read("/account/characters"+(after==null ? "" : "?after="+after),true,64*1024),OnlineRoster.class);
    validatePage(roster,after);
    return roster;
  }

  private static void validatePage(OnlineRoster roster,UUID after) throws IOException {
    if(roster==null || roster.characters()==null || roster.characters().size()>OnlineRoster.PAGE_SIZE)
      throw new IOException("Invalid account roster");
    String previous=after==null ? "" : after.toString();
    for(var character:roster.characters()) {
      if(character==null || character.id()==null || character.id().toString().compareTo(previous)<=0)
        throw new IOException("Invalid account roster order");
      previous=character.id().toString();
    }
    if(roster.nextAfter()!=null && (roster.characters().size()!=OnlineRoster.PAGE_SIZE
        || !roster.nextAfter().equals(roster.characters().getLast().id())))
      throw new IOException("Invalid account roster cursor");
  }

  private <T> T parse(String body,Class<T> type) throws IOException {
    try { return JSON.fromJson(body,type); }
    catch(RuntimeException invalid) { throw new IOException("Invalid account server response"); }
  }

  private String read(String path,boolean authenticated,int limit) throws IOException,InterruptedException {
    var request=HttpRequest.newBuilder(endpoint.resolve(path)).timeout(Duration.ofSeconds(2));
    if(authenticated) request.header("Authorization","Bearer "+accountKey)
        .header(OnlineProtocol.VERSION_HEADER,Integer.toString(OnlineProtocol.VERSION));
    CompletableFuture<HttpResponse<byte[]>> pending;
    try { pending=client.sendAsync(request.build(),ignored->new BoundedBody(limit)); }
    catch(RuntimeException failed) { throw new IOException("Account request unavailable"); }
    HttpResponse<byte[]> response;
    try { response=pending.get(2,TimeUnit.SECONDS); }
    catch(InterruptedException interrupted) { pending.cancel(true);throw interrupted; }
    catch(TimeoutException timeout) { pending.cancel(true);throw new IOException("Account request timed out"); }
    catch(ExecutionException|CancellationException failed) { throw new IOException("Account request unavailable"); }
    if(response.statusCode()!=200) throw new Rejected(response.statusCode());
    return new String(response.body(),StandardCharsets.UTF_8);
  }

  /** Bound streaming replies before allocating the entire body, including malformed/error bodies. */
  private static final class BoundedBody implements HttpResponse.BodySubscriber<byte[]> {
    private final int limit;
    private final HttpResponse.BodySubscriber<byte[]> delegate=HttpResponse.BodySubscribers.ofByteArray();
    private Flow.Subscription subscription;
    private long received;
    BoundedBody(int limit) { this.limit=limit; }
    @Override public CompletionStage<byte[]> getBody() { return delegate.getBody(); }
    @Override public void onSubscribe(Flow.Subscription subscription) { this.subscription=subscription;delegate.onSubscribe(subscription); }
    @Override public void onNext(List<ByteBuffer> buffers) {
      for(var buffer:buffers) received+=buffer.remaining();
      if(received>limit) { subscription.cancel();delegate.onError(new IOException("Account response exceeds limit")); }
      else delegate.onNext(buffers);
    }
    @Override public void onError(Throwable failure) { delegate.onError(failure); }
    @Override public void onComplete() { delegate.onComplete(); }
  }

  @Override public void close() { closed=true;client.shutdownNow(); }
}
