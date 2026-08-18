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

@Spawn(type = "Iraltok", x = 2998, y = 952, z = 0, stationary = false, aggressive = false)
public final class Iraltok extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Iraltok";

  public static final String DISPLAY_NAME = "${npc.iraltok}";

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
          "${npc.welcome.iraltok}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.0.0}"), "${npc.topic.iraltok.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.1.0}"), "${npc.topic.iraltok.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.2.0}"), "${npc.topic.iraltok.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.3.0}"), "${npc.topic.iraltok.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.4.0}"), "${npc.topic.iraltok.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.5.0}"), "${npc.topic.iraltok.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.6.0}", "${npc.topic_keyword.iraltok.6.1}"),
                  "${npc.topic.iraltok.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iraltok.7.0}",
                      "${npc.topic_keyword.iraltok.7.1}",
                      "${npc.topic_keyword.iraltok.7.2}"),
                  "${npc.topic.iraltok.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iraltok.8.0}",
                      "${npc.topic_keyword.iraltok.8.1}",
                      "${npc.topic_keyword.iraltok.8.2}"),
                  "${npc.topic.iraltok.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.9.0}"), "${npc.topic.iraltok.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.10.0}"),
                  "${npc.topic.iraltok.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.11.0}"),
                  "${npc.topic.iraltok.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iraltok.12.0}",
                      "${npc.topic_keyword.iraltok.12.1}",
                      "${npc.topic_keyword.iraltok.12.2}"),
                  "${npc.topic.iraltok.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.13.0}"),
                  "${npc.topic.iraltok.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.14.0}"),
                  "${npc.topic.iraltok.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.15.0}"),
                  "${npc.topic.iraltok.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iraltok.16.0}"),
                  "${npc.topic.iraltok.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iraltok.17.0}",
                      "${npc.topic_keyword.iraltok.17.1}",
                      "${npc.topic_keyword.iraltok.17.2}"),
                  "${npc.topic.iraltok.17}",
                  List.of())),
          "Mage",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  private static final TrainingBehavior TRAINING =
      new TrainingBehavior(
          true,
          List.of(
              new LearnScreen.TrainingOffer("fire_dart", 5, 532, true),
              new LearnScreen.TrainingOffer("flaming_arrow", 8, 5300, true)));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        return TRAINING.onKeyword(c, text);
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.markam.attacked." + (int) (Math.random() * 2));

        c.fleeFromPlayer();
      }
    };
  }

  public Iraltok(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
