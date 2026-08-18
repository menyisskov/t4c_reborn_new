package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class VargusChest extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "VargusChest";

  public static final String DISPLAY_NAME = "${npc.varguschest}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.varguschest}",
          List.of(),
          "VargusChestNPC",
          new NpcSpec.CombatProfile(100, 1000000, 10, 10, 10, 100000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_USER_HAS_SKULL_OF_OGRIMAR") == 0
            && c.flag("__FLAG_USER_KNOWS_WHERE_SKULL_OF_OGRIMAR_IS_HIDDEN") == 1) {

          c.flag("__FLAG_USER_HAS_SKULL_OF_OGRIMAR", 1);

          c.flag("__FLAG_USER_KNOWS_WHERE_SKULL_OF_OGRIMAR_IS_HIDDEN", 0);

          c.giveItem("skull_of_ogrimar");

          c.systemMessageKey("npc.varguschest.found");

        } else c.systemMessageKey("npc.varguschest.empty");
      }
    };
  }

  public VargusChest(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
