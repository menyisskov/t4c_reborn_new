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

public final class KingAedenasGravesoul extends ScriptedNpc {

  public static final String ID = "KingAedenasGravesoul";

  public static final String DISPLAY_NAME = "${npc.kingaedenasgravesoul}";

  public static final String SPRITE_BASE = "64kCentaurKing";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.kingaedenasgravesoul}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.0.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.0.1}"),
                  "${npc.topic.kingaedenasgravesoul.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.1.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.1.1}",
                      "${npc.topic_keyword.kingaedenasgravesoul.1.2}"),
                  "${npc.topic.kingaedenasgravesoul.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.2.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.2.1}"),
                  "${npc.topic.kingaedenasgravesoul.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.3.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.3.1}"),
                  "${npc.topic.kingaedenasgravesoul.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.4.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.4.1}"),
                  "${npc.topic.kingaedenasgravesoul.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.5.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.5.1}"),
                  "${npc.topic.kingaedenasgravesoul.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.6.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.6.1}"),
                  "${npc.topic.kingaedenasgravesoul.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.7.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.7.1}"),
                  "${npc.topic.kingaedenasgravesoul.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.8.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.8.1}"),
                  "${npc.topic.kingaedenasgravesoul.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.9.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.9.1}"),
                  "${npc.topic.kingaedenasgravesoul.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.10.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.10.1}"),
                  "${npc.topic.kingaedenasgravesoul.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.11.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.11.1}"),
                  "${npc.topic.kingaedenasgravesoul.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingaedenasgravesoul.12.0}"),
                  "${npc.topic.kingaedenasgravesoul.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingaedenasgravesoul.13.0}"),
                  "${npc.topic.kingaedenasgravesoul.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingaedenasgravesoul.14.0}"),
                  "${npc.topic.kingaedenasgravesoul.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.15.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.15.1}",
                      "${npc.topic_keyword.kingaedenasgravesoul.15.2}"),
                  "${npc.topic.kingaedenasgravesoul.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingaedenasgravesoul.16.0}"),
                  "${npc.topic.kingaedenasgravesoul.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingaedenasgravesoul.17.0}"),
                  "${npc.topic.kingaedenasgravesoul.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.18.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.18.1}",
                      "${npc.topic_keyword.kingaedenasgravesoul.18.2}"),
                  "${npc.topic.kingaedenasgravesoul.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingaedenasgravesoul.19.0}"),
                  "${npc.topic.kingaedenasgravesoul.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingaedenasgravesoul.20.0}"),
                  "${npc.topic.kingaedenasgravesoul.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.21.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.21.1}",
                      "${npc.topic_keyword.kingaedenasgravesoul.21.2}"),
                  "${npc.topic.kingaedenasgravesoul.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kingaedenasgravesoul.22.0}"),
                  "${npc.topic.kingaedenasgravesoul.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.23.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.23.1}",
                      "${npc.topic_keyword.kingaedenasgravesoul.23.2}",
                      "${npc.topic_keyword.kingaedenasgravesoul.23.3}"),
                  "${npc.topic.kingaedenasgravesoul.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kingaedenasgravesoul.24.0}",
                      "${npc.topic_keyword.kingaedenasgravesoul.24.1}",
                      "${npc.topic_keyword.kingaedenasgravesoul.24.2}",
                      "${npc.topic_keyword.kingaedenasgravesoul.24.3}",
                      "${npc.topic_keyword.kingaedenasgravesoul.24.4}"),
                  "${npc.topic.kingaedenasgravesoul.24}",
                  List.of())),
          "KingAedenasGravesoulNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int q = c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST");

        if (q == 16) c.sayKey("npc.aedenas.will");
        else if (q == 14 || q == 15) {

          c.sayKey("npc.aedenas.warning");

          c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 15);

        } else if (q >= 2) c.sayKey("npc.aedenas.return");
        else c.sayKey("npc.aedenas.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("COLLECTOR") && k.contains("BOOK")) {

          if (c.itemCount("collector_book") >= 5) c.askYesNo("aedenas_books");
          else c.sayKey("npc.aedenas.books.need");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"aedenas_books".equals(s)) return false;

        if (yes) {

          for (int i = 0; i < 5; i++) c.takeItem("collector_book");

          c.giveItem("ring_of_the_lion");

          c.giveXp(c.player().getLevel() * 3750);

          c.sayKey("npc.aedenas.books.ok");
        }

        return true;
      }
    };
  }

  public KingAedenasGravesoul(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
