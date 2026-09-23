package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0022: stationed in Silversky proper (1550,2450), worldZ 0 - gives "silversky_borderwatch",
// a standalone access quest that unlocks the Sunken Chancel's fast-travel entry without requiring
// the player to already be inside the Chancel. TideWardenBryn (the Chancel's own quest-giver) is
// stationed at the Chancel's edge and hands out silversky_tide_warden, whose kill target lives
// inside the zone itself - fine as the zone's main questline, but not a real "before you go"
// checkpoint. Corwin's own target (Antelope) is ordinary wildlife around Silversky's own
// outskirts, so this quest is completable without ever setting foot in the Chancel.
@Spawn(type = "SentinelCorwin", x = 1550, y = 2450, z = 0, stationary = false, aggressive = false)
public final class SentinelCorwin extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SentinelCorwin";

  public static final String DISPLAY_NAME = "${npc.sentinelcorwin}";

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
          "${npc.welcome.sentinelcorwin}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.sentinelcorwin.0.0}",
                      "${npc.topic_keyword.sentinelcorwin.0.1}"),
                  "${npc.topic.sentinelcorwin.0}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.GIVE_QUEST, List.of("silversky_borderwatch")))),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sentinelcorwin.1.0}"),
                  "${npc.topic.sentinelcorwin.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.sentinelcorwin.2.0}",
                      "${npc.topic_keyword.sentinelcorwin.2.1}"),
                  "${npc.topic.sentinelcorwin.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sentinelcorwin.3.0}"),
                  "${npc.topic.sentinelcorwin.3}",
                  List.of())),
          "SentinelCorwinNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public SentinelCorwin(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
