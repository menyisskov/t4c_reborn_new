package com.perso.T4C.server;

import com.perso.T4C.online.model.CharacterProgress;
import java.util.HashMap;
import java.util.random.RandomGenerator;

/** Server-side authored Sneak activation and moving concealment upkeep. */
final class OnlineSneak {
  static final long COOLDOWN_MILLIS = 1_000L;
  static final int WITNESS_RANGE = 40;
  private OnlineSneak() {}

  static CharacterProgress activate(CharacterProgress p, long now) {
    if(p==null || p.skills().getOrDefault("sneak",0)<=0) throw new IllegalArgumentException("not_learned");
    if(p.vitals().hp()<=0) throw new IllegalArgumentException("dead");
    if(p.cooldowns().getOrDefault("stun",0L)>now) throw new IllegalArgumentException("stunned");
    if(p.cooldowns().getOrDefault("sneak",0L)>now) throw new IllegalArgumentException("cooldown");
    var cooldowns = new HashMap<>(p.cooldowns());
    cooldowns.put("sneak", now + COOLDOWN_MILLIS);
    var hidden = ProgressEdits.withHiddenUntil(p, Long.MAX_VALUE);
    return ProgressEdits.withCooldowns(hidden, cooldowns);
  }

  static boolean remainsHidden(CharacterProgress p,com.perso.T4C.online.model.ItemContent items,
      long now,int witnesses,RandomGenerator random) {
    if(p==null || p.hiddenUntil()!=Long.MAX_VALUE) return false;
    var buffs=OnlineBuffs.totals(p,now);
    var equipment=OnlineEquipmentBonuses.resolve(p,items);
    int sneak=com.perso.T4C.combat.SkillBonusRules.effective("sneak",p.skills().getOrDefault("sneak",0),
        buffs.getOrDefault("skill:sneak",0),stat -> equipment.getOrDefault(stat,0));
    int dexterity=p.attributes().dexterity()+buffs.getOrDefault("dex",0)+equipment.getOrDefault(6,0);
    return staysHidden(sneak,dexterity,witnesses,random);
  }

  /** Same roll and witness penalty as the single-player StealthRules policy. */
  static boolean staysHidden(int sneakSkill,int agility,int witnesses,RandomGenerator random) {
    int penalty=witnesses<=0 ? 0 : (witnesses-1)*10;
    int success=Math.max(0,sneakSkill)+Math.max(0,agility)/6-penalty;
    return random.nextInt(100)+1<success;
  }
}
