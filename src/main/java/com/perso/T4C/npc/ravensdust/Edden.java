package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Edden extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Edden";

  public static final String DISPLAY_NAME = "${npc.edden}";

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
          "${npc.welcome.edden}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edden.0.0}"), "${npc.topic.edden.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edden.1.0}", "${npc.topic_keyword.edden.1.1}"),
                  "${npc.topic.edden.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edden.2.0}"), "${npc.topic.edden.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.edden.3.0}",
                      "${npc.topic_keyword.edden.3.1}",
                      "${npc.topic_keyword.edden.3.2}"),
                  "${npc.topic.edden.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edden.4.0}"), "${npc.topic.edden.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edden.5.0}"), "${npc.topic.edden.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edden.6.0}"), "${npc.topic.edden.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edden.7.0}", "${npc.topic_keyword.edden.7.1}"),
                  "${npc.topic.edden.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edden.8.0}"), "${npc.topic.edden.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.edden.9.0}",
                      "${npc.topic_keyword.edden.9.1}",
                      "${npc.topic_keyword.edden.9.2}",
                      "${npc.topic_keyword.edden.9.3}",
                      "${npc.topic_keyword.edden.9.4}"),
                  "${npc.topic.edden.9}",
                  List.of())),
          "Royal_Guard",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        long now = System.currentTimeMillis() / 1000L;

        if (c.globalFlag("GLOBAL_BANK_HAS_BEEN_ROBBED") > now) {

          if (c.globalFlag("GLOBAL_BANK_HAS_BEEN_ROBBED_BY") == c.flag("__FLAG_SHADEEN_PLAYER_B")) {

            while (c.hasItem("light_healing_potion")) c.takeItem("light_healing_potion");

            while (c.hasItem("healing_potion")) c.takeItem("healing_potion");

            c.sayKey("npc.edden.thief");

            c.teleport(209, 2338, 1);

          } else
            c.sayKey(
                c.player().getGender().equalsIgnoreCase("FEMALE")
                    ? "npc.edden.robbed.female"
                    : "npc.edden.robbed.male");

        } else {

          c.flag("__FLAG_THIEF", 0);

          c.sayKey("npc.edden.welcome");
        }
      }
    };
  }

  public Edden(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
