package com.perso.T4C.npc.oracle;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.registry.NpcContext;

public final class OracleGuardian1b extends OracleGuardianBase {

  public static final String ID = "OracleGuardian1b";

  public OracleGuardian1b(NpcContext c) throws GameException {

    super(ID, 2, c);
  }
}
