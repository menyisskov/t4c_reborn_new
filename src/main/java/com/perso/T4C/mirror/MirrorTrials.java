package com.perso.T4C.mirror;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.companion.CompanionDef;
import com.perso.T4C.npc.companion.CompanionSpellTrigger;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * T4C-0042: The Mirror of Echoes. Every life a character sheds at rebirth leaves an echo behind;
 * the Mirrorwarden in the Colosseum lets a player face it. The Echo is built from the player's
 * own character at the moment it is summoned - their look, name, level, combat skills and best
 * attack spell - and it scales across ten trials. Beating all ten binds the Echo as a companion.
 *
 * <p>All numbers live here so the monster ({@code monster/EchoOfSelf}), the NPC ({@code
 * npc/MirrorwardenYsmera}) and the reward hook in {@code MainGameScreen} agree. Per-character
 * progress is stored in plain quest flags, so no save-format change is needed.
 */
public final class MirrorTrials {
  public static final int MAX_TIER = 10;

  /** Highest trial the character has won (0..10). */
  public static final String FLAG_TIER = "mirror.tier";

  /** Trial the next summoned Echo fights at; written by the Mirrorwarden right before summoning. */
  public static final String FLAG_CHALLENGE = "mirror.challenge";

  /** Total Echoes defeated (trials and rematches). */
  public static final String FLAG_VICTORIES = "mirror.victories";

  /** Times an Echo has struck the character down. */
  public static final String FLAG_FALLS = "mirror.falls";

  /** 1 once the one-time "someone wears your face" login whisper has been shown. */
  public static final String FLAG_WHISPER = "mirror.whisper";

  public static final String ECHO_MONSTER_NAME = "Echo of Self";

  public static final String ECHO_COMPANION_ID = "mirror_echo";

  /** Most gold a single trial may pay (DESIGN_GUIDELINES section 7: endgame quest gold ceiling). */
  public static final int MAX_GOLD_REWARD = 1_500_000;

  /** Each of the Echo's hits takes this share of the player's max HP at trial 1 (before x tier). */
  static final double HIT_SHARE_MIN = 0.06;

  static final double HIT_SHARE_MAX = 0.10;

  /** Fights last about this many of the player's strongest opening blows. */
  static final int BASE_BLOWS_TO_WIN = 14;

  static final int BLOWS_PER_TIER = 2;

  /** Hits the Echo watches before it stops adjusting its health to the player's strength. */
  public static final int CALIBRATION_HITS = 5;

  /** An Echo that loses sight of its maker this long, or strays this far, fades away. */
  public static final int LEASH_TILES = 22;

  public static final long MAX_FIGHT_MILLIS = 8L * 60L * 1000L;

  public enum Archetype {
    WARRIOR,
    ARCHER,
    INTELLIGENCE_MAGE,
    WISDOM_MAGE,
    HYBRID_MAGE
  }

  private MirrorTrials() {}

  public static int clearedTier(Player player) {
    return player == null
        ? 0
        : Math.max(0, Math.min(MAX_TIER, player.getQuestFlag(FLAG_TIER)));
  }

  /** The trial a new Echo fights at: one past the highest win, or a rematch at the last trial. */
  public static int nextTier(Player player) {
    return Math.min(MAX_TIER, clearedTier(player) + 1);
  }

  public static boolean hasBoundEcho(Player player) {
    return clearedTier(player) >= MAX_TIER;
  }

  /** How much stronger an Echo of this trial is than the first one (1.0 at trial 1, 2.08 at 10). */
  public static double tierMultiplier(int tier) {
    int t = Math.max(1, Math.min(MAX_TIER, tier));
    return 1.0 + 0.12 * (t - 1);
  }

  public static int tierStrengthPercent(int tier) {
    return (int) Math.round((tierMultiplier(tier) - 1.0) * 100.0);
  }

  /** The Echo's health is this many times the strongest of the player's opening blows. */
  public static int blowsToWin(int tier) {
    int t = Math.max(1, Math.min(MAX_TIER, tier));
    return BASE_BLOWS_TO_WIN + BLOWS_PER_TIER * t;
  }

  /** Damage (after armor) a hit should land for, as [min, max], measured against the maker. */
  public static int[] hitRange(int playerMaxHp, int tier) {
    double multiplier = tierMultiplier(tier);
    int hp = Math.max(1, playerMaxHp);
    int min = Math.max(1, (int) Math.round(hp * HIT_SHARE_MIN * multiplier));
    int max = Math.max(min, (int) Math.round(hp * HIT_SHARE_MAX * multiplier));
    return new int[] {min, max};
  }

  /** Gold paid the first time a trial is won. */
  public static int goldReward(int level, int tier) {
    long gold = (long) Math.max(1, level) * 250L * Math.max(1, tier);
    return (int) Math.min(MAX_GOLD_REWARD, gold);
  }

  /** Gold paid for beating the final Echo again after all ten trials are won. */
  public static int rematchGold(int level) {
    return Math.max(1, level) * 100;
  }

  /** XP paid the first time a trial is won: a share of the XP the current level still needs. */
  public static int xpReward(long remainingXp, int level, int tier) {
    if (level >= GameConstants.MAX_PLAYER_LEVEL || remainingXp <= 0) return 0;
    double share = 0.25 + 0.05 * (Math.max(1, Math.min(MAX_TIER, tier)) - 1);
    return (int) Math.min(Integer.MAX_VALUE, Math.round(remainingXp * share));
  }

  /**
   * Progression multiplies every XP gain by {@link GameConstants#SERVER_XP_RATE}; the trial reward
   * is already a share of the real remaining XP, so it is fed in pre-divided by that rate.
   */
  static int unscaledXp(int xp) {
    if (xp <= 0) return 0;
    return Math.max(1, Math.round(xp / GameConstants.SERVER_XP_RATE));
  }

  /** The XP a reward actually credits once progression re-applies the server rate. */
  static int creditableXp(int xp) {
    if (xp <= 0) return 0;
    double credited = unscaledXp(xp) * (double) GameConstants.SERVER_XP_RATE;
    return (int) Math.min(Integer.MAX_VALUE, Math.round(credited));
  }

  public static Archetype archetypeOf(Player player) {
    if (player == null) return Archetype.WARRIOR;
    if (com.perso.T4C.helper.PlayerAppearanceDefaults.hasBowEquipped(player)) {
      return Archetype.ARCHER;
    }
    return archetypeOf(
        player.getEffectiveStrength(),
        player.getEffectiveDexterity(),
        player.getEffectiveIntelligence(),
        player.getEffectiveWisdom());
  }

  static Archetype archetypeOf(int strength, int agility, int intelligence, int wisdom) {
    int caster = Math.max(intelligence, wisdom);
    int fighter = Math.max(strength, agility);
    if (caster > fighter) {
      int low = Math.min(intelligence, wisdom);
      if (low >= caster * 0.8) return Archetype.HYBRID_MAGE;
      return intelligence > wisdom ? Archetype.INTELLIGENCE_MAGE : Archetype.WISDOM_MAGE;
    }
    return agility > strength ? Archetype.ARCHER : Archetype.WARRIOR;
  }

  /**
   * The strongest single-target-capable attack spell the player knows (highest level, then
   * highest damage), or null. The Echo casts it back at them.
   */
  public static SpellData signatureSpell(Player player) {
    if (player == null || player.getSpells() == null) return null;
    SpellData best = null;
    for (String known : player.getSpells()) {
      SpellData spell = SpellRegistry.findByName(known);
      if (!isEchoCastable(spell)) continue;
      if (best == null
          || spell.getMinLevel() > best.getMinLevel()
          || (spell.getMinLevel() == best.getMinLevel()
              && spell.getMaxDamage() > best.getMaxDamage())) {
        best = spell;
      }
    }
    return best;
  }

  static boolean isEchoCastable(SpellData spell) {
    return spell != null
        && spell.isAttack()
        && spell.getSpellId() > 0
        && spell.getElement() >= 1
        && spell.getElement() <= 6;
  }

  /**
   * A spell resisted at the player's baseline (100, or 5000 for light) should land at full
   * strength, so light spells aren't 50x weaker than every other school against their own caster.
   */
  public static int spellRawDamage(int wantedDamage, int element) {
    int baseline = element == 5 ? 5000 : 100;
    return (int) Math.min(Integer.MAX_VALUE, (long) wantedDamage * baseline / 100L);
  }

  /** The player's current paperdoll, as alternating BodyPart / sprite-name pairs. */
  public static Object[] appearanceOf(Player player) {
    List<Object> parts = new ArrayList<>();
    if (player != null && player.getAnimations() != null) {
      for (Map.Entry<BodyPart, String> part : player.getAnimations().getPartMap().entrySet()) {
        if (part.getKey() != null && part.getValue() != null && !part.getValue().isBlank()) {
          parts.add(part.getKey());
          parts.add(part.getValue());
        }
      }
    }
    return parts.toArray();
  }

  /**
   * The bound Echo companion, dressed exactly as the player is right now. Stronger than the
   * squire/apprentice companions sold elsewhere, since it is the reward for all ten trials.
   */
  public static CompanionDef echoCompanion(Player player) {
    List<CompanionDef.Part> parts = new ArrayList<>();
    Object[] appearance = appearanceOf(player);
    for (int i = 0; i + 1 < appearance.length; i += 2) {
      parts.add(new CompanionDef.Part((BodyPart) appearance[i], (String) appearance[i + 1]));
    }
    List<CompanionDef.SpellEntry> spells = new ArrayList<>();
    spells.add(
        new CompanionDef.SpellEntry(
            "spell.heal_light", CompanionSpellTrigger.HEAL_OWNER, 30, 14.0f, 0.4f, 12, 20, 1.6f,
            0.0f));
    SpellData signature = signatureSpell(player);
    if (signature != null) {
      spells.add(
          new CompanionDef.SpellEntry(
              signature.getKey(), CompanionSpellTrigger.ATTACK, 10, 4.0f, 0.0f, 10, 20, 2.5f,
              7.0f));
    }
    String name =
        I18n.message("mirror.echo.name", player == null ? "" : String.valueOf(player.getName()));
    return new CompanionDef(
        ECHO_COMPANION_ID, name, parts, null, 120, 18.0f, 8, 16, 2.0f, 1.2f, spells);
  }

  /** Placeholders every Echo line may use: 1 name, 2 level, 3 rebirths, 4 gold, 5 trial. */
  public static String line(String key, Player player, int tier) {
    String name = player == null ? "" : String.valueOf(player.getName());
    int level = player == null ? 1 : player.getLevel();
    int rebirths = player == null ? 0 : player.getRebirthCount();
    String gold = String.format(Locale.ROOT, "%,d", player == null ? 0L : (long) player.getGold());
    return String.format(Locale.ROOT, I18n.key(key), name, level, rebirths, gold, tier);
  }

  public static String introKey(int tier) {
    return "mirror.echo.intro." + Math.max(1, Math.min(MAX_TIER, tier));
  }

  public static String tauntKey(Archetype archetype) {
    return "mirror.echo.taunt." + archetype.name().toLowerCase(Locale.ROOT);
  }

  /** What winning an Echo fight paid, for the chat summary. */
  public record Reward(int tier, boolean firstClear, int xp, int gold, boolean boundEcho) {}

  /** Books a defeated Echo: advances the trial ladder and pays the player. */
  public static Reward recordVictory(
      Player player, int tier, com.perso.T4C.helper.XpCurve xpCurve) {
    int fought = Math.max(1, Math.min(MAX_TIER, tier));
    player.setQuestFlag(FLAG_VICTORIES, player.getQuestFlag(FLAG_VICTORIES) + 1);
    boolean firstClear = fought > clearedTier(player);
    if (!firstClear) {
      int gold = rematchGold(player.getLevel());
      player.addGold(gold);
      return new Reward(fought, false, 0, gold, false);
    }
    player.setQuestFlag(FLAG_TIER, fought);
    long remaining = Math.max(0L, player.getXpToNextLevel() - player.getCurrentXp());
    int xp = creditableXp(xpReward(remaining, player.getLevel(), fought));
    int gold = goldReward(player.getLevel(), fought);
    if (xp > 0) player.addXpExact(unscaledXp(xp), xpCurve);
    player.addGold(gold);
    return new Reward(fought, true, xp, gold, fought >= MAX_TIER);
  }

  public static void recordFall(Player player) {
    if (player != null) player.setQuestFlag(FLAG_FALLS, player.getQuestFlag(FLAG_FALLS) + 1);
  }

  /** The one-time login hint that something is waiting in the Colosseum, or null. */
  public static String takeLoginWhisper(Player player) {
    if (player == null || player.getQuestFlag(FLAG_WHISPER) != 0) return null;
    player.setQuestFlag(FLAG_WHISPER, 1);
    return line("mirror.whisper", player, 1);
  }
}
