package com.perso.T4C.npc.stoneheim;

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

@Spawn(type = "CryptCoffin1", x = 675, y = 1175, z = 1, stationary = true, aggressive = false)
public final class CryptCoffin1 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "CryptCoffin1";

  public static final String DISPLAY_NAME = "${npc.cryptcoffin1}";

  public static final String SPRITE_BASE = "@static:DungeonTomb1";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.cryptcoffin1}",
          List.of(),
          "CoffinNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onInitialise(NpcBehaviorContext c) {

        c.npc().setStationary(true);
      }

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (!c.isInRange(4)) {

          c.systemMessageKey("message.crypt.coffin_close");

          return;
        }

        if (!c.hasItem("crumbling_bone_key")) {

          c.systemMessageKey("message.crypt.coffin_locked");

          return;
        }

        int now = (int) (System.currentTimeMillis() / 1000L),
            next = c.npcFlag("LORD_STONECREST_REPOP_DELAY");

        while (c.hasItem("crumbling_bone_key")) c.takeItem("crumbling_bone_key");

        c.systemMessageKey("message.crypt.coffin_place");

        if (next == 0 || now >= next) {

          c.npcFlag("LORD_STONECREST_REPOP_DELAY", now + 36000);

          c.systemMessageKey("message.crypt.coffin_open");

          c.summon("LORDSTONECREST", 672, 1172, 1);

        } else {

          c.systemMessageKey("message.crypt.coffin_opening");

          c.systemMessageKey("message.crypt.coffin_empty");
        }
      }
    };
  }

  public CryptCoffin1(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
