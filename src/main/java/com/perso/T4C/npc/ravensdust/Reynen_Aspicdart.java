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
import java.util.List;

public final class Reynen_Aspicdart extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Reynen_Aspicdart";

  public static final String DISPLAY_NAME = "${npc.reynen_aspicdart}";

  public static final String SPRITE_BASE = "Thief";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.reynen_aspicdart}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.reynen_aspicdart.0.0}",
                      "${npc.topic_keyword.reynen_aspicdart.0.1}"),
                  "${npc.topic.reynen_aspicdart.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.reynen_aspicdart.1.0}",
                      "${npc.topic_keyword.reynen_aspicdart.1.1}",
                      "${npc.topic_keyword.reynen_aspicdart.1.2}"),
                  "${npc.topic.reynen_aspicdart.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.reynen_aspicdart.2.0}"),
                  "${npc.topic.reynen_aspicdart.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.reynen_aspicdart.3.0}",
                      "${npc.topic_keyword.reynen_aspicdart.3.1}"),
                  "${npc.topic.reynen_aspicdart.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.reynen_aspicdart.4.0}"),
                  "${npc.topic.reynen_aspicdart.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.reynen_aspicdart.5.0}",
                      "${npc.topic_keyword.reynen_aspicdart.5.1}"),
                  "${npc.topic.reynen_aspicdart.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.reynen_aspicdart.6.0}"),
                  "${npc.topic.reynen_aspicdart.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.reynen_aspicdart.7.0}"),
                  "${npc.topic.reynen_aspicdart.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.reynen_aspicdart.8.0}"),
                  "${npc.topic.reynen_aspicdart.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.reynen_aspicdart.9.0}",
                      "${npc.topic_keyword.reynen_aspicdart.9.1}",
                      "${npc.topic_keyword.reynen_aspicdart.9.2}",
                      "${npc.topic_keyword.reynen_aspicdart.9.3}"),
                  "${npc.topic.reynen_aspicdart.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.reynen_aspicdart.10.0}",
                      "${npc.topic_keyword.reynen_aspicdart.10.1}",
                      "${npc.topic_keyword.reynen_aspicdart.10.2}",
                      "${npc.topic_keyword.reynen_aspicdart.10.3}",
                      "${npc.topic_keyword.reynen_aspicdart.10.4}"),
                  "${npc.topic.reynen_aspicdart.10}",
                  List.of())),
          "Reynen_AspicdartNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("ADRIANA") || k.equals("ADRIANNA")) {

          c.sayKey(
              c.flag("__QUEST_ROYAL_KEY1") >= 4
                  ? "npc.reynen.adriana.yes"
                  : "npc.reynen.adriana.no");

          return true;
        }

        if (k.equals("ROYAL KEY")) {

          int q = c.flag("__QUEST_ROYAL_KEY1");

          c.sayKey(
              q == 0
                  ? "npc.reynen.key.0"
                  : q == 1
                      ? "npc.reynen.key.1"
                      : q == 2
                          ? "npc.reynen.key.2"
                          : q == 3
                              ? "npc.reynen.key.3"
                              : q == 4 ? "npc.reynen.key.4" : "npc.reynen.key.later");

          if (q == 4) c.flag("__QUEST_ROYAL_KEY1", 5);

          return true;
        }

        if (k.equals("CHEST") || k.equals("LOCKED") || k.contains("CHEST LOCK")) {

          if (c.flag("__QUEST_ROYAL_KEY1") == 5) {

            if (!c.hasItem("reynen_key")) c.giveItem("reynen_key");

            c.sayKey("npc.reynen.chest.key");

          } else c.sayKey("npc.reynen.chest.no");

          return true;
        }

        if (k.contains("ASSHOLE") || k.contains("FUCK") || k.contains("SUCK")) {

          if (c.player().getGold() >= 100) c.player().addGold(-(int) (c.player().getGold() * .1));

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public Reynen_Aspicdart(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
