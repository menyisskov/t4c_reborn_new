package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0033: one of the three surviving Forgewrights of the First Pact, now working within the
// Avalon Wilds (1300,1460), worldZ 0 - the order that helped bind Avalon's original fey pact
// centuries ago (see npc/HarbormasterRangor.java, quest/definition/PassageToAvalon.java for the
// pact's established lore). Corvain tempers the Godsforged chain's physical component (a Tempered
// Godcore) from Wyrmforged Embers - see quest/definition/ForgeTheGodcore.java. A standard
// single-item turn-in quest; no custom behavior needed here.
@Spawn(type = "EmberSmithCorvain", x = 1300, y = 1460, z = 0, stationary = false, aggressive = false)
public final class EmberSmithCorvain extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "EmberSmithCorvain";

  public static final String DISPLAY_NAME = "${npc.embersmithcorvain}";

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
          "${npc.welcome.embersmithcorvain}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.embersmithcorvain.0.0}",
                      "${npc.topic_keyword.embersmithcorvain.0.1}"),
                  "${npc.topic.embersmithcorvain.0}",
                  List.of(new NpcSpec.Action(ActionType.GIVE_QUEST, List.of("forge_the_godcore")))),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.embersmithcorvain.1.0}"),
                  "${npc.topic.embersmithcorvain.1}",
                  List.of())),
          "EmberSmithCorvainNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public EmberSmithCorvain(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
