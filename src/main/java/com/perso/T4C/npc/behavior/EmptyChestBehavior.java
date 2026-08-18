package com.perso.T4C.npc.behavior;

public final class EmptyChestBehavior implements NpcBehavior {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private final String emptyMessage;

  private final String tooFarMessage;

  public EmptyChestBehavior(String emptyMessage, String tooFarMessage) {

    this.emptyMessage = emptyMessage;

    this.tooFarMessage = tooFarMessage;
  }

  @Override
  public void onInitialise(NpcBehaviorContext c) {

    c.npc().setStationary(true);
  }

  @Override
  public void onConversationStart(NpcBehaviorContext c) {

    c.systemMessageKey(c.isInRange(4) ? emptyMessage : tooFarMessage);

    c.endConversation();
  }
}
