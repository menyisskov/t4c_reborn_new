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
    assertEquals(SpellRegistry.playerCastableSpells().size() + 1, content.spells().size());
    Map<Kind, Long> counts = new EnumMap<>(Kind.class);
    content.spells().values().forEach(spell -> counts.merge(spell.kind(), 1L, Long::sum));
    // These are the design's table; a content change that moves one is a deliberate decision.
    var expected = Map.ofEntries(Map.entry(Kind.NUKE, 40L), Map.entry(Kind.AREA_DAMAGE, 25L),
        Map.entry(Kind.DRAIN, 4L), Map.entry(Kind.HEAL, 4L), Map.entry(Kind.AREA_HEAL, 4L),
        Map.entry(Kind.BUFF, 26L), Map.entry(Kind.DEBUFF, 1L), Map.entry(Kind.DOT, 4L),
        Map.entry(Kind.SHIELD, 3L), Map.entry(Kind.GATEWAY, 7L), Map.entry(Kind.PORTAL, 6L),
        Map.entry(Kind.RECALL, 1L), Map.entry(Kind.DETECTION, 4L), Map.entry(Kind.DISPEL, 2L),
        Map.entry(Kind.SPECIAL, 2L));
    assertEquals(expected, counts);
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
  void stageOneIsExactlyTheFortyEightDirectSpells() {
    var stageOne = OnlineSpellContentV2Exporter.snapshot().spells().values().stream()
        .filter(spell -> spell.stage() == 1).toList();
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
