package com.perso.T4C.npc;

/** Bridges migrated server-side NPC effects to the active game screen. */
public final class NpcSummonBridge {
    @FunctionalInterface
    public interface SummonCallback {
        boolean summon(String monster, float worldX, float worldY, int worldZ);
    }

    private static SummonCallback summonCallback;

    private NpcSummonBridge() {}

    public static void setSummonCallback(SummonCallback callback) {
        summonCallback = callback;
    }

    public static boolean summon(String monster, float worldX, float worldY, int worldZ) {
        return summonCallback != null && summonCallback.summon(monster, worldX, worldY, worldZ);
    }
}
