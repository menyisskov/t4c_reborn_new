package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Lyria", x = 2181, y = 1276, z = 0, stationary = false, aggressive = false)
public final class Lyria extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Lyria";

  public static final String DISPLAY_NAME = "${npc.lyria}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoLeatherArms"),
              new NpcSpec.Part(BodyPart.BACK, "WoLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "WoLeatherLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "WoChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupNormalSword"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "WoLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "WoLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.lyria}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lyria.0.0}", "${npc.topic_keyword.lyria.0.1}"),
                  "${npc.topic.lyria.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lyria.1.0}",
                      "${npc.topic_keyword.lyria.1.1}",
                      "${npc.topic_keyword.lyria.1.2}"),
                  "${npc.topic.lyria.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lyria.2.0}"), "${npc.topic.lyria.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lyria.3.0}"), "${npc.topic.lyria.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lyria.4.0}", "${npc.topic_keyword.lyria.4.1}"),
                  "${npc.topic.lyria.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lyria.5.0}"), "${npc.topic.lyria.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lyria.6.0}"), "${npc.topic.lyria.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lyria.7.0}",
                      "${npc.topic_keyword.lyria.7.1}",
                      "${npc.topic_keyword.lyria.7.2}"),
                  "${npc.topic.lyria.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lyria.8.0}"), "${npc.topic.lyria.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lyria.9.0}"), "${npc.topic.lyria.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lyria.10.0}",
                      "${npc.topic_keyword.lyria.10.1}",
                      "${npc.topic_keyword.lyria.10.2}",
                      "${npc.topic_keyword.lyria.10.3}"),
                  "${npc.topic.lyria.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lyria.11.0}",
                      "${npc.topic_keyword.lyria.11.1}",
                      "${npc.topic_keyword.lyria.11.2}",
                      "${npc.topic_keyword.lyria.11.3}",
                      "${npc.topic_keyword.lyria.11.4}"),
                  "${npc.topic.lyria.11}",
                  List.of())),
          "LyriaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  public Lyria(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }
}
