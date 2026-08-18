package com.perso.T4C.npc.windhowl;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GeorgeTheIIIrd extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "GeorgeTheIIIrd";

  public static final String DISPLAY_NAME = "${npc.georgetheiiird}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.georgetheiiird}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.georgetheiiird.0.0}",
                      "${npc.topic_keyword.georgetheiiird.0.1}"),
                  "${npc.topic.georgetheiiird.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.georgetheiiird.1.0}",
                      "${npc.topic_keyword.georgetheiiird.1.1}",
                      "${npc.topic_keyword.georgetheiiird.1.2}"),
                  "${npc.topic.georgetheiiird.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.georgetheiiird.2.0}",
                      "${npc.topic_keyword.georgetheiiird.2.1}",
                      "${npc.topic_keyword.georgetheiiird.2.2}"),
                  "${npc.topic.georgetheiiird.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.georgetheiiird.3.0}"),
                  "${npc.topic.georgetheiiird.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.georgetheiiird.4.0}"),
                  "${npc.topic.georgetheiiird.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.georgetheiiird.5.0}",
                      "${npc.topic_keyword.georgetheiiird.5.1}",
                      "${npc.topic_keyword.georgetheiiird.5.2}",
                      "${npc.topic_keyword.georgetheiiird.5.3}",
                      "${npc.topic_keyword.georgetheiiird.5.4}"),
                  "${npc.topic.georgetheiiird.5}",
                  List.of())),
          "Guard_One",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 310, 65535, "1d29+21"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_USER_IS_A_TRAITOR") >= 3) {

          c.sayKey("npc.eraka.traitor");

          c.npc().provoke();

        } else c.sayKey("npc.george.welcome");
      }
    };
  }

  public GeorgeTheIIIrd(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
