package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spawn.SpawnKind;

@Spawn(
    type = "ORACLEGUARDIAN1F",
    x = 2812,
    y = 2328,
    z = 2,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "ORACLEGUARDIAN1F",
    x = 2860,
    y = 2280,
    z = 2,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
public final class OracleGuardian1f extends OracleGuardianBase {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OracleGuardian1f";

  public OracleGuardian1f(NpcContext c) throws GameException {

    super(ID, 6, c);
  }
}
