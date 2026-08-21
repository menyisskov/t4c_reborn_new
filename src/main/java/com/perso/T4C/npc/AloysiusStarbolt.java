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

@Spawn(type = "AloysiusStarbolt", x = 2949, y = 212, z = 4, stationary = false, aggressive = false)
public final class AloysiusStarbolt extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "AloysiusStarbolt";

  public static final String DISPLAY_NAME = "${npc.aloysiusstarbolt}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.aloysiusstarbolt}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.0.0}"),
                  "${npc.topic.aloysiusstarbolt.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.1.0}"),
                  "${npc.topic.aloysiusstarbolt.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.2.0}"),
                  "${npc.topic.aloysiusstarbolt.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.3.0}"),
                  "${npc.topic.aloysiusstarbolt.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.4.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.4.1}"),
                  "${npc.topic.aloysiusstarbolt.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.5.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.5.1}"),
                  "${npc.topic.aloysiusstarbolt.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.6.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.6.1}",
                      "${npc.topic_keyword.aloysiusstarbolt.6.2}"),
                  "${npc.topic.aloysiusstarbolt.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.7.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.7.1}",
                      "${npc.topic_keyword.aloysiusstarbolt.7.2}"),
                  "${npc.topic.aloysiusstarbolt.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.8.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.8.1}"),
                  "${npc.topic.aloysiusstarbolt.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.9.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.9.1}",
                      "${npc.topic_keyword.aloysiusstarbolt.9.2}"),
                  "${npc.topic.aloysiusstarbolt.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.10.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.10.1}"),
                  "${npc.topic.aloysiusstarbolt.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.11.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.11.1}"),
                  "${npc.topic.aloysiusstarbolt.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.12.0}"),
                  "${npc.topic.aloysiusstarbolt.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.13.0}"),
                  "${npc.topic.aloysiusstarbolt.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.14.0}"),
                  "${npc.topic.aloysiusstarbolt.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.15.0}"),
                  "${npc.topic.aloysiusstarbolt.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.16.0}"),
                  "${npc.topic.aloysiusstarbolt.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.17.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.17.1}",
                      "${npc.topic_keyword.aloysiusstarbolt.17.2}"),
                  "${npc.topic.aloysiusstarbolt.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.18.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.18.1}"),
                  "${npc.topic.aloysiusstarbolt.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.19.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.19.1}"),
                  "${npc.topic.aloysiusstarbolt.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.20.0}"),
                  "${npc.topic.aloysiusstarbolt.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.21.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.21.1}"),
                  "${npc.topic.aloysiusstarbolt.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aloysiusstarbolt.22.0}"),
                  "${npc.topic.aloysiusstarbolt.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.23.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.23.1}",
                      "${npc.topic_keyword.aloysiusstarbolt.23.2}",
                      "${npc.topic_keyword.aloysiusstarbolt.23.3}"),
                  "${npc.topic.aloysiusstarbolt.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aloysiusstarbolt.24.0}",
                      "${npc.topic_keyword.aloysiusstarbolt.24.1}",
                      "${npc.topic_keyword.aloysiusstarbolt.24.2}",
                      "${npc.topic_keyword.aloysiusstarbolt.24.3}",
                      "${npc.topic_keyword.aloysiusstarbolt.24.4}"),
                  "${npc.topic.aloysiusstarbolt.24}",
                  List.of())),
          "Mage",
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
        public void onConversationStart(NpcBehaviorContext c) {

          if (c.flag("__QUEST_KILLED_ORC_MAGUS") == 1) {

            if (c.flag("__QUEST_REWARDED_FOR_MAGUS") > 0) {

              c.flag("__QUEST_KILLED_ORC_MAGUS", 0);

              c.giveItem("ring_of_confidence");

              c.sayKey("npc.aloysius.magushunt.again");

            } else {

              c.flag("__QUEST_KILLED_ORC_MAGUS", 0);

              c.flag("__QUEST_REWARDED_FOR_MAGUS", 1);

              c.giveItem("platinum_ring");

              c.giveXp(25000);

              c.sayKey("npc.aloysius.magushunt.reward");
            }

            return;
          }

          c.sayKey(
              c.globalFlag("__GLOBAL_FLAG_ORC_QUEST") == 1
                  ? "npc.aloysius.disturbance"
                  : "npc.aloysius.welcome");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("PLATINUM RING")) {

            if (c.flag("__QUEST_REWARDED_FOR_MAGUS") == 1 && !c.hasItem("platinum_ring")) {

              c.sayKey("npc.aloysius.ring.offer");

              c.askYesNo("ring");

            } else c.sayKey("npc.aloysius.ring.info");

            return true;
          }

          if (k.equals("SCROLL") || k.equals("DEEP ONE")) {

            if (c.flag("__FLAG_USER_KNOWS_ABOUT_ALOYSIUS") == 0 && c.hasItem("note_deepone")) {

              c.takeItem("note_deepone");

              c.sayKey("npc.aloysius.scroll.destroyed");

            } else c.sayKey("npc.aloysius.scroll");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if (!"ring".equals(state)) return false;

          if (yes && c.player().getGold() >= 50000) {

            c.player().addGold(-50000);

            c.giveItem("platinum_ring");

            c.flag("__QUEST_REWARDED_FOR_MAGUS", 2);

            c.sayKey("npc.aloysius.ring.sold");

          } else if (yes) c.sayKey("npc.aloysius.gold");
          else c.sayKey("npc.aloysius.no");

          return true;
        }
      };

  public AloysiusStarbolt(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
