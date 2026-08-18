package com.perso.T4C.npc.remort;

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

@Spawn(type = "ColosseumOwner", x = 345, y = 490, z = 0, stationary = true, aggressive = false)
public final class ColosseumOwner extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ColosseumOwner";

  public static final String DISPLAY_NAME = "${npc.colosseumowner}";

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
          "${npc.welcome.colosseumowner}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumowner.0.0}",
                      "${npc.topic_keyword.colosseumowner.0.1}"),
                  "${npc.topic.colosseumowner.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumowner.1.0}"),
                  "${npc.topic.colosseumowner.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumowner.2.0}",
                      "${npc.topic_keyword.colosseumowner.2.1}",
                      "${npc.topic_keyword.colosseumowner.2.2}"),
                  "${npc.topic.colosseumowner.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumowner.3.0}"),
                  "${npc.topic.colosseumowner.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.colosseumowner.4.0}"),
                  "${npc.topic.colosseumowner.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumowner.5.0}",
                      "${npc.topic_keyword.colosseumowner.5.1}"),
                  "${npc.topic.colosseumowner.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumowner.6.0}",
                      "${npc.topic_keyword.colosseumowner.6.1}"),
                  "${npc.topic.colosseumowner.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.colosseumowner.7.0}",
                      "${npc.topic_keyword.colosseumowner.7.1}",
                      "${npc.topic_keyword.colosseumowner.7.2}",
                      "${npc.topic_keyword.colosseumowner.7.3}",
                      "${npc.topic_keyword.colosseumowner.7.4}"),
                  "${npc.topic.colosseumowner.7}",
                  List.of())),
          "ColosseumClerkNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public ColosseumOwner(NpcContext context) throws GameException {

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

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          c.sayKey(
              c.flag("__FLAG_USER_HAS_ENTERED_COLOSSEUM") == 0
                  ? "npc.topic.colosseumowner.0"
                  : "npc.colosseumowner.welcome.back");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.trim().toUpperCase(java.util.Locale.ROOT);

          if (k.equals("FIGHT") || k.equals("TEST")) {

            c.sayKey("npc.colosseumowner.fight.ask");

            c.askYesNo("fight");

            return true;
          }

          if (k.equals("SPECTATOR") || k.equals("SPECTATE")) {

            c.sayKey("npc.colosseumowner.spectator.ask");

            c.askYesNo("spectator");

            return true;
          }

          if (k.equals("BATTLEGROUND") || k.equals("COLOSSEUM")) {

            String state = c.pendingYesNo();

            if ("spectator".equals(state)) {

              if (k.equals("COLOSSEUM") && c.globalFlag("ACK_COLOSSEUM") != 0) {

                c.sayKey("npc.colosseumowner.spectator.denied_xp");

                return true;
              }

              c.sayKey("npc.colosseumowner.enjoy");

              c.teleport(k.equals("BATTLEGROUND") ? 2045 : 1745, 1815, 0);

              c.endConversation();

              return true;
            }

            if ("arena".equals(state)) {

              c.flag("__FLAG_USER_HAS_ENTERED_COLOSSEUM", 1);

              c.flag("__FLAG_ARENA_LEVEL", 0);

              c.flag("__FLAG_USER_LEVEL_SLICE", 0);

              c.sayKey("npc.colosseumowner.enjoy");

              c.teleport(
                  k.equals("BATTLEGROUND") ? 1990 : 1725,
                  k.equals("BATTLEGROUND") ? 1870 : 1835,
                  0);

              c.endConversation();

              return true;
            }
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean answer) {

          if (!answer) {

            c.sayKey("npc.colosseumowner.no");

            return true;
          }

          if ("fight".equals(state)) {

            if (c.globalFlag("ACK_COLOSSEUM") != 0 && c.flag("__FLAG_NUMBER_OF_REMORTS") < 1) {

              c.sayKey("npc.colosseumowner.battleground.only");

              c.askYesNo("battleground");

            } else {

              c.sayKey("npc.colosseumowner.arena.choice");

              c.askYesNo("arena");
            }

            return true;
          }

          if ("battleground".equals(state)) {

            if (!answer) {

              c.sayKey("npc.colosseumowner.no");

              return true;
            }

            c.sayKey("npc.colosseumowner.enjoy");

            c.teleport(1990, 1870, 0);

            c.endConversation();

            return true;
          }

          if ("spectator".equals(state)) {

            c.sayKey("npc.colosseumowner.spectator.choice");

            return true;
          }

          return false;
        }
      };
}
