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

@Spawn(
    type = "DantalirSongweaver",
    x = 190,
    y = 1585,
    z = 1,
    stationary = false,
    aggressive = false)
public final class DantalirSongweaver extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DantalirSongweaver";

  public static final String DISPLAY_NAME = "${npc.dantalirsongweaver}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupStuddedBodyArmor"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupFlail"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.dantalirsongweaver}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dantalirsongweaver.0.0}",
                      "${npc.topic_keyword.dantalirsongweaver.0.1}"),
                  "${npc.topic.dantalirsongweaver.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dantalirsongweaver.1.0}"),
                  "${npc.topic.dantalirsongweaver.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dantalirsongweaver.2.0}"),
                  "${npc.topic.dantalirsongweaver.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dantalirsongweaver.3.0}"),
                  "${npc.topic.dantalirsongweaver.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dantalirsongweaver.4.0}",
                      "${npc.topic_keyword.dantalirsongweaver.4.1}",
                      "${npc.topic_keyword.dantalirsongweaver.4.2}"),
                  "${npc.topic.dantalirsongweaver.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dantalirsongweaver.5.0}",
                      "${npc.topic_keyword.dantalirsongweaver.5.1}"),
                  "${npc.topic.dantalirsongweaver.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dantalirsongweaver.6.0}"),
                  "${npc.topic.dantalirsongweaver.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dantalirsongweaver.7.0}"),
                  "${npc.topic.dantalirsongweaver.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dantalirsongweaver.8.0}"),
                  "${npc.topic.dantalirsongweaver.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dantalirsongweaver.9.0}",
                      "${npc.topic_keyword.dantalirsongweaver.9.1}"),
                  "${npc.topic.dantalirsongweaver.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dantalirsongweaver.10.0}",
                      "${npc.topic_keyword.dantalirsongweaver.10.1}"),
                  "${npc.topic.dantalirsongweaver.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dantalirsongweaver.11.0}",
                      "${npc.topic_keyword.dantalirsongweaver.11.1}",
                      "${npc.topic_keyword.dantalirsongweaver.11.2}",
                      "${npc.topic_keyword.dantalirsongweaver.11.3}"),
                  "${npc.topic.dantalirsongweaver.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dantalirsongweaver.12.0}",
                      "${npc.topic_keyword.dantalirsongweaver.12.1}",
                      "${npc.topic_keyword.dantalirsongweaver.12.2}",
                      "${npc.topic_keyword.dantalirsongweaver.12.3}",
                      "${npc.topic_keyword.dantalirsongweaver.12.4}"),
                  "${npc.topic.dantalirsongweaver.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dantalirsongweaver.13.0}",
                      "${npc.topic_keyword.dantalirsongweaver.13.1}"),
                  "${npc.topic.dantalirsongweaver.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dantalirsongweaver.14.0}"),
                  "${npc.topic.dantalirsongweaver.14}",
                  List.of())),
          "DantalirSongweaverNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public DantalirSongweaver(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
