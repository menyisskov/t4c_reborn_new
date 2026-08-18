package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r278YoggothWorm extends DataMonster {
  public static final String SOUND_ATTACK = "Worm Attack.wav";
  public static final String SOUND_DEATH = "Worm Dying.wav";
  public static final String SOUND_HIT = "Worm Hit.wav";

  public r278YoggothWorm(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Yoggoth Worm",
        "${monster.yoggoth_worm}",
        1497,
        0,
        6,
        4869,
        0,
        0,
        30000L,
        "SmallWorm#m",
        "SmallWormA#k",
        "SmallWormC#k",
        "Worm Attack.wav",
        "Worm Dying.wav",
        "Worm Hit.wav",
        107,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Finely cut amethyst", 0.01f),
            new MonsterDef.LootDrop("Finely cut sapphire", 0.002f),
            new MonsterDef.LootDrop("Finely cut moonstone", 5.0E-4f)),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        0,
        68,
        0,
        new int[] {78, 78, 52, 105, 78, 5000, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1077805056,
        20016,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        41,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 97, 10091, 3, 10),
            new MonsterDef.Attack("1d90+69", 730, 100, 10091, 0, 2),
            new MonsterDef.Attack("", 0, 3, 10317, 3, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
