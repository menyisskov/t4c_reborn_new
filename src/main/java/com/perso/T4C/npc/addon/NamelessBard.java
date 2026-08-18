package com.perso.T4C.npc.addon;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "NamelessBard", x = 1656, y = 2574, z = 1, stationary = false, aggressive = false)
public final class NamelessBard extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String ID = "NamelessBard";

  public static final String DISPLAY_NAME = "${npc.namelessbard}";

  public static final String SPRITE_BASE = "Atrocity";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.namelessbard}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.namelessbard.0.0}"),
                  "${npc.topic.namelessbard.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.namelessbard.1.0}"),
                  "${npc.topic.namelessbard.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.namelessbard.2.0}"),
                  "${npc.topic.namelessbard.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.namelessbard.3.0}"),
                  "${npc.topic.namelessbard.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.namelessbard.4.0}",
                      "${npc.topic_keyword.namelessbard.4.1}",
                      "${npc.topic_keyword.namelessbard.4.2}",
                      "${npc.topic_keyword.namelessbard.4.3}",
                      "${npc.topic_keyword.namelessbard.4.4}"),
                  "${npc.topic.namelessbard.4}",
                  List.of())),
          "NamelessBardNPC",
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

          if (p < 29) c.sayKey("npc.namelessbard.progress.before29");
          else if (p == 29) c.sayKey("npc.namelessbard.progress.29");
          else if (p < 32) c.sayKey("npc.namelessbard.progress.before32");
          else if (p == 32 || p == 33) {

            c.sayKey("npc.namelessbard.progress.32");

            c.flag("ADDON_STORYLINE_PROGRESS", 33);

          } else c.sayKey("npc.namelessbard.progress.complete");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.startsWith("PUNIS")) {

            c.sayKey(p == 29 ? "npc.namelessbard.punish" : "npc.namelessbard.silence");

            return true;
          }

          if (k.equals("ABYSS")) {

            c.sayKey(p == 29 ? "npc.namelessbard.abyss" : "npc.namelessbard.silence");

            return true;
          }

          if (k.startsWith("REMNANT")) {

            if (p == 29) {

              c.sayKey("npc.namelessbard.remnant");

              c.flag("ADDON_STORYLINE_PROGRESS", 30);

            } else c.sayKey("npc.namelessbard.silence");

            return true;
          }

          if (k.startsWith("CURS")) {

            c.sayKey(p == 33 ? "npc.namelessbard.curse" : "npc.namelessbard.silence");

            return true;
          }

          return false;
        }
      };

  public NamelessBard(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
