package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.PassageToAvalon;
import com.perso.T4C.quest.definition.TidewornShoreScouts;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// The last dock still standing on the mainland shore facing Avalon (1500,1200), worldZ 0 - gives
// the two-stage "Passage to Avalon" chain: quest/definition/TidewornShoreScouts.java (prove
// yourself against the scouts first) then quest/definition/PassageToAvalon.java (the real
// assault on Ithrak's warband and the actual zone unlock). This is the same role the Oracle
// plays for rebirth: without finishing the chain, no fresh character can ever reach Avalon
// Sanctuary, since every NPC and shop that could sell a scroll_of_avalon or teach AvalonGateway
// is itself stationed inside Avalon.
//
// T4C-0032: the "avalon"/"passage" keyword is handled by javaBehavior() below instead of a plain
// GIVE_QUEST action, so it always offers/reports whichever stage of the chain the player is
// actually on - see the dispatch logic there. A character who already completed the original
// single-stage "passage_to_avalon" (pre-T4C-0032) is unaffected: QuestService.statusFor() still
// reports that quest COMPLETED, so the dispatch never re-offers the scouting stage to them.
@Spawn(type = "HarbormasterRangor", x = 1500, y = 1200, z = 0, stationary = false, aggressive = false)
public final class HarbormasterRangor extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HarbormasterRangor";

  public static final String DISPLAY_NAME = "${npc.harbormasterrangor}";

  public static final String SPRITE_BASE = null;

  // Declarative fallback/documentation entry for the compendium - real dispatch always happens
  // in javaBehavior() below, which intercepts these same keywords first.
  private static final NpcSpec.DialogueTopic AVALON_TOPIC =
      new NpcSpec.DialogueTopic(
          List.of(
              "${npc.topic_keyword.harbormasterrangor.0.0}",
              "${npc.topic_keyword.harbormasterrangor.0.1}"),
          "${npc.topic.harbormasterrangor.0}",
          List.of(new NpcSpec.Action(ActionType.GIVE_QUEST, List.of("tideworn_shore_scouts"))));

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
          "${npc.welcome.harbormasterrangor}",
          List.of(
              AVALON_TOPIC,
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.harbormasterrangor.1.0}"),
                  "${npc.topic.harbormasterrangor.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.harbormasterrangor.2.0}"),
                  "${npc.topic.harbormasterrangor.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.harbormasterrangor.3.0}"),
                  "${npc.topic.harbormasterrangor.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.harbormasterrangor.4.0}",
                      "${npc.topic_keyword.harbormasterrangor.4.1}"),
                  "${npc.topic.harbormasterrangor.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.harbormasterrangor.5.0}",
                      "${npc.topic_keyword.harbormasterrangor.5.1}"),
                  "${npc.topic.harbormasterrangor.5}",
                  List.of())),
          "HarbormasterRangorNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public HarbormasterRangor(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        if (!ScriptedNpc.matches(AVALON_TOPIC, keyword)) {
          return false;
        }
        Player player = context.player();
        QuestService quests = context.npc().questService();
        if (quests == null) {
          return false;
        }
        String questId = nextQuestIdFor(player);
        String response = quests.giveOrReport(questId, ID, player);
        if (response != null && !response.isBlank()) {
          context.say(response);
        }
        return true;
      }
    };
  }

  /** Stage two once it's ever been touched (active, or completed - including by a character who
   * finished the original single-stage quest before this chain existed); otherwise stage two
   * once stage one is done, or stage one itself. */
  private static String nextQuestIdFor(Player player) {
    if (QuestService.statusFor(player, PassageToAvalon.definition())
        != QuestService.STATUS_NOT_STARTED) {
      return "passage_to_avalon";
    }
    if (QuestService.statusFor(player, TidewornShoreScouts.definition())
        == QuestService.STATUS_COMPLETED) {
      return "passage_to_avalon";
    }
    return "tideworn_shore_scouts";
  }
}
