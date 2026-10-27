package com.perso.T4C.server;

import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * Server-side consumable drafts. The named instance must be a loose inventory copy of this
 * character; the draft applies the item's spell, then spends one charge of that instance.
 */
final class OnlineItemUse {
  private OnlineItemUse() {}

  static OnlineCasting.Result prepareUse(OnlineStore.CharacterState saved, UUID instanceId, long now,
      RandomGenerator random, ItemContent items, SpellCatalog spells) {
    var progress = OnlineCasting.aliveProgress(saved, now);
    var instance = progress.inventory().stream().filter(item -> item.id().equals(instanceId))
        .findFirst().orElseThrow(() -> OnlineCasting.rejected("item_not_owned"));
    if (progress.equipment().containsValue(instanceId)) throw OnlineCasting.rejected("item_equipped");
    var definition = items.items().get(instance.key());
    if (definition == null || definition.nbCharges() > 1) throw OnlineCasting.rejected("unsupported_item");
    // Per-instance charges are only proven for one charge or unlimited use; anything else fails closed.
    if (!definition.unlimitedUse() && instance.charges() != 1) throw OnlineCasting.rejected("unsupported_item");
    if (definition.spells().isEmpty()) throw OnlineCasting.rejected("no_effect");
    var selected = selectSpell(definition.spells(), random);
    if (selected == null) throw OnlineCasting.rejected("chance_failed");
    var spell = itemSpell(spells, selected.spellId());
    var effects = OnlineCasting.effects(spell, progress, items, random);
    if (effects.healthDelta() > 0 && progress.vitals().hp() >= progress.vitals().maxHp())
      throw OnlineCasting.rejected("no_healing_needed");
    if (effects.manaDelta() > 0 && progress.vitals().mana() >= progress.vitals().maxMana())
      throw OnlineCasting.rejected("no_mana_needed");
    var outcome = OnlineCasting.outcome(progress, spell, false, now, random, items);
    var applied = OnlineCasting.apply(progress, spell, effects, outcome, now);
    var inventory = spend(progress.inventory(), instance, definition.unlimitedUse());
    return new OnlineCasting.Result(ProgressEdits.withInventory(applied.progress(), inventory),
        applied.healthGained(), applied.manaSpent(), applied.manaGained());
  }

  /** Authored order: the first copy whose chance roll succeeds supplies the spell. */
  private static ItemContent.Spell selectSpell(List<ItemContent.Spell> candidates, RandomGenerator random) {
    for (var candidate : candidates) {
      int chance = candidate.chance() <= 0 ? 100 : Math.min(100, candidate.chance());
      if (random.nextInt(100) < chance) return candidate;
    }
    return null;
  }

  private static SpellContent.Spell itemSpell(SpellCatalog spells, int spellId) {
    var spell = spells.spells().values().stream().filter(candidate -> candidate.id() == spellId)
        .findFirst().orElseThrow(() -> OnlineCasting.rejected("no_effect"));
    if (spell.use() != SpellContent.Use.ITEM) throw OnlineCasting.rejected("unsupported_spell");
    return spell;
  }

  /** One unit leaves a stack; the last unit removes the instance. Unlimited items keep their copy. */
  private static List<CharacterProgress.Item> spend(List<CharacterProgress.Item> inventory,
      CharacterProgress.Item instance, boolean unlimited) {
    if (unlimited) return inventory;
    var next = new ArrayList<CharacterProgress.Item>(inventory.size());
    for (var item : inventory) {
      if (!item.id().equals(instance.id())) next.add(item);
      else if (item.quantity() > 1) next.add(new CharacterProgress.Item(item.id(), item.key(), item.quantity() - 1, item.charges()));
    }
    return next;
  }
}
