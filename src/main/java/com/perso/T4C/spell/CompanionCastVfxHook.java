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
        /**
         * Plays {@code spell} travelling from {@code casterPosition} to
         * {@code castOn}: projectile then impact, as a healer NPC does. A null
         * {@code castOn} means a self-heal, played on the spot.
         */
        void play(SpellData spell, com.perso.T4C.player.Player castOn,
                  Vector2 casterPosition, float worldX, float worldY, Runnable onImpact);
    }

    @FunctionalInterface
    public interface VanishEffect {
        /** Plays the departure visuals at the world position the companion left. */
        void play(float worldX, float worldY);
    }

    private static AttackEffect sharedAttack;
    private static HealEffect sharedHeal;
    private static VanishEffect sharedVanish;

    private CompanionCastVfxHook() {
    }

    public static void setShared(AttackEffect attackEffect, HealEffect healEffect,
                                 VanishEffect vanishEffect) {
        sharedAttack = attackEffect;
        sharedHeal = healEffect;
        sharedVanish = vanishEffect;
    }

    /** Plays the companion's departure burst where it stood. */
    public static void playVanish(float worldX, float worldY) {
        if (sharedVanish != null) {
            sharedVanish.play(worldX, worldY);
        }
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

    /**
     * Plays a heal projected onto {@code castOn} from {@code casterPosition},
     * running {@code onImpact} when it lands. Returns false when no renderer is
     * registered, so the caller can still apply the heal itself.
     */
    public static boolean playHeal(SpellData spell, com.perso.T4C.player.Player castOn,
                                   Vector2 casterPosition, Runnable onImpact) {
        if (sharedHeal == null || castOn == null) {
            return false;
        }
        Vector2 target = castOn.getPositionVector();
        sharedHeal.play(spell, castOn, casterPosition, target.x, target.y, onImpact);
        return true;
    }

    /** Plays a self-heal at the caster's own position, with no projectile. */
    public static void playSelfHeal(SpellData spell, float worldX, float worldY) {
        if (sharedHeal != null) {
            sharedHeal.play(spell, null, null, worldX, worldY, null);
        }
    }
}
