package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "OutriderKaelis", x = 2150, y = 2450, z = 0, stationary = false, aggressive = false)
public final class OutriderKaelis extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OutriderKaelis";

  public static final String DISPLAY_NAME = "${npc.outriderkaelis}";

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
          "${npc.welcome.outriderkaelis}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.outriderkaelis.0.0}",
                      "${npc.topic_keyword.outriderkaelis.0.1}"),
                  "${npc.topic.outriderkaelis.0}",
                  List.of(
                      new NpcSpec.Action(ActionType.GIVE_QUEST, List.of("drakes_lair_vigil")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.outriderkaelis.1.0}",
                      "${npc.topic_keyword.outriderkaelis.1.1}"),
                  "${npc.topic.outriderkaelis.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.outriderkaelis.2.0}"),
                  "${npc.topic.outriderkaelis.2}",
                  List.of())),
          "OutriderKaelisNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public OutriderKaelis(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
