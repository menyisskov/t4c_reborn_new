package com.perso.T4C.npc.oracle;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.registry.NpcContext;

public final class OracleGuardian1d extends OracleGuardianBase {

  public static final String ID = "OracleGuardian1d";

  public OracleGuardian1d(NpcContext c) throws GameException {

    super(ID, 4, c);
  }
}
