package com.perso.T4C.online;

import com.google.gson.Gson;
import com.perso.T4C.online.model.GameSocketProtocol;
import com.perso.T4C.online.model.OnlineProtocol;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/** One continuously open, authenticated native game session transport. */
final class GameSocketClient implements AutoCloseable {
  private static final Gson JSON = new Gson();
  private static final int CONNECT_TIMEOUT_MILLIS = 2_000;
  private static final int READ_TIMEOUT_MILLIS = 3_000;
  private final Socket socket;
  private final DataInputStream input;
  private final DataOutputStream output;
  private final AtomicLong requestIds = new AtomicLong();

  static GameSocketClient connect(URI endpoint, int port) throws IOException {
    if (port < 1 || port > 65535) throw new IOException("Server did not advertise a game socket");
    String host = endpoint.getHost();
    if (host == null) throw new IOException("Invalid game server address");
    Socket socket;
    if ("https".equalsIgnoreCase(endpoint.getScheme())) {
      SSLSocket tls = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
      try {
        tls.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
        tls.setSoTimeout(READ_TIMEOUT_MILLIS);
        SSLParameters parameters = tls.getSSLParameters();
        parameters.setEndpointIdentificationAlgorithm("HTTPS");
        parameters.setProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
        tls.setSSLParameters(parameters);
        tls.startHandshake();
      } catch (IOException failure) {
        try { tls.close(); } catch (IOException ignored) { }
        throw failure;
      }
      socket = tls;
    } else {
      try {
        if (!InetAddress.getByName(host).isLoopbackAddress())
          throw new IOException("Unencrypted game sockets are loopback-only");
      } catch (java.net.UnknownHostException invalidHost) {
        throw new IOException("Invalid loopback game server address", invalidHost);
      }
      socket = new Socket();
      socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
      socket.setSoTimeout(READ_TIMEOUT_MILLIS);
    }
    try {
      var client = new GameSocketClient(socket);
      client.negotiate();
      return client;
    } catch (IOException | RuntimeException failure) {
      try { socket.close(); } catch (IOException ignored) { }
      throw failure;
    }
  }

  private GameSocketClient(Socket socket) throws IOException {
    this.socket = socket;
    socket.setTcpNoDelay(true);
    socket.setKeepAlive(true);
    input = new DataInputStream(socket.getInputStream());
    output = new DataOutputStream(socket.getOutputStream());
  }

  private void negotiate() throws IOException {
    GameSocketProtocol.writeFrame(output, JSON.toJson(new GameSocketProtocol.Hello(OnlineProtocol.VERSION)));
    String response = GameSocketProtocol.readFrame(input);
    if (response == null) throw new IOException("Game server closed during protocol negotiation");
    GameSocketProtocol.HelloReply reply;
    try { reply = JSON.fromJson(response, GameSocketProtocol.HelloReply.class); }
    catch (RuntimeException invalid) { throw new IOException("Invalid game socket negotiation response", invalid); }
    if (reply == null || !reply.accepted() || reply.protocolVersion() != OnlineProtocol.VERSION)
      throw new IOException("Incompatible online protocol; update client and server together");
  }

  synchronized GameSocketProtocol.Reply request(String method, String command, String body) throws IOException {
    long id = requestIds.incrementAndGet();
    if (id <= 0) throw new IOException("Game socket request sequence exhausted");
    var request = new GameSocketProtocol.Request(id, method, command,
        body == null ? "" : body);
    GameSocketProtocol.writeFrame(output, JSON.toJson(request));
    String response = GameSocketProtocol.readFrame(input);
    if (response == null) throw new IOException("Game server disconnected");
    GameSocketProtocol.Reply reply;
    try { reply = JSON.fromJson(response, GameSocketProtocol.Reply.class); }
    catch (RuntimeException invalid) { throw new IOException("Invalid game socket reply", invalid); }
    if (reply == null || reply.id() != id || reply.status() < 100 || reply.status() > 599 || reply.body() == null)
      throw new IOException("Invalid or out-of-order game socket reply");
    return reply;
  }

  @Override public void close() throws IOException { socket.close(); }
}
