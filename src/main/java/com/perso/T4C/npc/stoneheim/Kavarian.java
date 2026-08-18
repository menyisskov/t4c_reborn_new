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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Kavarian", x = 531, y = 374, z = 0, stationary = false, aggressive = false)
public final class Kavarian extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Kavarian";

  public static final String DISPLAY_NAME = "${npc.kavarian}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.kavarian}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.0.0}", "${npc.topic_keyword.kavarian.0.1}"),
                  "${npc.topic.kavarian.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.1.0}", "${npc.topic_keyword.kavarian.1.1}"),
                  "${npc.topic.kavarian.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.2.0}"),
                  "${npc.topic.kavarian.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.3.0}"),
                  "${npc.topic.kavarian.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.4.0}"),
                  "${npc.topic.kavarian.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kavarian.5.0}",
                      "${npc.topic_keyword.kavarian.5.1}",
                      "${npc.topic_keyword.kavarian.5.2}"),
                  "${npc.topic.kavarian.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.6.0}"),
                  "${npc.topic.kavarian.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.7.0}", "${npc.topic_keyword.kavarian.7.1}"),
                  "${npc.topic.kavarian.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.8.0}"),
                  "${npc.topic.kavarian.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.9.0}"),
                  "${npc.topic.kavarian.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.10.0}"),
                  "${npc.topic.kavarian.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kavarian.11.0}"),
                  "${npc.topic.kavarian.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kavarian.12.0}",
                      "${npc.topic_keyword.kavarian.12.1}",
                      "${npc.topic_keyword.kavarian.12.2}",
                      "${npc.topic_keyword.kavarian.12.3}",
                      "${npc.topic_keyword.kavarian.12.4}"),
                  "${npc.topic.kavarian.12}",
                  List.of())),
          "KavarianNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.castSelfSpell(10269);
      }
    };
  }

  public Kavarian(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
