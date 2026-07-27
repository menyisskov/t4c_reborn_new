package com.perso.T4C.monster;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ClanRelationsBinaryIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Central registry for monster clan defaults and clan hostility rules.
 */
public final class MonsterClanRelations {
    private static final Map<MonsterClan, Set<MonsterClan>> ENEMIES = new EnumMap<>(MonsterClan.class);

    static {
        reloadFromDisk();
    }

    private MonsterClanRelations() {
    }

    public static MonsterClan resolveClan(String className, String monsterName) {
        String key = ((className == null ? "" : className) + " " + (monsterName == null ? "" : monsterName))
                .toLowerCase(Locale.ROOT);

        if (key.contains("goblin")) {
            return MonsterClan.GOBLIN;
        }
        if (key.contains("horse")) {
            return MonsterClan.HORSE;
        }
        if (key.contains("cow") || key.contains("dromadary") || key.contains("pegasus")
                || key.contains("pig") || key.contains("unicorn")) {
            return MonsterClan.ANIMAL;
        }
        if (key.contains("skeleton") || key.contains("zombie") || key.contains("mummy")
                || key.contains("lich")) {
            return MonsterClan.UNDEAD;
        }
        if (key.contains("demon") || key.contains("atrocity")) {
            return MonsterClan.DEMON;
        }
        if (key.contains("draconian") || key.contains("dragon")) {
            return MonsterClan.DRACONIAN;
        }
        if (key.contains("centaur")) {
            return MonsterClan.CENTAUR;
        }
        if (key.contains("skaven")) {
            return MonsterClan.SKAVEN;
        }
        if (key.contains("kobold")) {
            return MonsterClan.KOBOLD;
        }
        if (key.contains("kraanian")) {
            return MonsterClan.KRAANIAN;
        }
        if (key.contains("orc")) {
            return MonsterClan.ORC;
        }
        if (key.contains("wasp") || key.contains("scorpion") || key.contains("spider")
                || key.contains("tarantula")) {
            return MonsterClan.INSECT;
        }
        if (key.contains("cleric") || key.contains("guard") || key.contains("mage")
                || key.contains("paysan") || key.contains("priest") || key.contains("thief")
                || key.contains("warrior") || key.contains("warrio") || key.contains("wizard")
                || key.contains("tank")) {
            return MonsterClan.HUMAN;
        }
        if (key.contains("bat") || key.contains("beholder") || key.contains("minotaur")
                || key.contains("rat") || key.contains("slime") || key.contains("snake")
                || key.contains("taunting") || key.contains("tree ent") || key.contains("treeent")
                || key.contains("troll") || key.contains("worm")) {
            return MonsterClan.BEAST;
        }
        return MonsterClan.NEUTRAL;
    }

    public static boolean areEnemies(MonsterClan source, MonsterClan target) {
        if (source == null || target == null || source == target) {
            return false;
        }
        synchronized (ENEMIES) {
            return ENEMIES.getOrDefault(source, Set.of()).contains(target);
        }
    }

    public static void reloadFromDisk() {
        File file = new File(Paths.CLAN_RELATIONS_BIN);
        synchronized (ENEMIES) {
            ENEMIES.clear();
            if (!file.exists()) {
                addDefaultRelations();
                return;
            }
            try {
                applyRelations(ClanRelationsBinaryIO.read(file));
            } catch (Exception e) {
                addDefaultRelations();
            }
        }
    }

    public static java.util.List<ClanRelationsBinaryIO.Entry> getRelations() {
        synchronized (ENEMIES) {
            java.util.List<ClanRelationsBinaryIO.Entry> entries = new ArrayList<>();
            for (Map.Entry<MonsterClan, Set<MonsterClan>> sourceEntry : ENEMIES.entrySet()) {
                for (MonsterClan target : sourceEntry.getValue()) {
                    entries.add(new ClanRelationsBinaryIO.Entry(sourceEntry.getKey(), target));
                }
            }
            entries.sort(Comparator
                    .comparing((ClanRelationsBinaryIO.Entry entry) -> entry.source.name())
                    .thenComparing(entry -> entry.target.name()));
            return entries;
        }
    }

    public static void saveRelations(java.util.List<ClanRelationsBinaryIO.Entry> relations) throws IOException {
        synchronized (ENEMIES) {
            applyRelations(relations);
            ClanRelationsBinaryIO.write(new File(Paths.CLAN_RELATIONS_BIN), getRelations());
        }
    }

    public static java.util.List<ClanRelationsBinaryIO.Entry> defaultRelations() {
        return java.util.List.of(new ClanRelationsBinaryIO.Entry(MonsterClan.GOBLIN, MonsterClan.HORSE));
    }

    private static void applyRelations(java.util.List<ClanRelationsBinaryIO.Entry> relations) {
        ENEMIES.clear();
        if (relations == null) {
            return;
        }
        for (ClanRelationsBinaryIO.Entry relation : relations) {
            if (relation == null || relation.source == null || relation.target == null
                    || relation.source == relation.target || relation.source == MonsterClan.NEUTRAL
                    || relation.target == MonsterClan.NEUTRAL) {
                continue;
            }
            addEnemy(relation.source, relation.target);
        }
    }

    private static void addDefaultRelations() {
        applyRelations(defaultRelations());
    }

    private static void addEnemy(MonsterClan source, MonsterClan target) {
        ENEMIES.computeIfAbsent(source, ignored -> EnumSet.noneOf(MonsterClan.class)).add(target);
    }
}
