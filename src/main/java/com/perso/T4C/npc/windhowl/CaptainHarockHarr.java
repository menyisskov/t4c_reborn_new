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

@Spawn(
    type = "CaptainHarockHarr",
    x = 1583,
    y = 1215,
    z = 0,
    stationary = false,
    aggressive = false)
public final class CaptainHarockHarr extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "CaptainHarockHarr";

  public static final String DISPLAY_NAME = "${npc.captainharockharr}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.captainharockharr}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainharockharr.0.0}",
                      "${npc.topic_keyword.captainharockharr.0.1}"),
                  "${npc.topic.captainharockharr.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainharockharr.1.0}",
                      "${npc.topic_keyword.captainharockharr.1.1}",
                      "${npc.topic_keyword.captainharockharr.1.2}"),
                  "${npc.topic.captainharockharr.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainharockharr.2.0}",
                      "${npc.topic_keyword.captainharockharr.2.1}",
                      "${npc.topic_keyword.captainharockharr.2.2}"),
                  "${npc.topic.captainharockharr.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainharockharr.3.0}"),
                  "${npc.topic.captainharockharr.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainharockharr.4.0}"),
                  "${npc.topic.captainharockharr.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainharockharr.5.0}"),
                  "${npc.topic.captainharockharr.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainharockharr.6.0}"),
                  "${npc.topic.captainharockharr.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainharockharr.7.0}"),
                  "${npc.topic.captainharockharr.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainharockharr.8.0}"),
                  "${npc.topic.captainharockharr.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainharockharr.9.0}"),
                  "${npc.topic.captainharockharr.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.captainharockharr.10.0}"),
                  "${npc.topic.captainharockharr.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.captainharockharr.11.0}",
                      "${npc.topic_keyword.captainharockharr.11.1}",
                      "${npc.topic_keyword.captainharockharr.11.2}",
                      "${npc.topic_keyword.captainharockharr.11.3}",
                      "${npc.topic_keyword.captainharockharr.11.4}"),
                  "${npc.topic.captainharockharr.11}",
                  List.of())),
          "PirateHarockHarr",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public CaptainHarockHarr(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
