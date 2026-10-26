package com.perso.T4C.server;

import com.perso.T4C.combat.EquipmentBoostRules;
import com.perso.T4C.combat.WeaponDamageRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.player.BodyPart;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Derives bonuses from committed owned equipment; never call with a client-authored payload. */
final class OnlineEquipmentBonuses {
  private OnlineEquipmentBonuses() {}

  static Map<Integer, Integer> resolve(CharacterProgress progress, ItemContent content) {
    var owned = new HashMap<java.util.UUID, CharacterProgress.Item>();
    progress.inventory().forEach(item -> owned.put(item.id(), item));
    var equipped = new EnumMap<BodyPart, ItemContent.Definition>(BodyPart.class);
    progress.equipment().forEach((slot, id) -> {
      var item = owned.get(id);
      var definition = item == null ? null : content.items().get(item.key());
      if (definition == null) throw new IllegalArgumentException("Unknown owned equipment");
      equipped.put(BodyPart.valueOf(slot), definition);
    });
    var boosts = new ArrayList<EquipmentBoostRules.Boost>();
    // Stable slot order for deterministic recovery. Each online instance occupies one slot;
    // rendered secondary layers are not additional owned equipment or additional boosts.
    equipped.values().forEach(item -> item.boosts().forEach(boost -> boosts.add(
        new EquipmentBoostRules.Boost(boost.boostId(), boost.statId(), boost.expression(), boost.minWis(), boost.minInt()))));
    var attributes = progress.attributes();
    var base = new WeaponDamageRules.Stats(attributes.strength(), attributes.endurance(),
        attributes.dexterity(), attributes.intelligence(), attributes.wisdom(), progress.advancement().level());
    return EquipmentBoostRules.bonuses(base, progress.skills(), boosts);
  }
}
