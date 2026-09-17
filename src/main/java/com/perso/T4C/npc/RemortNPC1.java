package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "RemortNPC1", x = 1285, y = 890, z = 1, stationary = true, aggressive = false)
public final class RemortNPC1 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "RemortNPC1";

  public static final String DISPLAY_NAME = "${npc.remortnpc1}";

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
          "${npc.welcome.remortnpc1}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc1.0.0}", "${npc.topic_keyword.remortnpc1.0.1}"),
                  "${npc.topic.remortnpc1.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc1.1.0}",
                      "${npc.topic_keyword.remortnpc1.1.1}",
                      "${npc.topic_keyword.remortnpc1.1.2}"),
                  "${npc.topic.remortnpc1.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc1.2.0}", "${npc.topic_keyword.remortnpc1.2.1}"),
                  "${npc.topic.remortnpc1.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc1.3.0}", "${npc.topic_keyword.remortnpc1.3.1}"),
                  "${npc.topic.remortnpc1.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc1.4.0}",
                      "${npc.topic_keyword.remortnpc1.4.1}",
                      "${npc.topic_keyword.remortnpc1.4.2}",
                      "${npc.topic_keyword.remortnpc1.4.3}"),
                  "${npc.topic.remortnpc1.4}",
                  List.of())),
          "RemortNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public RemortNPC1(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          int p = c.flag("__FLAG_REMORT_PROCESS");

          if (p == 0) c.sayKey("npc.remortnpc1.welcome", c.flag("__FLAG_REMORT_POINTS"));
          else if (p == 1 && c.flag("__FLAG_REMORT_POINTS") > 0) c.sayKey("npc.remortnpc1.spend");
          else if (p == 1) {

            c.flag("__FLAG_REMORT_PROCESS", 2);

            c.sayKey("npc.remortnpc1.ready");

          } else c.sayKey("npc.remortnpc1.begin");
        }

        @Override
        public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("ASSIST") || k.equals("HELP")) {

            int p = c.flag("__FLAG_REMORT_PROCESS");

            if (p == 0) {

              c.flag("__FLAG_REMORT_PROCESS", 1);

              c.sayKey("npc.remortnpc1.assist");

            } else c.sayKey(p == 1 ? "npc.remortnpc1.associates" : "npc.remortnpc1.begin");

            return true;
          }

          if (k.equals("READY") || k.equals("BEGIN")) {

            if (c.flag("__FLAG_REMORT_PROCESS") == 2) {

              c.sayKey("npc.remortnpc1.teleport");

              c.teleport(
                  com.perso.T4C.config.GameConstants.REBIRTH_RETURN_TILE_X,
                  com.perso.T4C.config.GameConstants.REBIRTH_RETURN_TILE_Y,
                  com.perso.T4C.config.GameConstants.REBIRTH_RETURN_Z);

            } else c.sayKey("npc.remortnpc1.notready");

            return true;
          }

          return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
        }
      };
}
