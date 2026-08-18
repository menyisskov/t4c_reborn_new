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

@Spawn(type = "Gwen", x = 1667, y = 1271, z = 0, stationary = false, aggressive = false)
public final class Gwen extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Gwen";

  public static final String DISPLAY_NAME = "${npc.gwen}";

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
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "WoLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "WoLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.gwen}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gwen.0.0}", "${npc.topic_keyword.gwen.0.1}"),
                  "${npc.topic.gwen.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gwen.1.0}",
                      "${npc.topic_keyword.gwen.1.1}",
                      "${npc.topic_keyword.gwen.1.2}"),
                  "${npc.topic.gwen.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gwen.2.0}"), "${npc.topic.gwen.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gwen.3.0}",
                      "${npc.topic_keyword.gwen.3.1}",
                      "${npc.topic_keyword.gwen.3.2}"),
                  "${npc.topic.gwen.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gwen.4.0}"), "${npc.topic.gwen.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gwen.5.0}"), "${npc.topic.gwen.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gwen.6.0}"), "${npc.topic.gwen.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gwen.7.0}"), "${npc.topic.gwen.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gwen.8.0}"), "${npc.topic.gwen.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gwen.9.0}",
                      "${npc.topic_keyword.gwen.9.1}",
                      "${npc.topic_keyword.gwen.9.2}",
                      "${npc.topic_keyword.gwen.9.3}",
                      "${npc.topic_keyword.gwen.9.4}"),
                  "${npc.topic.gwen.9}",
                  List.of())),
          "GwenNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public Gwen(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
