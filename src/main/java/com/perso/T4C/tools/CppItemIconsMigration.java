package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ItemIconBinaryIO;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regenerates {@code assets/mappings/items/item_icons.bin} from the C++ client sources.
 *
 * <p>Ports the {@code ItemIcons} table used by the buy/sell dialog: every
 * {@code ItemIcons.BindSprite(IXXX, __OBJGROUP_YYY)} call in {@code VisualObjectList.cpp}
 * becomes an {@code appearanceId -> 64kIcon*} row, resolving the {@code IXXX} sprite-name
 * macros from the same file and the {@code __OBJGROUP_*} ids from {@code Apparence.h}.
 * Bindings to the {@code "??"} placeholder are dropped: a missing row already means
 * "no generic icon" to {@link com.perso.T4C.item.ItemIconRegistry}.
 *
 * <p>Usage: {@code CppItemIconsMigration <Apparence.h> <VisualObjectList.cpp> [--dry-run]}
 */
public final class CppItemIconsMigration {
    private static final Pattern OBJECT_GROUP = Pattern.compile(
            "^\\s*const\\s+unsigned\\s+int\\s+(__OBJGROUP_[A-Za-z0-9_]+)\\s*=\\s*([0-9]+)\\s*;");
    private static final Pattern ICON_MACRO = Pattern.compile(
            "^\\s*#define\\s+(I[A-Z0-9_]+)\\s+\"([^\"]+)\"");
    private static final Pattern BIND_ICON = Pattern.compile(
            "ItemIcons\\.BindSprite\\((I[A-Z0-9_]+)\\s*,\\s*(__OBJGROUP_[A-Za-z0-9_]+|[0-9]+)\\s*\\)");

    private CppItemIconsMigration() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "Usage: CppItemIconsMigration <Apparence.h> <VisualObjectList.cpp> [--dry-run]");
        }
        boolean dryRun = args.length > 2 && "--dry-run".equals(args[2]);

        Map<String, Integer> groupIds = new HashMap<>();
        for (String line : Files.readAllLines(Path.of(args[0]), StandardCharsets.ISO_8859_1)) {
            Matcher matcher = OBJECT_GROUP.matcher(line);
            if (matcher.find()) {
                groupIds.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
            }
        }

        Map<String, String> macros = new HashMap<>();
        Map<Integer, String> icons = new TreeMap<>();
        int placeholders = 0;
        int unresolved = 0;
        for (String line : Files.readAllLines(Path.of(args[1]), StandardCharsets.ISO_8859_1)) {
            Matcher macro = ICON_MACRO.matcher(line);
            if (macro.find()) {
                macros.put(macro.group(1), macro.group(2));
            }
            Matcher bind = BIND_ICON.matcher(line);
            if (!bind.find()) {
                continue;
            }
            String token = bind.group(2);
            Integer id = token.startsWith("__OBJGROUP_")
                    ? groupIds.get(token)
                    : Integer.valueOf(token);
            String sprite = macros.get(bind.group(1));
            if (id == null || sprite == null) {
                unresolved++;
                System.out.println("UNRESOLVED " + bind.group(1) + " / " + token);
                continue;
            }
            if ("??".equals(sprite)) {
                placeholders++;
                continue;
            }
            String previous = icons.put(id, sprite);
            if (previous != null && !previous.equals(sprite)) {
                System.out.printf("DUP id=%d %s -> %s%n", id, previous, sprite);
            }
        }

        System.out.printf("groups=%d macros=%d rows=%d distinct=%d placeholders=%d unresolved=%d%n",
                groupIds.size(), macros.size(), icons.size(),
                new TreeSet<>(icons.values()).size(), placeholders, unresolved);

        if (dryRun) {
            System.out.println("dry run - rien ecrit");
            return;
        }
        ItemIconBinaryIO.write(new File(Paths.ITEM_ICONS_BIN), icons);
        System.out.println("written: " + Paths.ITEM_ICONS_BIN);
    }
}
