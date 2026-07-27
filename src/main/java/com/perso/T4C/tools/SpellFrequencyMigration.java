package com.perso.T4C.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.spell.SpellData;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** One-shot v3-to-v4 spell binary enrichment from the original WDA JSON export. */
public final class SpellFrequencyMigration {
    private SpellFrequencyMigration() {
    }

    public static void main(String[] args) throws Exception {
        File source = new File(args.length > 0 ? args[0] : "assets/wda-json/spells.json");
        File target = new File(args.length > 1 ? args[1] : Paths.SPELLS_BIN);
        Map<Integer, String> frequencies = readFrequencies(source);
        List<SpellData> migrated = new ArrayList<>();
        for (SpellData spell : SpellBinaryIO.read(target)) {
            migrated.add(copy(spell, frequencies.get(spell.getSpellId())));
        }
        SpellBinaryIO.write(target, migrated);
        System.out.println("Migrated " + migrated.size() + " spells to binary v4 with timer frequencies.");
    }

    private static Map<Integer, String> readFrequencies(File source) throws Exception {
        Map<Integer, String> values = new HashMap<>();
        try (FileReader reader = new FileReader(source, StandardCharsets.UTF_8)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject object = element.getAsJsonObject();
                if (!object.has("spellId") || !object.has("frequency")) continue;
                values.put(object.get("spellId").getAsInt(), object.get("frequency").getAsString());
            }
        }
        return values;
    }

    private static SpellData copy(SpellData spell, String frequency) {
        return new SpellData(spell.getName(), spell.getDescription(), spell.getManaCost(), spell.getRadius(),
                spell.getMinInt(), spell.getMinWis(), spell.getMinLevel(), spell.isAttack(), spell.isLineOfSight(),
                spell.getIconId(), spell.getProjectileSpell(), spell.getImpactSpell(), spell.getMinDamage(),
                spell.getMaxDamage(), spell.getSound(), spell.getSoundImpact(), spell.getCooldownSeconds(),
                spell.getDuration(), frequency, spell.getPrice(), spell.getBuff(), spell.getSpellId(), spell.getElement(),
                spell.getTargetType(), spell.getAttackType(), spell.getSuccessRate(), spell.getMentalExhaustion(),
                spell.getPhysicalExhaustion(), spell.getAttackExhaustion(), spell.getVisualEffect(),
                spell.getVisualEffectTarget(), spell.isPvp(), spell.getT4cEffects());
    }
}
