package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineCastingTest {
  private static final long NOW = 10_000L;
  private static final String HEAL = "spell.heal_light";

  @Test void selfHealSpendsManaRestoresHealthAndPersistsExhaustionExpiries() throws Exception {
    var p = progress(new CharacterProgress.Vitals(10, 100, 20, 30), Set.of(HEAL), Map.of());
    var cast = OnlineCasting.prepareSelfCast(state(p), HEAL, NOW, new Random(7),
        ItemCatalog.loadBundled(), SpellCatalog.loadBundled());
    // heal_light restores at least 9 (1d5 + 8) and spends its 2 mana.
    assertTrue(cast.healthGained() >= 9, "healing must be at least the base roll");
    assertEquals(10 + cast.healthGained(), cast.progress().vitals().hp());
    assertEquals(2, cast.manaSpent());
    assertEquals(18, cast.progress().vitals().mana());
    assertTrue(cast.progress().cooldowns().get(OnlineCasting.MENTAL_EXHAUSTION) > NOW,
        "mental exhaustion is persisted as an absolute expiry");
    assertTrue(cast.progress().cooldowns().get(OnlineCasting.PHYSICAL_EXHAUSTION) > NOW);
  }

  @Test void mentalExhaustionBlocksCastingUntilItsExpiryAndNotAfterIt() throws Exception {
    var exhausted = progress(new CharacterProgress.Vitals(10, 100, 20, 30), Set.of(HEAL),
        Map.of(OnlineCasting.MENTAL_EXHAUSTION, NOW + 1_000));
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var refused = assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineCasting.prepareSelfCast(state(exhausted), HEAL, NOW, new Random(7), items, spells));
    assertEquals("exhausted", refused.getMessage());
    // An expiry equal to now is no longer active, so the cast is allowed at exactly that instant.
    var ready = OnlineCasting.prepareSelfCast(state(exhausted), HEAL, NOW + 1_000, new Random(7), items, spells);
    assertEquals(18, ready.progress().vitals().mana());
  }

  @Test void castingAtFullHealthIsAllowedAndStillSpendsMana() throws Exception {
    var full = progress(new CharacterProgress.Vitals(100, 100, 20, 30), Set.of(HEAL), Map.of());
    var cast = OnlineCasting.prepareSelfCast(state(full), HEAL, NOW, new Random(7),
        ItemCatalog.loadBundled(), SpellCatalog.loadBundled());
    assertEquals(0, cast.healthGained());
    assertEquals(2, cast.manaSpent());
  }

  @Test void unknownGrantOnlyAndUnaffordableCastsAreRefusedWithoutAnyDraft() throws Exception {
    var items = ItemCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var notLearned = progress(new CharacterProgress.Vitals(10, 100, 20, 30), Set.of(), Map.of());
    assertEquals("not_learned", assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineCasting.prepareSelfCast(state(notLearned), HEAL, NOW, new Random(7), items, spells)).getMessage());

    // Renew armor is granted by a quest and is never cast online, even once learned.
    var granted = progress(new CharacterProgress.Vitals(10, 100, 20, 30), Set.of("spell.renew_armor"), Map.of());
    assertEquals("unsupported_spell", assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineCasting.prepareSelfCast(state(granted), "spell.renew_armor", NOW, new Random(7), items, spells)).getMessage());

    var broke = progress(new CharacterProgress.Vitals(10, 100, 1, 30), Set.of(HEAL), Map.of());
    assertEquals("not_enough_mana", assertThrows(PreviewWorld.Rejected.class, () ->
        OnlineCasting.prepareSelfCast(state(broke), HEAL, NOW, new Random(7), items, spells)).getMessage());
  }

  private static OnlineStore.CharacterState state(CharacterProgress p) {
    return new OnlineStore.CharacterState(UUID.randomUUID(), UUID.randomUUID(), "Caster", 0, 3, 3, 0, 0, 0, p);
  }

  /** A valid warrior with the given vitals, learned spells and cooldowns. */
  private static CharacterProgress progress(CharacterProgress.Vitals vitals, Set<String> spells,
      Map<String, Long> cooldowns) {
    var base = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3)).progress();
    return new CharacterProgress(base.version(), base.characterClass(), base.gender(), base.attributes(),
        vitals, base.advancement(), List.of(), Map.of(), base.skills(), spells, Map.of(), List.of(), 0,
        base.respawn(), List.of(), cooldowns, 0, base.companion());
  }
}
