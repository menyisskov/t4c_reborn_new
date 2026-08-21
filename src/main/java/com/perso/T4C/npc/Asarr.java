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

@Spawn(type = "Asarr", x = 2139, y = 1226, z = 0, stationary = false, aggressive = false)
public final class Asarr extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Asarr";

  public static final String DISPLAY_NAME = "${npc.asarr}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupElvenHat"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.asarr}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.0.0}"), "${npc.topic.asarr.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.1.0}"), "${npc.topic.asarr.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.2.0}", "${npc.topic_keyword.asarr.2.1}"),
                  "${npc.topic.asarr.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.asarr.3.0}",
                      "${npc.topic_keyword.asarr.3.1}",
                      "${npc.topic_keyword.asarr.3.2}"),
                  "${npc.topic.asarr.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.4.0}"), "${npc.topic.asarr.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.5.0}", "${npc.topic_keyword.asarr.5.1}"),
                  "${npc.topic.asarr.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.asarr.6.0}",
                      "${npc.topic_keyword.asarr.6.1}",
                      "${npc.topic_keyword.asarr.6.2}"),
                  "${npc.topic.asarr.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.7.0}"), "${npc.topic.asarr.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.8.0}"), "${npc.topic.asarr.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.9.0}"), "${npc.topic.asarr.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.10.0}"), "${npc.topic.asarr.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.11.0}"), "${npc.topic.asarr.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.12.0}"), "${npc.topic.asarr.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.13.0}"), "${npc.topic.asarr.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.14.0}", "${npc.topic_keyword.asarr.14.1}"),
                  "${npc.topic.asarr.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.15.0}"), "${npc.topic.asarr.15}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.16.0}"), "${npc.topic.asarr.16}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.17.0}"), "${npc.topic.asarr.17}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.18.0}"), "${npc.topic.asarr.18}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.asarr.19.0}"), "${npc.topic.asarr.19}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.asarr.20.0}",
                      "${npc.topic_keyword.asarr.20.1}",
                      "${npc.topic_keyword.asarr.20.2}",
                      "${npc.topic_keyword.asarr.20.3}",
                      "${npc.topic_keyword.asarr.20.4}"),
                  "${npc.topic.asarr.20}",
                  List.of())),
          "Brigand_Leader",
          new NpcSpec.CombatProfile(100, 1000000, 65, 65, 65, 1000000, 250, 65535, "1d23+16"));

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

          if (k.equals("JOB")) {

            c.sayKey("npc.asarr.job.ask");

            c.askYesNo("job");

            return true;
          }

          if (k.equals("REWARD")) {

            int q = c.flag("__QUEST_BRIGAND_ROB_DIAMOND");

            c.sayKey(
                q == 1
                    ? "npc.asarr.diamond.ask"
                    : q == 2 ? "npc.asarr.diamond.done" : "npc.asarr.reward.none");

            if (q == 1) c.askYesNo("diamond");

            return true;
          }

          if (k.equals("TEACH") || k.equals("LEARN") || k.equals("TRAIN")) {

            c.sayKey(
                c.flag("__FLAG_BRIGAND_TRUST") > 0
                    ? "npc.asarr.training.ready"
                    : "npc.asarr.training.denied");

            return true;
          }

          if (k.equals("STONE OF LIFE")) {

            if (c.flag("__FLAG_BRIGAND_TRUST") == 0) c.sayKey("npc.asarr.stone.denied");
            else {

              c.sayKey("npc.asarr.stone.ask");

              c.askYesNo("stone");
            }

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if ("job".equals(state)) {

            if (yes) {

              c.flag("__QUEST_BRIGAND_ROB_DIAMOND", 1);

              c.sayKey("npc.asarr.job.given");

            } else c.sayKey("npc.asarr.no");

            return true;
          }

          if ("diamond".equals(state)) {

            if (yes && c.hasItem("diamond")) {

              c.takeItem("diamond");

              c.flag("__QUEST_BRIGAND_ROB_DIAMOND", 2);

              c.flag("__FLAG_BRIGAND_TRUST", 1);

              c.sayKey("npc.asarr.diamond.reward");

            } else if (yes) c.sayKey("npc.asarr.diamond.missing");
            else c.sayKey("npc.asarr.diamond.notdone");

            return true;
          }

          if ("stone".equals(state)) {

            if (!yes) {

              c.sayKey("npc.asarr.no");

              return true;
            }

            int gold = Math.min(1000, c.player().getGold());

            if (gold < 500) {

              c.sayKey("npc.asarr.stone.poor");

              return true;
            }

            c.player().addGold(-gold);

            c.giveItem("stone_of_life");

            c.globalFlag("__TROLL_STONE_OF_LIFE", 5);

            c.sayKey("npc.asarr.stone.given");

            return true;
          }

          return false;
        }
      };

  public Asarr(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
