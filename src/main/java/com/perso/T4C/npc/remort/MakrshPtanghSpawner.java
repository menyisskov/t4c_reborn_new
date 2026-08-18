package com.perso.T4C.npc.remort;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.script.ScriptedNpc;
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
        "MakrshPtanghNPC",
        new NpcSpec.CombatProfile(1, 1, 1, 1, 1, 1, 0, 1, "1d1"));
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      public void onConversationStart(NpcBehaviorContext c) {

        if (c.globalFlag("MAKRSH_PTANGH_IS_FIGHTING") == 1) {

          int t = c.globalFlag("MAKRSH_PTANGH_DEAD_TIMER");

          int n = (int) (System.currentTimeMillis() / 1000L);

          if (n < t || n >= t + 24000) {

            c.globalFlag("MAKRSH_PTANGH_IS_FIGHTING", 2);

            c.summon("MAKRSHPTANGH", 2265, 295, 0);
          }
        }
      }
    };
  }
}
