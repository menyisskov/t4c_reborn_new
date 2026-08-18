package com.perso.T4C.npc.arakas;

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
import java.util.List;

public final class Mirandul extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Mirandul";

  public static final String DISPLAY_NAME = "${npc.mirandul}";

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
              new NpcSpec.Part(BodyPart.HEAD, "PupLeatherHelm"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.mirandul}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mirandul.0.0}", "${npc.topic_keyword.mirandul.0.1}"),
                  "${npc.topic.mirandul.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirandul.1.0}",
                      "${npc.topic_keyword.mirandul.1.1}",
                      "${npc.topic_keyword.mirandul.1.2}"),
                  "${npc.topic.mirandul.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mirandul.2.0}"),
                  "${npc.topic.mirandul.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirandul.3.0}",
                      "${npc.topic_keyword.mirandul.3.1}",
                      "${npc.topic_keyword.mirandul.3.2}"),
                  "${npc.topic.mirandul.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mirandul.4.0}"),
                  "${npc.topic.mirandul.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mirandul.5.0}"),
                  "${npc.topic.mirandul.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mirandul.6.0}"),
                  "${npc.topic.mirandul.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirandul.7.0}",
                      "${npc.topic_keyword.mirandul.7.1}",
                      "${npc.topic_keyword.mirandul.7.2}",
                      "${npc.topic_keyword.mirandul.7.3}"),
                  "${npc.topic.mirandul.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirandul.8.0}",
                      "${npc.topic_keyword.mirandul.8.1}",
                      "${npc.topic_keyword.mirandul.8.2}",
                      "${npc.topic_keyword.mirandul.8.3}",
                      "${npc.topic_keyword.mirandul.8.4}"),
                  "${npc.topic.mirandul.8}",
                  List.of())),
          "Male_Brigand",
          new NpcSpec.CombatProfile(100, 1000000, 65, 65, 65, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public Mirandul(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
