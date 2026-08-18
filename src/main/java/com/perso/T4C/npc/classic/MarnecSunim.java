package com.perso.T4C.npc.classic;

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

public final class MarnecSunim extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MarnecSunim";

  public static final String DISPLAY_NAME = "${npc.marnecsunim}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1")),
          0,
          List.of(),
          "${npc.welcome.marnecsunim}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marnecsunim.0.0}",
                      "${npc.topic_keyword.marnecsunim.0.1}"),
                  "${npc.topic.marnecsunim.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.1.0}"),
                  "${npc.topic.marnecsunim.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.2.0}"),
                  "${npc.topic.marnecsunim.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marnecsunim.3.0}",
                      "${npc.topic_keyword.marnecsunim.3.1}",
                      "${npc.topic_keyword.marnecsunim.3.2}"),
                  "${npc.topic.marnecsunim.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marnecsunim.4.0}",
                      "${npc.topic_keyword.marnecsunim.4.1}",
                      "${npc.topic_keyword.marnecsunim.4.2}"),
                  "${npc.topic.marnecsunim.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.5.0}"),
                  "${npc.topic.marnecsunim.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.6.0}"),
                  "${npc.topic.marnecsunim.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.7.0}"),
                  "${npc.topic.marnecsunim.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.8.0}"),
                  "${npc.topic.marnecsunim.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.9.0}"),
                  "${npc.topic.marnecsunim.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.10.0}"),
                  "${npc.topic.marnecsunim.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.11.0}"),
                  "${npc.topic.marnecsunim.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.12.0}"),
                  "${npc.topic.marnecsunim.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.13.0}"),
                  "${npc.topic.marnecsunim.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.14.0}"),
                  "${npc.topic.marnecsunim.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marnecsunim.15.0}"),
                  "${npc.topic.marnecsunim.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marnecsunim.16.0}",
                      "${npc.topic_keyword.marnecsunim.16.1}",
                      "${npc.topic_keyword.marnecsunim.16.2}",
                      "${npc.topic_keyword.marnecsunim.16.3}",
                      "${npc.topic_keyword.marnecsunim.16.4}"),
                  "${npc.topic.marnecsunim.16}",
                  List.of())),
          "MarnecSunimNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.markam.attacked." + (int) (Math.random() * 2));

        c.fleeFromPlayer();
      }
    };
  }

  public MarnecSunim(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
