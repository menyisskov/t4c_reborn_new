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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Lothan", x = 2979, y = 968, z = 0, stationary = false, aggressive = false)
public final class Lothan extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Lothan";

  public static final String DISPLAY_NAME = "${npc.lothan}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.lothan}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.0.0}", "${npc.topic_keyword.lothan.0.1}"),
                  "${npc.topic.lothan.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lothan.1.0}",
                      "${npc.topic_keyword.lothan.1.1}",
                      "${npc.topic_keyword.lothan.1.2}",
                      "${npc.topic_keyword.lothan.1.3}"),
                  "${npc.topic.lothan.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.2.0}"), "${npc.topic.lothan.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lothan.3.0}",
                      "${npc.topic_keyword.lothan.3.1}",
                      "${npc.topic_keyword.lothan.3.2}"),
                  "${npc.topic.lothan.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.4.0}", "${npc.topic_keyword.lothan.4.1}"),
                  "${npc.topic.lothan.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.5.0}"), "${npc.topic.lothan.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.6.0}"), "${npc.topic.lothan.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.7.0}", "${npc.topic_keyword.lothan.7.1}"),
                  "${npc.topic.lothan.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.8.0}", "${npc.topic_keyword.lothan.8.1}"),
                  "${npc.topic.lothan.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.9.0}"), "${npc.topic.lothan.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.10.0}"), "${npc.topic.lothan.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.11.0}"), "${npc.topic.lothan.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.12.0}"), "${npc.topic.lothan.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.13.0}"), "${npc.topic.lothan.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.14.0}", "${npc.topic_keyword.lothan.14.1}"),
                  "${npc.topic.lothan.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.15.0}"), "${npc.topic.lothan.15}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.16.0}"), "${npc.topic.lothan.16}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.17.0}"), "${npc.topic.lothan.17}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.18.0}"), "${npc.topic.lothan.18}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.19.0}"), "${npc.topic.lothan.19}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.20.0}"), "${npc.topic.lothan.20}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.21.0}"), "${npc.topic.lothan.21}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.22.0}"), "${npc.topic.lothan.22}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.23.0}"), "${npc.topic.lothan.23}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.24.0}"), "${npc.topic.lothan.24}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lothan.25.0}"), "${npc.topic.lothan.25}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lothan.26.0}",
                      "${npc.topic_keyword.lothan.26.1}",
                      "${npc.topic_keyword.lothan.26.2}"),
                  "${npc.topic.lothan.26}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lothan.27.0}",
                      "${npc.topic_keyword.lothan.27.1}",
                      "${npc.topic_keyword.lothan.27.2}",
                      "${npc.topic_keyword.lothan.27.3}",
                      "${npc.topic_keyword.lothan.27.4}"),
                  "${npc.topic.lothan.27}",
                  List.of())),
          "Mage",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new TrainingBehavior(
        true,
        List.of(
            new LearnScreen.TrainingOffer("poison_arrow", 10, 12937, true),
            new LearnScreen.TrainingOffer("poison", 7, 3017, true),
            new LearnScreen.TrainingOffer("ice_shard", 8, 4473, true)));
  }

  public Lothan(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
