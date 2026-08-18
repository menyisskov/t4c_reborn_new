package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CelestinaWaterbreeze extends ScriptedNpc {

  public static final String ID = "CelestinaWaterbreeze";

  public static final String DISPLAY_NAME = "${npc.celestinawaterbreeze}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoMageRobe"),
              new NpcSpec.Part(BodyPart.BACK, "WoMageRobeUnder"),
              new NpcSpec.Part(BodyPart.BOOT, "WoBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.celestinawaterbreeze}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.0.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.0.1}"),
                  "${npc.topic.celestinawaterbreeze.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.1.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.1.1}"),
                  "${npc.topic.celestinawaterbreeze.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.2.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.2.1}"),
                  "${npc.topic.celestinawaterbreeze.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.3.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.3.1}",
                      "${npc.topic_keyword.celestinawaterbreeze.3.2}"),
                  "${npc.topic.celestinawaterbreeze.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.4.0}"),
                  "${npc.topic.celestinawaterbreeze.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.5.0}"),
                  "${npc.topic.celestinawaterbreeze.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.6.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.6.1}",
                      "${npc.topic_keyword.celestinawaterbreeze.6.2}"),
                  "${npc.topic.celestinawaterbreeze.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.7.0}"),
                  "${npc.topic.celestinawaterbreeze.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.8.0}"),
                  "${npc.topic.celestinawaterbreeze.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.9.0}"),
                  "${npc.topic.celestinawaterbreeze.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.10.0}"),
                  "${npc.topic.celestinawaterbreeze.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.11.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.11.1}"),
                  "${npc.topic.celestinawaterbreeze.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.12.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.12.1}"),
                  "${npc.topic.celestinawaterbreeze.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.13.0}"),
                  "${npc.topic.celestinawaterbreeze.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.14.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.14.1}"),
                  "${npc.topic.celestinawaterbreeze.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.15.0}"),
                  "${npc.topic.celestinawaterbreeze.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.16.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.16.1}"),
                  "${npc.topic.celestinawaterbreeze.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.17.0}"),
                  "${npc.topic.celestinawaterbreeze.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.18.0}"),
                  "${npc.topic.celestinawaterbreeze.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.19.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.19.1}",
                      "${npc.topic_keyword.celestinawaterbreeze.19.2}"),
                  "${npc.topic.celestinawaterbreeze.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.20.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.20.1}"),
                  "${npc.topic.celestinawaterbreeze.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.21.0}"),
                  "${npc.topic.celestinawaterbreeze.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celestinawaterbreeze.22.0}"),
                  "${npc.topic.celestinawaterbreeze.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.23.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.23.1}",
                      "${npc.topic_keyword.celestinawaterbreeze.23.2}",
                      "${npc.topic_keyword.celestinawaterbreeze.23.3}"),
                  "${npc.topic.celestinawaterbreeze.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celestinawaterbreeze.24.0}",
                      "${npc.topic_keyword.celestinawaterbreeze.24.1}",
                      "${npc.topic_keyword.celestinawaterbreeze.24.2}",
                      "${npc.topic_keyword.celestinawaterbreeze.24.3}",
                      "${npc.topic_keyword.celestinawaterbreeze.24.4}"),
                  "${npc.topic.celestinawaterbreeze.24}",
                  List.of())),
          "CelestinaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private final String key = "QUEST_ROYAL_KEY6";

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        if (text == null) return false;

        String k = text.toUpperCase();

        if (k.contains("ROYAL KEY")) {

          if (c.flag(key) >= 1) c.askYesNo("CELESTINA_GET_KEY");
          else c.sayKey("message.celestina.key_unknown");

          return true;
        }

        if (k.contains("GIVE KEY")) {

          int s = c.flag(key);

          if (s >= 2 && s <= 4) {

            if (c.karma() >= c.flag("QUEST_TARGET_KARMA")) {

              c.flag(key, 5);

              c.sayKey("message.celestina.key_mordrick");

            } else c.sayKey("message.celestina.karma_not_enough");

          } else if (s == 5) c.sayKey("message.celestina.key_already");
          else c.sayKey("message.celestina.key_unknown");

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean answer) {

        if ("CELESTINA_GET_KEY".equals(state)) {

          if (answer) c.askYesNo("CELESTINA_KILLED_GUARDIAN");
          else c.sayKey("message.celestina.key_no");

          return true;
        }

        if (!"CELESTINA_KILLED_GUARDIAN".equals(state)) return false;

        int s = c.flag(key);

        boolean killed = c.flag("GUARDIANS_KILLED") >= 1;

        if (answer && killed) {

          c.sayKey("message.celestina.admits_kill");

          adjust(c, 75, 50, 0);

          if (s == 1) c.flag(key, 2);

        } else if (answer) {

          c.sayKey("message.celestina.lie");

          adjust(c, 50, 25, 0);

          if (s == 1) c.flag(key, 3);

        } else if (killed) {

          c.sayKey("message.celestina.denial");

          adjust(c, 75, 50, 0);

          if (s == 1) c.flag(key, 4);

        } else {

          c.sayKey("message.celestina.honest");

          c.flag(key, 5);
        }

        return true;
      }

      private void adjust(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, int low, int med, int high) {

        int k = c.karma();

        c.flag("QUEST_TARGET_KARMA", k <= 100 ? k + low : k <= 150 ? k + med : k + high);
      }
    };
  }

  public CelestinaWaterbreeze(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
