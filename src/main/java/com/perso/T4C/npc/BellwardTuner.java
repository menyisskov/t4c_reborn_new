package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "BellwardTuner", x = 5705, y = 1555, z = 0, stationary = true, aggressive = false)
public final class BellwardTuner extends ConcordStoryNpc {
  public static final String ID = "BellwardTuner";
  public static final String DISPLAY_NAME = "${npc.concord.BellwardTuner.name}";
  public static final String SPRITE_BASE = null;

  public BellwardTuner(NpcContext context) throws GameException {
    super(spec(), context, 4);
  }

  public static NpcSpec spec() {
    return storySpec(ID);
  }
}
