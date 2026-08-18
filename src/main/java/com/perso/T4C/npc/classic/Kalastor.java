package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.TrainingBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Kalastor extends ScriptedNpc {

  public static final String ID = "Kalastor";

  public static final String DISPLAY_NAME = "${npc.kalastor}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger")),
          0,
          List.of(),
          "${npc.welcome.kalastor}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.0.0}", "${npc.topic_keyword.kalastor.0.1}"),
                  "${npc.topic.kalastor.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.1.0}"),
                  "${npc.topic.kalastor.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kalastor.2.0}",
                      "${npc.topic_keyword.kalastor.2.1}",
                      "${npc.topic_keyword.kalastor.2.2}"),
                  "${npc.topic.kalastor.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.3.0}"),
                  "${npc.topic.kalastor.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.4.0}", "${npc.topic_keyword.kalastor.4.1}"),
                  "${npc.topic.kalastor.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.5.0}"),
                  "${npc.topic.kalastor.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.6.0}"),
                  "${npc.topic.kalastor.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.7.0}"),
                  "${npc.topic.kalastor.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.8.0}"),
                  "${npc.topic.kalastor.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.9.0}"),
                  "${npc.topic.kalastor.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.10.0}"),
                  "${npc.topic.kalastor.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kalastor.11.0}", "${npc.topic_keyword.kalastor.11.1}"),
                  "${npc.topic.kalastor.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.12.0}"),
                  "${npc.topic.kalastor.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.13.0}"),
                  "${npc.topic.kalastor.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kalastor.14.0}", "${npc.topic_keyword.kalastor.14.1}"),
                  "${npc.topic.kalastor.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kalastor.15.0}", "${npc.topic_keyword.kalastor.15.1}"),
                  "${npc.topic.kalastor.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.16.0}"),
                  "${npc.topic.kalastor.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kalastor.17.0}", "${npc.topic_keyword.kalastor.17.1}"),
                  "${npc.topic.kalastor.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.18.0}"),
                  "${npc.topic.kalastor.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.19.0}"),
                  "${npc.topic.kalastor.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.20.0}"),
                  "${npc.topic.kalastor.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kalastor.21.0}", "${npc.topic_keyword.kalastor.21.1}"),
                  "${npc.topic.kalastor.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.22.0}"),
                  "${npc.topic.kalastor.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalastor.23.0}"),
                  "${npc.topic.kalastor.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kalastor.24.0}",
                      "${npc.topic_keyword.kalastor.24.1}",
                      "${npc.topic_keyword.kalastor.24.2}",
                      "${npc.topic_keyword.kalastor.24.3}"),
                  "${npc.topic.kalastor.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kalastor.25.0}",
                      "${npc.topic_keyword.kalastor.25.1}",
                      "${npc.topic_keyword.kalastor.25.2}",
                      "${npc.topic_keyword.kalastor.25.3}",
                      "${npc.topic_keyword.kalastor.25.4}"),
                  "${npc.topic.kalastor.25}",
                  List.of())),
          "KalastorNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new TrainingBehavior(
        false,
        List.of(
            new LearnScreen.TrainingOffer("peek", 100, 25, false),
            new LearnScreen.TrainingOffer("dodge", 5000, 10, false),
            new LearnScreen.TrainingOffer("archery", 5000, 15, false)));
  }

  public Kalastor(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
