package com.perso.T4C.skill;

import com.perso.T4C.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;

/** Learning and active-use rules for the skills present in the original server. */
public final class SkillService {
    public enum Failure { NONE, UNKNOWN_SKILL, ALREADY_LEARNED, LEVEL, ATTRIBUTES, PREREQUISITE, NOT_ENOUGH_POINTS, NOT_LEARNED, COOLDOWN }

    public record Result(boolean success, Failure failure, int newLevel) {
        private static Result failure(Failure failure, int level) { return new Result(false, failure, level); }
        private static Result success(int level) { return new Result(true, Failure.NONE, level); }
    }

    private static final Map<String, SkillDefinition> DEFINITIONS = createDefinitions();

    private SkillService() {
    }

    public static Map<String, SkillDefinition> definitions() {
        return DEFINITIONS;
    }

    public static Result learn(Player player, String skillId) {
        SkillDefinition definition = DEFINITIONS.get(skillId);
        int current = player == null ? 0 : player.getSkillLevel(skillId);
        if (player == null || definition == null) return Result.failure(Failure.UNKNOWN_SKILL, current);
        if (current > 0) return Result.failure(Failure.ALREADY_LEARNED, current);
        if (player.getLevel() < definition.minimumLevel()) return Result.failure(Failure.LEVEL, current);
        if (!meetsAttributes(player, definition)) return Result.failure(Failure.ATTRIBUTES, current);
        for (Map.Entry<String, Integer> prerequisite : definition.prerequisites().entrySet()) {
            if (player.getSkillLevel(prerequisite.getKey()) < prerequisite.getValue()) {
                return Result.failure(Failure.PREREQUISITE, current);
            }
        }
        int cost = Math.max(0, definition.learningCost());
        if (player.getSkillPoints() < cost) return Result.failure(Failure.NOT_ENOUGH_POINTS, current);
        player.setSkillPoints(player.getSkillPoints() - cost);
        player.setSkillLevel(skillId, 1);
        return Result.success(1);
    }

    /** Trains an already learned skill point-for-point, like Skills::TrainSkill. */
    public static Result train(Player player, String skillId, int points, int maximum) {
        int current = player == null ? 0 : player.getSkillLevel(skillId);
        if (player == null || !DEFINITIONS.containsKey(skillId)) return Result.failure(Failure.UNKNOWN_SKILL, current);
        if (current <= 0) return Result.failure(Failure.NOT_LEARNED, current);
        int quantity = Math.max(0, Math.min(points, Math.max(0, maximum - current)));
        if (quantity <= 0) return Result.success(current);
        if (player.getSkillPoints() < quantity) return Result.failure(Failure.NOT_ENOUGH_POINTS, current);
        player.setSkillPoints(player.getSkillPoints() - quantity);
        player.setSkillLevel(skillId, current + quantity);
        return Result.success(current + quantity);
    }

    /**
     * Starts an explicitly used skill. Passive combat skills are consumed by
     * CombatResolver and therefore never need a separate activation call.
     */
    public static Result use(Player player, String skillId, long cooldownMillis) {
        int level = player == null ? 0 : player.getSkillLevel(skillId);
        if (level <= 0) return Result.failure(Failure.NOT_LEARNED, level);
        if (!player.isSkillReady(skillId)) return Result.failure(Failure.COOLDOWN, level);
        switch (skillId) {
            case "rapid_healing" -> player.applyHeal(Math.max(1, level / 10), Math.max(1, level / 10));
            case "sneak" -> player.setHidden(true);
            default -> { /* Combat and interaction skills are resolved by their owning service. */ }
        }
        player.triggerSkillCooldown(skillId, Math.max(0L, cooldownMillis));
        return Result.success(level);
    }

    public static Result use(Player player, String skillId) {
        long cooldown = switch (skillId == null ? "" : skillId) {
            case "rapid_healing" -> 5_000L;
            case "sneak", "pick_lock", "rob" -> 1_000L;
            default -> 0L;
        };
        return use(player, skillId, cooldown);
    }

    private static boolean meetsAttributes(Player player, SkillDefinition definition) {
        return player.getStrength() >= definition.minimumStrength()
                && player.getEndurance() >= definition.minimumEndurance()
                && player.getDexterity() >= definition.minimumAgility()
                && player.getIntelligence() >= definition.minimumIntelligence()
                && player.getWisdom() >= definition.minimumWisdom();
    }

    private static Map<String, SkillDefinition> createDefinitions() {
        Map<String, SkillDefinition> skills = new LinkedHashMap<>();
        add(skills, "attack", 1, 0, 0, 0, 0, 0, Map.of());
        add(skills, "dodge", 1, 0, 0, 0, 0, 0, Map.of());
        add(skills, "archery", 1, 0, 0, 0, 0, 0, Map.of());
        add(skills, "powerful_blow", 15, 50, 0, 30, 0, 0, Map.of());
        add(skills, "stun_blow", 3, 25, 0, 20, 0, 0, Map.of());
        add(skills, "parry", 10, 0, 0, 30, 20, 0, Map.of());
        add(skills, "armor_penetration", 25, 75, 0, 40, 30, 0, Map.of());
        add(skills, "two_weapons", 25, 75, 0, 40, 30, 0, Map.of());
        add(skills, "rapid_healing", 30, 0, 80, 0, 0, 0, Map.of());
        add(skills, "pick_lock", 12, 0, 0, 40, 0, 0, Map.of());
        add(skills, "peek", 1, 0, 0, 0, 0, 0, Map.of());
        add(skills, "rob", 17, 0, 0, 50, 0, 0, Map.of("peek", 25));
        add(skills, "sneak", 24, 0, 0, 75, 0, 0, Map.of());
        return Map.copyOf(skills);
    }

    private static void add(Map<String, SkillDefinition> out, String id, int level, int str, int end,
                            int agi, int intel, int wis, Map<String, Integer> prerequisites) {
        out.put(id, new SkillDefinition(id, level, str, end, agi, intel, wis, 1, prerequisites));
    }
}
