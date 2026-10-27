package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.helper.CharacterClass;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RegionNpcsTest {
  @Test void templeConversationIgnoresSanctuaryButNotWallsOrRegionOwnership() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled();var authored=catalog.npcs().getFirst();
    var spec=new RegionMapLoader.Spec(authored.world(),"temple",authored.x()-5,authored.y()-5,12,12,authored.x(),authored.y());
    var items=ItemCatalog.loadBundled();var npcs=new RegionNpcs(catalog,items,spec);
    var terrain=new byte[12][12]; for(var row:terrain) java.util.Arrays.fill(row,(byte)6);
    var map=new PreviewMap(new boolean[12][12],terrain,spec.originX(),spec.originY(),5,5);
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    assertFalse(map.allowsCombat(3.5,5.5,5,5,false));
    assertEquals("npc.kilhiam.letter",talk(npcs,progress,map,authored.id()).messageKey());
    assertEquals(5,npcs.views().getFirst().x());
    terrain[5][4]=1;
    var wallMap=new PreviewMap(new boolean[12][12],terrain,spec.originX(),spec.originY(),5,5);
    assertThrows(IllegalArgumentException.class,()->talk(npcs,progress,wallMap,authored.id()));
    var other=new RegionNpcs(catalog,items,new RegionMapLoader.Spec(authored.world()+1,"other",0,0,12,12,5,5));
    assertTrue(other.views().isEmpty());
    assertThrows(IllegalArgumentException.class,()->talk(other,progress,map,authored.id()));
  }

  private static OnlineNpcInteractions.Result talk(RegionNpcs npcs,
      com.perso.T4C.online.model.CharacterProgress progress,PreviewMap map,String npcId) {
    var saved=new com.perso.T4C.server.persistence.OnlineStore.CharacterState(java.util.UUID.randomUUID(),
        java.util.UUID.randomUUID(),"Tester",0,3,3,0,0,0,progress);
    return npcs.prepare(saved,map,3.5,5.5,npcId,java.util.OptionalInt.empty(),100,new Random(1));
  }

  @Test void wardenAelricIsListedOnlyInTheRegionThatContainsHisSpawn() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled(); var items=ItemCatalog.loadBundled();
    var temple=new RegionNpcs(catalog,items,new RegionMapLoader.Spec(0,"temple",2904,1029,80,60,2948,1044));
    assertTrue(temple.views().stream().anyMatch(n->n.id().equals("WardenAelric")));
    var elsewhere=new RegionNpcs(catalog,items,new RegionMapLoader.Spec(0,"elsewhere",0,0,10,10,5,5));
    assertTrue(elsewhere.views().isEmpty());
  }
}
