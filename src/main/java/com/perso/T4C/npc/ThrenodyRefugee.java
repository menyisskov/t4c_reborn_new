package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "ThrenodyRefugee", x = 5508, y = 1154, z = 0, stationary = true, aggressive = false)
public final class ThrenodyRefugee extends ConcordStoryNpc {
  public static final String ID = "ThrenodyRefugee";
  public static final String DISPLAY_NAME = "${npc.concord.ThrenodyRefugee.name}";
  public static final String SPRITE_BASE = null;

  public ThrenodyRefugee(NpcContext context) throws GameException {
    super(spec(), context, 3);
  }

  public static NpcSpec spec() {
    return storySpec(ID);
  }
}
