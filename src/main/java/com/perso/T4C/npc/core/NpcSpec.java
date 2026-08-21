package com.perso.T4C.npc.core;

import com.perso.T4C.npc.ActionType;
import com.perso.T4C.player.BodyPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record NpcSpec(
    String id,
    String displayName,
    String spriteBase,
    List<Part> parts,
    int patrolRadiusTiles,
    List<String> fleeShouts,
    String welcomeText,
    List<DialogueTopic> topics,
    String sourceTemplate,
    String sourceScript,
    Map<String, String> sourceEvents,
    CombatProfile combatProfile) {

  public NpcSpec(
      String id,
      String displayName,
      String spriteBase,
      List<Part> parts,
      int patrolRadiusTiles,
      List<String> fleeShouts,
      String welcomeText,
      List<DialogueTopic> topics,
      String sourceTemplate) {

    this(
        id,
        displayName,
        spriteBase,
        parts,
        patrolRadiusTiles,
        fleeShouts,
        welcomeText,
        topics,
        sourceTemplate,
        null,
        Map.of(),
        CombatProfile.DEFAULT);
  }

  public NpcSpec(
      String id,
      String displayName,
      String spriteBase,
      List<Part> parts,
      int patrolRadiusTiles,
      List<String> fleeShouts,
      String welcomeText,
      List<DialogueTopic> topics,
      String sourceTemplate,
      CombatProfile combatProfile) {

    this(
        id,
        displayName,
        spriteBase,
        parts,
        patrolRadiusTiles,
        fleeShouts,
        welcomeText,
        topics,
        sourceTemplate,
        null,
        Map.of(),
        combatProfile);
  }

  public NpcSpec(
      String id,
      String displayName,
      String spriteBase,
      List<Part> parts,
      int patrolRadiusTiles,
      List<String> fleeShouts,
      String welcomeText,
      List<DialogueTopic> topics,
      String sourceTemplate,
      String sourceScript,
      Map<String, String> sourceEvents) {

    this(
        id,
        displayName,
        spriteBase,
        parts,
        patrolRadiusTiles,
        fleeShouts,
        welcomeText,
        topics,
        sourceTemplate,
        sourceScript,
        sourceEvents,
        CombatProfile.from(sourceEvents));
  }

  public NpcSpec {

    if (id == null || id.isBlank()) throw new IllegalArgumentException("NPC id is required");

    parts = List.copyOf(parts == null ? List.of() : parts);

    fleeShouts = List.copyOf(fleeShouts == null ? List.of() : fleeShouts);

    topics = List.copyOf(topics == null ? List.of() : topics);

    sourceEvents = Map.copyOf(sourceEvents == null ? Map.of() : sourceEvents);

    combatProfile = combatProfile == null ? CombatProfile.DEFAULT : combatProfile;
  }

  public Object[] partsArray() {
    List<Object> flattened = new ArrayList<>();
    for (Part part : parts) {
      if (part != null && part.bodyPart() != null) {
        flattened.add(part.bodyPart());
        flattened.add(part.spriteBase());
      }
    }
    return flattened.toArray();
  }

  public record Part(BodyPart bodyPart, String spriteBase) {

    public Part {

      if (bodyPart == null) throw new IllegalArgumentException("NPC body part is required");
    }
  }

  public record DialogueTopic(List<String> keywords, String response, List<Action> actions) {

    public DialogueTopic {

      keywords = List.copyOf(keywords == null ? List.of() : keywords);

      actions = List.copyOf(actions == null ? List.of() : actions);
    }
  }

  public record Action(ActionType type, List<String> targets) {

    public Action {

      if (type == null) throw new IllegalArgumentException("NPC action type is required");

      targets = List.copyOf(targets == null ? List.of() : targets);
    }
  }

  public record CombatProfile(
      int level,
      int maxHp,
      int strength,
      int endurance,
      int dexterity,
      int armorClass,
      int attackSkill,
      int dodge,
      String damageFormula) {

    public static final CombatProfile DEFAULT = new CombatProfile(1, 1, 10, 10, 10, 0, 1, 1, "1d3");

    public CombatProfile {

      level = Math.max(1, level);

      maxHp = Math.max(1, maxHp);

      strength = Math.max(1, strength);

      endurance = Math.max(1, endurance);

      dexterity = Math.max(1, dexterity);

      armorClass = Math.max(0, armorClass);

      attackSkill = Math.max(1, attackSkill);

      dodge = Math.max(1, dodge);

      damageFormula = damageFormula == null || damageFormula.isBlank() ? "1d3" : damageFormula;
    }

    static CombatProfile from(Map<String, String> values) {

      if (values == null || values.isEmpty()) return DEFAULT;

      return new CombatProfile(
          integer(values, "@combat.level", 1),
          integer(values, "@combat.hp", 1),
          integer(values, "@combat.str", 10),
          integer(values, "@combat.end", 10),
          integer(values, "@combat.dex", 10),
          integer(values, "@combat.ac", 0),
          integer(values, "@combat.attackSkill", 1),
          integer(values, "@combat.dodge", 1),
          values.getOrDefault("@combat.damage", "1d3"));
    }

    private static int integer(Map<String, String> values, String key, int fallback) {

      try {

        return Integer.parseInt(values.getOrDefault(key, Integer.toString(fallback)));

      } catch (NumberFormatException ignored) {

        return fallback;
      }
    }
  }
}
