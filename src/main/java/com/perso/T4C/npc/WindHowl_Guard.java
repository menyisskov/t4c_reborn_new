package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "WindHowl_Guard", x = 1752, y = 1238, z = 0, stationary = false, aggressive = false)
public final class WindHowl_Guard extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "WindHowl_Guard";

  public static final String DISPLAY_NAME = "${npc.windhowl_guard}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupPlateBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupPlateLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupPlateHelm"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleAxe"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupPlateGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupPlateGloveL")),
          0,
          List.of(),
          "${npc.welcome.windhowl_guard}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowl_guard.0.0}",
                      "${npc.topic_keyword.windhowl_guard.0.1}"),
                  "${npc.topic.windhowl_guard.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowl_guard.1.0}",
                      "${npc.topic_keyword.windhowl_guard.1.1}",
                      "${npc.topic_keyword.windhowl_guard.1.2}"),
                  "${npc.topic.windhowl_guard.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.windhowl_guard.2.0}"),
                  "${npc.topic.windhowl_guard.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowl_guard.3.0}",
                      "${npc.topic_keyword.windhowl_guard.3.1}",
                      "${npc.topic_keyword.windhowl_guard.3.2}"),
                  "${npc.topic.windhowl_guard.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.windhowl_guard.4.0}",
                      "${npc.topic_keyword.windhowl_guard.4.1}",
                      "${npc.topic_keyword.windhowl_guard.4.2}",
                      "${npc.topic_keyword.windhowl_guard.4.3}",
                      "${npc.topic_keyword.windhowl_guard.4.4}"),
                  "${npc.topic.windhowl_guard.4}",
                  List.of())),
          "Guard_Three",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 310, 65535, "1d29+21"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_USER_IS_A_TRAITOR") >= 3) {

          c.sayKey("npc.eraka.traitor");

          c.npc().provoke();

        } else c.sayKey("npc.windhowl.guard.welcome");
      }
    };
  }

  public WindHowl_Guard(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
