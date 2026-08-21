package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Crimsonscale", x = 842, y = 2220, z = 1, stationary = true, aggressive = false)
public final class Crimsonscale extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public static final String ID = "Crimsonscale";

  public static final String DISPLAY_NAME = "${npc.crimsonscale}";

  public static final String SPRITE_BASE = "DragonSTMOV";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.crimsonscale}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.0.0}"),
                  "${npc.topic.crimsonscale.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.1.0}"),
                  "${npc.topic.crimsonscale.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.2.0}"),
                  "${npc.topic.crimsonscale.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.crimsonscale.3.0}",
                      "${npc.topic_keyword.crimsonscale.3.1}"),
                  "${npc.topic.crimsonscale.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.4.0}"),
                  "${npc.topic.crimsonscale.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.5.0}"),
                  "${npc.topic.crimsonscale.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.crimsonscale.6.0}",
                      "${npc.topic_keyword.crimsonscale.6.1}"),
                  "${npc.topic.crimsonscale.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.crimsonscale.7.0}",
                      "${npc.topic_keyword.crimsonscale.7.1}"),
                  "${npc.topic.crimsonscale.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.8.0}"),
                  "${npc.topic.crimsonscale.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.crimsonscale.9.0}",
                      "${npc.topic_keyword.crimsonscale.9.1}"),
                  "${npc.topic.crimsonscale.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.crimsonscale.10.0}",
                      "${npc.topic_keyword.crimsonscale.10.1}"),
                  "${npc.topic.crimsonscale.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.11.0}"),
                  "${npc.topic.crimsonscale.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.12.0}"),
                  "${npc.topic.crimsonscale.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.crimsonscale.13.0}",
                      "${npc.topic_keyword.crimsonscale.13.1}"),
                  "${npc.topic.crimsonscale.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.14.0}"),
                  "${npc.topic.crimsonscale.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.15.0}"),
                  "${npc.topic.crimsonscale.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.crimsonscale.16.0}"),
                  "${npc.topic.crimsonscale.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.crimsonscale.17.0}",
                      "${npc.topic_keyword.crimsonscale.17.1}",
                      "${npc.topic_keyword.crimsonscale.17.2}",
                      "${npc.topic_keyword.crimsonscale.17.3}",
                      "${npc.topic_keyword.crimsonscale.17.4}"),
                  "${npc.topic.crimsonscale.17}",
                  List.of())),
          "CrimsonscaleNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (p < 33) c.sayKey("npc.crimson.progress.before33");
          else if (p == 33) c.sayKey("npc.crimson.progress.33");
          else if (p == 34) c.sayKey("npc.crimson.progress.34");
          else if (p < 42) c.sayKey("npc.crimson.progress.before42");
          else c.sayKey("npc.crimson.progress.done");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.contains("SOMETHING") && p == 33) {

            c.giveItem("vial_of_crimsonscale_blood");

            c.giveItem("armlet_of_flames");

            c.giveItem("gem_of_flames");

            c.flag("ADDON_STORYLINE_PROGRESS", 34);

            c.sayKey("npc.crimson.gift");

            return true;
          }

          if (k.equals("LETTER")
              || k.equals("BEGIN")
              || k.equals("CHOSEN")
              || k.equals("JUSTICE")
              || k.equals("WIND")
              || k.equals("DRAGON")) {

            c.sayKey(p == 33 ? "npc.crimson.lore" : "npc.crimson.busy");

            return true;
          }

          return false;
        }

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }
      };

  public Crimsonscale(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
