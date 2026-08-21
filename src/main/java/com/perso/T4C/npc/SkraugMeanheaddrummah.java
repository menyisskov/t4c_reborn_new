package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;

public final class SkraugMeanheaddrummah extends SkraugOwnerBase {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SkraugMeanheaddrummah";

  public SkraugMeanheaddrummah(NpcContext c) throws GameException {

    super(ID, c);
  }
}
