package com.perso.T4C.npc.catalog;

import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.TrainingBehavior;
import java.util.List;
import java.util.Map;

public final class TrainingCatalog {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private record E(boolean teach, List<LearnScreen.TrainingOffer> offers) {}

  private static LearnScreen.TrainingOffer o(String i, int l, int g, boolean t) {

    return new LearnScreen.TrainingOffer(i, l, g, t);
  }

  private static final Map<String, E> M =
      Map.ofEntries(
          Map.entry(
              "Asarr",
              new E(true, List.of(o("picklock", 1, 800, true), o("picklock", 100, 25, false)))),
          Map.entry(
              "Dunikus",
              new E(
                  true,
                  List.of(
                      o("lightning_bolt", 10, 10425, true), o("call_lightning", 13, 27508, true)))),
          Map.entry(
              "Lantalir",
              new E(true, List.of(o("meditate", 1, 3000, true), o("meditate", 100, 100, false)))),
          Map.entry(
              "Lyria",
              new E(
                  true,
                  List.of(
                      o("picklock", 1, 800, true),
                      o("peek", 100, 25, false),
                      o("picklock", 100, 100, false)))),
          Map.entry(
              "Mhorgwloth",
              new E(
                  true,
                  List.of(o("rapid_healing", 1, 5000, true), o("rapid_healing", 100, 200, false)))),
          Map.entry(
              "ChryseidaYolangda",
              new E(true, List.of(o("search", 1, 2000, true), o("search", 100, 50, false)))),
          Map.entry(
              "DantalirSongweaver",
              new E(true, List.of(o("sneak", 1, 2500, true), o("sneak", 100, 100, false)))),
          Map.entry(
              "DaranLightfoot",
              new E(true, List.of(o("rob", 1, 5000, true), o("rob", 100, 250, false)))),
          Map.entry(
              "DaranAnnithae",
              new E(true, List.of(o("rob", 1, 5000, true), o("rob", 100, 250, false)))),
          Map.entry(
              "DaranAtrocity",
              new E(true, List.of(o("rob", 1, 5000, true), o("rob", 100, 250, false)))),
          Map.entry("Eldrig", new E(false, List.of(o("attack", 5000, 10, false)))),
          Map.entry(
              "Eldantor",
              new E(true, List.of(o("meditate", 1, 3000, true), o("meditate", 100, 100, false)))),
          Map.entry(
              "TheLurker",
              new E(
                  false,
                  List.of(
                      o("dodge", 5000, 10, false),
                      o("peek", 100, 25, false),
                      o("picklock", 100, 100, false)))),
          Map.entry(
              "MeltarWinterstorm",
              new E(true, List.of(o("hide", 1, 1325, true), o("hide", 100, 75, false)))),
          Map.entry(
              "DerranIronstrife",
              new E(
                  false,
                  List.of(
                      o("attack", 5000, 10, false),
                      o("stun_blow", 100, 20, false),
                      o("powerful_blow", 100, 50, false)))),
          Map.entry(
              "MirymwenFeatherfoot",
              new E(false, List.of(o("dodge", 5000, 10, false), o("archery", 5000, 15, false)))),
          Map.entry(
              "Doremas",
              new E(
                  false,
                  List.of(
                      o("attack", 5000, 10, false),
                      o("parry", 100, 75, false),
                      o("dodge", 5000, 10, false)))),
          Map.entry(
              "Karl",
              new E(false, List.of(o("attack", 5000, 10, false), o("archery", 5000, 15, false)))),
          Map.entry(
              "Garnir",
              new E(
                  false,
                  List.of(
                      o("attack", 5000, 10, false),
                      o("stun_blow", 100, 20, false),
                      o("parry", 100, 75, false),
                      o("powerful_blow", 100, 50, false)))),
          Map.entry(
              "RablekSwiftblade",
              new E(
                  true,
                  List.of(
                      o("armor_penetration", 1, 7500, true),
                      o("armor_penetration", 100, 300, false)))),
          Map.entry(
              "RoenGreenleaf",
              new E(true, List.of(o("first_aid", 1, 1000, true), o("first_aid", 100, 30, false)))),
          Map.entry("Yrian", new E(true, List.of(o("heal_critical", 21, 29457, true)))),
          Map.entry("Kilhiam", new E(true, List.of(o("light", 5, 233, true)))),
          Map.entry("BishopCrowbanner", new E(true, List.of(o("healing", 35, 110825, true)))),
          Map.entry(
              "AloysiusStarbolt",
              new E(
                  true,
                  List.of(
                      o("detect_hidden", 16, 47425, true),
                      o("flame_wave", 18, 60800, true),
                      o("resist_ice", 20, 79028, true),
                      o("detect_invisible", 21, 92500, true)))),
          Map.entry(
              "Kavarian",
              new E(
                  true,
                  List.of(
                      o("dispel", 28, 170913, true),
                      o("flare", 25, 130800, true),
                      o("major_combat_sense", 27, 156948, true),
                      o("invisibility", 24, 122612, true)))),
          Map.entry(
              "Khimtesar",
              new E(
                  true,
                  List.of(
                      o("sunken_woods_gateway", 5, 175700, true),
                      o("lighthaven_gateway", 5, 175700, true),
                      o("windhowl_gateway", 5, 175700, true),
                      o("silversky_gateway", 5, 175700, true),
                      o("stonecrest_gateway", 5, 175700, true),
                      o("druid_s_point_gateway", 5, 175700, true),
                      o("wizard_s_vale_gateway", 5, 175700, true)))),
          Map.entry(
              "CelestinaWaterbreeze",
              new E(
                  true,
                  List.of(o("resist_fire", 19, 69617, true), o("ice_ball", 20, 75825, true)))),
          Map.entry(
              "MithannaSnowraven",
              new E(
                  true,
                  List.of(o("ice_storm", 26, 152425, true), o("blizzard", 29, 195508, true)))),
          Map.entry(
              "MarsacCred",
              new E(true, List.of(o("freeze", 11, 17200, true), o("ice_bolt", 13, 25625, true)))),
          Map.entry(
              "BrotherThorkas",
              new E(
                  true,
                  List.of(o("turn_undead", 17, 15713, true), o("tranquility", 26, 55252, true)))),
          Map.entry(
              "NissusHaloseeker",
              new E(
                  true,
                  List.of(
                      o("bless", 37, 126673, true),
                      o("mass_healing", 39, 143577, true),
                      o("healing_mist", 45, 200625, true)))),
          Map.entry("Mordenthal", new E(true, List.of(o("soul_steal", 41, 166192, true)))),
          Map.entry(
              "ElysanaBlackrose",
              new E(
                  true,
                  List.of(
                      o("minor_combat_sense", 13, 23808, true), o("mana_burst", 15, 40192, true)))),
          Map.entry(
              "Giamas",
              new E(
                  true,
                  List.of(
                      o("earthen_strength", 12, 22057, true),
                      o("stone_skin", 17, 52577, true),
                      o("earthquake", 20, 82297, true)))),
          Map.entry(
              "Etheanan",
              new E(
                  true,
                  List.of(
                      o("fire_shield", 21, 85632, true),
                      o("rain_of_fire", 22, 103297, true),
                      o("mana_surge", 23, 107028, true),
                      o("mana_shield", 14, 33553, true),
                      o("glacier", 23, 110825, true),
                      o("electric_shield", 21, 89033, true)))),
          Map.entry(
              "Xanth",
              new E(
                  true,
                  List.of(
                      o("drain_life", 19, 22057, true),
                      o("plague", 24, 42537, true),
                      o("greater_drain", 28, 63673, true)))),
          Map.entry(
              "ShamanWeethgwotha",
              new E(
                  true,
                  List.of(
                      o("entangle", 15, 37913, true),
                      o("boulders", 27, 161537, true),
                      o("true_sight", 28, 170913, true)))),
          Map.entry(
              "TwinShovanis",
              new E(
                  true,
                  List.of(
                      o("dust_devil", 7, 2388, true),
                      o("curse", 17, 17200, true),
                      o("word_of_recall", 12, 18753, true)))),
          Map.entry(
              "Filandrius",
              new E(
                  true,
                  List.of(
                      o("firestorm", 30, 205808, true),
                      o("meteor", 38, 350000, true),
                      o("avalanche", 32, 238292, true),
                      o("tsunami", 37, 323328, true),
                      o("inferno", 32, 243937, true),
                      o("tornado", 31, 221753, true),
                      o("hurricane", 34, 273152, true),
                      o("ice_shield", 33, 249648, true),
                      o("lighthaven_portal", 10, 297712, true),
                      o("druid_s_point_portal", 10, 297712, true),
                      o("silversky_portal", 10, 297712, true),
                      o("stonecrest_portal", 10, 297712, true),
                      o("sunken_woods_portal", 10, 297712, true),
                      o("windhowl_portal", 10, 297712, true),
                      o("wizard_s_vale_portal", 10, 297712, true)))),
          Map.entry(
              "Kederic",
              new E(
                  true,
                  List.of(o("barrier", 12, 18753, true), o("clear_thought", 14, 31472, true)))),
          Map.entry(
              "ArganorIargh",
              new E(
                  true,
                  List.of(
                      o("stun_blow", 1, 150, true),
                      o("parry", 1, 900, true),
                      o("powerful_blow", 1, 2500, true)))),
          Map.entry(
              "MorindinArrowmist",
              new E(true, List.of(o("first_aid", 1, 1000, true), o("parry", 1, 900, true)))),
          Map.entry(
              "HunterTruggWorgwloth",
              new E(
                  true,
                  List.of(o("rapid_healing", 1, 5000, true), o("rapid_healing", 100, 200, false)))),
          Map.entry(
              "LiurnClar",
              new E(
                  true, List.of(o("fire_bolt", 12, 20372, true), o("fireball", 14, 29457, true)))),
          Map.entry(
              "Zhakar",
              new E(
                  true,
                  List.of(
                      o("chain_lightning", 15, 35700, true),
                      o("vortex_of_air", 18, 60800, true),
                      o("nimbleness", 19, 72688, true)))),
          Map.entry(
              "TideWardenBryn",
              new E(
                  true,
                  List.of(
                      o("riptide_surge", 30, 50000, true), o("drowned_ward", 33, 65000, true)))),
          Map.entry(
              "RurikCinderwatch",
              new E(
                  true,
                  List.of(
                      o("cinderburst", 45, 140000, true),
                      o("emberheart_resolve", 44, 100000, true)))));

  public static NpcBehavior get(String id) {

    E e = M.get(id);

    return e == null ? null : new TrainingBehavior(e.teach, e.offers);
  }
}
