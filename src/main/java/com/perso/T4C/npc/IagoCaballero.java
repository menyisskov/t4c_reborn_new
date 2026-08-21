package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spawn.SpawnKind;
import java.util.List;

@Spawn(type = "IagoCaballero", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
@Spawn(
    type = "IAGOCABALLERO",
    x = 812,
    y = 2714,
    z = 0,
    stationary = false,
    aggressive = false,
    kind = SpawnKind.MONSTER)
public final class IagoCaballero extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "IagoCaballero";

  public static final String DISPLAY_NAME = "${npc.iagocaballero}";

  public static final String SPRITE_BASE = "BlackWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.iagocaballero}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.0.0}",
                      "${npc.topic_keyword.iagocaballero.0.1}",
                      "${npc.topic_keyword.iagocaballero.0.2}",
                      "${npc.topic_keyword.iagocaballero.0.3}"),
                  "${npc.topic.iagocaballero.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.1.0}",
                      "${npc.topic_keyword.iagocaballero.1.1}"),
                  "${npc.topic.iagocaballero.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.2.0}",
                      "${npc.topic_keyword.iagocaballero.2.1}"),
                  "${npc.topic.iagocaballero.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.3.0}",
                      "${npc.topic_keyword.iagocaballero.3.1}",
                      "${npc.topic_keyword.iagocaballero.3.2}"),
                  "${npc.topic.iagocaballero.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.4.0}"),
                  "${npc.topic.iagocaballero.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.5.0}"),
                  "${npc.topic.iagocaballero.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.6.0}"),
                  "${npc.topic.iagocaballero.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.7.0}"),
                  "${npc.topic.iagocaballero.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.8.0}"),
                  "${npc.topic.iagocaballero.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.9.0}"),
                  "${npc.topic.iagocaballero.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.10.0}"),
                  "${npc.topic.iagocaballero.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.11.0}",
                      "${npc.topic_keyword.iagocaballero.11.1}"),
                  "${npc.topic.iagocaballero.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.12.0}",
                      "${npc.topic_keyword.iagocaballero.12.1}"),
                  "${npc.topic.iagocaballero.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.13.0}",
                      "${npc.topic_keyword.iagocaballero.13.1}"),
                  "${npc.topic.iagocaballero.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.14.0}",
                      "${npc.topic_keyword.iagocaballero.14.1}"),
                  "${npc.topic.iagocaballero.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.15.0}"),
                  "${npc.topic.iagocaballero.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.16.0}"),
                  "${npc.topic.iagocaballero.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.17.0}"),
                  "${npc.topic.iagocaballero.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.18.0}"),
                  "${npc.topic.iagocaballero.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.19.0}"),
                  "${npc.topic.iagocaballero.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.20.0}"),
                  "${npc.topic.iagocaballero.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.21.0}",
                      "${npc.topic_keyword.iagocaballero.21.1}"),
                  "${npc.topic.iagocaballero.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.22.0}"),
                  "${npc.topic.iagocaballero.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.iagocaballero.23.0}"),
                  "${npc.topic.iagocaballero.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.24.0}",
                      "${npc.topic_keyword.iagocaballero.24.1}",
                      "${npc.topic_keyword.iagocaballero.24.2}",
                      "${npc.topic_keyword.iagocaballero.24.3}"),
                  "${npc.topic.iagocaballero.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.iagocaballero.25.0}",
                      "${npc.topic_keyword.iagocaballero.25.1}",
                      "${npc.topic_keyword.iagocaballero.25.2}",
                      "${npc.topic_keyword.iagocaballero.25.3}",
                      "${npc.topic_keyword.iagocaballero.25.4}"),
                  "${npc.topic.iagocaballero.25}",
                  List.of())),
          "IagoCaballeroNPC",
          new NpcSpec.CombatProfile(40, 847, 55, 50, 50, 20, 490, 170, "1d53+41"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_SEEK_SHADEEN") >= 3) {

          c.sayKey("${npc.welcome.iagocaballero}");

          c.npc().provoke();
        }
      }

      @Override
      public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.globalFlag("QUEST_GLOBAL_CABALLERO_ALIVE", 0);
      }

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (Math.random() < .25)
          c.shoutKey(Math.random() < .5 ? "npc.iago.attack.purify" : "npc.iago.attack.pain");
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (Math.random() < .25)
          c.shoutKey(Math.random() < .5 ? "npc.iago.attacked.strength" : "npc.iago.attacked.ow");
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.globalFlag("QUEST_GLOBAL_CABALLERO_ALIVE", 1);
      }
    };
  }

  public IagoCaballero(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
