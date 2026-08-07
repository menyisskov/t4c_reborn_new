package com.perso.T4C.tools;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.render.SpellRenderer;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adds the LevelUpClaude spell, which carries the generated level-up animation, without replacing
 * the rest of the catalogue.
 *
 * <p>Modelled on {@code spell.stone_skin}: self-targeted, no line of sight, a single attribute
 * buff. The visual is {@link LevelUpSpriteSeed}'s effect, so casting it shows the same rising
 * column of light the character gets when it gains a level.
 */
public final class LevelUpSpellSeed {

    public static final String SPELL_KEY = "spell.level_up_claude";
    public static final String DESCRIPTION_KEY = "spell.description.level_up_claude";
    /** Must match the frames produced by {@link LevelUpSpriteSeed}. */
    public static final String IMPACT_EFFECT = SpellRenderer.LEVEL_UP_EFFECT;

    private LevelUpSpellSeed() {
    }

    public static void main(String[] args) throws Exception {
        I18n.update(Map.of(
                SPELL_KEY, "LevelUpClaude",
                DESCRIPTION_KEY,
                "Une colonne de lumière élève le lanceur, accroissant brièvement sa sagesse."));

        List<SpellData> spells = new ArrayList<>(SpellRegistry.load());
        spells.removeIf(spell -> spell != null && SPELL_KEY.equals(spell.getKey()));

        spells.add(new SpellData(
                I18n.placeholder(SPELL_KEY),
                I18n.placeholder(DESCRIPTION_KEY),
                "0",
                0, 0, 0, 1,
                false, false,
                "64kSpellIconLightBoost", null, IMPACT_EFFECT,
                0, 0,
                null, "Seraph.wav",
                0, "300000", null, 0,
                null,
                0, 0, 5, SpellData.ATTACK_MENTAL,
                "100",
                null, null, null,
                0, 0,
                false,
                // Attribute buff, encoded like spell.stone_skin: parameter 1 is unused, 2 names
                // the attribute and 3 carries its amount. Arrays.asList tolerates the null slot.
                List.of(new SpellData.T4cEffect(2, java.util.Arrays.asList(
                        new SpellData.T4cEffect.EffectParam(1, null),
                        new SpellData.T4cEffect.EffectParam(2, "wisdom"),
                        new SpellData.T4cEffect.EffectParam(3, "5+self.wis/10"))))));

        SpellRegistry.save(spells);
        System.out.println("Seeded " + SPELL_KEY + "; catalogue size=" + spells.size());
    }
}
