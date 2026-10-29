package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.GameSocketProtocol;
import com.perso.T4C.online.model.OnlineProtocol;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Bounded persistent TCP sessions for native online gameplay. */
final class GameSocketServer implements AutoCloseable {
  private static final Gson JSON = new Gson();
  private static final int MAX_CONNECTIONS = 32;
  private static final int SOCKET_IDLE_TIMEOUT_MILLIS = 10_000;

  record Result(int status, String body, String sessionToken, boolean close) {
    Result {
      if (status < 100 || status > 599 || body == null
          || sessionToken != null && !sessionToken.matches("[0-9a-f]{64}"))
        throw new IllegalArgumentException("Invalid game socket result");
    }
  }

  interface Handler {
    Result request(GameSocketProtocol.Request request, String sessionToken, InetSocketAddress remote) throws Exception;
    void disconnected(String sessionToken);
  }

  private final ServerSocket listener;
  private final Handler handler;
  private final ThreadPoolExecutor clients = new ThreadPoolExecutor(
      MAX_CONNECTIONS, MAX_CONNECTIONS, 0, TimeUnit.MILLISECONDS,
      new ArrayBlockingQueue<>(MAX_CONNECTIONS), task -> {
        var thread = new Thread(task, "online-game-socket-client");
        thread.setDaemon(true);
        return thread;
      }, new ThreadPoolExecutor.AbortPolicy());
  private final Set<Socket> sockets = java.util.concurrent.ConcurrentHashMap.newKeySet();
  private final AtomicBoolean closed = new AtomicBoolean();
  private Thread acceptor;

  GameSocketServer(ServerSocket listener, Handler handler) {
    this.listener = java.util.Objects.requireNonNull(listener);
    this.handler = java.util.Objects.requireNonNull(handler);
  }

  int port() { return listener.getLocalPort(); }

  synchronized void start() {
    if (closed.get() || acceptor != null) throw new IllegalStateException("Game socket server already started or stopped");
    acceptor = new Thread(this::accept, "online-game-socket-accept");
    acceptor.setDaemon(true);
    acceptor.start();
  }

  private void accept() {
    while (!closed.get()) {
      try {
        Socket peer = listener.accept();
        try {
          peer.setSoTimeout(SOCKET_IDLE_TIMEOUT_MILLIS);
          sockets.add(peer);
          clients.execute(() -> serve(peer));
        } catch (java.util.concurrent.RejectedExecutionException full) {
          sockets.remove(peer);
          closeQuietly(peer);
        }
      } catch (IOException failure) {
        if (!closed.get()) close();
      }
    }
  }
  private void serve(Socket socket) {
    String sessionToken = null;
    boolean normalLeave = false;
    try (socket;
         var input = new DataInputStream(socket.getInputStream());
         var output = new DataOutputStream(socket.getOutputStream())) {
      var remote = (InetSocketAddress) socket.getRemoteSocketAddress();
      String first = GameSocketProtocol.readFrame(input);
      if (first == null) return;
      GameSocketProtocol.Hello hello = JSON.fromJson(first, GameSocketProtocol.Hello.class);
      boolean compatible = hello != null && hello.protocolVersion() == OnlineProtocol.VERSION;
      GameSocketProtocol.writeFrame(output,
          JSON.toJson(new GameSocketProtocol.HelloReply(OnlineProtocol.VERSION, compatible)));
      if (!compatible) return; // Version negotiation always precedes credentials.

      long previousId = 0;
      while (!closed.get()) {
        String frame = GameSocketProtocol.readFrame(input);
        if (frame == null) return;
        GameSocketProtocol.Request request = JSON.fromJson(frame, GameSocketProtocol.Request.class);
        if (!valid(request, previousId)) return;
        previousId = request.id();
        boolean join = request.method().equals("POST") && request.command().equals("join");
        if (sessionToken == null && !join) {
          var reply = new GameSocketProtocol.Reply(request.id(), 401, "{\"error\":\"session\"}");
          GameSocketProtocol.writeFrame(output, JSON.toJson(reply));
          return;
        }
        if (sessionToken != null && join) {
          var reply = new GameSocketProtocol.Reply(request.id(), 409, "{\"error\":\"session\"}");
          GameSocketProtocol.writeFrame(output, JSON.toJson(reply));
          return;
        }
        Result result = handler.request(request, sessionToken, remote);
        if (join && result.status() == 200 && result.sessionToken() == null) return;
        GameSocketProtocol.writeFrame(output,
            JSON.toJson(new GameSocketProtocol.Reply(request.id(), result.status(), result.body())));
        if (result.sessionToken() != null) sessionToken = result.sessionToken();
        if (request.command().equals("leave") && result.status() == 200) {
          sessionToken = null;
          normalLeave = true;
          return;
        }
        if (result.close()) return;
      }
    } catch (IOException | RuntimeException ignored) {
      // A broken or malformed peer gets a closed connection, never a retried game command.
    } catch (Exception failure) {
      // Handler failures are private; connection teardown releases the bound session below.
    } finally {
      sockets.remove(socket);
      closeQuietly(socket);
      if (sessionToken != null && !normalLeave) {
        try { handler.disconnected(sessionToken); } catch (RuntimeException ignored) { }
      }
    }
  }

  private static boolean valid(GameSocketProtocol.Request request, long previousId) {
    return request != null && request.id() > previousId && request.method() != null
        && (request.method().equals("GET") || request.method().equals("POST"))
        && request.command() != null && request.command().matches("[a-z][a-z0-9-]{0,31}")
        && request.body() != null && request.body().length() <= GameSocketProtocol.MAX_FRAME_BYTES;
  }

  private static void closeQuietly(Socket socket) {
    if (socket != null) try { socket.close(); } catch (IOException ignored) { }
  }

  @Override public void close() {
    if (!closed.compareAndSet(false, true)) return;
    try { listener.close(); } catch (IOException ignored) { }
    for (Socket socket : sockets) closeQuietly(socket);
    clients.shutdownNow();
    try { clients.awaitTermination(2, TimeUnit.SECONDS); }
    catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
  }
}
