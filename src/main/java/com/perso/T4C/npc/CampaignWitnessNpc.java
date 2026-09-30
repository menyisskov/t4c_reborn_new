package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import java.util.List;

/** A branch witness reveals the lieutenant's weakness after Maelin accepts the first deed. */
abstract class CampaignWitnessNpc extends ScriptedNpc {
  private final String branch;
  private final int firstQuestIndex;
  private final String clueFlag;
  private Player testimonyPlayer;
  private boolean testimonyHeard;

  protected CampaignWitnessNpc(
      NpcSpec spec, NpcContext context, String branch, int firstQuestIndex, String clueFlag)
      throws GameException {
    super(spec, context);
    this.branch = branch;
    this.firstQuestIndex = firstQuestIndex;
    this.clueFlag = clueFlag;
  }

  static NpcSpec witnessSpec(String id, String branch, boolean robe) {
    String prefix = "${npc.hollow_dawn." + branch;
    return new NpcSpec(
        id,
        prefix + ".name}",
        null,
        List.of(
            new NpcSpec.Part(BodyPart.BODY, robe ? "WoWhiteRobe" : "PupLeatherBody"),
            new NpcSpec.Part(BodyPart.BOOT, robe ? "WoLeatherBoots" : "PupLeatherBoots"),
            new NpcSpec.Part(
                robe ? BodyPart.ROBELEGS : BodyPart.LEGS,
                robe ? "WoClothRobe" : "PupLeatherPants")),
        0,
        List.of(),
        prefix + ".welcome}",
        List.of(
            new NpcSpec.DialogueTopic(
                List.of("${npc.hollow_dawn.keyword.testimony}"), prefix + ".testimony}", List.of()),
            new NpcSpec.DialogueTopic(
                List.of("${npc.hollow_dawn.keyword.clue}"), prefix + ".clue}", List.of())),
        id + "NPC");
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public void onConversationStart(NpcBehaviorContext context) {
        testimonyPlayer = context.player();
        testimonyHeard = false;
        context.say(specification().welcomeText());
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        int topic = -1;
        for (int i = 0; i < specification().topics().size(); i++) {
          if (ScriptedNpc.matches(specification().topics().get(i), keyword)) {
            topic = i;
            break;
          }
        }
        if (topic < 0) return false;
        if (testimonyPlayer != context.player()) {
          testimonyPlayer = context.player();
          testimonyHeard = false;
        }
        String prefix = "${npc.hollow_dawn." + branch;
        if (QuestService.statusFor(
                context.player(), HollowDawnCampaign.avalon().get(firstQuestIndex))
            != QuestService.STATUS_COMPLETED) {
          context.say(prefix + ".locked}");
        } else if (topic == 0) {
          testimonyHeard = true;
          context.say(prefix + ".testimony}");
        } else if (!testimonyHeard && context.flag(clueFlag) == 0) {
          context.say(prefix + ".hear_testimony}");
        } else {
          context.flag(clueFlag, 1);
          context.say(prefix + ".clue}");
        }
        return true;
      }
    };
  }
}
