package com.perso.T4C.content;

import java.util.Set;

/**
 * What this fork added or placed, as opposed to what it inherited from the original game.
 *
 * <p>Extracted from {@code tools/CompendiumExporter} (T4C-0063) because a second caller needed the
 * same lists: {@code spawn/SpawnPlacementTest} checks that everything this fork placed stands on
 * ground a player can actually walk to, and a private copy of the lists in the test would have
 * drifted the first time new content was added to only one of them.
 *
 * <p>"New" means authored by this fork. "Activated" means it already existed in the codebase and a
 * content pass finally gave it a place in the world - player-visible either way, so both are held
 * to the same placement rules, though the compendium labels them differently.
 */
public final class ForkContent {
  private ForkContent() {}

  public static final Set<String> NEW_MONSTER_NAMES =
      Set.of(
          "Drowned Acolyte",
          "Tideclaw Crab",
          "Mordrenn the Drowned Inquisitor",
          "Cinder Whelp",
          "Ashfang Stalker",
          "Ignarok the Emberfang",
          "Centaur Warrior",
          "Centaur King",
          "Barrow Wight",
          "The Hollow King",
          "Kraanian Wyrmling",
          "Lesser Drake",
          "Bastion Warden",
          "Greater Drake",
          "Kraanian Dragonguard",
          "Fey Warden",
          "Moonlit Stalker",
          "Veilbound Wraith",
          "Sundered Sentinel",
          "Sir Caradoc, the Sundered Knight",
          "Ysolde, the Veiled Matriarch",
          "The Verdant Warden",
          "Tideworn Reaver",
          "Coastwarden Ithrak",
          "The Rootcrown Wyrm",
          "The Pyreclaw Wyrm",
          "The Mistwing Wyrm",
          "The Duskmaw Wyrm",
          "The Galecrest Wyrm",
          "Warband Raider",
          "Warband Banner-Bearer",
          "Warband Warlord",
          "The Convergent Wyrm",
          "Sandglass Sentinel I",
          "Sandglass Sentinel II",
          "Sandglass Sentinel III",
          "Sandglass Sentinel IV",
          "Sandglass Sentinel V");

  /** Pre-existing legacy monsters that a content pass placed rather than authored. */
  public static final Set<String> ACTIVATED_MONSTER_NAMES = Set.of("Arch Drake");

  public static final Set<String> NEW_NPC_IDS =
      Set.of(
          "TideWardenBryn",
          "RurikCinderwatch",
          "SpellMerchant",
          "StorageChest",
          "ElderOphira",
          "QuartermasterElenna",
          "WayfarerBryndis",
          "ArchmageThalindra",
          "SisterIlyndra",
          "OutriderKaelis",
          "KeeperTamsin",
          "MarshalTorrhen",
          "WardenCael",
          "GrandmasterVoss",
          "HarbormasterRangor",
          "SentinelCorwin",
          "OutriderHalvard",
          "DockmasterThessaly",
          "EmberSmithCorvain",
          "WardenSeressa",
          "GrandmasterTholvenn",
          "AnchoriteRowan",
          "MirrorwardenYsmera",
          "OldCorrin",
          "KeeperOfTheSixthSeal",
          "TrialWardenOsric",
          "SunkenLedgerCoffer",
          "PlagueWardensStrongbox",
          "WyrmlingsHoardCasket",
          "WarbandsBuriedChest",
          "WardenAelric");

  /** Pre-existing legacy NPCs that a content pass placed rather than authored. */
  public static final Set<String> ACTIVATED_NPC_IDS = Set.of("RhodarHeatforge", "SkywatchIlvara");
}
