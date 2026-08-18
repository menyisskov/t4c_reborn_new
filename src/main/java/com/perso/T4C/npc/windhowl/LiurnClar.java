package com.perso.T4C.npc.windhowl;

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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "LiurnClar", x = 1599, y = 1250, z = 0, stationary = false, aggressive = false)
public final class LiurnClar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "LiurnClar";

  public static final String DISPLAY_NAME = "${npc.liurnclar}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.HEAD, "PupHornedHelmet")),
          0,
          List.of(),
          "${npc.welcome.liurnclar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.liurnclar.0.0}", "${npc.topic_keyword.liurnclar.0.1}"),
                  "${npc.topic.liurnclar.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.liurnclar.1.0}",
                      "${npc.topic_keyword.liurnclar.1.1}",
                      "${npc.topic_keyword.liurnclar.1.2}"),
                  "${npc.topic.liurnclar.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.liurnclar.2.0}",
                      "${npc.topic_keyword.liurnclar.2.1}",
                      "${npc.topic_keyword.liurnclar.2.2}"),
                  "${npc.topic.liurnclar.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.3.0}"),
                  "${npc.topic.liurnclar.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.4.0}"),
                  "${npc.topic.liurnclar.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.5.0}"),
                  "${npc.topic.liurnclar.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.6.0}"),
                  "${npc.topic.liurnclar.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.liurnclar.7.0}", "${npc.topic_keyword.liurnclar.7.1}"),
                  "${npc.topic.liurnclar.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.8.0}"),
                  "${npc.topic.liurnclar.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.9.0}"),
                  "${npc.topic.liurnclar.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.10.0}"),
                  "${npc.topic.liurnclar.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.11.0}"),
                  "${npc.topic.liurnclar.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.12.0}"),
                  "${npc.topic.liurnclar.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.13.0}"),
                  "${npc.topic.liurnclar.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.14.0}"),
                  "${npc.topic.liurnclar.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.15.0}"),
                  "${npc.topic.liurnclar.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.16.0}"),
                  "${npc.topic.liurnclar.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.17.0}"),
                  "${npc.topic.liurnclar.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.18.0}"),
                  "${npc.topic.liurnclar.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.19.0}"),
                  "${npc.topic.liurnclar.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.liurnclar.20.0}"),
                  "${npc.topic.liurnclar.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.liurnclar.21.0}",
                      "${npc.topic_keyword.liurnclar.21.1}",
                      "${npc.topic_keyword.liurnclar.21.2}",
                      "${npc.topic_keyword.liurnclar.21.3}"),
                  "${npc.topic.liurnclar.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.liurnclar.22.0}",
                      "${npc.topic_keyword.liurnclar.22.1}",
                      "${npc.topic_keyword.liurnclar.22.2}"),
                  "${npc.topic.liurnclar.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.liurnclar.23.0}",
                      "${npc.topic_keyword.liurnclar.23.1}",
                      "${npc.topic_keyword.liurnclar.23.2}",
                      "${npc.topic_keyword.liurnclar.23.3}"),
                  "${npc.topic.liurnclar.23}",
                  List.of())),
          "Wizard_Liurn_Clar",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("TOME") && k.contains("VALOR")) {

          if (c.flag("__FLAG_LIGHTBRINGER_OF_ARTHERK") == 0)
            c.sayKey("npc.liurn.tome.not_lightbringer");
          else if (c.flag("__QUEST_LIURN_TOME_OF_VALOR") == 0) {

            c.sayKey("npc.liurn.tome.ask");

            if (c.hasItem("magic_scripting_kit")
                && c.hasItem("chaos_key")
                && c.hasItem("blank_magical_tome")) c.askYesNo("liurn_tome");

          } else if ((int) (System.currentTimeMillis() / 1000)
              >= c.flag("__QUEST_TIMER_TOME_OF_VALOR")) {

            c.giveItem("tome_of_valor");

            c.flag("__QUEST_LIURN_TOME_OF_VALOR", 0);

            c.flag("__QUEST_TIMER_TOME_OF_VALOR", 0);

            c.sayKey("npc.liurn.tome.done");

          } else c.sayKey("npc.liurn.tome.wait");

          return true;
        }

        if (k.equals("TELEPORT")) {

          if (c.flag("__QUEST_ISLAND_ACCESS") >= 1) {

            c.sayKey("npc.liurn.teleport.ask");

            c.askYesNo("liurn_teleport");

          } else
            c.sayKey(
                c.flag("__QUEST_KALASTOR_MISSION") >= 1
                    ? "npc.liurn.teleport.denied"
                    : "npc.liurn.teleport.info");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!yes) return true;

        if ("liurn_tome".equals(s)) {

          if (c.hasItem("magic_scripting_kit")
              && c.hasItem("chaos_key")
              && c.hasItem("blank_magical_tome")) {

            c.takeItem("magic_scripting_kit");

            c.takeItem("chaos_key");

            c.takeItem("blank_magical_tome");

            c.flag("__QUEST_LIURN_TOME_OF_VALOR", 1);

            c.flag("__QUEST_TIMER_TOME_OF_VALOR", (int) (System.currentTimeMillis() / 1000) + 3600);

            c.sayKey("npc.liurn.tome.started");

          } else c.sayKey("npc.liurn.tome.missing");

          return true;
        }

        if ("liurn_teleport".equals(s)) {

          int fee = c.player().getLevel() * 100;

          if (c.player().getGold() >= fee) {

            c.player().addGold(-fee);

            c.teleport(1495, 2470, 0);

            c.sayKey("npc.liurn.teleport.done");

          } else c.sayKey("npc.liurn.teleport.poor");

          return true;
        }

        return false;
      }
    };
  }

  public LiurnClar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
