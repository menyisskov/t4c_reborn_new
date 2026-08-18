package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class Shadeen extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Shadeen";

  public static final String DISPLAY_NAME = "${npc.shadeen}";

  public static final String SPRITE_BASE = "Thief";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.shadeen}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.0.0}", "${npc.topic_keyword.shadeen.0.1}"),
                  "${npc.topic.shadeen.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shadeen.1.0}",
                      "${npc.topic_keyword.shadeen.1.1}",
                      "${npc.topic_keyword.shadeen.1.2}"),
                  "${npc.topic.shadeen.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.2.0}"), "${npc.topic.shadeen.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.3.0}", "${npc.topic_keyword.shadeen.3.1}"),
                  "${npc.topic.shadeen.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.4.0}"), "${npc.topic.shadeen.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.5.0}"), "${npc.topic.shadeen.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.6.0}"), "${npc.topic.shadeen.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.7.0}"), "${npc.topic.shadeen.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.8.0}"), "${npc.topic.shadeen.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shadeen.9.0}",
                      "${npc.topic_keyword.shadeen.9.1}",
                      "${npc.topic_keyword.shadeen.9.2}"),
                  "${npc.topic.shadeen.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.10.0}"),
                  "${npc.topic.shadeen.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.11.0}", "${npc.topic_keyword.shadeen.11.1}"),
                  "${npc.topic.shadeen.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.12.0}", "${npc.topic_keyword.shadeen.12.1}"),
                  "${npc.topic.shadeen.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.13.0}"),
                  "${npc.topic.shadeen.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.14.0}", "${npc.topic_keyword.shadeen.14.1}"),
                  "${npc.topic.shadeen.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.15.0}"),
                  "${npc.topic.shadeen.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.16.0}"),
                  "${npc.topic.shadeen.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.17.0}"),
                  "${npc.topic.shadeen.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.18.0}", "${npc.topic_keyword.shadeen.18.1}"),
                  "${npc.topic.shadeen.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.19.0}", "${npc.topic_keyword.shadeen.19.1}"),
                  "${npc.topic.shadeen.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.20.0}", "${npc.topic_keyword.shadeen.20.1}"),
                  "${npc.topic.shadeen.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadeen.21.0}"),
                  "${npc.topic.shadeen.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shadeen.22.0}",
                      "${npc.topic_keyword.shadeen.22.1}",
                      "${npc.topic_keyword.shadeen.22.2}",
                      "${npc.topic_keyword.shadeen.22.3}"),
                  "${npc.topic.shadeen.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shadeen.23.0}",
                      "${npc.topic_keyword.shadeen.23.1}",
                      "${npc.topic_keyword.shadeen.23.2}",
                      "${npc.topic_keyword.shadeen.23.3}",
                      "${npc.topic_keyword.shadeen.23.4}"),
                  "${npc.topic.shadeen.23}",
                  List.of())),
          "ShadeenNPC",
          new NpcSpec.CombatProfile(60, 1000000, 75, 68, 68, 64999, 730, 65535, "1d90+69"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (Math.random() < .08 && c.hasItem("light_healing_potion")) {

          c.takeItem("light_healing_potion");

          c.shoutKey("npc.shadeen.attack.potion");

          c.healToHalf();
        }
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (Math.random() < .1 && c.hasItem("light_healing_potion")) {

          c.takeItem("light_healing_potion");

          c.shoutKey("npc.shadeen.attacked.potion");

          c.healToHalf();
        }
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.shadeen.death");
      }
    };
  }

  public Shadeen(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
