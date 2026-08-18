package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MeltarWinterstorm extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MeltarWinterstorm";

  public static final String DISPLAY_NAME = "${npc.meltarwinterstorm}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.meltarwinterstorm}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.0.0}",
                      "${npc.topic_keyword.meltarwinterstorm.0.1}"),
                  "${npc.topic.meltarwinterstorm.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.1.0}",
                      "${npc.topic_keyword.meltarwinterstorm.1.1}"),
                  "${npc.topic.meltarwinterstorm.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.2.0}",
                      "${npc.topic_keyword.meltarwinterstorm.2.1}",
                      "${npc.topic_keyword.meltarwinterstorm.2.2}"),
                  "${npc.topic.meltarwinterstorm.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.3.0}"),
                  "${npc.topic.meltarwinterstorm.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.4.0}",
                      "${npc.topic_keyword.meltarwinterstorm.4.1}",
                      "${npc.topic_keyword.meltarwinterstorm.4.2}",
                      "${npc.topic_keyword.meltarwinterstorm.4.3}"),
                  "${npc.topic.meltarwinterstorm.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.5.0}"),
                  "${npc.topic.meltarwinterstorm.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.6.0}"),
                  "${npc.topic.meltarwinterstorm.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.7.0}"),
                  "${npc.topic.meltarwinterstorm.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.8.0}"),
                  "${npc.topic.meltarwinterstorm.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.9.0}"),
                  "${npc.topic.meltarwinterstorm.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.10.0}",
                      "${npc.topic_keyword.meltarwinterstorm.10.1}"),
                  "${npc.topic.meltarwinterstorm.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.11.0}"),
                  "${npc.topic.meltarwinterstorm.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.12.0}",
                      "${npc.topic_keyword.meltarwinterstorm.12.1}",
                      "${npc.topic_keyword.meltarwinterstorm.12.2}",
                      "${npc.topic_keyword.meltarwinterstorm.12.3}",
                      "${npc.topic_keyword.meltarwinterstorm.12.4}"),
                  "${npc.topic.meltarwinterstorm.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.13.0}"),
                  "${npc.topic.meltarwinterstorm.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.14.0}",
                      "${npc.topic_keyword.meltarwinterstorm.14.1}"),
                  "${npc.topic.meltarwinterstorm.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.15.0}"),
                  "${npc.topic.meltarwinterstorm.15}",
                  List.of())),
          "MeltarWinterstormNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int f = c.flag("__FLAG_FACTION_WITH_THIEVES");

        c.sayKey(
            f <= 0
                ? "npc.meltar.f0"
                : f <= 200
                    ? "npc.meltar.f200"
                    : f <= 400
                        ? "npc.meltar.f400"
                        : f <= 600
                            ? "npc.meltar.f600"
                            : f <= 800 ? "npc.meltar.f800" : "npc.meltar.fhigh");
      }
    };
  }

  public MeltarWinterstorm(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
