package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.StorageScreen;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "StorageChest", x = 2945, y = 1070, z = 0, stationary = true, aggressive = false)
@Spawn(type = "StorageChest", x = 1606, y = 1178, z = 0, stationary = true, aggressive = false)
public final class StorageChest extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "StorageChest";

  public static final String DISPLAY_NAME = "${npc.storagechest}";

  public static final String SPRITE_BASE = "@static:Vault";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.storagechest}",
          List.of(),
          "StorageChest",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public StorageChest(NpcContext context) throws GameException {

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

          GuiManager.open(new StorageScreen(c.player()));
        }
      };
}
