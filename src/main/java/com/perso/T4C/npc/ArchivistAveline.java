package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "ArchivistAveline", x = 4044, y = 1480, z = 0, stationary = true, aggressive = false)
public final class ArchivistAveline extends ConcordStoryNpc {
  public static final String ID = "ArchivistAveline";
  public static final String DISPLAY_NAME = "${npc.concord.ArchivistAveline.name}";
  public static final String SPRITE_BASE = null;

  public ArchivistAveline(NpcContext context) throws GameException {
    super(spec(), context, -1);
  }

  public static NpcSpec spec() {
    return storySpec(ID);
  }
}
