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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Isulgur", x = 2970, y = 1096, z = 0, stationary = false, aggressive = false)
public final class Isulgur extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Isulgur";

  public static final String DISPLAY_NAME = "${npc.isulgur}";

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
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.isulgur}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.0.0}", "${npc.topic_keyword.isulgur.0.1}"),
                  "${npc.topic.isulgur.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.isulgur.1.0}",
                      "${npc.topic_keyword.isulgur.1.1}",
                      "${npc.topic_keyword.isulgur.1.2}"),
                  "${npc.topic.isulgur.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.2.0}"), "${npc.topic.isulgur.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.isulgur.3.0}",
                      "${npc.topic_keyword.isulgur.3.1}",
                      "${npc.topic_keyword.isulgur.3.2}"),
                  "${npc.topic.isulgur.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.4.0}", "${npc.topic_keyword.isulgur.4.1}"),
                  "${npc.topic.isulgur.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.5.0}"), "${npc.topic.isulgur.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.6.0}"), "${npc.topic.isulgur.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.7.0}"), "${npc.topic.isulgur.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.8.0}"), "${npc.topic.isulgur.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.9.0}"), "${npc.topic.isulgur.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.10.0}"),
                  "${npc.topic.isulgur.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.11.0}"),
                  "${npc.topic.isulgur.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.12.0}"),
                  "${npc.topic.isulgur.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.13.0}"),
                  "${npc.topic.isulgur.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.14.0}"),
                  "${npc.topic.isulgur.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.15.0}"),
                  "${npc.topic.isulgur.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.16.0}"),
                  "${npc.topic.isulgur.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.17.0}"),
                  "${npc.topic.isulgur.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.18.0}"),
                  "${npc.topic.isulgur.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.isulgur.19.0}"),
                  "${npc.topic.isulgur.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.isulgur.20.0}",
                      "${npc.topic_keyword.isulgur.20.1}",
                      "${npc.topic_keyword.isulgur.20.2}",
                      "${npc.topic_keyword.isulgur.20.3}",
                      "${npc.topic_keyword.isulgur.20.4}"),
                  "${npc.topic.isulgur.20}",
                  List.of())),
          "IsulgurNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.isulgur.attacked." + (int) (Math.random() * 2));

        c.fleeFromPlayer();
      }
    };
  }

  public Isulgur(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
