package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.player.BodyPart;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OnlineRegenerationTest {
  private static final long T0 = 100_000L;
  private static final Map<Integer, Integer> NONE = Map.of();

  @Test void partialTicksDoNothingAndKeepTheirRemainder() {
    var p = progress(new CharacterProgress.Vitals(10, 100, 5, 50), T0);
    var same = OnlineRegeneration.settle(p, T0 + OnlineRegeneration.TICK_MILLIS - 1, new Random(1), NONE);
    assertSame(p, same);
    // Two and a half ticks settle two and leave the half tick on the anchor.
    var later = OnlineRegeneration.settle(p, T0 + 5_000, new Random(1), NONE);
    assertEquals(T0 + 2 * OnlineRegeneration.TICK_MILLIS, later.cooldowns().get(OnlineRegeneration.ANCHOR));
    int perTickMax = p.attributes().endurance() / 40 + 1;
    assertTrue(later.vitals().hp() >= 10 && later.vitals().hp() <= 10 + 2 * perTickMax);
  }

  @Test void regenerationNeverExceedsMaximumAndStopsAtFull() {
    var p = progress(new CharacterProgress.Vitals(98, 100, 49, 50), T0);
    var settled = OnlineRegeneration.settle(p, T0 + 600_000, new Random(2), NONE);
    assertEquals(100, settled.vitals().hp());
    assertEquals(50, settled.vitals().mana());
    assertEquals(T0 + 600_000, settled.cooldowns().get(OnlineRegeneration.ANCHOR));
  }

  @Test void deadCharactersDoNotRegenerateAndReanchorAtNow() {
    var p = progress(new CharacterProgress.Vitals(0, 100, 0, 50), T0);
    var settled = OnlineRegeneration.settle(p, T0 + 60_000, new Random(3), NONE);
    assertEquals(0, settled.vitals().hp());
    assertEquals(0, settled.vitals().mana());
    assertEquals(T0 + 60_000, settled.cooldowns().get(OnlineRegeneration.ANCHOR));
  }

  @Test void hugeGapIsBoundedToOneHourOfTicksAndReanchorsAtNow() {
    var p = progress(new CharacterProgress.Vitals(1, 1_000_000, 0, 1_000_000), T0);
    long now = T0 + 10L * 24 * 3_600_000;
    var settled = OnlineRegeneration.settle(p, now, new Random(4), NONE);
    int perTickMax = p.attributes().endurance() / 40 + 1;
    assertTrue(settled.vitals().hp() <= 1 + OnlineRegeneration.MAX_TICKS * perTickMax);
    assertTrue(settled.vitals().hp() > 1);
    assertEquals(now, settled.cooldowns().get(OnlineRegeneration.ANCHOR));
  }

  @Test void missingOrFutureAnchorReanchorsWithoutRegenerating() {
    var vitals = new CharacterProgress.Vitals(10, 100, 5, 50);
    var none = OnlineRegeneration.settle(progress(vitals, -1), T0, new Random(5), NONE);
    assertEquals(10, none.vitals().hp());
    assertEquals(T0, none.cooldowns().get(OnlineRegeneration.ANCHOR));
    var future = OnlineRegeneration.settle(progress(vitals, T0 + 50_000), T0, new Random(5), NONE);
    assertEquals(10, future.vitals().hp());
    assertEquals(T0, future.cooldowns().get(OnlineRegeneration.ANCHOR));
  }

  @Test void settlingTwiceAtTheSameInstantAddsNothing() {
    var p = progress(new CharacterProgress.Vitals(10, 100, 5, 50), T0);
    var once = OnlineRegeneration.settle(p, T0 + 20_000, new Random(6), NONE);
    var twice = OnlineRegeneration.settle(once, T0 + 20_000, new Random(7), NONE);
    assertEquals(once.vitals(), twice.vitals());
  }

  @Test void activeBuffsAndEquippedBoostsContributeAtSettleTime() {
    var base = progress(new CharacterProgress.Vitals(1, 100, 1, 50), T0);
    var itemId = java.util.UUID.randomUUID();
    var equipped = new CharacterProgress(base.version(), base.characterClass(), base.gender(), base.attributes(),
        base.vitals(), base.advancement(), List.of(new CharacterProgress.Item(itemId, "item.regen", 1, 0)),
        Map.of("RING1", itemId), Map.of("rapid_healing", 0), base.spells(), base.questFlags(), base.storage(),
        base.storageGold(), base.respawn(), List.of(
            new CharacterProgress.Buff("spell.regen", T0 + OnlineRegeneration.TICK_MILLIS + 1,
                0, 0, Map.of("end", 160, "int", 160, "wis", 160, "skill:rapid_healing", 24), T0),
            new CharacterProgress.Buff("spell.expired", T0, 0, 0,
                Map.of("end", 800, "int", 800, "wis", 800, "skill:rapid_healing", 800), T0)),
        base.cooldowns(), base.hiddenUntil(), base.companion());
    var definition = new ItemContent.Definition("item.regen", "Regeneration ring", BodyPart.RING1, null,
        null, null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, false, false,
        false, 1, 0, "0", "0", 0, false,
        List.of(new ItemContent.Boost(1, 2, "40", 0, 0),
            new ItemContent.Boost(2, 1, "160", 0, 0), new ItemContent.Boost(3, 4, "160", 0, 0),
            new ItemContent.Boost(4, 10029, "24", 0, 0)), List.of(), false, null);
    var catalog = new ItemContent(ItemContent.VERSION, Map.of("item.regen", definition));

    var afterTick = OnlineRegeneration.settle(equipped, T0 + OnlineRegeneration.TICK_MILLIS,
        new Random(0) {
          @Override public int nextInt(int origin, int bound) { return origin == 1 ? 1 : origin; }
        }, catalog);
    assertTrue(afterTick.vitals().hp() > 1, "Active endurance buff applies");
    assertTrue(afterTick.vitals().mana() > 1, "Active intelligence and wisdom buffs apply");
    assertEquals(3, afterTick.vitals().hp(), "Combined skill buff and equipped boost double the one point base regen");
  }

  private static CharacterProgress progress(CharacterProgress.Vitals vitals, long anchor) {
    var base = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3)).progress();
    Map<String, Long> cooldowns = anchor < 0 ? Map.of() : Map.of(OnlineRegeneration.ANCHOR, anchor);
    return new CharacterProgress(base.version(), base.characterClass(), base.gender(), base.attributes(),
        vitals, base.advancement(), List.of(), Map.of(), base.skills(), Set.of(), Map.of(), List.of(), 0,
        base.respawn(), List.of(), cooldowns, 0, base.companion());
  }
}
