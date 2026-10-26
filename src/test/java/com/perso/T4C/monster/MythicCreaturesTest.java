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

class MythicCreaturesTest {
  @Test
  void largeCreatureCanBeSelectedOnItsUpperBody() throws Exception {
    var dragon = new ObsidianDragon(MythicCreatures.definition(MythicCreatures.DRAGON), 0, 0);
    assertTrue(dragon.isMouseOver(0, -150));
    assertFalse(dragon.isMouseOver(0, -230));
  }

  @Test
  void approvedCreaturesHaveDirectionalAnimationsAndResolvableBossRewards()
      throws Exception {
    ItemJsonLoader.loadAndRegister("assets/items");
    try {
      var sprites = new HashSet<String>();
      SpriteBinIO.readAll(
          Path.of("assets/sprites"),
          "sprites",
          sprite -> sprites.add(SpriteBinIO.key(sprite.name())));
      var archDrake = MonsterRegistry.findByName("Arch Drake");
      for (var creature :
          List.of(MythicCreatures.DRAGON, MythicCreatures.GRIFFIN, MythicCreatures.HYDRA)) {
        var definition = MonsterRegistry.findByName(creature.name());
        assertNotNull(definition, creature.name());
        assertTrue(definition.getLevel() > 1200, creature.name());
        assertTrue(definition.getHealth() > archDrake.getHealth(), creature.name());
        assertTrue(definition.getHitDamageMin() > archDrake.getHitDamageMax(), creature.name());
        assertTrue(MonsterRank.isBoss(definition.getName()), creature.name());
        assertEquals(
            1,
            SpawnRegistry.monsters().stream()
                .filter(spawn -> spawn.type().equals(definition.getName()))
                .count(),
            creature.name());
        for (var drop : definition.getLoot())
          assertNotNull(ItemRegistry.findByKey(drop.getItem()), drop.getItem());
        assertTrue(
            definition.getLoot().stream()
                .anyMatch(drop -> drop.getItem().equals("serious_healing_potion")),
            creature.name());
        for (String frame :
            List.of(
                creature.sprite() + "000-a",
                creature.sprite() + "090-h",
                creature.sprite() + "135-a",
                creature.sprite() + "180-h",
                creature.sprite() + "A000-a",
                creature.sprite() + "A090-h",
                creature.sprite() + "A135-a",
                creature.sprite() + "A180-h",
                creature.sprite() + "C-a",
                creature.sprite() + "C-h"))
          assertTrue(sprites.contains(SpriteBinIO.key(frame)), frame);
      }
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }
}
