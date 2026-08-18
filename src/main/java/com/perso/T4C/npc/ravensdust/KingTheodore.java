package com.perso.T4C.npc.ravensdust;

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

@Spawn(type = "KingTheodore", x = 2848, y = 648, z = 4, stationary = true, aggressive = false)
public final class KingTheodore extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "KingTheodore";

  public static final String DISPLAY_NAME = "${npc.kingtheodore}";

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
              new NpcSpec.Part(BodyPart.HEAD, "PupGoldenCrown"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.kingtheodore}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.0.0}",
                      "${npc.topic_keyword.kingtheodore.0.1}"),
                  "${npc.topic.kingtheodore.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.1.0}",
                      "${npc.topic_keyword.kingtheodore.1.1}"),
                  "${npc.topic.kingtheodore.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.2.0}",
                      "${npc.topic_keyword.kingtheodore.2.1}",
                      "${npc.topic_keyword.kingtheodore.2.2}",
                      "${npc.topic_keyword.kingtheodore.2.3}"),
                  "${npc.topic.kingtheodore.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.3.0}",
                      "${npc.topic_keyword.kingtheodore.3.1}"),
                  "${npc.topic.kingtheodore.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.4.0}"),
                  "${npc.topic.kingtheodore.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.5.0}",
                      "${npc.topic_keyword.kingtheodore.5.1}"),
                  "${npc.topic.kingtheodore.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.6.0}"),
                  "${npc.topic.kingtheodore.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.7.0}",
                      "${npc.topic_keyword.kingtheodore.7.1}"),
                  "${npc.topic.kingtheodore.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.8.0}"),
                  "${npc.topic.kingtheodore.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.9.0}",
                      "${npc.topic_keyword.kingtheodore.9.1}"),
                  "${npc.topic.kingtheodore.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.10.0}",
                      "${npc.topic_keyword.kingtheodore.10.1}",
                      "${npc.topic_keyword.kingtheodore.10.2}"),
                  "${npc.topic.kingtheodore.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.11.0}",
                      "${npc.topic_keyword.kingtheodore.11.1}",
                      "${npc.topic_keyword.kingtheodore.11.2}"),
                  "${npc.topic.kingtheodore.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.12.0}"),
                  "${npc.topic.kingtheodore.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.13.0}",
                      "${npc.topic_keyword.kingtheodore.13.1}",
                      "${npc.topic_keyword.kingtheodore.13.2}",
                      "${npc.topic_keyword.kingtheodore.13.3}"),
                  "${npc.topic.kingtheodore.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.14.0}"),
                  "${npc.topic.kingtheodore.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.15.0}"),
                  "${npc.topic.kingtheodore.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.16.0}",
                      "${npc.topic_keyword.kingtheodore.16.1}"),
                  "${npc.topic.kingtheodore.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.17.0}",
                      "${npc.topic_keyword.kingtheodore.17.1}"),
                  "${npc.topic.kingtheodore.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.18.0}"),
                  "${npc.topic.kingtheodore.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.19.0}"),
                  "${npc.topic.kingtheodore.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.20.0}"),
                  "${npc.topic.kingtheodore.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.21.0}",
                      "${npc.topic_keyword.kingtheodore.21.1}"),
                  "${npc.topic.kingtheodore.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.22.0}"),
                  "${npc.topic.kingtheodore.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingtheodore.23.0}"),
                  "${npc.topic.kingtheodore.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.24.0}",
                      "${npc.topic_keyword.kingtheodore.24.1}",
                      "${npc.topic_keyword.kingtheodore.24.2}",
                      "${npc.topic_keyword.kingtheodore.24.3}",
                      "${npc.topic_keyword.kingtheodore.24.4}"),
                  "${npc.topic.kingtheodore.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingtheodore.25.0}",
                      "${npc.topic_keyword.kingtheodore.25.1}",
                      "${npc.topic_keyword.kingtheodore.25.2}",
                      "${npc.topic_keyword.kingtheodore.25.3}"),
                  "${npc.topic.kingtheodore.25}",
                  List.of())),
          "KingTheodoreNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.npc().setStationary(true);
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.kingtheodore.attacked");

        if ((int) (Math.random() * 2) == 0) c.teleport(206, 2380, 1);
        else c.teleport(218, 2336, 1);
      }
    };
  }

  public KingTheodore(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
