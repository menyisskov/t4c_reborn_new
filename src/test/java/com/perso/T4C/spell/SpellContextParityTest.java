package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellFormulaContexts.Elements;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Single-player half of the formula-context parity proof. A real {@link Player} is built for each
 * fixture character; its shared inputs must equal the original single-player construction (the
 * {@code legacy*} methods are the pre-extraction code, copied unchanged) and must match the
 * committed golden text, which the server module's {@code OnlineSpellContextParityTest} reproduces
 * from a committed online character with the same numbers. Regenerate the golden after an
 * intentional content change with {@code -Dspell.golden.update=true}.
 */
@ResourceLock("ItemRegistry")
class SpellContextParityTest {
  static final Path GOLDEN = Path.of("src/test/resources/spell/formula-context-golden.txt");
  static final int[] TARGET_RESISTANCE = {100, 250, 7, 100, 100, 3};

  @BeforeAll
  static void loadItems() {
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
  }

  /** Fixture: name -> {level, str, end, dex, int, wis}. Mirrored verbatim in the server test. */
  static final Map<String, int[]> CHARACTERS = new LinkedHashMap<>();
  static {
    CHARACTERS.put("plain", new int[] {10, 30, 25, 20, 15, 12});
    CHARACTERS.put("skilled", new int[] {120, 90, 80, 70, 60, 50});
    CHARACTERS.put("geared", new int[] {120, 90, 80, 70, 60, 50});
  }

  static final Map<String, Integer> SKILLS = Map.of("fire", 130, "light", 150, "dark", 95, "attack", 40,
      "archery", 20, "dodge", 33);
  static final Map<String, Integer> FLAGS = Map.of("legacy:power:fire", 15, "legacy:resist:dark", 40,
      "legacy:power:light", 7);
  static final BodyPart[] SLOTS = {BodyPart.NECK, BodyPart.RING1, BodyPart.HEAD, BodyPart.BELT, BodyPart.WEAPON};

  /** The first item (by key) with boosts for the slot; weapons must occupy two slots. */
  static Map<BodyPart, String> gear() {
    Map<BodyPart, String> chosen = new LinkedHashMap<>();
    List<ItemDefinition> items = new ArrayList<>(ItemRegistry.allByKey().values());
    items.sort(Comparator.comparing(ItemDefinition::getKey));
    for (BodyPart slot : SLOTS) {
      for (ItemDefinition item : items) {
        boolean mirrored = item.getSecondaryBodyPart() != null;
        if (item.getBodyPart() == slot && item.getNumId() > 0 && !item.getBoosts().isEmpty()
            && mirrored == (slot == BodyPart.WEAPON)) {
          chosen.put(slot, item.getKey());
          break;
        }
      }
    }
    return chosen;
  }

  static Player player(String name) {
    int[] c = CHARACTERS.get(name);
    Player player = new Player();
    player.setLevel(c[0]);
    player.setStrength(c[1]);
    player.setEndurance(c[2]);
    player.setDexterity(c[3]);
    player.setIntelligence(c[4]);
    player.setWisdom(c[5]);
    if (!name.equals("plain")) {
      SKILLS.forEach(player::setSkillLevel);
      FLAGS.forEach(player::setQuestFlag);
    }
    if (name.equals("geared")) {
      gear().forEach((slot, key) -> {
        player.getEquippedItems().put(slot, key);
        ItemDefinition item = ItemRegistry.findByKey(key);
        if (item.getSecondaryBodyPart() != null) player.getEquippedItems().put(item.getSecondaryBodyPart(), key);
      });
    }
    return player;
  }

  // ---- the original single-player constructions, unchanged ----

  private static Map<String, Integer> legacySelfTrue(Player caster) {
    Map<String, Integer> readings = new LinkedHashMap<>();
    readings.put("str", caster.getTrueStrength());
    readings.put("end", caster.getTrueEndurance());
    readings.put("agi", caster.getTrueDexterity());
    readings.put("int", caster.getTrueIntelligence());
    readings.put("wis", caster.getTrueWisdom());
    readings.put("level", caster.getLevel());
    readings.put("attack", caster.getTrueSkillLevel("attack"));
    readings.put("archery", caster.getTrueSkillLevel("archery"));
    readings.put("dodge", caster.getTrueSkillLevel("dodge"));
    for (String element : List.of("fire", "earth", "air", "water", "light", "dark")) {
      readings.put(element, caster.getTrueElementPower(element));
      readings.put("r_" + element, caster.getTrueElementResistance(element));
    }
    return readings;
  }

  private static Map<Integer, Integer> legacyEquipped(Player caster) {
    Map<Integer, Integer> counts = new LinkedHashMap<>();
    for (String key : caster.getEquippedItems().values()) {
      var item = ItemRegistry.findByKey(key);
      if (item != null && item.getNumId() > 0) counts.merge(item.getNumId(), 1, Integer::sum);
    }
    return counts;
  }

  private static DiceFormula.Context legacyCastRules(Player caster) {
    return new DiceFormula.Context(caster.getEffectiveStrength(), caster.getEffectiveEndurance(),
        caster.getEffectiveDexterity(), caster.getEffectiveIntelligence(), 0, caster.getEffectiveWisdom(), 0,
        caster.getLevel()).withSelfTrue(legacySelfTrue(caster)).withEquippedItems(legacyEquipped(caster));
  }

  private static DiceFormula.Context legacySelf(Player caster) {
    DiceFormula.Context base = legacyCastRules(caster);
    return new DiceFormula.Context(base.str, base.end, base.agi, base.intel, base.wil, base.wis, base.luck,
        base.level, 0, 100, 100, 100, 100, 100, 100,
        caster.getElementPower("fire"), caster.getElementPower("earth"), caster.getElementPower("air"),
        caster.getElementPower("water"), caster.getElementPower("light"), caster.getElementPower("dark"))
        .withSelfTrue(base.selfTrue).withEquippedItems(base.equippedItems);
  }

  private static DiceFormula.Context legacyAgainst(Player caster, int[] r) {
    return new DiceFormula.Context(caster.getEffectiveStrength(), caster.getEffectiveEndurance(),
        caster.getEffectiveDexterity(), caster.getEffectiveIntelligence(), 0, caster.getEffectiveWisdom(), 0,
        caster.getLevel(), 0, r[0], r[1], r[2], r[3], r[4], r[5],
        caster.getElementPower("fire"), caster.getElementPower("earth"), caster.getElementPower("air"),
        caster.getElementPower("water"), caster.getElementPower("light"), caster.getElementPower("dark"))
        .withSelfTrue(legacySelfTrue(caster)).withEquippedItems(legacyEquipped(caster));
  }

  private static Elements resist() {
    int[] r = TARGET_RESISTANCE;
    return new Elements(r[0], r[1], r[2], r[3], r[4], r[5]);
  }

  @Test
  void sharedBuildersReproduceTheOriginalSinglePlayerContexts() {
    for (String name : CHARACTERS.keySet()) {
      Player player = player(name);
      var caster = SpellCastingService.casterOf(player);
      assertEquals(SpellFormulaContexts.describe(legacyCastRules(player)),
          SpellFormulaContexts.describe(SpellCastingService.context(player)), name + " cast rules");
      assertEquals(SpellFormulaContexts.describe(legacySelf(player)),
          SpellFormulaContexts.describe(SpellFormulaContexts.forSelf(caster)), name + " self");
      assertEquals(SpellFormulaContexts.describe(legacySelf(player)),
          SpellFormulaContexts.describe(SpellEffectManager.casterContext(player)), name + " manager self");
      assertEquals(SpellFormulaContexts.describe(legacyAgainst(player, TARGET_RESISTANCE)),
          SpellFormulaContexts.describe(SpellFormulaContexts.against(caster, resist())), name + " target");
    }
  }

  @Test
  void gearedFixtureReallyEquipsBoostedGear() {
    var gear = gear();
    assertTrue(gear.size() >= 3, "fixture needs boosted gear: " + gear);
    assertNotEquals(
        SpellFormulaContexts.describe(SpellFormulaContexts.forSelf(SpellCastingService.casterOf(player("skilled")))),
        SpellFormulaContexts.describe(SpellFormulaContexts.forSelf(SpellCastingService.casterOf(player("geared")))),
        "gear must change at least one input");
  }

  @Test
  void contextsMatchTheCommittedGoldenSharedWithTheServer() throws Exception {
    var out = new StringBuilder("# Shared spell formula inputs; equal on single-player and online. Gear: ")
        .append(gear()).append('\n');
    for (String name : CHARACTERS.keySet()) {
      var caster = SpellCastingService.casterOf(player(name));
      out.append(name).append(" castRules ").append(SpellFormulaContexts.describe(SpellFormulaContexts.forCastRules(caster))).append('\n');
      out.append(name).append(" self ").append(SpellFormulaContexts.describe(SpellFormulaContexts.forSelf(caster))).append('\n');
      out.append(name).append(" target ").append(SpellFormulaContexts.describe(SpellFormulaContexts.against(caster, resist()))).append('\n');
    }
    if (Boolean.getBoolean("spell.golden.update")) {
      Files.createDirectories(GOLDEN.getParent());
      Files.writeString(GOLDEN, out.toString());
    }
    assertEquals(Files.readString(GOLDEN).replace("\r\n", "\n"), out.toString(),
        "Regenerate with -Dspell.golden.update=true and review the diff");
  }
}
