package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "HighPriestGunthar", x = 910, y = 2765, z = 1, stationary = false, aggressive = false)
public final class HighPriestGunthar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HighPriestGunthar";

  public static final String DISPLAY_NAME = "${npc.highpriestgunthar}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.highpriestgunthar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.0.0}"),
                  "${npc.topic.highpriestgunthar.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.1.0}"),
                  "${npc.topic.highpriestgunthar.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.2.0}"),
                  "${npc.topic.highpriestgunthar.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.3.0}"),
                  "${npc.topic.highpriestgunthar.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.4.0}"),
                  "${npc.topic.highpriestgunthar.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.5.0}"),
                  "${npc.topic.highpriestgunthar.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar.6.0}",
                      "${npc.topic_keyword.highpriestgunthar.6.1}"),
                  "${npc.topic.highpriestgunthar.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar.7.0}",
                      "${npc.topic_keyword.highpriestgunthar.7.1}"),
                  "${npc.topic.highpriestgunthar.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar.8.0}",
                      "${npc.topic_keyword.highpriestgunthar.8.1}"),
                  "${npc.topic.highpriestgunthar.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.9.0}"),
                  "${npc.topic.highpriestgunthar.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.10.0}"),
                  "${npc.topic.highpriestgunthar.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.11.0}"),
                  "${npc.topic.highpriestgunthar.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.12.0}"),
                  "${npc.topic.highpriestgunthar.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.13.0}"),
                  "${npc.topic.highpriestgunthar.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.14.0}"),
                  "${npc.topic.highpriestgunthar.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.15.0}"),
                  "${npc.topic.highpriestgunthar.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.16.0}"),
                  "${npc.topic.highpriestgunthar.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar.17.0}",
                      "${npc.topic_keyword.highpriestgunthar.17.1}"),
                  "${npc.topic.highpriestgunthar.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.18.0}"),
                  "${npc.topic.highpriestgunthar.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.19.0}"),
                  "${npc.topic.highpriestgunthar.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.20.0}"),
                  "${npc.topic.highpriestgunthar.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar.21.0}",
                      "${npc.topic_keyword.highpriestgunthar.21.1}"),
                  "${npc.topic.highpriestgunthar.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar.22.0}",
                      "${npc.topic_keyword.highpriestgunthar.22.1}"),
                  "${npc.topic.highpriestgunthar.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.23.0}"),
                  "${npc.topic.highpriestgunthar.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.24.0}"),
                  "${npc.topic.highpriestgunthar.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.25.0}"),
                  "${npc.topic.highpriestgunthar.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.26.0}"),
                  "${npc.topic.highpriestgunthar.26}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.27.0}"),
                  "${npc.topic.highpriestgunthar.27}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.28.0}"),
                  "${npc.topic.highpriestgunthar.28}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.29.0}"),
                  "${npc.topic.highpriestgunthar.29}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar.30.0}",
                      "${npc.topic_keyword.highpriestgunthar.30.1}"),
                  "${npc.topic.highpriestgunthar.30}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.31.0}"),
                  "${npc.topic.highpriestgunthar.31}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.32.0}"),
                  "${npc.topic.highpriestgunthar.32}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.33.0}"),
                  "${npc.topic.highpriestgunthar.33}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar.34.0}",
                      "${npc.topic_keyword.highpriestgunthar.34.1}"),
                  "${npc.topic.highpriestgunthar.34}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.highpriestgunthar.35.0}"),
                  "${npc.topic.highpriestgunthar.35}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar.36.0}",
                      "${npc.topic_keyword.highpriestgunthar.36.1}",
                      "${npc.topic_keyword.highpriestgunthar.36.2}",
                      "${npc.topic_keyword.highpriestgunthar.36.3}",
                      "${npc.topic_keyword.highpriestgunthar.36.4}"),
                  "${npc.topic.highpriestgunthar.36}",
                  List.of())),
          "HighPriestGuntharNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          String key =
              p < 5
                  ? "npc.welcome.highpriestgunthar"
                  : p == 5
                      ? "npc.topic.highpriestgunthar.1"
                      : p == 6
                          ? "npc.topic.highpriestgunthar.2"
                          : p == 18
                              ? "npc.topic.highpriestgunthar.3"
                              : p <= 22
                                  ? "npc.topic.highpriestgunthar.4"
                                  : p == 23
                                      ? "npc.topic.highpriestgunthar.5"
                                      : p == 24
                                          ? "npc.topic.highpriestgunthar.6"
                                          : p == 25
                                              ? "npc.topic.highpriestgunthar.7"
                                              : p < 38
                                                  ? "npc.topic.highpriestgunthar.8"
                                                  : p == 38
                                                      ? "npc.topic.highpriestgunthar.9"
                                                      : p == 39
                                                          ? "npc.topic.highpriestgunthar.10"
                                                          : p < 42
                                                              ? "npc.topic.highpriestgunthar.11"
                                                              : "npc.topic.highpriestgunthar.12";

          c.sayKey(key);
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("HEARTSTONE")) {

            if (c.flag("ADDON_STORYLINE_PROGRESS") == 5) {

              c.sayKey("npc.topic.highpriestgunthar.1");

              c.askYesNo("STONE");

            } else c.sayKey("npc.highpriestgunthar.heartstone.unavailable");

            return true;
          }

          if (k.equals("STONE") || k.equals("MERCHANT") || k.equals("DEMAND")) {

            if (c.flag("ADDON_STORYLINE_PROGRESS") == 5) c.sayKey("npc.topic.highpriestgunthar.13");

            return true;
          }

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (p == 5
              && java.util.Set.of(
                      "WAIT",
                      "NECROMANCER",
                      "CORRUPTERS",
                      "GLURI",
                      "BATTLE",
                      "EMANATION",
                      "HARVESTER",
                      "LIFE",
                      "DRAGON",
                      "COMPLETED",
                      "THEODORE",
                      "LOOK")
                  .contains(k)) {

            c.sayKey("npc.topic.highpriestgunthar.13");

            return true;
          }

          if (p == 6 && k.equals("NOMAD")) {

            c.sayKey("npc.topic.highpriestgunthar.16");

            return true;
          }

          if (p == 24
              && java.util.Set.of(
                      "KEPT",
                      "GODS",
                      "SERVICE",
                      "PHYSICAL",
                      "BODY",
                      "SACRIFIC",
                      "BIND",
                      "SEAL",
                      "TRUE",
                      "INTENTION")
                  .contains(k)) {

            c.sayKey("npc.topic.highpriestgunthar.17");

            return true;
          }

          if (p == 38
              && java.util.Set.of(
                      "BLESSING",
                      "ARTHERK",
                      "SOULS",
                      "STOP",
                      "PART",
                      "HARVESTER",
                      "ILLUSION",
                      "RECENT",
                      "WRONG",
                      "HEART",
                      "EFNISIEN")
                  .contains(k)) {

            c.sayKey("npc.topic.highpriestgunthar.18");

            return true;
          }

          if (k.equals("NAME") || k.equals("GUNTHAR")) {

            c.sayKey("npc.topic.highpriestgunthar.34");

            return true;
          }

          if (k.equals("WORK") || k.equals("PRIEST")) {

            c.sayKey("npc.topic.highpriestgunthar.35");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if ("STONE".equals(state)) {

            if (yes) c.sayKey("npc.topic.highpriestgunthar.14");
            else {

              c.sayKey("npc.topic.highpriestgunthar.15");

              c.askYesNo("STONE");
            }

            return true;
          }

          return false;
        }
      };

  public HighPriestGunthar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
