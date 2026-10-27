import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

class NativeAdapterSoakDeadlineProbe {
  public static void main(String[] args) throws Exception {
    var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/admin/metrics",exchange -> {
      exchange.sendResponseHeaders(200,0);
      try(var body=exchange.getResponseBody()) {
        body.write("{\"partial\":".getBytes(StandardCharsets.UTF_8));body.flush();
        try { Thread.sleep(5000); } catch(InterruptedException interrupted) { Thread.currentThread().interrupt(); }
      } catch(Exception ignored) {}
    });
    server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());server.start();
    try(var client=HttpClient.newHttpClient()) {
      var samples=new NativeAdapterSoak.RuntimeSamples("a".repeat(64));
      long start=System.nanoTime();
      samples.sample(client,URI.create("http://127.0.0.1:"+server.getAddress().getPort()));
      long elapsed=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);
      System.out.println("elapsedMillis="+elapsed+" samples="+samples.samples+" failures="+samples.failures);
      if(elapsed>3000 || samples.samples!=0 || samples.failures!=1) throw new AssertionError("Body deadline failed");
    } finally { server.stop(0); }
  }
}
