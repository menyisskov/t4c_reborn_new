package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Checks that every equipped appearance referenced by items.bin exists in sprites.bin. */
public final class EquipmentSpriteAudit {
    private EquipmentSpriteAudit() {
    }

    public static void main(String[] args) throws Exception {
        Path spriteDir = Path.of(args.length == 0 ? Paths.SPRITE_DIR : args[0]);
        Set<String> spriteNames = readNames(spriteDir);
        Set<String> missing = new LinkedHashSet<>();
        int checked = 0;
        for (ItemDefinition def : ItemRegistry.load()) {
            checked += check(def.getAppearanceEquippedPrimary(), spriteNames, missing);
            checked += check(def.getAppearanceEquippedSecondary(), spriteNames, missing);
            checked += check(def.getAppearanceInventory(), spriteNames, missing);
        }
        missing.forEach(name -> System.out.println("MISSING_SPRITE " + name));
        System.out.printf("Item sprite audit: references=%d missing=%d sprites=%d%n",
                checked, missing.size(), spriteNames.size());
        if (!missing.isEmpty()) System.exit(2);
    }

    private static int check(String base, Set<String> names, Set<String> missing) {
        if (base == null || base.isBlank()) return 0;
        String lower = base.toLowerCase(Locale.ROOT);
        boolean found = names.stream().anyMatch(name -> name.startsWith(lower));
        if (!found) missing.add(base);
        return 1;
    }

    /** Keeps only the names: PNG payloads are released as the read streams past. */
    private static Set<String> readNames(Path spriteDir) throws Exception {
        Set<String> names = new LinkedHashSet<>();
        SpriteBinIO.readAll(spriteDir, Paths.SPRITE_BIN_BASE,
                packed -> names.add(packed.name().toLowerCase(Locale.ROOT)));
        return names;
    }
}
