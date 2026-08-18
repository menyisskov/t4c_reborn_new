package com.perso.T4C.npc.skraug;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.registry.NpcContext;

public final class SkraugMunchhuntor extends SkraugOwnerBase {

  public static final String ID = "SkraugMunchhuntor";

  public SkraugMunchhuntor(NpcContext c) throws GameException {

    super(ID, c);
  }
}
