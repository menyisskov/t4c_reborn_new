package com.perso.T4C.npc.oracle;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.registry.NpcContext;

public final class OracleGuardian1a extends OracleGuardianBase {

  public static final String ID = "OracleGuardian1a";

  public OracleGuardian1a(NpcContext c) throws GameException {

    super(ID, 1, c);
  }
}
