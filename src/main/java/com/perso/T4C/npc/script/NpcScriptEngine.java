package com.perso.T4C.npc.script;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.npc.*;
import com.perso.T4C.npc.behavior.RebirthBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.player.PlayerProgression;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NpcScriptEngine {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private static final Pattern COMMAND =
      Pattern.compile("(?m)^\\s*(Command\\d*|CmdAND\\d*|ParamCmd)\\s*\\(");

  private static final Pattern SECTION =
      Pattern.compile(
          "(?m)^\\s*(?:Command\\d*|CmdAND\\d*|ParamCmd|YES|NO|YesNoELSE|Default)\\s*(?:\\(|$)");

  private static final Pattern LEGACY_STATE_MARKER =
      Pattern.compile("(?s);?\\}\\s*else\\s+if\\s*\\((.*?)\\)\\s*\\{;?");

  private static final Pattern C_STRING = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");

  private static final Pattern INT = Pattern.compile("-?\\d+");

  private static final Map<String, Integer> GLOBAL_FLAGS = new ConcurrentHashMap<>();

  private static final Map<String, Long> GLOBAL_FLAG_EXPIRATIONS = new ConcurrentHashMap<>();

  private static final Map<Integer, String> NUMERIC_GLOBAL_FLAGS =
      Map.of(
          30098, "GLOBAL_FLAG_GOBLIN_QUEST",
          30191, "GLOBAL_BANK_HAS_BEEN_ROBBED",
          30202, "GLOBAL_BANK_HAS_BEEN_ROBBED_BY",
          30464, "GLOBAL_FLAG_MAKRSH_PTANGH_DEAD_TIMER",
          30465, "GLOBAL_FLAG_MAKRSH_PTANGH_IS_FIGHTING");

  public static int globalFlagValue(String flag) {

    return GLOBAL_FLAGS.getOrDefault(flag, 0);
  }

  public static int globalFlagValue(int id) {

    String name = NUMERIC_GLOBAL_FLAGS.get(id);

    return name == null ? 0 : globalFlagValue(name);
  }

  private static final String FLAG_NUMBER_OF_REMORTS = "__FLAG_NUMBER_OF_REMORTS";

  private static final String FLAG_REMORT_PROCESS = "__FLAG_REMORT_PROCESS";

  private static final String FLAG_REMORT_POINTS = "__FLAG_REMORT_POINTS";

  public static int globalFlag(String flag) {

    return GLOBAL_FLAGS.getOrDefault(flag, 0);
  }

  public static void setGlobalFlag(String flag, int value) {

    if (value <= 0) GLOBAL_FLAGS.remove(flag);
    else GLOBAL_FLAGS.put(flag, value);

    GLOBAL_FLAG_EXPIRATIONS.remove(flag);
  }

  private static final String FLAG_RESIST_PREFIX = "legacy:resist:";

  private static final String FLAG_POWER_PREFIX = "legacy:power:";

  private static final List<String> ELEMENTS =
      List.of("fire", "water", "air", "earth", "light", "dark");

  private static final String EVENT_REBIRTH_REGALIA = "@rebirth.regalia";

  public record Result(
      String text,
      List<String> systemMessages,
      List<String> shopItems,
      List<SellRule> sellRules,
      List<String> taughtSpells,
      List<String> taughtSkills,
      List<String> trainedSkills,
      List<String> targetSpells,
      List<String> selfSpells,
      List<SkillOffer> skillOffers,
      List<FormulaOffer> formulaOffers,
      List<SummonRequest> summons,
      int xp,
      boolean heal,
      boolean endConversation,
      String pendingYesNo,
      boolean selfDestruct,
      int npcHpOverride,
      boolean effect) {

    static Result empty() {

      return new Result(
          null,
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          0,
          false,
          false,
          null,
          false,
          Integer.MIN_VALUE,
          false);
    }

    public boolean handled() {

      return text != null
          || !systemMessages.isEmpty()
          || !shopItems.isEmpty()
          || !sellRules.isEmpty()
          || xp != 0
          || heal
          || !taughtSpells.isEmpty()
          || !taughtSkills.isEmpty()
          || !trainedSkills.isEmpty()
          || !skillOffers.isEmpty()
          || !formulaOffers.isEmpty()
          || !targetSpells.isEmpty()
          || !selfSpells.isEmpty()
          || !summons.isEmpty()
          || endConversation
          || pendingYesNo != null
          || selfDestruct
          || npcHpOverride != Integer.MIN_VALUE
          || effect;
    }
  }

  public record SummonRequest(
      String monster, String xExpression, String yExpression, String zExpression) {}

  public record SellRule(String categories, long minimumPrice, long maximumPrice) {}

  public record SkillOffer(
      String skill, int limitOrInitialPoints, int goldCost, boolean teaching) {}

  public record FormulaOffer(int formulaId, int goldCost) {}

  private NpcScriptEngine() {}

  public static void performRebirth(Player player) {

    RebirthBehavior.perform(player);
  }

  private static void rebirth(Player player, String npcName) {

    int remorts = player.getQuestFlag(FLAG_NUMBER_OF_REMORTS) + 1;

    player.setQuestFlag(FLAG_NUMBER_OF_REMORTS, remorts);

    player.setRebirthCount(remorts);

    player.setQuestFlag(FLAG_REMORT_POINTS, GameConstants.REBIRTH_REMORT_POINTS_PER_REBIRTH);

    player.setQuestFlag(FLAG_REMORT_PROCESS, 0);

    int attribute =
        GameConstants.REBIRTH_BASE_ATTRIBUTE + remorts * GameConstants.REBIRTH_ATTRIBUTE_PER_REMORT;

    player.setStrength(attribute);

    player.setDexterity(attribute);

    player.setEndurance(attribute);

    player.setIntelligence(attribute);

    player.setWisdom(attribute);

    for (String element : ELEMENTS) {

      player.setQuestFlag(FLAG_RESIST_PREFIX + element, 0);

      player.setQuestFlag(FLAG_POWER_PREFIX + element, 0);
    }

    player.setLevel(1);

    player.setCurrentXp(0);

    XpCurve.loadDefault().applyToPlayer(player);

    player.setStatPoints(0);

    player.setSkillPoints(0);

    if (player.getSkills() != null) player.getSkills().clear();

    if (player.getSpells() != null) player.getSpells().clear();

    if (player.getQuickSlots() != null) player.getQuickSlots().clear();

    int maxHp = PlayerProgression.hitPointGainBase(attribute);

    int maxMana = PlayerProgression.manaGainBase(attribute, attribute);

    player.setMaxHp(maxHp);

    player.setCurrentHp(maxHp);

    player.setMaxMana(maxMana);

    player.setMana(maxMana);

    for (Player.ActiveBuff buff : new ArrayList<>(player.getActiveBuffs())) {

      player.dispelBuff(buff.getSpellName());
    }

    player.setHiddenFor(0L);

    List<String> regalia = rebirthRegalia(npcName);

    stripEquipment(player, regalia);

    grantSeraphRegalia(player, regalia);
  }

  private static List<String> rebirthRegalia(String npcName) {

    NpcSpec spec = NpcFactoryRegistry.specification(npcName);

    String raw = spec == null ? null : spec.sourceEvents().get(EVENT_REBIRTH_REGALIA);

    if ((raw == null || raw.isBlank()) && "Oracle".equals(npcName)) {

      return List.of("item.remort_white_wings", "item.ring_of_the_seraph");
    }

    if (raw == null || raw.isBlank()) return List.of();

    List<String> keys = new ArrayList<>();

    for (String key : raw.split(",")) {

      String trimmed = key.trim();

      if (!trimmed.isEmpty()) keys.add(trimmed);
    }

    return keys;
  }

  private static void stripEquipment(Player player, List<String> regalia) {

    for (BodyPart slot : new ArrayList<>(player.getEquippedItems().keySet())) {

      if (regalia.contains(player.getEquippedItems().get(slot))) continue;

      InventoryService.unequip(player, slot);
    }
  }

  private static void grantSeraphRegalia(Player player, List<String> regalia) {

    for (String key : regalia) {

      ItemDefinition definition = ItemRegistry.findByKey(key);

      if (definition == null || definition.getBodyPart() == null) continue;

      if (!player.getInventory().contains(key)) InventoryService.add(player, key);

      InventoryService.equip(player, definition.getBodyPart(), key);
    }

    PlayerAppearanceDefaults.applyDefaults(player);

    if (player.getAnimations() != null) player.getAnimations().refresh();
  }

  public static Result begin(String script, String npcName, Player player) {

    if (script == null || script.isBlank()) return Result.empty();

    int begin = script.indexOf("Begin");

    if (begin < 0) return Result.empty();

    Matcher firstCommand = COMMAND.matcher(script);

    int end = firstCommand.find(begin) ? firstCommand.start() : script.length();

    Matcher firstState = LEGACY_STATE_MARKER.matcher(script);

    if (firstState.find(begin)) end = Math.min(end, firstState.start());

    return execute(
        script.substring(begin + 5, end), npcName, player, scriptLocals(script, npcName, player));
  }

  public static Result event(
      String script, String npcName, Player player, int npcHp, int npcMaxHp) {

    if (script == null || script.isBlank() || player == null) return Result.empty();

    return execute(
        "long NPC_HP = " + npcHp + ";\nlong NPC_MAXHP = " + npcMaxHp + ";\n" + script,
        npcName,
        player);
  }

  public static Result respond(String script, String npcName, String input, Player player) {

    return respond(script, npcName, input, player, null);
  }

  public static Result respond(
      String script, String npcName, String input, Player player, String conversationState) {

    if (script == null || input == null || player == null) return Result.empty();

    Result legacyState = respondLegacyState(script, npcName, input, player, conversationState);

    if (legacyState.handled()) return legacyState;

    Matcher commands = COMMAND.matcher(script);

    List<Integer> starts = new ArrayList<>();

    while (commands.find()) starts.add(commands.start());

    String normalizedInput = normalize(input);

    for (int i = 0; i < starts.size(); i++) {

      int start = starts.get(i);

      int open = script.indexOf('(', start);

      int close = matching(script, open, '(', ')');

      if (close < 0) continue;

      List<String> keywords = strings(script.substring(open + 1, close));

      String commandKind = script.substring(start, open).trim();

      List<Long> parameters =
          commandKind.equals("ParamCmd") && !keywords.isEmpty()
              ? matchParameters(keywords.get(keywords.size() - 1), normalizedInput)
              : null;

      if (commandKind.equals("ParamCmd") && parameters == null) continue;

      boolean requireAll = script.substring(start, open).contains("CmdAND");

      java.util.function.Predicate<String> contained =
          keyword -> {
            String normalized = normalize(keyword);

            return keywordMatches(normalizedInput, normalized);
          };

      boolean match =
          commandKind.equals("ParamCmd")
              || (requireAll
                  ? keywords.stream().allMatch(contained)
                  : keywords.stream().anyMatch(contained));

      if (!match) continue;

      Matcher boundary = SECTION.matcher(script);

      int end = boundary.find(close + 1) ? boundary.start() : script.length();

      Matcher stateBoundary = LEGACY_STATE_MARKER.matcher(script);

      if (stateBoundary.find(close + 1)) end = Math.min(end, stateBoundary.start());

      String body = script.substring(close + 1, end);

      if (parameters != null) {

        StringBuilder declarations = new StringBuilder();

        for (int p = 0; p < parameters.size(); p++)
          declarations
              .append("long PARAM_")
              .append(p)
              .append(" = ")
              .append(parameters.get(p))
              .append(";\n");

        body = declarations + body;
      }

      return execute(body, npcName, player, scriptLocals(script, npcName, player));
    }

    Matcher defaultSection = Pattern.compile("(?m)^\\s*Default\\s*$").matcher(script);

    if (defaultSection.find()) {

      int end = script.indexOf("EndTalk", defaultSection.end());

      return execute(
          script.substring(defaultSection.end(), end < 0 ? script.length() : end),
          npcName,
          player,
          scriptLocals(script, npcName, player));
    }

    return Result.empty();
  }

  private static Result respondLegacyState(
      String script, String npcName, String input, Player player, String conversationState) {

    if (conversationState == null || conversationState.isBlank()) return Result.empty();

    Matcher markers = LEGACY_STATE_MARKER.matcher(script);

    List<LegacyStateSection> sections = new ArrayList<>();

    while (markers.find())
      sections.add(new LegacyStateSection(markers.group(1), markers.start(), markers.end()));

    String spoken = " " + normalize(input) + " ";

    for (int i = 0; i < sections.size(); i++) {

      LegacyStateSection section = sections.get(i);

      if (!Pattern.compile("\\bYesNo\\s*==\\s*" + Pattern.quote(conversationState) + "\\b")
          .matcher(section.condition())
          .find()) continue;

      List<String> words =
          strings(section.condition()).stream()
              .map(NpcScriptEngine::normalize)
              .filter(word -> !word.isBlank())
              .toList();

      boolean fallback = words.isEmpty();

      boolean matches = fallback || legacyKeywordCondition(section.condition(), words, spoken);

      if (!matches) continue;

      int end = i + 1 < sections.size() ? sections.get(i + 1).start() : script.length();

      Matcher standard = SECTION.matcher(script);

      if (standard.find(section.end()) && standard.start() < end) end = standard.start();

      return execute(
          script.substring(section.end(), end),
          npcName,
          player,
          scriptLocals(script, npcName, player));
    }

    return Result.empty();
  }

  private static boolean legacyKeywordCondition(
      String condition, List<String> words, String spoken) {

    String input = spoken.trim();

    List<Boolean> present = words.stream().map(word -> keywordMatches(input, word)).toList();

    boolean keywordOr = condition.contains("||");

    return keywordOr
        ? present.stream().anyMatch(Boolean::booleanValue)
        : present.stream().allMatch(Boolean::booleanValue);
  }

  private static boolean keywordMatches(String input, String keyword) {

    if (keyword == null || keyword.isBlank()) return false;

    if (keyword.chars().allMatch(Character::isDigit))
      return (" " + input + " ").contains(" " + keyword + " ");

    return input.contains(keyword);
  }

  private record LegacyStateSection(String condition, int start, int end) {}

  static Map<String, String> keywords(String script) {

    if (script == null || script.isBlank()) return Map.of();

    Map<String, String> result = new LinkedHashMap<>();

    Matcher commands = COMMAND.matcher(script);

    while (commands.find()) {

      int start = commands.start();

      int open = script.indexOf('(', start);

      int close = matching(script, open, '(', ')');

      if (close < 0) continue;

      String commandKind = script.substring(start, open).trim();

      if (commandKind.equals("ParamCmd")) continue;

      List<String> keywords =
          strings(script.substring(open + 1, close)).stream()
              .filter(keyword -> !keyword.isBlank())
              .toList();

      String sentence = commandKind.contains("CmdAND") ? String.join(" ", keywords) : null;

      for (String keyword : keywords) {

        result.putIfAbsent(keyword, sentence == null ? keyword : sentence);
      }
    }

    return result;
  }

  public static Result respondYesNo(
      String script, String npcName, String state, boolean yes, Player player) {

    if (script == null || state == null || player == null) return Result.empty();

    Pattern marker =
        Pattern.compile(
            "(?m)^\\s*(YES|NO|YesNoELSE)\\s*\\(\\s*" + Pattern.quote(state) + "\\s*\\)");

    Matcher matcher = marker.matcher(script);

    List<Section> sections = new ArrayList<>();

    while (matcher.find())
      sections.add(new Section(matcher.group(1), matcher.start(), matcher.end()));

    String wanted = yes ? "YES" : "NO";

    Section selected =
        sections.stream()
            .filter(section -> section.kind.equals(wanted))
            .findFirst()
            .orElseGet(
                () ->
                    sections.stream()
                        .filter(section -> section.kind.equals("YesNoELSE"))
                        .findFirst()
                        .orElse(null));

    if (selected == null) return Result.empty();

    Pattern anyMarker =
        Pattern.compile(
            "(?m)^\\s*(?:Command\\d*|CmdAND\\d*|YES|NO|YesNoELSE|Default)\\s*(?:\\(|$)");

    Matcher next = anyMarker.matcher(script);

    int end = script.length();

    if (next.find(selected.end)) end = next.start();

    return execute(
        script.substring(selected.end, end),
        npcName,
        player,
        scriptLocals(script, npcName, player));
  }

  private static Result execute(String block, String npcName, Player player) {

    return execute(block, npcName, player, Map.of());
  }

  private static Map<String, Long> scriptLocals(String script, String npcName, Player player) {

    int begin = script.indexOf("Begin");

    String preamble = begin < 0 ? script : script.substring(0, begin);

    Map<String, Long> locals = new LinkedHashMap<>();

    Pattern declaration =
        Pattern.compile(
            "(?:CONSTANT|BOOL|int|long|double|DWORD|BYTE|WORD|Fix|unsigned\\s+int)\\s+(\\w+)\\s*=\\s*(.+?);?$");

    for (String statement : statements(preamble)) {

      Matcher matcher = declaration.matcher(statement.trim());

      if (matcher.find())
        locals.put(matcher.group(1), number(matcher.group(2), npcName, player, locals));
    }

    return locals;
  }

  private static Result execute(
      String block, String npcName, Player player, Map<String, Long> initialLocals) {

    ArrayDeque<Branch> branches = new ArrayDeque<>();

    ArrayDeque<SwitchState> switches = new ArrayDeque<>();

    ArrayDeque<Integer> repeats = new ArrayDeque<>();

    Map<String, Long> locals = new LinkedHashMap<>(initialLocals);

    Map<String, String> itemHandles = new LinkedHashMap<>();

    StringBuilder response = new StringBuilder();

    List<String> systemMessages = new ArrayList<>();

    List<String> shop = new ArrayList<>();

    List<SellRule> sellRules = new ArrayList<>();

    List<String> taughtSpells = new ArrayList<>(),
        taughtSkills = new ArrayList<>(),
        trainedSkills = new ArrayList<>();

    List<SkillOffer> skillOffers = new ArrayList<>();

    List<FormulaOffer> formulaOffers = new ArrayList<>();

    List<String> targetSpells = new ArrayList<>(), selfSpells = new ArrayList<>();

    List<SummonRequest> summons = new ArrayList<>();

    int xp = 0;

    boolean heal = false, end = false;

    boolean legacyWhile = false;

    String pendingYesNo = null;

    boolean effect = false, selfDestruct = false;

    int npcHpOverride = Integer.MIN_VALUE;

    for (String statement : statements(block)) {

      String s = statement.trim();

      if (s.startsWith(";")) s = s.substring(1).trim();

      String args;

      if ((args = macroArgs(s, "SWITCH")) != null || (args = macroArgs(s, "switch")) != null) {

        switches.push(new SwitchState(number(args, npcName, player, locals), false, false));

        continue;
      }

      if ((args = macroArgs(s, "CASE")) != null) {

        if (!switches.isEmpty()) {

          SwitchState old = switches.pop();

          boolean selected = !old.matched && old.value == number(args, npcName, player, locals);

          switches.push(new SwitchState(old.value, old.matched || selected, selected));
        }

        continue;
      }

      if (s.equals("ENDCASE")) {

        if (!switches.isEmpty()) {

          SwitchState old = switches.pop();

          switches.push(new SwitchState(old.value, old.matched, false));
        }

        continue;
      }

      if (s.equals("OTHERWISE")) {

        if (!switches.isEmpty()) {

          SwitchState old = switches.pop();

          switches.push(new SwitchState(old.value, true, !old.matched));
        }

        continue;
      }

      if (s.equals("ENDSWITCH")) {

        if (!switches.isEmpty()) switches.pop();

        continue;
      }

      if ((args = macroArgs(s, "FOR")) != null) {

        List<String> values = splitArgs(args);

        int count =
            values.size() < 2
                ? 0
                : Math.max(
                    0,
                    safeInt(
                        number(values.get(1), npcName, player, locals)
                            - number(values.get(0), npcName, player, locals)));

        repeats.push(count);

        continue;
      }

      if (s.equals("ENDFOR")) {

        if (!repeats.isEmpty()) repeats.pop();

        continue;
      }

      if (s.startsWith("IF") && macroArgs(s, "IF") != null) {

        boolean parent = active(branches, switches);

        boolean selected = parent && bool(macroArgs(s, "IF"), npcName, player, locals);

        branches.push(new Branch(parent, selected, selected));

        continue;
      }

      if (s.startsWith("ELSEIF") && macroArgs(s, "ELSEIF") != null) {

        if (!branches.isEmpty()) {

          Branch old = branches.pop();

          boolean selected =
              old.parentActive
                  && !old.anyTaken
                  && bool(macroArgs(s, "ELSEIF"), npcName, player, locals);

          branches.push(new Branch(old.parentActive, old.anyTaken || selected, selected));
        }

        continue;
      }

      if (s.equals("ELSE")) {

        if (!branches.isEmpty()) {

          Branch old = branches.pop();

          boolean selected = old.parentActive && !old.anyTaken;

          branches.push(new Branch(old.parentActive, true, selected));
        }

        continue;
      }

      if (s.equals("ENDIF")) {

        if (!branches.isEmpty()) branches.pop();

        continue;
      }

      if (!active(branches, switches)) continue;

      Matcher inlineIncrement =
          Pattern.compile("if\\s*\\((.+)\\)\\s*\\+\\+(\\w+)\\s*;?").matcher(s);

      if (inlineIncrement.matches()) {

        if (bool(inlineIncrement.group(1), npcName, player, locals))
          locals.merge(inlineIncrement.group(2), 1L, Long::sum);

        continue;
      }

      if (s.startsWith("while") && s.contains("(")) {

        legacyWhile = true;

        continue;
      }

      if (s.endsWith(".MakeUpper();") || s.endsWith(".MakeUpper()")) continue;

      Matcher assignment =
          Pattern.compile(
                  "(?:CONSTANT|BOOL|int|long|double|DWORD|BYTE|WORD|Fix|unsigned\\s+int)\\s+(\\w+)\\s*=\\s*(.+?);?$")
              .matcher(s);

      if (assignment.find()) {

        locals.put(assignment.group(1), number(assignment.group(2), npcName, player, locals));

        continue;
      }

      assignment = Pattern.compile("(\\w+)\\s*=\\s*(.+?);?$").matcher(s);

      if (assignment.matches()) {

        if (assignment.group(1).equals("YesNo")) pendingYesNo = assignment.group(2).trim();
        else locals.put(assignment.group(1), number(assignment.group(2), npcName, player, locals));

        continue;
      }

      if (s.startsWith("Conversation") || s.equals("\"\"") || s.startsWith("//")) continue;

      if ((args = macroArgs(s, "PRIVATE_SYSTEM_MESSAGE")) != null) {

        List<String> values = strings(args);

        if (!values.isEmpty()) systemMessages.add(values.get(0).replace("%s", playerName(player)));

        continue;
      }

      if ((args = macroArgs(s, "GLOBAL_SYSTEM_MESSAGE")) != null
          || (args = macroArgs(s, "CHATTER_SHOUT")) != null
          || (args = macroArgs(s, "SHOUT")) != null) {

        List<String> values = strings(args);

        if (!values.isEmpty())
          systemMessages.add(values.get(values.size() - 1).replace("%s", playerName(player)));

        continue;
      }

      if (s.startsWith("INTL") || s.startsWith("FORMAT")) {

        String text = renderedText(s, npcName, player, locals);

        if (!text.isEmpty()) {

          if (!response.isEmpty()) response.append('\n');

          response.append(text);
        }

        continue;
      }

      if ((args = macroArgs(s, "GiveFlag")) != null) {

        setFlag(player, args, 1, "", npcName, locals);

        effect = true;

      } else if ((args = macroArgs(s, "RemFlag")) != null) {

        setFlag(player, args, 0, "", npcName, locals);

        effect = true;

      } else if ((args = macroArgs(s, "GiveGlobalFlag")) != null) {

        setGlobalFlag(args, npcName, player, locals);

        effect = true;

      } else if ((args = macroArgs(s, "GiveNPCFlag")) != null) {

        setFlag(player, args, 1, "npc:" + npcName + ":", npcName, locals);

        effect = true;

      } else if ((args = macroArgs(s, "GiveItem")) != null) {

        for (int i = 0; i < repetitions(repeats); i++) giveItem(player, firstArg(args));

      } else if ((args = macroArgs(s, "TakeItem")) != null) {

        for (int i = 0; i < repetitions(repeats); i++) takeItem(player, firstArg(args));

      } else if ((args = macroArgs(s, "GiveGold")) != null)
        player.addGold(safeInt(number(args, npcName, player, locals)) * repetitions(repeats));
      else if ((args = macroArgs(s, "GiveGoldNoEcho")) != null)
        player.addGold(safeInt(number(args, npcName, player, locals)) * repetitions(repeats));
      else if ((args = macroArgs(s, "TakeGold")) != null)
        player.addGold(-safeInt(number(args, npcName, player, locals)) * repetitions(repeats));
      else if ((args = macroArgs(s, "GiveXP")) != null)
        xp += safeInt(number(args, npcName, player, locals)) * repetitions(repeats);
      else if ((args = macroArgs(s, "GiveKarma")) != null) {

        player.setKarma(safeInt((long) player.getKarma() + number(args, npcName, player, locals)));

        effect = true;

      } else if ((args = macroArgs(s, "TELEPORT")) != null) {

        List<String> values = splitArgs(args);

        if (values.size() >= 3) {

          int x = safeInt(number(values.get(0), npcName, player, locals));

          int y = safeInt(number(values.get(1), npcName, player, locals));

          int z = safeInt(number(values.get(2), npcName, player, locals));

          player.setWorldPosition(
              x * com.perso.T4C.config.GameConstants.GRID_W,
              y * com.perso.T4C.config.GameConstants.GRID_H,
              z);

          effect = true;
        }

      } else if ((args = macroArgs(s, "SetDeathLocation")) != null) {

        List<String> values = splitArgs(args);

        if (values.size() >= 3) {

          player.setRespawnPoint(
              number(values.get(0), npcName, player, locals)
                  * com.perso.T4C.config.GameConstants.GRID_W,
              number(values.get(1), npcName, player, locals)
                  * com.perso.T4C.config.GameConstants.GRID_H,
              safeInt(number(values.get(2), npcName, player, locals)));

          long deathX = number(values.get(0), npcName, player, locals) & 0xFFFL;

          long deathY = number(values.get(1), npcName, player, locals) & 0xFFFL;

          long deathZ = number(values.get(2), npcName, player, locals) & 0xFFL;

          player.setQuestFlag(
              "__FLAG_DEATH_LOCATION", (int) ((deathX << 20) | (deathY << 8) | deathZ));

          effect = true;
        }

      } else if ((args = macroArgs(s, "REMORT_TO")) != null) {

        List<String> values = splitArgs(args);

        if (values.size() >= 3) {

          rebirth(player, npcName);

          player.setWorldPosition(
              number(values.get(0), npcName, player, locals)
                  * com.perso.T4C.config.GameConstants.GRID_W,
              number(values.get(1), npcName, player, locals)
                  * com.perso.T4C.config.GameConstants.GRID_H,
              safeInt(number(values.get(2), npcName, player, locals)));

          effect = true;
        }

      } else if ((args = macroArgs(s, "SET_STR")) != null) {

        player.setStrength(safeInt(number(args, npcName, player, locals)));

        effect = true;

      } else if ((args = macroArgs(s, "SET_AGI")) != null) {

        player.setDexterity(safeInt(number(args, npcName, player, locals)));

        effect = true;

      } else if ((args = macroArgs(s, "SET_END")) != null) {

        player.setEndurance(safeInt(number(args, npcName, player, locals)));

        effect = true;

      } else if ((args = macroArgs(s, "SET_INT")) != null) {

        player.setIntelligence(safeInt(number(args, npcName, player, locals)));

        effect = true;

      } else if ((args = macroArgs(s, "SET_WIS")) != null) {

        player.setWisdom(safeInt(number(args, npcName, player, locals)));

        effect = true;

      } else if ((args = elementalArgs(s, "RESIST")) != null) {

        player.setBaseElementResistance(
            elementName(s), safeInt(number(args, npcName, player, locals)));

        effect = true;

      } else if ((args = elementalArgs(s, "POWER")) != null) {

        player.setBaseElementPower(elementName(s), safeInt(number(args, npcName, player, locals)));

        effect = true;

      } else if ((args = macroArgs(s, "GetItemHandle")) != null) {

        List<String> values = splitArgs(args);

        if (values.size() >= 2)
          itemKey(values.get(0))
              .ifPresent(
                  key -> {
                    String handle = values.get(1).trim();

                    itemHandles.put(handle, key);

                    ItemDefinition item = ItemRegistry.findByKey(key);

                    if (item != null) locals.put(handle, (long) item.getNumId());
                  });

      } else if ((args = macroArgs(s, "TakeItemHandle")) != null) {

        String key = itemHandles.get(firstArg(args));

        if (key != null) {

          int count = legacyWhile ? InventoryService.count(player, key) : 1;

          for (int i = 0; i < count; i++) InventoryService.destroyOne(player, key);

          effect = true;
        }

      } else if ((args = macroArgs(s, "GiveUnitFlag")) != null) {

        List<String> values = splitArgs(args);

        if (values.size() >= 3) {

          String key = itemHandles.get(values.get(0).trim());

          if (key != null)
            player.setQuestFlag(
                "item:" + key + ":" + values.get(1).trim(),
                safeInt(number(values.get(2), npcName, player, locals)));

          effect = true;
        }

      } else if (s.contains("SetHP(")) {

        int open = s.indexOf("SetHP(");

        int close = matching(s, open + 5, '(', ')');

        if (close > open) {

          String hpArgs = s.substring(open + 6, close);

          int hp = safeInt(number(firstArg(hpArgs), npcName, player, locals));

          if (s.contains("self->SetHP")) npcHpOverride = hp;
          else player.setCurrentHp(Math.max(0, Math.min(player.getMaxHp(), hp)));

          effect = true;
        }

      } else if ((args = embeddedArgs(s, "SetMaxHP")) != null) {

        player.setMaxHp(Math.max(1, safeInt(number(firstArg(args), npcName, player, locals))));

        player.setCurrentHp(Math.min(player.getCurrentHp(), player.getMaxHp()));

        effect = true;

      } else if ((args = embeddedArgs(s, "SetMaxMana")) != null) {

        player.setMaxMana(Math.max(0, safeInt(number(firstArg(args), npcName, player, locals))));

        player.setMana(Math.min(player.getMana(), player.getMaxMana()));

        effect = true;

      } else if ((args = embeddedArgs(s, "SetGold")) != null) {

        player.setGold(safeInt(number(firstArg(args), npcName, player, locals)));

        effect = true;

      } else if ((args = embeddedArgs(s, "SetFlag")) != null) {

        List<String> values = splitArgs(args);

        if (values.size() >= 2)
          player.setQuestFlag(
              values.get(0).trim(), safeInt(number(values.get(1), npcName, player, locals)));

        effect = true;

      } else if ((args = macroArgs(s, "CastSpellTarget")) != null) {

        String spell = unquote(firstArg(args));

        NpcSpec caster = NpcFactoryRegistry.specification(npcName);

        String scriptedSpell = caster == null ? null : caster.sourceEvents().get("@spell." + spell);

        if (scriptedSpell != null) {

          Result nested = execute(scriptedSpell, npcName, player, locals);

          systemMessages.addAll(nested.systemMessages());

          targetSpells.addAll(nested.targetSpells());

          selfSpells.addAll(nested.selfSpells());

          heal |= nested.heal();

          effect |= nested.effect();

        } else targetSpells.add(spell);

      } else if ((args = macroArgs(s, "CastSpellSelf")) != null)
        selfSpells.add(unquote(firstArg(args)));
      else if ((args = macroArgs(s, "SUMMON2")) != null) collectSummon(args, summons, true);
      else if ((args = macroArgs(s, "SUMMON")) != null) collectSummon(args, summons, false);
      else if ((args = macroArgs(s, "SetYesNo")) != null) pendingYesNo = firstArg(args);
      else if ((args = macroArgs(s, "AddBuyItem")) != null) {

        List<String> values = splitArgs(args);

        if (values.size() >= 2) itemKey(values.get(1)).ifPresent(shop::add);

      } else if ((args = macroArgs(s, "AddTeachSkill")) != null) {

        List<String> values = splitArgs(args);

        String id = unquote(values.get(0));

        if (id.startsWith("spell.")) taughtSpells.add(id);
        else if (!id.isBlank()) {

          taughtSkills.add(id);

          int initial =
              values.size() > 1 ? safeInt(number(values.get(1), npcName, player, locals)) : 1;

          int cost =
              values.size() > 2 ? safeInt(number(values.get(2), npcName, player, locals)) : 0;

          skillOffers.add(new SkillOffer(id, Math.max(1, initial), Math.max(0, cost), true));
        }

      } else if ((args = macroArgs(s, "AddTrainSkill")) != null) {

        List<String> values = splitArgs(args);

        String id = unquote(values.get(0));

        if (!id.isBlank()) {

          trainedSkills.add(id);

          int maximum =
              values.size() > 1
                  ? safeInt(number(values.get(1), npcName, player, locals))
                  : Integer.MAX_VALUE;

          int cost =
              values.size() > 2 ? safeInt(number(values.get(2), npcName, player, locals)) : 0;

          skillOffers.add(new SkillOffer(id, Math.max(0, maximum), Math.max(0, cost), false));
        }

      } else if ((args = macroArgs(s, "AddTeachFormule")) != null) {

        List<String> values = splitArgs(args);

        if (values.size() >= 2)
          formulaOffers.add(
              new FormulaOffer(
                  safeInt(number(values.get(0), npcName, player, locals)),
                  Math.max(0, safeInt(number(values.get(1), npcName, player, locals)))));

      } else if ((args = macroArgs(s, "AddSellItem")) != null) {

        List<String> values = splitArgs(args);

        if (values.size() >= 3)
          sellRules.add(
              new SellRule(
                  values.get(0),
                  number(values.get(1), npcName, player, locals),
                  number(values.get(2), npcName, player, locals)));

      } else if (s.startsWith("SendBuyItemList")) {

      } else if (s.startsWith("SendTeachSkillList")
          || s.startsWith("SendTrainSkillList")
          || s.startsWith("SendTeachFormuleList")
          || s.startsWith("CreateFormuleList")) {

      } else if (s.startsWith("HealPlayer") || s.startsWith("Heal")) heal = true;
      else if (s.startsWith("SELF_DESTRUCT")) {

        selfDestruct = true;

        effect = true;

      } else if (s.contains("BREAK")) end = true;
    }

    return new Result(
        response.isEmpty() ? null : response.toString(),
        List.copyOf(systemMessages),
        List.copyOf(shop),
        List.copyOf(sellRules),
        List.copyOf(taughtSpells),
        List.copyOf(taughtSkills),
        List.copyOf(trainedSkills),
        List.copyOf(targetSpells),
        List.copyOf(selfSpells),
        List.copyOf(skillOffers),
        List.copyOf(formulaOffers),
        List.copyOf(summons),
        xp,
        heal,
        end,
        pendingYesNo,
        selfDestruct,
        npcHpOverride,
        effect);
  }

  private static void collectSummon(
      String args, List<SummonRequest> result, boolean explicitWorld) {

    List<String> values = splitArgs(args);

    if (values.size() < 3) return;

    String monster = unquote(values.get(0));

    String intl = functionArgs(values.get(0), "INTL");

    if (intl != null) {

      List<String> names = strings(intl);

      if (!names.isEmpty()) monster = names.get(names.size() - 1);
    }

    result.add(
        new SummonRequest(
            monster,
            values.get(1),
            values.get(2),
            explicitWorld && values.size() > 3 ? values.get(3) : null));
  }

  private static boolean bool(
      String expression, String npcName, Player player, Map<String, Long> locals) {

    String e =
        stripOuter(expression.trim().replaceAll("\\bAND\\b", "&&").replaceAll("\\bOR\\b", "||"));

    int split = topLevel(e, "||");

    if (split >= 0)
      return bool(e.substring(0, split), npcName, player, locals)
          || bool(e.substring(split + 2), npcName, player, locals);

    split = topLevel(e, "&&");

    if (split >= 0)
      return bool(e.substring(0, split), npcName, player, locals)
          && bool(e.substring(split + 2), npcName, player, locals);

    if (e.startsWith("!")) return !bool(e.substring(1), npcName, player, locals);

    for (String op : List.of("==", "!=", ">=", "<=", ">", "<")) {

      split = topLevel(e, op);

      if (split >= 0) {

        long left = number(e.substring(0, split), npcName, player, locals);

        long right = number(e.substring(split + op.length()), npcName, player, locals);

        return switch (op) {
          case "==" -> left == right;

          case "!=" -> left != right;

          case ">=" -> left >= right;

          case "<=" -> left <= right;

          case ">" -> left > right;

          default -> left < right;
        };
      }
    }

    return number(e, npcName, player, locals) != 0;
  }

  private static long number(
      String expression, String npcName, Player player, Map<String, Long> locals) {

    String e =
        stripOuter(
            expression
                .trim()
                .replaceAll("[;}]$", "")
                .replaceAll("(?i)\\s+(?:MINUTES?|HOURS?|SECONDS?)\\s+TDELAY\\s*$", "")
                .trim());

    for (String op : List.of("|", "&", ">>", "<<")) {

      int bit = topLevel(e, op);

      if (bit >= 0
          && !(op.equals("|") && e.startsWith("||", bit))
          && !(op.equals("&") && e.startsWith("&&", bit))) {

        long a = number(e.substring(0, bit), npcName, player, locals);

        long b = number(e.substring(bit + op.length()), npcName, player, locals);

        return switch (op) {
          case "|" -> a | b;

          case "&" -> a & b;

          case ">>" -> a >> b;

          default -> a << b;
        };
      }
    }

    for (String op : List.of("+", "-", "*", "/", "%")) {

      int split = topLevelArithmetic(e, op);

      if (split > 0) {

        long a = number(e.substring(0, split), npcName, player, locals);

        long b = number(e.substring(split + 1), npcName, player, locals);

        return switch (op) {
          case "+" -> a + b;

          case "-" -> a - b;

          case "*" -> a * b;

          case "/" -> b == 0 ? 0 : a / b;

          default -> b == 0 ? 0 : a % b;
        };
      }
    }

    String args;

    if ((args = functionArgs(e, "CheckFlag")) != null) return player.getQuestFlag(firstArg(args));

    if ((args = functionArgs(e, "lpUser->GetGodFlags")) != null) return 0;

    if ((args = functionArgs(e, "IsInRange")) != null) return 1;

    if ((args = functionArgs(e, "ViewFlag")) != null
        || (args = functionArgs(e, "target->ViewFlag")) != null)
      return player.getQuestFlag(firstArg(args));

    if ((args = functionArgs(e, "CheckNPCFlag")) != null)
      return player.getQuestFlag("npc:" + npcName + ":" + firstArg(args));

    if ((args = functionArgs(e, "CheckGlobalFlag")) != null) {

      String flag = firstArg(args);

      Long expires = GLOBAL_FLAG_EXPIRATIONS.get(flag);

      if (expires != null && expires <= System.currentTimeMillis()) {

        GLOBAL_FLAG_EXPIRATIONS.remove(flag);

        GLOBAL_FLAGS.remove(flag);

        return 0;
      }

      return GLOBAL_FLAGS.getOrDefault(flag, 0);
    }

    if ((args = functionArgs(e, "CheckUnitFlag")) != null) {

      List<String> values = splitArgs(args);

      if (values.size() < 2) return 0;

      ItemDefinition item =
          ItemRegistry.findByNumId(safeInt(locals.getOrDefault(values.get(0).trim(), 0L)));

      return item == null
          ? 0
          : player.getQuestFlag("item:" + item.getKey() + ":" + values.get(1).trim());
    }

    if ((args = functionArgs(e, "CheckItem")) != null) {

      String key = itemKey(firstArg(args)).orElse(null);

      return key == null ? 0 : InventoryService.count(player, key);
    }

    if ((args = functionArgs(e, "rnd.roll")) != null) {

      String diceArgs = functionArgs(stripOuter(args), "dice");

      if (diceArgs == null) return number(args, npcName, player, locals);

      List<String> values = splitArgs(diceArgs);

      int rolls =
          values.isEmpty()
              ? 1
              : Math.max(0, safeInt(number(values.get(0), npcName, player, locals)));

      int sides =
          values.size() < 2
              ? 1
              : Math.max(1, safeInt(number(values.get(1), npcName, player, locals)));

      long total = values.size() < 3 ? 0 : number(values.get(2), npcName, player, locals);

      for (int i = 0; i < rolls; i++)
        total += java.util.concurrent.ThreadLocalRandom.current().nextInt(1, sides + 1);

      return total;
    }

    if ((args = functionArgs(e, "rnd")) != null) {

      List<String> values = splitArgs(args);

      if (values.size() < 2) return 0;

      long minimum = number(values.get(0), npcName, player, locals);

      long maximum = number(values.get(1), npcName, player, locals);

      if (maximum < minimum) {

        long swap = minimum;

        minimum = maximum;

        maximum = swap;
      }

      if (maximum == minimum) return minimum;

      return java.util.concurrent.ThreadLocalRandom.current().nextLong(minimum, maximum + 1);
    }

    for (String wrapper : List.of("DWORD", "INT", "BYTE", "WORD", "Fix")) {

      if ((args = functionArgs(e, wrapper)) != null) return number(args, npcName, player, locals);
    }

    if ((args = functionArgs(e, "NUM_PARAM")) != null)
      return locals.getOrDefault("PARAM_" + firstArg(args), 0L);

    if ((args = functionArgs(e, "UserSkill")) != null)
      return player.getSkillLevel(unquote(firstArg(args)));

    if ((args = functionArgs(e, "abs")) != null)
      return Math.abs(number(args, npcName, player, locals));

    if ((args = functionArgs(e, "pow")) != null) {

      List<String> values = splitArgs(args);

      return values.size() < 2
          ? 0
          : Math.round(
              Math.pow(
                  number(values.get(0), npcName, player, locals),
                  number(values.get(1), npcName, player, locals)));
    }

    String timeCall = e.replace("TFCTime::", "");

    java.time.LocalTime now = java.time.LocalTime.now();

    if (timeCall.equals("IsDay()")) return now.getHour() >= 6 && now.getHour() < 18 ? 1 : 0;

    if (timeCall.equals("IsNight()")) return now.getHour() >= 20 || now.getHour() < 5 ? 1 : 0;

    if (timeCall.equals("IsEvening()")) return now.getHour() >= 18 && now.getHour() < 20 ? 1 : 0;

    if (timeCall.equals("IsSleepTime()")) return now.getHour() >= 22 || now.getHour() < 6 ? 1 : 0;

    if (timeCall.equals("IsMorning()")) return now.getHour() >= 6 && now.getHour() < 12 ? 1 : 0;

    if (timeCall.equals("IsAfterNoon()")) return now.getHour() >= 12 && now.getHour() < 18 ? 1 : 0;

    if (timeCall.equals("Hour()")) return now.getHour();

    if (timeCall.equals("Minute()")) return now.getMinute();

    return switch (e) {
      case "USER_LEVEL" -> player.getLevel();

      case "USER_KARMA" -> player.getKarma();

      case "USER_GOLD", "Gold" -> player.getGold();

      case "USER_STR" -> player.getStrength();

      case "USER_DEX", "USER_AGI" -> player.getDexterity();

      case "USER_END" -> player.getEndurance();

      case "USER_INT" -> player.getIntelligence();

      case "USER_WIS" -> player.getWisdom();

      case "USER_HP" -> player.getCurrentHp();

      case "USER_MAXHP" -> player.getMaxHp();

      case "target->GetHP()" -> player.getCurrentHp();

      case "target->GetMaxMana()" -> player.getMaxMana();

      case "target->GetWL().X" ->
          Math.round(player.getCoordinates().getX() / com.perso.T4C.config.GameConstants.GRID_W);

      case "target->GetWL().Y" ->
          Math.round(player.getCoordinates().getY() / com.perso.T4C.config.GameConstants.GRID_H);

      case "target->GetWL().world" -> player.getCoordinates().getZ();

      case "USER_TRUE_STR" -> player.getStrength();

      case "USER_TRUE_AGI", "USER_TRUE_DEX" -> player.getDexterity();

      case "USER_TRUE_END" -> player.getEndurance();

      case "USER_TRUE_INT" -> player.getIntelligence();

      case "USER_TRUE_WIS" -> player.getWisdom();

      case "USER_TRUE_FIRE_RESIST" -> player.getElementResistance("fire");

      case "USER_TRUE_WATER_RESIST" -> player.getElementResistance("water");

      case "USER_TRUE_AIR_RESIST" -> player.getElementResistance("air");

      case "USER_TRUE_EARTH_RESIST" -> player.getElementResistance("earth");

      case "USER_TRUE_DARK_RESIST" -> player.getElementResistance("dark");

      case "USER_TRUE_LIGHT_RESIST" -> player.getElementResistance("light");

      case "USER_TRUE_FIRE_POWER" -> player.getElementPower("fire");

      case "USER_TRUE_WATER_POWER" -> player.getElementPower("water");

      case "USER_TRUE_AIR_POWER" -> player.getElementPower("air");

      case "USER_TRUE_EARTH_POWER" -> player.getElementPower("earth");

      case "USER_TRUE_DARK_POWER" -> player.getElementPower("dark");

      case "USER_TRUE_LIGHT_POWER" -> player.getElementPower("light");

      case "USER_TRUE_MAXHP" -> player.getMaxHp();

      case "TRUE" -> 1;

      case "FALSE", "NULL" -> 0;

      case "ACK_MAXREMORTS" -> GameConstants.REBIRTH_MAX_REMORTS;

      case "CurrentRound" -> System.currentTimeMillis() / 1000L;

      default -> locals.getOrDefault(e, parseLong(e));
    };
  }

  private static void setFlag(
      Player player,
      String args,
      int defaultValue,
      String prefix,
      String npcName,
      Map<String, Long> locals) {

    List<String> values = splitArgs(args);

    if (values.isEmpty()) return;

    int value =
        values.size() > 1 ? safeInt(number(values.get(1), npcName, player, locals)) : defaultValue;

    String valueExpression = values.size() > 1 ? values.get(1) : "";

    Matcher delay =
        Pattern.compile("(?i)\\b(MINUTES?|HOURS?|SECONDS?)\\s+TDELAY\\b").matcher(valueExpression);

    if (delay.find()) {

      long multiplier =
          delay.group(1).toUpperCase(Locale.ROOT).startsWith("HOUR")
              ? 3_600_000L
              : delay.group(1).toUpperCase(Locale.ROOT).startsWith("MINUTE") ? 60_000L : 1_000L;

      long duration = Math.max(0L, value) * multiplier;

      int round = safeInt((System.currentTimeMillis() + duration) / 1000L);

      player.setTimedQuestFlag(prefix + values.get(0).trim(), round, duration);

    } else {

      player.setQuestFlag(prefix + values.get(0).trim(), value);
    }
  }

  private static void setGlobalFlag(
      String args, String npcName, Player player, Map<String, Long> locals) {

    List<String> values = splitArgs(args);

    if (values.isEmpty()) return;

    String flag = values.get(0).trim();

    String expression = values.size() > 1 ? values.get(1) : "1";

    Matcher delay =
        Pattern.compile("(?i)\\b(MINUTES?|HOURS?|SECONDS?)\\s+TDELAY\\b").matcher(expression);

    if (delay.find()) {

      long multiplier =
          delay.group(1).toUpperCase(Locale.ROOT).startsWith("HOUR")
              ? 3_600_000L
              : delay.group(1).toUpperCase(Locale.ROOT).startsWith("MINUTE") ? 60_000L : 1_000L;

      long duration = Math.max(0L, number(expression, npcName, player, locals)) * multiplier;

      GLOBAL_FLAGS.put(flag, safeInt((System.currentTimeMillis() + duration) / 1000L));

      GLOBAL_FLAG_EXPIRATIONS.put(flag, System.currentTimeMillis() + duration);

    } else {

      GLOBAL_FLAGS.put(flag, safeInt(number(expression, npcName, player, locals)));

      GLOBAL_FLAG_EXPIRATIONS.remove(flag);
    }
  }

  private static void giveItem(Player player, String token) {

    itemKey(token).ifPresent(key -> InventoryService.add(player, key));
  }

  private static void takeItem(Player player, String token) {

    itemKey(token).ifPresent(key -> InventoryService.destroyOne(player, key));
  }

  private static java.util.Optional<String> itemKey(String token) {

    try {

      ItemDefinition item = ItemRegistry.findByNumId(Integer.parseInt(token.trim()));

      return java.util.Optional.ofNullable(item == null ? null : item.getKey());

    } catch (NumberFormatException ignored) {

      ItemDefinition item = ItemRegistry.findByKey(token.trim().replace("\"", ""));

      return java.util.Optional.ofNullable(item == null ? null : item.getKey());
    }
  }

  private static List<String> statements(String source) {

    List<String> result = new ArrayList<>();

    StringBuilder pending = new StringBuilder();

    int balance = 0;

    for (String raw : source.replace("\r", "").split("\n")) {

      String line = raw.strip();

      if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*") || line.startsWith("*"))
        continue;

      if (!pending.isEmpty()) pending.append(' ');

      pending.append(line);

      balance += parenDelta(line);

      if (balance > 0) continue;

      String complete = pending.toString().trim();

      pending.setLength(0);

      balance = 0;

      if (complete.startsWith("}")
          || complete.equals("{")
          || complete.equals("InitTalk")
          || complete.equals("EndTalk")
          || complete.equals("Begin")
          || complete.equals("Default")) continue;

      Matcher cppCase = Pattern.compile("case\\s+(.+?)\\s*:\\s*(.*)").matcher(complete);

      if (cppCase.matches()) {

        result.add("CASE(" + cppCase.group(1) + ")");

        if (!cppCase.group(2).isBlank()) result.add(cppCase.group(2));

      } else if (complete.startsWith("default:")) {

        result.add("OTHERWISE");

        String tail = complete.substring("default:".length()).trim();

        if (!tail.isBlank()) result.add(tail);

      } else result.add(complete);
    }

    if (!pending.isEmpty()) result.add(pending.toString());

    return result;
  }

  private static int parenDelta(String line) {

    int delta = 0;

    boolean string = false, escape = false;

    for (int i = 0; i < line.length(); i++) {

      char c = line.charAt(i);

      if (escape) {

        escape = false;

        continue;
      }

      if (c == '\\' && string) {

        escape = true;

        continue;
      }

      if (c == '"') {

        string = !string;

        continue;
      }

      if (!string && c == '(') delta++;

      if (!string && c == ')') delta--;
    }

    return delta;
  }

  private static String macroArgs(String statement, String macro) {

    String s = statement.trim();

    if (!s.startsWith(macro)) return null;

    if (s.length() > macro.length()) {

      char boundary = s.charAt(macro.length());

      if (boundary != '(' && !Character.isWhitespace(boundary)) return null;
    }

    int open = s.indexOf('(', macro.length());

    if (open < 0) return null;

    int close = matching(s, open, '(', ')');

    return close < 0 ? null : s.substring(open + 1, close);
  }

  private static String functionArgs(String expression, String function) {

    String s = expression.trim();

    if (!s.startsWith(function)) return null;

    int open = s.indexOf('('), close = matching(s, open, '(', ')');

    return open < 0 || close != s.length() - 1 ? null : s.substring(open + 1, close);
  }

  private static String embeddedArgs(String statement, String function) {

    int name = statement.indexOf(function + "(");

    if (name < 0) return null;

    int open = name + function.length();

    int close = matching(statement, open, '(', ')');

    return close < 0 ? null : statement.substring(open + 1, close);
  }

  private static String elementalArgs(String statement, String suffix) {

    Matcher matcher =
        Pattern.compile("SET_(?:AIR|WATER|EARTH|FIRE|LIGHT|DARK)_" + suffix + "\\s*\\(")
            .matcher(statement);

    if (!matcher.find()) return null;

    int open = statement.indexOf('(', matcher.start());

    int close = matching(statement, open, '(', ')');

    return close < 0 ? null : statement.substring(open + 1, close);
  }

  private static String elementName(String statement) {

    Matcher matcher = Pattern.compile("SET_(AIR|WATER|EARTH|FIRE|LIGHT|DARK)_").matcher(statement);

    return matcher.find() ? matcher.group(1).toLowerCase(Locale.ROOT) : "";
  }

  private static List<String> splitArgs(String args) {

    List<String> result = new ArrayList<>();

    int start = 0, depth = 0;

    boolean string = false, escape = false;

    for (int i = 0; i < args.length(); i++) {

      char c = args.charAt(i);

      if (escape) {

        escape = false;

        continue;
      }

      if (c == '\\' && string) {

        escape = true;

        continue;
      }

      if (c == '"') {

        string = !string;

        continue;
      }

      if (string) continue;

      if (c == '(') depth++;
      else if (c == ')') depth--;
      else if (c == ',' && depth == 0) {

        result.add(args.substring(start, i).trim());

        start = i + 1;
      }
    }

    result.add(args.substring(start).trim());

    return result;
  }

  private static String firstArg(String args) {

    return splitArgs(args).get(0).trim();
  }

  private static int repetitions(ArrayDeque<Integer> repeats) {

    long result = 1;

    for (int count : repeats) result = Math.min(10_000, result * count);

    return (int) result;
  }

  private static String unquote(String value) {

    String v = value == null ? "" : value.trim();

    return v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")
        ? unescape(v.substring(1, v.length() - 1))
        : v;
  }

  private static boolean active(ArrayDeque<Branch> branches, ArrayDeque<SwitchState> switches) {

    return (branches.isEmpty() || branches.peek().active)
        && switches.stream().allMatch(SwitchState::active);
  }

  private static int safeInt(long value) {

    return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, value));
  }

  private static long parseLong(String value) {

    try {

      String v = value.trim();

      return v.matches("(?i)0x[0-9a-f]+")
          ? Long.parseUnsignedLong(v.substring(2), 16)
          : Long.parseLong(v);

    } catch (Exception ignored) {

      return 0;
    }
  }

  private static String stripOuter(String value) {

    String result = value;

    while (result.startsWith("(") && matching(result, 0, '(', ')') == result.length() - 1)
      result = result.substring(1, result.length() - 1).trim();

    return result;
  }

  private static int topLevel(String value, String operator) {

    int depth = 0;

    for (int i = 0; i <= value.length() - operator.length(); i++) {

      char c = value.charAt(i);

      if (c == '(') depth++;
      else if (c == ')') depth--;

      if (depth == 0 && value.startsWith(operator, i)) return i;
    }

    return -1;
  }

  private static int topLevelArithmetic(String value, String operator) {

    int depth = 0;

    for (int i = value.length() - 1; i >= 0; i--) {

      char c = value.charAt(i);

      if (c == ')') depth++;
      else if (c == '(') depth--;

      if (depth == 0 && value.startsWith(operator, i)) {

        if (operator.equals("-") && i + 1 < value.length() && value.charAt(i + 1) == '>') continue;

        return i;
      }
    }

    return -1;
  }

  private static List<String> strings(String source) {

    List<String> values = new ArrayList<>();

    Matcher matcher = C_STRING.matcher(source);

    while (matcher.find()) values.add(unescape(matcher.group(1)));

    return values;
  }

  private static String renderedText(
      String source, String npcName, Player player, Map<String, Long> locals) {

    String formatArgs = macroArgs(source, "FORMAT");

    if (formatArgs != null) {

      List<String> arguments = splitArgs(formatArgs);

      if (!arguments.isEmpty()) {

        List<String> formats = strings(arguments.get(0));

        String rendered = String.join("", formats);

        for (int i = 1; i < arguments.size(); i++) {

          String expression = arguments.get(i).trim();

          String value =
              expression.equals("USER_NAME")
                  ? playerName(player)
                  : Long.toString(number(expression, npcName, player, locals));

          rendered = rendered.replaceFirst("%(?:l|ll)?[udi]|%s", Matcher.quoteReplacement(value));
        }

        return rendered.replace("%s", playerName(player));
      }
    }

    StringBuilder out = new StringBuilder();

    Matcher parts = Pattern.compile("INTL\\s*\\(|C\\s*\\(").matcher(source);

    while (parts.find()) {

      int open = source.indexOf('(', parts.start());

      int close = matching(source, open, '(', ')');

      if (close < 0) break;

      String call = source.substring(parts.start(), open).trim();

      String args = source.substring(open + 1, close);

      if (call.equals("C")) {

        out.append(number(args, npcName, player, locals));

      } else {

        List<String> values = strings(args);

        if (!values.isEmpty()) out.append(String.join("", values));
      }
    }

    if (out.isEmpty()) {

      List<String> values = strings(source);

      if (!values.isEmpty()) out.append(String.join("", values));
    }

    return out.toString().replace("%s", playerName(player));
  }

  private static List<Long> matchParameters(String template, String normalizedInput) {

    String normalizedTemplate = normalize(template.replace("$", " zzparamzz "));

    StringBuilder regex = new StringBuilder("^");

    int captures = 0;

    for (String token : normalizedTemplate.split(" ")) {

      if (regex.length() > 1) regex.append("\\s+");

      if (token.equals("zzparamzz")) {

        regex.append("(-?\\d+)");

        captures++;

      } else regex.append(Pattern.quote(token));
    }

    regex.append("$");

    Matcher matcher = Pattern.compile(regex.toString()).matcher(normalizedInput);

    if (!matcher.matches()) return null;

    List<Long> result = new ArrayList<>(captures);

    for (int i = 1; i <= captures; i++) result.add(parseLong(matcher.group(i)));

    return result;
  }

  private static int matching(String text, int open, char left, char right) {

    if (open < 0) return -1;

    int depth = 0;

    boolean string = false, escape = false;

    for (int i = open; i < text.length(); i++) {

      char c = text.charAt(i);

      if (escape) {

        escape = false;

        continue;
      }

      if (c == '\\' && string) {

        escape = true;

        continue;
      }

      if (c == '"') {

        string = !string;

        continue;
      }

      if (string) continue;

      if (c == left) depth++;

      if (c == right && --depth == 0) return i;
    }

    return -1;
  }

  private static String normalize(String value) {

    return java.text.Normalizer.normalize(value == null ? "" : value, java.text.Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "")
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^\\p{L}\\p{N}]+", " ")
        .trim()
        .replaceAll("\\s+", " ");
  }

  private static String playerName(Player player) {

    try {

      return String.valueOf(player.getClass().getMethod("getName").invoke(player));

    } catch (Exception ignored) {

      return "adventurer";
    }
  }

  private static String unescape(String value) {

    return value
        .replace("\\\"", "\"")
        .replace("\\'", "'")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\\\", "\\");
  }

  private record Branch(boolean parentActive, boolean anyTaken, boolean active) {}

  private record SwitchState(long value, boolean matched, boolean active) {}

  private record Section(String kind, int start, int end) {}
}
