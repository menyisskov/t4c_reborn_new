package com.perso.T4C.spell;

import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellCastingServiceTest {

    @Test
    void castingDoesNotRecheckLearningRequirements() throws Exception {
        Player caster = new Player();
        caster.setMana(100);
        SpellData barrier = new SpellData(
                "Barrier", "", "10", 0,
                59, 24, 21,
                false, true, "", "", "",
                0, 0, "", "", 0, "120000", 0, null);

        SpellCastingService.Result result = SpellCastingService.begin(
                new SpellCastingService.Request(
                        barrier, caster, SpellCastingService.TargetKind.SELF,
                        0f, true, false, false));

        assertTrue(result.success(), () -> "Unexpected cast failure: " + result.failure());
    }
}
