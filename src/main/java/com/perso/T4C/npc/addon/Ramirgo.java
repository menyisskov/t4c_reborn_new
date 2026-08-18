package com.perso.T4C.npc.addon;

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

public final class Ramirgo extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Ramirgo";

  public static final String DISPLAY_NAME = "${npc.ramirgo}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupSimpleStaff")),
          0,
          List.of(),
          "${npc.welcome.ramirgo}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ramirgo.0.0}"), "${npc.topic.ramirgo.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ramirgo.1.0}"), "${npc.topic.ramirgo.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ramirgo.2.0}"), "${npc.topic.ramirgo.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ramirgo.3.0}",
                      "${npc.topic_keyword.ramirgo.3.1}",
                      "${npc.topic_keyword.ramirgo.3.2}",
                      "${npc.topic_keyword.ramirgo.3.3}",
                      "${npc.topic_keyword.ramirgo.3.4}"),
                  "${npc.topic.ramirgo.3}",
                  List.of())),
          "OlinHaad1NPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public Ramirgo(NpcContext context) throws GameException {

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
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          c.sayKey(
              p < 39 ? "npc.ramirgo.before39" : p == 39 ? "npc.ramirgo.39" : "npc.ramirgo.done");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.equals("GUARDIAN")) {

            c.sayKey(p == 39 ? "npc.ramirgo.guardian" : "npc.ramirgo.busy");

            return true;
          }

          if (k.equals("ENTER")) {

            if (p == 39) {

              c.sayKey("npc.ramirgo.enter");

              c.teleport(1746, 1524, 1);

            } else c.sayKey("npc.ramirgo.busy");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.ramirgo.work");

            return true;
          }

          return false;
        }
      };
}
