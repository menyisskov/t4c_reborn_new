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

@Spawn(type = "BrotherKiran", x = 2955, y = 1048, z = 0, stationary = false, aggressive = false)
public final class BrotherKiran extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "BrotherKiran";

  public static final String DISPLAY_NAME = "${npc.brotherkiran}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.brotherkiran}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherkiran.0.0}"),
                  "${npc.topic.brotherkiran.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherkiran.1.0}",
                      "${npc.topic_keyword.brotherkiran.1.1}"),
                  "${npc.topic.brotherkiran.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherkiran.2.0}",
                      "${npc.topic_keyword.brotherkiran.2.1}"),
                  "${npc.topic.brotherkiran.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherkiran.3.0}",
                      "${npc.topic_keyword.brotherkiran.3.1}"),
                  "${npc.topic.brotherkiran.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherkiran.4.0}",
                      "${npc.topic_keyword.brotherkiran.4.1}",
                      "${npc.topic_keyword.brotherkiran.4.2}"),
                  "${npc.topic.brotherkiran.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherkiran.5.0}",
                      "${npc.topic_keyword.brotherkiran.5.1}",
                      "${npc.topic_keyword.brotherkiran.5.2}"),
                  "${npc.topic.brotherkiran.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherkiran.6.0}"),
                  "${npc.topic.brotherkiran.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherkiran.7.0}"),
                  "${npc.topic.brotherkiran.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherkiran.8.0}"),
                  "${npc.topic.brotherkiran.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherkiran.9.0}"),
                  "${npc.topic.brotherkiran.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherkiran.10.0}"),
                  "${npc.topic.brotherkiran.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherkiran.11.0}",
                      "${npc.topic_keyword.brotherkiran.11.1}",
                      "${npc.topic_keyword.brotherkiran.11.2}",
                      "${npc.topic_keyword.brotherkiran.11.3}",
                      "${npc.topic_keyword.brotherkiran.11.4}"),
                  "${npc.topic.brotherkiran.11}",
                  List.of())),
          "Priest",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

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

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          c.sayKey(
              c.flag("ADDON_CRIMSONSCALE_LETTER") == 0
                  ? "npc.kiran.noletter"
                  : p == 19
                      ? "npc.kiran.see.highpriest"
                      : p == 20
                          ? "npc.kiran.note"
                          : p == 21
                              ? "npc.kiran.townhall"
                              : p < 42 ? "npc.kiran.temple" : "npc.kiran.complete");

          if (p == 19 && c.flag("ADDON_CRIMSONSCALE_LETTER") != 0) c.askYesNo("highpriest");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("IMPORTANT") || k.equals("THING")) {

            int p = c.flag("ADDON_STORYLINE_PROGRESS");

            c.sayKey(p == 19 || p == 20 ? "npc.kiran.important" : "npc.kiran.notnow");

            if (p == 19 || p == 20) c.flag("ADDON_STORYLINE_PROGRESS", 21);

            return true;
          }

          if (k.equals("HEAL")) {

            if (c.player().getCurrentHp() >= c.player().getMaxHp()) {

              c.sayKey("npc.kiran.noheal");

              return true;
            }

            c.sayKey("npc.kiran.heal.ask");

            c.askYesNo("heal");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if ("highpriest".equals(state)) {

            c.sayKey(yes ? "npc.kiran.highpriest.yes" : "npc.kiran.highpriest.no");

            return true;
          }

          if (!"heal".equals(state)) return false;

          if (!yes) {

            c.sayKey("npc.kiran.heal.no");

            return true;
          }

          int missing = c.player().getMaxHp() - c.player().getCurrentHp(),
              cost = Math.max(1, missing / 2);

          if (c.player().getGold() < cost) {

            c.sayKey("npc.kiran.heal.poor");

            return true;
          }

          c.player().addGold(-cost);

          c.player().applyHeal(c.player().getMaxHp(), c.player().getMaxHp());

          c.sayKey("npc.kiran.heal.done");

          return true;
        }
      };

  public BrotherKiran(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
