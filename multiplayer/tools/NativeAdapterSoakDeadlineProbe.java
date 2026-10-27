import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Bounded local peer for the operator sampler's deadline and complete-body limit. */
class NativeAdapterSoakDeadlineProbe {
  private record Result(long elapsedMillis, NativeAdapterSoak.RuntimeSamples samples) {}

  public static void main(String[] args) throws Exception {
    var stalled=sample("{\"partial\":".getBytes(StandardCharsets.UTF_8),true);
    if(stalled.elapsedMillis()>3000 || stalled.samples().samples!=0 || stalled.samples().failures!=1)
      throw new AssertionError("Whole-response deadline failed");

    byte[] dense=denseMetrics();
    if(dense.length<=64*1024 || dense.length>=NativeAdapterSoak.METRICS_RESPONSE_LIMIT)
      throw new AssertionError("Dense fixture must cross the old cap and fit the new cap");
    var accepted=sample(dense,false);
    if(accepted.samples().samples!=1 || accepted.samples().failures!=0
        || accepted.samples().initial.getAsJsonObject("httpTraffic").getAsJsonArray("rows").size()!=897
        || accepted.samples().latest.getAsJsonObject("httpTraffic").getAsJsonArray("rows").size()!=897
        || accepted.samples().initial==accepted.samples().latest
        || accepted.samples().initial.getAsJsonObject("httpTraffic")
            ==accepted.samples().latest.getAsJsonObject("httpTraffic"))
      throw new AssertionError("Dense operator snapshot was not retained intact");

    byte[] oversized=("{\"padding\":\""+"x".repeat(NativeAdapterSoak.METRICS_RESPONSE_LIMIT)+"\"}")
        .getBytes(StandardCharsets.UTF_8);
    var refused=sample(oversized,false);
    if(refused.samples().samples!=0 || refused.samples().failures!=1)
      throw new AssertionError("Response above finite cap was accepted");

    System.out.println("stallMillis="+stalled.elapsedMillis()+" stallFailures=1"
        +" denseBytes="+dense.length+" denseRows=897 acceptedSamples=1"
        +" oversizedBytes="+oversized.length+" oversizedFailures=1");
  }

  private static Result sample(byte[] body,boolean stall) throws Exception {
    var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    var executor=Executors.newVirtualThreadPerTaskExecutor();
    server.createContext("/admin/metrics",exchange -> {
      try(var output=exchange.getResponseBody()) {
        exchange.sendResponseHeaders(200,stall ? 0 : body.length);
        output.write(body);
        if(stall) {
          output.flush();
          Thread.sleep(5_000);
        }
      } catch(java.io.IOException cancelled) {
        // The client deliberately cancels stalled and oversized responses.
      } catch(InterruptedException cancelled) {
        Thread.currentThread().interrupt();
      } finally { exchange.close(); }
    });
    server.setExecutor(executor);
    server.start();
    try(var client=HttpClient.newHttpClient()) {
      var samples=new NativeAdapterSoak.RuntimeSamples("a".repeat(64));
      long start=System.nanoTime();
      samples.sample(client,URI.create("http://127.0.0.1:"+server.getAddress().getPort()));
      return new Result(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start),samples);
    } finally {
      server.stop(0);
      executor.shutdownNow();
      if(!executor.awaitTermination(3,TimeUnit.SECONDS))
        throw new AssertionError("Owned probe executor did not stop");
    }
  }

  private static byte[] denseMetrics() {
    String[] routes={"/","/client.js","/style.css","/strings","/account/characters","/map",
        "/state","/character","/corpses","/health","/admin/metrics","/join",
        "/admin/shutdown","/input","/attack","/recover","/equip","/unequip",
        "/interact","/pickup","/region-ready","/leave","other"};
    String[] methods={"GET","POST","OTHER"};
    String[] statuses={"unsent","200","400","401","403","404","405","409",
        "413","415","426","503","other"};
    String large=Long.toString(Long.MAX_VALUE);
    var result=new StringBuilder(500_000);
    result.append("{\"processUptimeMillis\":1,\"simulation\":{\"completedTicks\":1},")
        .append("\"httpTraffic\":{\"completed\":897,\"rows\":[");
    boolean first=true;
    for(String route:routes) for(String method:methods) for(String status:statuses) {
      if(!first) result.append(',');
      first=false;
      result.append("{\"route\":\"").append(route).append("\",\"method\":\"")
          .append(method).append("\",\"selectedStatus\":\"").append(status)
          .append("\",\"completed\":").append(large)
          .append(",\"requestBodyBytesRead\":").append(large)
          .append(",\"responseBodyBytesFullyWritten\":").append(large)
          .append(",\"readFailures\":").append(large)
          .append(",\"responseFailures\":").append(large)
          .append(",\"headersSent\":").append(large)
          .append(",\"handlerLifetimeBuckets\":[");
      for(int bucket=0;bucket<8;bucket++) {
        if(bucket>0) result.append(',');
        result.append(large);
      }
      result.append("]}");
    }
    result.append("]}}");
    return result.toString().getBytes(StandardCharsets.UTF_8);
  }
}
