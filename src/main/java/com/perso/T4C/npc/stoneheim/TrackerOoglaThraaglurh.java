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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(
    type = "TrackerOoglaThraaglurh",
    x = 2235,
    y = 95,
    z = 2,
    stationary = false,
    aggressive = false)
public final class TrackerOoglaThraaglurh extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "TrackerOoglaThraaglurh";

  public static final String DISPLAY_NAME = "${npc.trackerooglathraaglurh}";

  public static final String SPRITE_BASE = "64kSkavenSkavenger";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.trackerooglathraaglurh}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.trackerooglathraaglurh.0.0}",
                      "${npc.topic_keyword.trackerooglathraaglurh.0.1}"),
                  "${npc.topic.trackerooglathraaglurh.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.trackerooglathraaglurh.1.0}",
                      "${npc.topic_keyword.trackerooglathraaglurh.1.1}",
                      "${npc.topic_keyword.trackerooglathraaglurh.1.2}"),
                  "${npc.topic.trackerooglathraaglurh.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.trackerooglathraaglurh.2.0}",
                      "${npc.topic_keyword.trackerooglathraaglurh.2.1}",
                      "${npc.topic_keyword.trackerooglathraaglurh.2.2}",
                      "${npc.topic_keyword.trackerooglathraaglurh.2.3}"),
                  "${npc.topic.trackerooglathraaglurh.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.trackerooglathraaglurh.3.0}"),
                  "${npc.topic.trackerooglathraaglurh.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.trackerooglathraaglurh.4.0}",
                      "${npc.topic_keyword.trackerooglathraaglurh.4.1}"),
                  "${npc.topic.trackerooglathraaglurh.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.trackerooglathraaglurh.5.0}",
                      "${npc.topic_keyword.trackerooglathraaglurh.5.1}"),
                  "${npc.topic.trackerooglathraaglurh.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.trackerooglathraaglurh.6.0}",
                      "${npc.topic_keyword.trackerooglathraaglurh.6.1}"),
                  "${npc.topic.trackerooglathraaglurh.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.trackerooglathraaglurh.7.0}",
                      "${npc.topic_keyword.trackerooglathraaglurh.7.1}",
                      "${npc.topic_keyword.trackerooglathraaglurh.7.2}",
                      "${npc.topic_keyword.trackerooglathraaglurh.7.3}",
                      "${npc.topic_keyword.trackerooglathraaglurh.7.4}"),
                  "${npc.topic.trackerooglathraaglurh.7}",
                  List.of())),
          "TrackerOoglaThraaglurhNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1
            && c.itemCount("villain_skull") >= 2
            && c.itemCount("scroll_of_hate") >= 2) {

          c.sayKey("npc.oogla.trade.ask");

          c.askYesNo("oogla_trade");

        } else c.sayKey("npc.oogla.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("SKULL OF EVIL")
            || k.contains("VILLAIN")
            || k.contains("SCROLL")
            || k.contains("HATE")
            || k.contains("ENOUGH")) {

          if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1) c.sayKey("npc.oogla.lore");
          else {

            c.sayKey("npc.oogla.bash");

            c.npc().provoke();
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if (!"oogla_trade".equals(state)) return false;

        if (yes
            && c.player().getGold() >= 15000
            && c.itemCount("villain_skull") >= 2
            && c.itemCount("scroll_of_hate") >= 2) {

          c.takeItem("villain_skull");

          c.takeItem("villain_skull");

          c.takeItem("scroll_of_hate");

          c.takeItem("scroll_of_hate");

          c.player().addGold(-15000);

          c.giveItem("skull_of_evil");

          c.flag("__FLAG_USER_HAS_SKULL_OF_EVIL", 1);

          c.sayKey("npc.oogla.trade.done");
        }

        return true;
      }
    };
  }

  public TrackerOoglaThraaglurh(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
