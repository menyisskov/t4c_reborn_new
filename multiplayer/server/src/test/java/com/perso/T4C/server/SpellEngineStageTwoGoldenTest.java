package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.RandomSource;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.spell.ElementRules;
import com.perso.T4C.spell.SpellFormulaContexts;
import com.perso.T4C.spell.engine.SpellEngine;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Online half of the stage-2 engine golden: the same fixture as the root module's {@code
 * SpellEngineStageTwoGoldenTest}, cast through {@link OnlineSpellStrikes#plan} from committed online
 * progress, must reproduce the text single-player's own code path produced. The text includes the
 * combat and spell numbers the buffs moved, measured from the committed result with the server's
 * own loadout and context builders.
 */
class SpellEngineStageTwoGoldenTest {
  private static final Path GOLDEN = Path.of("../../src/test/resources/spell/engine-stage2-golden.txt");
  static final long NOW = 1_000_000_000_000L;
  static final String[] CASTERS = {"plain", "skilled", "geared", "buffed"};
  static final List<String> PRE_BUFFS = List.of("spell.bless", "spell.stone_skin", "spell.mana_surge");

  static SpellCatalogV2 catalog() throws Exception {
    return new SpellCatalogV2(SpellEngineGoldenTest.content(), 2);
  }

  static List<String> learnedComponents(String caster) {
    return switch (caster) {
      case "plain" -> List.of("spell.barrier", "spell.protection", "spell.mana_shield");
      case "skilled" -> List.of("spell.barrier", "spell.ultra_barrier", "spell.stone_skin",
          "spell.ultra_stone_skin", "spell.mana_surge", "spell.ultra_mana_surge");
      default -> SpellEngine.RENEW_ARMOR_COMPONENTS;
    };
  }

  /** The fixture character with the spell and its components learned, plenty of mana and the pre-buffs. */
  static CharacterProgress fixture(String caster, String spellKey, ItemContent items) throws Exception {
    var base = OnlineSpellContextParityTest.character(caster.equals("buffed") ? "geared" : caster, items);
    var spells = new java.util.HashSet<String>(learnedComponents(caster));
    spells.add(spellKey);
    var vitals = base.vitals();
    var progress = ProgressEdits.withSpells(ProgressEdits.withVitals(base,
        new CharacterProgress.Vitals(vitals.hp(), vitals.maxHp(), 10_000, 10_000)), spells);
    if (caster.equals("buffed")) {
      var random = new Random(99);
      for (String key : PRE_BUFFS) {
        var context = SpellFormulaContexts.forSelf(OnlineSpellContexts.caster(progress, items, NOW));
        SpellEngine.BuffResolution resolved;
        try (var ignored = RandomSource.use(random)) {
          resolved = SpellEngine.resolveBuff(SpellEngineGoldenTest.content().spells().get(key), context);
        }
        progress = OnlineBuffs.apply(progress, new SpellEngine.Event.ApplyBuff(null, key, resolved.durationSeconds(),
            resolved.modifiers()), NOW);
      }
    }
    return progress;
  }

  /** The numbers a buff can move, from committed progress with the server's own builders. */
  static long[] measure(CharacterProgress progress, ItemContent items) {
    var profile = OnlineCombatLoadout.resolve(progress, items, NOW, false);
    var combat = profile.profile();
    var caster = OnlineSpellContexts.caster(progress, items, NOW);
    var resistance = OnlineSpellContexts.effectiveResistance(progress, items, NOW);
    var values = new ArrayList<Long>(List.of((long) combat.level(), (long) combat.strength(),
        (long) combat.endurance(), (long) combat.agility(), (long) combat.attack(), (long) combat.archery(),
        (long) combat.dodge(), Math.round(combat.armorClass() * 100), (long) progress.vitals().maxHp(),
        (long) profile.stats().intelligence(), (long) profile.stats().wisdom()));
    for (String element : ElementRules.ELEMENTS) values.add((long) caster.power().of(element));
    for (String element : ElementRules.ELEMENTS) values.add((long) resistance.of(element));
    return values.stream().mapToLong(Long::longValue).toArray();
  }

  static String delta(long[] before, long[] after) {
    var out = new StringBuilder();
    for (int i = 0; i < before.length; i++) out.append(i == 0 ? "" : ",").append(after[i] - before[i]);
    return out.toString();
  }

  static String mods(List<SpellEngine.Modifier> modifiers) {
    return modifiers.stream().map(modifier -> modifier.attribute() + ":" + modifier.amount()).collect(Collectors.joining(","));
  }

  static OnlineStore.CharacterState state(CharacterProgress progress) {
    return new OnlineStore.CharacterState(UUID.randomUUID(), UUID.randomUUID(), "Caster", 0, 3, 3, 0, 0, 0, progress);
  }

  /** One cast through the real server draft; appends the same fields the single-player half prints. */
  static CharacterProgress castSelf(CharacterProgress progress, SpellContentV2.Spell spell, long seed, ItemContent items,
      StringBuilder out) throws Exception {
    var map = new PreviewMap(new boolean[30][60], 0, 0, 3, 3);
    OnlineSpellStrikes.Plan plan;
    try {
      plan = OnlineSpellStrikes.plan(state(progress), spell.key(), new SpellEngine.Target.Self(), NOW, new Random(seed),
          items, catalog(), map, 5.5, 5.5, 0, null);
    } catch (PreviewWorld.Rejected refused) {
      String verdict = refused.getMessage().equals("spell_failed") ? "ACTIVATION_FAILED" : refused.getMessage().toUpperCase();
      out.append(" verdict=").append(verdict).append(" mana=0");
      return null;
    }
    out.append(" verdict=NONE mana=").append(plan.manaSpent());
    boolean detInv = false, detHid = false;
    Integer hide = null;
    int dispelled = 0;
    var running = new HashSet<String>();
    for (var buff : OnlineBuffs.active(progress, NOW)) running.add(buff.spell());
    for (var event : plan.events()) {
      switch (event) {
        case SpellEngine.Event.SelfHeal heal -> out.append(" heal=").append(heal.amount());
        case SpellEngine.Event.FriendHeal heal -> out.append(" friendHeal=").append(heal.recipient()).append(':').append(heal.amount());
        case SpellEngine.Event.ApplyBuff buff -> {
          if (spell.kind() == SpellContentV2.Kind.SPECIAL)
            out.append(" ").append(buff.spellKey().substring(6)).append("=").append(buff.durationSeconds()).append(":")
                .append(mods(buff.modifiers()));
          else out.append(" dur=").append(buff.durationSeconds()).append(" mods=").append(mods(buff.modifiers()));
        }
        case SpellEngine.Event.Hide h -> hide = (int) Math.round(h.durationMillis() / 1000.0);
        case SpellEngine.Event.DetectInvisible d -> detInv = true;
        case SpellEngine.Event.DetectHidden d -> detHid = true;
        case SpellEngine.Event.Dispel d -> { if (running.remove(d.spellKey())) dispelled++; }
        default -> fail("Unexpected event " + event);
      }
    }
    if (hide != null) out.append(" hide=").append(hide);
    if (detInv) out.append(" detinv");
    if (detHid) out.append(" dethid");
    if (dispelled > 0) out.append(" dispelled=").append(dispelled);
    return plan.progress();
  }

  static String selfScenario(SpellContentV2.Spell spell, int keyIndex, String caster, int casterIndex) throws Exception {
    var items = ItemCatalog.loadBundled();
    var progress = fixture(caster, spell.key(), items);
    if (spell.kind() == SpellContentV2.Kind.DISPEL) {
      var buffs = new ArrayList<>(progress.buffs());
      for (String target : new java.util.TreeSet<>(spell.dispelTargets().values()))
        buffs.add(new CharacterProgress.Buff(target, NOW + 600_000, 0, 0, Map.of("armorClass", 0), 1));
      progress = new CharacterProgress(progress.version(), progress.characterClass(), progress.gender(),
          progress.attributes(), progress.vitals(), progress.advancement(), progress.inventory(), progress.equipment(),
          progress.skills(), progress.spells(), progress.questFlags(), progress.storage(), progress.storageGold(),
          progress.respawn(), buffs, progress.cooldowns(), progress.hiddenUntil(), progress.companion());
    }
    long[] before = measure(progress, items);
    var out = new StringBuilder(spell.key() + " " + caster);
    long seed = 1_000L + keyIndex * 7L + casterIndex;
    var after = castSelf(progress, spell, seed, items, out);
    if (after != null && spell.kind() == SpellContentV2.Kind.BUFF) {
      // The single-player half keeps drawing from the same seeded generator for the second cast.
      var second = new StringBuilder();
      var again = castSelfContinuing(progress, spell, seed, items, second);
      out.append(" |").append(second);
      if (again != null) after = again;
    }
    if (after == null) return out.toString();
    var names = OnlineBuffs.active(after, NOW).stream().map(CharacterProgress.Buff::spell).sorted()
        .collect(Collectors.joining(","));
    return out + " delta=" + delta(before, measure(after, items)) + " buffs=" + names;
  }

  /**
   * The second cast of the same spell: single-player's generator keeps its state across both casts, so the
   * replay draws the first cast's numbers again before the second cast's.
   */
  private static CharacterProgress castSelfContinuing(CharacterProgress start, SpellContentV2.Spell spell, long seed,
      ItemContent items, StringBuilder out) throws Exception {
    var random = new Random(seed);
    var map = new PreviewMap(new boolean[30][60], 0, 0, 3, 3);
    var first = OnlineSpellStrikes.plan(state(start), spell.key(), new SpellEngine.Target.Self(), NOW, random, items,
        catalog(), map, 5.5, 5.5, 0, null);
    try {
      var second = OnlineSpellStrikes.plan(state(first.progress()), spell.key(), new SpellEngine.Target.Self(), NOW,
          random, items, catalog(), map, 5.5, 5.5, 0, null);
      out.append(" verdict=NONE mana=").append(second.manaSpent());
      for (var event : second.events())
        if (event instanceof SpellEngine.Event.ApplyBuff buff)
          out.append(" dur=").append(buff.durationSeconds()).append(" mods=").append(mods(buff.modifiers()));
      return second.progress();
    } catch (PreviewWorld.Rejected refused) {
      out.append(" verdict=").append(refused.getMessage().toUpperCase()).append(" mana=0");
      return first.progress();
    }
  }

  static String hostileScenario(SpellContentV2.Spell spell, int keyIndex, String caster, int casterIndex) throws Exception {
    var items = ItemCatalog.loadBundled();
    var progress = fixture(caster, spell.key(), items);
    var request = new SpellEngine.CastRequest(spell, OnlineSpellContexts.caster(progress, items, NOW), 7, 10,
        new SpellEngine.Target.Unit(1), true, 10_000, false, false, false);
    var resolved = SpellEngine.cast(request, SpellEngineGoldenTest.Fixture.standard(), new Random(1_000L + keyIndex * 7L + casterIndex));
    var out = new StringBuilder(spell.key() + " " + caster + " verdict=" + resolved.verdict().name());
    out.append(" mana=").append(resolved.costs() == null ? 0 : resolved.costs().manaCost());
    for (var event : resolved.events()) out.append(" unexpected=").append(event);
    return out.toString();
  }

  @Test
  void engineReproducesTheSinglePlayerGoldenForAllThirtyThreeSpells() throws Exception {
    var content = SpellEngineGoldenTest.content();
    var keys = new ArrayList<String>();
    content.spells().values().stream().filter(spell -> spell.stage() == 2 && !spell.key().startsWith("spell.item_")).forEach(spell -> keys.add(spell.key()));
    java.util.Collections.sort(keys);
    assertEquals(33, keys.size());
    var text = new StringBuilder("# Stage-2 spell outcomes from the single-player code path; the engine must equal them.\n");
    for (int k = 0; k < keys.size(); k++) {
      var spell = content.spells().get(keys.get(k));
      for (int c = 0; c < CASTERS.length; c++)
        text.append(spell.kind() == SpellContentV2.Kind.DEBUFF
                ? hostileScenario(spell, k, CASTERS[c], c)
                : selfScenario(spell, k, CASTERS[c], c))
            .append('\n');
    }
    Files.writeString(Path.of("target/stage2-actual.txt"), text.toString());
    assertEquals(Files.readString(GOLDEN).replace("\r\n", "\n"), text.toString());
  }
}
