package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The optional public look of a snapshot actor: decoding, older servers and bounds. */
class OnlineActorLookTest {
  private static OnlineConnection.Actor actor(String name, String gender, Map<String, String> equipment) {
    return new OnlineConnection.Actor(1, 1, 1, 0, 0, 5, 5, name, gender, equipment);
  }

  @Test
  void anActorFromAnOlderServerDecodesWithoutALook() {
    var decoded =
        new Gson()
            .fromJson(
                "{\"id\":7,\"x\":1.5,\"y\":2.5,\"sequence\":0,\"appearance\":1,\"hp\":3,\"maxHp\":9}",
                OnlineConnection.Actor.class);
    assertNull(decoded.name());
    assertNull(decoded.gender());
    assertNull(decoded.equipment());
    assertTrue(decoded.lookValid());
  }

  @Test
  void aPublicLookRoundTripsThroughTheWireJson() {
    var gson = new Gson();
    var sent = actor("Brindle", "female", Map.of("BODY", "item.acolytes_dawnlit_robe"));
    var back = gson.fromJson(gson.toJson(sent), OnlineConnection.Actor.class);
    assertEquals(sent, back);
    assertTrue(back.lookValid());
  }

  @Test
  void malformedOrOversizedLooksAreRejected() {
    assertFalse(actor("", "male", null).lookValid());
    assertFalse(actor("x".repeat(65), "male", null).lookValid());
    assertFalse(actor("A", "robot", null).lookValid());
    assertFalse(actor("A", "male", Map.of("NOT_A_SLOT", "item.x")).lookValid());
    assertFalse(actor("A", "male", Map.of("BODY", "item." + "x".repeat(260))).lookValid());
    var tooMany = new HashMap<String, String>();
    for (int i = 0; i < 40; i++) tooMany.put("BODY" + i, "item.x");
    assertFalse(actor("A", "male", tooMany).lookValid());
    var nullValue = new HashMap<String, String>();
    nullValue.put("BODY", null);
    assertFalse(actor("A", "male", nullValue).lookValid());
  }
}
