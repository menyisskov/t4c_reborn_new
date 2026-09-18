package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "TideWardenBryn", x = 1520, y = 2440, z = 0, stationary = false, aggressive = false)
public final class TideWardenBryn extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "TideWardenBryn";

  public static final String DISPLAY_NAME = "${npc.tidewardenbryn}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.tidewardenbryn}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.tidewardenbryn.0.0}",
                      "${npc.topic_keyword.tidewardenbryn.0.1}"),
                  "${npc.topic.tidewardenbryn.0}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.GIVE_QUEST, List.of("silversky_tide_warden")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.tidewardenbryn.1.0}",
                      "${npc.topic_keyword.tidewardenbryn.1.1}"),
                  "${npc.topic.tidewardenbryn.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.tidewardenbryn.2.0}",
                      "${npc.topic_keyword.tidewardenbryn.2.1}",
                      "${npc.topic_keyword.tidewardenbryn.2.2}"),
                  "${npc.topic.tidewardenbryn.2}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING,
                          List.of("riptide_surge", "drowned_ward")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.tidewardenbryn.3.0}",
                      "${npc.topic_keyword.tidewardenbryn.3.1}"),
                  "${npc.topic.tidewardenbryn.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tidewardenbryn.4.0}"),
                  "${npc.topic.tidewardenbryn.4}",
                  List.of())),
          "TideWardenBrynNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public TideWardenBryn(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
