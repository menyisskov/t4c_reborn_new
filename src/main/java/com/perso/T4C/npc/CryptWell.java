package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "CryptWell", x = 705, y = 1005, z = 1, stationary = true, aggressive = false)
public final class CryptWell extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "CryptWell";

  public static final String DISPLAY_NAME = "${npc.cryptwell}";

  public static final String SPRITE_BASE = "@static:DungeonWell";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.cryptwell}",
          List.of(),
          "CryptWellNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public CryptWell(NpcContext context) throws GameException {

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

          respawn(c, "GLOBAL_FLAG_CRYPT_CHEST_1_DESTROYED", "CRYPTCHEST1", 560, 1075);

          respawn(c, "GLOBAL_FLAG_CRYPT_CHEST_2_DESTROYED", "CRYPTCHEST2", 558, 1068);

          respawn(c, "GLOBAL_FLAG_CRYPT_CHEST_3_DESTROYED", "CRYPTCHEST3", 561, 1061);

          respawn(c, "GLOBAL_FLAG_CRYPT_CHEST_4_DESTROYED", "CRYPTCHEST4", 568, 1058);

          respawn(c, "GLOBAL_FLAG_CRYPT_CHEST_5_DESTROYED", "CRYPTCHEST5", 575, 1060);

          if (!c.hasItem("dew_covered_metal_key")) {

            c.giveItem("dew_covered_metal_key");

            c.sayKey("npc.cryptwell.key");

          } else {

            c.sayKey("npc.cryptwell.fall");
          }

          c.teleport(569, 1069, 1);
        }

        private void respawn(
            com.perso.T4C.npc.behavior.NpcBehaviorContext c,
            String flag,
            String monster,
            int x,
            int y) {

          if (c.globalFlag(flag) == 1) {

            c.globalFlag(flag, 0);

            c.summon(monster, x, y, 1);
          }
        }
      };
}
