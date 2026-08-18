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

public final class Sigfried extends ScriptedNpc {

  public static final String ID = "Sigfried";

  public static final String DISPLAY_NAME = "${npc.sigfried}";

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
          "${npc.welcome.sigfried}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.0.0}", "${npc.topic_keyword.sigfried.0.1}"),
                  "${npc.topic.sigfried.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.sigfried.1.0}",
                      "${npc.topic_keyword.sigfried.1.1}",
                      "${npc.topic_keyword.sigfried.1.2}"),
                  "${npc.topic.sigfried.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.2.0}"),
                  "${npc.topic.sigfried.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.3.0}", "${npc.topic_keyword.sigfried.3.1}"),
                  "${npc.topic.sigfried.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.4.0}"),
                  "${npc.topic.sigfried.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.sigfried.5.0}",
                      "${npc.topic_keyword.sigfried.5.1}",
                      "${npc.topic_keyword.sigfried.5.2}"),
                  "${npc.topic.sigfried.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.6.0}"),
                  "${npc.topic.sigfried.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.7.0}", "${npc.topic_keyword.sigfried.7.1}"),
                  "${npc.topic.sigfried.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.8.0}"),
                  "${npc.topic.sigfried.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.9.0}"),
                  "${npc.topic.sigfried.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.10.0}"),
                  "${npc.topic.sigfried.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.11.0}"),
                  "${npc.topic.sigfried.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sigfried.12.0}"),
                  "${npc.topic.sigfried.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.sigfried.13.0}",
                      "${npc.topic_keyword.sigfried.13.1}",
                      "${npc.topic_keyword.sigfried.13.2}",
                      "${npc.topic_keyword.sigfried.13.3}",
                      "${npc.topic_keyword.sigfried.13.4}"),
                  "${npc.topic.sigfried.13}",
                  List.of())),
          "ShopKeeper",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.sigfried.attacked." + (int) (Math.random() * 2));

        c.fleeFromPlayer();
      }
    };
  }

  public Sigfried(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
