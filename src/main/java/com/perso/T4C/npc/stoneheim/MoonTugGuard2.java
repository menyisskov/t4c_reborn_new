package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class MoonTugGuard2 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MoonTugGuard2";

  public static final String DISPLAY_NAME = "${npc.moontugguard2}";

  public static final String SPRITE_BASE = "64kSkavenWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.moontugguard2}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.moontugguard2.0.0}",
                      "${npc.topic_keyword.moontugguard2.0.1}",
                      "${npc.topic_keyword.moontugguard2.0.2}",
                      "${npc.topic_keyword.moontugguard2.0.3}"),
                  "${npc.topic.moontugguard2.0}",
                  List.of())),
          "MoonTugSkraugBattlefieldGuardNPC",
          new NpcSpec.CombatProfile(100, 32590, 115, 104, 104, 50, 1210, 410, "1d172+134"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public MoonTugGuard2(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
