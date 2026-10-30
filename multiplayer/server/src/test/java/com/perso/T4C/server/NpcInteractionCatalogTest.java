package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NpcInteractionCatalogTest {
  @Test void lighthavenSamaritanIsAnExplicitlyRegisteredOnlineQuestGiver() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled();
    var samaritan=catalog.require("LighthavenSamaritan");
    assertEquals(0,samaritan.world());
    assertEquals(2931,samaritan.x());
    assertEquals(1072,samaritan.y());
    assertThrows(IllegalArgumentException.class,()->catalog.require("UnsupportedNpc"));
  }
}
