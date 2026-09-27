package com.perso.T4C.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.mapping.definition.ObjectMappingDefinitions;
import com.perso.T4C.mapping.definition.ObjectPositionDefinitions;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class TempleBlessingChestTest {
  private static ObjectPos chestAt(TempleBlessingService.Shrine shrine) {
    return new ObjectPos(
        TempleBlessingService.OBJECT_NAME, shrine.tileX(), shrine.tileY(), shrine.worldZ());
  }

  /**
   * The chest a player clicks comes from {@link ObjectPositionDefinitions}, while the blessing it
   * hands out comes from {@link TempleBlessingService#SHRINES}. A chest in one list and not the
   * other is either a chest that does nothing when clicked or a blessing nobody can reach, and
   * neither fails loudly on its own.
   */
  @Test
  void everyPlacedChestHasAShrineAndEveryShrineHasAPlacedChest() {
    Set<String> placed =
        ObjectPositionDefinitions.all().stream()
            .filter(pos -> TempleBlessingService.OBJECT_NAME.equalsIgnoreCase(pos.name()))
            .map(pos -> pos.x() + "," + pos.y() + ",z" + pos.z())
            .collect(Collectors.toSet());
    Set<String> shrines =
        TempleBlessingService.SHRINES.stream()
            .map(s -> s.tileX() + "," + s.tileY() + ",z" + s.worldZ())
            .collect(Collectors.toSet());
    assertEquals(shrines, placed, "Placed blessing chests and shrine tiles must match exactly");
    assertEquals(TempleBlessingService.SHRINES.size(), placed.size(), "Two shrines share a tile");
  }

  /** Without a mapping the renderer never draws the chest, so it cannot be clicked. */
  @Test
  void theChestObjectIsMappedAndClickable() {
    var mapping = ObjectMappingDefinitions.all().get(TempleBlessingService.OBJECT_NAME);
    assertNotNull(mapping, "The blessing chest needs an object mapping to be drawn");
    assertTrue(
        mapping.clickAnimate,
        "The blessing chest is not a container item, so only clickAnimate makes it clickable");
  }

  /**
   * Each key in the blessing list must still resolve to a spell that produces at least one
   * attribute boost. This is what catches a renamed spell, and it is what catches a formula the
   * evaluator cannot read - an unresolved variable yields 0, not an error.
   */
  @Test
  void everyBlessingSpellResolvesAndProducesEffects() {
    Player player = new Player();
    var context =
        com.perso.T4C.spell.SpellEffectManager.externalCasterContext(
            player,
            TempleBlessingService.TOWN_CASTER_INTELLIGENCE,
            TempleBlessingService.TOWN_CASTER_WISDOM);
    var manager = new com.perso.T4C.spell.SpellEffectManager();
    for (String key : TempleBlessingService.BLESSING_SPELLS) {
      SpellData spell = SpellRegistry.findByName(key);
      assertNotNull(spell, () -> "Blessing spell no longer resolves: " + key);
      List<SpellData.SpellEffect> effects = manager.resolvePlayerBuffEffects(spell, context);
      assertFalse(effects.isEmpty(), () -> "Blessing spell produces no effects: " + key);
      for (SpellData.SpellEffect effect : effects) {
        assertNotEquals0(effect, key);
      }
    }
  }

  private static void assertNotEquals0(SpellData.SpellEffect effect, String key) {
    assertTrue(
        Integer.parseInt(effect.getAmount()) != 0,
        () -> "Blessing spell " + key + " boosts " + effect.getAttribute() + " by 0");
  }

  @Test
  void clickingTheChestLaysEveryWardOnTheClicker() {
    Player player = new Player();
    TempleBlessingService service = new TempleBlessingService();
    TempleBlessingService.Result result =
        service.bless(player, chestAt(TempleBlessingService.SHRINES.get(0)));
    assertTrue(result.blessed());
    assertEquals(TempleBlessingService.BLESSING_SPELLS.size(), result.spellCount());
    assertEquals(
        TempleBlessingService.BLESSING_SPELLS.size(),
        player.getActiveBuffs().size(),
        "Each ward should be its own buff on the bar");
  }

  /**
   * T4C-0066: the blessing now also tops off health and mana, not just the wards. The restore has
   * to read {@code getMaxHp()} after the ward loop, not before, since Bless itself can raise it -
   * healing to the pre-blessing ceiling would leave the player short of the new one.
   */
  @Test
  void clickingTheChestRestoresFullHealthAndMana() {
    Player player = new Player();
    player.setMaxHp(500);
    player.setCurrentHp(1);
    player.setMaxMana(300);
    player.setMana(0);
    TempleBlessingService service = new TempleBlessingService();
    TempleBlessingService.Result result =
        service.bless(player, chestAt(TempleBlessingService.SHRINES.get(0)));
    assertTrue(result.blessed());
    assertEquals(player.getMaxHp(), player.getCurrentHp(), "Blessing should heal to full");
    assertEquals(player.getMaxMana(), player.getMana(), "Blessing should restore mana to full");
  }

  /** A second click refreshes the same wards rather than stacking a second copy of each. */
  @Test
  void clickingTwiceRefreshesRatherThanStacks() {
    Player player = new Player();
    TempleBlessingService service = new TempleBlessingService();
    ObjectPos chest = chestAt(TempleBlessingService.SHRINES.get(0));
    service.bless(player, chest);
    int afterFirst = player.getActiveBuffs().size();
    service.bless(player, chest);
    assertEquals(afterFirst, player.getActiveBuffs().size());
  }

  /**
   * Avalon's archmage has to actually buy something, or the walk out there is pointless and the
   * stronger stats on that one shrine are decoration.
   */
  @Test
  void avalonGivesAStrongerBlessingThanAnOrdinaryTown() {
    TempleBlessingService service = new TempleBlessingService();
    TempleBlessingService.Shrine town =
        TempleBlessingService.SHRINES.stream()
            .filter(s -> s.casterWisdom() == TempleBlessingService.TOWN_CASTER_WISDOM)
            .findFirst()
            .orElseThrow();
    TempleBlessingService.Shrine avalon =
        TempleBlessingService.SHRINES.stream()
            .filter(s -> s.casterWisdom() == TempleBlessingService.AVALON_CASTER_WISDOM)
            .findFirst()
            .orElseThrow();

    Player weak = new Player();
    Player strong = new Player();
    service.bless(weak, chestAt(town));
    service.bless(strong, chestAt(avalon));
    assertTrue(
        strong.getArmorClassBoost() > weak.getArmorClassBoost(),
        "Avalon should ward you better than an ordinary town");
    assertTrue(
        strong.getEffectiveWisdom() > weak.getEffectiveWisdom(),
        "Avalon's Tranquility should be stronger");
  }

  /**
   * The five ordinary towns are deliberately interchangeable: whichever one you are standing in,
   * the blessing is the same, and only Avalon is worth travelling for.
   */
  @Test
  void theOrdinaryTownsAllGiveTheSameBlessing() {
    assertEquals(
        5,
        TempleBlessingService.SHRINES.stream()
            .filter(
                s ->
                    s.casterWisdom() == TempleBlessingService.TOWN_CASTER_WISDOM
                        && s.casterIntelligence() == TempleBlessingService.TOWN_CASTER_INTELLIGENCE)
            .count(),
        "Five towns should share the ordinary blessing");
    assertEquals(
        1,
        TempleBlessingService.SHRINES.stream()
            .filter(s -> s.casterWisdom() == TempleBlessingService.AVALON_CASTER_WISDOM)
            .count(),
        "Only Avalon Sanctuary should give the archmage's blessing");
  }

  /**
   * DESIGN_GUIDELINES.md quotes what each blessing is worth, and a doc that restates game maths
   * goes stale the moment someone retunes the caster stats. These are the numbers in that table; if
   * you change the stats on purpose, change both.
   *
   * <p>Max HP is left out deliberately: Bless rolls {@code 1d(wis/4)} into it, so it has no single
   * value to pin.
   */
  @Test
  void theBlessingIsWorthWhatTheGuidelinesSay() {
    TempleBlessingService service = new TempleBlessingService();

    Player town = new Player();
    service.bless(town, chestAt(shrineWithWisdom(TempleBlessingService.TOWN_CASTER_WISDOM)));
    assertEquals(59, town.getArmorClassBoost(), "Ordinary town armour class");
    assertEquals(100, town.getEffectiveWisdom(), "Ordinary town wisdom");
    assertEquals(33, town.getEffectiveStrength(), "Ordinary town strength");

    Player avalon = new Player();
    service.bless(avalon, chestAt(shrineWithWisdom(TempleBlessingService.AVALON_CASTER_WISDOM)));
    assertEquals(258, avalon.getArmorClassBoost(), "Avalon armour class");
    assertEquals(750, avalon.getEffectiveWisdom(), "Avalon wisdom");
    assertEquals(135, avalon.getEffectiveStrength(), "Avalon strength");
  }

  private static TempleBlessingService.Shrine shrineWithWisdom(int casterWisdom) {
    return TempleBlessingService.SHRINES.stream()
        .filter(s -> s.casterWisdom() == casterWisdom)
        .findFirst()
        .orElseThrow();
  }

  /**
   * A saved buff keeps only its name and remaining time; its effects are rebuilt on load.
   * Rebuilding them from the character - which is right for a spell you cast on yourself - would
   * rescale an archmage's blessing down to your own wisdom, so a blessing that read +258 armour
   * before you logged out would read a fraction of that when you came back.
   */
  @Test
  void aBlessingSurvivesSavingAndLoadingAtFullStrength() throws Exception {
    Player blessed = new Player();
    new TempleBlessingService()
        .bless(blessed, chestAt(shrineWithWisdom(TempleBlessingService.AVALON_CASTER_WISDOM)));
    int armourBefore = blessed.getArmorClassBoost();
    int wisdomBefore = blessed.getEffectiveWisdom();
    assertTrue(armourBefore > 0, "The blessing should have applied in the first place");

    PlayerStateDto saved = PlayerStateMapper.fromPlayer(blessed);
    Player reloaded = new Player();
    PlayerStateMapper.applyToPlayer(saved, reloaded);

    assertEquals(armourBefore, reloaded.getArmorClassBoost(), "Armour class after a reload");
    assertEquals(wisdomBefore, reloaded.getEffectiveWisdom(), "Wisdom after a reload");
  }

  /**
   * A save written before blessings existed carries no caster, which must keep meaning "the player
   * cast it" - the buff is rebuilt from the character, exactly as it always was.
   */
  @Test
  void anOlderSaveWithNoRecordedCasterStillLoads() throws Exception {
    Player player = new Player();
    player.setWisdom(120);
    player.setIntelligence(80);
    SpellData tranquility = SpellRegistry.findByName("spell.tranquility");
    assertNotNull(tranquility);
    var manager = new com.perso.T4C.spell.SpellEffectManager();
    player.applyBuff(
        tranquility.getName(),
        tranquility.getDescription(),
        tranquility.getIconId(),
        manager.resolveDurationSeconds(tranquility, player),
        false,
        manager.resolvePlayerBuffEffects(tranquility, player));
    int wisdomBefore = player.getEffectiveWisdom();

    PlayerStateDto saved = PlayerStateMapper.fromPlayer(player);
    for (PlayerStateDto.ActiveBuffState buff : saved.activeBuffs) {
      assertEquals(0, buff.casterWisdom, "A self-cast buff records no caster");
      assertEquals(0, buff.casterIntelligence, "A self-cast buff records no caster");
    }
    Player reloaded = new Player();
    reloaded.setWisdom(120);
    reloaded.setIntelligence(80);
    PlayerStateMapper.applyToPlayer(saved, reloaded);
    assertEquals(wisdomBefore, reloaded.getEffectiveWisdom());
  }

  /** Any other object on the map must be left alone for ChestService and the rest to handle. */
  @Test
  void anOrdinaryObjectIsNotABlessingChest() {
    assertNull(TempleBlessingService.shrineAt(new ObjectPos("Chest 1", 2688, 362, 0)));
    assertNull(TempleBlessingService.shrineAt(null));
    // Right name, wrong tile: only the placed chests bless.
    assertNull(
        TempleBlessingService.shrineAt(new ObjectPos(TempleBlessingService.OBJECT_NAME, 1, 1, 0)));
    Player player = new Player();
    assertFalse(
        new TempleBlessingService()
            .bless(player, new ObjectPos("Chest 1", 2688, 362, 0))
            .blessed());
    assertTrue(player.getActiveBuffs().isEmpty());
  }
}
