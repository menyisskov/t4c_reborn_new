package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MoonwakeWitnessIlyra", x = 3930, y = 830, z = 0, stationary = true, aggressive = false)
public final class MoonwakeWitnessIlyra extends CampaignWitnessNpc {
  public static final String ID = "MoonwakeWitnessIlyra";
  public static final String DISPLAY_NAME = "${npc.hollow_dawn.moonwake.name}";
  public static final String SPRITE_BASE = null;
  private static final NpcSpec SPEC = witnessSpec(ID, "moonwake", true);

  public MoonwakeWitnessIlyra(NpcContext context) throws GameException {
    super(SPEC, context, "moonwake", 0, HollowDawnCampaign.MOONWAKE_CLUE);
  }

  public static NpcSpec spec() { return SPEC; }
}
