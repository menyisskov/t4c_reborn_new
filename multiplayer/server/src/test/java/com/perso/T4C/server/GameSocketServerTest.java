package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.perso.T4C.online.model.GameSocketProtocol;
import com.perso.T4C.online.model.OnlineProtocol;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class GameSocketServerTest {
  private static final Gson JSON = new Gson();

  @Test void negotiatesBeforeLoginAndKeepsTheAuthenticatedTokenOnTheSocket() throws Exception {
    String secret = "a".repeat(64);
    var authenticated = new AtomicReference<String>();
    var disconnected = new CountDownLatch(1);
    var handler = new GameSocketServer.Handler() {
      @Override public GameSocketServer.Result request(GameSocketProtocol.Request request, String token,
          InetSocketAddress remote) {
        if (request.command().equals("join")) return new GameSocketServer.Result(200,"{\"id\":7}",secret,false);
        authenticated.set(token);
        return new GameSocketServer.Result(200,"{\"tick\":1}",null,false);
      }
      @Override public void disconnected(String token) { authenticated.set(token);disconnected.countDown(); }
    };
    try (var server = new GameSocketServer(ServerTransport.local().createGameSocket(0),handler)) {
      server.start();
      try (var socket = new Socket()) {
        socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(),server.port()),1000);
        var input = new java.io.DataInputStream(socket.getInputStream());
        var output = new java.io.DataOutputStream(socket.getOutputStream());
        GameSocketProtocol.writeFrame(output,JSON.toJson(new GameSocketProtocol.Hello(OnlineProtocol.VERSION)));
        var hello = JsonParser.parseString(GameSocketProtocol.readFrame(input)).getAsJsonObject();
        assertTrue(hello.get("accepted").getAsBoolean());
        assertEquals(OnlineProtocol.VERSION,hello.get("protocolVersion").getAsInt());
        var login = new GameSocketProtocol.Request(1,"POST","join",secret);
        GameSocketProtocol.writeFrame(output,JSON.toJson(login));
        var joined = JsonParser.parseString(GameSocketProtocol.readFrame(input)).getAsJsonObject();
        assertEquals(200,joined.get("status").getAsInt());
        assertFalse(joined.get("body").getAsString().contains(secret),"Credentials never return as a bearer token");
        GameSocketProtocol.writeFrame(output,JSON.toJson(new GameSocketProtocol.Request(2,"GET","state","")));
        var state = JsonParser.parseString(GameSocketProtocol.readFrame(input)).getAsJsonObject();
        assertEquals(200,state.get("status").getAsInt());
        assertEquals(secret,authenticated.get(),"The server binds credentials to the live socket session");
      }
      assertTrue(disconnected.await(2,TimeUnit.SECONDS),"Unexpected socket loss releases the player session");
      assertEquals(secret,authenticated.get());
    }
  }

  @Test void rejectsVersionMismatchBeforeTheJoinRequestCanBeSent() throws Exception {
    var requestCount = new java.util.concurrent.atomic.AtomicInteger();
    var handler = new GameSocketServer.Handler() {
      @Override public GameSocketServer.Result request(GameSocketProtocol.Request request,String token,
          InetSocketAddress remote) {
        requestCount.incrementAndGet();
        return new GameSocketServer.Result(200,"{}",null,false);
      }
      @Override public void disconnected(String token) { }
    };
    try(var server=new GameSocketServer(ServerTransport.local().createGameSocket(0),handler)) {
      server.start();
      try(var socket=new Socket()) {
        socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(),server.port()),1000);
        var input=new java.io.DataInputStream(socket.getInputStream());
        var output=new java.io.DataOutputStream(socket.getOutputStream());
        GameSocketProtocol.writeFrame(output,JSON.toJson(new GameSocketProtocol.Hello(OnlineProtocol.VERSION-1)));
        var hello=JsonParser.parseString(GameSocketProtocol.readFrame(input)).getAsJsonObject();
        assertFalse(hello.get("accepted").getAsBoolean());
        assertEquals(0,requestCount.get());
      }
    }
  }

  @Test void previewServerRoutesJoinMovementAndLeaveThroughTheSocketSession() throws Exception {
    var map = new PreviewMap(new boolean[8][8], 0, 0, 3, 3);
    try(var server=new PreviewServer(map,0);var http=HttpClient.newHttpClient()) {
      server.start();
      var discovery=http.send(HttpRequest.newBuilder(URI.create(server.url()+"/map")).GET().build(),
          HttpResponse.BodyHandlers.ofString());
      assertEquals(200,discovery.statusCode());
      int port=JsonParser.parseString(discovery.body()).getAsJsonObject().get("gameSocketPort").getAsInt();
      assertTrue(port>0);
      try(var socket=new Socket()) {
        socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(),port),1000);
        var input=new java.io.DataInputStream(socket.getInputStream());
        var output=new java.io.DataOutputStream(socket.getOutputStream());
        GameSocketProtocol.writeFrame(output,JSON.toJson(new GameSocketProtocol.Hello(OnlineProtocol.VERSION)));
        assertTrue(JsonParser.parseString(GameSocketProtocol.readFrame(input)).getAsJsonObject().get("accepted").getAsBoolean());
        GameSocketProtocol.writeFrame(output,JSON.toJson(new GameSocketProtocol.Request(1,"POST","join","")));
        var joined=JsonParser.parseString(GameSocketProtocol.readFrame(input)).getAsJsonObject();
        assertEquals(200,joined.get("status").getAsInt());
        assertFalse(joined.get("body").getAsString().contains("token"));
        GameSocketProtocol.writeFrame(output,JSON.toJson(new GameSocketProtocol.Request(2,"GET","state","")));
        var state=JsonParser.parseString(GameSocketProtocol.readFrame(input)).getAsJsonObject();
        assertEquals(200,state.get("status").getAsInt());
        assertEquals(1,JsonParser.parseString(state.get("body").getAsString()).getAsJsonObject()
            .getAsJsonArray("players").size());
        GameSocketProtocol.writeFrame(output,JSON.toJson(new GameSocketProtocol.Request(3,"POST","input","0 1 1 0")));
        assertEquals(200,JsonParser.parseString(GameSocketProtocol.readFrame(input)).getAsJsonObject().get("status").getAsInt());
        GameSocketProtocol.writeFrame(output,JSON.toJson(new GameSocketProtocol.Request(4,"POST","leave","")));
        assertEquals(200,JsonParser.parseString(GameSocketProtocol.readFrame(input)).getAsJsonObject().get("status").getAsInt());
      }
    }
  }
}
