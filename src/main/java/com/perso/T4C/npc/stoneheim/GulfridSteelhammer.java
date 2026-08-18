package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "GulfridSteelhammer", x = 220, y = 760, z = 0, stationary = false, aggressive = false)
public final class GulfridSteelhammer extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "GulfridSteelhammer";

  public static final String DISPLAY_NAME = "${npc.gulfridsteelhammer}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.gulfridsteelhammer}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gulfridsteelhammer.0.0}",
                      "${npc.topic_keyword.gulfridsteelhammer.0.1}"),
                  "${npc.topic.gulfridsteelhammer.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gulfridsteelhammer.1.0}"),
                  "${npc.topic.gulfridsteelhammer.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gulfridsteelhammer.2.0}",
                      "${npc.topic_keyword.gulfridsteelhammer.2.1}",
                      "${npc.topic_keyword.gulfridsteelhammer.2.2}"),
                  "${npc.topic.gulfridsteelhammer.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gulfridsteelhammer.3.0}",
                      "${npc.topic_keyword.gulfridsteelhammer.3.1}"),
                  "${npc.topic.gulfridsteelhammer.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gulfridsteelhammer.4.0}"),
                  "${npc.topic.gulfridsteelhammer.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gulfridsteelhammer.5.0}"),
                  "${npc.topic.gulfridsteelhammer.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gulfridsteelhammer.6.0}"),
                  "${npc.topic.gulfridsteelhammer.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.gulfridsteelhammer.7.0}"),
                  "${npc.topic.gulfridsteelhammer.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gulfridsteelhammer.8.0}",
                      "${npc.topic_keyword.gulfridsteelhammer.8.1}"),
                  "${npc.topic.gulfridsteelhammer.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gulfridsteelhammer.9.0}",
                      "${npc.topic_keyword.gulfridsteelhammer.9.1}",
                      "${npc.topic_keyword.gulfridsteelhammer.9.2}",
                      "${npc.topic_keyword.gulfridsteelhammer.9.3}"),
                  "${npc.topic.gulfridsteelhammer.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.gulfridsteelhammer.10.0}",
                      "${npc.topic_keyword.gulfridsteelhammer.10.1}",
                      "${npc.topic_keyword.gulfridsteelhammer.10.2}",
                      "${npc.topic_keyword.gulfridsteelhammer.10.3}",
                      "${npc.topic_keyword.gulfridsteelhammer.10.4}"),
                  "${npc.topic.gulfridsteelhammer.10}",
                  List.of())),
          "GulfridSteelhammerNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private final java.util.List<String> weapons =
          java.util.List.of(
              "high_metal_long_sword",
              "high_metal_broadsword",
              "high_metal_scimitar",
              "high_metal_bastard_sword",
              "high_metal_bastard_sword_dual",
              "high_metal_hand_axe",
              "high_metal_battle_axe",
              "mithril_dagger",
              "mithril_blade",
              "mithril_blade_dual",
              "mithril_flail",
              "oak_flatbow",
              "oak_longbow",
              "oak_reflex_bow",
              "oak_recurve_bow",
              "oak_compound_bow",
              "oak_composite_bow",
              "flight_arrow");

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("BUY") || k.equals("WEAPON") || k.equals("WARES")) {

          c.askYesNo("browse");

          return true;
        }

        if (k.equals("ORACLE")) {

          c.sayKey(
              c.flag("__QUEST_FIXED_ALIGNMENT") >= 1
                  ? "npc.gulfrid.oracle.good"
                  : "npc.gulfrid.oracle.bad");

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

        if (!"browse".equals(state)) return false;

        if (yes) c.openShop(weapons);
        else c.sayKey("npc.gulfrid.no");

        return true;
      }
    };
  }

  public GulfridSteelhammer(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
