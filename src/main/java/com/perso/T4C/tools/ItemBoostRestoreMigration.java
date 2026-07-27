package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ItemDefBinaryIO;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Restores the WDA equipment boosts that were dropped from {@code assets/items/items.bin}.
 *
 * <p>The boosts were present until commit {@code 63a5fa4} and vanished when the file was
 * regenerated for the v6 (container loot groups) format from a source that no longer carried
 * them; the serializer itself never lost them. Because that same regeneration also brought in
 * legitimate newer values — 58 items have updated weights, prices and requirements — this tool
 * deliberately merges *only* the boost lists onto the current definitions instead of restoring
 * the old file wholesale. Every other field is taken from the current registry.
 *
 * <p>Items are matched by key. Keys missing from the donor, or already carrying boosts, are left
 * untouched.
 *
 * <p>Usage: {@code ItemBoostRestoreMigration <donor items.bin> [--dry-run]}
 */
public final class ItemBoostRestoreMigration {
    private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private ItemBoostRestoreMigration() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Usage: ItemBoostRestoreMigration <donor items.bin> [--dry-run]");
        }
        boolean dryRun = args.length > 1 && "--dry-run".equals(args[1]);

        Map<String, List<ItemDefinition.ItemBoost>> donorBoosts = new LinkedHashMap<>();
        for (ItemDefinition donor : ItemDefBinaryIO.read(new File(args[0]))) {
            if (!donor.getBoosts().isEmpty()) {
                donorBoosts.put(donor.getKey(), donor.getBoosts());
            }
        }

        List<ItemDefinition> current = ItemRegistry.load();
        int restored = 0;
        int alreadyPresent = 0;
        List<ItemDefinition> merged = new java.util.ArrayList<>(current.size());
        for (ItemDefinition item : current) {
            List<ItemDefinition.ItemBoost> boosts = donorBoosts.get(item.getKey());
            if (boosts == null || boosts.isEmpty()) {
                merged.add(item);
                continue;
            }
            if (!item.getBoosts().isEmpty()) {
                alreadyPresent++;
                merged.add(item);
                continue;
            }
            merged.add(withBoosts(item, boosts));
            restored++;
        }

        int totalBoosts = merged.stream().mapToInt(item -> item.getBoosts().size()).sum();
        System.out.printf("donorItems=%d items=%d restored=%d alreadyPresent=%d totalBoosts=%d%n",
                donorBoosts.size(), current.size(), restored, alreadyPresent, totalBoosts);

        if (dryRun) {
            System.out.println("dry run - rien ecrit");
            return;
        }

        File target = new File(Paths.ITEMS_BIN);
        File backup = new File(target.getPath() + ".backup-boosts-" + BACKUP_TIME.format(LocalDateTime.now()));
        Files.copy(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("backup: " + backup.getPath());

        ItemRegistry.save(merged);
        System.out.println("written: " + Paths.ITEMS_BIN);
    }

    private static ItemDefinition withBoosts(ItemDefinition item, List<ItemDefinition.ItemBoost> boosts) {
        return new ItemDefinition(
                item.getKey(), item.getName(),
                item.getBodyPart(), item.getAppearanceEquippedPrimary(),
                item.getSecondaryBodyPart(), item.getAppearanceEquippedSecondary(),
                item.getAppearanceInventory(),
                item.getPrice(), item.getWeight(), item.getArmorClass(),
                item.getDodgeLost(), item.getMinEnd(),
                item.getReqAttack(), item.getReqStr(), item.getReqAgi(),
                item.getMinInt(), item.getMinWis(), item.getAttackSpeed(),
                item.isUnique(), item.isBow(), item.isUnlimitedUse(),
                item.getNumId(), item.getStructure(), item.getAppearanceId(),
                item.getDmgFormula(), item.getAtkDelay(),
                item.getRadiance(), item.getNbCharges(), item.isCanSummon(),
                item.getLockName(), item.getLockDiff(), item.getSignText(),
                item.getContainerGold(), item.getGlobalRespawn(), item.getLocalRespawn(),
                item.getSpells(), boosts, item.getContainerLootGroups());
    }
}
