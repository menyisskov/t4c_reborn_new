package com.perso.T4C.npc.oracle;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.registry.NpcContext;

public final class OracleGuardian1f extends OracleGuardianBase {

  public static final String ID = "OracleGuardian1f";

  public OracleGuardian1f(NpcContext c) throws GameException {

    super(ID, 6, c);
  }
}
