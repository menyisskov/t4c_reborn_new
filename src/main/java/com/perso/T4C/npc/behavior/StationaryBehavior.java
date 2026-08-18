package com.perso.T4C.npc.behavior;

public final class StationaryBehavior implements NpcBehavior {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final StationaryBehavior INSTANCE = new StationaryBehavior();

  private StationaryBehavior() {}

  @Override
  public void onInitialise(NpcBehaviorContext c) {

    c.npc().setStationary(true);
  }
}
