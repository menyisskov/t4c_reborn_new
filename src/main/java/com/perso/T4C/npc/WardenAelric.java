package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "WardenAelric", x = 2948, y = 1044, z = 0, stationary = false, aggressive = false)
public final class WardenAelric extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "WardenAelric";

  public static final String DISPLAY_NAME = "${npc.wardenaelric}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield")),
          0,
          List.of(),
          "${npc.welcome.wardenaelric}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wardenaelric.0.0}",
                      "${npc.topic_keyword.wardenaelric.0.1}"),
                  "${npc.topic.wardenaelric.0}",
                  List.of(new NpcSpec.Action(ActionType.GIVE_QUEST, List.of("renewed_wards")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wardenaelric.1.0}",
                      "${npc.topic_keyword.wardenaelric.1.1}"),
                  "${npc.topic.wardenaelric.1}",
                  List.of())),
          "WardenAelricNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public WardenAelric(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
