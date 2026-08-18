package com.perso.T4C.mapping.definition;

import com.perso.T4C.render.ObjectMapping;
import java.util.Map;

public final class ObjectMappingDefinitions {
  private ObjectMappingDefinitions() {}

  public static Map<String, ObjectMapping> all() {
    return Map.ofEntries(
        Map.entry(
            "APPLE",
            new ObjectMapping(
                40006, "Fruits 1", false, false, "", "", false, "${object.apple}", 0)),
        Map.entry(
            "BALANCE",
            new ObjectMapping(
                0, "Misc 4 - Part 1 6", false, false, "", "", false, "${object.balance}", 0)),
        Map.entry(
            "BANJO",
            new ObjectMapping(
                0, "Misc 7 - All 9", false, false, "", "", false, "${object.banjo}", 0)),
        Map.entry(
            "BATTLE CHEST 1",
            new ObjectMapping(
                41470, "Chest", false, false, "", "", false, "${object.battle_chest_1}", 0)),
        Map.entry(
            "BATTLE CHEST 2",
            new ObjectMapping(
                41471, "Chest", false, false, "", "", false, "${object.battle_chest_2}", 0)),
        Map.entry(
            "BATTLE CHEST 3",
            new ObjectMapping(
                41472, "Chest", false, false, "", "", false, "${object.battle_chest_3}", 0)),
        Map.entry(
            "BEER_MUG",
            new ObjectMapping(
                0, "Misc 1 - Part 2 2", false, false, "", "", false, "${object.beer_mug}", 0)),
        Map.entry(
            "BEER_MUG_TOO_FULL",
            new ObjectMapping(
                0,
                "Misc 1 - Part 2 3",
                false,
                false,
                "",
                "",
                false,
                "${object.beer_mug_too_full}",
                0)),
        Map.entry(
            "BEVERAGE",
            new ObjectMapping(
                0, "Misc 1 - Part 2 5", false, false, "", "", false, "${object.beverage}", 0)),
        Map.entry(
            "BLACK_POTION",
            new ObjectMapping(
                0, "Misc 9 - All 4", false, false, "", "", false, "${object.black_potion}", 0)),
        Map.entry(
            "BLUE_POTION",
            new ObjectMapping(
                0, "Misc 9 - All 2", false, false, "", "", false, "${object.blue_potion}", 0)),
        Map.entry(
            "BONE CHEST",
            new ObjectMapping(
                41482, "Chest", false, false, "", "", false, "${object.bone_chest}", 0)),
        Map.entry(
            "BREAD",
            new ObjectMapping(
                0, "Misc 6 - All 6", false, false, "", "", false, "${object.bread}", 0)),
        Map.entry(
            "BROOM_ON_FLOOR",
            new ObjectMapping(
                0,
                "Misc 4 - Part 1 3",
                false,
                false,
                "",
                "",
                false,
                "${object.broom_on_floor}",
                0)),
        Map.entry(
            "BROOM_ON_WALL",
            new ObjectMapping(
                0, "Misc 4 - Part 1 1", false, false, "", "", false, "${object.broom_on_wall}", 0)),
        Map.entry(
            "CAMPFIRE",
            new ObjectMapping(
                40840, "Camp1", false, false, "", "", false, "${object.campfire}", 0)),
        Map.entry(
            "CD ZONE CHEST 1",
            new ObjectMapping(
                41162, "Chest", false, false, "", "", false, "${object.cd_zone_chest_1}", 0)),
        Map.entry(
            "CD ZONE CHEST 2",
            new ObjectMapping(
                41163, "Chest", false, false, "", "", false, "${object.cd_zone_chest_2}", 0)),
        Map.entry(
            "CD ZONE CHEST 3",
            new ObjectMapping(
                41164, "Chest", false, false, "", "", false, "${object.cd_zone_chest_3}", 0)),
        Map.entry(
            "CD ZONE CHEST 4",
            new ObjectMapping(
                41165, "Chest", false, false, "", "", false, "${object.cd_zone_chest_4}", 0)),
        Map.entry(
            "CD ZONE CHEST 5",
            new ObjectMapping(
                41166, "Chest", false, false, "", "", false, "${object.cd_zone_chest_5}", 0)),
        Map.entry(
            "CENTAUR CHEST 1",
            new ObjectMapping(
                41285, "Chest", false, false, "", "", false, "${object.centaur_chest_1}", 0)),
        Map.entry(
            "CENTAUR CHEST 2",
            new ObjectMapping(
                41286, "Chest", false, false, "", "", false, "${object.centaur_chest_2}", 0)),
        Map.entry(
            "CENTAUR CHEST 3",
            new ObjectMapping(
                41287, "Chest", false, false, "", "", false, "${object.centaur_chest_3}", 0)),
        Map.entry(
            "CHANDELLE",
            new ObjectMapping(
                0, "Lights 1", false, false, "", "", false, "${object.chandelle}", 0)),
        Map.entry(
            "CHANDELLE_SUR_PIED",
            new ObjectMapping(
                0, "Lights 2", false, false, "", "", false, "${object.chandelle_sur_pied}", 0)),
        Map.entry(
            "CHEST 1",
            new ObjectMapping(40086, "Chest", false, false, "", "", false, "${object.chest_1}", 0)),
        Map.entry(
            "CHEST 10",
            new ObjectMapping(
                40097, "Chest", false, false, "", "", false, "${object.chest_10}", 0)),
        Map.entry(
            "CHEST 11",
            new ObjectMapping(
                40098, "Chest", false, false, "", "", false, "${object.chest_11}", 0)),
        Map.entry(
            "CHEST 12",
            new ObjectMapping(
                40099, "Chest", false, false, "", "", false, "${object.chest_12}", 0)),
        Map.entry(
            "CHEST 13",
            new ObjectMapping(
                40101, "Chest", false, false, "", "", false, "${object.chest_13}", 0)),
        Map.entry(
            "CHEST 14",
            new ObjectMapping(
                40111, "Chest", false, false, "", "", false, "${object.chest_14}", 0)),
        Map.entry(
            "CHEST 15",
            new ObjectMapping(
                40121, "Chest", false, false, "", "", false, "${object.chest_15}", 0)),
        Map.entry(
            "CHEST 2",
            new ObjectMapping(40087, "Chest", false, false, "", "", false, "${object.chest_2}", 0)),
        Map.entry(
            "CHEST 3",
            new ObjectMapping(40088, "Chest", false, false, "", "", false, "${object.chest_3}", 0)),
        Map.entry(
            "CHEST 4",
            new ObjectMapping(40089, "Chest", false, false, "", "", false, "${object.chest_4}", 0)),
        Map.entry(
            "CHEST 5",
            new ObjectMapping(40090, "Chest", false, false, "", "", false, "${object.chest_5}", 0)),
        Map.entry(
            "CHEST 6",
            new ObjectMapping(40091, "Chest", false, false, "", "", false, "${object.chest_6}", 0)),
        Map.entry(
            "CHEST 7",
            new ObjectMapping(40092, "Chest", false, false, "", "", false, "${object.chest_7}", 0)),
        Map.entry(
            "CHICKEN",
            new ObjectMapping(
                0, "Misc 6 - All 5", false, false, "", "", false, "${object.chicken}", 0)),
        Map.entry(
            "CLOSED_WOODEN_DOOR",
            new ObjectMapping(
                0,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                true,
                "${object.closed_wooden_door}",
                1)),
        Map.entry(
            "CLOSED_WOODEN_DOOR_FLIP",
            new ObjectMapping(
                0,
                "RockDoor%d$11",
                true,
                true,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                true,
                "${object.closed_wooden_door_flip}",
                1)),
        Map.entry(
            "CUP_FULL",
            new ObjectMapping(
                0, "Misc 1 - Part 2 6", false, false, "", "", false, "${object.cup_full}", 0)),
        Map.entry(
            "DANTALIR CHEST",
            new ObjectMapping(
                41680, "Chest", false, false, "", "", false, "${object.dantalir_chest}", 0)),
        Map.entry(
            "DEAD_FISHES",
            new ObjectMapping(
                0, "Misc 7 - All 2", false, false, "", "", false, "${object.dead_fishes}", 0)),
        Map.entry(
            "DECOR003",
            new ObjectMapping(
                0, "Misc 2 - All 3", false, false, "", "", false, "${object.decor003}", 0)),
        Map.entry(
            "DECOR011",
            new ObjectMapping(
                0, "Misc 6 - All 8", false, false, "", "", false, "${object.decor011}", 0)),
        Map.entry(
            "DECOR015",
            new ObjectMapping(
                0, "Misc 9 - All 8", false, false, "", "", false, "${object.decor015}", 0)),
        Map.entry(
            "DECOR016",
            new ObjectMapping(0, "Crates 1", false, false, "", "", false, "${object.decor016}", 0)),
        Map.entry(
            "EMPTY_GLASS",
            new ObjectMapping(
                0, "Misc 1 - Part 2 4", false, false, "", "", false, "${object.empty_glass}", 0)),
        Map.entry(
            "EMPTY_MUG",
            new ObjectMapping(
                0, "Misc 1 - Part 2 1", false, false, "", "", false, "${object.empty_mug}", 0)),
        Map.entry(
            "FAKE WOODEN DOOR",
            new ObjectMapping(
                41522,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.fake_wooden_door}",
                0)),
        Map.entry(
            "FAT_JAMBON",
            new ObjectMapping(
                0, "Misc 6 - All 4", false, false, "", "", false, "${object.fat_jambon}", 0)),
        Map.entry(
            "FISHING_POLE",
            new ObjectMapping(
                0, "Misc 7 - All 1", false, false, "", "", false, "${object.fishing_pole}", 0)),
        Map.entry(
            "FLIPPED WOODEN DOOR",
            new ObjectMapping(
                41487,
                "RockDoor%d$11",
                true,
                true,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.flipped_wooden_door}",
                0)),
        Map.entry(
            "HOURGLASS",
            new ObjectMapping(
                40262, "Misc 4 - Part 1 7", false, false, "", "", false, "${object.hourglass}", 0)),
        Map.entry(
            "JAMBON",
            new ObjectMapping(
                0, "Misc 6 - All 1", false, false, "", "", false, "${object.jambon}", 0)),
        Map.entry(
            "KITCHEN_BOWL",
            new ObjectMapping(
                0, "Kitchen 2", false, false, "", "", false, "${object.kitchen_bowl}", 0)),
        Map.entry(
            "KITCHEN_FORK",
            new ObjectMapping(
                0, "Kitchen 4", false, false, "", "", false, "${object.kitchen_fork}", 0)),
        Map.entry(
            "LONG_SAUSAGE",
            new ObjectMapping(
                0, "Misc 6 - All 2", false, false, "", "", false, "${object.long_sausage}", 0)),
        Map.entry(
            "LONGUE_VUE",
            new ObjectMapping(
                0, "Misc 1 - Part 1 1", false, false, "", "", false, "${object.longue_vue}", 0)),
        Map.entry(
            "MAD CHEST 1",
            new ObjectMapping(
                41601, "Misc 2 - All 1", false, false, "", "", false, "${object.mad_chest_1}", 0)),
        Map.entry(
            "MAD CHEST 2",
            new ObjectMapping(
                41602, "Misc 2 - All 1", false, false, "", "", false, "${object.mad_chest_2}", 0)),
        Map.entry(
            "MAD CHEST 3",
            new ObjectMapping(
                41603, "Misc 2 - All 1", false, false, "", "", false, "${object.mad_chest_3}", 0)),
        Map.entry(
            "MADRIGAN CHEST",
            new ObjectMapping(
                41599,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.madrigan_chest}",
                0)),
        Map.entry(
            "MAP CAULDRON",
            new ObjectMapping(
                41708,
                "64kTrollCauldron-a",
                false,
                false,
                "",
                "",
                false,
                "${object.map_cauldron}",
                0)),
        Map.entry(
            "MAP FLIPPED LOCKED DOOR",
            new ObjectMapping(
                41604,
                "RockDoor%d$11",
                true,
                true,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.map_flipped_locked_door}",
                0)),
        Map.entry(
            "MAP LOCKED CHEST",
            new ObjectMapping(
                40893, "Chest", false, false, "", "", false, "${object.map_locked_chest}", 0)),
        Map.entry(
            "MAP LOCKED DOOR",
            new ObjectMapping(
                40892,
                "RockDoor%d$11",
                true,
                true,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.map_locked_door}",
                0)),
        Map.entry(
            "MAP PILE OF GOLD",
            new ObjectMapping(
                40894, "Coins 3", false, false, "", "", false, "${object.map_pile_of_gold}", 0)),
        Map.entry(
            "MAP WHIRLPOOL",
            new ObjectMapping(
                41363,
                "64kSpellGateGround-a",
                false,
                false,
                "",
                "",
                false,
                "${object.map_whirlpool}",
                0)),
        Map.entry(
            "MAP WHIRLPOOL GREY",
            new ObjectMapping(
                41684,
                "64kSpellGateGround2-a",
                false,
                false,
                "",
                "",
                false,
                "${object.map_whirlpool_grey}",
                0)),
        Map.entry(
            "MC CHEST 1",
            new ObjectMapping(
                41274, "Chest", false, false, "", "", false, "${object.mc_chest_1}", 0)),
        Map.entry(
            "MC CHEST 2",
            new ObjectMapping(
                41275, "Chest", false, false, "", "", false, "${object.mc_chest_2}", 0)),
        Map.entry(
            "MC CHEST 3",
            new ObjectMapping(
                41276, "Chest", false, false, "", "", false, "${object.mc_chest_3}", 0)),
        Map.entry(
            "MC CHEST 4",
            new ObjectMapping(
                41277, "Chest", false, false, "", "", false, "${object.mc_chest_4}", 0)),
        Map.entry(
            "MC CHEST 5",
            new ObjectMapping(
                41278, "Chest", false, false, "", "", false, "${object.mc_chest_5}", 0)),
        Map.entry(
            "MC CHEST 6",
            new ObjectMapping(
                41279, "Chest", false, false, "", "", false, "${object.mc_chest_6}", 0)),
        Map.entry(
            "MC CHEST 7",
            new ObjectMapping(
                41280, "Chest", false, false, "", "", false, "${object.mc_chest_7}", 0)),
        Map.entry(
            "MIRROR",
            new ObjectMapping(0, "Mirrors 1", false, false, "", "", false, "${object.mirror}", 0)),
        Map.entry(
            "MORDENTHAL DUNGEON 1",
            new ObjectMapping(
                41167, "Chest", false, false, "", "", false, "${object.mordenthal_dungeon_1}", 0)),
        Map.entry(
            "MORDENTHAL DUNGEON 2",
            new ObjectMapping(
                41168, "Chest", false, false, "", "", false, "${object.mordenthal_dungeon_2}", 0)),
        Map.entry(
            "MORDENTHAL DUNGEON 3",
            new ObjectMapping(
                41169, "Chest", false, false, "", "", false, "${object.mordenthal_dungeon_3}", 0)),
        Map.entry(
            "MORDENTHAL DUNGEON 4",
            new ObjectMapping(
                41271, "Chest", false, false, "", "", false, "${object.mordenthal_dungeon_4}", 0)),
        Map.entry(
            "MORDENTHAL DUNGEON 5",
            new ObjectMapping(
                41272, "Chest", false, false, "", "", false, "${object.mordenthal_dungeon_5}", 0)),
        Map.entry(
            "MORDENTHAL DUNGEON 6",
            new ObjectMapping(
                41273, "Chest", false, false, "", "", false, "${object.mordenthal_dungeon_6}", 0)),
        Map.entry(
            "NETHER CHEST 1",
            new ObjectMapping(
                41289, "Chest", false, false, "", "", false, "${object.nether_chest_1}", 0)),
        Map.entry(
            "NETHER CHEST 2",
            new ObjectMapping(
                41290, "Chest", false, false, "", "", false, "${object.nether_chest_2}", 0)),
        Map.entry(
            "NETHER CHEST 3",
            new ObjectMapping(
                41291, "Chest", false, false, "", "", false, "${object.nether_chest_3}", 0)),
        Map.entry(
            "NO RETURN CHEST 1",
            new ObjectMapping(
                41696, "Chest", false, false, "", "", false, "${object.no_return_chest_1}", 0)),
        Map.entry(
            "NO RETURN CHEST 2",
            new ObjectMapping(
                41697, "Chest", false, false, "", "", false, "${object.no_return_chest_2}", 0)),
        Map.entry(
            "NO RETURN CHEST 3",
            new ObjectMapping(
                41698, "Chest", false, false, "", "", false, "${object.no_return_chest_3}", 0)),
        Map.entry(
            "ORACLE BONUS CHEST 1",
            new ObjectMapping(
                41494,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_bonus_chest_1}",
                0)),
        Map.entry(
            "ORACLE BONUS CHEST 2",
            new ObjectMapping(
                41495,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_bonus_chest_2}",
                0)),
        Map.entry(
            "ORACLE BONUS CHEST 3",
            new ObjectMapping(
                41496,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_bonus_chest_3}",
                0)),
        Map.entry(
            "ORACLE BONUS CHEST 4",
            new ObjectMapping(
                41523,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_bonus_chest_4}",
                0)),
        Map.entry(
            "ORACLE CHEST 1",
            new ObjectMapping(
                41438,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_1}",
                0)),
        Map.entry(
            "ORACLE CHEST 10",
            new ObjectMapping(
                41447,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_10}",
                0)),
        Map.entry(
            "ORACLE CHEST 11",
            new ObjectMapping(
                41448,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_11}",
                0)),
        Map.entry(
            "ORACLE CHEST 12",
            new ObjectMapping(
                41489,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_12}",
                0)),
        Map.entry(
            "ORACLE CHEST 13",
            new ObjectMapping(
                41490,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_13}",
                0)),
        Map.entry(
            "ORACLE CHEST 16",
            new ObjectMapping(
                41514,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_16}",
                0)),
        Map.entry(
            "ORACLE CHEST 2",
            new ObjectMapping(
                41439,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_2}",
                0)),
        Map.entry(
            "ORACLE CHEST 3",
            new ObjectMapping(
                41440,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_3}",
                0)),
        Map.entry(
            "ORACLE CHEST 4",
            new ObjectMapping(
                41441,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_4}",
                0)),
        Map.entry(
            "ORACLE CHEST 5",
            new ObjectMapping(
                41442,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_5}",
                0)),
        Map.entry(
            "ORACLE CHEST 6",
            new ObjectMapping(
                41443,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_6}",
                0)),
        Map.entry(
            "ORACLE CHEST 7",
            new ObjectMapping(
                41444,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_7}",
                0)),
        Map.entry(
            "ORACLE CHEST 8",
            new ObjectMapping(
                41445,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_8}",
                0)),
        Map.entry(
            "ORACLE CHEST 9",
            new ObjectMapping(
                41446,
                "Misc 2 - All 1",
                false,
                false,
                "",
                "",
                false,
                "${object.oracle_chest_9}",
                0)),
        Map.entry(
            "ORACLE DOOR A",
            new ObjectMapping(
                41430,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_a}",
                0)),
        Map.entry(
            "ORACLE DOOR B",
            new ObjectMapping(
                41431,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_b}",
                0)),
        Map.entry(
            "ORACLE DOOR C",
            new ObjectMapping(
                41432,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_c}",
                0)),
        Map.entry(
            "ORACLE DOOR D",
            new ObjectMapping(
                41433,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_d}",
                0)),
        Map.entry(
            "ORACLE DOOR E",
            new ObjectMapping(
                41434,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_e}",
                0)),
        Map.entry(
            "ORACLE DOOR F",
            new ObjectMapping(
                41435,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_f}",
                0)),
        Map.entry(
            "ORACLE DOOR G",
            new ObjectMapping(
                41436,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_g}",
                0)),
        Map.entry(
            "ORACLE DOOR H",
            new ObjectMapping(
                41437,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_h}",
                0)),
        Map.entry(
            "ORACLE DOOR H2",
            new ObjectMapping(
                41449,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_h2}",
                0)),
        Map.entry(
            "ORACLE DOOR I1",
            new ObjectMapping(
                41491,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_i1}",
                0)),
        Map.entry(
            "ORACLE DOOR I2",
            new ObjectMapping(
                41493,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_i2}",
                0)),
        Map.entry(
            "ORACLE DOOR J",
            new ObjectMapping(
                41505,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_j}",
                0)),
        Map.entry(
            "ORACLE DOOR K",
            new ObjectMapping(
                41506,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_k}",
                0)),
        Map.entry(
            "ORACLE DOOR L",
            new ObjectMapping(
                41507,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_l}",
                0)),
        Map.entry(
            "ORACLE DOOR M",
            new ObjectMapping(
                41508,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_m}",
                0)),
        Map.entry(
            "ORACLE DOOR N",
            new ObjectMapping(
                41509,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_n}",
                0)),
        Map.entry(
            "ORACLE DOOR O",
            new ObjectMapping(
                41520,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.oracle_door_o}",
                0)),
        Map.entry(
            "ORACLE SIGN 1",
            new ObjectMapping(
                41497, "Sign1", false, false, "", "", false, "${object.oracle_sign_1}", 0)),
        Map.entry(
            "ORACLE SIGN 2",
            new ObjectMapping(
                41498, "Sign1", false, false, "", "", false, "${object.oracle_sign_2}", 0)),
        Map.entry(
            "ORACLE SIGN 3",
            new ObjectMapping(
                41499, "Sign1", false, false, "", "", false, "${object.oracle_sign_3}", 0)),
        Map.entry(
            "ORACLE SIGN 4",
            new ObjectMapping(
                41500, "Sign1", false, false, "", "", false, "${object.oracle_sign_4}", 0)),
        Map.entry(
            "ORACLE SIGN 5",
            new ObjectMapping(
                41501, "Sign1", false, false, "", "", false, "${object.oracle_sign_5}", 0)),
        Map.entry(
            "ORACLE SIGN 6",
            new ObjectMapping(
                41502, "Sign1", false, false, "", "", false, "${object.oracle_sign_6}", 0)),
        Map.entry(
            "ORDINARY CHEST",
            new ObjectMapping(
                41479, "Chest", false, false, "", "", false, "${object.ordinary_chest}", 0)),
        Map.entry(
            "PACK_OF_PASTRIES",
            new ObjectMapping(
                0, "Misc 6 - All 7", false, false, "", "", false, "${object.pack_of_pastries}", 0)),
        Map.entry(
            "PURPLE_POTION",
            new ObjectMapping(
                0, "Misc 9 - All 6", false, false, "", "", false, "${object.purple_potion}", 0)),
        Map.entry(
            "RAVEN CHEST 10",
            new ObjectMapping(
                40284, "Chest", false, false, "", "", false, "${object.raven_chest_10}", 0)),
        Map.entry(
            "RAVEN CHEST 4",
            new ObjectMapping(
                40278, "Chest", false, false, "", "", false, "${object.raven_chest_4}", 0)),
        Map.entry(
            "RAVEN CHEST 5",
            new ObjectMapping(
                40279, "Chest", false, false, "", "", false, "${object.raven_chest_5}", 0)),
        Map.entry(
            "RAVEN CHEST 6",
            new ObjectMapping(
                40280, "Chest", false, false, "", "", false, "${object.raven_chest_6}", 0)),
        Map.entry(
            "RAVEN CHEST 7",
            new ObjectMapping(
                40281, "Chest", false, false, "", "", false, "${object.raven_chest_7}", 0)),
        Map.entry(
            "RAVEN CHEST 8",
            new ObjectMapping(
                40282, "Chest", false, false, "", "", false, "${object.raven_chest_8}", 0)),
        Map.entry(
            "RAVEN CHEST 9",
            new ObjectMapping(
                40283, "Chest", false, false, "", "", false, "${object.raven_chest_9}", 0)),
        Map.entry(
            "REYNEN CHEST",
            new ObjectMapping(
                40274, "Chest", false, false, "", "", false, "${object.reynen_chest}", 0)),
        Map.entry(
            "ROAD 1",
            new ObjectMapping(40955, "Sign1", false, false, "", "", false, "${object.road_1}", 0)),
        Map.entry(
            "ROAD 10",
            new ObjectMapping(40964, "Sign2", false, false, "", "", false, "${object.road_10}", 0)),
        Map.entry(
            "ROAD 11",
            new ObjectMapping(40965, "Sign3", false, false, "", "", false, "${object.road_11}", 0)),
        Map.entry(
            "ROAD 12",
            new ObjectMapping(40966, "Sign3", false, false, "", "", false, "${object.road_12}", 0)),
        Map.entry(
            "ROAD 13",
            new ObjectMapping(40967, "Sign2", false, false, "", "", false, "${object.road_13}", 0)),
        Map.entry(
            "ROAD 15",
            new ObjectMapping(40969, "Sign1", false, false, "", "", false, "${object.road_15}", 0)),
        Map.entry(
            "ROAD 16",
            new ObjectMapping(40970, "Sign1", false, false, "", "", false, "${object.road_16}", 0)),
        Map.entry(
            "ROAD 17",
            new ObjectMapping(40971, "Sign1", false, false, "", "", false, "${object.road_17}", 0)),
        Map.entry(
            "ROAD 18",
            new ObjectMapping(40972, "Sign3", false, false, "", "", false, "${object.road_18}", 0)),
        Map.entry(
            "ROAD 19",
            new ObjectMapping(40973, "Sign2", false, false, "", "", false, "${object.road_19}", 0)),
        Map.entry(
            "ROAD 2",
            new ObjectMapping(40956, "Sign2", false, false, "", "", false, "${object.road_2}", 0)),
        Map.entry(
            "ROAD 20",
            new ObjectMapping(40974, "Sign1", false, false, "", "", false, "${object.road_20}", 0)),
        Map.entry(
            "ROAD 21",
            new ObjectMapping(40975, "Sign2", false, false, "", "", false, "${object.road_21}", 0)),
        Map.entry(
            "ROAD 3",
            new ObjectMapping(40957, "Sign3", false, false, "", "", false, "${object.road_3}", 0)),
        Map.entry(
            "ROAD 4",
            new ObjectMapping(40958, "Sign1", false, false, "", "", false, "${object.road_4}", 0)),
        Map.entry(
            "ROAD 5",
            new ObjectMapping(40959, "Sign1", false, false, "", "", false, "${object.road_5}", 0)),
        Map.entry(
            "ROAD 6",
            new ObjectMapping(40960, "Sign1", false, false, "", "", false, "${object.road_6}", 0)),
        Map.entry(
            "ROAD 7",
            new ObjectMapping(40961, "Sign1", false, false, "", "", false, "${object.road_7}", 0)),
        Map.entry(
            "ROAD 8",
            new ObjectMapping(40962, "Sign2", false, false, "", "", false, "${object.road_8}", 0)),
        Map.entry(
            "ROAD 9",
            new ObjectMapping(40963, "Sign2", false, false, "", "", false, "${object.road_9}", 0)),
        Map.entry(
            "ROULEAU_A_PATE",
            new ObjectMapping(
                0, "Kitchen 6", false, false, "", "", false, "${object.rouleau_a_pate}", 0)),
        Map.entry(
            "RUSTED CHEST",
            new ObjectMapping(
                41480, "Chest", false, false, "", "", false, "${object.rusted_chest}", 0)),
        Map.entry(
            "SIGN 1",
            new ObjectMapping(
                40934, "64kArmorShopSign-a", false, false, "", "", false, "${object.sign_1}", 1)),
        Map.entry(
            "SIGN 10",
            new ObjectMapping(
                40943, "64kPawnShopSign-a", false, false, "", "", false, "${object.sign_10}", 1)),
        Map.entry(
            "SIGN 11",
            new ObjectMapping(
                40944, "64kArmorShopSign-a", false, true, "", "", false, "${object.sign_11}", 1)),
        Map.entry(
            "SIGN 12",
            new ObjectMapping(
                40945, "64kWeaponShopSign-a", false, true, "", "", false, "${object.sign_12}", 1)),
        Map.entry(
            "SIGN 13",
            new ObjectMapping(
                40946, "64kInnShopSign-a", false, true, "", "", false, "${object.sign_13}", 1)),
        Map.entry(
            "SIGN 14",
            new ObjectMapping(
                40947, "64kPotionShopSign-a", false, true, "", "", false, "${object.sign_14}", 1)),
        Map.entry(
            "SIGN 15",
            new ObjectMapping(
                40948, "64kWeaponShopSign-a", false, false, "", "", false, "${object.sign_15}", 1)),
        Map.entry(
            "SIGN 16",
            new ObjectMapping(
                40949, "64kInnShopSign-a", false, true, "", "", false, "${object.sign_16}", 1)),
        Map.entry(
            "SIGN 17",
            new ObjectMapping(
                40950, "64kPawnShopSign-a", false, false, "", "", false, "${object.sign_17}", 1)),
        Map.entry(
            "SIGN 18",
            new ObjectMapping(
                40951, "64kPawnShopSign-a", false, false, "", "", false, "${object.sign_18}", 1)),
        Map.entry(
            "SIGN 19",
            new ObjectMapping(
                40952, "64kWeaponShopSign-a", false, false, "", "", false, "${object.sign_19}", 1)),
        Map.entry(
            "SIGN 20",
            new ObjectMapping(
                40953, "64kArmorShopSign-a", false, true, "", "", false, "${object.sign_20}", 1)),
        Map.entry(
            "SIGN 21",
            new ObjectMapping(
                40954, "64kWeaponShopSign-a", false, false, "", "", false, "${object.sign_21}", 1)),
        Map.entry(
            "SIGN 3",
            new ObjectMapping(
                40936, "64kWeaponShopSign-a", false, false, "", "", false, "${object.sign_3}", 1)),
        Map.entry(
            "SIGN 4",
            new ObjectMapping(
                40937, "64kArmorShopSign-a", false, true, "", "", false, "${object.sign_4}", 1)),
        Map.entry(
            "SIGN 5",
            new ObjectMapping(
                40938, "64kArmorShopSign-a", false, false, "", "", false, "${object.sign_5}", 1)),
        Map.entry(
            "SIGN 6",
            new ObjectMapping(
                40939, "64kInnShopSign-a", false, false, "", "", false, "${object.sign_6}", 1)),
        Map.entry(
            "SIGN 7",
            new ObjectMapping(
                40940, "64kWeaponShopSign-a", false, true, "", "", false, "${object.sign_7}", 1)),
        Map.entry(
            "SIGN 9",
            new ObjectMapping(
                40942, "64kInnShopSign-a", false, false, "", "", false, "${object.sign_9}", 1)),
        Map.entry(
            "SILVER CHEST",
            new ObjectMapping(
                41481, "Chest", false, false, "", "", false, "${object.silver_chest}", 0)),
        Map.entry(
            "SKRAUG CORPSE 1",
            new ObjectMapping(
                41455,
                "64kSkavenPeonC-t",
                false,
                false,
                "",
                "",
                false,
                "${object.skraug_corpse_1}",
                0)),
        Map.entry(
            "SKRAUG CORPSE 2",
            new ObjectMapping(
                41456,
                "64kSkavenSkavengerC-s",
                false,
                false,
                "",
                "",
                false,
                "${object.skraug_corpse_2}",
                0)),
        Map.entry(
            "SKRAUG CORPSE 3",
            new ObjectMapping(
                41457,
                "64kSkavenShamanC-s",
                false,
                false,
                "",
                "",
                false,
                "${object.skraug_corpse_3}",
                0)),
        Map.entry(
            "SKRAUG CORPSE 4",
            new ObjectMapping(
                41458,
                "64kSkavenWarriorC-s",
                false,
                false,
                "",
                "",
                false,
                "${object.skraug_corpse_4}",
                0)),
        Map.entry(
            "STEEL SAFE",
            new ObjectMapping(
                41483, "Vault", false, false, "", "", false, "${object.steel_safe}", 0)),
        Map.entry(
            "STONECREST CRYPT CHEST 1",
            new ObjectMapping(
                41354,
                "Chest",
                false,
                false,
                "",
                "",
                false,
                "${object.stonecrest_crypt_chest_1}",
                0)),
        Map.entry(
            "STONECREST CRYPT CHEST 2",
            new ObjectMapping(
                41355,
                "Chest",
                false,
                false,
                "",
                "",
                false,
                "${object.stonecrest_crypt_chest_2}",
                0)),
        Map.entry(
            "STONECREST CRYPT CHEST 3",
            new ObjectMapping(
                41356,
                "Chest",
                false,
                false,
                "",
                "",
                false,
                "${object.stonecrest_crypt_chest_3}",
                0)),
        Map.entry(
            "TAMBOUR",
            new ObjectMapping(
                0, "Misc 7 - All 10", false, false, "", "", false, "${object.tambour}", 0)),
        Map.entry(
            "THEODORE CHEST 1",
            new ObjectMapping(
                40240, "Chest", false, false, "", "", false, "${object.theodore_chest_1}", 0)),
        Map.entry(
            "THEODORE CHEST 2",
            new ObjectMapping(
                40241, "Chest", false, false, "", "", false, "${object.theodore_chest_2}", 0)),
        Map.entry(
            "THEODORE CHEST 3",
            new ObjectMapping(
                40242, "Chest", false, false, "", "", false, "${object.theodore_chest_3}", 0)),
        Map.entry(
            "WASP ZONE CHEST 1",
            new ObjectMapping(
                41156, "Chest", false, false, "", "", false, "${object.wasp_zone_chest_1}", 0)),
        Map.entry(
            "WASP ZONE CHEST 2",
            new ObjectMapping(
                41157, "Chest", false, false, "", "", false, "${object.wasp_zone_chest_2}", 0)),
        Map.entry(
            "WASP ZONE CHEST 3",
            new ObjectMapping(
                41158, "Chest", false, false, "", "", false, "${object.wasp_zone_chest_3}", 0)),
        Map.entry(
            "WASP ZONE CHEST 4",
            new ObjectMapping(
                41159, "Chest", false, false, "", "", false, "${object.wasp_zone_chest_4}", 0)),
        Map.entry(
            "WOOD_PACK_CLASSED",
            new ObjectMapping(
                0,
                "Misc 4 - Part 2 3",
                false,
                false,
                "",
                "",
                false,
                "${object.wood_pack_classed}",
                0)),
        Map.entry(
            "WOOD_PACK_MIXED",
            new ObjectMapping(
                0,
                "Misc 4 - Part 2 4",
                false,
                false,
                "",
                "",
                false,
                "${object.wood_pack_mixed}",
                0)),
        Map.entry(
            "WOODEN DOOR",
            new ObjectMapping(
                40842,
                "RockDoor%d$11",
                true,
                false,
                "Open Wooden Door.wav",
                "Close Wooden Door.wav",
                false,
                "${object.wooden_door}",
                0)),
        Map.entry(
            "WOODEN_BOWL",
            new ObjectMapping(
                0, "Misc 3 - Part 1 8", false, false, "", "", false, "${object.wooden_bowl}", 0)),
        Map.entry(
            "WOODEN_CHAIR_2",
            new ObjectMapping(
                0, "Chairs 4", false, false, "", "", false, "${object.wooden_chair_2}", 0)),
        Map.entry(
            "WOODEN_CHAIR_2_FLIP",
            new ObjectMapping(
                0, "Chairs 4", false, true, "", "", false, "${object.wooden_chair_2_flip}", 0)),
        Map.entry(
            "WOODEN_CHAIR_2_I_REV",
            new ObjectMapping(
                0, "Shito 4", false, true, "", "", false, "${object.wooden_chair_2_i_rev}", 0)),
        Map.entry(
            "WOODEN_CHAIR_2_REV",
            new ObjectMapping(
                0, "Shito 4", false, false, "", "", false, "${object.wooden_chair_2_rev}", 0)),
        Map.entry(
            "WOODEN_CHAIR_FLIP",
            new ObjectMapping(
                0, "Chairs 3", false, true, "", "", false, "${object.wooden_chair_flip}", 0)),
        Map.entry(
            "WOODEN_CHAIR_I_REV",
            new ObjectMapping(
                0, "Shito 3", false, true, "", "", false, "${object.wooden_chair_i_rev}", 0)),
        Map.entry(
            "WOODEN_CHAIR_REV",
            new ObjectMapping(
                0, "Shito 3", false, false, "", "", false, "${object.wooden_chair_rev}", 0)),
        Map.entry(
            "WOODEN_CUP",
            new ObjectMapping(
                0, "Misc 1 - Part 2 7", false, false, "", "", false, "${object.wooden_cup}", 0)),
        Map.entry(
            "WOODEN_ROUND_CHAIR",
            new ObjectMapping(
                0, "Chairs 2", false, false, "", "", false, "${object.wooden_round_chair}", 0)),
        Map.entry(
            "WOODEN_ROUND_CHAIR_2",
            new ObjectMapping(
                0, "Chairs 5", false, false, "", "", false, "${object.wooden_round_chair_2}", 0)),
        Map.entry(
            "WOODEN_ROUND_CHAIR_2_FLIP",
            new ObjectMapping(
                0,
                "Chairs 5",
                false,
                true,
                "",
                "",
                false,
                "${object.wooden_round_chair_2_flip}",
                0)),
        Map.entry(
            "WOODEN_ROUND_CHAIR_2_I_REV",
            new ObjectMapping(
                0,
                "Shito 5",
                false,
                true,
                "",
                "",
                false,
                "${object.wooden_round_chair_2_i_rev}",
                0)),
        Map.entry(
            "WOODEN_ROUND_CHAIR_2_REV",
            new ObjectMapping(
                0,
                "Shito 5",
                false,
                false,
                "",
                "",
                false,
                "${object.wooden_round_chair_2_rev}",
                0)),
        Map.entry(
            "WOODEN_VASE",
            new ObjectMapping(
                0, "Misc 3 - Part 1 9", false, false, "", "", false, "${object.wooden_vase}", 0)));
  }
}
