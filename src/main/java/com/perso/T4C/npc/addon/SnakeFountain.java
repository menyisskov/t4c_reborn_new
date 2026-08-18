package com.perso.T4C.npc.addon;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "SnakeFountain", x = 826, y = 1907, z = 1, stationary = true, aggressive = false)
public final class SnakeFountain extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SnakeFountain";

  public static final String DISPLAY_NAME = "${npc.snakefountain}";

  public static final String SPRITE_BASE = "@static:DungeonWell";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.snakefountain}",
          List.of(),
          "WellNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.isInRange(4)) {

          c.systemMessageKey("npc.snakefountain.inscription");

          if (!c.hasItem("flask_of_gluriurl_blood") && !c.hasItem("heart_of_gluriurl")) {

            c.systemMessageKey("npc.snakefountain.gather");

            c.giveItem("flask_of_gluriurl_blood");

          } else c.systemMessageKey("npc.snakefountain.snakes");

        } else if (c.isInRange(8)) c.systemMessageKey("npc.snakefountain.far");
        else c.systemMessageKey("npc.snakefountain.too_far");
      }
    };
  }

  public SnakeFountain(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
