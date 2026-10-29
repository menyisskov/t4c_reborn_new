package com.perso.T4C.online;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.online.model.GameSocketProtocol;
import com.perso.T4C.online.model.OnlineProtocol;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Test adapter: exercises the native socket client while forwarding into existing route fixtures. */
final class LoopbackGameSocket implements AutoCloseable {
  private static final Gson JSON = new Gson();
  private final URI httpOrigin;
  private final ServerSocket listener;
  private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
  private final AtomicBoolean closed = new AtomicBoolean();
  private final CountDownLatch left = new CountDownLatch(1);
  private final Thread acceptor;

  LoopbackGameSocket(com.sun.net.httpserver.HttpServer http) throws IOException {
    httpOrigin = URI.create("http://127.0.0.1:" + http.getAddress().getPort());
    listener = new ServerSocket(0, 8, InetAddress.getLoopbackAddress());
    acceptor = new Thread(this::accept, "test-game-socket-accept");
    acceptor.setDaemon(true);
    acceptor.start();
  }

  int port() { return listener.getLocalPort(); }

  private void accept() {
    while (!closed.get()) {
      try {
        Socket socket = listener.accept();
        sockets.add(socket);
        Thread worker = new Thread(() -> serve(socket), "test-game-socket-client");
        worker.setDaemon(true);
        worker.start();
      } catch (IOException stopped) {
        if (!closed.get()) close();
      }
    }
  }

  private void serve(Socket socket) {
    String token = null;
    try (socket; var input = new DataInputStream(socket.getInputStream());
        var output = new DataOutputStream(socket.getOutputStream()); var client = HttpClient.newHttpClient()) {
      socket.setSoTimeout(5_000);
      String helloFrame = GameSocketProtocol.readFrame(input);
      if (helloFrame == null) return;
      var hello = JSON.fromJson(helloFrame, GameSocketProtocol.Hello.class);
      boolean compatible = hello != null && hello.protocolVersion() == GameSocketProtocol.VERSION;
      GameSocketProtocol.writeFrame(output,
          JSON.toJson(new GameSocketProtocol.HelloReply(GameSocketProtocol.VERSION, compatible)));
      if (!compatible) return;
      long previousId = 0;
      while (!closed.get()) {
        String frame = GameSocketProtocol.readFrame(input);
        if (frame == null) return;
        var request = JSON.fromJson(frame, GameSocketProtocol.Request.class);
        if (request == null || request.id() <= previousId) return;
        previousId = request.id();
        String route="/"+request.command()+(request.query().isEmpty() ? "" : "?"+request.query());
        var builder = HttpRequest.newBuilder(httpOrigin.resolve(route))
            .timeout(Duration.ofSeconds(3)).header(OnlineProtocol.VERSION_HEADER, Integer.toString(OnlineProtocol.VERSION));
        if (request.authorization()!=null) builder.header("Authorization",request.authorization());
        else if (token != null) builder.header("Authorization", "Bearer " + token);
        if (request.method().equals("POST")) {
          String contentType=request.command().equals("account/characters")
              ? "application/json" : "text/plain";
          builder.header("Origin", httpOrigin.toString()).header("Content-Type", contentType)
              .POST(HttpRequest.BodyPublishers.ofString(request.body()));
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (request.command().equals("join") && response.statusCode() == 200) {
          JsonObject joined = JsonParser.parseString(body).getAsJsonObject();
          token = joined.remove("token").getAsString();
          body = JSON.toJson(joined);
        }
        GameSocketProtocol.writeFrame(output,
            JSON.toJson(new GameSocketProtocol.Reply(request.id(), response.statusCode(), body)));
        if (request.command().equals("leave") && response.statusCode() == 200) { left.countDown(); return; }
      }
    } catch (Exception ignored) {
      // The test fixture mirrors a real socket disconnect.
    } finally { sockets.remove(socket); }
  }

  @Override public void close() {
    if (closed.get()) return;
    try { left.await(1, TimeUnit.SECONDS); }
    catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
    if (!closed.compareAndSet(false, true)) return;
    try { listener.close(); } catch (IOException ignored) { }
    sockets.forEach(socket -> { try { socket.close(); } catch (IOException ignored) { } });
  }
}
