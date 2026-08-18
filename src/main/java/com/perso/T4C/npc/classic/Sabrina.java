package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class Sabrina extends ScriptedNpc {

  public static final String ID = "Sabrina";

  public static final String DISPLAY_NAME = "${npc.sabrina}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.sabrina.welcome}",
          List.of(
              new NpcSpec.DialogueTopic(List.of("HEAL"), "${npc.sabrina.heal}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("NAME", "WHO ARE YOU"), "${npc.sabrina.name}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("WORK", "OCCUPATION", "WHAT DO YOU"), "${npc.sabrina.work}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("BYE", "LEAVE", "QUIT"), "${npc.sabrina.bye}", List.of())),
          "SabrinaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onInitialise(NpcBehaviorContext c) {

        c.castSelfSpell(10269);
      }

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        int rats = c.flag("RATS_KILLED");

        c.sayKey(
            rats == 25
                ? "npc.sabrina.rats.done"
                : rats >= 20 ? "npc.sabrina.rats.most" : "npc.sabrina.rats.help");
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT).trim();

        if ("HEAL".equals(k)) {

          c.sayKey("npc.sabrina.heal");

          c.castTargetSpell(10270);

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public void onAttacked(NpcBehaviorContext c) {

        if ((int) (Math.random() * 11) == 1)
          c.shoutKey("npc.sabrina.attacked." + (int) (Math.random() * 3));

        c.fleeFromPlayer();
      }
    };
  }

  public Sabrina(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
