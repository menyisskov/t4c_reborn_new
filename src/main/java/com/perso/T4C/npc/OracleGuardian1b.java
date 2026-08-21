package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;

public final class OracleGuardian1b extends OracleGuardianBase {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OracleGuardian1b";

  public OracleGuardian1b(NpcContext c) throws GameException {

    super(ID, 2, c);
  }
}
