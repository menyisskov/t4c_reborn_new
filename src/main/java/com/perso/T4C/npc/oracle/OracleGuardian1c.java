package com.perso.T4C.npc.oracle;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.registry.NpcContext;

public final class OracleGuardian1c extends OracleGuardianBase {

  public static final String ID = "OracleGuardian1c";

  public OracleGuardian1c(NpcContext c) throws GameException {

    super(ID, 3, c);
  }
}
