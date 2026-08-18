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

public final class Eldantor extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Eldantor";

  public static final String DISPLAY_NAME = "${npc.eldantor}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.eldantor}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.0.0}", "${npc.topic_keyword.eldantor.0.1}"),
                  "${npc.topic.eldantor.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eldantor.1.0}",
                      "${npc.topic_keyword.eldantor.1.1}",
                      "${npc.topic_keyword.eldantor.1.2}"),
                  "${npc.topic.eldantor.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.2.0}", "${npc.topic_keyword.eldantor.2.1}"),
                  "${npc.topic.eldantor.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.3.0}"),
                  "${npc.topic.eldantor.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.4.0}"),
                  "${npc.topic.eldantor.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.5.0}"),
                  "${npc.topic.eldantor.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.6.0}", "${npc.topic_keyword.eldantor.6.1}"),
                  "${npc.topic.eldantor.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.7.0}"),
                  "${npc.topic.eldantor.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.8.0}"),
                  "${npc.topic.eldantor.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.9.0}"),
                  "${npc.topic.eldantor.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eldantor.10.0}", "${npc.topic_keyword.eldantor.10.1}"),
                  "${npc.topic.eldantor.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.11.0}"),
                  "${npc.topic.eldantor.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.12.0}"),
                  "${npc.topic.eldantor.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eldantor.13.0}",
                      "${npc.topic_keyword.eldantor.13.1}",
                      "${npc.topic_keyword.eldantor.13.2}",
                      "${npc.topic_keyword.eldantor.13.3}"),
                  "${npc.topic.eldantor.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eldantor.14.0}",
                      "${npc.topic_keyword.eldantor.14.1}",
                      "${npc.topic_keyword.eldantor.14.2}",
                      "${npc.topic_keyword.eldantor.14.3}",
                      "${npc.topic_keyword.eldantor.14.4}"),
                  "${npc.topic.eldantor.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldantor.15.0}"),
                  "${npc.topic.eldantor.15}",
                  List.of())),
          "EldantorNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("MAGIC SCRIPTING KIT")) {

          c.sayKey("npc.eldantor.kit.ask");

          c.askYesNo("eldantor_kit");

          return true;
        }

        if (k.equals("ORACLE")) {

          c.sayKey(
              c.flag("__QUEST_FIXED_ALIGNMENT") >= 1
                  ? "npc.eldantor.oracle.good"
                  : "npc.eldantor.oracle.bad");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"eldantor_kit".equals(s)) return false;

        if (yes && c.player().getGold() >= 20000) {

          c.player().addGold(-20000);

          c.giveItem("magic_scripting_kit");

          c.sayKey("npc.eldantor.kit.done");

        } else if (yes) c.sayKey("npc.eldantor.kit.gold");

        return true;
      }
    };
  }

  public Eldantor(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
