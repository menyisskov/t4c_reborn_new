package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(
    type = "OracleTrappedChest2a",
    x = 2848,
    y = 2252,
    z = 2,
    stationary = true,
    aggressive = false)
public final class OracleTrappedChest2a extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OracleTrappedChest2a";

  public static final String DISPLAY_NAME = "${npc.oracletrappedchest2a}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.oracletrappedchest2a}",
          List.of(),
          "OracleChestNPC",
          new NpcSpec.CombatProfile(100, 1000000, 10, 10, 10, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (!c.isInRange(4)) {

          c.systemMessageKey("message.oracle.chest.close");

          return;
        }

        if (c.hasItem("scorched_metal_key")) {

          c.systemMessageKey("message.oracle.chest.empty");

          return;
        }

        int s = c.globalFlag("TRAPPED_CHEST_A");

        if (s == 0) s = 1 + (int) (Math.random() * 7);

        if (s == 1) {

          c.giveItem("scorched_metal_key");

          c.systemMessageKey("message.oracle.trapped.key");

          c.globalFlag("TRAPPED_CHEST_A", 1);

        } else {

          c.systemMessageKey("message.oracle.trapped.triggered");

          c.systemMessageKey("message.oracle.trapped.empty");

          c.castTargetSpell("spell.mob_oracle_trapped_chest_fireball_spell");

          c.globalFlag("TRAPPED_CHEST_A", s);
        }
      }
    };
  }

  public OracleTrappedChest2a(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
