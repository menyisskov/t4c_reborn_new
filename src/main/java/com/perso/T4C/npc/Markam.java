package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Markam", x = 2954, y = 1102, z = 0, stationary = false, aggressive = false)
public final class Markam extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Markam";

  public static final String DISPLAY_NAME = "${npc.markam}";

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
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.markam}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.0.0}", "${npc.topic_keyword.markam.0.1}"),
                  "${npc.topic.markam.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.markam.1.0}",
                      "${npc.topic_keyword.markam.1.1}",
                      "${npc.topic_keyword.markam.1.2}"),
                  "${npc.topic.markam.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.2.0}"), "${npc.topic.markam.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.markam.3.0}",
                      "${npc.topic_keyword.markam.3.1}",
                      "${npc.topic_keyword.markam.3.2}"),
                  "${npc.topic.markam.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.4.0}", "${npc.topic_keyword.markam.4.1}"),
                  "${npc.topic.markam.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.5.0}"), "${npc.topic.markam.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.6.0}"), "${npc.topic.markam.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.7.0}"), "${npc.topic.markam.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.8.0}"), "${npc.topic.markam.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.9.0}"), "${npc.topic.markam.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.10.0}"), "${npc.topic.markam.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.11.0}"), "${npc.topic.markam.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.12.0}"), "${npc.topic.markam.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.13.0}"), "${npc.topic.markam.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.14.0}"), "${npc.topic.markam.14}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.15.0}"), "${npc.topic.markam.15}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.16.0}"), "${npc.topic.markam.16}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.17.0}"), "${npc.topic.markam.17}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.18.0}"), "${npc.topic.markam.18}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.markam.19.0}"), "${npc.topic.markam.19}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.markam.20.0}",
                      "${npc.topic_keyword.markam.20.1}",
                      "${npc.topic_keyword.markam.20.2}",
                      "${npc.topic_keyword.markam.20.3}",
                      "${npc.topic_keyword.markam.20.4}"),
                  "${npc.topic.markam.20}",
                  List.of())),
          "MarkamNPC",
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

  public Markam(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
