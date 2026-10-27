package com.perso.T4C.combat;

/** Shared equipment skill routing, preserving offline trained/buff/gear composition. */
public final class SkillBonusRules {
  private SkillBonusRules() {}

  public static int effective(String skill, int trained, int temporary, java.util.function.IntUnaryOperator equipment) {
    return trained + temporary + equipment.applyAsInt(directSkillStatId(skill))
        + equipment.applyAsInt(skillBoostStatId(skill));
  }

  private static int directSkillStatId(String skillId) {
    if (skillId == null) return Integer.MIN_VALUE;
    return switch (skillId) {
      case "attack" -> 8;
      case "dodge" -> 9;
      default -> Integer.MIN_VALUE;
    };
  }

  private static int skillBoostStatId(String skillId) {
    if (skillId == null) return Integer.MIN_VALUE;
    return switch (skillId) {
      case "stun_blow" -> 10001;
      case "powerful_blow" -> 10002;
      case "parry" -> 10008;
      case "dodge" -> 10011;
      case "attack" -> 10012;
      case "hide" -> 10014;
      case "rob" -> 10015;
      case "sneak" -> 10016;
      case "picklock" -> 10026;
      case "armor_penetration" -> 10027;
      case "peek" -> 10028;
      case "rapid_healing" -> 10029;
      case "archery" -> 10035;
      case "two_weapons" -> 10036;
      default -> Integer.MIN_VALUE;
    };
  }

}
