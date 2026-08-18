package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class OracleEscapeFlippedDoor extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OracleEscapeFlippedDoor";

  public static final String DISPLAY_NAME = "${npc.oracleescapeflippeddoor}";

  public static final String SPRITE_BASE = "@static:RockDoor1";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.oracleescapeflippeddoor}",
          List.of(),
          "FlippedDoorNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public OracleEscapeFlippedDoor(NpcContext context) throws GameException {

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
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          long packed = Integer.toUnsignedLong(c.flag("__FLAG_DEATH_LOCATION"));

          int x = (int) ((packed >>> 20) & 0xFFF),
              y = (int) ((packed >>> 8) & 0xFFF),
              world = (int) (packed & 0xFF);

          if (packed == 0) {

            x = 2941;

            y = 1062;

            world = 0;
          }

          c.teleport(x, y, world);

          c.systemMessageKey("npc.oracleescapeflippeddoor.transported");

          c.endConversation();
        }
      };
}
