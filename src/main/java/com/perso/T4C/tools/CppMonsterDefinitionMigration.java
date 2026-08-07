package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.monster.MonsterDef;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Imports addon monster definitions referenced by spawns but absent from the WDA monster table. */
public final class CppMonsterDefinitionMigration {
    private static final Pattern NPC_ASSIGNMENT = Pattern.compile("npc\\s*=\\s*\\(?\\s*([A-Za-z0-9_]+)\\s*\\)?\\s*;", Pattern.CASE_INSENSITIVE);
    private static final Pattern SET_MONSTER = Pattern.compile("SET_MONSTER\\s*\\(\\s*([A-Za-z0-9_]+)\\s*\\)(.*?)(?=SET_MONSTER\\s*\\(|\\z)", Pattern.DOTALL);
    private static final Pattern APPEARANCE_DEFINE = Pattern.compile("#define\\s+__MOBAPPEAR_([A-Za-z0-9_]+)\\s+(\\d+)");
    private record AnimationPatterns(String walk, String attack, String death) {}

    private CppMonsterDefinitionMigration() {
    }

    public static void main(String[] args) throws Exception {
        Path cppRoot = Path.of(args.length > 0 ? args[0] : "C:/T4C/T4C TOOLS/T4C_V1R7X/GoN/src VS2019/server");
        File target = new File(args.length > 1 ? args[1] : Paths.MONSTERS_BIN);
        File spawns = new File(args.length > 2 ? args[2] : Paths.MONSTER_SPAWNS_BIN);
        List<MonsterDef> definitions = new ArrayList<>(MonsterDefBinaryIO.read(target));
        Set<String> known = new LinkedHashSet<>();
        definitions.forEach(definition -> known.add(definition.getName()));
        Set<String> missing = new LinkedHashSet<>();
        for (SpawnBinaryIO.Entry entry : SpawnBinaryIO.read(spawns)) if (!known.contains(entry.type)) missing.add(entry.type);

        Map<String, Path> classes = new HashMap<>();
        Map<String, Integer> appearances = new HashMap<>();
        StringBuilder setup = new StringBuilder();
        try (Stream<Path> paths = Files.walk(cppRoot)) {
            for (Path path : paths.filter(Files::isRegularFile).filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".cpp")).toList()) {
                if (path.toString().contains(File.separator + "Debug" + File.separator)) continue;
                String base = path.getFileName().toString().replaceFirst("(?i)\\.cpp$", "");
                classes.putIfAbsent(base.toLowerCase(Locale.ROOT), path);
                if (base.equalsIgnoreCase("MonsterStatSetup")) setup.append(Files.readString(path, StandardCharsets.ISO_8859_1)).append('\n');
                if (base.equalsIgnoreCase("GameDefs")) readAppearanceDefines(path, appearances);
            }
        }
        // GameDefs is a header, so it is not visited by the .cpp scan above.
        try (Stream<Path> paths = Files.walk(cppRoot)) {
            for (Path path : paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("GameDefs.h")).toList()) {
                readAppearanceDefines(path, appearances);
            }
        }
        Map<String, String> blocks = blocks(setup.toString());
        Map<Integer, AnimationPatterns> patterns = new HashMap<>();
        for (MonsterDef definition : definitions) {
            if (definition.getAppearance() > 0 && definition.getWalkPattern() != null) {
                patterns.putIfAbsent(definition.getAppearance(), new AnimationPatterns(definition.getWalkPattern(),
                        definition.getAttackPattern(), definition.getDeathPattern()));
            }
        }
        int imported = 0;
        for (String type : missing) {
            Path source = classes.get(type.toLowerCase(Locale.ROOT));
            if (source == null) continue;
            Matcher assignment = NPC_ASSIGNMENT.matcher(Files.readString(source, StandardCharsets.ISO_8859_1));
            if (!assignment.find()) continue;
            String block = blocks.get(assignment.group(1).toLowerCase(Locale.ROOT));
            if (block == null || integer(block, "MOB_HP", 0) <= 0) continue;
            definitions.add(definition(type, block, appearances, patterns));
            imported++;
        }
        int repaired = 0;
        for (int i = 0; i < definitions.size(); i++) {
            MonsterDef current = definitions.get(i);
            if (current.getAppearance() != 0 && current.getAppearance() != 20005) continue;
            Path source = classes.get(current.getName().replaceFirst("1$", "").toLowerCase(Locale.ROOT));
            if (source == null) continue;
            Matcher assignment = NPC_ASSIGNMENT.matcher(Files.readString(source, StandardCharsets.ISO_8859_1));
            if (!assignment.find()) continue;
            String block = blocks.get(assignment.group(1).toLowerCase(Locale.ROOT));
            if (block == null) continue;
            int authoritativeAppearance = appearance(block, appearances);
            if (authoritativeAppearance <= 0 || authoritativeAppearance == current.getAppearance()) continue;
            definitions.set(i, definition(current.getName(), block, appearances, patterns));
            repaired++;
        }
        MonsterDefBinaryIO.write(target, definitions);
        System.out.println("Imported " + imported + " C++ monster definitions, repaired " + repaired
                + " fallback appearances; " + (missing.size() - imported) + " aliases remain for audit.");
    }

    private static Map<String, String> blocks(String source) {
        Map<String, String> result = new HashMap<>();
        Matcher matcher = SET_MONSTER.matcher(source);
        while (matcher.find()) result.put(matcher.group(1).toLowerCase(Locale.ROOT), matcher.group(2));
        return result;
    }

    private static MonsterDef definition(String type, String block, Map<String, Integer> appearances,
                                         Map<Integer, AnimationPatterns> patternsByAppearance) {
        String roll = text(block, "MOB_ATTACK_DMG_ROLL", "1d4");
        int appearance = appearance(block, appearances);
        AnimationPatterns animation = patternsByAppearance.getOrDefault(appearance,
                appearance == 20013 ? new AnimationPatterns("Demon#i", "DemonA#i", "DemonC#k")
                        : new AnimationPatterns("Warrio#l", "WarrioA#l", "WarrioC"));
        int[] damage = diceBounds(roll);
        int[] values = {
                integer(block, "MOB_AIR_RESIST", 0), integer(block, "MOB_EARTH_RESIST", 0),
                integer(block, "MOB_WATER_RESIST", 0), integer(block, "MOB_FIRE_RESIST", 0),
                integer(block, "MOB_DARK_RESIST", 0), integer(block, "MOB_LIGHT_RESIST", 0),
                integer(block, "MOB_AIR_POWER", 100), integer(block, "MOB_EARTH_POWER", 100),
                integer(block, "MOB_WATER_POWER", 100), integer(block, "MOB_FIRE_POWER", 100),
                integer(block, "MOB_DARK_POWER", 100), integer(block, "MOB_LIGHT_POWER", 100)
        };
        int ac = integer(block, "MOB_AC", 0);
        List<MonsterDef.Attack> attacks = List.of(new MonsterDef.Attack(roll,
                integer(block, "MOB_ATTACK_SKILL", 50), integer(block, "MOB_ATTACK_PERCENTAGE", 100), 0, 0, 0));
        return new MonsterDef(type, displayName(type), integer(block, "MOB_HP", 1), integer(block, "MOB_MANA", 0),
                decimal(block, "MOB_XP_PER_HIT", 0), decimal(block, "MOB_XP_DEATH", 0), damage[0], damage[1], 30_000L,
                animation.walk(), animation.attack(), animation.death(), null, null, null,
                integer(block, "MOB_MIN_GOLD", 0), integer(block, "MOB_MAX_GOLD", 0), List.of(), false, 0f,
                integer(block, "MOB_STR", 0), integer(block, "MOB_END", 0), integer(block, "MOB_DEX", 0),
                integer(block, "MOB_INT", 0), integer(block, "MOB_WIL", 0), integer(block, "MOB_WIS", 0), integer(block, "MOB_LCK", 0), values,
                integer(block, "MOB_LEVEL", 1), integer(block, "MOB_DODGE_SKILL", 0), 0, Float.floatToIntBits(ac), appearance,
                0, 0, 0, 0, 0, 0, 0, 0, integer(block, "MOB_AGRESSIVNESS", 0), 0, 0, true, attacks, false, 0, List.of());
    }

    private static int appearance(String block, Map<String, Integer> appearances) {
        Matcher matcher = Pattern.compile("MOB_(?:NPC_)?APPEARANCE\\s*\\(\\s*([A-Za-z0-9_]+)",
                Pattern.CASE_INSENSITIVE).matcher(block);
        if (!matcher.find()) {
            return 0;
        }
        return appearances.getOrDefault(matcher.group(1).toUpperCase(Locale.ROOT), 0);
    }

    private static void readAppearanceDefines(Path path, Map<String, Integer> appearances) throws Exception {
        Matcher matcher = APPEARANCE_DEFINE.matcher(Files.readString(path, StandardCharsets.ISO_8859_1));
        while (matcher.find()) appearances.putIfAbsent(matcher.group(1).toUpperCase(Locale.ROOT), Integer.parseInt(matcher.group(2)));
    }

    private static int integer(String block, String macro, int fallback) {
        Matcher matcher = Pattern.compile("(?m)^\\s*" + macro + "\\s*\\(\\s*(-?\\d+)").matcher(block);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private static int decimal(String block, String macro, int fallback) {
        Matcher matcher = Pattern.compile("(?m)^\\s*" + macro + "\\s*\\(\\s*(-?\\d+(?:\\.\\d+)?)").matcher(block);
        return matcher.find() ? (int) Double.parseDouble(matcher.group(1)) : fallback;
    }

    private static String text(String block, String macro, String fallback) {
        Matcher matcher = Pattern.compile("(?m)^\\s*" + macro + "\\s*\\(\\s*\"([^\"]+)\"").matcher(block);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    private static int[] diceBounds(String formula) {
        Matcher matcher = Pattern.compile("(\\d+)d(\\d+)(?:\\s*([+-])\\s*(\\d+))?", Pattern.CASE_INSENSITIVE).matcher(formula);
        if (!matcher.find()) return new int[]{1, 4};
        int count = Integer.parseInt(matcher.group(1));
        int sides = Integer.parseInt(matcher.group(2));
        int bonus = matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4)) * ("-".equals(matcher.group(3)) ? -1 : 1);
        return new int[]{Math.max(0, count + bonus), Math.max(0, count * sides + bonus)};
    }

    private static String displayName(String type) {
        return type.replaceFirst("^MOB", "").replaceAll("(?<=[a-z])(?=[A-Z])", " ");
    }
}
