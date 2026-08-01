package com.perso.T4C.spell;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.monster.BaseMonster;

/**
 * Lets an ally companion play spell visuals without depending on
 * {@link com.perso.T4C.screens.MainGameScreen}, mirroring {@link NpcCastVfxHook}.
 *
 * <p>{@code NpcCastVfxHook} can only target the player, so companions get their
 * own hook: an offensive cast needs a {@link BaseMonster} target and must defer
 * its effect until the projectile lands, while a heal simply plays at a point.
 */
public final class CompanionCastVfxHook {

    @FunctionalInterface
    public interface AttackEffect {
        /**
         * Launches {@code spell} from {@code casterPosition} toward {@code target},
         * running {@code onImpact} when the projectile arrives (or immediately when
         * the spell has no projectile).
         */
        void play(SpellData spell, BaseMonster target, Vector2 casterPosition, Runnable onImpact);
    }

    @FunctionalInterface
    public interface HealEffect {
        /** Plays {@code spell}'s impact visuals at the given world position. */
        void play(SpellData spell, float worldX, float worldY);
    }

    private static AttackEffect sharedAttack;
    private static HealEffect sharedHeal;

    private CompanionCastVfxHook() {
    }

    public static void setShared(AttackEffect attackEffect, HealEffect healEffect) {
        sharedAttack = attackEffect;
        sharedHeal = healEffect;
    }

    /** Returns false when no renderer is registered, so the caller can still apply the effect. */
    public static boolean playAttack(SpellData spell, BaseMonster target,
                                     Vector2 casterPosition, Runnable onImpact) {
        if (sharedAttack == null) {
            return false;
        }
        sharedAttack.play(spell, target, casterPosition, onImpact);
        return true;
    }

    public static void playHeal(SpellData spell, float worldX, float worldY) {
        if (sharedHeal != null) {
            sharedHeal.play(spell, worldX, worldY);
        }
    }
}
