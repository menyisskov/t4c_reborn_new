package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "EmberglassWarderSoren", x = 4280, y = 2620, z = 0, stationary = true, aggressive = false)
public final class EmberglassWarderSoren extends CampaignWitnessNpc {
  public static final String ID = "EmberglassWarderSoren";
  public static final String DISPLAY_NAME = "${npc.hollow_dawn.emberglass.name}";
  public static final String SPRITE_BASE = null;
  private static final NpcSpec SPEC = witnessSpec(ID, "emberglass", false);

  public EmberglassWarderSoren(NpcContext context) throws GameException {
    super(SPEC, context, "emberglass", 2, HollowDawnCampaign.EMBERGLASS_CLUE);
  }

  public static NpcSpec spec() { return SPEC; }
}
