package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spell.SpellFormulaContexts;
import com.perso.T4C.spell.SpellFormulaContexts.Elements;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Online half of the formula-context parity proof. The same fixture characters as the root
 * module's {@code SpellContextParityTest} (same numbers, same gear picked by the same rule from the
 * same exported item data) must produce exactly the committed golden text single-player produced
 * from a live {@code Player}.
 */
class OnlineSpellContextParityTest {
  private static final Path GOLDEN = Path.of("../../src/test/resources/spell/formula-context-golden.txt");
  private static final int[] TARGET_RESISTANCE = {100, 250, 7, 100, 100, 3};
  private static final Map<String, int[]> CHARACTERS = new LinkedHashMap<>();
  static {
    CHARACTERS.put("plain", new int[] {10, 30, 25, 20, 15, 12});
    CHARACTERS.put("skilled", new int[] {120, 90, 80, 70, 60, 50});
    CHARACTERS.put("geared", new int[] {120, 90, 80, 70, 60, 50});
  }
  private static final Map<String, Integer> SKILLS = Map.of("fire", 130, "light", 150, "dark", 95, "attack", 40,
      "archery", 20, "dodge", 33);
  private static final Map<String, Integer> FLAGS = Map.of("legacy:power:fire", 15, "legacy:resist:dark", 40,
      "legacy:power:light", 7);
  private static final BodyPart[] SLOTS = {BodyPart.NECK, BodyPart.RING1, BodyPart.HEAD, BodyPart.BELT, BodyPart.WEAPON};

  private static Map<BodyPart, String> gear(ItemContent items) {
    Map<BodyPart, String> chosen = new LinkedHashMap<>();
    var sorted = new ArrayList<>(items.items().values());
    sorted.sort(Comparator.comparing(ItemContent.Definition::key));
    for (BodyPart slot : SLOTS) {
      for (var item : sorted) {
        boolean mirrored = item.secondaryBodyPart() != null;
        if (item.bodyPart() == slot && item.numId() > 0 && !item.boosts().isEmpty()
            && mirrored == (slot == BodyPart.WEAPON)) {
          chosen.put(slot, item.key());
          break;
        }
      }
    }
    return chosen;
  }

  private static CharacterProgress character(String name, ItemContent items) {
    int[] c = CHARACTERS.get(name);
    var base = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3)).progress();
    var inventory = new ArrayList<CharacterProgress.Item>();
    var equipment = new HashMap<String, UUID>();
    if (name.equals("geared")) {
      gear(items).forEach((slot, key) -> {
        var id = UUID.randomUUID();
        inventory.add(new CharacterProgress.Item(id, key, 1, 0));
        equipment.put(slot.name(), id);
      });
    }
    boolean plain = name.equals("plain");
    // Attributes are (strength, endurance, dexterity, wisdom, intelligence).
    return new CharacterProgress(base.version(), base.characterClass(), base.gender(),
        new CharacterProgress.Attributes(c[1], c[2], c[3], c[5], c[4]), base.vitals(),
        new CharacterProgress.Advancement(c[0], 0, 0, 0, 0), inventory, equipment,
        plain ? Map.of() : SKILLS, base.spells(), plain ? Map.of() : FLAGS, List.of(), 0, base.respawn(),
        List.of(), Map.of(), 0, base.companion());
  }

  private static Elements resist() {
    int[] r = TARGET_RESISTANCE;
    return new Elements(r[0], r[1], r[2], r[3], r[4], r[5]);
  }

  @Test void onlineCharactersYieldExactlyTheContextsSinglePlayerProduced() throws Exception {
    var items = ItemCatalog.loadBundled();
    var out = new StringBuilder("# Shared spell formula inputs; equal on single-player and online. Gear: ")
        .append(gear(items)).append('\n');
    for (String name : CHARACTERS.keySet()) {
      var caster = OnlineSpellContexts.caster(character(name, items), items);
      out.append(name).append(" castRules ").append(SpellFormulaContexts.describe(SpellFormulaContexts.forCastRules(caster))).append('\n');
      out.append(name).append(" self ").append(SpellFormulaContexts.describe(SpellFormulaContexts.forSelf(caster))).append('\n');
      out.append(name).append(" target ").append(SpellFormulaContexts.describe(SpellFormulaContexts.against(caster, resist()))).append('\n');
    }
    assertEquals(Files.readString(GOLDEN).replace("\r\n", "\n"), out.toString(),
        "Online inputs differ from the single-player golden (regenerate it from the root module only after the owner agrees)");
  }

  /** The context {@code OnlineCasting} built before the shared builder, copied unchanged. */
  private static DiceFormula.Context legacyOnlineContext(CharacterProgress progress, ItemContent items) {
    var bonuses = OnlineEquipmentBonuses.resolve(progress, items);
    var base = progress.attributes();
    int skill = progress.skills().getOrDefault("light", 0);
    int light = (skill <= 0 ? 100 : skill)
        + progress.questFlags().getOrDefault("legacy:power:light", 0)
        + bonuses.getOrDefault(23, 0);
    return new DiceFormula.Context(base.strength() + bonuses.getOrDefault(3, 0),
        base.endurance() + bonuses.getOrDefault(2, 0), base.dexterity() + bonuses.getOrDefault(6, 0),
        base.intelligence() + bonuses.getOrDefault(1, 0), 0, base.wisdom() + bonuses.getOrDefault(4, 0), 0,
        progress.advancement().level(), 0, 100, 100, light);
  }

  @Test void everyCurrentlySupportedSpellStillEvaluatesIdenticallyWithTheSharedContext() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    int checked = 0;
    for (String name : CHARACTERS.keySet()) {
      var progress = character(name, items);
      var old = legacyOnlineContext(progress, items);
      var rules = OnlineSpellContexts.forCastRules(progress, items);
      var self = OnlineSpellContexts.forSelf(progress, items);
      for (var spell : spells.spells().values()) {
        for (String formula : List.of(spell.manaCost(), spell.successRate(), spell.mentalExhaustion(),
            spell.physicalExhaustion(), spell.attackExhaustion())) {
          checked += same(formula, old, rules, spell.key());
        }
        for (var effect : spell.effects())
          for (var param : effect.params())
            if (param.expression() != null) checked += same(param.expression(), old, self, spell.key());
      }
    }
    assertTrue(checked > 50, "formulas actually compared: " + checked);
  }

  private static int same(String formula, DiceFormula.Context old, DiceFormula.Context shared, String spell) {
    var f = DiceFormula.of(formula);
    assertEquals(f.min(old), f.min(shared), spell + " min " + formula);
    assertEquals(f.max(old), f.max(shared), spell + " max " + formula);
    return 1;
  }
}
