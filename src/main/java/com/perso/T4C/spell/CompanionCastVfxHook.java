package com.perso.T4C.spell;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.monster.core.BaseMonster;

public final class CompanionCastVfxHook {
  @FunctionalInterface
  public interface AttackEffect {
    void play(SpellData spell, BaseMonster target, Vector2 casterPosition, Runnable onImpact);
  }

  @FunctionalInterface
  public interface HealEffect {
    void play(
        SpellData spell,
        com.perso.T4C.player.Player castOn,
        Vector2 casterPosition,
        float worldX,
        float worldY,
        Runnable onImpact);
  }

  @FunctionalInterface
  public interface VanishEffect {
    void play(float worldX, float worldY);
  }

  private static AttackEffect sharedAttack;
  private static HealEffect sharedHeal;
  private static VanishEffect sharedVanish;

  private CompanionCastVfxHook() {}

  public static void setShared(
      AttackEffect attackEffect, HealEffect healEffect, VanishEffect vanishEffect) {
    sharedAttack = attackEffect;
    sharedHeal = healEffect;
    sharedVanish = vanishEffect;
  }

  public static void playVanish(float worldX, float worldY) {
    if (sharedVanish != null) {
      sharedVanish.play(worldX, worldY);
    }
  }

  public static boolean playAttack(
      SpellData spell, BaseMonster target, Vector2 casterPosition, Runnable onImpact) {
    if (sharedAttack == null) {
      return false;
    }
    sharedAttack.play(spell, target, casterPosition, onImpact);
    return true;
  }

  public static boolean playHeal(
      SpellData spell,
      com.perso.T4C.player.Player castOn,
      Vector2 casterPosition,
      Runnable onImpact) {
    if (sharedHeal == null || castOn == null) {
      return false;
    }
    Vector2 target = castOn.getPositionVector();
    sharedHeal.play(spell, castOn, casterPosition, target.x, target.y, onImpact);
    return true;
  }

  public static void playSelfHeal(SpellData spell, float worldX, float worldY) {
    if (sharedHeal != null) {
      sharedHeal.play(spell, null, null, worldX, worldY, null);
    }
  }
}
