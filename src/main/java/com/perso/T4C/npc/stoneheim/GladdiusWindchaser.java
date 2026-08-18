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
import java.util.List;

public final class GladdiusWindchaser extends ScriptedNpc {

  public static final String ID = "GladdiusWindchaser";

  public static final String DISPLAY_NAME = "${npc.gladdiuswindchaser}";

  public static final String SPRITE_BASE = "64kCentaurKing";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.gladdiuswindchaser}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.0.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.0.1}"),
                  "${npc.topic.gladdiuswindchaser.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gladdiuswindchaser.1.0}"),
                  "${npc.topic.gladdiuswindchaser.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.2.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.2.1}"),
                  "${npc.topic.gladdiuswindchaser.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.3.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.3.1}"),
                  "${npc.topic.gladdiuswindchaser.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.4.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.4.1}"),
                  "${npc.topic.gladdiuswindchaser.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.5.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.5.1}"),
                  "${npc.topic.gladdiuswindchaser.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.6.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.6.1}"),
                  "${npc.topic.gladdiuswindchaser.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.7.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.7.1}"),
                  "${npc.topic.gladdiuswindchaser.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.8.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.8.1}"),
                  "${npc.topic.gladdiuswindchaser.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.9.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.9.1}",
                      "${npc.topic_keyword.gladdiuswindchaser.9.2}"),
                  "${npc.topic.gladdiuswindchaser.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.10.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.10.1}",
                      "${npc.topic_keyword.gladdiuswindchaser.10.2}",
                      "${npc.topic_keyword.gladdiuswindchaser.10.3}"),
                  "${npc.topic.gladdiuswindchaser.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gladdiuswindchaser.11.0}",
                      "${npc.topic_keyword.gladdiuswindchaser.11.1}",
                      "${npc.topic_keyword.gladdiuswindchaser.11.2}",
                      "${npc.topic_keyword.gladdiuswindchaser.11.3}",
                      "${npc.topic_keyword.gladdiuswindchaser.11.4}"),
                  "${npc.topic.gladdiuswindchaser.11}",
                  List.of())),
          "GladdiusWindchaserNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public GladdiusWindchaser(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
