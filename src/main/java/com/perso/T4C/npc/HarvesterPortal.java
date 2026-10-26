package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "HarvesterPortal", x = 1129, y = 1190, z = 0, stationary = true, aggressive = false)
public final class HarvesterPortal extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HarvesterPortal";

  public static final String DISPLAY_NAME = "${npc.harvesterportal}";

  public static final String SPRITE_BASE = "@static:SimplePortal-a";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.harvesterportal}",
          List.of(),
          "PortalNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public void onConversationStart(NpcBehaviorContext c) {
        if (c.player().getLevel() < 50) {
          c.sayKey("npc.harvesterportal.level");
          return;
        }
        if (!c.hasItem("hel_soulstone")
            || !c.hasItem("jormungand_soulstone")
            || !c.hasItem("fenrir_soulstone")) {
          c.sayKey("npc.harvesterportal.missing");
          return;
        }
        float previousX = c.player().getPositionVector().x;
        float previousY = c.player().getPositionVector().y;
        int previousZ = c.player().getCoordinates().getZ();
        c.teleport(2710, 1040, 1);
        if (!c.summon("HARVESTEROFLIFE", 2710, 1040, 1)) {
          c.player().setWorldPosition(previousX, previousY, previousZ);
          c.sayKey("npc.harvesterportal.unavailable");
          return;
        }
        c.takeItem("hel_soulstone");
        c.takeItem("jormungand_soulstone");
        c.takeItem("fenrir_soulstone");
        c.sayKey("npc.welcome.harvesterportal");
      }
    };
  }

  public HarvesterPortal(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
