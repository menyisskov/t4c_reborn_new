package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "KeeperVael", x = 5505, y = 1150, z = 0, stationary = true, aggressive = false)
public final class KeeperVael extends CampaignQuestNpc {
  public static final String ID = "KeeperVael";
  public static final String DISPLAY_NAME = "${npc.keeper_vael}";
  public static final String SPRITE_BASE = null;
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.keeper_vael.welcome}",
          CampaignTopics.all(),
          "KeeperVaelNPC");

  public KeeperVael(NpcContext context) throws GameException {
    super(SPEC, context, HollowDawnCampaign.threnody(), HollowDawnCampaign.avalon().getLast());
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
