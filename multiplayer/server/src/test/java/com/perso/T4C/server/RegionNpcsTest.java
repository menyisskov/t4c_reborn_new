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
    assertEquals("npc.kilhiam.letter",npcs.prepare(progress,map,3.5,5.5,authored.id()).messageKey());
    assertEquals(5,npcs.views().getFirst().x());
    terrain[5][4]=1;
    var wallMap=new PreviewMap(new boolean[12][12],terrain,spec.originX(),spec.originY(),5,5);
    assertThrows(IllegalArgumentException.class,()->npcs.prepare(progress,wallMap,3.5,5.5,authored.id()));
    var other=new RegionNpcs(catalog,items,new RegionMapLoader.Spec(authored.world()+1,"other",0,0,12,12,5,5));
    assertTrue(other.views().isEmpty());
    assertThrows(IllegalArgumentException.class,()->other.prepare(progress,map,3.5,5.5,authored.id()));
  }
}
