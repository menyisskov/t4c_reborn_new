package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "CarmanSpawner", x = 0, y = 0, z = 0, stationary = true, aggressive = false)
public final class CarmanSpawner extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "CarmanSpawner";

  public static final String DISPLAY_NAME = "${npc.carmanspawner}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupBarossaShield")),
          0,
          List.of(),
          "${npc.welcome.carmanspawner}",
          List.of(),
          "CarmanSpawnerNPC",
          new NpcSpec.CombatProfile(100, 1000000, 115, 104, 104, 1000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        if (c.flag("__FLAG_ADDON_USER_SPAWNED_CARMAN") == 1) {

          c.sayKey("npc.carmanspawner.ruse");

          return true;
        }

        c.sayKey("npc.carmanspawner.busy");

        return true;
      }

      @Override
      public void onDestroy(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.castSelfSpell(10765);

        c.summon("Carman", 1663, 1040, 0);
      }
    };
  }

  public CarmanSpawner(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
