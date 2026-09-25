package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Drowned Acolyte", x = 1710, y = 2270, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drowned Acolyte", x = 1760, y = 2330, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drowned Acolyte", x = 1790, y = 2280, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drowned Acolyte", x = 1700, y = 2340, z = 0, stationary = false, aggressive = true)
public final class DrownedAcolyte extends DataMonster {
  // Reuses the Zombie animation/sound family — no dedicated "drowned cultist" sprite
  // exists yet; the theme is carried by name/stats/resists instead (see item-creator
  // skill's color/graphics-matching guidance for the same tradeoff on items).
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String CANONICAL_NAME = "Drowned Acolyte";

  public DrownedAcolyte(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Drowned Acolyte",
        "${monster.drowned_acolyte}",
        1050,
        0,
        5,
        4200,
        35,
        65,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC!j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        60,
        200,
        java.util.List.of(
            new MonsterDef.LootDrop("healing_potion", 0.04f),
            // Rare trash-mob source for the zone's Drowned Inquisition set (Mordrenn is the main
            // one).
            new MonsterDef.LootDrop("drowned_inquisition_armor", 0.004f),
            new MonsterDef.LootDrop("drowned_inquisition_boots", 0.004f),
            new MonsterDef.LootDrop("drowned_inquisition_gauntlets", 0.004f),
            new MonsterDef.LootDrop("drowned_inquisition_helmet", 0.004f),
            new MonsterDef.LootDrop("drowned_inquisition_leggings", 0.004f),
            new MonsterDef.LootDrop("drowned_inquisition_protector", 0.004f)),
        false,
        0.0f,
        50,
        46,
        46,
        60,
        0,
        48,
        0,
        new int[] {100, 95, 135, 85, 160, 35, 100, 100, 100, 100, 100, 100},
        38,
        165,
        0,
        25,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        60,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d58+42", 390, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("DrownedAcolyte"),
        java.util.Map.of());
  }
}
