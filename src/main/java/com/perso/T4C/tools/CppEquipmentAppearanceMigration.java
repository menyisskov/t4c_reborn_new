package com.perso.T4C.tools;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Imports the equipped puppet sprite names from the original C++ client.
 *
 * <p>The WDA appearance id is the PUPEQ value.  Its meaning depends on the
 * equipped body slot, exactly like {@code Object->PuppetInfo[slot]} in
 * Puppet.cpp.  Keeping this migration mechanical prevents individual items
 * from silently falling back to their display name as a sprite name.</p>
 */
public final class CppEquipmentAppearanceMigration {
    private static final Pattern DEFINE = Pattern.compile("^\\s*#define\\s+(PUPEQ_[A-Za-z0-9_]+)\\s+([0-9]+)\\b");
    private static final Pattern OBJECT_GROUP = Pattern.compile(
            "^\\s*const\\s+unsigned\\s+int\\s+(__OBJGROUP_[A-Za-z0-9_]+)\\s*=\\s*([0-9]+)\\s*;");
    private static final Pattern SWITCH = Pattern.compile("switch\\s*\\(Object->PuppetInfo\\[(\\d+)\\]\\)");
    private static final Pattern CASE = Pattern.compile("^\\s*case\\s+(PUPEQ_[A-Za-z0-9_]+|[0-9]+)\\s*:");
    private static final Pattern SPRITE = Pattern.compile("LoadSprite3D\\([^;]*?\"([^\"]+)\"([^;]*)\\);");
    private static final Pattern GROUP_CASE = Pattern.compile("^\\s*case\\s+(__OBJGROUP_[A-Za-z0-9_]+)\\s*:");
    private static final Pattern PUPPET_ASSIGNMENT = Pattern.compile(
            "Object->PuppetInfo\\[(PUP_[A-Za-z0-9_]+)\\]\\s*=\\s*(PUPEQ_[A-Za-z0-9_]+)");
    private static final Pattern BIND_INVENTORY = Pattern.compile(
            "BIND_INV\\((__OBJGROUP_[A-Za-z0-9_]+|[0-9]+)\\s*,\\s*\"([^\"]+)\"\\)");
    private static final Pattern BIND_INVENTORY_PALETTE = Pattern.compile(
            "BIND_INV_PAL\\((__OBJGROUP_[A-Za-z0-9_]+|[0-9]+)\\s*,\\s*\"([^\"]+)\"\\s*,\\s*([0-9]+)\\)");
    private static final Pattern CREATE_GROUND = Pattern.compile(
            "pVObject\\[(__OBJGROUP_[A-Za-z0-9_]+|[0-9]+)\\]\\.CreateSprite\\(\"([^\"]+)\"([^;]*)\\);");
    private static final Pattern ICON_MACRO = Pattern.compile(
            "^\\s*#define\\s+(I[A-Z0-9_]+)\\s+\"([^\"]+)\"");
    private static final Pattern GENERIC_ICON = Pattern.compile(
            "ItemIcons\\.BindSprite\\((I[A-Z0-9_]+)\\s*,\\s*(__OBJGROUP_[A-Za-z0-9_]+|[0-9]+)\\s*\\)");
    private static final Pattern GROUP_ALIAS = Pattern.compile(
            "case\\s+(__OBJGROUP_[A-Za-z0-9_]+)\\s*:\\s*tType\\s*=\\s*(__OBJGROUP_[A-Za-z0-9_]+)\\s*;");
    private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private CppEquipmentAppearanceMigration() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: CppEquipmentAppearanceMigration <Puppet.h> <Puppet.cpp> <Apparence.h> <VisualObjectList.cpp> [--dry-run]");
        }
        boolean dryRun = java.util.Arrays.asList(args).contains("--dry-run");
        boolean authoritative = java.util.Arrays.asList(args).contains("--authoritative");
        ParsedPuppet parsedPuppet = parsePuppet(Path.of(args[0]), Path.of(args[1]));
        Map<Integer, Map<Integer, String>> appearances = parseObjectAppearances(
                Path.of(args[2]), Path.of(args[3]), parsedPuppet);
        Map<Integer, String> inventoryAppearances = parseInventoryAppearances(Path.of(args[2]), Path.of(args[3]));
        List<ItemDefinition> source = ItemRegistry.load();
        List<ItemDefinition> migrated = new ArrayList<>(source.size());
        int changed = 0;
        int unresolved = 0;

        for (ItemDefinition def : source) {
            BodyPart bodyPart = def.getBodyPart();
            SlotMapping slots = slotsFor(bodyPart);
            String primary = def.getAppearanceEquippedPrimary();
            String secondary = def.getAppearanceEquippedSecondary();
            String inventory = def.getAppearanceInventory();
            BodyPart secondaryPart = def.getSecondaryBodyPart();
            if (def.getAppearanceId() > 0) {
                String mappedInventory = inventoryAppearances.get(def.getAppearanceId());
                if (!isBlank(mappedInventory) && (authoritative || isBlank(inventory))) inventory = mappedInventory;
            }
            if (slots != null && def.getAppearanceId() > 0) {
                String mappedPrimary = lookup(appearances, slots.primarySlot, def.getAppearanceId());
                if (!isBlank(mappedPrimary) && (authoritative || isBlank(primary))) primary = mappedPrimary;
                if (slots.secondarySlot != null) {
                    String mappedSecondary = lookup(appearances, slots.secondarySlot, def.getAppearanceId());
                    if (!isBlank(mappedSecondary) && (authoritative || isBlank(secondary))) secondary = mappedSecondary;
                    if (!isBlank(secondary)) {
                        secondaryPart = slots.secondaryPart;
                    }
                }
            }
            if (slots != null && isBlank(primary) && def.getAppearanceId() > 0) {
                SlotAppearance alternative = uniqueAppearance(appearances, def.getAppearanceId());
                if (alternative != null) {
                    BodyPart actualPart = bodyPartForPuppetSlot(alternative.slot);
                    if (actualPart != null) {
                        primary = alternative.sprite;
                        bodyPart = actualPart;
                    }
                }
            }
            boolean visualEquipment = slots != null && def.getAppearanceId() > 0;
            if (!same(primary, def.getAppearanceEquippedPrimary())
                    || !same(secondary, def.getAppearanceEquippedSecondary())
                    || !same(inventory, def.getAppearanceInventory())
                    || secondaryPart != def.getSecondaryBodyPart()
                    || bodyPart != def.getBodyPart()) {
                changed++;
                System.out.printf("FIX id=%d slot=%s key=%s -> %s%s%n", def.getAppearanceId(),
                        def.getBodyPart(), def.getKey(), primary,
                        isBlank(secondary) ? "" : " + " + secondary);
                migrated.add(copyWithAppearance(def, bodyPart, primary, secondaryPart, secondary, inventory));
            } else {
                migrated.add(def);
            }
        }

        System.out.printf("Equipment audit: definitions=%d fixed=%d unresolved=%d%n",
                source.size(), changed, unresolved);
        if (!dryRun && changed > 0) {
            Path itemFile = Path.of("assets/items/items.bin");
            Path backup = itemFile.resolveSibling("items.bin.backup-appearance-" + LocalDateTime.now().format(BACKUP_TIME));
            Files.copy(itemFile, backup, StandardCopyOption.COPY_ATTRIBUTES);
            ItemRegistry.save(migrated);
            System.out.println("Backup: " + backup);
        }
    }

    static ParsedPuppet parsePuppet(Path header, Path source) throws Exception {
        return parsePuppet(header, source, false);
    }

    static ParsedPuppet parsePuppet(Path header, Path source, boolean female) throws Exception {
        Map<String, Integer> ids = new HashMap<>();
        for (String line : Files.readAllLines(header, StandardCharsets.ISO_8859_1)) {
            Matcher matcher = DEFINE.matcher(line);
            if (matcher.find()) {
                ids.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
            }
        }
        Map<Integer, Map<Integer, String>> result = new LinkedHashMap<>();
        int slot = -1;
        List<String> pendingCases = new ArrayList<>();
        boolean genderConditional = false;
        boolean femaleBranch = false;
        for (String line : Files.readAllLines(source, StandardCharsets.ISO_8859_1)) {
            Matcher switchMatcher = SWITCH.matcher(line);
            if (switchMatcher.find()) {
                slot = Integer.parseInt(switchMatcher.group(1));
                pendingCases.clear();
                genderConditional = false;
                continue;
            }
            if (slot < 0) {
                continue;
            }
            Matcher caseMatcher = CASE.matcher(line);
            if (caseMatcher.find()) {
                pendingCases.add(caseMatcher.group(1));
                continue;
            }
            if (line.contains("Object->Type == 10011")) {
                genderConditional = true;
                femaleBranch = false;
                continue;
            }
            if (genderConditional && line.trim().startsWith("else")) {
                femaleBranch = true;
                continue;
            }
            Matcher spriteMatcher = SPRITE.matcher(line);
            if (spriteMatcher.find() && !pendingCases.isEmpty()
                    && (!genderConditional || female == femaleBranch)) {
                String sprite = paletteQualified(spriteMatcher.group(1), spriteMatcher.group(2));
                Map<Integer, String> byId = result.computeIfAbsent(slot, ignored -> new LinkedHashMap<>());
                for (String constant : pendingCases) {
                    Integer id = ids.get(constant);
                    if (id == null && constant.chars().allMatch(Character::isDigit)) {
                        id = Integer.parseInt(constant);
                    }
                    if (id != null) {
                        byId.putIfAbsent(id, sprite); // first branch is the Pup (male) branch
                    }
                }
            } else if (line.contains("break;")) {
                pendingCases.clear();
                genderConditional = false;
                femaleBranch = false;
            }
        }
        return new ParsedPuppet(ids, result);
    }

    static Map<Integer, Map<Integer, String>> parseObjectAppearances(Path appearanceHeader,
            Path visualObjectList, ParsedPuppet puppet) throws Exception {
        Map<String, Integer> groupIds = new HashMap<>();
        for (String line : Files.readAllLines(appearanceHeader, StandardCharsets.ISO_8859_1)) {
            Matcher matcher = OBJECT_GROUP.matcher(line);
            if (matcher.find()) {
                groupIds.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
            }
        }
        Map<Integer, Map<Integer, String>> result = new LinkedHashMap<>();
        List<String> pendingGroups = new ArrayList<>();
        boolean inPuppetize = false;
        boolean puppetizeSignature = false;
        boolean bodyStarted = false;
        int braceDepth = 0;
        for (String line : Files.readAllLines(visualObjectList, StandardCharsets.ISO_8859_1)) {
            if (!inPuppetize && line.startsWith("void Puppetize(TFCObject* Object")) {
                puppetizeSignature = true;
            }
            if (!inPuppetize && puppetizeSignature) {
                if (line.contains(";")) {
                    puppetizeSignature = false;
                    continue;
                }
                if (line.contains("{")) {
                    inPuppetize = true;
                    bodyStarted = true;
                }
            }
            if (!inPuppetize) continue;
            Matcher groupCase = GROUP_CASE.matcher(line);
            if (groupCase.find()) {
                pendingGroups.add(groupCase.group(1));
                continue;
            }
            Matcher assignment = PUPPET_ASSIGNMENT.matcher(line);
            if (assignment.find() && !pendingGroups.isEmpty()) {
                Integer slot = puppetSlot(assignment.group(1));
                Integer puppetId = puppet.ids.get(assignment.group(2));
                String sprite = slot == null || puppetId == null ? null
                        : lookup(puppet.sprites, slot, puppetId);
                if (slot != null && sprite != null) {
                    Map<Integer, String> byGroup = result.computeIfAbsent(slot, ignored -> new LinkedHashMap<>());
                    for (String group : pendingGroups) {
                        Integer groupId = groupIds.get(group);
                        if (groupId != null) byGroup.put(groupId, sprite);
                    }
                }
            } else if (line.contains("break;")) {
                pendingGroups.clear();
            }
            int opens = (int) line.chars().filter(character -> character == '{').count();
            int closes = (int) line.chars().filter(character -> character == '}').count();
            if (opens > 0) bodyStarted = true;
            braceDepth += opens - closes;
            if (bodyStarted && braceDepth == 0) break;
        }
        return result;
    }

    static Map<Integer, String> parseInventoryAppearances(Path appearanceHeader,
            Path visualObjectList) throws Exception {
        Map<String, Integer> groupIds = new HashMap<>();
        for (String line : Files.readAllLines(appearanceHeader, StandardCharsets.ISO_8859_1)) {
            Matcher matcher = OBJECT_GROUP.matcher(line);
            if (matcher.find()) groupIds.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
        }
        Map<Integer, String> result = new LinkedHashMap<>();
        Map<Integer, String> groundFallbacks = new LinkedHashMap<>();
        Map<Integer, String> genericIcons = new LinkedHashMap<>();
        Map<String, String> iconMacros = new HashMap<>();
        Map<Integer, Integer> aliases = new LinkedHashMap<>();
        for (String line : Files.readAllLines(visualObjectList, StandardCharsets.ISO_8859_1)) {
            Matcher macroMatcher = ICON_MACRO.matcher(line);
            if (macroMatcher.find()) iconMacros.put(macroMatcher.group(1), macroMatcher.group(2));
            Matcher aliasMatcher = GROUP_ALIAS.matcher(line);
            if (aliasMatcher.find()) {
                Integer from = groupIds.get(aliasMatcher.group(1));
                Integer to = groupIds.get(aliasMatcher.group(2));
                if (from != null && to != null) aliases.put(from, to);
            }
            Matcher groundMatcher = CREATE_GROUND.matcher(line);
            if (groundMatcher.find()) {
                Integer id = resolveGroupId(groundMatcher.group(1), groupIds);
                if (id != null) groundFallbacks.put(id,
                        paletteQualifiedGround(groundMatcher.group(2), groundMatcher.group(3)));
            }
            Matcher iconMatcher = GENERIC_ICON.matcher(line);
            if (iconMatcher.find()) {
                Integer id = resolveGroupId(iconMatcher.group(2), groupIds);
                String sprite = iconMacros.get(iconMatcher.group(1));
                if (id != null && sprite != null && !"??".equals(sprite)) genericIcons.put(id, sprite);
            }
            Matcher paletteMatcher = BIND_INVENTORY_PALETTE.matcher(line);
            if (paletteMatcher.find()) {
                Integer id = resolveGroupId(paletteMatcher.group(1), groupIds);
                if (id != null) result.put(id, qualify(paletteMatcher.group(2), Integer.parseInt(paletteMatcher.group(3))));
                continue;
            }
            Matcher matcher = BIND_INVENTORY.matcher(line);
            if (!matcher.find()) continue;
            Integer id = resolveGroupId(matcher.group(1), groupIds);
            if (id != null) result.put(id, matcher.group(2));
        }
        genericIcons.forEach(result::putIfAbsent);
        groundFallbacks.forEach(result::putIfAbsent);
        aliases.forEach((from, to) -> {
            String inherited = result.get(to);
            if (inherited != null) result.putIfAbsent(from, inherited);
        });
        return result;
    }

    private static Integer resolveGroupId(String token, Map<String, Integer> groupIds) {
        if (token.startsWith("__OBJGROUP_")) return groupIds.get(token);
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String paletteQualifiedGround(String base, String argumentsAfterName) {
        Matcher numbers = Pattern.compile("-?[0-9]+").matcher(argumentsAfterName);
        List<Integer> values = new ArrayList<>();
        while (numbers.find()) values.add(Integer.parseInt(numbers.group()));
        int palette = values.size() >= 2 ? values.get(values.size() - 1) : 0;
        return qualify(base, palette);
    }

    private static String paletteQualified(String base, String argumentsAfterName) {
        Matcher numbers = Pattern.compile("-?[0-9]+").matcher(argumentsAfterName);
        List<Integer> values = new ArrayList<>();
        while (numbers.find()) values.add(Integer.parseInt(numbers.group()));
        int palette = values.size() >= 4 ? values.get(values.size() - 1) : 0;
        return qualify(base, palette);
    }

    private static String qualify(String base, int palette) {
        return palette <= 0 ? base : base + "__pal" + palette;
    }

    private static Integer puppetSlot(String name) {
        return switch (name) {
            case "PUP_HAND_LEFT" -> 0;
            case "PUP_ARM_LEFT" -> 1;
            case "PUP_FOOT" -> 2;
            case "PUP_LEGS" -> 3;
            case "PUP_BODY" -> 4;
            case "PUP_HAND_RIGHT" -> 6;
            case "PUP_ARM_RIGHT" -> 7;
            case "PUP_WEAPON" -> 8;
            case "PUP_SHIELD" -> 9;
            case "PUP_BOOT" -> 10;
            case "PUP_HAT" -> 11;
            case "PUP_CAPE" -> 12;
            case "PUP_BACKBODY" -> 13;
            case "PUP_ROBELEGS" -> 16;
            case "PUP_MASK" -> 17;
            case "PUP_WEAPON2" -> 18;
            default -> null;
        };
    }

    private static String lookup(Map<Integer, Map<Integer, String>> mappings, int slot, int id) {
        return mappings.getOrDefault(slot, Map.of()).get(id);
    }

    private static SlotAppearance uniqueAppearance(Map<Integer, Map<Integer, String>> mappings, int id) {
        SlotAppearance found = null;
        for (Map.Entry<Integer, Map<Integer, String>> entry : mappings.entrySet()) {
            String sprite = entry.getValue().get(id);
            if (sprite == null) continue;
            if (found != null && !found.sprite.equals(sprite)) return null;
            found = new SlotAppearance(entry.getKey(), sprite);
        }
        return found;
    }

    private static BodyPart bodyPartForPuppetSlot(int slot) {
        return switch (slot) {
            case 0 -> BodyPart.LEFT_HAND;
            case 3 -> BodyPart.LEGS;
            case 4 -> BodyPart.BODY;
            case 8 -> BodyPart.WEAPON;
            case 9 -> BodyPart.SHIELD;
            case 10 -> BodyPart.BOOT;
            case 11 -> BodyPart.HEAD;
            case 12, 13 -> BodyPart.BACK;
            case 16 -> BodyPart.ROBELEGS;
            case 17 -> BodyPart.MASK;
            case 18 -> BodyPart.WEAPON2;
            default -> null;
        };
    }

    private static SlotMapping slotsFor(BodyPart part) {
        if (part == null) return null;
        return switch (part) {
            case LEFT_HAND -> new SlotMapping(0, 6, BodyPart.RIGHT_HAND);
            case LEGS -> new SlotMapping(3, null, null);
            case BODY -> new SlotMapping(4, null, null);
            case HEAD, HAT -> new SlotMapping(11, null, null);
            case FEET -> new SlotMapping(2, null, null);
            case BOOT -> new SlotMapping(10, null, null);
            case BACK, CAPE -> new SlotMapping(12, null, null);
            case SHIELD -> new SlotMapping(9, null, null);
            case WEAPON -> new SlotMapping(8, null, null);
            case WEAPON2 -> new SlotMapping(18, null, null);
            case ROBELEGS -> new SlotMapping(16, null, null);
            case MASK -> new SlotMapping(17, null, null);
            default -> null; // jewelry and cosmetic/non-rendered equipment
        };
    }

    private static ItemDefinition copyWithAppearance(ItemDefinition d, BodyPart bodyPart, String primary,
            BodyPart secondaryPart, String secondary, String inventory) {
        return new ItemDefinition(d.getKey(), d.getName(), bodyPart, primary,
                secondaryPart, secondary, inventory, d.getPrice(), d.getWeight(),
                d.getArmorClass(), d.getDodgeLost(), d.getMinEnd(), d.getReqAttack(), d.getReqStr(),
                d.getReqAgi(), d.getMinInt(), d.getMinWis(), d.getAttackSpeed(), d.isUnique(),
                d.isBow(), d.isUnlimitedUse(), d.getNumId(), d.getStructure(), d.getAppearanceId(),
                d.getDmgFormula(), d.getAtkDelay(), d.getRadiance(), d.getNbCharges(), d.isCanSummon(),
                d.getLockName(), d.getLockDiff(), d.getSignText(), d.getContainerGold(),
                d.getGlobalRespawn(), d.getLocalRespawn(), d.getSpells(), d.getBoosts());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean same(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private record SlotMapping(int primarySlot, Integer secondarySlot, BodyPart secondaryPart) {
    }

    private record SlotAppearance(int slot, String sprite) {
    }

    record ParsedPuppet(Map<String, Integer> ids, Map<Integer, Map<Integer, String>> sprites) {
    }
}
