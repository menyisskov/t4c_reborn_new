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

public final class Moonrock extends ScriptedNpc {

  public static final String ID = "Moonrock";

  public static final String DISPLAY_NAME = "${npc.moonrock}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupWoodenStaff")),
          0,
          List.of(),
          "${npc.welcome.moonrock}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.0.0}", "${npc.topic_keyword.moonrock.0.1}"),
                  "${npc.topic.moonrock.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.1.0}", "${npc.topic_keyword.moonrock.1.1}"),
                  "${npc.topic.moonrock.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.2.0}"),
                  "${npc.topic.moonrock.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.3.0}"),
                  "${npc.topic.moonrock.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.4.0}", "${npc.topic_keyword.moonrock.4.1}"),
                  "${npc.topic.moonrock.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.5.0}"),
                  "${npc.topic.moonrock.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.6.0}"),
                  "${npc.topic.moonrock.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.7.0}"),
                  "${npc.topic.moonrock.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.moonrock.8.0}",
                      "${npc.topic_keyword.moonrock.8.1}",
                      "${npc.topic_keyword.moonrock.8.2}"),
                  "${npc.topic.moonrock.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.9.0}", "${npc.topic_keyword.moonrock.9.1}"),
                  "${npc.topic.moonrock.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.moonrock.10.0}", "${npc.topic_keyword.moonrock.10.1}"),
                  "${npc.topic.moonrock.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.11.0}"),
                  "${npc.topic.moonrock.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.12.0}"),
                  "${npc.topic.moonrock.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.13.0}"),
                  "${npc.topic.moonrock.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.moonrock.14.0}",
                      "${npc.topic_keyword.moonrock.14.1}",
                      "${npc.topic_keyword.moonrock.14.2}"),
                  "${npc.topic.moonrock.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.15.0}"),
                  "${npc.topic.moonrock.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.16.0}"),
                  "${npc.topic.moonrock.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.17.0}"),
                  "${npc.topic.moonrock.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.moonrock.18.0}"),
                  "${npc.topic.moonrock.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.moonrock.19.0}",
                      "${npc.topic_keyword.moonrock.19.1}",
                      "${npc.topic_keyword.moonrock.19.2}"),
                  "${npc.topic.moonrock.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.moonrock.20.0}",
                      "${npc.topic_keyword.moonrock.20.1}",
                      "${npc.topic_keyword.moonrock.20.2}"),
                  "${npc.topic.moonrock.20}",
                  List.of())),
          "MoonrockNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new TrainingAndFleeBehavior(
        true,
        List.of(
            new LearnScreen.TrainingOffer("heal_light", 9, 897, true),
            new LearnScreen.TrainingOffer("cure_poison", 6, 1825, true),
            new LearnScreen.TrainingOffer("protection", 7, 3712, true),
            new LearnScreen.TrainingOffer("heal_serious", 14, 8177, true)),
        "npc.markam.attacked.");
  }

  public Moonrock(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
