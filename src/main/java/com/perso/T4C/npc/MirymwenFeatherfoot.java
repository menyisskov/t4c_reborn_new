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
    type = "MirymwenFeatherfoot",
    x = 140,
    y = 1550,
    z = 1,
    stationary = false,
    aggressive = false)
public final class MirymwenFeatherfoot extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MirymwenFeatherfoot";

  public static final String DISPLAY_NAME = "${npc.mirymwenfeatherfoot}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "WoLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "WoLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.mirymwenfeatherfoot}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirymwenfeatherfoot.0.0}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.0.1}"),
                  "${npc.topic.mirymwenfeatherfoot.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirymwenfeatherfoot.1.0}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.1.1}"),
                  "${npc.topic.mirymwenfeatherfoot.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirymwenfeatherfoot.2.0}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.2.1}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.2.2}"),
                  "${npc.topic.mirymwenfeatherfoot.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mirymwenfeatherfoot.3.0}"),
                  "${npc.topic.mirymwenfeatherfoot.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirymwenfeatherfoot.4.0}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.4.1}"),
                  "${npc.topic.mirymwenfeatherfoot.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirymwenfeatherfoot.5.0}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.5.1}"),
                  "${npc.topic.mirymwenfeatherfoot.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirymwenfeatherfoot.6.0}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.6.1}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.6.2}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.6.3}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.6.4}"),
                  "${npc.topic.mirymwenfeatherfoot.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mirymwenfeatherfoot.7.0}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.7.1}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.7.2}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.7.3}",
                      "${npc.topic_keyword.mirymwenfeatherfoot.7.4}"),
                  "${npc.topic.mirymwenfeatherfoot.7}",
                  List.of())),
          "MirymwenFeatherfootNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public MirymwenFeatherfoot(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
