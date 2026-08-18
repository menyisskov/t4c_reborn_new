package com.perso.T4C.npc.ravensdust;

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

@Spawn(type = "Samuel", x = 2819, y = 493, z = 4, stationary = false, aggressive = false)
public final class Samuel extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Samuel";

  public static final String DISPLAY_NAME = "${npc.samuel}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.samuel}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.0.0}", "${npc.topic_keyword.samuel.0.1}"),
                  "${npc.topic.samuel.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.1.0}", "${npc.topic_keyword.samuel.1.1}"),
                  "${npc.topic.samuel.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.2.0}"), "${npc.topic.samuel.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.3.0}"), "${npc.topic.samuel.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.samuel.4.0}",
                      "${npc.topic_keyword.samuel.4.1}",
                      "${npc.topic_keyword.samuel.4.2}"),
                  "${npc.topic.samuel.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.5.0}"), "${npc.topic.samuel.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.6.0}"), "${npc.topic.samuel.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.7.0}"), "${npc.topic.samuel.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.8.0}"), "${npc.topic.samuel.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.9.0}"), "${npc.topic.samuel.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.10.0}"), "${npc.topic.samuel.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.11.0}", "${npc.topic_keyword.samuel.11.1}"),
                  "${npc.topic.samuel.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.12.0}", "${npc.topic_keyword.samuel.12.1}"),
                  "${npc.topic.samuel.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.13.0}"), "${npc.topic.samuel.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.14.0}", "${npc.topic_keyword.samuel.14.1}"),
                  "${npc.topic.samuel.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.15.0}", "${npc.topic_keyword.samuel.15.1}"),
                  "${npc.topic.samuel.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.16.0}"), "${npc.topic.samuel.16}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.17.0}"), "${npc.topic.samuel.17}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.samuel.18.0}"), "${npc.topic.samuel.18}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.samuel.19.0}",
                      "${npc.topic_keyword.samuel.19.1}",
                      "${npc.topic_keyword.samuel.19.2}",
                      "${npc.topic_keyword.samuel.19.3}"),
                  "${npc.topic.samuel.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.samuel.20.0}",
                      "${npc.topic_keyword.samuel.20.1}",
                      "${npc.topic_keyword.samuel.20.2}",
                      "${npc.topic_keyword.samuel.20.3}"),
                  "${npc.topic.samuel.20}",
                  List.of())),
          "Nobleman",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("DEATH JESTER")) {

          if (c.flag("__QUEST_FLAG_DEATH_JESTER_DONE") == 1) c.sayKey("npc.samuel.jester.done");
          else if (c.flag("__QUEST_FLAG_KILLED_DEATH_JESTER") == 1
              && c.itemCount("death_jester_skull") > 0) {

            while (c.hasItem("death_jester_skull")) c.takeItem("death_jester_skull");

            c.giveItem("scarab_protector");

            c.giveXp(30000);

            c.flag("__QUEST_FLAG_KILLED_DEATH_JESTER", 0);

            c.flag("__QUEST_FLAG_DEATH_JESTER_DONE", 1);

            c.sayKey("npc.samuel.jester.reward");

          } else if (c.itemCount("death_jester_skull") > 0) {

            while (c.hasItem("death_jester_skull")) c.takeItem("death_jester_skull");

            c.sayKey("npc.samuel.jester.unworthy");

          } else c.sayKey("npc.samuel.jester.ask");

          return true;
        }

        if (k.contains("WAX") && k.contains("APPLE")) {

          if (c.flag("__QUEST_WAX_APPLE") == 0) c.sayKey("npc.samuel.apple.locked");
          else if (c.flag("__QUEST_FLAG_DEATH_JESTER_DONE") == 1) {

            c.sayKey("npc.samuel.apple.ask");

            c.askYesNo("samuel_apple");

          } else c.sayKey("npc.samuel.apple.jester");

          return true;
        }

        if (k.contains("CANDLE") && k.contains("ETERNITY")) {

          c.sayKey("npc.samuel.candle.info");

          if (c.itemCount("wasp_wax") >= 5 && c.player().getGold() >= 5000)
            c.askYesNo("samuel_candle");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if ("samuel_apple".equals(state)) {

          if (!yes) return true;

          int wax = c.itemCount("wasp_wax");

          if (wax < 10) {

            c.sayKey("npc.samuel.apple.missing");

            return true;
          }

          if (c.player().getGold() < 1000) {

            c.sayKey("npc.samuel.apple.gold");

            return true;
          }

          for (int i = 0; i < 10; i++) c.takeItem("wasp_wax");

          c.player().addGold(-1000);

          if (Math.random() > .1) c.giveItem("wax_apple");

          c.sayKey("npc.samuel.apple.result");

          return true;
        }

        if ("samuel_candle".equals(state)) {

          if (!yes) return true;

          if (c.itemCount("wasp_wax") < 5 || c.player().getGold() < 5000) {

            c.sayKey("npc.samuel.candle.missing");

            return true;
          }

          for (int i = 0; i < 5; i++) c.takeItem("wasp_wax");

          c.player().addGold(-5000);

          c.giveItem("candle_of_eternity");

          c.sayKey("npc.samuel.candle.done");

          return true;
        }

        return false;
      }
    };
  }

  public Samuel(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
