package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class Battlebard extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Battle bard";

  public Battlebard(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Battle bard",
        "${monster.battle_bard}",
        396,
        0,
        3,
        641,
        19,
        43,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        39,
        121,
        java.util.List.of(
            new MonsterDef.LootDrop("Rusted long sword", 0.01f),
            new MonsterDef.LootDrop("Leather armor", 0.01f),
            new MonsterDef.LootDrop("Leather boots", 0.01f),
            new MonsterDef.LootDrop("Leather pants", 0.01f)),
        false,
        0.0f,
        37,
        34,
        34,
        41,
        0,
        34,
        0,
        new int[] {83, 83, 83, 83, 111, 5000, 100, 100, 100, 100, 100, 100},
        22,
        98,
        0,
        1076232192,
        10011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        46,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d25+18", 274, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
