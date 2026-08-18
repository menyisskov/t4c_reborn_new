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

public final class MorindinArrowmist extends ScriptedNpc {

  public static final String ID = "MorindinArrowmist";

  public static final String DISPLAY_NAME = "${npc.morindinarrowmist}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.HEAD, "PupElvenHat"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleAxe"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.morindinarrowmist}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.morindinarrowmist.0.0}",
                      "${npc.topic_keyword.morindinarrowmist.0.1}"),
                  "${npc.topic.morindinarrowmist.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.morindinarrowmist.1.0}",
                      "${npc.topic_keyword.morindinarrowmist.1.1}"),
                  "${npc.topic.morindinarrowmist.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.2.0}"),
                  "${npc.topic.morindinarrowmist.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.3.0}"),
                  "${npc.topic.morindinarrowmist.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.4.0}"),
                  "${npc.topic.morindinarrowmist.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.5.0}"),
                  "${npc.topic.morindinarrowmist.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.morindinarrowmist.6.0}",
                      "${npc.topic_keyword.morindinarrowmist.6.1}"),
                  "${npc.topic.morindinarrowmist.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.7.0}"),
                  "${npc.topic.morindinarrowmist.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.8.0}"),
                  "${npc.topic.morindinarrowmist.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.9.0}"),
                  "${npc.topic.morindinarrowmist.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.morindinarrowmist.10.0}",
                      "${npc.topic_keyword.morindinarrowmist.10.1}",
                      "${npc.topic_keyword.morindinarrowmist.10.2}"),
                  "${npc.topic.morindinarrowmist.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.11.0}"),
                  "${npc.topic.morindinarrowmist.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.12.0}"),
                  "${npc.topic.morindinarrowmist.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.morindinarrowmist.13.0}",
                      "${npc.topic_keyword.morindinarrowmist.13.1}"),
                  "${npc.topic.morindinarrowmist.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.morindinarrowmist.14.0}"),
                  "${npc.topic.morindinarrowmist.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.morindinarrowmist.15.0}",
                      "${npc.topic_keyword.morindinarrowmist.15.1}",
                      "${npc.topic_keyword.morindinarrowmist.15.2}",
                      "${npc.topic_keyword.morindinarrowmist.15.3}"),
                  "${npc.topic.morindinarrowmist.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.morindinarrowmist.16.0}",
                      "${npc.topic_keyword.morindinarrowmist.16.1}",
                      "${npc.topic_keyword.morindinarrowmist.16.2}",
                      "${npc.topic_keyword.morindinarrowmist.16.3}",
                      "${npc.topic_keyword.morindinarrowmist.16.4}"),
                  "${npc.topic.morindinarrowmist.16}",
                  List.of())),
          "Hunter",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int r = (int) (Math.random() * 5);

        if (r < 4) c.shoutKey("npc.morindin.attack." + r);
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int r = (int) (Math.random() * 5);

        if (r < 4) c.shoutKey("npc.morindin.attacked." + r);
      }
    };
  }

  public MorindinArrowmist(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
