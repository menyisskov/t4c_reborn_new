package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "MarshalTorrhen", x = 1700, y = 1560, z = 0, stationary = false, aggressive = false)
public final class MarshalTorrhen extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MarshalTorrhen";

  public static final String DISPLAY_NAME = "${npc.marshaltorrhen}";

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
          "${npc.welcome.marshaltorrhen}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marshaltorrhen.0.0}",
                      "${npc.topic_keyword.marshaltorrhen.0.1}"),
                  "${npc.topic.marshaltorrhen.0}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.GIVE_QUEST, List.of("windhowl_marches_centaurs")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marshaltorrhen.1.0}",
                      "${npc.topic_keyword.marshaltorrhen.1.1}"),
                  "${npc.topic.marshaltorrhen.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marshaltorrhen.2.0}",
                      "${npc.topic_keyword.marshaltorrhen.2.1}"),
                  "${npc.topic.marshaltorrhen.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marshaltorrhen.3.0}"),
                  "${npc.topic.marshaltorrhen.3}",
                  List.of())),
          "MarshalTorrhenNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public MarshalTorrhen(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
