package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.BankBehavior;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Mithrand", x = 2961, y = 1093, z = 0, stationary = false, aggressive = false)
public final class Mithrand extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Mithrand";

  public static final String DISPLAY_NAME = "${npc.mithrand}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.mithrand}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.0.0}", "${npc.topic_keyword.mithrand.0.1}"),
                  "${npc.topic.mithrand.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithrand.1.0}",
                      "${npc.topic_keyword.mithrand.1.1}",
                      "${npc.topic_keyword.mithrand.1.2}"),
                  "${npc.topic.mithrand.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.2.0}"),
                  "${npc.topic.mithrand.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.3.0}", "${npc.topic_keyword.mithrand.3.1}"),
                  "${npc.topic.mithrand.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.4.0}"),
                  "${npc.topic.mithrand.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.5.0}"),
                  "${npc.topic.mithrand.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.6.0}"),
                  "${npc.topic.mithrand.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.7.0}"),
                  "${npc.topic.mithrand.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.8.0}"),
                  "${npc.topic.mithrand.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.9.0}"),
                  "${npc.topic.mithrand.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.10.0}"),
                  "${npc.topic.mithrand.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.11.0}"),
                  "${npc.topic.mithrand.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.12.0}"),
                  "${npc.topic.mithrand.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.13.0}"),
                  "${npc.topic.mithrand.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.14.0}"),
                  "${npc.topic.mithrand.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.15.0}"),
                  "${npc.topic.mithrand.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.16.0}"),
                  "${npc.topic.mithrand.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.17.0}"),
                  "${npc.topic.mithrand.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.18.0}"),
                  "${npc.topic.mithrand.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.19.0}"),
                  "${npc.topic.mithrand.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.20.0}"),
                  "${npc.topic.mithrand.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithrand.21.0}"),
                  "${npc.topic.mithrand.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithrand.22.0}",
                      "${npc.topic_keyword.mithrand.22.1}",
                      "${npc.topic_keyword.mithrand.22.2}",
                      "${npc.topic_keyword.mithrand.22.3}",
                      "${npc.topic_keyword.mithrand.22.4}"),
                  "${npc.topic.mithrand.22}",
                  List.of())),
          "Nobleman",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new BankBehavior("__FLAG_BANK_OF_WINDHOWL") {

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.mithrand.attacked." + (int) (Math.random() * 2));

        c.fleeFromPlayer();
      }
    };
  }

  public Mithrand(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
