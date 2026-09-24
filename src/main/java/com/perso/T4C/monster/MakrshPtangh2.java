package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;

public final class MakrshPtangh2 extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 3.wav";
  public static final String SOUND_DEATH = "Vampire Dying.wav";
  public static final String SOUND_HIT = "Vampire Hit.wav";

  public MakrshPtangh2(MonsterDef definition, float worldX, float worldY) throws GameException {
    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MakrshPtangh2",
        "${monster.makrshptangh2}",
        52500,
        0,
        50,
        200000,
        1,
        10,
        30000L,
        "64kLich#l",
        "64kLichA#j",
        "64kLichC!w",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        400,
        1500,
        java.util.List.of(
            new MonsterDef.LootDrop("ptanghs_boneshard_longbow", 0.01f),
            new MonsterDef.LootDrop("ptanghs_lichbone_staff", 0.01f),
            new MonsterDef.LootDrop("item.wyrmforged_ember", 0.05f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f),
            new MonsterDef.LootDrop("item.black_locust_composite_bow_3", 0.03f),
            new MonsterDef.LootDrop("item.ancient_two_handed_sword_3", 0.01f),
            new MonsterDef.LootDrop("item.ancient_broad_axe_3", 0.01f),
            new MonsterDef.LootDrop("item.ancient_morningstar_3", 0.01f),
            new MonsterDef.LootDrop("item.shield_of_the_ages", 0.02f),
            new MonsterDef.LootDrop("item.ancient_platemail_armor", 0.01f),
            new MonsterDef.LootDrop("item.ancient_plate_protector", 0.04f),
            new MonsterDef.LootDrop("item.ancient_platemail_gauntlets", 0.05f),
            new MonsterDef.LootDrop("item.ancient_platemail_helmet", 0.02f),
            new MonsterDef.LootDrop("item.ancient_platemail_leggings", 0.04f),
            new MonsterDef.LootDrop("item.ancient_platemail_boots", 0.05f)),
        false,
        0.0f,
        215,
        194,
        194,
        255,
        200,
        200,
        55,
        new int[] {5000, 5000, 5000, 5000, 5000, 5000, 100, 100, 100, 100, 100, 100},
        200,
        890,
        0,
        100,
        20058,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d390+360", 2410, 40, 0, 0, 0),
            new MonsterDef.Attack("boulders", 0, 15, 10708, 0, 25),
            new MonsterDef.Attack("glacier", 0, 15, 10709, 0, 25),
            new MonsterDef.Attack("meteor", 0, 15, 10710, 0, 25),
            new MonsterDef.Attack("teleport", 0, 8, 10596, 0, 25),
            new MonsterDef.Attack("regeneration", 0, 5, 10618, 0, 25)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
