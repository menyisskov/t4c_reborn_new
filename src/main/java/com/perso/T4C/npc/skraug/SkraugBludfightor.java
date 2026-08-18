package com.perso.T4C.npc.skraug;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.registry.NpcContext;

public final class SkraugBludfightor extends SkraugOwnerBase {

  public static final String ID = "SkraugBludfightor";

  public SkraugBludfightor(NpcContext c) throws GameException {

    super(ID, c);
  }
}
