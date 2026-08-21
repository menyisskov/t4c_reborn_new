package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.companion.CompanionDef;
import com.perso.T4C.npc.companion.TamedCompanionFactory;
import org.junit.jupiter.api.Test;

class TamedCompanionFactoryTest {
  @Test
  void packWolfUsesTheSpriteBaseExpectedByNpcRendering() {
    MonsterDef wolf = MonsterRegistry.findByName("Pack Wolf");
    assertNotNull(wolf);
    CompanionDef companion = TamedCompanionFactory.fromMonster(wolf);
    assertNotNull(companion);
    assertEquals("Wolf", companion.getSpriteBase());
  }

  @Test
  void stripsMonsterFrameTerminatorFromDirectionalPatterns() {
    assertEquals("Wolf", TamedCompanionFactory.companionSpriteBase("Wolf#i"));
  }
}
