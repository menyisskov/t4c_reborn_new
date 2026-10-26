package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterRank;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.spawn.SpawnRegistry;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class MythicBeastsTest {
  @Test
  void highLevelBeastsHaveCompleteArtReachableLootAndOneBossSpawn() throws Exception {
    ItemJsonLoader.loadAndRegister("assets/items");
    try {
      var spriteKeys = new HashSet<String>();
      SpriteBinIO.readAll(
          Path.of("assets/sprites"),
          "sprites",
          sprite -> spriteKeys.add(SpriteBinIO.key(sprite.name())));
      for (var beast : List.of(MythicBeasts.CENTAUR, MythicBeasts.PEGASUS, MythicBeasts.UNICORN)) {
        var def = MonsterRegistry.findByName(beast.name());
        assertNotNull(def, beast.name());
        assertTrue(def.getLevel() >= 1000, beast.name());
        assertTrue(def.getHitDamageMin() > 2000, beast.name());
        assertTrue(def.getHealth() > MonsterRegistry.findByName("Arch Drake").getHealth());
        assertTrue(MonsterRank.isBoss(def.getName()), beast.name());
        assertEquals(
            1,
            SpawnRegistry.monsters().stream().filter(s -> s.type().equals(def.getName())).count(),
            beast.name());
        for (String sprite :
            List.of(beast.sprite() + "000-a", beast.sprite() + "A000-a", beast.sprite() + "C-a"))
          assertTrue(spriteKeys.contains(SpriteBinIO.key(sprite)), sprite);
        for (var drop : def.getLoot())
          assertNotNull(ItemRegistry.findByKey(drop.getItem()), drop.getItem());
      }
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }
}
