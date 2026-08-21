package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Grott", x = 1058, y = 2431, z = 0, stationary = false, aggressive = false)
public final class Grott extends DataMonster {
  public static final String SOUND_ATTACK = "Troll Attack.wav";
  public static final String SOUND_DEATH = "Troll Dying.wav";
  public static final String SOUND_HIT = "Troll Hit.wav";

  public static final String CANONICAL_NAME = "Grott";

  public Grott(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Grott",
        "${monster.grott}",
        1694,
        0,
        4,
        4174,
        40,
        95,
        30000L,
        "GreenTroll#f",
        "GreenTrollA#g",
        "GreenTrollC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        142,
        440,
        java.util.List.of(
            new MonsterDef.LootDrop("Shaman mantle", 0.07f),
            new MonsterDef.LootDrop("Jade ring of sorcery", 0.05f),
            new MonsterDef.LootDrop("Mana elixir", 0.04f),
            new MonsterDef.LootDrop("Manastone", 0.03f),
            new MonsterDef.LootDrop("Serious healing potion", 0.04f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.03f)),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {75, 75, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        40,
        200,
        0,
        1076756480,
        20010,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d56+39", 490, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10120, 3, 13),
            new MonsterDef.Attack("", 0, 40, 10086, 3, 13),
            new MonsterDef.Attack("", 0, 15, 10122, 3, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
