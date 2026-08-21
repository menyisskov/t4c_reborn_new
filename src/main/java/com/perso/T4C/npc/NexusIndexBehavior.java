package com.perso.T4C.npc;

import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;

final class NexusIndexBehavior implements NpcBehavior {

  static final NpcBehavior INSTANCE = new NexusIndexBehavior();

  private NexusIndexBehavior() {}

  @Override
  public void onConversationStart(NpcBehaviorContext c) {

    String id = c.npc().getSpec().id();

    int index = Integer.parseInt(id.substring("NexusStone".length()));

    if (c.flag("ADDON_STORYLINE_PROGRESS") < 27) {

      c.sayKey("npc.nexus.ancient");

      return;
    }

    if (!c.hasItem("runed_stone_tablet")) {

      c.sayKey("npc.nexus.noTablet");

      return;
    }

    String activated = "ADDON_NEXUS_" + index + "_ACTIVATED";

    if (c.flag(activated) != 0) {

      c.sayKey("npc.nexus.done");

      return;
    }

    c.takeItem("runed_stone_tablet");

    c.flag(activated, 1);

    c.flag("ADDON_NEXUS_STONES_ACTIVATED", c.flag("ADDON_NEXUS_STONES_ACTIVATED") + 1);

    c.sayKey(
        c.flag("ADDON_NEXUS_STONES_ACTIVATED") >= 5 ? "npc.nexus.last" : "npc.nexus.activated");

    c.giveItem("runed_stone_tablet");
  }
}
