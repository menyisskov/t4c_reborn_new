package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.spell.SpellData;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Aligns each spell's sound/soundImpact/projectileSpell/impactSpell with the
 * original T4C client (GoN VS2019 source), using a visualEffect-keyed table
 * extracted from VisualObjectList.cpp (LoadObject/Add/SetMonsterStats) and
 * Packet.cpp (SummonID impact dispatch).
 *
 * Table format per __SPELL_* id: cppName, sprite, sound, impactSprite, impactSound.
 * Join key is SpellData.visualEffect, which stores the original client's
 * 30000-range __SPELL_* constant (distinct from SpellData.spellId, the
 * WDA Objects table id).
 */
public final class SpellVisualsMigration {

    private SpellVisualsMigration() {
    }

    static final class CppSpellEntry {
        String cppName;
        String sprite;
        String sound;
        String impactSprite;
        String impactSound;
    }

    public static void main(String[] args) throws Exception {
        String tablePath = args.length > 0 ? args[0] : "tools_data/cpp_spell_table.json";
        boolean apply = args.length > 1 && "--apply".equals(args[1]);

        Map<String, CppSpellEntry> cppTable = loadTable(tablePath);
        Set<String> soundFiles = readSoundFileNames(new File("assets/sounds"));

        List<SpellData> current = SpellBinaryIO.read(new File("assets/spells/spells.bin"));
        List<SpellData> updated = new ArrayList<>(current.size());
        Set<String> missingSounds = new TreeSet<>();

        int changed = 0;
        int noCppMatch = 0;
        for (SpellData spell : current) {
            CppSpellEntry entry = cppTable.get(String.valueOf(spell.getVisualEffect()));
            if (entry == null) {
                updated.add(spell);
                if (spell.getVisualEffect() > 0) noCppMatch++;
                continue;
            }

            String newSound = withExtension(entry.sound);
            String newSoundImpact = withExtension(entry.impactSound);
            String newProjectile = entry.sprite;
            String newImpact = entry.impactSprite;

            checkSoundCoverage(newSound, soundFiles, missingSounds);
            checkSoundCoverage(newSoundImpact, soundFiles, missingSounds);

            boolean diff = !eq(spell.getSound(), newSound)
                    || !eq(spell.getSoundImpact(), newSoundImpact)
                    || !eq(spell.getProjectileSpell(), newProjectile)
                    || !eq(spell.getImpactSpell(), newImpact);

            if (diff) {
                changed++;
                System.out.printf(Locale.ROOT,
                        "[%d] %s (%s)%n  sound: %s -> %s%n  soundImpact: %s -> %s%n  projectileSpell: %s -> %s%n  impactSpell: %s -> %s%n",
                        spell.getSpellId(), spell.getName(), entry.cppName,
                        spell.getSound(), newSound,
                        spell.getSoundImpact(), newSoundImpact,
                        spell.getProjectileSpell(), newProjectile,
                        spell.getImpactSpell(), newImpact);
            }

            updated.add(new SpellData(
                    spell.getName(), spell.getDescription(), spell.getManaCost(),
                    spell.getRadius(), spell.getMinInt(), spell.getMinWis(), spell.getMinLevel(),
                    spell.isAttack(), spell.isLineOfSight(),
                    spell.getIconId(), newProjectile, newImpact,
                    spell.getMinDamage(), spell.getMaxDamage(),
                    newSound, newSoundImpact,
                    spell.getCooldownSeconds(), spell.getDuration(), spell.getFrequency(), spell.getPrice(),
                    spell.getBuff(),
                    spell.getSpellId(), spell.getElement(), spell.getTargetType(), spell.getAttackType(),
                    spell.getSuccessRate(),
                    spell.getMentalExhaustion(), spell.getPhysicalExhaustion(), spell.getAttackExhaustion(),
                    spell.getVisualEffect(), spell.getVisualEffectTarget(),
                    spell.isPvp(), spell.getT4cEffects()));
        }

        System.out.printf(Locale.ROOT, "%nSpell visuals: %d changed, %d without C++ match, %d total%n",
                changed, noCppMatch, current.size());
        if (!missingSounds.isEmpty()) {
            System.out.println("Sound files referenced but not found in assets/sounds (" + missingSounds.size() + "):");
            missingSounds.forEach(s -> System.out.println("  " + s));
        }

        if (apply) {
            SpellBinaryIO.write(new File("assets/spells/spells.bin"), updated);
            System.out.println("Applied: assets/spells/spells.bin rewritten.");
        } else {
            System.out.println("Dry run (pass --apply as 2nd arg to write changes).");
        }
    }

    private static boolean eq(String a, String b) {
        if (a == null || a.isEmpty()) a = null;
        if (b == null || b.isEmpty()) b = null;
        return java.util.Objects.equals(a, b);
    }

    private static String withExtension(String soundName) {
        if (soundName == null || soundName.isBlank()) return null;
        return soundName.endsWith(".wav") ? soundName : soundName + ".wav";
    }

    private static Map<String, CppSpellEntry> loadTable(String path) throws Exception {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, CppSpellEntry>>() {
        }.getType();
        try (FileReader reader = new FileReader(path)) {
            return gson.fromJson(reader, type);
        }
    }

    private static void checkSoundCoverage(String fileName, Set<String> soundFiles, Set<String> missing) {
        if (fileName == null || fileName.isBlank()) return;
        if (!soundFiles.contains(fileName.toLowerCase(Locale.ROOT))) missing.add(fileName);
    }

    private static Set<String> readSoundFileNames(File soundsDir) {
        Set<String> names = new HashSet<>();
        File[] files = soundsDir.listFiles();
        if (files == null) return names;
        for (File f : files) {
            names.add(f.getName().toLowerCase(Locale.ROOT));
        }
        return names;
    }
}
