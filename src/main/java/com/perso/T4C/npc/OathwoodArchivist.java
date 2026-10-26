package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "OathwoodArchivist", x = 3774, y = 1214, z = 0, stationary = true, aggressive = false)
public final class OathwoodArchivist extends ConcordStoryNpc {
  public static final String ID = "OathwoodArchivist";
  public static final String DISPLAY_NAME = "${npc.concord.OathwoodArchivist.name}";
  public static final String SPRITE_BASE = null;

  public OathwoodArchivist(NpcContext context) throws GameException {
    super(spec(), context, 0);
  }

  public static NpcSpec spec() {
    return storySpec(ID);
  }
}
