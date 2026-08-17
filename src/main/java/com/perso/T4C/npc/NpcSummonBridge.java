package com.perso.T4C.npc;

/** Bridges migrated server-side NPC effects to the active game screen. */
public final class NpcSummonBridge {
    public static final String SUMMON_DEATH_COUNTER = "@summon.deathCounter";
    public static final String SUMMON_DEATH_ITEM = "@summon.deathItem";
    public static final String SUMMON_DEATH_MESSAGE = "@summon.deathMessage";
    private static final java.util.Map<String, java.util.ArrayDeque<DeathEffect>> trackedSummons =
            new java.util.HashMap<>();
    public record DeathEffect(String counterFlag, String itemKey, String message) {}
    @FunctionalInterface
    public interface SummonCallback {
        boolean summon(String monster, float worldX, float worldY, int worldZ);
    }

    private static SummonCallback summonCallback;

    private NpcSummonBridge() {}

    public static void setSummonCallback(SummonCallback callback) {
        summonCallback = callback;
    }

    public static synchronized boolean summon(String monster, float worldX, float worldY, int worldZ) {
        return summon(monster, worldX, worldY, worldZ, java.util.Map.of());
    }

    public static synchronized boolean summon(String monster, float worldX, float worldY, int worldZ,
                                               java.util.Map<String, String> metadata) {
        boolean summoned = summonCallback != null && summonCallback.summon(monster, worldX, worldY, worldZ);
        String counter = metadata.get(SUMMON_DEATH_COUNTER);
        if (counter != null && !counter.isBlank()) {
            DeathEffect effect = new DeathEffect(counter, metadata.get(SUMMON_DEATH_ITEM),
                    metadata.get(SUMMON_DEATH_MESSAGE));
            if (summoned) trackedSummons.computeIfAbsent(normalize(monster), ignored -> new java.util.ArrayDeque<>())
                    .addLast(effect);
            else NpcScriptEngine.setGlobalFlag(counter, NpcScriptEngine.globalFlag(counter) - 1);
        }
        return summoned;
    }

    /** Returns the data-defined effects associated with the death of a tracked summon. */
    public static synchronized DeathEffect summonedMonsterDefeated(String monster) {
        String key = normalize(monster);
        java.util.ArrayDeque<DeathEffect> effects = trackedSummons.get(key);
        if (effects == null || effects.isEmpty()) return null;
        DeathEffect effect = effects.removeFirst();
        if (effects.isEmpty()) trackedSummons.remove(key);
        NpcScriptEngine.setGlobalFlag(effect.counterFlag(),
                NpcScriptEngine.globalFlag(effect.counterFlag()) - 1);
        return effect;
    }

    private static String normalize(String monster) {
        return monster == null ? "" : monster.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
