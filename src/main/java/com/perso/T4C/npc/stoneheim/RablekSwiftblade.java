package com.perso.T4C.npc.stoneheim;

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

public final class RablekSwiftblade extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "RablekSwiftblade";

  public static final String DISPLAY_NAME = "${npc.rablekswiftblade}";

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
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.rablekswiftblade}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rablekswiftblade.0.0}",
                      "${npc.topic_keyword.rablekswiftblade.0.1}"),
                  "${npc.topic.rablekswiftblade.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rablekswiftblade.1.0}"),
                  "${npc.topic.rablekswiftblade.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rablekswiftblade.2.0}",
                      "${npc.topic_keyword.rablekswiftblade.2.1}"),
                  "${npc.topic.rablekswiftblade.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rablekswiftblade.3.0}",
                      "${npc.topic_keyword.rablekswiftblade.3.1}",
                      "${npc.topic_keyword.rablekswiftblade.3.2}"),
                  "${npc.topic.rablekswiftblade.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rablekswiftblade.4.0}",
                      "${npc.topic_keyword.rablekswiftblade.4.1}"),
                  "${npc.topic.rablekswiftblade.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rablekswiftblade.5.0}"),
                  "${npc.topic.rablekswiftblade.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rablekswiftblade.6.0}"),
                  "${npc.topic.rablekswiftblade.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rablekswiftblade.7.0}",
                      "${npc.topic_keyword.rablekswiftblade.7.1}"),
                  "${npc.topic.rablekswiftblade.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rablekswiftblade.8.0}",
                      "${npc.topic_keyword.rablekswiftblade.8.1}",
                      "${npc.topic_keyword.rablekswiftblade.8.2}",
                      "${npc.topic_keyword.rablekswiftblade.8.3}",
                      "${npc.topic_keyword.rablekswiftblade.8.4}"),
                  "${npc.topic.rablekswiftblade.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rablekswiftblade.9.0}",
                      "${npc.topic_keyword.rablekswiftblade.9.1}",
                      "${npc.topic_keyword.rablekswiftblade.9.2}",
                      "${npc.topic_keyword.rablekswiftblade.9.3}",
                      "${npc.topic_keyword.rablekswiftblade.9.4}"),
                  "${npc.topic.rablekswiftblade.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rablekswiftblade.10.0}"),
                  "${npc.topic.rablekswiftblade.10}",
                  List.of())),
          "RablekSwiftbladeNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public RablekSwiftblade(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
