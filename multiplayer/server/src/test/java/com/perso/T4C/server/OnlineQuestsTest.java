package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRules;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineQuestsTest {
  private static final String FLAG = "quest.renewed_wards.status";
  private static final String KILLS = "quest.renewed_wards.kills";

  @Test void acceptingOnceStartsTheQuestAndARepeatedOfferOnlyReportsProgress() throws Exception {
    var definition = QuestCatalog.loadBundled().definition("renewed_wards");
    var fresh = progress(1, 10, List.of(), Map.of(), Map.of(), Set.of());
    var accepted = OnlineQuests.topic(fresh, definition, 0);
    assertTrue(accepted.changed());
    assertEquals(OnlineQuests.messageKey("${quest.renewed_wards.offer}"), accepted.messageKey());
    assertEquals(1, accepted.progress().questFlags().get(FLAG));
    assertEquals(0, accepted.progress().questFlags().get(KILLS));
    var again = OnlineQuests.topic(accepted.progress(), definition, 0);
    assertFalse(again.changed());
    assertEquals(OnlineQuests.PROGRESS_KEY, again.messageKey());
    assertEquals(List.of(0, 12, 12), again.args());
  }

  @Test void elevenOfTwelveKillsReportsProgressAndGreetingNeverTurnsIn() throws Exception {
    var definition = QuestCatalog.loadBundled().definition("renewed_wards");
    var p = progress(1, 10, List.of(), Map.of(), Map.of(FLAG, 1, KILLS, 11), Set.of());
    var topic = OnlineQuests.topic(p, definition, 0);
    assertFalse(topic.changed());
    assertEquals(List.of(11, 12, 1), topic.args());
    var greeting = OnlineQuests.greet(state(p, 0), definition, ItemCatalog.loadBundled(), UUID::randomUUID, 100, new Random(1));
    assertFalse(greeting.changed());
    assertEquals(0, greeting.gold());
    assertEquals("npc.welcome.wardenaelric", greeting.messageKey());
    assertEquals(p, greeting.progress());
  }

  @Test void twelveOfTwelveTurnsInExactlyOnceWithRewardGoldExperienceAndSpell() throws Exception {
    var catalog = QuestCatalog.loadBundled();
    var definition = catalog.definition("renewed_wards");
    var items = ItemCatalog.loadBundled();
    var p = progress(1, 10, List.of(), Map.of(), Map.of(FLAG, 1, KILLS, 12), Set.of());
    var saved = state(p, 0);
    var turnedIn = OnlineQuests.greet(saved, definition, items, UUID::randomUUID, 100, new Random(1));
    assertTrue(turnedIn.changed());
    assertEquals(500, turnedIn.gold());
    assertEquals(400, turnedIn.experience());
    assertEquals("quest.renewed_wards.completion", turnedIn.messageKey());
    assertEquals(2, turnedIn.progress().questFlags().get(FLAG));
    assertTrue(turnedIn.progress().spells().contains("spell.renew_armor"));

    // The committed state after the first turn-in: a second greeting must not pay out again.
    var afterCommit = state(turnedIn.progress(), 400);
    var repeat = OnlineQuests.greet(afterCommit, definition, items, UUID::randomUUID, 100, new Random(1));
    assertFalse(repeat.changed());
    assertEquals(0, repeat.gold());
    assertEquals(0, repeat.experience());
    assertEquals(turnedIn.progress(), repeat.progress());
  }

  @Test void requiredItemTurnInConsumesOnlyTheRequiredQuantityAndRefusesWornCopies() throws Exception {
    var definition = fetchQuest("item.ambrosia", 2, null);
    var items = ItemCatalog.loadBundled();
    var stack = new CharacterProgress.Item(UUID.randomUUID(), "item.ambrosia", 3, 1);
    var p = progress(1, 10, List.of(stack), Map.of(), activeFlags(definition, 1), Set.of());
    var turnedIn = OnlineQuests.greet(state(p, 0), definition, items, UUID::randomUUID, 100, new Random(1));
    assertTrue(turnedIn.changed());
    var leftover = turnedIn.progress().inventory();
    assertEquals(1, leftover.size());
    assertEquals(stack.id(), leftover.getFirst().id(), "A split stack keeps its identity");
    assertEquals(1, leftover.getFirst().quantity());

    var worn = new CharacterProgress.Item(UUID.randomUUID(), "item.ambrosia", 2, 1);
    var wornOnly = progress(1, 10, List.of(worn), Map.of("weapon", worn.id()), activeFlags(definition, 1), Set.of());
    var refused = OnlineQuests.greet(state(wornOnly, 0), definition, items, UUID::randomUUID, 100, new Random(1));
    assertFalse(refused.changed());
    assertEquals(wornOnly.inventory(), refused.progress().inventory(), "Equipped copies are never consumed");
    assertEquals(0, refused.gold());
  }

  @Test void rewardItemThatWouldExceedCarryCapacityBlocksTheWholeTurnIn() throws Exception {
    var definition = fetchQuest(null, 0, "item.ambrosia");
    var items = ItemCatalog.loadBundled();
    // Strength 1 carries about 3 weight; two ambrosia already weigh 4, so one more cannot be admitted.
    var heavy = new CharacterProgress.Item(UUID.randomUUID(), "item.ambrosia", 2, 1);
    var p = progress(1, 1, List.of(heavy), Map.of(), activeFlags(definition, 1), Set.of());
    var blocked = OnlineQuests.greet(state(p, 0), definition, items, UUID::randomUUID, 100, new Random(1));
    assertFalse(blocked.changed());
    assertEquals(OnlineQuests.BLOCKED_KEY, blocked.messageKey());
    assertEquals(p, blocked.progress(), "A refused reward changes nothing");
    assertEquals(0, blocked.experience());
  }

  private static QuestDef fetchQuest(String requiredItemKey, int requiredQuantity, String rewardItemKey) {
    return new QuestDef("fetch_fixture", "t", "WardenAelric", "Skeleton", 1, 0, 0, 0, 0, 50, 10,
        "${quest.fetch_fixture.offer}", "${quest.fetch_fixture.completion}",
        "${quest.fetch_fixture.completed}", null, requiredItemKey, requiredQuantity, null,
        rewardItemKey, 0, null, null);
  }

  /** Active status with the requirement met, keyed by this definition's own flags. */
  private static Map<String, Integer> activeFlags(QuestDef definition, int kills) {
    return Map.of(QuestRules.statusFlag(definition), QuestRules.STATUS_ACTIVE,
        QuestRules.killsFlag(definition), kills);
  }

  private static OnlineStore.CharacterState state(CharacterProgress p, long experience) {
    return new OnlineStore.CharacterState(UUID.randomUUID(), UUID.randomUUID(), "Quester", 0, 3, 3, 0,
        experience, 0, p);
  }

  /** A valid warrior with the given inventory, equipment, flags and strength. */
  private static CharacterProgress progress(int level, int strength, List<CharacterProgress.Item> inventory,
      Map<String, UUID> equipment, Map<String, Integer> flags, Set<String> spells) {
    var base = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3)).progress();
    var attributes = new CharacterProgress.Attributes(strength, base.attributes().endurance(),
        base.attributes().dexterity(), base.attributes().wisdom(), base.attributes().intelligence());
    var advancement = new CharacterProgress.Advancement(level, base.advancement().statPoints(),
        base.advancement().skillPoints(), 0, 0);
    return new CharacterProgress(base.version(), base.characterClass(), base.gender(), attributes,
        base.vitals(), advancement, inventory, equipment, base.skills(), spells, flags, List.of(), 0,
        base.respawn(), List.of(), Map.of(), 0, base.companion());
  }
}
