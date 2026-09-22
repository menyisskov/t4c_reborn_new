package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// Spell trainer at the Avalon Sanctuary Spell Trainer's Tower door (1362,1512), worldZ 0. Wired
// declaratively via an OPEN_SPELL_LEARNING action, exactly as SkywatchIlvara teaches Leyward
// Bastion — teaches Avalon's invented "Elder"-tier ley-line spells (level 40-260 group support
// and nukes): Leyward Bastion, Veilstone Aegis, Wellspring Mercy, Sunscour and Gravebreaker.
// (Originally named after a real t4cfantasy.com/Addon "Ancient tier" spell list; renamed to
// invented names that don't collide with the real game's own spells.) Also teaches the
// "Apex"-tier spells (level 320-900, T4C-0018) that fill the gap between the Elder tier and the
// level-1000 curve cap / Avalon boss band: Voidreave Lance, Stormcaller's Judgment, Sanctum
// Ward, Emberqueen's Wrath and Cataclysm's Herald.
@Spawn(type = "ArchmageThalindra", x = 1362, y = 1512, z = 0, stationary = false, aggressive = false)
public final class ArchmageThalindra extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "ArchmageThalindra";

  public static final String DISPLAY_NAME = "${npc.archmagethalindra}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.archmagethalindra}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.0.0}",
                      "${npc.topic_keyword.archmagethalindra.0.1}",
                      "${npc.topic_keyword.archmagethalindra.0.2}",
                      "${npc.topic_keyword.archmagethalindra.0.3}"),
                  "${npc.topic.archmagethalindra.0}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING,
                          List.of(
                              "leyward_bastion",
                              "veilstone_aegis",
                              "wellspring_mercy",
                              "sunscour",
                              "gravebreaker")))),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.archmagethalindra.1.0}"),
                  "${npc.topic.archmagethalindra.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.archmagethalindra.2.0}"),
                  "${npc.topic.archmagethalindra.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.archmagethalindra.3.0}"),
                  "${npc.topic.archmagethalindra.3}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING,
                          List.of(
                              "voidreave_lance",
                              "stormcallers_judgment",
                              "sanctum_ward",
                              "emberqueens_wrath",
                              "cataclysms_herald"))))),
          "ArchmageThalindraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public ArchmageThalindra(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
