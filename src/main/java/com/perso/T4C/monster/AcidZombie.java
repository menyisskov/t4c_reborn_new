package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class AcidZombie extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String CANONICAL_NAME = "Acid Zombie";

  public AcidZombie(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Acid Zombie",
        "${monster.acid_zombie}",
        1497,
        0,
        6,
        4869,
        0,
        0,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        107,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Serious healing potion", 0.03f),
            new MonsterDef.LootDrop("High metal long sword", 0.01f),
            new MonsterDef.LootDrop("Potion of fury", 0.02f),
            new MonsterDef.LootDrop("Potion of clear thought", 0.02f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Finely cut carnelian", 0.01f),
            new MonsterDef.LootDrop("Finely cut ruby", 0.002f),
            new MonsterDef.LootDrop("Finely cut garnet", 5.0E-4f)),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        0,
        68,
        0,
        new int[] {78, 78, 105, 52, 5025, 65, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1077805056,
        20009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        34,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 40, 10091, 2, 12),
            new MonsterDef.Attack("1d90+69", 730, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10119, 0, 12),
            new MonsterDef.Attack("", 0, 5, 10320, 2, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
