package com.perso.T4C.npc.behavior;

import com.perso.T4C.gui.screen.LearnScreen;
import java.util.List;

public final class TrainingAndFleeBehavior implements NpcBehavior {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private final TrainingBehavior training;

  private final String messagePrefix;

  public TrainingAndFleeBehavior(
      boolean teaching, List<LearnScreen.TrainingOffer> offers, String messagePrefix) {

    this.training = new TrainingBehavior(teaching, offers);

    this.messagePrefix = messagePrefix;
  }

  @Override
  public boolean onKeyword(NpcBehaviorContext c, String text) {

    return training.onKeyword(c, text);
  }

  @Override
  public void onAttacked(NpcBehaviorContext c) {

    c.shoutKey(messagePrefix + (int) (Math.random() * 2));

    c.fleeFromPlayer();
  }
}
