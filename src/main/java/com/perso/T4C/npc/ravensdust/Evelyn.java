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
import java.util.List;

public final class Evelyn extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Evelyn";

  public static final String DISPLAY_NAME = "${npc.evelyn}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape")),
          0,
          List.of(),
          "${npc.welcome.evelyn}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.evelyn.0.0}", "${npc.topic_keyword.evelyn.0.1}"),
                  "${npc.topic.evelyn.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.evelyn.1.0}"), "${npc.topic.evelyn.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.evelyn.2.0}",
                      "${npc.topic_keyword.evelyn.2.1}",
                      "${npc.topic_keyword.evelyn.2.2}"),
                  "${npc.topic.evelyn.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.evelyn.3.0}"), "${npc.topic.evelyn.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.evelyn.4.0}", "${npc.topic_keyword.evelyn.4.1}"),
                  "${npc.topic.evelyn.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.evelyn.5.0}", "${npc.topic_keyword.evelyn.5.1}"),
                  "${npc.topic.evelyn.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.evelyn.6.0}", "${npc.topic_keyword.evelyn.6.1}"),
                  "${npc.topic.evelyn.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.evelyn.7.0}", "${npc.topic_keyword.evelyn.7.1}"),
                  "${npc.topic.evelyn.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.evelyn.8.0}",
                      "${npc.topic_keyword.evelyn.8.1}",
                      "${npc.topic_keyword.evelyn.8.2}",
                      "${npc.topic_keyword.evelyn.8.3}"),
                  "${npc.topic.evelyn.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.evelyn.9.0}",
                      "${npc.topic_keyword.evelyn.9.1}",
                      "${npc.topic_keyword.evelyn.9.2}",
                      "${npc.topic_keyword.evelyn.9.3}",
                      "${npc.topic_keyword.evelyn.9.4}"),
                  "${npc.topic.evelyn.9}",
                  List.of())),
          "EvelynNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int h = java.time.LocalTime.now().getHour();

        c.sayKey(h >= 18 || h < 6 ? "npc.evelyn.night" : "npc.evelyn.day");
      }

      @Override
      public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.castSelfSpell(10729);
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT).trim();

        if ((k.contains("WILL") && k.contains("RING"))
            && c.hasItem("iron_ring")
            && c.hasItem("pouch_of_willow_bark")) {

          c.askYesNo("willowisp");

          return true;
        }

        if (k.equals("FUCK") || k.equals("SUCK") || k.equals("ASSHOLE") || k.equals("ASS")) {

          c.npc().provoke();

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if (!"willowisp".equals(state)) return false;

        if (yes && c.hasItem("iron_ring") && c.hasItem("pouch_of_willow_bark")) {

          c.takeItem("iron_ring");

          c.takeItem("pouch_of_willow_bark");

          c.giveItem("willowisp_ring");
        }

        return true;
      }
    };
  }

  public Evelyn(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
