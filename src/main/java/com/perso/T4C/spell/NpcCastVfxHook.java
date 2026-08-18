package com.perso.T4C.spell;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.player.Player;

public final class NpcCastVfxHook {
  @FunctionalInterface
  public interface CastEffect {
    void play(SpellData spell, Player player, Vector2 casterPosition);
  }

  @FunctionalInterface
  public interface SelfEffect {
    void play(SpellData spell, Vector2 casterPosition);
  }

  private static CastEffect shared;
  private static SelfEffect selfShared;

  private NpcCastVfxHook() {}

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
