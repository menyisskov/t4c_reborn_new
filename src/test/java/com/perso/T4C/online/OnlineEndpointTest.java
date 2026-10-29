package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import org.junit.jupiter.api.Test;

class OnlineEndpointTest {
  @Test void permitsTlsOriginsAndLiteralLocalDevelopmentOnly() {
    assertEquals(URI.create("https://game.example:18443"),
        OnlineEndpoint.validate(URI.create("https://game.example:18443/")));
    assertEquals(URI.create("http://127.0.0.1:18765"),
        OnlineEndpoint.validate(URI.create("http://127.0.0.1:18765")));
    assertThrows(IllegalArgumentException.class,
        () -> OnlineEndpoint.validate(URI.create("http://game.example:18765")));
    assertThrows(IllegalArgumentException.class,
        () -> OnlineEndpoint.validate(URI.create("https://user:secret@game.example")));
    assertThrows(IllegalArgumentException.class,
        () -> OnlineEndpoint.validate(URI.create("https://game.example/login?secret=value")));
  }

  @Test void defaultGameSocketPortIsAdjacentToTheConfiguredOrigin() throws Exception {
    assertEquals(444,GameSocketClient.defaultPort(URI.create("https://game.example")));
    assertEquals(18766,GameSocketClient.defaultPort(URI.create("http://127.0.0.1:18765")));
    assertThrows(java.io.IOException.class,
        () -> GameSocketClient.defaultPort(URI.create("https://game.example:65535")));
  }
}
