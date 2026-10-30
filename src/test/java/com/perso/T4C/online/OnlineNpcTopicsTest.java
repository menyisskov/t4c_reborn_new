package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.i18n.I18n;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class OnlineNpcTopicsTest {
  @Test
  void topicKeysUseTheLowercaseAuthoredNpcIdentity() {
    assertEquals("npc.topic.aaronbrownbark.1", OnlineNpcTopics.key("AaronBrownbark", 1));
  }

  @Test
  void availableTopicsAreContiguousFromZeroAndStopAtTheFirstGap() {
    Set<String> catalogue = Set.of("npc.topic.guard.0", "npc.topic.guard.1", "npc.topic.guard.3");
    assertEquals(List.of(0, 1), OnlineNpcTopics.available("Guard", catalogue::contains));
  }

  @Test
  void availableTopicsAreCappedAtTheNumberKeys() {
    Predicate<String> everything = key -> true;
    assertEquals(OnlineNpcTopics.MAX_CHOICES, OnlineNpcTopics.available("Guard", everything).size());
  }

  @Test
  void lighthavenSamaritanExposesOnlyItsAuthoredErrandTopicAndPreservesItsActualIndex() {
    Set<String> catalogue = Set.of(
        "npc.topic.lighthavensamaritan.0", "npc.topic.lighthavensamaritan.37",
        "npc.topic.lighthavensamaritan.38");
    var topics = OnlineNpcTopics.available("LighthavenSamaritan", catalogue::contains);
    assertEquals(List.of(37), topics);
    assertEquals(OptionalInt.of(37), OnlineNpcTopics.choose(topics, 1));
    assertTrue(OnlineNpcTopics.choose(topics, 2).isEmpty());
  }

  @Test
  void digitsChooseOnlyListedTopics() {
    var topics = List.of(0, 1);
    assertEquals(OptionalInt.of(0), OnlineNpcTopics.choose(topics, 1));
    assertEquals(OptionalInt.of(1), OnlineNpcTopics.choose(topics, 2));
    assertTrue(OnlineNpcTopics.choose(topics, 3).isEmpty());
    assertTrue(OnlineNpcTopics.choose(topics, 0).isEmpty());
  }

  @Test
  void shippedCatalogueOffersTopicsForAnAuthoredGiver() {
    assertTrue(OnlineNpcTopics.available("AaronBrownbark", I18n::has).contains(0));
  }
}
