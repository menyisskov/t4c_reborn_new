package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MonsterCatalogTest {
  @Test void packagedDefinitionsResolveRealStartingAreaSpawnsWithoutDesktopRuntime() throws Exception {
    var content=MonsterCatalog.loadBundled();
    var map=PreviewMap.load(java.nio.file.Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var region=content.region(0,map.originX,map.originY,map.width,map.height);
    assertFalse(region.isEmpty(),"The real starting area contains authored guards");
    for (var spawn:region) {
      assertNotNull(spawn.definition(),"Starting-area actors require a known registry definition");
      var definition=content.monsters().get(spawn.definition());
      assertNotNull(definition);
      assertTrue(definition.getHealth()>0);
      assertNotNull(definition.getWalkPattern());
    }
    assertThrows(UnsupportedOperationException.class,() -> content.spawns().clear());
    assertThrows(UnsupportedOperationException.class,() -> content.monsters().clear());
  }

  @Test void unknownVersionsAndDanglingResolvedSpawnsFailBeforeStartup() throws Exception {
    var json=new Gson().toJsonTree(MonsterCatalog.loadBundled()).getAsJsonObject();
    json.addProperty("version",999);
    assertThrows(IOException.class,() -> MonsterCatalog.read(bytes(json.toString())));
    json.addProperty("version",1);
    json.getAsJsonArray("spawns").get(0).getAsJsonObject().addProperty("definition","missing-definition");
    assertThrows(IOException.class,() -> MonsterCatalog.read(bytes(json.toString())));
  }

  private static ByteArrayInputStream bytes(String value) {
    return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
  }

  @Test void classifiedResourceIsStagedWhileTheHeadlessRuntimeStillLoadsVersionOne() throws Exception {
    assertEquals(1,MonsterCatalog.loadBundled().version());
    try(var input=MonsterCatalog.class.getResourceAsStream("/content/monsters-v2.json")) {
      assertNotNull(input);
      var staged=new Gson().fromJson(new java.io.InputStreamReader(input,StandardCharsets.UTF_8),
          com.perso.T4C.online.model.MonsterContent.Classified.class);
      assertEquals(2,staged.version());
      assertTrue(staged.spawns().stream().anyMatch(s->s.ownership()==
          com.perso.T4C.online.model.MonsterContent.Ownership.NPC));
      assertThrows(IllegalArgumentException.class,()->new com.perso.T4C.online.model.MonsterContent.Classified(
          99,staged.monsters(),staged.authorities(),staged.relations(),staged.spawns()));
      var malformed=new Gson().toJsonTree(staged).getAsJsonObject();
      malformed.addProperty("version",99);
      assertThrows(RuntimeException.class,()->new Gson().fromJson(malformed,
          com.perso.T4C.online.model.MonsterContent.Classified.class));
    }
  }
}
