package com.perso.T4C.combat;

import com.perso.T4C.monster.core.MonsterDef;
import java.util.Map;
import java.util.random.RandomGenerator;

/** Shared normalization of authored monster combat values, including legacy packed armor. */
public final class MonsterCombatRules {
  private MonsterCombatRules() {}
  public record Stats(int level,int strength,int endurance,int agility,int intelligence,int attack,int dodge,
      int armorMin,int armorMax) {
    public CombatProfile profile(boolean stunned,RandomGenerator random) {
      return new CombatProfile(level,strength,endurance,agility,attack,attack,dodge,
          rollArmor(armorMin,armorMax,random),stunned,false,true,Map.of());
    }
  }
  public static Stats resolve(MonsterDef definition) {
    int armorMin=decodeArmor(definition.getAcMin());
    return new Stats(Math.max(1,definition.getLevel()),Math.max(1,definition.getStr()),
        Math.max(1,definition.getEnd()),Math.max(1,definition.getAgi()),Math.max(1,definition.getIntel()),
        Math.max(1,definition.getAgi()+definition.getLevel()),Math.max(1,definition.getDodge()),
        armorMin,Math.max(armorMin,decodeArmor(definition.getAcMax())));
  }
  public static double rollArmor(int minimum,int maximum,RandomGenerator random) {
    int spread=maximum-minimum;
    return minimum+(spread>0 ? random.nextInt(spread+1) : 0);
  }
  private static int decodeArmor(int armor) {
    if (armor>=0 && armor<100_000) return armor;
    float decoded=Float.intBitsToFloat(armor);
    return Float.isFinite(decoded) ? Math.max(0,Math.round(decoded)) : 0;
  }
}
