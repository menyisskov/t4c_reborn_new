package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.model.SpellContentV2.Kind;
import com.perso.T4C.spell.SpellRegistry;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** The classification the engine stages are planned from (SPELL_ENGINE_DESIGN 1.2 and 6). */
class OnlineSpellContentV2ExporterTest {
  @Test
  void everyPlayerSpellIsClassifiedAndTheCountsMatchTheDesign() {
    var content = OnlineSpellContentV2Exporter.snapshot();
    assertEquals(SpellRegistry.playerCastableSpells().size() + 13, content.spells().size());
    Map<Kind, Long> counts = new EnumMap<>(Kind.class);
    content.spells().values().stream().filter(spell -> !spell.helperOnly())
        .forEach(spell -> counts.merge(spell.kind(), 1L, Long::sum));
    // These are the design's table; a content change that moves one is a deliberate decision.
    var expected = Map.ofEntries(Map.entry(Kind.NUKE, 40L), Map.entry(Kind.AREA_DAMAGE, 25L),
        Map.entry(Kind.DRAIN, 4L), Map.entry(Kind.HEAL, 4L), Map.entry(Kind.AREA_HEAL, 4L),
        Map.entry(Kind.BUFF, 36L), Map.entry(Kind.DEBUFF, 1L), Map.entry(Kind.DOT, 4L),
        Map.entry(Kind.SHIELD, 3L), Map.entry(Kind.GATEWAY, 7L), Map.entry(Kind.PORTAL, 6L),
        Map.entry(Kind.RECALL, 1L), Map.entry(Kind.DETECTION, 4L), Map.entry(Kind.DISPEL, 2L),
        Map.entry(Kind.SPECIAL, 2L));
    assertEquals(expected, counts);
    assertTrue(content.spells().get("spell.entangle_effect").helperOnly());
    assertTrue(content.spells().get("spell.entangle_effect_2").helperOnly());
    assertFalse(content.spells().get("spell.entangle").helperOnly());
  }

  @Test
  void exportsPotionOfFuryForTheAuthoritativeItemBuffPath() {
    var spell = OnlineSpellContentV2Exporter.snapshot().spells().get("spell.item_potion_of_fury");
    assertNotNull(spell);
    assertEquals(Kind.BUFF, spell.kind());
    assertEquals(2, spell.stage());
    assertEquals(300_000, Integer.parseInt(spell.duration()));
    assertTrue(spell.effects().stream().anyMatch(effect -> effect.type() == 2
        && effect.params().stream().anyMatch(param -> param.id() == 2 && "Strength".equals(param.expression()))));
  }

  @Test
  void exportsPotionOfNimblenessWithItsAuthoredDurationAndAgilityEffect() {
    var spell = OnlineSpellContentV2Exporter.snapshot().spells().get("spell.item_potion_of_nimbleness");
    assertNotNull(spell);
    assertEquals(Kind.BUFF, spell.kind());
    assertEquals(2, spell.stage());
    assertEquals(300_000, Integer.parseInt(spell.duration()));
    assertTrue(spell.effects().stream().anyMatch(effect -> effect.type() == 2
        && effect.params().stream().anyMatch(param -> param.id() == 2 && "agility".equals(param.expression()))
        && effect.params().stream().anyMatch(param -> param.id() == 3 && "10".equals(param.expression()))));
  }

  @Test
  void exportsHyperPotionOfNimblenessWithItsAuthoredFormulaAndDuration() {
    var spell = OnlineSpellContentV2Exporter.snapshot().spells().get("spell.item_hyper_potion_of_nimbleness");
    assertNotNull(spell);
    assertEquals(Kind.BUFF, spell.kind());
    assertEquals(2, spell.stage());
    assertEquals(300_000, Integer.parseInt(spell.duration()));
    assertTrue(spell.effects().stream().anyMatch(effect -> effect.type() == 2
        && effect.params().stream().anyMatch(param -> param.id() == 2 && "Agility".equals(param.expression()))
        && effect.params().stream().anyMatch(param -> param.id() == 3
            && "if(self.true_agi/4<50?50:self.true_agi/4)".equals(param.expression()))));
  }

  @Test
  void exportsTheThreeOrdinaryStatPotionsWithTheirAuthoredEffectsAndDuration() {
    var attributes = Map.of(
        "spell.item_potion_of_clear_thought", "Intelligence",
        "spell.item_potion_of_fortitude", "Endurance",
        "spell.item_potion_of_tranquility", "Wisdom");
    var content = OnlineSpellContentV2Exporter.snapshot();
    for (var entry : attributes.entrySet()) {
      var spell = content.spells().get(entry.getKey());
      assertNotNull(spell, entry.getKey());
      assertEquals(Kind.BUFF, spell.kind(), entry.getKey());
      assertEquals(2, spell.stage(), entry.getKey());
      assertEquals(300_000, Integer.parseInt(spell.duration()), entry.getKey());
      assertTrue(spell.effects().stream().anyMatch(effect -> effect.type() == 2
          && effect.params().stream().anyMatch(param -> param.id() == 2 && entry.getValue().equals(param.expression()))
          && effect.params().stream().anyMatch(param -> param.id() == 3 && "10".equals(param.expression()))),
          entry.getKey());
    }
  }

  @Test
  void exportsTheFourHyperStatPotionsWithTheirAuthoredFormulasAndDuration() {
    var formulas = Map.of(
        "spell.item_hyper_potion_of_clear_thought",
            Map.entry("Intelligence", "if(self.true_int/4<50?50:self.true_int/4)"),
        "spell.item_hyper_potion_of_fortitude",
            Map.entry("Endurance", "if(self.true_end/4<50?50:self.true_end/4)"),
        "spell.item_hyper_potion_of_tranquility",
            Map.entry("Wisdom", "if(self.true_wis/4<50?50:self.true_wis/4)"),
        "spell.item_hyper_potion_of_fury", Map.entry("Strength", "if(self.true_str/4<50?50:self.true_str/4)"));
    var content = OnlineSpellContentV2Exporter.snapshot();
    var legacy = OnlineSpellContentExporter.snapshot();
    for (var entry : formulas.entrySet()) {
      var spell = content.spells().get(entry.getKey());
      assertNotNull(spell, entry.getKey());
      assertEquals(Kind.BUFF, spell.kind(), entry.getKey());
      assertEquals(2, spell.stage(), entry.getKey());
      assertEquals(300_000, Integer.parseInt(spell.duration()), entry.getKey());
      assertTrue(spell.effects().stream().anyMatch(effect -> effect.type() == 2
          && effect.params().stream().anyMatch(param ->
              param.id() == 2 && entry.getValue().getKey().equals(param.expression()))
          && effect.params().stream().anyMatch(param ->
              param.id() == 3 && entry.getValue().getValue().equals(param.expression()))),
          entry.getKey());
      assertEquals(spell.effects(), legacy.spells().get(entry.getKey()).effects(), entry.getKey());
    }
  }

  @Test
  void exportsHyperPotionOfViolentRageWithItsAuthoredCompoundEffectsAndDuration() {
    var v2 = OnlineSpellContentV2Exporter.snapshot().spells().get("spell.item_hyper_potion_of_violent_rage");
    var v1 = OnlineSpellContentExporter.snapshot().spells().get("spell.item_hyper_potion_of_violent_rage");
    assertNotNull(v2);
    assertNotNull(v1);
    assertEquals(Kind.BUFF, v2.kind());
    assertEquals(2, v2.stage());
    assertEquals(60_000, Integer.parseInt(v2.duration()));
    assertEquals(com.perso.T4C.online.model.SpellContent.Use.ITEM, v1.use());
    assertEquals(10183, v1.id());
    assertEquals(v1.effects(), v2.effects());
    assertEquals(4, v2.effects().size());
    assertTrue(v2.effects().stream().anyMatch(effect -> effect.params().stream()
        .anyMatch(param -> param.id() == 2 && "str".equals(param.expression()))
        && effect.params().stream().anyMatch(param -> param.id() == 3 && "self.true_str*3".equals(param.expression()))));
    assertTrue(v2.effects().stream().anyMatch(effect -> effect.params().stream()
        .anyMatch(param -> param.id() == 2 && "attack".equals(param.expression()))
        && effect.params().stream().anyMatch(param -> param.id() == 3 && "self.true_attack*3".equals(param.expression()))));
    assertTrue(v2.effects().stream().anyMatch(effect -> effect.params().stream()
        .anyMatch(param -> param.id() == 2 && "AC".equals(param.expression()))
        && effect.params().stream().anyMatch(param -> param.id() == 3 && "-self.AC".equals(param.expression()))));
    assertTrue(v2.effects().stream().anyMatch(effect -> effect.params().stream()
        .anyMatch(param -> param.id() == 2 && "dodge".equals(param.expression()))
        && effect.params().stream().anyMatch(param -> param.id() == 3 && "-self.true_dodge*2".equals(param.expression()))));
  }

  @Test
  void stageOneIsExactlyTheFortyEightDirectSpells() {
    var stageOne = OnlineSpellContentV2Exporter.snapshot().spells().values().stream()
        .filter(spell -> !spell.helperOnly() && spell.stage() == 1).toList();
    assertEquals(48, stageOne.size());
    assertTrue(stageOne.stream().allMatch(spell -> spell.support().equals("stage-1")));
    var kinds = stageOne.stream().map(spell -> spell.kind()).collect(Collectors.toSet());
    assertEquals(java.util.Set.of(Kind.NUKE, Kind.DRAIN, Kind.HEAL), kinds);
    // Every stage-1 spell carries a first-parameter damage or heal formula the engine evaluates.
    assertTrue(stageOne.stream().allMatch(spell -> spell.effects().stream().anyMatch(effect ->
        (effect.type() == 1 || effect.type() == 10) && effect.params().stream().anyMatch(param ->
            param.id() == 1 && param.expression() != null && !param.expression().isBlank()))
        || spell.maxDamage() > 0));
    assertTrue(stageOne.stream().filter(spell -> spell.kind() == Kind.NUKE).allMatch(spell ->
        spell.attack() && spell.targetType() == 11 && spell.radius() >= 0));
    assertTrue(stageOne.stream().filter(spell -> spell.kind() == Kind.HEAL).allMatch(spell ->
        !spell.attack() && spell.targetType() == 3));
  }

  @Test
  void theSelfHealKeepsTheSameFormulasAsTheLiveAllowlist() {
    var v2 = OnlineSpellContentV2Exporter.snapshot().spells().get("spell.heal_light");
    var v1 = OnlineSpellContentExporter.snapshot().spells().get("spell.heal_light");
    assertEquals(v1.effects(), v2.effects());
    assertEquals(v1.manaCost(), v2.manaCost());
    assertEquals(v1.mentalExhaustion(), v2.mentalExhaustion());
    assertEquals(v1.id(), v2.id());
    assertEquals(1, v2.stage());
  }

  @Test
  void exportsRestoreSpellsFromFiniteMultiChargeItems() {
    OnlineSpellContentExporter.snapshot(); // Load authored items and spells.
    var potion = ItemRegistry.findByKey("item.critical_healing_potion");
    var multiCharge = withCharges(potion, 3, false);

    var exported = OnlineSpellContentExporter.snapshot(java.util.List.of(multiCharge));
    var spellId = potion.getSpells().getFirst().getSpellId();

    assertTrue(exported.spells().values().stream().anyMatch(spell ->
        spell.id() == spellId && spell.use() == com.perso.T4C.online.model.SpellContent.Use.ITEM));
    assertFalse(OnlineSpellContentExporter.snapshot(java.util.List.of(withCharges(potion, 3, true)))
        .spells().values().stream().anyMatch(spell -> spell.id() == spellId
            && spell.use() == com.perso.T4C.online.model.SpellContent.Use.ITEM));
  }

  @Test
  void exportsPotionOfFuryAsAnItemOnlySpell() {
    OnlineSpellContentExporter.snapshot();
    var item = ItemRegistry.findByKey("item.potion_of_fury");
    assertNotNull(item);
    var id = item.getSpells().getFirst().getSpellId();
    var spell = OnlineSpellContentExporter.snapshot().spells().values().stream()
        .filter(candidate -> candidate.id() == id).findFirst().orElseThrow();
    assertEquals("spell.item_potion_of_fury", spell.key());
    assertEquals(com.perso.T4C.online.model.SpellContent.Use.ITEM, spell.use());
  }

  private static ItemDefinition withCharges(ItemDefinition item, int charges, boolean unlimited) {
    return new ItemDefinition(item.getKey(), item.getName(), item.getBodyPart(),
        item.getAppearanceEquippedPrimary(), item.getSecondaryBodyPart(), item.getAppearanceEquippedSecondary(),
        item.getAppearanceInventory(), item.getPrice(), item.getWeight(), item.getArmorClass(), item.getDodgeLost(),
        item.getMinEnd(), item.getReqAttack(), item.getReqStr(), item.getReqAgi(), item.getMinInt(), item.getMinWis(),
        item.getAttackSpeed(), item.isUnique(), item.isBow(), unlimited, item.getNumId(), item.getStructure(),
        item.getAppearanceId(), item.getDmgFormula(), item.getAtkDelay(), item.getRadiance(), charges,
        item.isCanSummon(), item.getLockName(), item.getLockDiff(), item.getSignText(), item.getContainerGold(),
        item.getGlobalRespawn(), item.getLocalRespawn(), item.getSpells(), item.getBoosts(),
        item.getContainerLootGroups(), item.isUndroppable());
  }
}
