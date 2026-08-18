package com.perso.T4C.npc.windhowl;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "IroualKnowall", x = 1784, y = 1233, z = 0, stationary = false, aggressive = false)
public final class IroualKnowall extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "IroualKnowall";

  public static final String DISPLAY_NAME = "${npc.iroualknowall}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.iroualknowall}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iroualknowall.0.0}",
                      "${npc.topic_keyword.iroualknowall.0.1}"),
                  "${npc.topic.iroualknowall.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iroualknowall.1.0}",
                      "${npc.topic_keyword.iroualknowall.1.1}",
                      "${npc.topic_keyword.iroualknowall.1.2}"),
                  "${npc.topic.iroualknowall.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iroualknowall.2.0}",
                      "${npc.topic_keyword.iroualknowall.2.1}",
                      "${npc.topic_keyword.iroualknowall.2.2}"),
                  "${npc.topic.iroualknowall.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iroualknowall.3.0}"),
                  "${npc.topic.iroualknowall.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iroualknowall.4.0}"),
                  "${npc.topic.iroualknowall.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iroualknowall.5.0}"),
                  "${npc.topic.iroualknowall.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iroualknowall.6.0}"),
                  "${npc.topic.iroualknowall.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iroualknowall.7.0}"),
                  "${npc.topic.iroualknowall.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iroualknowall.8.0}",
                      "${npc.topic_keyword.iroualknowall.8.1}",
                      "${npc.topic_keyword.iroualknowall.8.2}",
                      "${npc.topic_keyword.iroualknowall.8.3}",
                      "${npc.topic_keyword.iroualknowall.8.4}"),
                  "${npc.topic.iroualknowall.8}",
                  List.of())),
          "IroualKnowallNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public IroualKnowall(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
