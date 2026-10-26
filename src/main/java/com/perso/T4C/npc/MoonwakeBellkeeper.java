package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MoonwakeBellkeeper", x = 3946, y = 631, z = 0, stationary = true, aggressive = false)
public final class MoonwakeBellkeeper extends ConcordStoryNpc {
  public static final String ID = "MoonwakeBellkeeper";
  public static final String DISPLAY_NAME = "${npc.concord.MoonwakeBellkeeper.name}";
  public static final String SPRITE_BASE = null;

  public MoonwakeBellkeeper(NpcContext context) throws GameException {
    super(spec(), context, 1);
  }

  public static NpcSpec spec() {
    return storySpec(ID);
  }
}
