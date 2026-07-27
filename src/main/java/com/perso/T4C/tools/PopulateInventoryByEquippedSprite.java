package com.perso.T4C.tools;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Replaces the saved inventory with one representative item per equipped sprite base. */
public final class PopulateInventoryByEquippedSprite {
    private PopulateInventoryByEquippedSprite() {
    }

    public static void main(String[] args) throws Exception {
        boolean allEquipment = java.util.Arrays.asList(args).contains("--all-equipment");
        String stateArgument = java.util.Arrays.stream(args)
                .filter(arg -> !arg.startsWith("--"))
                .findFirst().orElse(PlayerStateStore.DEFAULT_FILENAME);
        Path statePath = Path.of(stateArgument);
        PlayerStateDto state = PlayerStateStore.load(statePath.toString());
        if (state == null) throw new IllegalStateException("Player state not found: " + statePath);

        Set<String> coveredSprites = new LinkedHashSet<>();
        List<String> inventory = allEquipment && state.inventory != null
                ? new ArrayList<>(state.inventory) : new ArrayList<>();
        Set<String> existingItems = new LinkedHashSet<>(inventory);
        for (ItemDefinition def : ItemRegistry.load()) {
            if (def.getBodyPart() == null || def.getKey() == null || def.getKey().isBlank()) continue;
            String primary = clean(def.getAppearanceEquippedPrimary());
            String secondary = clean(def.getAppearanceEquippedSecondary());
            if (!allEquipment) {
                if (primary == null && secondary == null) continue;
                boolean addsSprite = (primary != null && !coveredSprites.contains(primary))
                        || (secondary != null && !coveredSprites.contains(secondary));
                if (!addsSprite) continue;
            } else if (existingItems.contains(def.getKey())) {
                continue;
            }
            inventory.add(def.getKey());
            existingItems.add(def.getKey());
            if (primary != null) coveredSprites.add(primary);
            if (secondary != null) coveredSprites.add(secondary);
        }

        Path absolute = statePath.toAbsolutePath().normalize();
        Path backup = absolute.resolveSibling(absolute.getFileName() + ".backup-inventory-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
        Files.copy(absolute, backup, StandardCopyOption.COPY_ATTRIBUTES);
        state.inventory = inventory;
        PlayerStateStore.save(statePath.toString(), state);
        System.out.printf("Inventory populated: items=%d mode=%s equippedSprites=%d backup=%s%n",
                inventory.size(), allEquipment ? "all-equipment" : "one-per-sprite",
                coveredSprites.size(), backup);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
