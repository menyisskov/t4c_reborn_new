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
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Aquinos", x = 1557, y = 2405, z = 0, stationary = false, aggressive = false)
public final class Aquinos extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Aquinos";

  public static final String DISPLAY_NAME = "${npc.aquinos}";

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
          "${npc.welcome.aquinos}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aquinos.0.0}"), "${npc.topic.aquinos.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aquinos.1.0}"), "${npc.topic.aquinos.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aquinos.2.0}"), "${npc.topic.aquinos.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aquinos.3.0}"), "${npc.topic.aquinos.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.aquinos.4.0}"), "${npc.topic.aquinos.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aquinos.5.0}",
                      "${npc.topic_keyword.aquinos.5.1}",
                      "${npc.topic_keyword.aquinos.5.2}",
                      "${npc.topic_keyword.aquinos.5.3}",
                      "${npc.topic_keyword.aquinos.5.4}"),
                  "${npc.topic.aquinos.5}",
                  List.of())),
          "HighPriestGuntharNPC",
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

          if (p < 28) c.sayKey("npc.aquinos.progress.before28");
          else if (p == 28) c.sayKey("npc.aquinos.progress.28");
          else if (p == 29) c.sayKey("npc.aquinos.progress.29");
          else if (p < 42) c.sayKey("npc.aquinos.progress.before42");
          else c.sayKey("npc.aquinos.progress.complete");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.equals("VISIT") && p == 28) {

            c.sayKey("npc.aquinos.visit");

            return true;
          }

          if (k.equals("LOCATION") && p == 28) {

            c.sayKey("npc.aquinos.location");

            return true;
          }

          if (k.equals("WHERE") && p == 28) {

            c.sayKey("npc.aquinos.where");

            c.flag("ADDON_STORYLINE_PROGRESS", 29);

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.aquinos.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.aquinos.work");

            return true;
          }

          return false;
        }
      };

  public Aquinos(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
