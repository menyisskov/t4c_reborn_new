package com.perso.T4C.tools;

import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Sets standard T4C spell prices and estimates prices absent from the reference table. */
public final class SpellPriceMigration {
    private static final Map<String, Integer> STANDARD_PRICES = Map.ofEntries(
            Map.entry("spell.light", 233),
            Map.entry("spell.fire_dart", 532),
            Map.entry("spell.heal_light", 897),
            Map.entry("spell.stone_shard", 1328),
            Map.entry("spell.cure_poison", 1825),
            Map.entry("spell.dust_devil", 2388),
            Map.entry("spell.poison", 3017),
            Map.entry("spell.curse", 17200),
            Map.entry("spell.ice_shard", 17200),
            Map.entry("spell.protection", 3712),
            Map.entry("spell.barrier", 18753),
            Map.entry("spell.word_of_recall", 18753),
            Map.entry("spell.flaming_arrow", 20372),
            Map.entry("spell.lesser_drain", 7152),
            Map.entry("spell.drain_life", 22057),
            Map.entry("spell.strength", 22057),
            Map.entry("spell.heal_serious", 8177),
            Map.entry("spell.glacier", 110825),
            Map.entry("spell.heal_critical", 110825),
            Map.entry("spell.detect_invisible", 92500),
            Map.entry("spell.sanctuary", 99632),
            Map.entry("spell.rain_of_fire", 103297),
            Map.entry("spell.mana_surge", 107028),
            Map.entry("spell.invisibility", 122612),
            Map.entry("spell.bless", 126673),
            Map.entry("spell.shatter", 14292)
    );

    private SpellPriceMigration() { }

    public static void main(String[] args) throws Exception {
        List<SpellData> updated = new ArrayList<>();
        for (SpellData spell : SpellRegistry.load()) {
            if (spell == null) continue;
            int price = STANDARD_PRICES.getOrDefault(spell.getKey(), estimate(spell.getMinLevel()));
            SpellData updatedSpell = spell.withPrice(price);
            if ("spell.lesser_drain".equals(spell.getKey())) {
                List<SpellData.T4cEffect> effects = new ArrayList<>();
                effects.add(new SpellData.T4cEffect(10, List.of(
                        new SpellData.T4cEffect.EffectParam(1,
                                "-( ( 1d21 + 13 + self.int/18 ) * self.dark/target.r_dark )"))));
                updatedSpell = updatedSpell.withT4cEffects(effects);
            }
            updated.add(updatedSpell);
            System.out.printf("%-32s %d%n", spell.getKey(), price);
        }
        SpellRegistry.save(updated);
    }

    private static int estimate(int level) {
        int safeLevel = Math.max(2, level);
        return Math.max(233, (int) Math.round(233 + 200 * Math.pow(safeLevel - 2, 1.6)));
    }
}
