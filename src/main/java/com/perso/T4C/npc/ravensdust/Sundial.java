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
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spawn.SpawnKind;
import java.util.List;

@Spawn(type = "Sundial", x = 0, y = 0, z = 0, stationary = true, aggressive = false)
@Spawn(
    type = "SUNDIAL",
    x = 1014,
    y = 668,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 1022,
    y = 662,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 1024,
    y = 672,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 1455,
    y = 2500,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 1470,
    y = 2426,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 1521,
    y = 2412,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 1564,
    y = 2554,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 1667,
    y = 1242,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 1723,
    y = 1245,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 273,
    y = 744,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 277,
    y = 827,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 2881,
    y = 1070,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 295,
    y = 808,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 677,
    y = 547,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 684,
    y = 543,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 703,
    y = 576,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 708,
    y = 580,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 735,
    y = 628,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 737,
    y = 549,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 738,
    y = 551,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 739,
    y = 547,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 739,
    y = 616,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 748,
    y = 516,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 751,
    y = 693,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 752,
    y = 691,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 764,
    y = 510,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 764,
    y = 730,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 764,
    y = 732,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 765,
    y = 611,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 766,
    y = 505,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 766,
    y = 729,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 782,
    y = 564,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 804,
    y = 532,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 804,
    y = 758,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 806,
    y = 760,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 808,
    y = 617,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 815,
    y = 673,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 816,
    y = 778,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 818,
    y = 781,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 819,
    y = 608,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 821,
    y = 615,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 824,
    y = 699,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 836,
    y = 698,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 841,
    y = 588,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 843,
    y = 691,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 845,
    y = 592,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 845,
    y = 661,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 877,
    y = 548,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 879,
    y = 735,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 883,
    y = 587,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 886,
    y = 560,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 887,
    y = 723,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 887,
    y = 745,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 890,
    y = 566,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 896,
    y = 652,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 896,
    y = 670,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 898,
    y = 559,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 900,
    y = 533,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 902,
    y = 543,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 918,
    y = 771,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 920,
    y = 644,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 955,
    y = 736,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 959,
    y = 700,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 963,
    y = 724,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 966,
    y = 1031,
    z = 0,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 970,
    y = 626,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
@Spawn(
    type = "SUNDIAL",
    x = 972,
    y = 691,
    z = 1,
    stationary = false,
    aggressive = true,
    kind = SpawnKind.MONSTER)
public final class Sundial extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

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
