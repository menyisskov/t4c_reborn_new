package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.player.BodyPart;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

/** Lossless migration of Vircom's original NPC C++ sources into runtime catalogues. */
public final class LegacyNpcSourceMigration {
    private static final Charset SOURCE_CHARSET = Charset.forName("windows-1252");
    private static final Pattern METHOD = Pattern.compile("void\\s+(\\w+)::(Create|OnTalk|OnPopup|OnAttack|OnAttacked|OnDeath|OnInitialise|OnDestroy)\\s*\\(");
    private static final List<String> EVENT_METHODS = List.of(
            "OnPopup", "OnAttack", "OnAttacked", "OnDeath", "OnInitialise", "OnDestroy");
    private static final Pattern NAME = Pattern.compile("SET_NPC_NAME\\s*\\(\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern POSITION = Pattern.compile("npc\\.InitialPos\\.(X|Y|world)\\s*=\\s*(-?\\d+)");
    private static final Pattern TEMPLATE = Pattern.compile("npc\\s*=\\s*\\(?\\s*(\\w+)\\s*\\)?\\s*;");
    private static final Pattern INTL = Pattern.compile("INTL\\s*\\(\\s*\\d+\\s*,");
    // Mirrors NpcScriptEngine.COMMAND: CmdAND sections are real dialogue topics too, and
    // omitting them here hid their keywords from the dialogue list even though the engine matched them.
    private static final Pattern COMMAND = Pattern.compile("(?m)^\\s*(?:Command\\d*|CmdAND\\d*|ParamCmd)\\s*\\(");
    private static final Pattern C_STRING = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern TALK_CALL = Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");
    private static final Set<String> SUPPORTED_TALK_CALLS = Set.of(
            "INTL", "FORMAT", "Command", "Command2", "Command3", "Command4", "Command5",
            "CmdAND", "CmdAND2", "CmdAND3", "CmdAND4", "CmdAND5", "ParamCmd", "IF", "ELSEIF",
            "YES", "NO", "YesNoELSE", "SetYesNo", "SWITCH", "CASE", "FOR",
            "CheckFlag", "CheckNPCFlag", "CheckGlobalFlag", "CheckItem", "CheckUnitFlag", "ViewFlag",
            "GiveFlag", "RemFlag", "GiveGlobalFlag", "GiveNPCFlag", "GiveUnitFlag",
            "GiveItem", "TakeItem", "GetItemHandle", "TakeItemHandle", "GiveGold", "GiveGoldNoEcho",
            "TakeGold", "GiveXP", "GiveKarma", "HealPlayer", "SetHP", "SetDeathLocation", "REMORT_TO",
            "TELEPORT", "SUMMON", "SUMMON2", "FROM_NPC", "FROM_USER", "CastSpellTarget", "CastSpellSelf",
            "AddBuyItem", "SendBuyItemList", "AddSellItem", "SendSellItemList", "AddTeachSkill",
            "SendTeachSkillList", "AddTrainSkill", "SendTrainSkillList", "CreateFormuleList",
            "AddTeachFormule", "SendTeachFormuleList", "PRIVATE_SYSTEM_MESSAGE",
            "GLOBAL_SYSTEM_MESSAGE", "CHATTER_SHOUT", "SHOUT", "SET_STR", "SET_AGI", "SET_END", "SET_INT",
            "SET_WIS", "NUM_PARAM", "UserSkill", "DWORD", "BYTE", "WORD", "INT", "Fix", "dice", "roll",
            "C", "rnd", "double", "if", "while", "switch", "GetGodFlags", "MakeUpper",
            "Do", "OnInitialise", "OnAttack", "OnAttacked", "OnDeath", "SetCanMove", "SetDestination",
            "IsDay", "IsNight", "IsEvening", "IsSleepTime", "IsMorning", "IsAfterNoon", "Hour", "Minute",
            "IsInRange", "GetWL", "GetHP", "GetMaxMana", "AND", "OR", "LOG_GOLD_DEPOSIT", "LOG_GOLD_WITHDRAW",
            "SET_AIR_POWER", "SET_AIR_RESIST", "SET_WATER_POWER", "SET_WATER_RESIST", "SET_EARTH_POWER",
            "SET_EARTH_RESIST", "SET_FIRE_POWER", "SET_FIRE_RESIST", "SET_LIGHT_POWER", "SET_LIGHT_RESIST",
            "SET_DARK_POWER", "SET_DARK_RESIST", "SetFlag", "SetGold", "SetMaxHP", "SetMaxMana", "abs", "pow");

    private LegacyNpcSourceMigration() {}

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Usage: LegacyNpcSourceMigration <ServeurLib_NPC_DLL_SRC root>");
        }
        Path root = Path.of(String.join(" ", args));
        if (!Files.isDirectory(root)) throw new IllegalArgumentException("NPC source root not found: " + root);

        Map<String, TemplateAppearance> appearances = loadAppearances(root);
        Map<String, Integer> objectConstants = loadObjectConstants(root.resolve("Include").resolve("DynObjListing.h"));
        List<ImportedNpc> imported = new ArrayList<>();
        try (var projects = Files.list(root)) {
            for (Path project : projects.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("Dll Npcs")).toList()) {
                try (var files = Files.list(project)) {
                    for (Path file : files.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".cpp")).toList()) {
                        ImportedNpc npc = parse(file, appearances, objectConstants);
                        if (npc != null) imported.add(npc);
                    }
                }
            }
        }
        imported.sort(Comparator.comparing(n -> n.def.getName(), String.CASE_INSENSITIVE_ORDER));
        Map<String, ImportedNpc> unique = new LinkedHashMap<>();
        for (ImportedNpc npc : imported) {
            ImportedNpc previous = unique.putIfAbsent(npc.def.getName().toLowerCase(Locale.ROOT), npc);
            if (previous != null) throw new IllegalStateException("Duplicate NPC id " + npc.def.getName());
        }

        List<NpcDef> defs = unique.values().stream().map(n -> n.def).toList();
        List<SpawnBinaryIO.Entry> spawns = unique.values().stream().map(ImportedNpc::spawn).toList();
        Map<String, String> translations = new LinkedHashMap<>();
        for (NpcDef def : defs) {
            String identity = I18n.normalizedKey(def.getName());
            translations.put("npc." + identity, def.getDisplayName());
            translations.put("npc.welcome." + identity, def.getWelcomeText());
            for (int topicIndex = 0; topicIndex < def.getTopics().size(); topicIndex++) {
                NpcDef.DialogTopic topic = def.getTopics().get(topicIndex);
                if (topic.getResponse() != null) translations.put("npc.topic." + identity + "." + topicIndex, topic.getResponse());
                for (int keywordIndex = 0; keywordIndex < topic.getKeywords().size(); keywordIndex++) {
                    translations.put("npc.topic_keyword." + identity + "." + topicIndex + "." + keywordIndex,
                            topic.getKeywords().get(keywordIndex));
                }
            }
        }
        I18n.update(translations);
        NpcDefBinaryIO.write(new File(Paths.NPCS_BIN), defs);
        SpawnBinaryIO.write(new File(Paths.NPC_SPAWNS_BIN), spawns);
        long scriptBytes = defs.stream().map(NpcDef::getSourceScript).filter(java.util.Objects::nonNull)
                .mapToLong(s -> s.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).sum();
        long missingAppearance = defs.stream().filter(d -> d.getSpriteBase() == null && d.getParts().isEmpty()).count();
        Map<String, Long> unsupportedTalkCalls = defs.stream().map(NpcDef::getSourceScript)
                .filter(java.util.Objects::nonNull).flatMap(script -> {
                    List<String> calls = new ArrayList<>();
                    Matcher matcher = TALK_CALL.matcher(codeOnly(script));
                    while (matcher.find()) if (!SUPPORTED_TALK_CALLS.contains(matcher.group(1))) calls.add(matcher.group(1));
                    return calls.stream();
                }).collect(java.util.stream.Collectors.groupingBy(name -> name, java.util.TreeMap::new,
                        java.util.stream.Collectors.counting()));
        System.out.printf("Migrated %d NPCs, %d spawns, %d dialogue topics, %d source-script bytes, %d missing appearances%n",
                defs.size(), spawns.size(), defs.stream().mapToInt(d -> d.getTopics().size()).sum(), scriptBytes,
                missingAppearance);
        System.out.printf("Unsupported OnTalk call kinds: %d (%d calls)%n", unsupportedTalkCalls.size(),
                unsupportedTalkCalls.values().stream().mapToLong(Long::longValue).sum());
        unsupportedTalkCalls.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(30).forEach(entry -> System.out.printf("  unsupported %-32s %d%n", entry.getKey(), entry.getValue()));
        if (!unsupportedTalkCalls.isEmpty()) {
            for (NpcDef def : defs) {
                if (def.getSourceScript() == null) continue;
                Matcher matcher = TALK_CALL.matcher(codeOnly(def.getSourceScript()));
                Set<String> names = new java.util.TreeSet<>();
                while (matcher.find()) if (!SUPPORTED_TALK_CALLS.contains(matcher.group(1))) names.add(matcher.group(1));
                if (!names.isEmpty()) System.out.printf("  unsupported NPC %-32s %s%n", def.getName(), names);
            }
        }
        Map<String, Long> unsupportedEventCalls = defs.stream().flatMap(def -> def.getSourceEvents().values().stream())
                .flatMap(script -> {
                    List<String> calls = new ArrayList<>();
                    Matcher matcher = TALK_CALL.matcher(codeOnly(script));
                    while (matcher.find() && !SUPPORTED_TALK_CALLS.contains(matcher.group(1))) calls.add(matcher.group(1));
                    return calls.stream();
                }).collect(java.util.stream.Collectors.groupingBy(name -> name, java.util.TreeMap::new,
                        java.util.stream.Collectors.counting()));
        System.out.printf("Unsupported lifecycle call kinds: %d (%d calls)%n", unsupportedEventCalls.size(),
                unsupportedEventCalls.values().stream().mapToLong(Long::longValue).sum());
        unsupportedEventCalls.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(40).forEach(entry -> System.out.printf("  lifecycle unsupported %-28s %d%n", entry.getKey(), entry.getValue()));
        if (missingAppearance > 0) {
            defs.stream().filter(d -> d.getSpriteBase() == null && d.getParts().isEmpty())
                    .collect(java.util.stream.Collectors.groupingBy(NpcDef::getSourceTemplate,
                            java.util.TreeMap::new, java.util.stream.Collectors.counting()))
                    .forEach((template, count) -> System.out.printf("  missing template %-36s %d%n", template, count));
        }
    }

    private static ImportedNpc parse(Path file, Map<String, TemplateAppearance> appearances,
            Map<String, Integer> objectConstants) throws Exception {
        String source = Files.readString(file, SOURCE_CHARSET);
        String create = methodBody(source, "Create");
        String talk = methodBody(source, "OnTalk");
        if (create == null || talk == null) return null;
        talk = inlineCalledHelpers(source, talk);
        Matcher nameMatcher = NAME.matcher(create);
        if (!nameMatcher.find()) return null;
        String className = className(source, "Create");
        String displayName = stripNumericPrefix(unescape(nameMatcher.group(1)));
        Matcher templateMatcher = TEMPLATE.matcher(create);
        String template = templateMatcher.find() ? templateMatcher.group(1) : "";
        int x = 0, y = 0, z = 0;
        Matcher positions = POSITION.matcher(create);
        while (positions.find()) {
            int value = Integer.parseInt(positions.group(2));
            switch (positions.group(1)) { case "X" -> x = value; case "Y" -> y = value; default -> z = value; }
        }

        List<NpcDef.DialogTopic> topics = extractTopics(talk);
        String welcome = firstIntl(afterToken(talk, "Begin"));
        String project = file.getParent().getFileName().toString();
        TemplateAppearance appearance = appearances.getOrDefault(project + "\u0000" + template,
                appearances.getOrDefault(template, TemplateAppearance.EMPTY));
        // A declared but never initialized template is invisible in the original server.
        // Keeping it explicit avoids fabricating a naked puppet at its coordinates.
        if (appearance == TemplateAppearance.EMPTY) appearance = new TemplateAppearance(List.of(), "@invisible");
        String translatedTalk = translateSymbolicConstants(translateObjectConstants(talk, objectConstants));
        Map<String, String> sourceEvents = new LinkedHashMap<>();
        for (String event : EVENT_METHODS) {
            String body = methodBody(source, event);
            if (body != null && !body.isBlank()) sourceEvents.put(event,
                    translateSymbolicConstants(translateObjectConstants(body, objectConstants)));
        }
        sourceEvents.put("@combat.hp", Integer.toString(appearance.hp));
        sourceEvents.put("@combat.level", Integer.toString(appearance.level));
        sourceEvents.put("@combat.dodge", Integer.toString(appearance.dodge));
        sourceEvents.put("@combat.ac", Integer.toString(appearance.ac));
        sourceEvents.put("@combat.attackSkill", Integer.toString(appearance.attackSkill));
        sourceEvents.put("@combat.damage", appearance.damageFormula);
        sourceEvents.put("@combat.str", Integer.toString(appearance.str));
        sourceEvents.put("@combat.end", Integer.toString(appearance.end));
        sourceEvents.put("@combat.dex", Integer.toString(appearance.dex));
        NpcDef def = new NpcDef(className, displayName, appearance.parts, appearance.spriteBase,
                0, List.of(), welcome == null ? "" : welcome, topics, template, translatedTalk, sourceEvents);
        SpawnBinaryIO.Entry spawn = new SpawnBinaryIO.Entry();
        spawn.type = className; spawn.x = x; spawn.y = y; spawn.z = z;
        // Creatures can move by default in the original server. Only explicit
        // SetCanMove(FALSE) lifecycle code makes an NPC stationary.
        spawn.stationary = Pattern.compile("SetCanMove\\s*\\(\\s*FALSE\\s*\\)").matcher(codeOnly(source)).find();
        spawn.aggressive = false;
        return new ImportedNpc(def, spawn);
    }

    private static List<NpcDef.DialogTopic> extractTopics(String talk) {
        List<NpcDef.DialogTopic> result = new ArrayList<>();
        Matcher commands = COMMAND.matcher(talk);
        List<Integer> starts = new ArrayList<>();
        while (commands.find()) starts.add(commands.start());
        for (int i = 0; i < starts.size(); i++) {
            int start = starts.get(i);
            int end = i + 1 < starts.size() ? starts.get(i + 1) : talk.length();
            int open = talk.indexOf('(', start);
            int close = matching(talk, open, '(', ')');
            if (close < 0 || close >= end) continue;
            List<String> keywords = intlStrings(talk.substring(open + 1, close));
            String response = firstIntl(talk.substring(close + 1, end));
            if (!keywords.isEmpty()) result.add(new NpcDef.DialogTopic(keywords, response, List.of()));
        }
        return result;
    }

    private static List<String> intlStrings(String text) {
        List<String> values = new ArrayList<>();
        Matcher matcher = INTL.matcher(text);
        while (matcher.find()) {
            String value = stringExpression(text, matcher.end());
            if (value != null && !value.isBlank()) values.add(value);
        }
        return values;
    }

    private static String firstIntl(String text) {
        if (text == null) return null;
        Matcher matcher = INTL.matcher(text);
        return matcher.find() ? stringExpression(text, matcher.end()) : null;
    }

    private static String stringExpression(String text, int start) {
        int close = matching(text, text.lastIndexOf('(', start), '(', ')');
        int end = close < 0 ? Math.min(text.length(), start + 16_384) : close;
        Matcher strings = C_STRING.matcher(text.substring(start, end));
        StringBuilder value = new StringBuilder();
        while (strings.find()) value.append(unescape(strings.group(1)));
        return value.isEmpty() ? null : value.toString();
    }

    private static Map<String, TemplateAppearance> loadAppearances(Path root) throws Exception {
        Map<String, TemplateAppearance> result = new HashMap<>();
        EquipmentAppearances equipment = loadEquipmentAppearances(root, false);
        EquipmentAppearances femaleEquipment = loadEquipmentAppearances(root, true);
        try (var files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.getFileName().toString().equalsIgnoreCase("MonsterStatSetup.cpp")).toList()) {
                String source = Files.readString(file, SOURCE_CHARSET);
                Matcher blocks = Pattern.compile("SET_MONSTER\\s*\\(\\s*(\\w+)\\s*\\)").matcher(source);
                List<MatcherSnapshot> found = new ArrayList<>();
                while (blocks.find()) found.add(new MatcherSnapshot(blocks.group(1), blocks.start(), blocks.end()));
                for (int i = 0; i < found.size(); i++) {
                    MatcherSnapshot current = found.get(i);
                    String block = source.substring(current.end, i + 1 < found.size() ? found.get(i + 1).start : source.length());
            boolean female = block.matches("(?s).*MOB_NPC_APPEARANCE\\s*\\(\\s*FEMALE_PUPPET\\s*\\).*" );
            TemplateAppearance parsed = appearance(block, female ? femaleEquipment : equipment).withCombat(block);
                    result.put(file.getParent().getFileName() + "\u0000" + current.name, parsed);
                    result.putIfAbsent(current.name, parsed);
                }
            }
        }
        return result;
    }

    private static Map<String, Integer> loadObjectConstants(Path header) throws Exception {
        Map<String, Integer> result = new HashMap<>();
        Pattern define = Pattern.compile("(?m)^\\s*#define\\s+(__OBJ_[A-Za-z0-9_]+)\\s+([0-9]+)\\b");
        Matcher matcher = define.matcher(Files.readString(header, SOURCE_CHARSET));
        while (matcher.find()) result.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
        return result;
    }

    private static String translateObjectConstants(String script, Map<String, Integer> constants) {
        Matcher matcher = Pattern.compile("\\b__OBJ_[A-Za-z0-9_]+\\b").matcher(script);
        StringBuffer translated = new StringBuffer(script.length());
        while (matcher.find()) {
            Integer id = constants.get(matcher.group());
            matcher.appendReplacement(translated, id == null ? matcher.group() : id.toString());
        }
        matcher.appendTail(translated);
        return translated.toString();
    }

    /** Turns server-only spell/skill constants into stable runtime catalogue keys. */
    private static String translateSymbolicConstants(String script) {
        Matcher matcher = Pattern.compile("\\b__(SPELL|SKILL)_([A-Za-z0-9_]+)\\b").matcher(script);
        StringBuffer translated = new StringBuffer(script.length());
        while (matcher.find()) {
            String key = matcher.group(2).toLowerCase(Locale.ROOT);
            if (matcher.group(1).equals("SKILL")) {
                key = switch (key) {
                    case "powerfull_blow" -> "powerful_blow";
                    case "twoweapons" -> "two_weapons";
                    case "resurect" -> "resurrect";
                    default -> key;
                };
            } else {
                key = "spell." + key;
            }
            matcher.appendReplacement(translated, Matcher.quoteReplacement("\"" + key + "\""));
        }
        matcher.appendTail(translated);
        return translated.toString();
    }

    private static TemplateAppearance appearance(String block, EquipmentAppearances equipment) {
        Matcher objectMatcher = Pattern.compile("MOB_OBJ_APPEARANCE\\s*\\(\\s*(\\w+)").matcher(block);
        if (objectMatcher.find()) {
            String sprite = switch (objectMatcher.group(1)) {
                case "WELL_TALK" -> "DungeonWell";
                case "CHEST_TALK" -> "Chest";
                case "SUNDIAL_TALK" -> "Horloge Solaire";
                case "PORTAL" -> "SimplePortal-a";
                case "VAULT_TALK", "VAULT_TALK_I" -> "Vault";
                case "DOOR_TALK", "DOOR_TALK_I" -> "RockDoor1";
                case "COFFIN_TALK" -> "DungeonTomb1";
                case "RIB_TALK" -> "Object_Ribcage";
                default -> null;
            };
            return new TemplateAppearance(List.of(), sprite == null ? null : "@static:" + sprite);
        }
        Matcher kindMatcher = Pattern.compile("MOB_(?:NPC_)?APPEARANCE\\s*\\(\\s*(\\w+)").matcher(block);
        String kind = kindMatcher.find() ? kindMatcher.group(1) : "";
        if (!kind.equals("PUPPET") && !kind.equals("FEMALE_PUPPET")) {
            if (kind.startsWith("INVISIBLE_")) {
                return new TemplateAppearance(List.of(), "@invisible");
            }
            return new TemplateAppearance(List.of(), switch (kind) {
                case "ORC" -> "Orc"; case "DEMON" -> "Demon"; case "DRAGON" -> "DragonSTMOV";
                case "GOBLIN" -> "Goblin"; case "PIG" -> "Pig"; case "TROLL" -> "Troll";
                case "HUMAN_PEASANT" -> "PaysanModel1"; case "HUMAN_PAYSANNE" -> "PaysanneModel1";
                case "PLAYER_PRIEST" -> "Priest"; case "PLAYER_SWORDMAN" -> "BlackWarrior";
                case "PLAYER_THIEF" -> "Thief"; case "PLAYER_MAGE" -> "Wizard";
                case "RAT" -> "Rat"; case "ATROCITY" -> "Atrocity"; case "ATROCITYBOSS" -> "AtrocityBoss";
                case "ZOMBIE" -> "Zombie"; case "BRIGAND", "HUMAN_SWORDMAN" -> "Warrio";
                case "SKAVEN_PEON", "SKAVEN_PEON2" -> "64kSkavenPeon";
                case "SKAVEN_SHAMAN", "SKAVEN_SHAMAN2" -> "64kSkavenShaman";
                case "SKAVEN_SKAVENGER", "SKAVEN_SKAVENGER2" -> "64kSkavenSkavenger";
                case "SKAVEN_WARRIOR", "SKAVEN_WARRIOR2" -> "64kSkavenWarrior";
                case "SKELETON_KING" -> "64kSkeletonKing"; case "SKELETON_CENTAUR" -> "64kCentaurSkeleton";
                case "CENTAUR_WARRIOR" -> "64kCentaurWarrior"; case "CENTAUR_ARCHER" -> "64kCentaurArcher";
                case "CENTAUR_SHAMAN" -> "64kCentaurShaman"; case "CENTAUR_KING" -> "64kCentaurKing";
                case "AGMORKIAN" -> "Agmorkian"; case "SKELETON" -> "Skeleton"; case "MUMMY" -> "Mummy";
                case "WASP" -> "GiantWasp"; case "TREE_ENT" -> "TreeEnt"; case "TARANTULA" -> "Tarantula";
                case "SMALL_WORM" -> "SmallWorm"; case "BIG_WORM" -> "BigWorm"; case "SNAKE" -> "Snake";
                case "KRAANIANTANK", "FROZEN_KRAANIANTANK" -> "Tank"; case "KRAANIANFLYING" -> "KraanianFlying";
                case "KRAANIAN" -> "Kraanian"; case "KRAANIANMILIPEDE" -> "KraanianMilipede";
                case "TAUNTING" -> "Taunting"; case "SPIDER" -> "Spider"; case "RED_GOBLINBOSS" -> "GoblinBoss";
                case "OOZE" -> "Kobold"; case "BAT" -> "Bat"; case "WOLF" -> "Wolf"; case "LICH" -> "64kLich";
                case "SCORPION" -> "Scorpion"; case "GREEN_TROLL" -> "GreenTroll"; case "HORSE" -> "Horse";
                case "DRACONIS_LEATHER" -> "MonsDraconianLeather";
                case "INVISIBLE_PRIEST", "INVISIBLE_THIEF" -> null;
                default -> null;
            });
        }
        boolean female = kind.equals("FEMALE_PUPPET");
        List<NpcDef.Part> parts = new ArrayList<>();
        addDress(parts, block, "BODY", BodyPart.BODY, female ? "WoClothBody" : "PupBodyClothSet1", equipment, female);
        addDress(parts, block, "FEET", BodyPart.FEET, female ? "WoLeatherBoots" : "PupLeatherBoots", equipment, female);
        addDress(parts, block, "LEGS", BodyPart.LEGS, female ? "WoClothRobe" : "PupLegsClothSet1", equipment, female);
        addDress(parts, block, "HELM", BodyPart.HEAD, "PupChainMailCoif", equipment, female);
        addDress(parts, block, "WEAPON", BodyPart.WEAPON, "PupBattleSword", equipment, female);
        addDress(parts, block, "SHIELD", BodyPart.SHIELD, "PupRomanShield", equipment, female);
        addDress(parts, block, "CAPE", BodyPart.BACK, "PupRedCape", equipment, female);
        addDress(parts, block, "GLOVES", BodyPart.LEFT_HAND, "PupLeatherGloveL", equipment, female);
        return new TemplateAppearance(parts, null);
    }

    private static void addDress(List<NpcDef.Part> parts, String block, String macro, BodyPart part,
                                 String fallback, EquipmentAppearances equipment, boolean female) {
        Matcher declaration = Pattern.compile("(?m)^\\s*MOB_DRESS_" + macro + "\\s*\\(\\s*(\\w+)").matcher(block);
        if (!declaration.find()) return;
        Integer group = equipment.groupIds.get(declaration.group(1));
        boolean mapped = false;
        if (group != null) {
            for (var slot : equipment.bySlot.entrySet()) {
                String sprite = slot.getValue().get(group);
                BodyPart mappedPart = puppetBodyPart(slot.getKey());
                // In the original client SET1 robe legs are hidden for male puppets and
                // WoClothRobe is installed only by the female branch.
                if (!female && mappedPart == BodyPart.ROBELEGS && "WoClothRobe".equals(sprite)) continue;
                if (sprite != null && mappedPart != null) {
                    parts.removeIf(existing -> existing.getBodyPart() == mappedPart);
                    parts.add(new NpcDef.Part(mappedPart, sprite));
                    mapped = true;
                }
            }
        }
        if (!mapped) parts.add(new NpcDef.Part(part, fallback));
    }

    private static EquipmentAppearances loadEquipmentAppearances(Path sourceRoot, boolean female) throws Exception {
        Path distribution = sourceRoot;
        while (distribution != null && !distribution.getFileName().toString().equalsIgnoreCase("T4C_V1R7X"))
            distribution = distribution.getParent();
        if (distribution == null) return EquipmentAppearances.EMPTY;
        Path client = distribution.resolve("GoN").resolve("src VS2019").resolve("client").resolve("gon");
        Path puppetHeader = client.resolve("Puppet.h");
        Path puppetSource = client.resolve("Puppet.cpp");
        Path appearanceHeader = client.resolve("Apparence.h");
        Path visualObjects = client.resolve("VisualObjectList.cpp");
        Path gameDefs = sourceRoot.resolve("Include").resolve("GameDefs.h");
        if (!Files.isRegularFile(puppetHeader) || !Files.isRegularFile(puppetSource)
                || !Files.isRegularFile(appearanceHeader) || !Files.isRegularFile(visualObjects)
                || !Files.isRegularFile(gameDefs)) return EquipmentAppearances.EMPTY;
        CppEquipmentAppearanceMigration.ParsedPuppet puppet =
                CppEquipmentAppearanceMigration.parsePuppet(puppetHeader, puppetSource, female);
        Map<Integer, Map<Integer, String>> bySlot = CppEquipmentAppearanceMigration.parseObjectAppearances(
                appearanceHeader, visualObjects, puppet);
        Map<String, Integer> groups = new HashMap<>();
        Matcher matcher = Pattern.compile("(?m)^\\s*const\\s+unsigned\\s+int\\s+__OBJGROUP_(\\w+)\\s*=\\s*(\\d+)")
                .matcher(Files.readString(gameDefs, SOURCE_CHARSET));
        while (matcher.find()) groups.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
        return new EquipmentAppearances(groups, bySlot);
    }

    private static BodyPart puppetBodyPart(int slot) {
        return switch (slot) {
            case 0 -> BodyPart.LEFT_HAND; case 1 -> BodyPart.LEFT_ARM; case 2 -> BodyPart.FEET;
            case 3 -> BodyPart.LEGS; case 4 -> BodyPart.BODY; case 6 -> BodyPart.RIGHT_HAND;
            case 7 -> BodyPart.RIGHT_ARM; case 8 -> BodyPart.WEAPON;
            case 9 -> BodyPart.SHIELD; case 10 -> BodyPart.BOOT; case 11 -> BodyPart.HEAD;
            case 12, 13 -> BodyPart.BACK; case 16 -> BodyPart.ROBELEGS;
            case 17 -> BodyPart.MASK; case 18 -> BodyPart.WEAPON2; default -> null;
        };
    }

    private static String className(String source, String method) {
        Matcher matcher = METHOD.matcher(source);
        while (matcher.find()) if (matcher.group(2).equals(method)) return matcher.group(1);
        throw new IllegalArgumentException("Missing method " + method);
    }

    private static String methodBody(String source, String method) {
        Matcher matcher = METHOD.matcher(source);
        while (matcher.find()) {
            if (!matcher.group(2).equals(method)) continue;
            int open = source.indexOf('{', matcher.end());
            int close = matching(source, open, '{', '}');
            return open >= 0 && close > open ? source.substring(open + 1, close) : null;
        }
        return null;
    }

    /** Expands the legacy MAKE_FUNC/CALL_FUNC mini-functions into the persisted OnTalk body. */
    private static String inlineCalledHelpers(String source, String talk) {
        Matcher calls = Pattern.compile("CALL_FUNC\\s*\\(\\s*(\\w+)\\s*\\)").matcher(talk);
        StringBuffer expanded = new StringBuffer(talk.length());
        while (calls.find()) {
            String name = calls.group(1);
            Matcher definition = Pattern.compile("MAKE_FUNC\\s*\\(\\s*" + Pattern.quote(name) + "\\s*\\)").matcher(source);
            String body = "";
            if (definition.find()) {
                int end = source.indexOf("END_FUNC", definition.end());
                if (end >= 0) body = source.substring(definition.end(), end);
            }
            calls.appendReplacement(expanded, Matcher.quoteReplacement(body));
        }
        calls.appendTail(expanded);
        return expanded.toString();
    }

    /** Removes comments and string contents so coverage counts executable calls only. */
    private static String codeOnly(String source) {
        StringBuilder out = new StringBuilder(source.length());
        boolean string = false, character = false, line = false, block = false, escape = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i), n = i + 1 < source.length() ? source.charAt(i + 1) : 0;
            if (line) { if (c == '\n') { line = false; out.append(c); } else out.append(' '); continue; }
            if (block) { if (c == '*' && n == '/') { block = false; out.append("  "); i++; } else out.append(c == '\n' ? '\n' : ' '); continue; }
            if (string || character) {
                if (escape) { escape = false; out.append(' '); continue; }
                if (c == '\\') { escape = true; out.append(' '); continue; }
                if (string && c == '"') string = false;
                else if (character && c == '\'') character = false;
                out.append(' '); continue;
            }
            if (c == '/' && n == '/') { line = true; out.append("  "); i++; continue; }
            if (c == '/' && n == '*') { block = true; out.append("  "); i++; continue; }
            if (c == '"') { string = true; out.append(' '); continue; }
            if (c == '\'') { character = true; out.append(' '); continue; }
            out.append(c);
        }
        return out.toString();
    }

    /** Matches delimiters while ignoring C/C++ strings and comments. */
    private static int matching(String text, int open, char left, char right) {
        if (open < 0) return -1;
        int depth = 0; boolean string = false, character = false, line = false, block = false, escape = false;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i), n = i + 1 < text.length() ? text.charAt(i + 1) : 0;
            if (line) { if (c == '\n') line = false; continue; }
            if (block) { if (c == '*' && n == '/') { block = false; i++; } continue; }
            if (string || character) {
                if (escape) { escape = false; continue; }
                if (c == '\\') { escape = true; continue; }
                if (string && c == '"') string = false;
                if (character && c == '\'') character = false;
                continue;
            }
            if (c == '/' && n == '/') { line = true; i++; continue; }
            if (c == '/' && n == '*') { block = true; i++; continue; }
            if (c == '"') { string = true; continue; }
            if (c == '\'') { character = true; continue; }
            if (c == left) depth++;
            if (c == right && --depth == 0) return i;
        }
        return -1;
    }

    private static String afterToken(String text, String token) {
        int index = text.indexOf(token);
        return index < 0 ? text : text.substring(index + token.length());
    }

    private static String stripNumericPrefix(String name) { return name.replaceFirst("^\\[\\d+]\\s*", ""); }
    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\'", "'").replace("\\n", "\n")
                .replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\");
    }

    private record ImportedNpc(NpcDef def, SpawnBinaryIO.Entry spawn) {}
    private record EquipmentAppearances(Map<String, Integer> groupIds,
                                        Map<Integer, Map<Integer, String>> bySlot) {
        private static final EquipmentAppearances EMPTY = new EquipmentAppearances(Map.of(), Map.of());
    }
    private record TemplateAppearance(List<NpcDef.Part> parts, String spriteBase, int hp, int level,
                                      int dodge, int ac, int attackSkill, String damageFormula,
                                      int str, int end, int dex) {
        private TemplateAppearance(List<NpcDef.Part> parts, String spriteBase) {
            this(parts, spriteBase, 1, 1, 0, 0, 0, "1d3", 10, 10, 10);
        }
        private TemplateAppearance withCombat(String block) {
            return new TemplateAppearance(parts, spriteBase, macroInt(block, "MOB_HP", hp),
                    macroInt(block, "MOB_LEVEL", level), macroInt(block, "MOB_DODGE_SKILL", dodge),
                    macroInt(block, "MOB_AC", ac), macroInt(block, "MOB_ATTACK_SKILL", attackSkill),
                    macroString(block, "MOB_ATTACK_DMG_ROLL", damageFormula),
                    macroInt(block, "MOB_STR", str), macroInt(block, "MOB_END", end),
                    macroInt(block, "MOB_DEX", dex));
        }
        private static final TemplateAppearance EMPTY = new TemplateAppearance(List.of(), null);
    }
    private static int macroInt(String block, String macro, int fallback) {
        Matcher matcher = Pattern.compile("\\b" + macro + "\\s*\\(\\s*(-?\\d+)").matcher(block);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }
    private static String macroString(String block, String macro, String fallback) {
        Matcher matcher = Pattern.compile("\\b" + macro + "\\s*\\(\\s*\"([^\"]+)\"").matcher(block);
        return matcher.find() ? matcher.group(1).replace(" )", ")") : fallback;
    }
    private record MatcherSnapshot(String name, int start, int end) {}
}
