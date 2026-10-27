package com.perso.T4C.server;

import com.perso.T4C.combat.EquipmentEligibilityRules;
import com.perso.T4C.combat.WeaponDamageRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.player.BodyPart;
import java.util.*;

/** Pure transition from committed server state; returned progress must commit before publication. */
final class OnlineEquipmentCommands {
  private OnlineEquipmentCommands() {}

  static CharacterProgress equip(CharacterProgress progress, ItemContent catalog, UUID instance, BodyPart slot) {
    requireProgress(progress);
    var item = progress.inventory().stream().filter(i -> i.id().equals(instance)).findFirst()
        .orElseThrow(() -> rejected("item_not_owned"));
    var definition = definition(catalog, item.key());
    if (item.quantity() != 1 || progress.equipment().containsValue(instance)) throw rejected("equipment_conflict");
    if (!EquipmentEligibilityRules.slotMatches(slot, definition.bodyPart())) throw rejected("wrong_slot");
    var attrs = progress.attributes();
    if (!EquipmentEligibilityRules.meets(new WeaponDamageRules.Stats(attrs.strength(), attrs.endurance(),
        attrs.dexterity(), attrs.intelligence(), attrs.wisdom(), progress.advancement().level()),
        progress.skills().getOrDefault("attack", 0), new EquipmentEligibilityRules.Requirements(
            definition.minEnd(), definition.reqStr(), definition.reqAgi(), definition.minInt(), definition.minWis(), definition.reqAttack())))
      throw rejected("requirements");

    var equipped = resolve(progress, catalog);
    var main = equipped.get(BodyPart.WEAPON);
    var off = equipped.get(BodyPart.WEAPON2);
    if (off == null) off = equipped.get(BodyPart.SHIELD);
    if ((EquipmentEligibilityRules.isQuiver(definition.key()) && main != null && !main.isBow())
        || (definition.bodyPart() == BodyPart.WEAPON && !definition.isBow()
            && off != null && EquipmentEligibilityRules.isQuiver(off.key()))) throw rejected("equipment_conflict");

    var occupied = occupied(slot, definition);
    var next = new HashMap<>(progress.equipment());
    equipped.forEach((oldSlot, oldItem) -> {
      if (!Collections.disjoint(occupied, occupied(oldSlot, oldItem))) next.remove(oldSlot.name());
    });
    // Online inventory retains all owned instances. Displacement changes references, never grants items.
    next.put(slot.name(), instance);
    return withEquipment(progress, next);
  }

  static CharacterProgress unequip(CharacterProgress progress, BodyPart slot) {
    requireProgress(progress);
    var next = new HashMap<>(progress.equipment());
    if (next.remove(slot.name()) == null) throw rejected("item_not_equipped");
    return withEquipment(progress, next);
  }

  private static Map<BodyPart, ItemContent.Definition> resolve(CharacterProgress progress, ItemContent catalog) {
    var owned = new HashMap<UUID, String>();
    progress.inventory().forEach(item -> owned.put(item.id(), item.key()));
    var result = new EnumMap<BodyPart, ItemContent.Definition>(BodyPart.class);
    progress.equipment().forEach((slot, id) -> {
      BodyPart part;
      try { part = BodyPart.valueOf(slot); }
      catch (IllegalArgumentException error) { throw rejected("wrong_slot"); }
      result.put(part, definition(catalog, owned.get(id)));
    });
    return result;
  }

  private static Set<BodyPart> occupied(BodyPart slot, ItemContent.Definition definition) {
    var slots = EnumSet.of(slot);
    if (definition.secondaryBodyPart() != null) slots.add(definition.secondaryBodyPart());
    if (slots.contains(BodyPart.WEAPON2) || slots.contains(BodyPart.SHIELD)) {
      slots.add(BodyPart.WEAPON2); slots.add(BodyPart.SHIELD);
    }
    return slots;
  }

  private static ItemContent.Definition definition(ItemContent content, String key) {
    var item = key == null ? null : content.items().get(key);
    if (item == null) throw rejected("unknown_item");
    return item;
  }

  private static void requireProgress(CharacterProgress progress) {
    if (progress == null) throw rejected("uninitialized_character");
  }

  private static PreviewWorld.Rejected rejected(String reason) { return new PreviewWorld.Rejected(409, reason); }

  private static CharacterProgress withEquipment(CharacterProgress p, Map<String, UUID> equipment) {
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(), p.vitals(),
        p.advancement(), p.inventory(), equipment, p.skills(), p.spells(), p.questFlags(), p.storage(),
        p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(), p.hiddenUntil(), p.companion());
  }
}
