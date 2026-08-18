package com.perso.T4C.npc.addon;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Antonian", x = 2372, y = 733, z = 0, stationary = false, aggressive = false)
public final class Antonian extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Antonian";

  public static final String DISPLAY_NAME = "${npc.antonian}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupMageRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.antonian}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.0.0}"),
                  "${npc.topic.antonian.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.antonian.1.0}",
                      "${npc.topic_keyword.antonian.1.1}",
                      "${npc.topic_keyword.antonian.1.2}"),
                  "${npc.topic.antonian.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.2.0}"),
                  "${npc.topic.antonian.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.3.0}"),
                  "${npc.topic.antonian.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.4.0}"),
                  "${npc.topic.antonian.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.5.0}", "${npc.topic_keyword.antonian.5.1}"),
                  "${npc.topic.antonian.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.6.0}"),
                  "${npc.topic.antonian.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.7.0}"),
                  "${npc.topic.antonian.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.8.0}"),
                  "${npc.topic.antonian.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.9.0}"),
                  "${npc.topic.antonian.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.10.0}"),
                  "${npc.topic.antonian.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.antonian.11.0}"),
                  "${npc.topic.antonian.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.antonian.12.0}",
                      "${npc.topic_keyword.antonian.12.1}",
                      "${npc.topic_keyword.antonian.12.2}",
                      "${npc.topic_keyword.antonian.12.3}",
                      "${npc.topic_keyword.antonian.12.4}"),
                  "${npc.topic.antonian.12}",
                  List.of())),
          "GuildLeaderEthanNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int progress = c.flag("ADDON_STORYLINE_PROGRESS");

          int activated = c.flag("ADDON_NEXUS_STONES_ACTIVATED");

          if (progress < 26) c.sayKey("npc.antonian.progress.before26");
          else if (progress == 26) {

            c.sayKey("npc.antonian.progress.26");

            c.askYesNo("tablet");

          } else if (progress == 27) {

            c.sayKey("npc.antonian.progress.27");

            c.askYesNo("directions");

          } else if (progress == 28) c.sayKey("npc.antonian.progress.28");
          else if (activated > 5 && activated < 15) c.sayKey("npc.antonian.progress.nexus");
          else if (activated >= 15 && c.flag("ADDON_NEXUS_STONEHEIM_ACTIVATED") == 0)
            c.sayKey("npc.antonian.progress.unlock");
          else if (progress < 42) c.sayKey("npc.antonian.progress.before42");
          else c.sayKey("npc.antonian.progress.complete");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if ("tablet".equals(state)) {

            c.sayKey(yes ? "npc.antonian.tablet.yes" : "npc.antonian.tablet.no");

            return true;
          }

          if ("directions".equals(state)) {

            if (!yes) {

              c.sayKey("npc.antonian.directions.no");

              return true;
            }

            String[] keys = {
              "npc.antonian.direction.1",
              "npc.antonian.direction.2",
              "npc.antonian.direction.3",
              "npc.antonian.direction.4",
              "npc.antonian.direction.5"
            };

            for (int i = 1; i <= 5; i++)
              if (c.flag("ADDON_NEXUS_" + i + "_ACTIVATED") == 0) {

                c.sayKey(keys[i - 1]);

                return true;
              }

            c.sayKey(
                c.flag("QUEST_ISLAND_ACCESS") == 0
                    ? "npc.antonian.direction.doneLocked"
                    : "npc.antonian.direction.done");

            if (c.flag("QUEST_ISLAND_ACCESS") > 0) {

              c.flag("ADDON_STORYLINE_PROGRESS", 28);

              c.systemMessageKey("npc.antonian.reward");
            }

            return true;
          }

          return false;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("NAME")) {

            c.sayKey("npc.antonian.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.antonian.work");

            return true;
          }

          if (k.equals("DIRECTION") || k.equals("DIRECTIONS")) {

            c.askYesNo("directions");

            return true;
          }

          if (k.equals("UNLOCK")) {

            c.sayKey("npc.antonian.unlock");

            return true;
          }

          return false;
        }
      };

  public Antonian(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
