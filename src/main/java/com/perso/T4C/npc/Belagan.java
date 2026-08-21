package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Belagan", x = 1598, y = 2539, z = 0, stationary = false, aggressive = false)
public final class Belagan extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Belagan";

  public static final String DISPLAY_NAME = "${npc.belagan}";

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
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs")),
          0,
          List.of(),
          "${npc.welcome.belagan}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.0.0}", "${npc.topic_keyword.belagan.0.1}"),
                  "${npc.topic.belagan.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.1.0}"), "${npc.topic.belagan.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.2.0}", "${npc.topic_keyword.belagan.2.1}"),
                  "${npc.topic.belagan.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.3.0}"), "${npc.topic.belagan.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.4.0}"), "${npc.topic.belagan.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.5.0}", "${npc.topic_keyword.belagan.5.1}"),
                  "${npc.topic.belagan.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.6.0}"), "${npc.topic.belagan.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.7.0}", "${npc.topic_keyword.belagan.7.1}"),
                  "${npc.topic.belagan.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.8.0}"), "${npc.topic.belagan.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.9.0}"), "${npc.topic.belagan.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.10.0}"),
                  "${npc.topic.belagan.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.11.0}"),
                  "${npc.topic.belagan.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.belagan.12.0}"),
                  "${npc.topic.belagan.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.belagan.13.0}",
                      "${npc.topic_keyword.belagan.13.1}",
                      "${npc.topic_keyword.belagan.13.2}",
                      "${npc.topic_keyword.belagan.13.3}",
                      "${npc.topic_keyword.belagan.13.4}"),
                  "${npc.topic.belagan.13}",
                  List.of())),
          "BelaganNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.contains("REYNEN") || k.contains("ASPICDART")) {

            int q = c.flag("__QUEST_ROYAL_KEY1");

            c.sayKey(
                q == 1
                    ? "npc.belagan.favor"
                    : q == 2
                        ? "npc.belagan.book.need"
                        : q == 3
                            ? "npc.belagan.aspicdart"
                            : q == 4 ? "npc.belagan.done" : "npc.belagan.busy");

            if (q == 1) c.askYesNo("favor");

            if (q == 3) c.flag("__QUEST_ROYAL_KEY1", 4);

            return true;
          }

          if (k.contains("BOOK") && k.contains("WARFARE")) {

            if (c.flag("__QUEST_ROYAL_KEY1") == 2 && c.hasItem("book_of_warfare")) {

              c.takeItem("book_of_warfare");

              c.flag("__QUEST_ROYAL_KEY1", 3);

              c.sayKey("npc.belagan.book.given");

            } else c.sayKey("npc.belagan.book.info");

            return true;
          }

          if (k.equals("VISITOR")) {

            int v = c.flag("__QUEST_VISITOR_SPOTTED");

            if (v == 0) {

              c.flag("__QUEST_VISITOR_SPOTTED", c.flag("__QUEST_DAMIEN_SUBPLOT") == 2 ? 1 : 0);

              c.sayKey("npc.belagan.visitor.first");

            } else if (v == 1) {

              c.flag("__QUEST_VISITOR_SPOTTED", 2);

              c.sayKey("npc.belagan.visitor.second");

            } else c.sayKey("npc.belagan.visitor.done");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if (!"favor".equals(state)) return false;

          if (yes) {

            c.sayKey("npc.belagan.favor.ask");

            c.flag("__QUEST_ROYAL_KEY1", 2);

          } else c.sayKey("npc.belagan.no");

          return true;
        }
      };

  public Belagan(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
