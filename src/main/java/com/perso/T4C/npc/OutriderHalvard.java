package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0022: stationed in Windhowl proper (1830,1310), worldZ 0 - gives "windhowl_borderwatch",
// a standalone access quest that unlocks Cinderreach Hills' fast-travel entry without requiring
// the player to already be inside the Hills. RurikCinderwatch (the Hills' own quest-giver) sits
// right at the zone's boundary and hands out emberfang_hills_bounty, whose kill target lives
// inside the zone itself - fine as the zone's main questline, but not a real "before you go"
// checkpoint. Halvard's own target (Brigand) is ordinary banditry on the roads around Windhowl,
// so this quest is completable without ever setting foot in the Hills.
@Spawn(type = "OutriderHalvard", x = 1830, y = 1310, z = 0, stationary = false, aggressive = false)
public final class OutriderHalvard extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OutriderHalvard";

  public static final String DISPLAY_NAME = "${npc.outriderhalvard}";

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
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield")),
          0,
          List.of(),
          "${npc.welcome.outriderhalvard}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.outriderhalvard.0.0}",
                      "${npc.topic_keyword.outriderhalvard.0.1}"),
                  "${npc.topic.outriderhalvard.0}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.GIVE_QUEST, List.of("windhowl_borderwatch")))),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.outriderhalvard.1.0}"),
                  "${npc.topic.outriderhalvard.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.outriderhalvard.2.0}",
                      "${npc.topic_keyword.outriderhalvard.2.1}"),
                  "${npc.topic.outriderhalvard.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.outriderhalvard.3.0}"),
                  "${npc.topic.outriderhalvard.3}",
                  List.of())),
          "OutriderHalvardNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public OutriderHalvard(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
