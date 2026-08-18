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

public final class Julian extends ScriptedNpc {

  public static final String ID = "Julian";

  public static final String DISPLAY_NAME = "${npc.julian}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.julian}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.0.0}", "${npc.topic_keyword.julian.0.1}"),
                  "${npc.topic.julian.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.julian.1.0}",
                      "${npc.topic_keyword.julian.1.1}",
                      "${npc.topic_keyword.julian.1.2}"),
                  "${npc.topic.julian.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.2.0}", "${npc.topic_keyword.julian.2.1}"),
                  "${npc.topic.julian.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.3.0}", "${npc.topic_keyword.julian.3.1}"),
                  "${npc.topic.julian.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.4.0}"), "${npc.topic.julian.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.julian.5.0}",
                      "${npc.topic_keyword.julian.5.1}",
                      "${npc.topic_keyword.julian.5.2}"),
                  "${npc.topic.julian.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.6.0}", "${npc.topic_keyword.julian.6.1}"),
                  "${npc.topic.julian.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.7.0}", "${npc.topic_keyword.julian.7.1}"),
                  "${npc.topic.julian.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.8.0}", "${npc.topic_keyword.julian.8.1}"),
                  "${npc.topic.julian.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.9.0}"), "${npc.topic.julian.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.10.0}"), "${npc.topic.julian.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.11.0}"), "${npc.topic.julian.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.julian.12.0}"), "${npc.topic.julian.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.julian.13.0}",
                      "${npc.topic_keyword.julian.13.1}",
                      "${npc.topic_keyword.julian.13.2}",
                      "${npc.topic_keyword.julian.13.3}"),
                  "${npc.topic.julian.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.julian.14.0}",
                      "${npc.topic_keyword.julian.14.1}",
                      "${npc.topic_keyword.julian.14.2}",
                      "${npc.topic_keyword.julian.14.3}",
                      "${npc.topic_keyword.julian.14.4}"),
                  "${npc.topic.julian.14}",
                  List.of())),
          "JulianNPC",
          new NpcSpec.CombatProfile(
              200, 1000000, 500, 500, 500, 1000000, 1210, 65535, "1d172+134"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.sayKey(
            c.flag("__QUEST_FIXED_ALIGNMENT") == -1
                ? "npc.julian.welcome.good"
                : "npc.julian.welcome.other");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("ROBE") && k.contains("HELL")) {

          if (c.itemCount("robe_of_hell") >= 3) c.askYesNo("julian_robes");

          return true;
        }

        if (k.contains("NEPHIL") && k.contains("SPIDER") && k.contains("VENOM")) {

          if (c.flag("__QUEST_FIXED_ALIGNMENT") == -1) c.askYesNo("julian_venom");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("julian_robes".equals(s)) {

          if (yes) {

            for (int i = 0; i < 3; i++) c.takeItem("robe_of_hell");

            c.giveItem("gloom_staff");

            c.giveXp(c.player().getLevel() * 2500);

            c.sayKey("npc.julian.robes.ok");
          }

          return true;
        }

        if ("julian_venom".equals(s)) {

          if (yes
              && c.itemCount("spider_venom") >= 3
              && c.hasItem("tarantula_fang")
              && c.hasItem("alchemy_kit")
              && c.player().getGold() >= 20000) {

            for (int i = 0; i < 3; i++) c.takeItem("spider_venom");

            c.takeItem("tarantula_fang");

            c.player().addGold(-20000);

            c.giveItem("black_widow_venom");

            c.giveXp(c.player().getLevel() * 2000);

            c.sayKey("npc.julian.venom.ok");
          }

          return true;
        }

        return false;
      }
    };
  }

  public Julian(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
