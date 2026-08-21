package com.perso.T4C.npc.core;

import com.perso.T4C.npc.companion.CompanionManager;
import com.perso.T4C.quest.QuestService;
import java.util.function.Supplier;

public record NpcContext(
    QuestService questService, Supplier<CompanionManager> companionManagerSupplier) {

  public NpcContext {

    companionManagerSupplier =
        companionManagerSupplier == null ? () -> null : companionManagerSupplier;
  }

  public NpcContext(QuestService questService) {

    this(questService, () -> null);
  }
}
