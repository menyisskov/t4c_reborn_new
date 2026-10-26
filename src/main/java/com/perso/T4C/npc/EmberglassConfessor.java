package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "EmberglassConfessor",
    x = 4326,
    y = 2726,
    z = 0,
    stationary = true,
    aggressive = false)
public final class EmberglassConfessor extends ConcordStoryNpc {
  public static final String ID = "EmberglassConfessor";
  public static final String DISPLAY_NAME = "${npc.concord.EmberglassConfessor.name}";
  public static final String SPRITE_BASE = null;

  public EmberglassConfessor(NpcContext context) throws GameException {
    super(spec(), context, 2);
  }

  public static NpcSpec spec() {
    return storySpec(ID);
  }
}
