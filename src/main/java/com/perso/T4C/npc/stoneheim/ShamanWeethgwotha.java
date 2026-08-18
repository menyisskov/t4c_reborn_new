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

public final class ShamanWeethgwotha extends ScriptedNpc {

  public static final String ID = "ShamanWeethgwotha";

  public static final String DISPLAY_NAME = "${npc.shamanweethgwotha}";

  public static final String SPRITE_BASE = "64kSkavenShaman";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.shamanweethgwotha}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shamanweethgwotha.0.0}",
                      "${npc.topic_keyword.shamanweethgwotha.0.1}"),
                  "${npc.topic.shamanweethgwotha.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shamanweethgwotha.1.0}",
                      "${npc.topic_keyword.shamanweethgwotha.1.1}"),
                  "${npc.topic.shamanweethgwotha.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shamanweethgwotha.2.0}",
                      "${npc.topic_keyword.shamanweethgwotha.2.1}",
                      "${npc.topic_keyword.shamanweethgwotha.2.2}"),
                  "${npc.topic.shamanweethgwotha.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shamanweethgwotha.3.0}",
                      "${npc.topic_keyword.shamanweethgwotha.3.1}"),
                  "${npc.topic.shamanweethgwotha.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shamanweethgwotha.4.0}",
                      "${npc.topic_keyword.shamanweethgwotha.4.1}",
                      "${npc.topic_keyword.shamanweethgwotha.4.2}",
                      "${npc.topic_keyword.shamanweethgwotha.4.3}",
                      "${npc.topic_keyword.shamanweethgwotha.4.4}"),
                  "${npc.topic.shamanweethgwotha.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shamanweethgwotha.5.0}",
                      "${npc.topic_keyword.shamanweethgwotha.5.1}",
                      "${npc.topic_keyword.shamanweethgwotha.5.2}",
                      "${npc.topic_keyword.shamanweethgwotha.5.3}",
                      "${npc.topic_keyword.shamanweethgwotha.5.4}"),
                  "${npc.topic.shamanweethgwotha.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shamanweethgwotha.6.0}",
                      "${npc.topic_keyword.shamanweethgwotha.6.1}"),
                  "${npc.topic.shamanweethgwotha.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shamanweethgwotha.7.0}",
                      "${npc.topic_keyword.shamanweethgwotha.7.1}"),
                  "${npc.topic.shamanweethgwotha.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shamanweethgwotha.8.0}",
                      "${npc.topic_keyword.shamanweethgwotha.8.1}"),
                  "${npc.topic.shamanweethgwotha.8}",
                  List.of())),
          "ShamanWeethgwothaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1 && c.itemCount("mandrake") >= 5) {

          c.sayKey("npc.weethgwotha.trade");

          c.askYesNo("weeth_trade");

        } else c.sayKey("npc.welcome.shamanweethgwotha");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("ALCHEMY") || k.contains("ALKEMI")) {

          if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1) {

            c.sayKey("npc.weethgwotha.kit.ask");

            c.askYesNo("weeth_kit");

          } else {

            c.sayKey("npc.weethgwotha.hostile");

            c.npc().provoke();
          }

          return true;
        }

        if (k.contains("SCROLL") && k.contains("HATE")) {

          if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1) c.sayKey("npc.weethgwotha.scroll");
          else {

            c.sayKey("npc.weethgwotha.hostile");

            c.npc().provoke();
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("weeth_trade".equals(s)) {

          if (yes && c.itemCount("mandrake") >= 5) {

            for (int i = 0; i < 5; i++) c.takeItem("mandrake");

            c.giveItem("scroll_of_hate");

            c.sayKey("npc.weethgwotha.trade.done");
          }

          return true;
        }

        if ("weeth_kit".equals(s)) {

          if (yes && c.player().getGold() >= 15000) {

            c.player().addGold(-15000);

            c.giveItem("alchemy_kit");

            c.sayKey("npc.weethgwotha.kit.done");

          } else if (yes) c.sayKey("npc.weethgwotha.kit.poor");

          return true;
        }

        return false;
      }
    };
  }

  public ShamanWeethgwotha(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
