package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StationaryBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class Sundial extends ScriptedNpc {

  public static final String ID = "Sundial";

  public static final String DISPLAY_NAME = "${npc.sundial}";

  public static final String SPRITE_BASE = "@static:Horloge Solaire";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.sundial}",
          List.of(),
          "SundialNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        StationaryBehavior.INSTANCE.onInitialise(c);
      }

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        java.time.LocalTime now = java.time.LocalTime.now();

        int hour = now.getHour();

        if (hour <= 3) {

          c.systemMessageKey("message.sundial.not_up");

          return;
        }

        if (hour >= 20) {

          c.systemMessageKey("message.sundial.not_visible");

          return;
        }

        String[] messages = {
          "message.sundial.4_past_highmoon",
          "message.sundial.5_past_highmoon",
          "message.sundial.6_past_highmoon",
          "message.sundial.5_before_highsun",
          "message.sundial.4_before_highsun",
          "message.sundial.3_before_highsun",
          "message.sundial.2_before_highsun",
          "message.sundial.almost_highsun",
          "message.sundial.highsun",
          "message.sundial.bit_past_highsun",
          "message.sundial.2_past_highsun",
          "message.sundial.3_past_highsun",
          "message.sundial.4_past_highsun",
          "message.sundial.5_past_highsun",
          "message.sundial.6_past_highsun",
          "message.sundial.5_before_highmoon",
          "message.sundial.4_before_highmoon"
        };

        int index = hour - 4;

        if (now.getMinute() > 30) index++;

        c.systemMessageKey(messages[Math.max(0, Math.min(index, messages.length - 1))]);
      }
    };
  }

  public Sundial(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
