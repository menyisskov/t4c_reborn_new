package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spawn.SpawnKind;

@Spawn(
    type = "ORACLEGUARDIAN1C",
    x = 2812,
    y = 2352,
    z = 2,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "ORACLEGUARDIAN1C",
    x = 2884,
    y = 2280,
    z = 2,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
public final class OracleGuardian1c extends OracleGuardianBase {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OracleGuardian1c";

  public OracleGuardian1c(NpcContext c) throws GameException {

    super(ID, 3, c);
  }
}
