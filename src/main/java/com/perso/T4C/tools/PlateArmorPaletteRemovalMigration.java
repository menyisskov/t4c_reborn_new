package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ItemDefBinaryIO;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.helper.SpawnGroupBinaryIO;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Removes the palette-recolored Plate armor sprites ({@code 64kInvPlate*__palN} inventory icons and
 * {@code PupPlate*__palN} worn-body sprites), the items whose appearance references them, and every
 * monster/NPC that uses one of those items or sprites — including their spawns.
 */
public final class PlateArmorPaletteRemovalMigration {

    private PlateArmorPaletteRemovalMigration() {
    }

    public static void main(String[] args) throws Exception {
        Path spriteDir = Path.of("assets/sprites");
        removeSprites(spriteDir);

        File itemsFile = new File(Paths.ITEMS_BIN);
        List<ItemDefinition> items = ItemDefBinaryIO.read(itemsFile);
        Set<String> removedItemKeys = new HashSet<>();
        Set<Integer> removedItemNumIds = new HashSet<>();
        List<ItemDefinition> keptItems = new ArrayList<>();
        for (ItemDefinition item : items) {
            if (usesPlatePalette(item.getAppearanceInventory())
                    || usesPlatePalette(item.getAppearanceEquippedPrimary())
                    || usesPlatePalette(item.getAppearanceEquippedSecondary())) {
                removedItemKeys.add(item.getKey());
                removedItemNumIds.add(item.getNumId());
            } else {
                keptItems.add(item);
            }
        }
        ItemDefBinaryIO.write(itemsFile, keptItems);

        File monstersFile = new File(Paths.MONSTERS_BIN);
        List<MonsterDef> monsters = MonsterDefBinaryIO.read(monstersFile);
        Set<String> removedMonsterNames = new HashSet<>();
        List<MonsterDef> keptMonsters = new ArrayList<>();
        for (MonsterDef monster : monsters) {
            if (usesRemovedItem(monster, removedItemNumIds)) {
                removedMonsterNames.add(monster.getName().toLowerCase(Locale.ROOT));
            } else {
                keptMonsters.add(monster);
            }
        }
        MonsterDefBinaryIO.write(monstersFile, keptMonsters);

        File npcsFile = new File(Paths.NPCS_BIN);
        List<NpcDef> npcs = NpcDefBinaryIO.read(npcsFile);
        Set<String> removedNpcNames = new HashSet<>();
        List<NpcDef> keptNpcs = new ArrayList<>();
        for (NpcDef npc : npcs) {
            boolean uses = usesPlatePalette(npc.getSpriteBase())
                    || npc.getParts().stream().anyMatch(part -> usesPlatePalette(part.getSpriteBase()));
            if (uses) {
                removedNpcNames.add(npc.getName().toLowerCase(Locale.ROOT));
            } else {
                keptNpcs.add(npc);
            }
        }
        NpcDefBinaryIO.write(npcsFile, keptNpcs);

        int removedMonsterSpawns = pruneSpawns(new File(Paths.MONSTER_SPAWNS_BIN), removedMonsterNames);
        int removedNpcSpawns = pruneSpawns(new File(Paths.NPC_SPAWNS_BIN), removedNpcNames);
        int removedGroupRefs = pruneSpawnGroups(new File(Paths.SPAWN_GROUPS_BIN), removedMonsterNames);

        System.out.printf(
                "items_removed=%d monsters_removed=%d npcs_removed=%d monster_spawns_removed=%d "
                        + "npc_spawns_removed=%d spawn_group_refs_removed=%d%n",
                removedItemKeys.size(), removedMonsterNames.size(), removedNpcNames.size(),
                removedMonsterSpawns, removedNpcSpawns, removedGroupRefs);
    }

    private static void removeSprites(Path spriteDir) throws Exception {
        List<SpriteBinIO.Packed> sprites = SpriteBinIO.readAllToList(spriteDir, Paths.SPRITE_BIN_BASE);
        List<SpriteBinIO.Packed> kept = sprites.stream()
                .filter(sprite -> !usesPlatePalette(sprite.name()))
                .toList();
        SpriteBinIO.writeSharded(spriteDir, Paths.SPRITE_BIN_BASE, kept);
        System.out.printf("sprites_removed=%d%n", sprites.size() - kept.size());
    }

    private static boolean usesPlatePalette(String spriteName) {
        if (spriteName == null) return false;
        String lower = spriteName.toLowerCase(Locale.ROOT);
        return (lower.startsWith("64kinvplate") || lower.startsWith("pupplate")) && lower.contains("__pal");
    }

    private static boolean usesRemovedItem(MonsterDef monster, Set<Integer> removedItemNumIds) {
        return removedItemNumIds.contains(monster.getItemBody())
                || removedItemNumIds.contains(monster.getItemFeet())
                || removedItemNumIds.contains(monster.getItemHands())
                || removedItemNumIds.contains(monster.getItemHead())
                || removedItemNumIds.contains(monster.getItemLegs())
                || removedItemNumIds.contains(monster.getItemWeapon())
                || removedItemNumIds.contains(monster.getItemShield())
                || removedItemNumIds.contains(monster.getItemBack());
    }

    private static int pruneSpawns(File file, Set<String> removedNames) throws Exception {
        if (!file.exists() || removedNames.isEmpty()) return 0;
        List<SpawnBinaryIO.Entry> entries = SpawnBinaryIO.read(file);
        List<SpawnBinaryIO.Entry> kept = entries.stream()
                .filter(entry -> !removedNames.contains(entry.type.toLowerCase(Locale.ROOT)))
                .toList();
        SpawnBinaryIO.write(file, kept);
        return entries.size() - kept.size();
    }

    private static int pruneSpawnGroups(File file, Set<String> removedMonsterNames) throws Exception {
        if (!file.exists() || removedMonsterNames.isEmpty()) return 0;
        List<SpawnGroup> groups = SpawnGroupBinaryIO.read(file);
        int[] removedCount = {0};
        List<SpawnGroup> updated = groups.stream()
                .map(group -> {
                    List<String> creatures = group.getCreatures().stream()
                            .filter(creature -> !removedMonsterNames.contains(creature.toLowerCase(Locale.ROOT)))
                            .toList();
                    removedCount[0] += group.getCreatures().size() - creatures.size();
                    if (creatures.size() == group.getCreatures().size()) return group;
                    return new SpawnGroup(group.getName(), group.getTmin(), group.getTmax(),
                            group.getDistance(), group.getSpawnCount(), creatures, group.getPositions());
                })
                .filter(group -> !group.getCreatures().isEmpty())
                .toList();
        SpawnGroupBinaryIO.write(file, updated);
        return removedCount[0];
    }
}
