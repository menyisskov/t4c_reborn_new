package com.perso.T4C.npc.oracle;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.registry.NpcContext;

public final class OracleGuardian1e extends OracleGuardianBase {

  public static final String ID = "OracleGuardian1e";

  public OracleGuardian1e(NpcContext c) throws GameException {

    super(ID, 5, c);
  }
}
