package com.perso.T4C.spell;

import com.perso.T4C.helper.SpellBinaryIO;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntangleSpellTest {
    @Test
    void catalogueContainsGonMovementExhaustEffect() throws Exception {
        SpellData effect = SpellBinaryIO.read(new File("assets/spells/spells.bin")).stream()
                .filter(spell -> spell.getSpellId() == 10349).findFirst().orElse(null);
        assertNotNull(effect);
        assertEquals("1750", effect.getPhysicalExhaustion());
    }

}
