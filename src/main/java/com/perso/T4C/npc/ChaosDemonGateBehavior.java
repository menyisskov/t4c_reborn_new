package com.perso.T4C.npc;

import com.perso.T4C.npc.behavior.*;

public final class ChaosDemonGateBehavior implements NpcBehavior {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private final int tileX, tileY, world;

  private final String blockedMessage;

  public ChaosDemonGateBehavior(int tileX, int tileY, int world, String blockedMessage) {

    this.tileX = tileX;

    this.tileY = tileY;

    this.world = world;

    this.blockedMessage = blockedMessage;
  }

  @Override
  public void onInitialise(NpcBehaviorContext c) {

    c.npc().setStationary(true);
  }

  @Override
  public void onConversationStart(NpcBehaviorContext c) {

    if (!c.isInRange(4)) c.sayKey("npc.portal.too_far");
    else if (c.flag("ADDON_TERROR_DEMON_KILLED") == 1
        && c.flag("ADDON_CHAOS_DEMON_KILLED") == 1
        && c.flag("ADDON_DARKNESS_DEMON_KILLED") == 1) c.teleport(tileX, tileY, world);
    else c.sayKey(blockedMessage);

    c.endConversation();
  }
}
