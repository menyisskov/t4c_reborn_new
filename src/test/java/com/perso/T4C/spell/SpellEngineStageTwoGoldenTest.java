package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.combat.CombatProfiles;
import com.perso.T4C.helper.RandomSource;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.engine.SpellEngine;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Single-player half of the stage-2 engine golden. Every stage-2 spell (timed buffs, the curse,
 * invisibility and detection, dispels, Renew Armor) is cast by four fixture characters through the
 * <em>single-player</em> code path with a seeded {@link RandomSource}, mirroring {@code
 * MainGameScreen.castDefensiveSpell} step for step (the cast rules, the heal roll, the buff amounts
 * and duration, Renew Armor's re-lay of the learned protections, the utility effects). The outcome
 * and the resulting combat/spell numbers (as differences from the character before the cast) are
 * compared with the committed golden text; the server module's {@code SpellEngineStageTwoGoldenTest}
 * reproduces it from {@code SpellEngine} and committed online progress. Regenerate after an
 * intentional content change with {@code -Dspell.golden.update=true}.
 *
 * <p>The fourth caster, "buffed", already runs Bless, Stone Skin and Mana Surge, so effective and
 * true readings differ and every formula that reads a true value is exercised against that.
 */
@ResourceLock("ItemRegistry")
class SpellEngineStageTwoGoldenTest {
  static final Path GOLDEN = Path.of("src/test/resources/spell/engine-stage2-golden.txt");
  static final Path CONTENT = Path.of("multiplayer/server/src/main/resources/content/spells-v2.json");
  static final String[] CASTERS = {"plain", "skilled", "geared", "buffed"};
  static final List<String> PRE_BUFFS = List.of("spell.bless", "spell.stone_skin", "spell.mana_surge");
  static final long PRE_BUFF_SEED = 99L;

  @BeforeAll
  static void loadItems() {
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
  }

  static SpellContentV2 content() throws Exception {
    return new Gson().fromJson(Files.readString(CONTENT), SpellContentV2.class);
  }

  static List<String> stageTwoKeys() throws Exception {
    var keys = new ArrayList<String>();
    content().spells().values().stream().filter(spell -> spell.stage() == 2).forEach(spell -> keys.add(spell.key()));
    java.util.Collections.sort(keys);
    return keys;
  }

  /** The learned Renew Armor components per fixture caster (the server fixture mirrors this). */
  static List<String> learnedComponents(String caster) {
    return switch (caster) {
      case "plain" -> List.of("spell.barrier", "spell.protection", "spell.mana_shield");
      case "skilled" -> List.of("spell.barrier", "spell.ultra_barrier", "spell.stone_skin",
          "spell.ultra_stone_skin", "spell.mana_surge", "spell.ultra_mana_surge");
      default -> SpellEngine.RENEW_ARMOR_COMPONENTS;
    };
  }

  static void lay(Player player, SpellData spell, SpellEffectManager manager) {
    var effects = manager.resolvePlayerBuffEffects(spell, player);
    player.applyBuff(spell.getName(), spell.getDescription(), spell.getIconId(),
        manager.resolveDurationSeconds(spell, player), false, effects);
  }

  static Player player(String caster, SpellData cast) {
    Player player = SpellContextParityTest.player(caster.equals("buffed") ? "geared" : caster);
    player.setMana(10_000);
    // A real character has maximum health; a bare Player has none, which would hide the refresh arithmetic.
    player.setMaxHp(500);
    var learned = new ArrayList<String>(List.of(cast.getKey()));
    learned.addAll(learnedComponents(caster));
    player.setSpells(learned);
    if (caster.equals("buffed")) {
      var manager = new SpellEffectManager();
      try (RandomSource.Scope ignored = RandomSource.use(new Random(PRE_BUFF_SEED))) {
        for (String key : PRE_BUFFS) lay(player, SpellRegistry.findByName(key), manager);
      }
    }
    return player;
  }

  /** The numbers a buff can move, to be reported as differences from before the cast. */
  static long[] measure(Player player) {
    var profile = CombatProfiles.fromPlayer(player);
    var values = new ArrayList<Long>(List.of((long) profile.level(), (long) profile.strength(),
        (long) profile.endurance(), (long) profile.agility(), (long) profile.attack(), (long) profile.archery(),
        (long) profile.dodge(), Math.round(profile.armorClass() * 100), (long) player.getMaxHp(),
        (long) player.getEffectiveIntelligence(), (long) player.getEffectiveWisdom()));
    for (String element : ElementRules.ELEMENTS) values.add((long) player.getElementPower(element));
    for (String element : ElementRules.ELEMENTS) values.add((long) player.getElementResistance(element));
    return values.stream().mapToLong(Long::longValue).toArray();
  }

  static String delta(long[] before, long[] after) {
    var out = new StringBuilder();
    for (int i = 0; i < before.length; i++) out.append(i == 0 ? "" : ",").append(after[i] - before[i]);
    return out.toString();
  }

  static String buffNames(Player player) {
    return player.getActiveBuffs().stream()
        .map(buff -> {
          var spell = SpellRegistry.findByName(buff.getSpellName());
          return spell == null ? buff.getSpellName() : spell.getKey();
        })
        .sorted()
        .collect(Collectors.joining(","));
  }

  /** One pass through {@code castDefensiveSpell}; false when the rules refused the cast. */
  static boolean castSelf(Player player, SpellData spell, SpellEffectManager manager, StringBuilder out) {
    var cast = SpellCastingService.begin(new SpellCastingService.Request(
        spell, player, SpellCastingService.TargetKind.SELF, 0f, true, false, true));
    out.append(" verdict=").append(cast.failure().name()).append(" mana=").append(cast.manaSpent());
    if (!cast.success()) return false;
    int heal = manager.resolvePlayerHealthDelta(spell, player);
    if (heal != 0) out.append(" heal=").append(heal);
    var effects = manager.resolvePlayerBuffEffects(spell, player);
    if (!effects.isEmpty()) {
      int seconds = manager.resolveDurationSeconds(spell, player);
      player.applyBuff(spell.getName(), spell.getDescription(), spell.getIconId(), seconds, false, effects);
      out.append(" dur=").append(seconds).append(" mods=").append(effects.stream()
          .map(effect -> effect.getAttribute() + ":" + effect.getAmount()).collect(Collectors.joining(",")));
    }
    if ("${spell.renew_armor}".equals(spell.getName())) {
      // MainGameScreen.applyRenewArmorRecast, in its fixed order.
      for (String key : SpellEngine.RENEW_ARMOR_COMPONENTS) {
        SpellData component = SpellRegistry.findByName(key);
        if (component == null || !SpellCastingService.hasLearnedSpell(player, component)) continue;
        var componentEffects = manager.resolvePlayerBuffEffects(component, player);
        if (componentEffects.isEmpty()) continue;
        int seconds = manager.resolveDurationSeconds(component, player);
        player.applyBuff(component.getName(), component.getDescription(), component.getIconId(), seconds, false,
            componentEffects);
        out.append(" ").append(key.substring(6)).append("=").append(seconds).append(":")
            .append(componentEffects.stream().map(effect -> effect.getAttribute() + ":" + effect.getAmount())
                .collect(Collectors.joining(",")));
      }
    }
    var utility = manager.applyPlayerUtilityEffects(spell, player);
    if (utility.invisibilityApplied()) out.append(" hide=").append(Math.round(player.getHiddenRemainingMillis() / 1000.0));
    if (utility.detectInvisibleApplied()) out.append(" detinv");
    if (utility.detectHiddenApplied()) out.append(" dethid");
    if (utility.dispelledEffects() > 0) out.append(" dispelled=").append(utility.dispelledEffects());
    return true;
  }

  static String selfScenario(SpellContentV2.Spell content, int keyIndex, String casterName, int casterIndex) {
    SpellData spell = SpellRegistry.findByName(content.key());
    assertNotNull(spell, content.key());
    Player player = player(casterName, spell);
    var manager = new SpellEffectManager();
    if (content.kind() == SpellContentV2.Kind.DISPEL) {
      // Every spell the dispel can name is running, so each roll that succeeds removes something.
      for (var effect : spell.getT4cEffects()) {
        int id = Integer.parseInt(effect.getParameters().get(0).getExpression().trim());
        SpellData target = SpellRegistry.findById(id);
        if (target != null && !player.hasBuff(target.getName()))
          player.applyBuff(target.getName(), "", "", 600, false, List.of());
      }
    }
    long[] before = measure(player);
    var out = new StringBuilder(content.key() + " " + casterName);
    try (RandomSource.Scope ignored = RandomSource.use(new Random(1_000L + keyIndex * 7L + casterIndex))) {
      boolean cast = castSelf(player, spell, manager, out);
      if (cast && content.kind() == SpellContentV2.Kind.BUFF) {
        out.append(" |");
        castSelf(player, spell, manager, out);
      }
    }
    out.append(" delta=").append(delta(before, measure(player))).append(" buffs=").append(buffNames(player));
    return out.toString();
  }

  /** Hostile cast of the curse: the rules, then the impact roll, which is nothing on a monster. */
  static String hostileScenario(SpellContentV2.Spell content, int keyIndex, String casterName, int casterIndex)
      throws Exception {
    SpellData spell = SpellRegistry.findByName(content.key());
    assertNotNull(spell, content.key());
    Player player = player(casterName, spell);
    var out = new StringBuilder(content.key() + " " + casterName);
    try (RandomSource.Scope ignored = RandomSource.use(new Random(1_000L + keyIndex * 7L + casterIndex))) {
      var cast = SpellCastingService.begin(new SpellCastingService.Request(
          spell, player, SpellCastingService.TargetKind.HOSTILE_UNIT, 3f, true, false, true));
      out.append(" verdict=").append(cast.failure().name()).append(" mana=").append(cast.manaSpent());
      if (cast.success()) {
        var monster = SpellEngineStageOneGoldenTest.monster(new int[] {100, 250, 7, 100, 100, 3});
        var impact = new SpellEffectManager().resolve(spell, player, monster, 0d);
        if (impact.healthDelta() != 0) out.append(" delta=").append(impact.healthDelta());
      }
    }
    return out.toString();
  }

  static String produce() throws Exception {
    var text = new StringBuilder("# Stage-2 spell outcomes from the single-player code path; the engine must equal them.\n");
    var keys = stageTwoKeys();
    var content = content();
    for (int k = 0; k < keys.size(); k++) {
      var spell = content.spells().get(keys.get(k));
      for (int c = 0; c < CASTERS.length; c++)
        text.append(spell.kind() == SpellContentV2.Kind.DEBUFF
                ? hostileScenario(spell, k, CASTERS[c], c)
                : selfScenario(spell, k, CASTERS[c], c))
            .append('\n');
    }
    return text.toString();
  }

  @Test
  void thirtyThreeSpellsMatchTheCommittedGolden() throws Exception {
    assertEquals(33, stageTwoKeys().size());
    String text = produce();
    if (Boolean.getBoolean("spell.golden.update")) {
      Files.createDirectories(GOLDEN.getParent());
      Files.writeString(GOLDEN, text);
    }
    assertEquals(Files.readString(GOLDEN).replace("\r\n", "\n"), text,
        "Regenerate with -Dspell.golden.update=true and review the diff");
  }

  @Test
  void theFixtureExercisesRefreshesTrueReadingsDispelsFlagsAndRenewArmor() throws Exception {
    String text = produce();
    assertTrue(text.lines().anyMatch(line -> line.startsWith("spell.stone_skin buffed") && line.contains(" | verdict=NONE")),
        "a recast refreshes the running buff");
    assertTrue(text.lines().anyMatch(line -> line.startsWith("spell.invisibility ") && line.contains(" hide=")));
    assertTrue(text.contains(" detinv") && text.contains(" dethid"));
    assertTrue(text.lines().anyMatch(line -> line.startsWith("spell.dispel ") && line.contains(" dispelled=")));
    assertTrue(text.lines().anyMatch(line -> line.startsWith("spell.renew_armor geared") && line.contains(" ultra_mana_surge=")));
    assertFalse(text.lines().anyMatch(line -> line.startsWith("spell.renew_armor plain") && line.contains(" ultra_")),
        "components the caster has not learned are not re-laid");
    assertEquals(33 * CASTERS.length, text.lines().filter(line -> line.startsWith("spell.")).count());
  }

  /** The Renew Armor list is single-player's own; the engine's copy must not drift from it. */
  @Test
  void renewArmorComponentsEqualTheListInTheGameScreen() throws Exception {
    String source = Files.readString(Path.of("src/main/java/com/perso/T4C/screens/MainGameScreen.java"));
    int start = source.indexOf("RENEW_ARMOR_COMPONENT_KEYS =");
    String block = source.substring(start, source.indexOf(");", start));
    var keys = new ArrayList<String>();
    var matcher = java.util.regex.Pattern.compile("\\$\\{(spell\\.[a-z_]+)}").matcher(block);
    while (matcher.find()) keys.add(matcher.group(1));
    assertEquals(SpellEngine.RENEW_ARMOR_COMPONENTS, keys);
  }
}
