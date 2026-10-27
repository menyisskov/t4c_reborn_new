package com.perso.T4C.server;

import com.perso.T4C.item.CarryCapacityRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRules;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.random.RandomGenerator;

/**
 * Server-side quest dialogue and turn-in. Each call returns one immutable draft built from committed
 * progress; the caller commits it once and publishes the message only after that commit succeeds.
 */
final class OnlineQuests {
  static final String PROGRESS_KEY = "message.quest_progress_dialog";
  static final String BLOCKED_KEY = "message.quest_reward_item_blocked";

  enum Readiness { NOT_STARTED, COMPLETED, NEEDS_KILLS, NEEDS_ITEM, NEEDS_LEVEL, READY }

  /** Draft to commit (gold and experience are added by the transaction) and the message to show. */
  record Result(CharacterProgress progress, long gold, long experience, String messageKey,
      List<Integer> args, boolean changed) {
    Result {
      Objects.requireNonNull(progress);
      args = List.copyOf(args);
    }
  }

  private OnlineQuests() {}

  static Readiness readiness(CharacterProgress progress, QuestDef definition) {
    Map<String, Integer> flags = progress.questFlags();
    ToIntFunction<String> lookup = key -> flags.getOrDefault(key, 0);
    int status = QuestRules.status(lookup, definition);
    if (status == QuestRules.STATUS_COMPLETED) return Readiness.COMPLETED;
    if (status != QuestRules.STATUS_ACTIVE) return Readiness.NOT_STARTED;
    if (QuestRules.kills(lookup, definition) < definition.getRequiredKills()) return Readiness.NEEDS_KILLS;
    if (definition.getRequiredItemKey() != null
        && countUnequipped(progress, definition.getRequiredItemKey()) < definition.getRequiredItemQty()) {
      return Readiness.NEEDS_ITEM;
    }
    if (progress.advancement().level() < definition.getMinLevel()) return Readiness.NEEDS_LEVEL;
    return Readiness.READY;
  }

  /** Worn gear is never consumed by a turn-in, so only loose instances count toward the requirement. */
  private static int countUnequipped(CharacterProgress progress, String key) {
    var worn = new HashSet<>(progress.equipment().values());
    int total = 0;
    for (var item : progress.inventory()) {
      if (item.key().equals(key) && !worn.contains(item.id())) total += item.quantity();
    }
    return total;
  }

  /** Greeting turns in a ready quest; any other greeting is the giver's welcome, with no change. */
  static Result greet(OnlineStore.CharacterState saved, QuestDef definition, ItemContent items,
      Supplier<UUID> instanceId, long now, RandomGenerator random) {
    if (readiness(saved.progress(), definition) == Readiness.READY) {
      return turnIn(saved, definition, items, instanceId, now, random);
    }
    return reply(saved.progress(), npcKey("welcome", definition, null), List.of(), false);
  }

  /** Topic 0 is the quest itself; topic 1 is the giver's lore line and never changes state. */
  static Result topic(CharacterProgress progress, QuestDef definition, int topic) {
    if (topic == 1) return reply(progress, npcKey("topic", definition, "1"), List.of(), false);
    if (topic != 0) throw new IllegalArgumentException("Unsupported quest topic");
    return switch (readiness(progress, definition)) {
      case NOT_STARTED -> {
        Map<String, Integer> current = progress.questFlags();
        ToIntFunction<String> lookup = key -> current.getOrDefault(key, 0);
        var flags = new HashMap<>(current);
        flags.putAll(QuestRules.accept(lookup, definition));
        yield reply(ProgressEdits.withFlags(progress, flags), messageKey(definition.getOfferText()),
            List.of(), true);
      }
      case COMPLETED -> reply(progress, messageKey(definition.getCompletedText()), List.of(), false);
      default -> progressReply(progress, definition);
    };
  }

  private static Result progressReply(CharacterProgress progress, QuestDef definition) {
    Map<String, Integer> flags = progress.questFlags();
    int kills = QuestRules.kills(key -> flags.getOrDefault(key, 0), definition);
    int required = definition.getRequiredKills();
    return reply(progress, PROGRESS_KEY, List.of(kills, required, required - kills), false);
  }

  /** Atomic turn-in: items, reward item, completion flags, spell and experience land as one draft. */
  static Result turnIn(OnlineStore.CharacterState saved, QuestDef definition, ItemContent items,
      Supplier<UUID> instanceId, long now, RandomGenerator random) {
    var progress = saved.progress();
    if (readiness(progress, definition) != Readiness.READY)
      throw new IllegalArgumentException("Quest is not ready");
    var inventory = consumeRequired(progress, definition);
    if (definition.getRewardItemKey() != null) {
      var reward = items.items().get(definition.getRewardItemKey());
      if (reward == null) throw new IllegalArgumentException("Unresolved quest reward item");
      var afterConsumption = ProgressEdits.withInventory(progress, inventory);
      long maximum = CarryCapacityRules.maximumWeight(OnlinePickupRules.effectiveStrength(afterConsumption, items, now));
      if (!CarryCapacityRules.canAdd(OnlinePickupRules.carriedWeight(afterConsumption, items), reward.weight(), maximum)) {
        // Offline order: a refused reward blocks the whole turn-in and leaves the quest untouched.
        return new Result(progress, 0, 0, BLOCKED_KEY, List.of(), false);
      }
      inventory = new ArrayList<>(inventory);
      inventory.add(new CharacterProgress.Item(instanceId.get(), reward.key(), 1, OnlineLootDrafts.charges(reward)));
    }
    var flags = new HashMap<>(progress.questFlags());
    flags.putAll(QuestRules.completionFlags(definition));
    var spells = new HashSet<>(progress.spells());
    if (definition.getRewardSpellKey() != null) spells.add(definition.getRewardSpellKey());
    var updated = ProgressEdits.withSpells(
        ProgressEdits.withFlags(ProgressEdits.withInventory(progress, inventory), flags), spells);
    var rewarded = new OnlineStore.CharacterState(saved.id(), saved.accountId(), saved.name(), saved.world(),
        saved.x(), saved.y(), saved.gold(), saved.experience(), saved.revision(), updated,
        saved.locationEpoch(), saved.portalCooldownUntil());
    var award = OnlineProgression.award(rewarded, definition.getRewardXp(), now, random);
    return new Result(award.progress(), definition.getRewardGold(), award.experience(),
        messageKey(definition.getCompletionText()), List.of(), true);
  }

  /** Oldest instances first, splitting a stack's quantity rather than destroying its identity. */
  private static List<CharacterProgress.Item> consumeRequired(CharacterProgress progress, QuestDef definition) {
    var worn = new HashSet<>(progress.equipment().values());
    int remaining = definition.getRequiredItemQty();
    var kept = new ArrayList<CharacterProgress.Item>();
    for (var item : progress.inventory()) {
      boolean eligible = remaining > 0 && item.key().equals(definition.getRequiredItemKey()) && !worn.contains(item.id());
      if (!eligible) {
        kept.add(item);
        continue;
      }
      int take = Math.min(remaining, item.quantity());
      remaining -= take;
      if (take < item.quantity()) {
        kept.add(new CharacterProgress.Item(item.id(), item.key(), item.quantity() - take, item.charges()));
      }
    }
    return kept;
  }

  private static Result reply(CharacterProgress progress, String key, List<Integer> args, boolean changed) {
    return new Result(progress, 0, 0, key, args, changed);
  }

  /** Authored text references look like ${quest.id.offer}; the reply carries the bare key. */
  static String messageKey(String textReference) {
    if (textReference.startsWith("${") && textReference.endsWith("}")) {
      return textReference.substring(2, textReference.length() - 1);
    }
    return textReference;
  }

  private static String npcKey(String kind, QuestDef definition, String suffix) {
    String giver = definition.getGiverNpc().toLowerCase(Locale.ROOT);
    return suffix == null ? "npc." + kind + "." + giver : "npc." + kind + "." + giver + "." + suffix;
  }
}
