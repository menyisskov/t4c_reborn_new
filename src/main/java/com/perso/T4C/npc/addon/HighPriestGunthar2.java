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

@Spawn(type = "HighPriestGunthar2", x = 0, y = 0, z = 0, stationary = true, aggressive = false)
public final class HighPriestGunthar2 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HighPriestGunthar2";

  public static final String DISPLAY_NAME = "${npc.highpriestgunthar2}";

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
          "${npc.welcome.highpriestgunthar2}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.highpriestgunthar2.0.0}",
                      "${npc.topic_keyword.highpriestgunthar2.0.1}",
                      "${npc.topic_keyword.highpriestgunthar2.0.2}",
                      "${npc.topic_keyword.highpriestgunthar2.0.3}",
                      "${npc.topic_keyword.highpriestgunthar2.0.4}"),
                  "${npc.topic.highpriestgunthar2.0}",
                  List.of())),
          "HighPriestGunthar2NPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 0, 0, 50000, "1d3"));

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
        public void onPopup(NpcBehaviorContext c) {

          c.castSelfSpell(10752);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (p == 22) {

            c.sayKey("npc.gunthar2.progress.22");

            c.askYesNo("stone");

          } else if (p == 23) c.sayKey("npc.gunthar2.progress.23");
          else c.sayKey("npc.gunthar2.progress.other");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String s, boolean yes) {

          if (!"stone".equals(s)) return false;

          if (yes) {

            c.sayKey("npc.gunthar2.stone.yes");

            c.giveXp(200000);

            c.flag("ADDON_STORYLINE_PROGRESS", 23);

          } else c.sayKey("npc.gunthar2.stone.no");

          return true;
        }
      };

  public HighPriestGunthar2(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
