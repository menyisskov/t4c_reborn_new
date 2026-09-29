package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.quest.definition.FadingVeilReckoning;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "ChroniclerMaelin", x = 4050, y = 1480, z = 0, stationary = true, aggressive = false)
public final class ChroniclerMaelin extends CampaignQuestNpc {
  public static final String ID = "ChroniclerMaelin";
  public static final String DISPLAY_NAME = "${npc.chronicler_maelin}";
  public static final String SPRITE_BASE = null;
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.chronicler_maelin.welcome}",
          CampaignTopics.all(),
          "ChroniclerMaelinNPC");

  public ChroniclerMaelin(NpcContext context) throws GameException {
    super(SPEC, context, HollowDawnCampaign.avalon(), FadingVeilReckoning.definition());
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
