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
// declaratively via an OPEN_SPELL_LEARNING action, exactly as SkywatchIlvara teaches Sentinel —
// teaches the canon "Ancient tier" (t4cfantasy.com/Addon, level 200+ group support spells)
// already shipped in this codebase: Sentinel, Divine Veil, Clemancy, Undead Annihilation and
// Omega Planetoids.
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
                              "sentinel",
                              "divine_veil",
                              "clemancy",
                              "undead_annihilation",
                              "omega_planetoids")))),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.archmagethalindra.1.0}"),
                  "${npc.topic.archmagethalindra.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.archmagethalindra.2.0}"),
                  "${npc.topic.archmagethalindra.2}",
                  List.of())),
          "ArchmageThalindraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public ArchmageThalindra(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
