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
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Glaenshenmilandira extends ScriptedNpc {

  public static final String ID = "Glaenshenmilandira";

  public static final String DISPLAY_NAME = "${npc.glaenshenmilandira}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe"),
              new NpcSpec.Part(BodyPart.BACK, "PupSeraphWhiteWings")),
          0,
          List.of(),
          "${npc.welcome.glaenshenmilandira}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.glaenshenmilandira.0.0}",
                      "${npc.topic_keyword.glaenshenmilandira.0.1}"),
                  "${npc.topic.glaenshenmilandira.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.glaenshenmilandira.1.0}",
                      "${npc.topic_keyword.glaenshenmilandira.1.1}",
                      "${npc.topic_keyword.glaenshenmilandira.1.2}"),
                  "${npc.topic.glaenshenmilandira.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.glaenshenmilandira.2.0}"),
                  "${npc.topic.glaenshenmilandira.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.glaenshenmilandira.3.0}"),
                  "${npc.topic.glaenshenmilandira.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.glaenshenmilandira.4.0}",
                      "${npc.topic_keyword.glaenshenmilandira.4.1}"),
                  "${npc.topic.glaenshenmilandira.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.glaenshenmilandira.5.0}",
                      "${npc.topic_keyword.glaenshenmilandira.5.1}"),
                  "${npc.topic.glaenshenmilandira.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.glaenshenmilandira.6.0}"),
                  "${npc.topic.glaenshenmilandira.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.glaenshenmilandira.7.0}"),
                  "${npc.topic.glaenshenmilandira.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.glaenshenmilandira.8.0}",
                      "${npc.topic_keyword.glaenshenmilandira.8.1}",
                      "${npc.topic_keyword.glaenshenmilandira.8.2}",
                      "${npc.topic_keyword.glaenshenmilandira.8.3}",
                      "${npc.topic_keyword.glaenshenmilandira.8.4}"),
                  "${npc.topic.glaenshenmilandira.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.glaenshenmilandira.9.0}",
                      "${npc.topic_keyword.glaenshenmilandira.9.1}",
                      "${npc.topic_keyword.glaenshenmilandira.9.2}",
                      "${npc.topic_keyword.glaenshenmilandira.9.3}"),
                  "${npc.topic.glaenshenmilandira.9}",
                  List.of())),
          "GlaenshenmilandiraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int q = c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST");

        if (q == 16
            && c.flag("__FLAG_COUNTER_PURIFIER_KILLED")
                <= c.flag("__FLAG_COUNTER_NEOFLARE_KILLED")) {

          c.sayKey("npc.glaen.will.ask");

          c.askYesNo("glaen_will");

        } else if (q == 17) c.sayKey("npc.glaen.will.done");
        else if (q == 16) c.sayKey("npc.glaen.will.atone");
        else c.sayKey("npc.glaen.will.none");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("LUTE") && k.contains("PEACE")) {

          int q = c.flag("__QUEST_DIONYSUS_RECHARGE_HARP");

          if (q == 1) {

            c.flag("__QUEST_DIONYSUS_RECHARGE_HARP", 2);

            c.sayKey("npc.glaen.lute.ask");

          } else if (q == 2 && c.itemCount("magical_lute") >= 6) c.askYesNo("glaen_lute");
          else if (q == 2) c.sayKey("npc.glaen.lute.need");
          else if (q == 3) c.sayKey("npc.glaen.lute.done");
          else c.sayKey("npc.glaen.lute.none");

          return true;
        }

        if (k.equals("RETURN")) {

          c.teleport(205, 671, 0);

          c.sayKey("npc.glaen.return");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if ("glaen_will".equals(state)) {

          if (yes) {

            while (c.hasItem("fang_of_true_resolve")) c.takeItem("fang_of_true_resolve");

            while (c.hasItem("hammer_of_finality")) c.takeItem("hammer_of_finality");

            while (c.hasItem("drum_of_fate")) c.takeItem("drum_of_fate");

            c.giveItem("will_of_artherk");

            c.flag("__QUEST_FLAG_ARTHERK_WILL", 1);

            c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 17);

            c.giveXp(250000);

            c.sayKey("npc.glaen.will.created");
          }

          return true;
        }

        if ("glaen_lute".equals(state)) {

          if (!yes) return true;

          int n = c.itemCount("magical_lute");

          if (n < 6) return true;

          for (int i = 0; i < n; i++) c.takeItem("magical_lute");

          if (Math.random() * 100 < Math.min(100, 30 + n * 5)) {

            c.flag("__QUEST_DIONYSUS_RECHARGE_HARP", 3);

            c.giveXp(c.player().getLevel() * 2000);

            c.sayKey("npc.glaen.lute.success");

          } else c.sayKey("npc.glaen.lute.fail");

          return true;
        }

        return false;
      }
    };
  }

  public Glaenshenmilandira(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
