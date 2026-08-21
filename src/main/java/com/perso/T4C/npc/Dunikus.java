package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Dunikus", x = 2831, y = 146, z = 0, stationary = false, aggressive = false)
public final class Dunikus extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Dunikus";

  public static final String DISPLAY_NAME = "${npc.dunikus}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupMageRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.HEAD, "PupNakedHead"),
              new NpcSpec.Part(BodyPart.HAT, "PupElvenHat"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupLichStaff")),
          0,
          List.of(),
          "${npc.welcome.dunikus}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.0.0}", "${npc.topic_keyword.dunikus.0.1}"),
                  "${npc.topic.dunikus.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dunikus.1.0}",
                      "${npc.topic_keyword.dunikus.1.1}",
                      "${npc.topic_keyword.dunikus.1.2}"),
                  "${npc.topic.dunikus.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.2.0}", "${npc.topic_keyword.dunikus.2.1}"),
                  "${npc.topic.dunikus.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dunikus.3.0}",
                      "${npc.topic_keyword.dunikus.3.1}",
                      "${npc.topic_keyword.dunikus.3.2}"),
                  "${npc.topic.dunikus.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.4.0}"), "${npc.topic.dunikus.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.5.0}"), "${npc.topic.dunikus.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.6.0}"), "${npc.topic.dunikus.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.7.0}", "${npc.topic_keyword.dunikus.7.1}"),
                  "${npc.topic.dunikus.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.8.0}", "${npc.topic_keyword.dunikus.8.1}"),
                  "${npc.topic.dunikus.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.9.0}", "${npc.topic_keyword.dunikus.9.1}"),
                  "${npc.topic.dunikus.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.10.0}"),
                  "${npc.topic.dunikus.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.11.0}"),
                  "${npc.topic.dunikus.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.12.0}", "${npc.topic_keyword.dunikus.12.1}"),
                  "${npc.topic.dunikus.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dunikus.13.0}", "${npc.topic_keyword.dunikus.13.1}"),
                  "${npc.topic.dunikus.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dunikus.14.0}",
                      "${npc.topic_keyword.dunikus.14.1}",
                      "${npc.topic_keyword.dunikus.14.2}",
                      "${npc.topic_keyword.dunikus.14.3}"),
                  "${npc.topic.dunikus.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dunikus.15.0}",
                      "${npc.topic_keyword.dunikus.15.1}",
                      "${npc.topic_keyword.dunikus.15.2}"),
                  "${npc.topic.dunikus.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dunikus.16.0}",
                      "${npc.topic_keyword.dunikus.16.1}",
                      "${npc.topic_keyword.dunikus.16.2}"),
                  "${npc.topic.dunikus.16}",
                  List.of())),
          "DunikusNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private final String quest = "QUEST_DRUIDIC_FLASK";

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String keyword) {

        if (keyword == null || !keyword.toUpperCase().contains("POTION")) return false;

        int state = c.flag(quest);

        if (state < 1) {

          c.sayKey("message.dunikus.potion.ask");

          c.askYesNo("DUNIKUS_POTION");

        } else if (state == 1) {

          if (c.hasItem("apple") && c.hasItem("leaf_of_a_pink_tree")) finish(c);
          else if (c.hasItem("apple")) {

            c.takeItem("apple");

            c.flag(quest, 2);

            c.sayKey("message.dunikus.potion.need_leaf");

          } else if (c.hasItem("leaf_of_a_pink_tree")) {

            c.takeItem("leaf_of_a_pink_tree");

            c.flag(quest, 3);

            c.sayKey("message.dunikus.potion.need_apple");

          } else c.sayKey("message.dunikus.potion.need_both");

        } else if (state == 2) {

          if (c.hasItem("leaf_of_a_pink_tree")) {

            c.takeItem("leaf_of_a_pink_tree");

            finish(c);

          } else c.sayKey("message.dunikus.potion.need_leaf");

        } else if (state == 3) {

          if (c.hasItem("apple")) {

            c.takeItem("apple");

            finish(c);

          } else c.sayKey("message.dunikus.potion.need_apple");
        }

        return true;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean answer) {

        if (!"DUNIKUS_POTION".equals(state)) return false;

        if (answer) {

          c.sayKey("message.dunikus.potion.accept");

          c.flag(quest, 1);

        } else c.sayKey("message.dunikus.potion.decline");

        return true;
      }

      private void finish(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.takeItem("apple");

        c.takeItem("leaf_of_a_pink_tree");

        c.giveItem("flask_of_bluish_liquid");

        c.flag(quest, 0);

        c.sayKey("message.dunikus.potion.complete");
      }
    };
  }

  public Dunikus(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
