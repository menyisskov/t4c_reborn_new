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

@Spawn(type = "BrotherNiuss", x = 1693, y = 1176, z = 0, stationary = false, aggressive = false)
public final class BrotherNiuss extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "BrotherNiuss";

  public static final String DISPLAY_NAME = "${npc.brotherniuss}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupFlail")),
          0,
          List.of(),
          "${npc.welcome.brotherniuss}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherniuss.0.0}",
                      "${npc.topic_keyword.brotherniuss.0.1}"),
                  "${npc.topic.brotherniuss.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherniuss.1.0}",
                      "${npc.topic_keyword.brotherniuss.1.1}",
                      "${npc.topic_keyword.brotherniuss.1.2}"),
                  "${npc.topic.brotherniuss.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherniuss.2.0}",
                      "${npc.topic_keyword.brotherniuss.2.1}",
                      "${npc.topic_keyword.brotherniuss.2.2}"),
                  "${npc.topic.brotherniuss.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherniuss.3.0}"),
                  "${npc.topic.brotherniuss.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherniuss.4.0}"),
                  "${npc.topic.brotherniuss.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherniuss.5.0}",
                      "${npc.topic_keyword.brotherniuss.5.1}",
                      "${npc.topic_keyword.brotherniuss.5.2}",
                      "${npc.topic_keyword.brotherniuss.5.3}",
                      "${npc.topic_keyword.brotherniuss.5.4}"),
                  "${npc.topic.brotherniuss.5}",
                  List.of())),
          "BrotherNiussNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public BrotherNiuss(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
