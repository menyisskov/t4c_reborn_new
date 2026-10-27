package com.perso.T4C.combat;

import com.perso.T4C.monster.core.MonsterDef;
import java.util.Map;
import java.util.List;
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
  /** Caller filters range and unsupported spell states before choosing an authored attack. */
  public static MonsterDef.Attack chooseAttack(List<MonsterDef.Attack> eligible,RandomGenerator random) {
    if (eligible.isEmpty()) return null;
    int totalWeight=0;
    for (var attack:eligible) totalWeight+=Math.max(0,attack.getValue2());
    if (totalWeight<=0) return eligible.get(eligible.size()-1);
    int roll=random.nextInt(totalWeight)+1,cumulative=0;
    for (var attack:eligible) {
      cumulative+=Math.max(0,attack.getValue2());
      if (roll<=cumulative) return attack;
    }
    return eligible.get(eligible.size()-1);
  }
  /** Preserves the desktop monster's legacy dice parsing exactly. */
  public static int rollDamage(String formula,RandomGenerator random) {
    if (formula==null || formula.isEmpty()) return 0;
    var matcher=java.util.regex.Pattern.compile("(\\d+)d(\\d+)([+-]\\d+)?",java.util.regex.Pattern.CASE_INSENSITIVE)
        .matcher(formula.trim());
    if (!matcher.find()) return 0;
    int count=Integer.parseInt(matcher.group(1)),sides=Integer.parseInt(matcher.group(2));
    int total=matcher.group(3)!=null ? Integer.parseInt(matcher.group(3)) : 0;
    for (int i=0;i<count;i++) total+=random.nextInt(sides)+1;
    return Math.max(0,total);
  }
  private static int decodeArmor(int armor) {
    if (armor>=0 && armor<100_000) return armor;
    float decoded=Float.intBitsToFloat(armor);
    return Float.isFinite(decoded) ? Math.max(0,Math.round(decoded)) : 0;
  }
}
