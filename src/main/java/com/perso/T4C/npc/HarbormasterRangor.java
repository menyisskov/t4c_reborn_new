package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// The last dock still standing on the mainland shore facing Avalon (1500,1200), worldZ 0 - gives
// "passage_to_avalon" (see quest/definition/PassageToAvalon.java), the special access quest that
// unlocks the crossing itself, the same role the Oracle plays for rebirth: without completing it,
// no fresh character can ever reach Avalon Sanctuary, since every NPC and shop that could sell a
// scroll_of_avalon or teach AvalonGateway is itself stationed inside Avalon.
@Spawn(type = "HarbormasterRangor", x = 1500, y = 1200, z = 0, stationary = false, aggressive = false)
public final class HarbormasterRangor extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HarbormasterRangor";

  public static final String DISPLAY_NAME = "${npc.harbormasterrangor}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.harbormasterrangor}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.harbormasterrangor.0.0}",
                      "${npc.topic_keyword.harbormasterrangor.0.1}"),
                  "${npc.topic.harbormasterrangor.0}",
                  List.of(
                      new NpcSpec.Action(ActionType.GIVE_QUEST, List.of("passage_to_avalon")))),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.harbormasterrangor.1.0}"),
                  "${npc.topic.harbormasterrangor.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.harbormasterrangor.2.0}"),
                  "${npc.topic.harbormasterrangor.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.harbormasterrangor.3.0}"),
                  "${npc.topic.harbormasterrangor.3}",
                  List.of())),
          "HarbormasterRangorNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public HarbormasterRangor(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
