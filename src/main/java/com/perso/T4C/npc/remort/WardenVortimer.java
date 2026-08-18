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
import java.util.List;

public final class WardenVortimer extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "WardenVortimer";

  public static final String DISPLAY_NAME = "${npc.wardenvortimer}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1")),
          0,
          List.of(),
          "${npc.welcome.wardenvortimer}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wardenvortimer.0.0}",
                      "${npc.topic_keyword.wardenvortimer.0.1}"),
                  "${npc.topic.wardenvortimer.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wardenvortimer.1.0}",
                      "${npc.topic_keyword.wardenvortimer.1.1}",
                      "${npc.topic_keyword.wardenvortimer.1.2}"),
                  "${npc.topic.wardenvortimer.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wardenvortimer.2.0}",
                      "${npc.topic_keyword.wardenvortimer.2.1}"),
                  "${npc.topic.wardenvortimer.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wardenvortimer.3.0}",
                      "${npc.topic_keyword.wardenvortimer.3.1}",
                      "${npc.topic_keyword.wardenvortimer.3.2}"),
                  "${npc.topic.wardenvortimer.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.wardenvortimer.4.0}"),
                  "${npc.topic.wardenvortimer.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wardenvortimer.5.0}",
                      "${npc.topic_keyword.wardenvortimer.5.1}",
                      "${npc.topic_keyword.wardenvortimer.5.2}",
                      "${npc.topic_keyword.wardenvortimer.5.3}"),
                  "${npc.topic.wardenvortimer.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wardenvortimer.6.0}",
                      "${npc.topic_keyword.wardenvortimer.6.1}",
                      "${npc.topic_keyword.wardenvortimer.6.2}",
                      "${npc.topic_keyword.wardenvortimer.6.3}",
                      "${npc.topic_keyword.wardenvortimer.6.4}"),
                  "${npc.topic.wardenvortimer.6}",
                  List.of())),
          "WardenVortimerNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.trim().toUpperCase(java.util.Locale.ROOT);

          if (k.equals("DOOR") || k.equals("KEY") || k.equals("ENTER")) {

            if (c.flag("__FLAG_NUMBER_OF_REMORTS") == 0) c.sayKey("npc.wardenvortimer.noRemort");
            else if (!c.hasItem("mad_house_key")) {

              c.sayKey("npc.wardenvortimer.key.offer");

              c.askYesNo("giveKey");

            } else c.sayKey("npc.wardenvortimer.key.have");

            return true;
          }

          if (k.equals("RETURN")) {

            if (!c.hasItem("mad_house_key")) c.sayKey("npc.wardenvortimer.return.none");
            else {

              c.sayKey("npc.wardenvortimer.return.ask");

              c.askYesNo("returnKey");
            }

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean answer) {

          if ("giveKey".equals(state)) {

            if (answer) {

              c.giveItem("mad_house_key");

              c.sayKey("npc.wardenvortimer.key.given");

            } else c.sayKey("npc.wardenvortimer.no");

            return true;
          }

          if ("returnKey".equals(state)) {

            if (answer) {

              while (c.hasItem("mad_house_key")) c.takeItem("mad_house_key");

              c.sayKey("npc.wardenvortimer.returned");

            } else c.sayKey("npc.wardenvortimer.return.cancel");

            return true;
          }

          return false;
        }
      };

  public WardenVortimer(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
