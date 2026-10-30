package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.spell.SpellCastRules;
import com.perso.T4C.spell.SpellFormulaContexts;
import com.perso.T4C.spell.engine.SpellEngine;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Timed buffs in committed progress: laying, refreshing, expiry, the join shift, old records, and what is folded in. */
class OnlineBuffsTest {
  private static final long NOW = 1_000_000_000_000L;

  private static CharacterProgress base() throws Exception {
    var items = ItemCatalog.loadBundled();
    var p = OnlineSpellContextParityTest.character("geared", items);
    return ProgressEdits.withVitals(p, new CharacterProgress.Vitals(100, 200, 500, 500));
  }

  private static SpellEngine.Event.ApplyBuff buff(String key, int seconds, Object... attributeAmounts) {
    var modifiers = new java.util.ArrayList<SpellEngine.Modifier>();
    for (int i = 0; i < attributeAmounts.length; i += 2)
      modifiers.add(new SpellEngine.Modifier((String) attributeAmounts[i], (Integer) attributeAmounts[i + 1]));
    return new SpellEngine.Event.ApplyBuff(null, key, seconds, modifiers);
  }

  @Test void layingARecastRefreshesTheBuffInsteadOfStackingAndMaximumHealthFollowsTheAmounts() throws Exception {
    var p = OnlineBuffs.apply(base(), buff("spell.bless", 1800, "maxHp", 70, "skill:attack", 29), NOW);
    assertEquals(270, p.vitals().maxHp());
    assertEquals(100, p.vitals().hp(), "laying a buff never heals");
    assertEquals(NOW + 1_800_000, p.buffs().get(0).expiresAt());
    var again = OnlineBuffs.apply(p, buff("spell.bless", 1800, "maxHp", 60, "skill:attack", 25), NOW + 5_000);
    assertEquals(1, again.buffs().size(), "one effect per spell");
    assertEquals(260, again.vitals().maxHp(), "the old amount is taken back before the new one is added");
    assertEquals(NOW + 1_805_000, again.buffs().get(0).expiresAt(), "a recast restarts the timer");
    assertEquals(25, OnlineBuffs.totals(again, NOW + 5_000).get("skill:attack"));
    var other = OnlineBuffs.apply(again, buff("spell.stone_skin", 1200, "armorClass", 17), NOW + 6_000);
    assertEquals(2, other.buffs().size());
    assertEquals(17, OnlineBuffs.totals(other, NOW + 6_000).get("armorClass"), "different spells add up");
  }

  @Test void expiryIsStrictAndTakesBackMaximumHealthAndClampsCurrentHealth() throws Exception {
    var p = OnlineBuffs.apply(base(), buff("spell.bless", 10, "maxHp", 70), NOW);
    p = ProgressEdits.withVitals(p, new CharacterProgress.Vitals(260, 270, 500, 500));
    assertEquals(1, OnlineBuffs.active(p, NOW + 9_999).size());
    assertTrue(OnlineBuffs.active(p, NOW + 10_000).isEmpty(), "now == expiresAt is already expired, as offline");
    assertSame(p, OnlineBuffs.expire(p, NOW + 9_999), "nothing to take off before the end");
    var expired = OnlineBuffs.expire(p, NOW + 10_000);
    assertTrue(expired.buffs().isEmpty());
    assertEquals(200, expired.vitals().maxHp());
    assertEquals(200, expired.vitals().hp(), "current health never exceeds the maximum after expiry");
  }

  @Test void aBuffWithNoDurationNeverExpiresAndIsNeverShifted() throws Exception {
    var p = OnlineBuffs.apply(base(), buff("spell.light", 0, "radiance", 100), NOW);
    assertEquals(Long.MAX_VALUE, p.buffs().get(0).expiresAt());
    assertEquals(1, OnlineBuffs.active(p, Long.MAX_VALUE - 1).size());
    var anchored = OnlineBuffs.anchored(p, NOW);
    var joined = OnlineBuffs.shiftForJoin(anchored, NOW + 3_600_000);
    assertEquals(Long.MAX_VALUE, joined.buffs().get(0).expiresAt());
  }

  @Test void loggedOutTimeNeverRunsABuffDown() throws Exception {
    var p = OnlineBuffs.apply(base(), buff("spell.protection", 1200, "armorClass", 3), NOW);
    long expires = p.buffs().get(0).expiresAt();
    // Online for a minute, last settled at NOW + 60 s, then away for an hour.
    var left = OnlineBuffs.anchored(p, NOW + 60_000);
    assertEquals(NOW + 60_000, left.cooldowns().get(OnlineBuffs.ANCHOR));
    long rejoin = NOW + 60_000 + 3_600_000;
    var joined = OnlineBuffs.shiftForJoin(left, rejoin);
    assertEquals(expires + 3_600_000, joined.buffs().get(0).expiresAt(), "the hour away is added back");
    assertEquals(rejoin, joined.cooldowns().get(OnlineBuffs.ANCHOR));
    assertEquals(1, OnlineBuffs.active(joined, rejoin + 1_139_000).size(), "the remaining time is what it was when it left");
    assertTrue(OnlineBuffs.active(joined, rejoin + 1_140_000).isEmpty());
    assertSame(joined, OnlineBuffs.shiftForJoin(joined, rejoin), "joining again at the same instant changes nothing");
    var plain = base();
    assertSame(plain, OnlineBuffs.shiftForJoin(plain, rejoin), "a character without buffs is untouched");
  }

  @Test void dispelRemovesARunningBuffAndItsMaximumHealth() throws Exception {
    var p = OnlineBuffs.apply(base(), buff("spell.bless", 1800, "maxHp", 70), NOW);
    var gone = OnlineBuffs.dispel(p, "spell.bless", NOW + 1);
    assertTrue(gone.buffs().isEmpty());
    assertEquals(200, gone.vitals().maxHp());
    assertSame(p, OnlineBuffs.dispel(p, "spell.stone_skin", NOW + 1), "nothing to remove");
    assertSame(p, OnlineBuffs.dispel(p, "spell.bless", NOW + 1_800_000), "an expired buff is not dispelled twice");
  }

  @Test void aBuffFromBeforeAmountsWereStoredAndAnUnmodelledAttributeFailClosed() throws Exception {
    var p = base();
    var old = new CharacterProgress(1, p.characterClass(), p.gender(), p.attributes(), p.vitals(), p.advancement(),
        p.inventory(), p.equipment(), p.skills(), p.spells(), p.questFlags(), p.storage(), p.storageGold(), p.respawn(),
        List.of(new CharacterProgress.Buff("spell.bless", NOW + 1_000, 0, 0)), p.cooldowns(), 0, p.companion());
    var items = ItemCatalog.loadBundled();
    assertThrows(IllegalArgumentException.class, () -> OnlineCombatLoadout.resolve(old, items, NOW, false));
    assertDoesNotThrow(() -> OnlineCombatLoadout.resolve(old, items, NOW + 1_000, false), "once it has ended it is ignored");
    assertEquals("unsupported_effects", assertThrows(PreviewWorld.Rejected.class,
        () -> OnlineCasting.aliveProgress(state(old), NOW)).getMessage());
    var unknown = OnlineBuffs.apply(base(), buff("spell.odd", 60, "exp", 25), NOW);
    assertThrows(IllegalArgumentException.class, () -> OnlineCombatLoadout.resolve(unknown, items, NOW, false));
    assertThrows(IllegalArgumentException.class, () -> OnlineSpellContexts.caster(unknown, items, NOW));
  }

  private static OnlineStore.CharacterState state(CharacterProgress progress) {
    return new OnlineStore.CharacterState(UUID.randomUUID(), UUID.randomUUID(), "Caster", 0, 3, 3, 0, 0, 0, progress);
  }

  @Test void aRecordWrittenBeforeBuffsHadAmountsDecodesWithNoAmountsAndKeepsItsVersion() throws Exception {
    var gson = new Gson();
    var tree = gson.toJsonTree(base()).getAsJsonObject();
    tree.addProperty("version", 1);
    var oldBuff = new com.google.gson.JsonObject();
    oldBuff.addProperty("spell", "spell.heal_light");
    oldBuff.addProperty("expiresAt", 123L);
    oldBuff.addProperty("casterIntelligence", 75);
    oldBuff.addProperty("casterWisdom", 75);
    var buffs = new com.google.gson.JsonArray();
    buffs.add(oldBuff);
    tree.add("buffs", buffs);
    var decoded = gson.fromJson(tree, CharacterProgress.class);
    assertEquals(1, decoded.version());
    var buff = decoded.buffs().get(0);
    assertEquals(75, buff.casterIntelligence());
    assertTrue(buff.modifiers().isEmpty());
    assertEquals(0, buff.appliedAt());
    // Re-encoding keeps it a valid record that decodes to the same thing.
    assertEquals(decoded, gson.fromJson(gson.toJsonTree(decoded), CharacterProgress.class));
    assertThrows(RuntimeException.class, () -> {
      tree.addProperty("version", 3);
      gson.fromJson(tree, CharacterProgress.class);
    }, "versions from the future are still refused");
  }

  @Test void foldedBuffsMatchWhatTheStoredAmountsSay() throws Exception {
    var items = ItemCatalog.loadBundled();
    var plain = base();
    var before = OnlineCombatLoadout.resolve(plain, items, NOW, false);
    var buffed = OnlineBuffs.apply(OnlineBuffs.apply(plain, buff("spell.stone_skin", 1200, "armorClass", 17), NOW),
        buff("spell.nimbleness", 1200, "dex", 11, "skill:dodge", 9), NOW);
    var after = OnlineCombatLoadout.resolve(buffed, items, NOW, false);
    assertEquals(before.profile().armorClass() + 17, after.profile().armorClass(), 1e-9);
    assertEquals(before.profile().agility() + 11, after.profile().agility());
    assertEquals(before.profile().dodge() + 9, after.profile().dodge());
    assertEquals(before.stats().strength(), after.stats().strength());
    // The same character after the buffs ended is the character before them.
    var ended = OnlineCombatLoadout.resolve(buffed, items, NOW + 1_200_000, false);
    assertEquals(before.profile(), ended.profile());
    assertEquals(before.stats(), ended.stats());
    // Strength carries more: the carry limit reads the buffed value.
    var strong = OnlineBuffs.apply(plain, buff("spell.earthen_strength", 1200, "str", 12), NOW);
    var content = ItemCatalog.loadBundled();
    assertEquals(OnlinePickupRules.effectiveStrength(plain, content, NOW) + 12, OnlinePickupRules.effectiveStrength(strong, content, NOW));
    assertEquals(OnlinePickupRules.effectiveStrength(plain, content, NOW), OnlinePickupRules.effectiveStrength(strong, content, NOW + 1_200_000));
  }

  @Test void trueReadingsNeverIncludeBuffsSoARecastRefreshesInsteadOfCompounding() throws Exception {
    var items = ItemCatalog.loadBundled();
    var plain = base();
    var buffed = OnlineBuffs.apply(plain, buff("spell.tranquility", 1200, "wis", 40, "power:fire", 33), NOW);
    var before = OnlineSpellContexts.caster(plain, items, NOW);
    var after = OnlineSpellContexts.caster(buffed, items, NOW);
    assertEquals(before.effective().wisdom() + 40, after.effective().wisdom());
    assertEquals(before.trueStats(), after.trueStats());
    assertEquals(before.power().fire() + 33, after.power().fire());
    assertEquals(before.truePower(), after.truePower());
    assertEquals(before.trueResistance(), after.trueResistance());
    var resisted = OnlineBuffs.apply(plain, buff("spell.resist_fire", 60, "resist:fire", 100), NOW);
    assertEquals(OnlineSpellContexts.effectiveResistance(plain, items, NOW).fire() + 100,
        OnlineSpellContexts.effectiveResistance(resisted, items, NOW).fire());
    assertEquals(before.trueResistance(), OnlineSpellContexts.caster(resisted, items, NOW).trueResistance());
  }

  @Test void aBuffCastCommitsOneDraftThatIsCoveredByTheClockAndRefreshesOnARecast() throws Exception {
    var items = ItemCatalog.loadBundled();
    var catalog = new SpellCatalogV2(SpellEngineGoldenTest.content(), 2);
    var p = ProgressEdits.withSpells(base(), Set.of("spell.protection", "spell.bless"));
    var map = new PreviewMap(new boolean[30][60], 0, 0, 3, 3);
    var first = OnlineSpellStrikes.plan(state(p), "spell.protection", new SpellEngine.Target.Self(), NOW, new Random(1),
        items, catalog, map, 5.5, 5.5, 0, null);
    assertEquals(9, first.manaSpent());
    assertEquals(List.of("spell.protection"), first.buffs().stream().map(OnlineSpellStrikes.Laid::spell).toList());
    assertEquals(NOW + 1_200_000, first.buffs().get(0).expiresAt());
    assertEquals(CharacterProgress.CURRENT_VERSION, first.progress().version());
    assertTrue(first.progress().cooldowns().containsKey(OnlineBuffs.ANCHOR), "a buffed character is anchored");
    var recast = OnlineSpellStrikes.plan(state(first.progress()), "spell.protection", new SpellEngine.Target.Self(),
        NOW + 400_000, new Random(1), items, catalog, map, 5.5, 5.5, 0, null);
    assertEquals(1, recast.progress().buffs().size());
    assertEquals(NOW + 1_600_000, recast.progress().buffs().get(0).expiresAt());
    // Expired buffs are taken off lazily at the next use.
    var late = OnlineCasting.aliveProgress(state(first.progress()), NOW + 1_200_000);
    assertTrue(late.buffs().isEmpty());
  }

  @Test void aFriendlyBuffUsesTheCastersStatsAndTheRecipientsElementsAndOnlyBuffsMayBeLaidOnAnother() throws Exception {
    var items = ItemCatalog.loadBundled();
    var content = SpellEngineGoldenTest.content();
    var caster = OnlineSpellContexts.caster(base(), items, NOW);
    var recipientProgress = OnlineSpellContextParityTest.character("plain", items);
    var recipient = OnlineSpellContexts.caster(recipientProgress, items, NOW);
    var world = new SpellEngineGoldenTest.Fixture() {
      @Override public SpellEngine.FriendView friend(long id) {
        return id == 7 ? new SpellEngine.FriendView(7, 9, 10, true, recipient) : id == 8 ? new SpellEngine.FriendView(8, 9, 10, false, recipient) : null;
      }
    };
    java.util.function.Function<String, SpellEngine.CastRequest> request = key -> new SpellEngine.CastRequest(
        content.spells().get(key), caster, 7, 10, new SpellEngine.Target.Friend(7), true, 10_000, false, false, false);
    var barrier = SpellEngine.cast(request.apply("spell.barrier"), world, new Random(1));
    assertTrue(barrier.success());
    var laid = (SpellEngine.Event.ApplyBuff) barrier.events().get(0);
    assertEquals(7L, laid.recipient());
    assertEquals("armorClass", laid.modifiers().get(0).attribute());
    // Mana Surge reads the recipient's own element powers (target.true_*), not the caster's.
    var surge = (SpellEngine.Event.ApplyBuff) SpellEngine.cast(request.apply("spell.mana_surge"), world, new Random(1)).events().get(0);
    var expected = SpellEngine.resolveBuff(content.spells().get("spell.mana_surge"),
        SpellEngine.friendContext(caster, recipient));
    assertEquals(expected.modifiers(), surge.modifiers());
    var selfSurge = SpellEngine.resolveBuff(content.spells().get("spell.mana_surge"), SpellFormulaContexts.forSelf(caster));
    assertNotEquals(selfSurge.modifiers(), surge.modifiers(), "the recipient's elements are the ones read");
    // Spells limited to the caster, flags and attacks are never laid on someone else; the shared rules say why.
    assertEquals(SpellCastRules.Verdict.WRONG_TARGET, SpellEngine.cast(request.apply("spell.clear_thought"), world, new Random(1)).verdict());
    assertEquals(SpellCastRules.Verdict.WRONG_TARGET, SpellEngine.cast(request.apply("spell.invisibility"), world, new Random(1)).verdict());
    var friendHeal = SpellEngine.cast(request.apply("spell.heal_light"), world, new Random(1));
    assertTrue(friendHeal.success());
    var heal = (SpellEngine.Event.FriendHeal) friendHeal.events().get(0);
    assertEquals(7L, heal.recipient());
    assertTrue(heal.amount() > 0);
    assertEquals("friend_unavailable", SpellEngine.cast(new SpellEngine.CastRequest(content.spells().get("spell.barrier"), caster,
        7, 10, new SpellEngine.Target.Friend(8), true, 10_000, false, false, false), world, new Random(1)).refusal());
    assertEquals("friend_unavailable", SpellEngine.cast(new SpellEngine.CastRequest(content.spells().get("spell.barrier"), caster,
        7, 10, new SpellEngine.Target.Friend(99), true, 10_000, false, false, false), world, new Random(1)).refusal());
    var far = new SpellEngine.CastRequest(content.spells().get("spell.barrier"), caster, -20, 10,
        new SpellEngine.Target.Friend(7), true, 10_000, false, false, false);
    assertEquals(SpellCastRules.Verdict.OUT_OF_RANGE, SpellEngine.cast(far, world, new Random(1)).verdict());
    world.sight = false;
    assertEquals(SpellCastRules.Verdict.NO_LINE_OF_SIGHT, SpellEngine.cast(request.apply("spell.barrier"), world, new Random(1)).verdict());
  }

  @Test void theStageGateOpensStageOneTwoThreeAndOnlyWordOfRecallFromStageFour() throws Exception {
    var catalog = SpellCatalogV2.bundled();
    assertEquals(48 + 34 + 42 + 1, catalog.castableCount());
    assertTrue(catalog.castableKey("spell.fireball") && catalog.castableKey("spell.poison") && catalog.castableKey("spell.earthquake"));
    assertTrue(catalog.castableKey("spell.barrier") && catalog.castableKey("spell.dispel") && catalog.castableKey("spell.curse"));
    assertTrue(catalog.castableKey("spell.renew_armor") && catalog.castableKey("spell.true_sight"));
    assertTrue(catalog.castableKey("spell.word_of_recall"));
    assertFalse(catalog.castableKey("spell.entangle") || catalog.castableKey("spell.tame_beast"));
    assertEquals(SpellContentV2.Kind.BUFF, catalog.castable("spell.light").kind());
  }
}
