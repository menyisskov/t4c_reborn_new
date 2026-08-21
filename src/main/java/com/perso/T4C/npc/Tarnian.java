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

@Spawn(type = "Tarnian", x = 2780, y = 1205, z = 0, stationary = false, aggressive = false)
public final class Tarnian extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Tarnian";

  public static final String DISPLAY_NAME = "${npc.tarnian}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape")),
          0,
          List.of(),
          "${npc.welcome.tarnian}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.0.0}"), "${npc.topic.tarnian.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.1.0}"), "${npc.topic.tarnian.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.2.0}"), "${npc.topic.tarnian.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.3.0}"), "${npc.topic.tarnian.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.4.0}"), "${npc.topic.tarnian.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.5.0}"), "${npc.topic.tarnian.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.6.0}"), "${npc.topic.tarnian.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.7.0}"), "${npc.topic.tarnian.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.8.0}"), "${npc.topic.tarnian.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.9.0}", "${npc.topic_keyword.tarnian.9.1}"),
                  "${npc.topic.tarnian.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.10.0}"),
                  "${npc.topic.tarnian.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tarnian.11.0}"),
                  "${npc.topic.tarnian.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.tarnian.12.0}",
                      "${npc.topic_keyword.tarnian.12.1}",
                      "${npc.topic_keyword.tarnian.12.2}",
                      "${npc.topic_keyword.tarnian.12.3}",
                      "${npc.topic_keyword.tarnian.12.4}"),
                  "${npc.topic.tarnian.12}",
                  List.of())),
          "TarnianNPC",
          new NpcSpec.CombatProfile(60, 1000000, 20, 22, 24, 100000, 730, 250, "1d90+69"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (p < 25) c.sayKey("npc.tarnian.progress.before25");
          else if (p == 25) {

            c.sayKey("npc.tarnian.progress.25");

            c.askYesNo("gunthar");

          } else if (p == 26) c.sayKey("npc.tarnian.progress.26");
          else if (p < 42) c.sayKey("npc.tarnian.progress.before42");
          else c.sayKey("npc.tarnian.progress.complete");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if (!"gunthar".equals(state)) return false;

          if (yes) c.sayKey("npc.tarnian.gunthar.yes");
          else {

            c.sayKey("npc.tarnian.gunthar.no");

            c.endConversation();
          }

          return true;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.equals("BELIEVE") || k.equals("BELIEV")) {

            if (p == 25) {

              c.sayKey("npc.tarnian.believe");

              c.giveItem("runed_stone_tablet");

              c.flag("ADDON_STORYLINE_PROGRESS", 26);

            } else c.sayKey("npc.tarnian.busy");

            return true;
          }

          if (k.equals("DARKSTONE")
              || k.equals("WORTH")
              || k.equals("GLURIURL")
              || k.equals("SKULL")
              || k.equals("ONE")
              || k.equals("SON")
              || k.equals("MURDER")
              || k.equals("FANATIC")
              || k.equals("RAVEN")
              || k.equals("DUST")) {

            c.sayKey(p == 25 ? "npc.tarnian.story" : "npc.tarnian.busy");

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.tarnian.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.tarnian.work");

            return true;
          }

          return false;
        }
      };

  public Tarnian(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
