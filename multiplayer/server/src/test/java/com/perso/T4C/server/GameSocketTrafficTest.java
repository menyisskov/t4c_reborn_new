package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.online.model.GameSocketProtocol;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class GameSocketTrafficTest {
  private static final Gson JSON = new Gson();

  @Test void countingStreamsCountOnlyBytesActuallyTransferred() throws Exception {
    var traffic = new GameSocketTraffic();
    var sink = new ByteArrayOutputStream();
    try (var out = traffic.counting(sink)) {
      out.write(7);
      out.write(new byte[10], 2, 5);
    }
    try (var in = traffic.counting(new ByteArrayInputStream(new byte[9]))) {
      assertEquals(0, in.read());
      assertEquals(4, in.read(new byte[4], 0, 4));
      assertEquals(4, in.readNBytes(8).length);
      assertEquals(-1, in.read(), "End of stream is not a byte");
    }
    var snapshot = traffic.snapshot();
    assertEquals(6, snapshot.bytesWritten());
    assertEquals(9, snapshot.bytesRead());
  }

  @Test void socketCountersMatchTheFramesActuallyExchangedAndConnectionsReturnToZero() throws Exception {
    var traffic = new GameSocketTraffic();
    var handler = new GameSocketServer.Handler() {
      @Override public GameSocketServer.Result request(GameSocketProtocol.Request request, String token, InetSocketAddress remote) {
        return new GameSocketServer.Result(200, "{\"tick\":1}", null, false);
      }
      @Override public void disconnected(String token) { }
    };
    long sent = 0, received = 0;
    try (var server = new GameSocketServer(ServerTransport.local().createGameSocket(0), handler, traffic)) {
      server.start();
      try (var socket = new Socket()) {
        socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), server.port()), 1000);
        var input = new java.io.DataInputStream(socket.getInputStream());
        var output = new java.io.DataOutputStream(socket.getOutputStream());
        String hello = JSON.toJson(new GameSocketProtocol.Hello(GameSocketProtocol.VERSION));
        GameSocketProtocol.writeFrame(output, hello);
        sent += 4 + hello.getBytes(StandardCharsets.UTF_8).length;
        received += 4 + GameSocketProtocol.readFrame(input).getBytes(StandardCharsets.UTF_8).length;
        // Pre-login map bootstrap is allowed without a session; three round trips of different sizes.
        for (int id = 1; id <= 3; id++) {
          String frame = JSON.toJson(new GameSocketProtocol.Request(id, "GET", "map", "x".repeat(id * 100)));
          GameSocketProtocol.writeFrame(output, frame);
          sent += 4 + frame.getBytes(StandardCharsets.UTF_8).length;
          received += 4 + GameSocketProtocol.readFrame(input).getBytes(StandardCharsets.UTF_8).length;
        }
        var during = traffic.snapshot();
        assertEquals(1, during.connectionsAccepted());
        assertEquals(1, during.connectionsOpen());
        assertEquals(sent, during.bytesRead(), "Every frame header and payload byte the server read");
        assertEquals(received, during.bytesWritten(), "Every frame header and payload byte the server wrote");
      }
      long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
      while (traffic.snapshot().connectionsOpen() != 0 && System.nanoTime() < until) Thread.sleep(10);
      var after = traffic.snapshot();
      assertEquals(0, after.connectionsOpen(), "A closed connection is no longer open");
      assertEquals(1, after.connectionsAccepted());
      assertEquals(0, after.connectionsRefusedFull());
    }
  }
}
