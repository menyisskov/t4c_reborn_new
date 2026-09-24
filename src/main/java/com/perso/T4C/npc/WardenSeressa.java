package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0033: one of the three surviving Forgewrights of the First Pact (see
// npc/EmberSmithCorvain.java), working within the Avalon Wilds (1355,1465), worldZ 0. Seressa
// binds the Godsforged chain's arcane component (a Bound Godsigil) from Veiled Aether Shards -
// see quest/definition/BindTheGodsigil.java. A standard single-item turn-in quest; no custom
// behavior needed here.
@Spawn(type = "WardenSeressa", x = 1355, y = 1465, z = 0, stationary = false, aggressive = false)
public final class WardenSeressa extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "WardenSeressa";

  public static final String DISPLAY_NAME = "${npc.wardenseressa}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.wardenseressa}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wardenseressa.0.0}",
                      "${npc.topic_keyword.wardenseressa.0.1}"),
                  "${npc.topic.wardenseressa.0}",
                  List.of(new NpcSpec.Action(ActionType.GIVE_QUEST, List.of("bind_the_godsigil")))),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.wardenseressa.1.0}"),
                  "${npc.topic.wardenseressa.1}",
                  List.of())),
          "WardenSeressaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public WardenSeressa(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
