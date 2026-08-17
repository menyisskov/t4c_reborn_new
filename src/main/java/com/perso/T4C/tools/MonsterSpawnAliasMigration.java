package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.monster.MonsterDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Seeds {@code spawnAliases} on the monsters that spawn files refer to under a different name
 * than their {@code monsters.bin} entry. Replaces the alias table previously hardcoded as
 * {@code SPAWN_ALIASES} in {@code MonsterRegistry}.
 */
public final class MonsterSpawnAliasMigration {

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("Crawling Mummy", "Mummy"),
            Map.entry("Dark Synk", "Wraith Bat"),
            Map.entry("Dark Tarantula", "Tarantula"),
            Map.entry("GoblinBoss", "Goblin Chieftain"),
            Map.entry("Horse", "Wild Horse"),
            Map.entry("Kraanian", "Kraanian Worker"),
            Map.entry("KraanianFlying", "Kraanian Flyer"),
            Map.entry("Olin Haad", "OLINHAAD3"),
            Map.entry("Rat", "Brown Rat"),
            Map.entry("Xarrax", "Goblin Warlord")
    );

    private MonsterSpawnAliasMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean dryRun = args.length > 0 && "--dry-run".equals(args[args.length - 1]);
        File file = new File(Paths.MONSTERS_BIN);
        List<MonsterDef> defs = MonsterDefBinaryIO.read(file);

        List<MonsterDef> updated = new ArrayList<>(defs.size());
        List<String> missing = new ArrayList<>();
        for (MonsterDef def : defs) {
            List<String> aliasesForThis = new ArrayList<>();
            for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(def.getName())) {
                    aliasesForThis.add(entry.getKey());
                }
            }
            if (aliasesForThis.isEmpty()) {
                updated.add(def);
                continue;
            }
            updated.add(new MonsterDef(def.getName(), def.getDisplayName(), def.getHealth(), def.getMana(),
                    def.getXpPerHit(), def.getXpOnDeath(), def.getHitDamageMin(), def.getHitDamageMax(),
                    def.getRespawnTime(), def.getWalkPattern(), def.getAttackPattern(), def.getDeathPattern(),
                    def.getSoundAttack(), def.getSoundDeath(), def.getSoundHit(), def.getGoldMin(), def.getGoldMax(),
                    def.getLoot(), def.isAnimateWhileStationary(), def.getStationaryAnimationPauseSeconds(),
                    def.getStr(), def.getEnd(), def.getAgi(), def.getIntel(), def.getWill(), def.getWis(),
                    def.getLuck(), def.getResists(), def.getLevel(), def.getDodge(), def.getAcMin(), def.getAcMax(),
                    def.getAppearance(), def.getItemBody(), def.getItemFeet(), def.getItemHands(), def.getItemHead(),
                    def.getItemLegs(), def.getItemWeapon(), def.getItemShield(), def.getItemBack(), def.getAggro(),
                    def.getClan(), def.getSpeed(), def.isCanAttack(), def.getAttacks(), def.isTameable(),
                    def.getTameMaxLevel(), aliasesForThis, def.getSourceEvents()));
        }

        Map<String, MonsterDef> byName = new java.util.HashMap<>();
        for (MonsterDef def : defs) byName.put(def.getName().toLowerCase(java.util.Locale.ROOT), def);
        for (String canonical : ALIASES.values()) {
            if (!byName.containsKey(canonical.toLowerCase(java.util.Locale.ROOT))) missing.add(canonical);
        }
        if (!missing.isEmpty()) {
            System.out.println("Warning: canonical monster(s) not found, alias(es) skipped: " + missing);
        }

        if (dryRun) {
            System.out.println("(dry run) would write spawnAliases for " + ALIASES.size() + " alias entries");
            return;
        }
        MonsterDefBinaryIO.write(file, updated);
        System.out.println("Wrote " + file + " with spawnAliases for " + ALIASES.size() + " alias entries");
    }
}
