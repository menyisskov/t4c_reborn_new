package com.perso.T4C.tools;

import com.perso.T4C.monster.MonsterDef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MercenaryMonsterMigrationTest {
    @Test
    void upsertIsIdempotentAndUsesCppStats() {
        List<MonsterDef> once = MercenaryMonsterMigration.upsert(List.of());
        List<MonsterDef> twice = MercenaryMonsterMigration.upsert(once);

        assertEquals(1, twice.stream()
                .filter(definition -> MercenaryMonsterMigration.MONSTER_NAME.equals(definition.getName()))
                .count());
        MonsterDef definition = twice.get(0);
        assertEquals(116, definition.getHealth());
        assertEquals(20, definition.getClan());
        assertEquals(43, definition.getDodge());
        assertEquals("1d10+5", definition.getAttacks().get(0).getName());
        assertEquals(94, definition.getAttacks().get(0).getValue1());
    }
}
