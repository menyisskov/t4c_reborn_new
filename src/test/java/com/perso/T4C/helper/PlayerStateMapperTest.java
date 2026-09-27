package com.perso.T4C.helper;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.combat.SeraphAuraService;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class PlayerStateMapperTest {
  @Test
  void preservesHalfTilePlayerCoordinates() throws Exception {
    Player player = new Player();
    player.setWorldPosition(3.5f * GRID_W, 4.5f * GRID_H, 0);
    PlayerStateDto state = PlayerStateMapper.fromPlayer(player);
    assertEquals(3.5f, state.x);
    assertEquals(4.5f, state.y);
  }

  @Test
  void clampsSavesFromBeforeTheLevelAndRebirthCaps() throws Exception {
    PlayerStateDto state = PlayerStateMapper.fromPlayer(new Player());
    state.level = 638;
    state.currentXp = 12345;
    state.xpToNextLevel = 99999;
    state.rebirthCount = 105;
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(state, restored);
    assertEquals(com.perso.T4C.config.GameConstants.MAX_PLAYER_LEVEL, restored.getLevel());
    assertEquals(0, restored.getCurrentXp());
    assertEquals(0, restored.getXpToNextLevel());
    assertEquals(com.perso.T4C.config.GameConstants.REBIRTH_MAX_REMORTS, restored.getRebirthCount());
  }

  /**
   * T4C-0084: a save made while the level-400 spells existed still names them in the spellbook, on
   * quick slots, in macros and as an active buff; all of those references are dropped on load and
   * everything else is kept untouched.
   */
  @Test
  void dropsRemovedSpellsFromOldSaves() throws Exception {
    PlayerStateDto state = PlayerStateMapper.fromPlayer(new Player());
    state.spells =
        new java.util.ArrayList<>(
            java.util.List.of("${spell.meteor}", "${spell.ashfall}", "${spell.sanctum_ward}"));
    state.quickSlots =
        new java.util.ArrayList<>(
            java.util.List.of(
                new com.perso.T4C.model.QuickSlotEntry(0, "${spell.meteor}"),
                new com.perso.T4C.model.QuickSlotEntry(1, "${spell.heavenfall}")));
    com.perso.T4C.config.MacroBinding kept = new com.perso.T4C.config.MacroBinding();
    kept.setSpellName("${spell.emberqueens_wrath}");
    com.perso.T4C.config.MacroBinding gone = new com.perso.T4C.config.MacroBinding();
    gone.setSpellName("${spell.tectonic_ruin}");
    state.macros = new java.util.ArrayList<>(java.util.List.of(gone, kept));
    PlayerStateDto.ActiveBuffState ward = new PlayerStateDto.ActiveBuffState();
    ward.spellName = "${spell.sanctum_ward}";
    ward.remainingSeconds = 60;
    ward.totalDurationSeconds = 120;
    state.activeBuffs = new java.util.ArrayList<>(java.util.List.of(ward));

    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(state, restored);

    assertEquals(java.util.List.of("${spell.meteor}"), restored.getSpells());
    assertEquals(1, restored.getQuickSlots().size());
    assertEquals("${spell.meteor}", restored.getQuickSlots().get(0).getSpell());
    assertEquals(1, restored.getMacros().size());
    assertEquals("${spell.emberqueens_wrath}", restored.getMacros().get(0).getSpellName());
    assertTrue(
        restored.getActiveBuffs().stream()
            .noneMatch(b -> "${spell.sanctum_ward}".equals(b.getSpellName())));
  }

  @Test
  void preservesQuestFlags() throws Exception {
    Player source = new Player();
    source.setQuestFlag("quest.lighthaven_samaritan_rats.status", 1);
    source.setQuestFlag("quest.__NEWBIE_QUEST.kills", 7);
    PlayerStateDto state = PlayerStateMapper.fromPlayer(source);
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(state, restored);
    assertEquals(1, restored.getQuestFlag("quest.lighthaven_samaritan_rats.status"));
    assertEquals(7, restored.getQuestFlag("quest.__NEWBIE_QUEST.kills"));
  }

  @Test
  void restoresCanonicalLightBuffWithItsRuntimeIdentityAndEffects() throws Exception {
    PlayerStateDto state = new PlayerStateDto();
    PlayerStateDto.ActiveBuffState light = new PlayerStateDto.ActiveBuffState();
    light.spellName = "spell.light";
    light.remainingSeconds = 299;
    light.totalDurationSeconds = 600;
    state.activeBuffs = java.util.List.of(light);
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(state, restored);
    assertEquals(1, restored.getActiveBuffs().size());
    assertEquals("${spell.light}", restored.getActiveBuffs().get(0).getSpellName());
    assertTrue(restored.hasRadianceBuff());
  }

  @Test
  void collapsesDuplicatePersistedSeraphAurasToTheCanonicalRuntimeBuff() throws Exception {
    PlayerStateDto state = new PlayerStateDto();
    state.rebirthCount = 1;
    PlayerStateDto.ActiveBuffState aura = new PlayerStateDto.ActiveBuffState();
    aura.spellName = SeraphAuraService.AURA_NAME;
    aura.remainingSeconds = Long.MAX_VALUE;
    aura.totalDurationSeconds = Long.MAX_VALUE;
    state.activeBuffs = java.util.List.of(aura, aura);
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(state, restored);
    SeraphAuraService.synchronize(restored);
    assertEquals(1, restored.getActiveBuffs().size());
    assertEquals(SeraphAuraService.AURA_NAME, restored.getActiveBuffs().get(0).getSpellName());
  }

  @Test
  void macrosBelongToTheCharacterNotTheInstall() throws Exception {
    Player caster = new Player();
    caster.getMacros().add(new com.perso.T4C.config.MacroBinding("Fire Dart", 34, 0));
    PlayerStateDto casterState = PlayerStateMapper.fromPlayer(caster);
    assertEquals(1, casterState.macros.size());

    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(casterState, restored);
    assertEquals(1, restored.getMacros().size());
    assertEquals("Fire Dart", restored.getMacros().get(0).getSpellName());

    // A second character loaded from its own save must not see the first one's bindings, and a
    // save written before T4C-0059 has no macros at all rather than inheriting the shared list.
    PlayerStateDto legacyState = PlayerStateMapper.fromPlayer(new Player());
    legacyState.macros = null;
    Player other = new Player();
    PlayerStateMapper.applyToPlayer(legacyState, other);
    assertTrue(other.getMacros().isEmpty());
  }
}
