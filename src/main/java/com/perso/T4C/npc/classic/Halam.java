package com.perso.T4C.npc.classic;

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

public final class Halam extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Halam";

  public static final String DISPLAY_NAME = "${npc.halam}";

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
          "${npc.welcome.halam}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.0.0}", "${npc.topic_keyword.halam.0.1}"),
                  "${npc.topic.halam.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.halam.1.0}",
                      "${npc.topic_keyword.halam.1.1}",
                      "${npc.topic_keyword.halam.1.2}"),
                  "${npc.topic.halam.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.halam.2.0}",
                      "${npc.topic_keyword.halam.2.1}",
                      "${npc.topic_keyword.halam.2.2}"),
                  "${npc.topic.halam.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.3.0}"), "${npc.topic.halam.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.4.0}"), "${npc.topic.halam.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.5.0}"), "${npc.topic.halam.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.6.0}"), "${npc.topic.halam.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.7.0}"), "${npc.topic.halam.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.8.0}"), "${npc.topic.halam.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.9.0}"), "${npc.topic.halam.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.10.0}"), "${npc.topic.halam.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.11.0}"), "${npc.topic.halam.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.12.0}"), "${npc.topic.halam.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.13.0}"), "${npc.topic.halam.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.14.0}"), "${npc.topic.halam.14}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.15.0}"), "${npc.topic.halam.15}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.halam.16.0}"), "${npc.topic.halam.16}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.halam.17.0}",
                      "${npc.topic_keyword.halam.17.1}",
                      "${npc.topic_keyword.halam.17.2}",
                      "${npc.topic_keyword.halam.17.3}"),
                  "${npc.topic.halam.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.halam.18.0}",
                      "${npc.topic_keyword.halam.18.1}",
                      "${npc.topic_keyword.halam.18.2}",
                      "${npc.topic_keyword.halam.18.3}",
                      "${npc.topic_keyword.halam.18.4}"),
                  "${npc.topic.halam.18}",
                  List.of())),
          "HalamNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_GOBLIN_GEENA_GO_SEE_HALAM") == 1) {

          c.flag("__QUEST_GOBLIN_GEENA_GO_SEE_HALAM", 0);

          c.giveItem(Math.random() < .333 ? "girdle_of_courage" : "ring_of_the_bear");

          c.sayKey("npc.halam.geena");

        } else c.sayKey("npc.halam.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        if (text != null && text.toUpperCase(java.util.Locale.ROOT).equals("NIGHTSWORD")) {

          c.askYesNo("halam_bribe");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"halam_bribe".equals(s)) return false;

        if (yes && c.player().getGold() >= 1000) {

          c.player().addGold(-1000);

          c.sayKey("npc.halam.bribe.ok");

        } else if (yes) c.sayKey("npc.halam.bribe.poor");

        return true;
      }
    };
  }

  public Halam(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
