package com.perso.T4C.npc.behavior;

import com.perso.T4C.player.Player;

public interface NpcBehavior {

  default void onConversationStart(NpcBehaviorContext context) {}

  default boolean onKeyword(NpcBehaviorContext context, String keyword) {

    return false;
  }

  default boolean onYesNo(NpcBehaviorContext context, String state, boolean answer) {

    return false;
  }

  default void onInitialise(NpcBehaviorContext context) {}

  default void onPopup(NpcBehaviorContext context) {}

  default void onAttack(NpcBehaviorContext context) {}

  default void onAttacked(NpcBehaviorContext context) {}

  default void onDeath(NpcBehaviorContext context) {}

  default void onDestroy(NpcBehaviorContext context) {}

  default void onHit(NpcBehaviorContext context) {}

  default void onAttackHit(NpcBehaviorContext context) {}

  default boolean handles(Player player) {

    return player != null;
  }
}
