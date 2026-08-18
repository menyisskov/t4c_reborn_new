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

@Spawn(type = "GuildLeaderEthan", x = 1710, y = 1198, z = 0, stationary = false, aggressive = false)
public final class GuildLeaderEthan extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "GuildLeaderEthan";

  public static final String DISPLAY_NAME = "${npc.guildleaderethan}";

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
          "${npc.welcome.guildleaderethan}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.0.0}"),
                  "${npc.topic.guildleaderethan.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.1.0}"),
                  "${npc.topic.guildleaderethan.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.2.0}"),
                  "${npc.topic.guildleaderethan.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.3.0}"),
                  "${npc.topic.guildleaderethan.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.4.0}"),
                  "${npc.topic.guildleaderethan.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guildleaderethan.5.0}",
                      "${npc.topic_keyword.guildleaderethan.5.1}"),
                  "${npc.topic.guildleaderethan.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.6.0}"),
                  "${npc.topic.guildleaderethan.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.7.0}"),
                  "${npc.topic.guildleaderethan.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.8.0}"),
                  "${npc.topic.guildleaderethan.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.9.0}"),
                  "${npc.topic.guildleaderethan.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.10.0}"),
                  "${npc.topic.guildleaderethan.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.11.0}"),
                  "${npc.topic.guildleaderethan.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.12.0}"),
                  "${npc.topic.guildleaderethan.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guildleaderethan.13.0}",
                      "${npc.topic_keyword.guildleaderethan.13.1}"),
                  "${npc.topic.guildleaderethan.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.14.0}"),
                  "${npc.topic.guildleaderethan.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.15.0}"),
                  "${npc.topic.guildleaderethan.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.16.0}"),
                  "${npc.topic.guildleaderethan.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.17.0}"),
                  "${npc.topic.guildleaderethan.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.18.0}"),
                  "${npc.topic.guildleaderethan.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guildleaderethan.19.0}"),
                  "${npc.topic.guildleaderethan.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guildleaderethan.20.0}",
                      "${npc.topic_keyword.guildleaderethan.20.1}",
                      "${npc.topic_keyword.guildleaderethan.20.2}",
                      "${npc.topic_keyword.guildleaderethan.20.3}",
                      "${npc.topic_keyword.guildleaderethan.20.4}"),
                  "${npc.topic.guildleaderethan.20}",
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

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (p < 6) c.sayKey("npc.ethan.progress.before6");
          else if (p == 6) c.sayKey("npc.ethan.progress.6");
          else if (p < 9) c.sayKey("npc.ethan.progress.before9");
          else if (p == 9) c.sayKey("npc.ethan.progress.9");
          else if (p == 10) {

            if (c.player().getGold() >= 2000) {

              c.sayKey("npc.ethan.progress.10");

              c.player().addGold(-2000);

              c.flag("ADDON_STORYLINE_PROGRESS", 11);

            } else c.sayKey("npc.ethan.need2000");

          } else if (p == 11) c.sayKey("npc.ethan.progress.11");
          else if (p == 12) c.sayKey("npc.ethan.progress.12");
          else if (p < 42) c.sayKey("npc.ethan.progress.before42");
          else c.sayKey("npc.ethan.progress.complete");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.equals("TASK") && p == 6) {

            c.sayKey("npc.ethan.task");

            c.flag("ADDON_STORYLINE_PROGRESS", 7);

            return true;
          }

          if (k.equals("MONEY")) {

            if (p == 9 && c.player().getGold() >= 1000) {

              c.player().addGold(-1000);

              c.flag("ADDON_STORYLINE_PROGRESS", 11);

              c.sayKey("npc.ethan.money");

            } else if (p == 9) {

              c.flag("ADDON_STORYLINE_PROGRESS", 10);

              c.sayKey("npc.ethan.double");

            } else c.sayKey("npc.ethan.busy");

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.ethan.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.ethan.work");

            return true;
          }

          return false;
        }
      };

  public GuildLeaderEthan(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
