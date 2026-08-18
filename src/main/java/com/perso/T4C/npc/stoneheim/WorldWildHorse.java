package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class WorldWildHorse extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Pegase Attack.wav";
  public static final String SOUND_DEATH = "Pegase Dying.wav";
  public static final String SOUND_HIT = "Pegase Hit.wav";

  public static final String ID = "WorldWildHorse";

  public static final String DISPLAY_NAME = "${npc.worldwildhorse}";

  public static final String SPRITE_BASE = "Horse";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.worldwildhorse}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.0.0}",
                      "${npc.topic_keyword.worldwildhorse.0.1}"),
                  "${npc.topic.worldwildhorse.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.1.0}",
                      "${npc.topic_keyword.worldwildhorse.1.1}",
                      "${npc.topic_keyword.worldwildhorse.1.2}"),
                  "${npc.topic.worldwildhorse.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.2.0}",
                      "${npc.topic_keyword.worldwildhorse.2.1}"),
                  "${npc.topic.worldwildhorse.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.3.0}",
                      "${npc.topic_keyword.worldwildhorse.3.1}"),
                  "${npc.topic.worldwildhorse.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.4.0}",
                      "${npc.topic_keyword.worldwildhorse.4.1}",
                      "${npc.topic_keyword.worldwildhorse.4.2}"),
                  "${npc.topic.worldwildhorse.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.5.0}",
                      "${npc.topic_keyword.worldwildhorse.5.1}",
                      "${npc.topic_keyword.worldwildhorse.5.2}"),
                  "${npc.topic.worldwildhorse.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.6.0}",
                      "${npc.topic_keyword.worldwildhorse.6.1}"),
                  "${npc.topic.worldwildhorse.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.7.0}",
                      "${npc.topic_keyword.worldwildhorse.7.1}"),
                  "${npc.topic.worldwildhorse.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.8.0}",
                      "${npc.topic_keyword.worldwildhorse.8.1}"),
                  "${npc.topic.worldwildhorse.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.9.0}",
                      "${npc.topic_keyword.worldwildhorse.9.1}"),
                  "${npc.topic.worldwildhorse.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.10.0}",
                      "${npc.topic_keyword.worldwildhorse.10.1}"),
                  "${npc.topic.worldwildhorse.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.11.0}",
                      "${npc.topic_keyword.worldwildhorse.11.1}"),
                  "${npc.topic.worldwildhorse.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.12.0}",
                      "${npc.topic_keyword.worldwildhorse.12.1}"),
                  "${npc.topic.worldwildhorse.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.13.0}",
                      "${npc.topic_keyword.worldwildhorse.13.1}",
                      "${npc.topic_keyword.worldwildhorse.13.2}",
                      "${npc.topic_keyword.worldwildhorse.13.3}"),
                  "${npc.topic.worldwildhorse.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.worldwildhorse.14.0}",
                      "${npc.topic_keyword.worldwildhorse.14.1}",
                      "${npc.topic_keyword.worldwildhorse.14.2}",
                      "${npc.topic_keyword.worldwildhorse.14.3}"),
                  "${npc.topic.worldwildhorse.14}",
                  List.of())),
          "WorldWildHorseNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public WorldWildHorse(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        private final String SCROLL = "scroll_of_horse_friendship";

        @Override
        public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          if (c.itemCount(SCROLL) < 10) c.endConversation();
          else c.sayKey("npc.welcome.worldwildhorse");
        }

        @Override
        public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.contains("TELEPORT") && k.contains("LOCATION"))
            return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);

          int x = -1, y = -1;

          if (k.contains("STONECREST") && k.contains("PLAZA")) {

            x = 286;

            y = 730;

          } else if (k.contains("RAVEN") && k.contains("DUST") && k.contains("DESERT")) {

            x = 280;

            y = 2520;

          } else if (k.contains("RAVEN") && k.contains("DUST") && k.contains("STONEHENGE")) {

            x = 1695;

            y = 2165;

          } else if (k.contains("SILVERSKY") && k.contains("CASTLE")) {

            x = 1495;

            y = 2465;

          } else if (k.contains("BLACKBLOOD") && k.contains("CASTLE")) {

            x = 410;

            y = 1725;

          } else if (k.contains("WINDHOWL") && k.contains("ENTRANCE")) {

            x = 1825;

            y = 1300;

          } else if (k.contains("LIGHTHAVEN") && k.contains("BRIDGE")) {

            x = 2775;

            y = 1015;

          } else if (k.contains("ARAKAS") && k.contains("STONEHENGE")) {

            x = 2790;

            y = 175;

          } else if (k.contains("ANNABELLE") && k.contains("HOUSE")) {

            x = 1580;

            y = 210;

          } else if (k.contains("ARAKAS") && k.contains("BRIGANDS")) {

            x = 2180;

            y = 1245;
          }

          if (x >= 0) {

            if (!c.hasItem(SCROLL)) {

              c.sayKey("npc.worldwildhorse.no_scroll");

              return true;
            }

            c.sayKey("npc.worldwildhorse.go");

            c.takeItem(SCROLL);

            c.teleport(x, y, 0);

            c.summon("WORLDWILDHORSE", x + 1, y + 1, 0);

            c.systemMessageKey("message.worldwildhorse.travel");

            c.selfDestructNpc();

            return true;
          }

          if (k.contains("FUCK")
              || k.contains("SUCK")
              || k.contains("ASSHOLE")
              || k.contains(" ASS ")) {

            while (c.hasItem(SCROLL)) c.takeItem(SCROLL);

            c.sayKey("npc.worldwildhorse.insult");

            return true;
          }

          return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
        }
      };
}
