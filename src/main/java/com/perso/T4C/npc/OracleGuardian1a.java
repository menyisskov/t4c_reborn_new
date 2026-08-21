package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spawn.SpawnKind;

@Spawn(
    type = "ORACLEGUARDIAN1A",
    x = 2824,
    y = 2340,
    z = 2,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "ORACLEGUARDIAN1A",
    x = 2872,
    y = 2292,
    z = 2,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
public final class OracleGuardian1a extends OracleGuardianBase {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OracleGuardian1a";

  public OracleGuardian1a(NpcContext c) throws GameException {

    super(ID, 1, c);
  }
}
