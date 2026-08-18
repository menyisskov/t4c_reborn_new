package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.TrainingAndFleeBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Ortanalas extends ScriptedNpc {

  public static final String ID = "Ortanalas";

  public static final String DISPLAY_NAME = "${npc.ortanalas}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.ortanalas}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ortanalas.0.0}", "${npc.topic_keyword.ortanalas.0.1}"),
                  "${npc.topic.ortanalas.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ortanalas.1.0}",
                      "${npc.topic_keyword.ortanalas.1.1}",
                      "${npc.topic_keyword.ortanalas.1.2}"),
                  "${npc.topic.ortanalas.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ortanalas.2.0}", "${npc.topic_keyword.ortanalas.2.1}"),
                  "${npc.topic.ortanalas.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ortanalas.3.0}",
                      "${npc.topic_keyword.ortanalas.3.1}",
                      "${npc.topic_keyword.ortanalas.3.2}"),
                  "${npc.topic.ortanalas.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ortanalas.4.0}"),
                  "${npc.topic.ortanalas.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ortanalas.5.0}"),
                  "${npc.topic.ortanalas.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ortanalas.6.0}", "${npc.topic_keyword.ortanalas.6.1}"),
                  "${npc.topic.ortanalas.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ortanalas.7.0}",
                      "${npc.topic_keyword.ortanalas.7.1}",
                      "${npc.topic_keyword.ortanalas.7.2}",
                      "${npc.topic_keyword.ortanalas.7.3}",
                      "${npc.topic_keyword.ortanalas.7.4}"),
                  "${npc.topic.ortanalas.7}",
                  List.of())),
          "OrtanalasNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new TrainingAndFleeBehavior(
        false,
        List.of(
            new LearnScreen.TrainingOffer("attack", 5000, 10, false),
            new LearnScreen.TrainingOffer("archery", 5000, 15, false),
            new LearnScreen.TrainingOffer("stun_blow", 100, 20, false),
            new LearnScreen.TrainingOffer("powerful_blow", 100, 50, false)),
        "npc.markam.attacked.");
  }

  public Ortanalas(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
