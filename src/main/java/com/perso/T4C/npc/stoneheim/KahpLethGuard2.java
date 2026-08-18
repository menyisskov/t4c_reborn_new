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

public final class KahpLethGuard2 extends ScriptedNpc {

  public static final String ID = "KahpLethGuard2";

  public static final String DISPLAY_NAME = "${npc.kahplethguard2}";

  public static final String SPRITE_BASE = "64kSkavenWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.kahplethguard2}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kahplethguard2.0.0}",
                      "${npc.topic_keyword.kahplethguard2.0.1}",
                      "${npc.topic_keyword.kahplethguard2.0.2}",
                      "${npc.topic_keyword.kahplethguard2.0.3}"),
                  "${npc.topic.kahplethguard2.0}",
                  List.of())),
          "KahpLethSkraugBattlefieldGuardNPC",
          new NpcSpec.CombatProfile(100, 32590, 115, 104, 104, 50, 1210, 410, "1d 172 + 134"));

  public KahpLethGuard2(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }
}
