package com.perso.T4C.npc;

import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.monster.MonsterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
