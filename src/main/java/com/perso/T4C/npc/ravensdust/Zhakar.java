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

@Spawn(type = "Zhakar", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
public final class Zhakar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Zhakar";

  public static final String DISPLAY_NAME = "${npc.zhakar}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupWoodenStaff")),
          0,
          List.of(),
          "${npc.welcome.zhakar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.0.0}"), "${npc.topic.zhakar.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.1.0}"), "${npc.topic.zhakar.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.2.0}"), "${npc.topic.zhakar.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.3.0}"), "${npc.topic.zhakar.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.4.0}"), "${npc.topic.zhakar.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.5.0}"), "${npc.topic.zhakar.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.6.0}"), "${npc.topic.zhakar.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.7.0}", "${npc.topic_keyword.zhakar.7.1}"),
                  "${npc.topic.zhakar.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.8.0}", "${npc.topic_keyword.zhakar.8.1}"),
                  "${npc.topic.zhakar.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.9.0}", "${npc.topic_keyword.zhakar.9.1}"),
                  "${npc.topic.zhakar.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.10.0}", "${npc.topic_keyword.zhakar.10.1}"),
                  "${npc.topic.zhakar.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.11.0}"), "${npc.topic.zhakar.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.12.0}", "${npc.topic_keyword.zhakar.12.1}"),
                  "${npc.topic.zhakar.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.13.0}", "${npc.topic_keyword.zhakar.13.1}"),
                  "${npc.topic.zhakar.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zhakar.14.0}",
                      "${npc.topic_keyword.zhakar.14.1}",
                      "${npc.topic_keyword.zhakar.14.2}"),
                  "${npc.topic.zhakar.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.15.0}"), "${npc.topic.zhakar.15}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.16.0}"), "${npc.topic.zhakar.16}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.17.0}"), "${npc.topic.zhakar.17}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.18.0}"), "${npc.topic.zhakar.18}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.19.0}"), "${npc.topic.zhakar.19}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.20.0}"), "${npc.topic.zhakar.20}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.21.0}"), "${npc.topic.zhakar.21}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.22.0}"), "${npc.topic.zhakar.22}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.23.0}", "${npc.topic_keyword.zhakar.23.1}"),
                  "${npc.topic.zhakar.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.24.0}"), "${npc.topic.zhakar.24}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.25.0}"), "${npc.topic.zhakar.25}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.26.0}"), "${npc.topic.zhakar.26}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.27.0}", "${npc.topic_keyword.zhakar.27.1}"),
                  "${npc.topic.zhakar.27}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zhakar.28.0}",
                      "${npc.topic_keyword.zhakar.28.1}",
                      "${npc.topic_keyword.zhakar.28.2}",
                      "${npc.topic_keyword.zhakar.28.3}"),
                  "${npc.topic.zhakar.28}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.29.0}", "${npc.topic_keyword.zhakar.29.1}"),
                  "${npc.topic.zhakar.29}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.30.0}", "${npc.topic_keyword.zhakar.30.1}"),
                  "${npc.topic.zhakar.30}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.31.0}"), "${npc.topic.zhakar.31}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.32.0}", "${npc.topic_keyword.zhakar.32.1}"),
                  "${npc.topic.zhakar.32}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zhakar.33.0}",
                      "${npc.topic_keyword.zhakar.33.1}",
                      "${npc.topic_keyword.zhakar.33.2}",
                      "${npc.topic_keyword.zhakar.33.3}"),
                  "${npc.topic.zhakar.33}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.34.0}", "${npc.topic_keyword.zhakar.34.1}"),
                  "${npc.topic.zhakar.34}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.35.0}"), "${npc.topic.zhakar.35}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.36.0}"), "${npc.topic.zhakar.36}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.37.0}"), "${npc.topic.zhakar.37}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.38.0}"), "${npc.topic.zhakar.38}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.39.0}", "${npc.topic_keyword.zhakar.39.1}"),
                  "${npc.topic.zhakar.39}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.40.0}", "${npc.topic_keyword.zhakar.40.1}"),
                  "${npc.topic.zhakar.40}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.41.0}"), "${npc.topic.zhakar.41}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.42.0}"), "${npc.topic.zhakar.42}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zhakar.43.0}",
                      "${npc.topic_keyword.zhakar.43.1}",
                      "${npc.topic_keyword.zhakar.43.2}",
                      "${npc.topic_keyword.zhakar.43.3}"),
                  "${npc.topic.zhakar.43}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zhakar.44.0}",
                      "${npc.topic_keyword.zhakar.44.1}",
                      "${npc.topic_keyword.zhakar.44.2}",
                      "${npc.topic_keyword.zhakar.44.3}"),
                  "${npc.topic.zhakar.44}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zhakar.45.0}"),
                  "${npc.topic.zhakar.45}",
                  List.of())),
          "ZhakarNPC",
          new NpcSpec.CombatProfile(95, 6012, 110, 100, 100, 47, 1150, 390, "1d164+127"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.castSelfSpell(10294);
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int roll = 1 + (int) (Math.random() * 10);

        if (roll <= 2) c.shoutKey("npc.zhakar.attacked." + roll);
      }
    };
  }

  public Zhakar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
