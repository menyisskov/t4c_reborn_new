package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "CryptChest5", x = 575, y = 1060, z = 1, stationary = true, aggressive = false)
public final class CryptChest5 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "CryptChest5";

  public static final String DISPLAY_NAME = "${npc.cryptchest5}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.cryptchest5}",
          List.of(),
          "OracleChestNPC",
          new NpcSpec.CombatProfile(100, 1000000, 10, 10, 10, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (!c.isInRange(4)) {

          c.systemMessageKey("message.crypt.close");

          return;
        }

        if (!c.hasItem("dew_covered_metal_key")) {

          c.systemMessageKey("message.crypt.locked");

          return;
        }

        int s = c.flag("CRYPT_RANDOM_CHEST");

        if (s == 0) s = 1 + (int) (Math.random() * 5);

        c.takeItem("dew_covered_metal_key");

        c.systemMessageKey("message.crypt.unlock");

        c.systemMessageKey("message.crypt.vanish");

        if (s == 5 && !c.hasItem("crumbling_bone_key")) {

          c.giveItem("crumbling_bone_key");

          c.flag("CRYPT_RANDOM_CHEST", 0);

          c.systemMessageKey("message.crypt.bone_key");

        } else c.systemMessageKey("message.oracle.chest.empty");
      }
    };
  }

  public CryptChest5(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
