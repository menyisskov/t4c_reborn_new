package com.perso.T4C.skill;

import com.perso.T4C.player.Player;

import java.util.Map;

/** Learning and active-use rules for the skills present in the original server. */
public final class SkillService {
    public enum Failure { NONE, UNKNOWN_SKILL, ALREADY_LEARNED, LEVEL, ATTRIBUTES, PREREQUISITE, NOT_ENOUGH_POINTS, NOT_LEARNED, COOLDOWN }

    public record Result(boolean success, Failure failure, int newLevel) {
        private static Result failure(Failure failure, int level) { return new Result(false, failure, level); }
        private static Result success(int level) { return new Result(true, Failure.NONE, level); }
    }

    private SkillService() {
    }

    public static Map<String, SkillDefinition> definitions() {
        return SkillRegistry.load();
    }

    public static Result learn(Player player, String skillId) {
        SkillDefinition definition = SkillRegistry.findById(skillId);
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
        if (player == null || SkillRegistry.findById(skillId) == null) return Result.failure(Failure.UNKNOWN_SKILL, current);
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
        SkillDefinition definition = SkillRegistry.findById(skillId);
        long cooldown = definition == null ? 0L : Math.max(0L, definition.useCooldownMillis());
        return use(player, skillId, cooldown);
    }

    private static boolean meetsAttributes(Player player, SkillDefinition definition) {
        return player.getStrength() >= definition.minimumStrength()
                && player.getEndurance() >= definition.minimumEndurance()
                && player.getDexterity() >= definition.minimumAgility()
                && player.getIntelligence() >= definition.minimumIntelligence()
                && player.getWisdom() >= definition.minimumWisdom();
    }
}
