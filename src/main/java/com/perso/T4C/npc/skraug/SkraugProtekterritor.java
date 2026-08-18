package com.perso.T4C.npc.skraug;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.registry.NpcContext;

public final class SkraugProtekterritor extends SkraugOwnerBase {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SkraugProtekterritor";

  public SkraugProtekterritor(NpcContext c) throws GameException {

    super(ID, c);
  }
}
