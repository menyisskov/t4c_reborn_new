package com.perso.T4C.tools;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.item.ItemRegistry;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;

/** Replaces the saved inventory with the valid item keys listed by a migration manifest. */
public final class PopulateInventoryFromManifest {
    private PopulateInventoryFromManifest() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PopulateInventoryFromManifest <player-state.json> <manifest.txt>");
        }
        String stateFile = args[0];
        Path statePath = Path.of(stateFile).toAbsolutePath().normalize();
        Path manifestPath = Path.of(args[1]).toAbsolutePath().normalize();
        PlayerStateDto state = PlayerStateStore.load(stateFile);
        if (state == null) throw new IllegalStateException("Player state not found: " + statePath);

        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (String line : Files.readAllLines(manifestPath, StandardCharsets.UTF_8)) {
            String key = line.trim();
            if (!key.isEmpty() && ItemRegistry.findByKey(key) != null) keys.add(key);
        }
        List<String> requested = Files.readAllLines(manifestPath, StandardCharsets.UTF_8);
        if (keys.size() != requested.stream().map(String::trim).filter(value -> !value.isEmpty()).distinct().count()) {
            throw new IllegalStateException("Manifest contains item keys absent from items.bin");
        }

        Path backup = statePath.resolveSibling(statePath.getFileName() + ".backup-gon-items-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
        Files.copy(statePath, backup, StandardCopyOption.COPY_ATTRIBUTES);
        state.inventory = List.copyOf(keys);
        PlayerStateStore.save(stateFile, state);
        System.out.printf("Inventory replaced: newItems=%d backup=%s%n", keys.size(), backup);
    }
}
