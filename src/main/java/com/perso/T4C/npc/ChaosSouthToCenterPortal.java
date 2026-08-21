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
    type = "ChaosSouthToCenterPortal",
    x = 1570,
    y = 1697,
    z = 1,
    stationary = true,
    aggressive = false)
public final class ChaosSouthToCenterPortal extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ChaosSouthToCenterPortal";

  public static final String DISPLAY_NAME = "${npc.chaossouthtocenterportal}";

  public static final String SPRITE_BASE = "@static:SimplePortal-a";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.chaossouthtocenterportal}",
          List.of(),
          "PortalNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public ChaosSouthToCenterPortal(NpcContext context) throws GameException {

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
      new GateBehavior(1604, 1664, 1, "npc.welcome.chaossouthtocenterportal");

  private static final class GateBehavior implements NpcBehavior {
    private final int tileX;
    private final int tileY;
    private final int world;
    private final String blockedMessage;

    private GateBehavior(int tileX, int tileY, int world, String blockedMessage) {
      this.tileX = tileX;
      this.tileY = tileY;
      this.world = world;
      this.blockedMessage = blockedMessage;
    }

    @Override
    public void onInitialise(NpcBehaviorContext context) {
      context.npc().setStationary(true);
    }

    @Override
    public void onConversationStart(NpcBehaviorContext context) {
      if (!context.isInRange(4)) context.sayKey("npc.portal.too_far");
      else if (context.flag("ADDON_TERROR_DEMON_KILLED") == 1
          && context.flag("ADDON_CHAOS_DEMON_KILLED") == 1
          && context.flag("ADDON_DARKNESS_DEMON_KILLED") == 1) {
        context.teleport(tileX, tileY, world);
      } else context.sayKey(blockedMessage);
      context.endConversation();
    }
  }
}
