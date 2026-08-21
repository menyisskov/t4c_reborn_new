package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import java.util.List;

public final class MakrshPtanghSpawner extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MakrshPtanghSpawner";

  public MakrshPtanghSpawner(NpcContext c) throws GameException {

    super(spec(), c);
  }

  public static NpcSpec spec() {

    return new NpcSpec(
        ID,
        "${npc.makrshptanghspawner}",
        "@invisible",
        List.of(),
        0,
        List.of(),
        null,
        List.of(),
        "MakrshPtanghSpawnerNPC",
        new NpcSpec.CombatProfile(1, 1, 1, 1, 1, 1, 0, 1, "1d1"));
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onPopup(NpcBehaviorContext c) {
        int fighting =
            Math.max(
                c.globalFlag("__GLOBAL_FLAG_MAKRSH_PTANGH_IS_FIGHTING"),
                Math.max(
                    c.globalFlag("GLOBAL_FLAG_MAKRSH_PTANGH_IS_FIGHTING"),
                    c.globalFlag("MAKRSH_PTANGH_IS_FIGHTING")));
        if (fighting != 1) return;
        int deadTimer =
            Math.max(
                c.globalFlag("__GLOBAL_FLAG_MAKRSH_PTANGH_DEAD_TIMER"),
                Math.max(
                    c.globalFlag("GLOBAL_FLAG_MAKRSH_PTANGH_DEAD_TIMER"),
                    c.globalFlag("MAKRSH_PTANGH_DEAD_TIMER")));
        int now = (int) (System.currentTimeMillis() / 50L);
        if (now < deadTimer || now >= deadTimer + 24000) {
          c.globalFlag("__GLOBAL_FLAG_MAKRSH_PTANGH_IS_FIGHTING", 2);
          c.summon("MAKRSHPTANGH", 2265, 295, 1);
        }
      }
    };
  }
}
