package com.perso.T4C.npc.ravensdust;

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

public final class Rawlin extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Rawlin";

  public static final String DISPLAY_NAME = "${npc.rawlin}";

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
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.rawlin}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rawlin.0.0}", "${npc.topic_keyword.rawlin.0.1}"),
                  "${npc.topic.rawlin.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rawlin.1.0}"), "${npc.topic.rawlin.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rawlin.2.0}", "${npc.topic_keyword.rawlin.2.1}"),
                  "${npc.topic.rawlin.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rawlin.3.0}",
                      "${npc.topic_keyword.rawlin.3.1}",
                      "${npc.topic_keyword.rawlin.3.2}"),
                  "${npc.topic.rawlin.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rawlin.4.0}"), "${npc.topic.rawlin.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rawlin.5.0}"), "${npc.topic.rawlin.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rawlin.6.0}"), "${npc.topic.rawlin.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rawlin.7.0}"), "${npc.topic.rawlin.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rawlin.8.0}"), "${npc.topic.rawlin.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rawlin.9.0}",
                      "${npc.topic_keyword.rawlin.9.1}",
                      "${npc.topic_keyword.rawlin.9.2}",
                      "${npc.topic_keyword.rawlin.9.3}"),
                  "${npc.topic.rawlin.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rawlin.10.0}"), "${npc.topic.rawlin.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rawlin.11.0}",
                      "${npc.topic_keyword.rawlin.11.1}",
                      "${npc.topic_keyword.rawlin.11.2}",
                      "${npc.topic_keyword.rawlin.11.3}",
                      "${npc.topic_keyword.rawlin.11.4}"),
                  "${npc.topic.rawlin.11}",
                  List.of())),
          "RawlinNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (Math.random() < .08)
          c.shoutKey(Math.random() < .5 ? "npc.rawlin.attack.forge" : "npc.rawlin.attack.bleed");
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (Math.random() < .08)
          c.shoutKey(Math.random() < .5 ? "npc.rawlin.attacked.ouch" : "npc.rawlin.attacked.done");
      }
    };
  }

  public Rawlin(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
