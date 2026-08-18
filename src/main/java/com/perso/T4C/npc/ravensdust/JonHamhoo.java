package com.perso.T4C.npc.ravensdust;

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

@Spawn(type = "JonHamhoo", x = 1416, y = 2342, z = 0, stationary = false, aggressive = false)
public final class JonHamhoo extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "JonHamhoo";

  public static final String DISPLAY_NAME = "${npc.jonhamhoo}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.jonhamhoo}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jonhamhoo.0.0}", "${npc.topic_keyword.jonhamhoo.0.1}"),
                  "${npc.topic.jonhamhoo.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jonhamhoo.1.0}", "${npc.topic_keyword.jonhamhoo.1.1}"),
                  "${npc.topic.jonhamhoo.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jonhamhoo.2.0}",
                      "${npc.topic_keyword.jonhamhoo.2.1}",
                      "${npc.topic_keyword.jonhamhoo.2.2}"),
                  "${npc.topic.jonhamhoo.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jonhamhoo.3.0}"),
                  "${npc.topic.jonhamhoo.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jonhamhoo.4.0}"),
                  "${npc.topic.jonhamhoo.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jonhamhoo.5.0}",
                      "${npc.topic_keyword.jonhamhoo.5.1}",
                      "${npc.topic_keyword.jonhamhoo.5.2}",
                      "${npc.topic_keyword.jonhamhoo.5.3}"),
                  "${npc.topic.jonhamhoo.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jonhamhoo.6.0}",
                      "${npc.topic_keyword.jonhamhoo.6.1}",
                      "${npc.topic_keyword.jonhamhoo.6.2}"),
                  "${npc.topic.jonhamhoo.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jonhamhoo.7.0}"),
                  "${npc.topic.jonhamhoo.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jonhamhoo.8.0}"),
                  "${npc.topic.jonhamhoo.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jonhamhoo.9.0}", "${npc.topic_keyword.jonhamhoo.9.1}"),
                  "${npc.topic.jonhamhoo.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jonhamhoo.10.0}", "${npc.topic_keyword.jonhamhoo.10.1}"),
                  "${npc.topic.jonhamhoo.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jonhamhoo.11.0}"),
                  "${npc.topic.jonhamhoo.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jonhamhoo.12.0}",
                      "${npc.topic_keyword.jonhamhoo.12.1}",
                      "${npc.topic_keyword.jonhamhoo.12.2}",
                      "${npc.topic_keyword.jonhamhoo.12.3}"),
                  "${npc.topic.jonhamhoo.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jonhamhoo.13.0}",
                      "${npc.topic_keyword.jonhamhoo.13.1}",
                      "${npc.topic_keyword.jonhamhoo.13.2}",
                      "${npc.topic_keyword.jonhamhoo.13.3}",
                      "${npc.topic_keyword.jonhamhoo.13.4}"),
                  "${npc.topic.jonhamhoo.13}",
                  List.of())),
          "JonHamhooNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public JonHamhoo(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
