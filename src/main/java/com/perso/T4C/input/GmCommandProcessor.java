package com.perso.T4C.input;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.combat.ArmorClassRules;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.npc.behavior.RebirthBehavior;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.npc.core.NpcWorldFlags;
import com.perso.T4C.player.GmRank;
import com.perso.T4C.player.GmSeed;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.teleport.NamedLocation;
import com.perso.T4C.teleport.NamedLocations;
import com.perso.T4C.ui.SystemMessage;
import com.perso.T4C.world.DayNightCycle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class GmCommandProcessor {
  private final XpCurve xpCurve;
  private final NPCManager npcManager;
  private final MonsterManager monsterManager;
  private final Consumer<Player> saveHandler;
  private final CharacterDirectory characters;
  private DayNightCycle dayNightCycle;

  public GmCommandProcessor(
      XpCurve xpCurve,
      NPCManager npcManager,
      MonsterManager monsterManager,
      CharacterDirectory characters) {
    this(xpCurve, npcManager, monsterManager, PlayerStateStore::save, characters);
  }

  GmCommandProcessor(
      XpCurve xpCurve,
      NPCManager npcManager,
      MonsterManager monsterManager,
      Consumer<Player> saveHandler) {
    this(xpCurve, npcManager, monsterManager, saveHandler, null);
  }

  GmCommandProcessor(
      XpCurve xpCurve,
      NPCManager npcManager,
      MonsterManager monsterManager,
      Consumer<Player> saveHandler,
      CharacterDirectory characters) {
    this.xpCurve = xpCurve;
    this.npcManager = npcManager;
    this.monsterManager = monsterManager;
    this.saveHandler = saveHandler == null ? player -> {} : saveHandler;
    this.characters = characters;
  }

  /**
   * The rank a command needs. Commands that change the whole world (clock, world flags) or other
   * characters' ranks are Super GM only; with several players online they affect everyone.
   */
  static GmRank requiredRank(String cmd) {
    return switch (cmd) {
      case "help", "commands" -> GmRank.PLAYER;
      case "gm", "time", "day", "night", "gflag", "globalflag" -> GmRank.SUPER_GM;
      default -> GmRank.GM;
    };
  }

  public void setDayNightCycle(DayNightCycle dayNightCycle) {
    this.dayNightCycle = dayNightCycle;
  }

  public boolean handleChatMessage(String text, Player player) {
    if (text == null || !text.stripLeading().startsWith(".")) {
      return false;
    }
    String command = text.stripLeading().substring(1).trim();
    if (command.isEmpty()) {
      SystemMessage.showShared("GM: type .help to list commands.");
    } else {
      execute(command, player);
    }
    return true;
  }

  public void execute(String raw, Player player) {
    if (raw == null || raw.isBlank() || player == null) {
      return;
    }
    String[] parts = raw.trim().split("\\s+", 2);
    String cmd = parts[0].toLowerCase(Locale.ROOT);
    String arg = parts.length > 1 ? parts[1].trim() : "";
    GmRank rank = player.getGmRank();
    if (!rank.atLeast(requiredRank(cmd))) {
      // Players aren't told which GM commands exist; a GM is told what rank they're missing.
      SystemMessage.showShared(
          rank == GmRank.PLAYER
              ? "Unknown command ." + cmd + " (type .help)"
              : "GM: ." + cmd + " needs the Super GM rank");
      return;
    }
    try {
      switch (cmd) {
        case "gm":
          gmRank(player, arg);
          break;
        case "gmlist", "staff":
          gmList(player);
          break;
        case "level", "setlevel":
          setLevel(player, parseInt(arg));
          break;
        case "xp", "setxp":
          setXp(player, parseLong(arg));
          break;
        case "gold", "setgold":
          setGold(player, parseInt(arg));
          break;
        case "hp", "sethp":
          setHp(player, parseInt(arg));
          break;
        case "mana", "setmana":
          setMana(player, parseInt(arg));
          break;
        case "str", "strength", "setstrength":
          setStat(player, "str", parseInt(arg));
          break;
        case "dex", "dexterity", "setdexterity":
          setStat(player, "dex", parseInt(arg));
          break;
        case "end", "endurance", "setendurance":
          setStat(player, "end", parseInt(arg));
          break;
        case "int",
            "intelect",
            "intellect",
            "intelligence",
            "setintelect",
            "setintellect",
            "setintelligence":
          setStat(player, "int", parseInt(arg));
          break;
        case "wis", "wisdom", "setwisdom":
          setStat(player, "wis", parseInt(arg));
          break;
        case "statpts", "setstatpoints":
          player.setStatPoints(Math.max(0, parseInt(arg)));
          ok("Stat points set to " + player.getStatPoints(), player);
          break;
        case "skillpts", "setskillpoints":
          player.setSkillPoints(Math.max(0, parseInt(arg)));
          ok("Skill points set to " + player.getSkillPoints(), player);
          break;
        case "summon":
          summon(player, arg);
          break;
        case "teleport", "tp":
          teleport(player, arg);
          break;
        case "pos", "getpos", "where":
          position(player);
          break;
        case "flag":
          flag(player, arg);
          break;
        case "unflag":
          unflag(player, arg);
          break;
        case "flags":
          listFlags(player, arg);
          break;
        case "gflag", "globalflag":
          globalFlag(arg);
          break;
        case "learn":
          learn(player, arg);
          break;
        case "unlearn":
          unlearn(player, arg);
          break;
        case "stats":
          stats(player);
          break;
        case "sanctu", "sanctuary":
          sanctuary(player, arg);
          break;
        case "save":
          ok("Character saved", player);
          break;
        case "god":
          player.setGmInvulnerable(toggle(arg, player.isGmInvulnerable(), "god"));
          SystemMessage.showShared("GM: god mode " + (player.isGmInvulnerable() ? "on" : "off"));
          break;
        case "peace":
          player.setGmPeace(toggle(arg, player.isGmPeace(), "peace"));
          SystemMessage.showShared(
              "GM: peace mode " + (player.isGmPeace() ? "on - monsters ignore you" : "off"));
          break;
        case "time":
          time(arg);
          break;
        case "day":
          time("12");
          break;
        case "night":
          time("0");
          break;
        case "rebirth":
          rebirth(player, arg);
          break;
        case "setpower", "power":
          setElementPower(player, arg);
          break;
        case "collision", "noclip":
          collision(player, arg, "noclip".equals(cmd));
          break;
        case "speed":
          speed(player, arg);
          break;
        case "help", "commands":
          help(rank);
          break;
        default:
          SystemMessage.showShared("GM: unknown command ." + cmd + " (type .help)");
      }
    } catch (NumberFormatException e) {
      SystemMessage.showShared("GM: invalid value \"" + arg + "\" - must be a number.");
    } catch (UsageException e) {
      SystemMessage.showShared("GM: " + e.getMessage());
    }
  }

  private void setLevel(Player player, int level) {
    if (level < 1) level = 1;
    level = Math.min(level, GameConstants.MAX_PLAYER_LEVEL);
    player.setLevel(level);
    if (xpCurve != null) {
      long next = xpCurve.getXpToNextLevel(level);
      if (next > 0 || level >= GameConstants.MAX_PLAYER_LEVEL) player.setXpToNextLevel(next);
    }
    ok("Level set to " + player.getLevel(), player);
  }

  private void setXp(Player player, long xp) {
    player.setCurrentXp(Math.max(0, xp));
    ok("XP set to " + player.getCurrentXp(), player);
  }

  private void setGold(Player player, int gold) {
    player.setGold(Math.max(0, gold));
    ok("Gold set to " + player.getGold(), player);
  }

  private void setHp(Player player, int hp) {
    int v = Math.max(1, hp);
    player.setMaxHp(v);
    player.setCurrentHp(v);
    ok("HP set to " + v + " / " + v, player);
  }

  private void setMana(Player player, int mana) {
    int v = Math.max(0, mana);
    player.setMaxMana(v);
    player.setMana(v);
    ok("Mana set to " + v + " / " + v, player);
  }

  private void setStat(Player player, String stat, int value) {
    int v = Math.max(1, value);
    switch (stat) {
      case "str":
        player.setStrength(v);
        ok("Strength set to " + v, player);
        break;
      case "dex":
        player.setDexterity(v);
        ok("Dexterity set to " + v, player);
        break;
      case "end":
        player.setEndurance(v);
        ok("Endurance set to " + v, player);
        break;
      case "int":
        player.setIntelligence(v);
        ok("Intelligence set to " + v, player);
        break;
      case "wis":
        player.setWisdom(v);
        ok("Wisdom set to " + v, player);
        break;
    }
  }

  private void summon(Player player, String arg) {
    String[] parts = arg == null ? new String[0] : arg.trim().split("\\s+", 2);
    if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
      SystemMessage.showShared(
          "GM: usage .summon item <key>, .summon npc <name>, .summon monster <name> [count]");
      return;
    }
    String type = parts[0].toLowerCase();
    String name = parts[1].trim();
    switch (type) {
      case "item":
        summonItem(player, name);
        break;
      case "npc":
        summonNpc(player, name);
        break;
      case "monster":
        summonMonster(player, name);
        break;
      default:
        SystemMessage.showShared("GM: unknown summon type \"" + type + "\"");
        break;
    }
  }

  private static final int SUMMON_ITEM_MAX_COUNT = 50;

  private void summonItem(Player player, String arg) {
    String key = arg;
    int count = 1;
    int lastSpace = arg.lastIndexOf(' ');
    if (lastSpace > 0) {
      Integer parsed = tryParseInt(arg.substring(lastSpace + 1).trim());
      if (parsed != null) {
        count = Math.max(1, Math.min(parsed, SUMMON_ITEM_MAX_COUNT));
        key = arg.substring(0, lastSpace).trim();
      }
    }
    if (ItemRegistry.findByKey(key) == null) {
      SystemMessage.showShared("GM: unknown item \"" + key + "\"");
      return;
    }
    if (player.getInventory() == null) {
      SystemMessage.showShared("GM: inventory unavailable");
      return;
    }
    for (int i = 0; i < count; i++) {
      player.getInventory().add(key);
    }
    ok("Summoned " + (count > 1 ? count + " x " : "item ") + key, player);
  }

  private void summonNpc(Player player, String name) {
    if (npcManager == null) {
      SystemMessage.showShared("GM: NPC manager unavailable");
      return;
    }
    if (npcManager.spawnNPC(name, player.getPositionVector().x, player.getPositionVector().y)) {
      SystemMessage.showShared("GM: Summoned NPC " + name);
    } else {
      SystemMessage.showShared("GM: unknown NPC \"" + name + "\"");
    }
  }

  private static final int SUMMON_MONSTER_MAX_COUNT = 50;

  private void summonMonster(Player player, String arg) {
    if (monsterManager == null) {
      SystemMessage.showShared("GM: monster manager unavailable");
      return;
    }
    String name = arg;
    int count = 1;
    int lastSpace = arg.lastIndexOf(' ');
    if (lastSpace > 0) {
      String tail = arg.substring(lastSpace + 1).trim();
      try {
        int parsed = Integer.parseInt(tail);
        count = Math.max(1, Math.min(parsed, SUMMON_MONSTER_MAX_COUNT));
        name = arg.substring(0, lastSpace).trim();
      } catch (NumberFormatException ignored) {
        // trailing token isn't a count; treat the whole arg as the monster name
      }
    }
    if (name.isBlank()) {
      SystemMessage.showShared("GM: unknown monster \"" + arg + "\"");
      return;
    }
    Vector2 feetPosition = new Vector2(player.getPositionVector());
    int spawned = 0;
    for (int i = 0; i < count; i++) {
      float offsetX = feetPosition.x + ThreadLocalRandom.current().nextInt(-32, 33);
      float offsetY = feetPosition.y + ThreadLocalRandom.current().nextInt(-32, 33);
      if (monsterManager.spawnMonster(name, offsetX, offsetY, false)) {
        spawned++;
      }
    }
    if (spawned == 0) {
      SystemMessage.showShared("GM: unknown monster \"" + name + "\"");
    } else {
      SystemMessage.showShared("GM: Summoned " + spawned + " x " + name);
    }
  }

  private void teleport(Player player, String arg) {
    String value = arg == null ? "" : arg.trim();
    if (value.regionMatches(true, 0, "to", 0, 2)
        && (value.length() == 2 || Character.isWhitespace(value.charAt(2)))) {
      value = value.substring(2).trim();
    }
    if (value.regionMatches(true, 0, "npc ", 0, 4)) {
      teleportToNpc(player, value.substring(4).trim());
      return;
    }
    String[] coords = value.split("\\s*,\\s*");
    if (coords.length != 3) {
      if (!value.isEmpty() && teleportToPlace(player, value)) {
        return;
      }
      SystemMessage.showShared(
          value.isEmpty()
              ? "GM: usage .tp X,Y,Z | .tp <place> | .tp npc <id>"
              : "GM: unknown place \"" + value + "\" (for an NPC use .tp npc <id>)");
      return;
    }
    int tileX = parseInt(coords[0]);
    int tileY = parseInt(coords[1]);
    int z = parseInt(coords[2]);
    player.setWorldPosition(tileX * GRID_W, tileY * GRID_H, z);
    ok("Teleported to " + tileX + "," + tileY + "," + z, player);
  }

  private void gmRank(Player player, String arg) {
    if (arg.isEmpty()) {
      SystemMessage.showShared("GM: your rank is " + player.getGmRank().label());
      return;
    }
    int lastSpace = arg.lastIndexOf(' ');
    GmRank rank = lastSpace > 0 ? GmRank.parse(arg.substring(lastSpace + 1)) : null;
    if (rank == null) {
      throw new UsageException("usage .gm <character> player|gm|super");
    }
    String name = arg.substring(0, lastSpace).trim();
    if (name.equalsIgnoreCase(player.getName())) {
      throw new UsageException("you can't change your own rank");
    }
    if (GmSeed.isOwner(name)) {
      throw new UsageException(name + " is a server owner - their rank can't be changed in game");
    }
    String canonical = characters == null ? null : characters.findName(name);
    if (canonical == null || !characters.setStoredRank(canonical, rank)) {
      SystemMessage.showShared("GM: no character named \"" + name + "\"");
      return;
    }
    SystemMessage.showShared("GM: " + canonical + " is now " + rankPhrase(rank));
  }

  private static String rankPhrase(GmRank rank) {
    return switch (rank) {
      case PLAYER -> "a regular player";
      case GM -> "a GM";
      case SUPER_GM -> "a Super GM";
    };
  }

  private void gmList(Player player) {
    Map<String, GmRank> ranks = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (characters != null) {
      ranks.putAll(characters.storedRanks());
    }
    if (player.getName() != null) {
      ranks.put(player.getName(), player.getStoredGmRank());
    }
    List<String> staff = new ArrayList<>();
    for (Map.Entry<String, GmRank> entry : ranks.entrySet()) {
      GmRank effective = GmSeed.isOwner(entry.getKey()) ? GmRank.SUPER_GM : entry.getValue();
      if (effective != GmRank.PLAYER) {
        staff.add(
            entry.getKey()
                + " ("
                + effective.label()
                + (GmSeed.isOwner(entry.getKey()) ? ", owner" : "")
                + ")");
      }
    }
    SystemMessage.showShared(
        staff.isEmpty() ? "GM: no GMs" : "GM: staff - " + String.join(", ", staff));
  }

  private boolean teleportToPlace(Player player, String name) {
    String needle = name.toLowerCase(Locale.ROOT);
    NamedLocation match = null;
    for (NamedLocation location : NamedLocations.all()) {
      String candidate = location.displayName().toLowerCase(Locale.ROOT);
      if (candidate.equals(needle)) {
        match = location;
        break;
      }
      if (match == null && candidate.startsWith(needle)) {
        match = location;
      }
    }
    if (match == null) {
      return false;
    }
    player.setWorldPosition(match.tileX() * GRID_W, match.tileY() * GRID_H, match.worldZ());
    ok("Teleported to " + match.displayName(), player);
    return true;
  }

  private void teleportToNpc(Player player, String id) {
    if (id.isEmpty()) {
      throw new UsageException("usage .tp npc <id>");
    }
    for (SpawnDefinition spawn : SpawnRegistry.npcs()) {
      if (spawn.type() != null && spawn.type().equalsIgnoreCase(id)) {
        player.setWorldPosition(spawn.x() * GRID_W, spawn.y() * GRID_H, spawn.z());
        ok(
            "Teleported to " + spawn.type() + " at " + spawn.x() + "," + spawn.y() + "," + spawn.z(),
            player);
        return;
      }
    }
    SystemMessage.showShared("GM: no NPC spawn named \"" + id + "\"");
  }

  private void position(Player player) {
    var c = player.getCoordinates();
    SystemMessage.showShared(
        "GM: you are at "
            + (int) (c.getX() / GRID_W)
            + ","
            + (int) (c.getY() / GRID_H)
            + ","
            + c.getZ());
  }

  private void flag(Player player, String arg) {
    String[] parts = arg.split("\\s+");
    if (arg.isEmpty() || parts.length > 2) {
      throw new UsageException("usage .flag <key> [value]");
    }
    String key = parts[0];
    if (parts.length == 2) {
      player.setQuestFlag(key, parseInt(parts[1]));
      ok("flag " + key + " = " + player.getQuestFlag(key), player);
    } else {
      SystemMessage.showShared("GM: flag " + key + " = " + player.getQuestFlag(key));
    }
  }

  private void unflag(Player player, String arg) {
    if (arg.isEmpty() || arg.contains(" ")) {
      throw new UsageException("usage .unflag <key>");
    }
    player.clearQuestFlag(arg);
    ok("flag " + arg + " removed", player);
  }

  private static final int FLAG_LIST_LIMIT = 20;

  private void listFlags(Player player, String filter) {
    String needle = filter.toLowerCase(Locale.ROOT);
    List<String> matches = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : new TreeMap<>(player.getQuestFlags()).entrySet()) {
      if (entry.getKey().toLowerCase(Locale.ROOT).contains(needle)) {
        matches.add(entry.getKey() + " = " + entry.getValue());
      }
    }
    if (matches.isEmpty()) {
      SystemMessage.showShared(
          "GM: no flags" + (needle.isEmpty() ? "" : " matching \"" + filter + "\""));
      return;
    }
    int shown = Math.min(matches.size(), FLAG_LIST_LIMIT);
    for (String line : matches.subList(0, shown)) {
      SystemMessage.showShared("GM:   " + line);
    }
    if (matches.size() > shown) {
      SystemMessage.showShared(
          "GM: ... " + (matches.size() - shown) + " more - narrow it with .flags <text>");
    }
  }

  private void globalFlag(String arg) {
    String[] parts = arg.split("\\s+");
    if (arg.isEmpty() || parts.length > 2) {
      throw new UsageException("usage .gflag <key> [value]");
    }
    if (parts.length == 2) {
      NpcWorldFlags.set(parts[0], parseInt(parts[1]));
    }
    SystemMessage.showShared("GM: global flag " + parts[0] + " = " + NpcWorldFlags.get(parts[0]));
  }

  private void stats(Player player) {
    SystemMessage.showShared(
        String.format(
            Locale.ROOT,
            "GM: level %d (rebirth %d) | XP %d / %d | HP %d/%d | mana %d/%d | gold %d",
            player.getLevel(),
            player.getRebirthCount(),
            player.getCurrentXp(),
            player.getXpToNextLevel(),
            player.getCurrentHp(),
            player.getMaxHp(),
            player.getMana(),
            player.getMaxMana(),
            player.getGold()));
    SystemMessage.showShared(
        String.format(
            Locale.ROOT,
            "GM: STR %d DEX %d END %d INT %d WIS %d | AC %.0f | stat pts %d, skill pts %d",
            player.getStrength(),
            player.getDexterity(),
            player.getEndurance(),
            player.getIntelligence(),
            player.getWisdom(),
            ArmorClassRules.effectiveArmorClass(player),
            player.getStatPoints(),
            player.getSkillPoints()));
    StringBuilder powers = new StringBuilder("GM: power/resist");
    for (String element : ELEMENTS) {
      powers
          .append(" | ")
          .append(element)
          .append(' ')
          .append(player.getElementPower(element))
          .append('/')
          .append(player.getElementResistance(element));
    }
    SystemMessage.showShared(powers.toString());
  }

  private void sanctuary(Player player, String arg) {
    if ("clear".equalsIgnoreCase(arg) || "reset".equalsIgnoreCase(arg)) {
      player.clearRespawnPoint();
      ok("Respawn point reset to the default temple", player);
      return;
    }
    if (arg.isEmpty()) {
      var c = player.getCoordinates();
      player.setRespawnPoint(c.getX(), c.getY(), c.getZ());
      ok("Respawn point set here", player);
      return;
    }
    String[] coords = arg.split("\\s*,\\s*");
    if (coords.length != 3) {
      throw new UsageException("usage .sanctu [X,Y,Z | clear]");
    }
    int tileX = parseInt(coords[0]);
    int tileY = parseInt(coords[1]);
    int z = parseInt(coords[2]);
    player.setRespawnPoint(tileX * GRID_W, tileY * GRID_H, z);
    ok("Respawn point set to " + tileX + "," + tileY + "," + z, player);
  }

  private void time(String arg) {
    if (dayNightCycle == null) {
      SystemMessage.showShared("GM: day/night cycle unavailable");
      return;
    }
    if (!arg.isEmpty()) {
      float hour = parseFloat(arg);
      if (hour < 0 || hour >= 24) {
        throw new UsageException("hour must be from 0 to 23");
      }
      dayNightCycle.setHour(hour);
    }
    float hour = dayNightCycle.getHour();
    SystemMessage.showShared(
        String.format(
            Locale.ROOT,
            "GM: time is %02d:%02d (%s)",
            (int) hour,
            (int) ((hour % 1f) * 60f),
            dayNightCycle.isNight() ? "night" : "day"));
  }

  private static boolean toggle(String arg, boolean current, String name) {
    String mode = arg == null ? "" : arg.trim().toLowerCase(Locale.ROOT);
    return switch (mode) {
      case "", "toggle" -> !current;
      case "on", "enable", "enabled", "1" -> true;
      case "off", "disable", "disabled", "0" -> false;
      default -> throw new UsageException("usage ." + name + " on|off|toggle");
    };
  }

  private void unlearn(Player player, String arg) {
    String requested = stripTextId(arg).trim();
    if (requested.isEmpty()) {
      throw new UsageException("usage .unlearn <spell>");
    }
    SpellData spell = findSpell(requested);
    String name = spell == null ? requested : spell.getName();
    List<String> spells = player.getSpells();
    if (spells != null
        && spells.removeIf(
            known ->
                known != null
                    && (known.equalsIgnoreCase(name) || known.equalsIgnoreCase(requested)))) {
      ok("Forgot spell " + name, player);
    } else {
      SystemMessage.showShared("GM: doesn't know " + name);
    }
  }

  private void learn(Player player, String arg) {
    String requested = stripTextId(arg).trim();
    if (requested.isEmpty()) {
      SystemMessage.showShared("GM: usage .learn <spell>");
      return;
    }
    SpellData spell = findSpell(requested);
    if (spell == null) {
      SystemMessage.showShared("GM: unknown spell \"" + requested + "\"");
      return;
    }
    List<String> spells = player.getSpells();
    if (spells == null) {
      spells = new ArrayList<>();
      player.setSpells(spells);
    }
    for (String known : spells) {
      if (known != null && known.equalsIgnoreCase(spell.getName())) {
        SystemMessage.showShared("GM: already knows " + spell.getName());
        return;
      }
    }
    spells.add(spell.getName());
    ok("Learned spell " + spell.getName(), player);
  }

  private void rebirth(Player player, String arg) {
    if (!arg.isBlank()) {
      SystemMessage.showShared("GM: usage .rebirth");
      return;
    }
    if (!RebirthBehavior.perform(player)) {
      SystemMessage.showShared(
          "GM: already at the rebirth limit (" + GameConstants.REBIRTH_MAX_REMORTS + ")");
      return;
    }
    player.setWorldPosition(1315 * GRID_W, 920 * GRID_H, 1);
    ok("Rebirth performed (remort " + player.getRebirthCount() + ")", player);
  }

  private static final List<String> ELEMENTS = List.of("fire", "water", "air", "earth", "light", "dark");

  private void setElementPower(Player player, String arg) {
    String[] parts = arg.split("\\s+", 2);
    if (parts.length < 2) {
      SystemMessage.showShared("GM: usage .setpower <element> <value>");
      return;
    }
    String element = parts[0].toLowerCase(Locale.ROOT);
    if (!ELEMENTS.contains(element)) {
      SystemMessage.showShared("GM: unknown element \"" + element + "\" (fire/water/air/earth/light/dark)");
      return;
    }
    int value = parseInt(parts[1].trim());
    player.setQuestFlag("legacy:power:" + element, value);
    ok(element + " power set (now " + player.getElementPower(element) + " total)", player);
  }

  private void collision(Player player, String arg, boolean noclipSyntax) {
    String mode = arg == null ? "" : arg.trim().toLowerCase();
    if (mode.isEmpty() || "toggle".equals(mode)) {
      player.setPlayerCollisionsEnabled(!player.isPlayerCollisionsEnabled());
    } else if ("on".equals(mode)
        || "enable".equals(mode)
        || "enabled".equals(mode)
        || "1".equals(mode)) {
      player.setPlayerCollisionsEnabled(!noclipSyntax);
    } else if ("off".equals(mode)
        || "disable".equals(mode)
        || "disabled".equals(mode)
        || "0".equals(mode)) {
      player.setPlayerCollisionsEnabled(noclipSyntax);
    } else {
      SystemMessage.showShared(
          "GM: usage ." + (noclipSyntax ? "noclip" : "collision") + " on|off|toggle");
      return;
    }
    SystemMessage.showShared(
        "GM: noclip " + (player.isPlayerCollisionsEnabled() ? "disabled" : "enabled"));
  }

  private void speed(Player player, String arg) {
    String value = arg == null ? "" : arg.trim().toLowerCase();
    float current = player.getGmSpeedMultiplier();
    float next;
    if (value.isEmpty() || "+".equals(value) || "up".equals(value) || "increase".equals(value)) {
      next = current + 0.25f;
    } else if ("-".equals(value) || "down".equals(value) || "decrease".equals(value)) {
      next = current - 0.25f;
    } else if ("reset".equals(value) || "normal".equals(value)) {
      next = 1.0f;
    } else {
      next = parseFloat(value);
    }
    player.setGmSpeedMultiplier(next);
    SystemMessage.showShared(
        String.format(
            java.util.Locale.ROOT, "GM: speed multiplier %.2fx", player.getGmSpeedMultiplier()));
  }

  private SpellData findSpell(String requested) {
    SpellData exact = SpellRegistry.findByName(requested);
    if (exact != null) {
      return exact;
    }
    Integer spellId = tryParseInt(requested);
    for (SpellData spell : SpellRegistry.load()) {
      if (spell == null) {
        continue;
      }
      if (spellId != null && spell.getSpellId() == spellId) {
        return spell;
      }
      if (spell.getName() != null && spell.getName().equalsIgnoreCase(requested)) {
        return spell;
      }
    }
    return null;
  }

  private void ok(String msg, Player player) {
    saveHandler.accept(player);
    SystemMessage.showShared("GM: " + msg);
  }

  private void help(GmRank rank) {
    if (rank == GmRank.PLAYER) {
      SystemMessage.showShared("No chat commands are available yet.");
      return;
    }
    if (rank.atLeast(GmRank.SUPER_GM)) {
      SystemMessage.showShared(
          "GM (Super): .gm NAME player|gm|super | .time [H] | .day | .night | .gflag KEY [X]");
    }
    SystemMessage.showShared(
        "GM: .tp X,Y,Z | .tp PLACE | .tp npc ID | .pos | .sanctu [X,Y,Z|clear] | .save");
    SystemMessage.showShared(
        "GM: .noclip | .god | .peace [on|off] | .speed N|up|down|reset | .gmlist");
    SystemMessage.showShared("GM: .setLevel/.setXp/.setGold/.setHp/.setMana X | .stats");
    SystemMessage.showShared(
        "GM: .setStrength/.setDexterity/.setEndurance/.setIntelligence/.setWisdom X");
    SystemMessage.showShared(
        "GM: .setStatPoints/.setSkillPoints X | .summon item|npc|monster NAME [count] | .rebirth | .setpower ELEMENT X");
    SystemMessage.showShared(
        "GM: .learn/.unlearn SPELL | .flag KEY [X] | .unflag KEY | .flags [TEXT]");
  }

  /** Bad arguments: reported to the player, nothing changed or saved. */
  private static final class UsageException extends RuntimeException {
    UsageException(String message) {
      super(message);
    }
  }

  private static int parseInt(String s) {
    if (s == null || s.isEmpty()) throw new NumberFormatException("empty");
    return Integer.parseInt(s);
  }

  private static long parseLong(String s) {
    if (s == null || s.isEmpty()) throw new NumberFormatException("empty");
    return Long.parseLong(s);
  }

  private static Integer tryParseInt(String s) {
    try {
      return parseInt(s);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private static float parseFloat(String s) {
    if (s == null || s.isEmpty()) throw new NumberFormatException("empty");
    return Float.parseFloat(s);
  }

  private static String stripTextId(String s) {
    if (s == null) {
      return "";
    }
    return s.replaceAll("\\s*\\[[0-9]+]\\s*", " ").replaceAll("\\s+", " ");
  }
}
