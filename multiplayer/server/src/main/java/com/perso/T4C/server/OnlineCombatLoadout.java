package com.perso.T4C.server;

import com.perso.T4C.combat.CombatProfile;
import com.perso.T4C.combat.SkillBonusRules;
import com.perso.T4C.combat.WeaponDamageRules;
import com.perso.T4C.combat.WeaponTimingRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.player.BodyPart;

/** Derived committed loadout. Active spells/world wards must be integrated before supporting them. */
record OnlineCombatLoadout(CombatProfile profile, WeaponDamageRules.Stats stats,
    WeaponDamageRules.Weapon primary, WeaponDamageRules.Weapon secondary, int damageBonus,
    long attackCooldownMillis) {
  static OnlineCombatLoadout resolve(CharacterProgress progress, ItemContent content, long now, boolean stunned) {
    if (now < 0 || progress == null) throw new IllegalArgumentException("Missing authoritative combat state");
    if (progress.buffs().stream().anyMatch(buff -> buff.expiresAt() > now)
        || progress.questFlags().entrySet().stream().anyMatch(flag -> flag.getKey().startsWith("worldevent.") && flag.getValue() != 0))
      throw new IllegalArgumentException("Active spell/world-event combat effects are not integrated");
    var equipment = OnlineEquipmentBonuses.equipment(progress, content);
    var bonuses = OnlineEquipmentBonuses.resolve(progress, content);
    var base = progress.attributes();
    var stats = new WeaponDamageRules.Stats(base.strength() + bonuses.getOrDefault(3, 0),
        base.endurance() + bonuses.getOrDefault(2, 0), base.dexterity() + bonuses.getOrDefault(6, 0),
        base.intelligence() + bonuses.getOrDefault(1, 0), base.wisdom() + bonuses.getOrDefault(4, 0),
        progress.advancement().level());
    java.util.function.ToIntFunction<String> skill = name -> SkillBonusRules.effective(name,
        progress.skills().getOrDefault(name, 0), 0, stat -> bonuses.getOrDefault(stat, 0));
    double armor = equipment.values().stream().mapToDouble(item -> Math.max(0, item.armorClass())).sum();
    long penalty = 0;
    for (var item : equipment.values()) penalty = Math.min(Integer.MAX_VALUE,
        penalty + Math.min(Integer.MAX_VALUE, Math.max(0L, item.dodgeLost())));
    var profile = new CombatProfile(stats.level(), stats.strength(), stats.endurance(), stats.dexterity(),
        Math.max(1, skill.applyAsInt("attack")), Math.max(1, skill.applyAsInt("archery")),
        Math.max(1, Math.max(1, skill.applyAsInt("dodge")) - (int) penalty),
        Math.max(0, armor + bonuses.getOrDefault(20, 0)), stunned, progress.hiddenUntil() > now,
        equipment.containsKey(BodyPart.WEAPON), progress.skills());
    var main = equipment.get(BodyPart.WEAPON);
    var off = equipment.get(BodyPart.WEAPON2);
    // SHIELD shares occupancy constraints, not the offline WEAPON2 damage input.
    // Secondary visual layers of a two-handed item are not a second owned weapon.
    double speed = main == null ? 1.0 : WeaponTimingRules.resolve(main.atkDelay(), main.attackSpeed()).attacksPerSecond();
    return new OnlineCombatLoadout(profile, stats, main == null ? null : main.weapon(),
        off == null ? null : off.weapon(), bonuses.getOrDefault(10, 0), WeaponTimingRules.cooldownMillis(speed));
  }
}
