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
// declaratively via OPEN_SPELL_LEARNING actions, exactly as SkywatchIlvara teaches Leyward
// Bastion. "train"/"learn" opens Avalon's ley-line support spells (Veilstone Aegis, Wellspring
// Mercy, Leyward Bastion, Dawnwell Renewal, Sanctum Ward). Naming a school ("fire", "water",
// "earth", "air", "dark", "light") opens that school's high-tier attack ladder - one spell at each
// of levels 150/200/250/300/350/400, all built on HighTierSpellCurve (T4C-0025). "mantle" opens a
// small shop of the six elemental archmage mantles.
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
                              "veilstone_aegis",
                              "wellspring_mercy",
                              "leyward_bastion",
                              "dawnwell_renewal",
                              "sanctum_ward")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.1.0}"),
                  "${npc.topic.archmagethalindra.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.2.0}"),
                  "${npc.topic.archmagethalindra.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.3.0}"),
                  "${npc.topic.archmagethalindra.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.4.0}"),
                  "${npc.topic.archmagethalindra.4}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING,
                          List.of(
                              "scorchbrand",
                              "pyreburst",
                              "magmaheart_lance",
                              "sunforge_brand",
                              "emberqueens_wrath",
                              "ashfall")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.5.0}"),
                  "${npc.topic.archmagethalindra.5}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING,
                          List.of(
                              "rime_lance",
                              "frostgale",
                              "abyssal_spear",
                              "tidebreaker",
                              "drowning_deep",
                              "cataclysms_herald")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.6.0}"),
                  "${npc.topic.archmagethalindra.6}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING,
                          List.of(
                              "stonefang",
                              "land_slide",
                              "gravebreaker",
                              "mountains_fist",
                              "worldroot_upheaval",
                              "tectonic_ruin")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.7.0}"),
                  "${npc.topic.archmagethalindra.7}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING,
                          List.of(
                              "galespike",
                              "thunderhead",
                              "skysplitter",
                              "tempest_lance",
                              "stormcallers_judgment",
                              "heavenfall")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.8.0}"),
                  "${npc.topic.archmagethalindra.8}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING,
                          List.of(
                              "nightfang",
                              "shadowblight",
                              "soulrend",
                              "voidreave_lance",
                              "umbral_tide",
                              "eclipse_of_ruin")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.9.0}"),
                  "${npc.topic.archmagethalindra.9}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING,
                          List.of(
                              "sunscour",
                              "dawnflare",
                              "radiant_spear",
                              "seraphs_verdict",
                              "hallowed_nova",
                              "solar_apotheosis")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archmagethalindra.10.0}",
                      "${npc.topic_keyword.archmagethalindra.10.1}"),
                  "${npc.topic.archmagethalindra.10}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SHOP,
                          List.of(
                              "pyromancers_mantle",
                              "tidecallers_mantle",
                              "geomancers_mantle",
                              "windweavers_mantle",
                              "lightbringers_mantle",
                              "shadowmancers_mantle"))))),
          "ArchmageThalindraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public ArchmageThalindra(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
