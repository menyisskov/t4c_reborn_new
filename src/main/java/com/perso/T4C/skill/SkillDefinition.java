package com.perso.T4C.skill;

import java.util.Map;

/** Requirements and cost curve for one learnable T4C skill. */
public record SkillDefinition(
        String id,
        int minimumLevel,
        int minimumStrength,
        int minimumEndurance,
        int minimumAgility,
        int minimumIntelligence,
        int minimumWisdom,
        int learningCost,
        Map<String, Integer> prerequisites,
        long useCooldownMillis) {

    public SkillDefinition {
        prerequisites = prerequisites == null ? Map.of() : Map.copyOf(prerequisites);
    }

}
