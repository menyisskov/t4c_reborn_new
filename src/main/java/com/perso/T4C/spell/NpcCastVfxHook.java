package com.perso.T4C.spell;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.player.Player;

/**
 * Lets NPC dialog actions (e.g. Brother Kiran's CAST keyword) trigger the
 * same launch/impact sound, projectile and visual effect as a player-cast
 * spell, without giving {@code npc} code a dependency on
 * {@link com.perso.T4C.screens.MainGameScreen}. The active screen registers
 * itself via {@link #setShared}, mirroring {@link com.perso.T4C.ui.SystemMessage#setShared}.
 */
public final class NpcCastVfxHook {

    @FunctionalInterface
    public interface CastEffect {
        /** {@code casterPosition} is where the projectile (if any) starts from. */
        void play(SpellData spell, Player player, Vector2 casterPosition);
    }

    @FunctionalInterface
    public interface SelfEffect {
        void play(SpellData spell, Vector2 casterPosition);
    }

    private static CastEffect shared;
    private static SelfEffect selfShared;

    private NpcCastVfxHook() {
    }

    public static void setShared(CastEffect instance) {
        shared = instance;
        if (instance == null) selfShared = null;
    }

    public static void setShared(CastEffect instance, SelfEffect selfInstance) {
        shared = instance;
        selfShared = selfInstance;
    }

    public static void playOnPlayer(SpellData spell, Player player, Vector2 casterPosition) {
        if (shared != null) {
            shared.play(spell, player, casterPosition);
        }
    }

    public static void playOnSelf(SpellData spell, Vector2 casterPosition) {
        if (selfShared != null) selfShared.play(spell, casterPosition);
    }
}
