package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class PreviewServerTest {
  private HttpRequest request(String url, String path, String token, String body) {
    var builder = HttpRequest.newBuilder(URI.create(url + path)).timeout(Duration.ofSeconds(5));
    if (token != null) builder.header("Authorization", "Bearer " + token);
    if (body != null) builder.header("Origin", url).header("Content-Type", "text/plain")
        .POST(HttpRequest.BodyPublishers.ofString(body));
    return builder.build();
  }
  private JsonObject object(HttpResponse<String> response) { return JsonParser.parseString(response.body()).getAsJsonObject(); }

  @Test void twentyClientsReplicateAndReconnectOverRealHttpWithoutSharingCredentials() throws Exception {
    var map = new PreviewMap(new boolean[60][80], 0, 0, 30, 30);
    try (var server = new PreviewServer(map, 0); var client = HttpClient.newHttpClient()) {
      server.start();
      String url = server.url();
      var tokens = new ArrayList<String>();
      for (int i = 0; i < PreviewWorld.MAX_PLAYERS; i++) {
        var joined = client.send(request(url, "/join", null, ""), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, joined.statusCode()); tokens.add(object(joined).get("token").getAsString());
      }
      assertEquals(409, client.send(request(url, "/join", null, ""), HttpResponse.BodyHandlers.ofString()).statusCode());
      long startTick = 0, endTick = 0;
      for (int round = 0; round < 20; round++) {
        var pending = new ArrayList<CompletableFuture<HttpResponse<String>>>();
        for (String token : tokens)
          pending.add(client.sendAsync(request(url, "/state", token, null), HttpResponse.BodyHandlers.ofString()));
        CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new)).get();
        for (var future : pending) {
          var result = future.get(); assertEquals(200, result.statusCode());
          assertEquals(PreviewWorld.MAX_PLAYERS, object(result).getAsJsonArray("players").size());
          for (String token : tokens) assertFalse(result.body().contains(token));
          endTick = object(result).get("tick").getAsLong();
        }
        if (round == 0) startTick = endTick;
      }
      assertTrue(endTick > startTick);
      var moved = client.send(request(url, "/input", tokens.getFirst(), "1 1 0"), HttpResponse.BodyHandlers.ofString());
      assertEquals(200, moved.statusCode());
      // Observe the same actor position from another authenticated client after a later server tick.
      JsonObject state;
      long deadline = System.nanoTime() + Duration.ofSeconds(3).toNanos();
      do {
        state = object(client.send(request(url, "/state", tokens.get(1), null), HttpResponse.BodyHandlers.ofString()));
      } while (state.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble() <= map.spawnX
          && System.nanoTime() < deadline);
      assertTrue(state.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble() > map.spawnX);
      assertEquals(200, client.send(request(url, "/leave", tokens.getFirst(), ""), HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(401, client.send(request(url, "/state", tokens.getFirst(), null), HttpResponse.BodyHandlers.ofString()).statusCode());
      var joined = client.send(request(url, "/join", null, ""), HttpResponse.BodyHandlers.ofString());
      assertEquals(200, joined.statusCode()); assertNotEquals(tokens.getFirst(), object(joined).get("token").getAsString());
    }
  }

  @Test void rejectsCrossOriginUnauthorizedAndMalformedCommandsAndCanRestart() throws Exception {
    var map = new PreviewMap(new boolean[8][8], 0, 0, 3, 3);
    int port;
    try (var client = HttpClient.newHttpClient()) {
      try (var server = new PreviewServer(map, 0)) {
        server.start(); String url = server.url(); port = URI.create(url).getPort();
        var foreign = HttpRequest.newBuilder(URI.create(url + "/join")).header("Origin", "https://example.org")
            .header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(403, client.send(foreign, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(401, client.send(request(url, "/state", null, null), HttpResponse.BodyHandlers.ofString()).statusCode());
        String token = object(client.send(request(url, "/join", null, ""), HttpResponse.BodyHandlers.ofString())).get("token").getAsString();
        assertEquals(400, client.send(request(url, "/input", token, "1 99 0"), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(400, client.send(request(url, "/input", token, "1 1 0 9999 9999"), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(413, client.send(request(url, "/input", token, "x".repeat(65)), HttpResponse.BodyHandlers.ofString()).statusCode());
      }
      try (var restarted = new PreviewServer(map, port)) {
        restarted.start();
        assertEquals(200, client.send(request(restarted.url(), "/health", null, null), HttpResponse.BodyHandlers.ofString()).statusCode());
      }
    }
  }
}
