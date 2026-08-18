package com.perso.T4C.npc.behavior;

import com.perso.T4C.gui.screen.LearnScreen;
import java.util.List;
import java.util.Locale;

public final class TrainingBehavior implements NpcBehavior {

  private final List<LearnScreen.TrainingOffer> offers;

  public TrainingBehavior(boolean teaching, List<LearnScreen.TrainingOffer> offers) {

    this.offers = List.copyOf(offers);
  }

  @Override
  public boolean onKeyword(NpcBehaviorContext ctx, String keyword) {

    String k = keyword == null ? "" : keyword.toUpperCase(Locale.ROOT);

    boolean hasTeaching = offers.stream().anyMatch(LearnScreen.TrainingOffer::teaching);

    boolean hasTraining = offers.stream().anyMatch(o -> !o.teaching());

    if ((hasTeaching && (k.contains("TEACH") || k.contains("LEARN")))
        || (hasTraining && (k.contains("TRAIN") || k.contains("SKILL")))) {

      boolean teach = k.contains("TEACH") || k.contains("LEARN");

      List<LearnScreen.TrainingOffer> visible =
          offers.stream().filter(o -> o.teaching() == teach).toList();

      if (!visible.isEmpty()) ctx.openSkillLearning(visible);

      return !visible.isEmpty();
    }

    return false;
  }
}
