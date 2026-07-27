package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

/** Imports every object from a text file produced by Decompiler_T4C_172. */
public final class WdaItemDefinitionMigration {
    private static final Map<String, String> EXPLICIT_GAMEPLAY_DONORS = Map.of(
            // Monster dressing key; AWH shield is its low-level playable variant.
            "Skeleton shield", "AWH shield"
    );

    private WdaItemDefinitionMigration() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1) throw new IllegalArgumentException(
                "Usage: WdaItemDefinitionMigration <decompiled-edit.txt> [--only-new] [--dry-run] [--manifest=<file>]");
        Set<String> options = new LinkedHashSet<>(List.of(args));
        boolean onlyNew = options.contains("--only-new");
        boolean dryRun = options.contains("--dry-run");
        Path manifest = options.stream().filter(value -> value.startsWith("--manifest="))
                .map(value -> Path.of(value.substring("--manifest=".length())))
                .findFirst().orElse(null);
        List<ItemDefinition> current = ItemRegistry.load();
        Map<String, ItemDefinition> merged = new LinkedHashMap<>();
        for (ItemDefinition definition : current) merged.put(definition.getKey(), definition);

        Map<Integer, String> inventorySprites = Files.exists(Path.of("assets/wda-json/appearance_id_to_inv_sprite.json"))
                ? loadSprites("assets/wda-json/appearance_id_to_inv_sprite.json") : Map.of();
        Map<String, List<ItemDefinition.ItemBoost>> boosts = Files.exists(Path.of("assets/wda-json/objects.json"))
                ? loadBoosts("assets/wda-json/objects.json") : Map.of();
        int added = 0;
        int updated = 0;
        List<String> addedKeys = new ArrayList<>();
        for (Map<String, String> raw : parseObjects(Path.of(args[0]))) {
            String key = raw.get("ID string");
            if (key == null || key.isBlank()) continue;
            ItemDefinition previous = merged.get(key);
            if (onlyNew && previous != null) continue;
            ItemDefinition imported = convert(raw, previous, inventorySprites, boosts.getOrDefault(key, List.of()));
            merged.put(key, imported);
            if (previous == null) {
                added++;
                addedKeys.add(key);
            } else updated++;
        }
        int enriched = dryRun ? 0 : enrichAppearanceClones(merged);
        if (!dryRun) ItemRegistry.save(new ArrayList<>(merged.values()));
        if (manifest != null) {
            if (manifest.getParent() != null) Files.createDirectories(manifest.getParent());
            Files.write(manifest, addedKeys, Charset.forName("UTF-8"));
        }
        System.out.printf(Locale.ROOT, "WDA items migrated: %d updated, %d added, %d appearance clones enriched, %d total%n",
                updated, added, enriched, merged.size());
    }

    /**
     * The WDA contains zero-stat monster dressing objects beside their playable
     * counterpart (for example Skeleton shield / AWH shield). When exactly one
     * stat-bearing item shares the appearance and slot, inherit its gameplay
     * values without changing the cosmetic key or sprites.
     */
    static int enrichAppearanceClones(Map<String, ItemDefinition> definitions) {
        int changed = 0;
        for (Map.Entry<String, String> alias : EXPLICIT_GAMEPLAY_DONORS.entrySet()) {
            ItemDefinition target = definitions.get(alias.getKey());
            ItemDefinition donor = definitions.get(alias.getValue());
            if (target != null && donor != null && hasZeroGameplayStats(target)) {
                definitions.put(alias.getKey(), copyGameplayStats(target, donor));
                System.out.printf("Explicit item inheritance: %s <- %s%n", alias.getKey(), alias.getValue());
                changed++;
            }
        }
        return changed;
    }

    private static boolean hasZeroGameplayStats(ItemDefinition d) {
        return d.getArmorClass() == 0d && d.getDodgeLost() == 0 && d.getMinEnd() == 0
                && d.getReqAttack() == 0 && d.getReqStr() == 0 && d.getReqAgi() == 0
                && d.getMinInt() == 0 && d.getMinWis() == 0
                && (d.getDmgFormula() == null || d.getDmgFormula().isBlank());
    }

    private static ItemDefinition copyGameplayStats(ItemDefinition target, ItemDefinition donor) {
        return new ItemDefinition(target.getKey(), target.getName(), target.getBodyPart(),
                target.getAppearanceEquippedPrimary(), target.getSecondaryBodyPart(),
                target.getAppearanceEquippedSecondary(), target.getAppearanceInventory(),
                donor.getPrice(), donor.getWeight(), donor.getArmorClass(), donor.getDodgeLost(),
                donor.getMinEnd(), donor.getReqAttack(), donor.getReqStr(), donor.getReqAgi(),
                donor.getMinInt(), donor.getMinWis(), donor.getAttackSpeed(),
                target.isUnique(), target.isBow(), target.isUnlimitedUse(),
                target.getNumId(), target.getStructure(), target.getAppearanceId(),
                donor.getDmgFormula(), donor.getAtkDelay(), target.getRadiance(), target.getNbCharges(),
                target.isCanSummon(), target.getLockName(), target.getLockDiff(), target.getSignText(),
                target.getContainerGold(), target.getGlobalRespawn(), target.getLocalRespawn(), target.getSpells(),
                target.getBoosts(), target.getContainerLootGroups());
    }

    static List<Map<String, String>> parseObjects(Path file) throws Exception {
        // The historical decompiler writes ANSI text.
        List<String> lines = Files.readAllLines(file, Charset.forName("windows-1252"));
        List<Map<String, String>> objects = new ArrayList<>();
        Map<String, String> current = null;
        for (String sourceLine : lines) {
            String line = sourceLine.trim();
            if ("[object]".equalsIgnoreCase(line)) {
                if (current != null) objects.add(current);
                current = new LinkedHashMap<>();
                continue;
            }
            if (line.startsWith("[") && current != null) {
                objects.add(current);
                current = null;
                continue;
            }
            if (current == null || line.isEmpty() || line.startsWith("#")) continue;
            int equals = line.indexOf('=');
            if (equals < 1) continue;
            String name = line.substring(0, equals).trim();
            String value = stripComment(line.substring(equals + 1).trim());
            current.put(name, unquote(value));
        }
        if (current != null) objects.add(current);
        return objects;
    }

    private static ItemDefinition convert(Map<String, String> raw, ItemDefinition old,
                                          Map<Integer, String> inventorySprites,
                                          List<ItemDefinition.ItemBoost> boosts) {
        String key = raw.get("ID string");
        String name = value(raw, "name", old == null ? key : old.getName());
        int appearance = integer(raw, "skin ID", old == null ? 0 : old.getAppearanceId());
        String inventorySprite = old == null ? inventorySprites.get(appearance) : old.getAppearanceInventory();
        String equippedPrimary = old == null ? null : old.getAppearanceEquippedPrimary();
        BodyPart secondary = old == null ? null : old.getSecondaryBodyPart();
        String equippedSecondary = old == null ? null : old.getAppearanceEquippedSecondary();
        BodyPart slot = bodyPart(raw.get("equipment position"), key, equippedPrimary, inventorySprite);
        String delay = nullable(raw.get("delay between strikes"));

        return new ItemDefinition(key, name, slot, equippedPrimary, secondary, equippedSecondary, inventorySprite,
                decimalLong(raw, "resell price", decimalLong(raw, "resell price old", old == null ? 0 : old.getPrice())),
                decimalLong(raw, "weight", old == null ? 0 : old.getWeight()),
                decimal(raw, "armor class", old == null ? 0 : old.getArmorClass()),
                decimalLong(raw, "dodge malus", old == null ? 0 : old.getDodgeLost()),
                decimalLong(raw, "minimum endurance", old == null ? 0 : old.getMinEnd()),
                decimalLong(raw, "minimum attack", old == null ? 0 : old.getReqAttack()),
                decimalLong(raw, "minimum strength", old == null ? 0 : old.getReqStr()),
                decimalLong(raw, "minimum agility", old == null ? 0 : old.getReqAgi()),
                decimalLong(raw, "minimum intelligence", old == null ? 0 : old.getMinInt()),
                decimalLong(raw, "minimum wisdom", old == null ? 0 : old.getMinWis()),
                old == null ? attackSpeed(delay) : old.getAttackSpeed(),
                bool(raw, "is unique", old != null && old.isUnique()),
                bool(raw, "is ranged weapon", old != null && old.isBow()),
                bool(raw, "unlimited use", old != null && old.isUnlimitedUse()),
                integer(raw, "ID number", old == null ? 0 : old.getNumId()),
                structure(raw.get("structure")), appearance,
                nullable(raw.get("damage formula")), delay,
                integer(raw, "radius", old == null ? 0 : old.getRadiance()),
                integer(raw, "number of charges", old == null ? 0 : old.getNbCharges()),
                bool(raw, "can be summoned", old != null && old.isCanSummon()),
                nullable(raw.get("key object name")), integer(raw, "keylock difficulty", old == null ? 0 : old.getLockDiff()),
                nullable(raw.get("sign text")), integer(raw, "container gold", old == null ? 0 : old.getContainerGold()),
                integer(raw, "container global respawn time", old == null ? 0 : old.getGlobalRespawn()),
                integer(raw, "container respawn time", old == null ? 0 : old.getLocalRespawn()),
                associatedSpells(raw, old), boosts, containerLootGroups(raw, old));
    }

    private static List<ItemDefinition.ItemSpell> associatedSpells(Map<String, String> raw, ItemDefinition old) {
        List<ItemDefinition.ItemSpell> spells = new ArrayList<>();
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            if (!entry.getKey().startsWith("associated spell ") || !entry.getKey().endsWith(": spell ID")) continue;
            int id = integer(Map.of("id", entry.getValue()), "id", 0);
            if (id > 0) spells.add(new ItemDefinition.ItemSpell(id, 0, 100));
        }
        return spells.isEmpty() && old != null ? old.getSpells() : spells;
    }

    private static List<ItemDefinition.ContainerLootGroup> containerLootGroups(Map<String, String> raw, ItemDefinition old) {
        Map<Integer, List<String>> groups = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("container group (\\d+): object \\d+", java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(entry.getKey());
            if (matcher.matches()) groups.computeIfAbsent(Integer.parseInt(matcher.group(1)), ignored -> new ArrayList<>()).add(entry.getValue());
        }
        if (groups.isEmpty()) return old == null ? List.of() : old.getContainerLootGroups();
        return groups.values().stream().map(ItemDefinition.ContainerLootGroup::new).toList();
    }

    static Map<String, List<ItemDefinition.ItemBoost>> loadBoosts(String file) throws Exception {
        try (FileReader reader = new FileReader(new File(file))) {
            List<Map<String, Object>> rows = new Gson().fromJson(reader,
                    new TypeToken<List<Map<String, Object>>>() {}.getType());
            Map<String, List<ItemDefinition.ItemBoost>> result = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                Object id = row.get("id");
                Object rawBoosts = row.get("boosts");
                if (id == null || !(rawBoosts instanceof List<?> list)) continue;
                List<ItemDefinition.ItemBoost> parsed = new ArrayList<>();
                for (Object value : list) {
                    if (!(value instanceof Map<?, ?> boost)) continue;
                    parsed.add(new ItemDefinition.ItemBoost(number(boost.get("type")),
                            number(boost.get("value")), String.valueOf(boost.get("expression")),
                            number(boost.get("duration")), number(boost.get("chance"))));
                }
                result.put(String.valueOf(id), parsed);
            }
            return result;
        }
    }

    private static int number(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static Map<Integer, String> loadSprites(String file) throws Exception {
        try (FileReader reader = new FileReader(new File(file))) {
            List<Map<String, Object>> rows = new Gson().fromJson(reader,
                    new TypeToken<List<Map<String, Object>>>() {}.getType());
            Map<Integer, String> result = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                result.put(((Number) row.get("id")).intValue(), String.valueOf(row.get("sprite")));
            }
            return result;
        }
    }

    private static BodyPart bodyPart(String value, String key, String equippedSprite, String inventorySprite) {
        if (value == null) return null;
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "body" -> BodyPart.BODY; case "feet" -> BodyPart.FEET; case "hands" -> BodyPart.LEFT_HAND;
            case "head" -> BodyPart.HEAD; case "legs" -> BodyPart.LEGS; case "finger" -> BodyPart.RING1;
            case "wrist" -> BodyPart.BRACER; case "neck" -> BodyPart.NECK; case "right-handed" -> BodyPart.WEAPON;
            case "left-handed" -> looksLikeShield(key, equippedSprite, inventorySprite)
                    ? BodyPart.SHIELD : BodyPart.WEAPON2;
            case "two-handed" -> BodyPart.WEAPON;
            case "waist" -> BodyPart.BELT; case "back" -> BodyPart.BACK; default -> null;
        };
    }

    private static boolean looksLikeShield(String... values) {
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains("shield")) return true;
        }
        return false;
    }

    private static int structure(String value) {
        if (value == null) return 0;
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "weapon" -> 1; case "armor" -> 2; case "container" -> 3; case "door" -> 4;
            case "potion" -> 5; case "standard" -> 6; case "sign" -> 7; case "quiver" -> 8;
            case "bow" -> 9; case "chest" -> 10; case "guildchest" -> 11; case "door3n" -> 12;
            case "door3f" -> 13; case "door5n" -> 14; case "door5f" -> 15; default -> 0;
        };
    }

    private static String stripComment(String value) {
        boolean quoted = false;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '"') quoted = !quoted;
            if (!quoted && value.charAt(i) == '#') return value.substring(0, i).trim();
        }
        return value;
    }

    private static String unquote(String value) {
        return value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")
                ? value.substring(1, value.length() - 1) : value;
    }

    private static String nullable(String value) { return value == null || value.isBlank() ? null : value; }
    private static String value(Map<String, String> map, String key, String fallback) {
        String value = nullable(map.get(key)); return value == null ? fallback : value;
    }
    private static boolean bool(Map<String, String> map, String key, boolean fallback) {
        String value = map.get(key); return value == null ? fallback : Boolean.parseBoolean(value);
    }
    private static int integer(Map<String, String> map, String key, int fallback) {
        try { return (int) Double.parseDouble(map.get(key)); } catch (Exception ignored) { return fallback; }
    }
    private static long decimalLong(Map<String, String> map, String key, long fallback) {
        try { return Math.round(Double.parseDouble(map.get(key))); } catch (Exception ignored) { return fallback; }
    }
    private static double decimal(Map<String, String> map, String key, double fallback) {
        try { return Double.parseDouble(map.get(key)); } catch (Exception ignored) { return fallback; }
    }
    private static double attackSpeed(String delay) {
        try { double ms = Double.parseDouble(delay); return ms > 0 ? 1000d / ms : 1d; }
        catch (Exception ignored) { return 1d; }
    }
}
