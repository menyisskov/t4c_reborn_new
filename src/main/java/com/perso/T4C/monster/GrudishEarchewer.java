package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Grudish Earchewer", x = 864, y = 1196, z = 2, stationary = false, aggressive = true)
public final class GrudishEarchewer extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public static final String CANONICAL_NAME = "Grudish Earchewer";

  public GrudishEarchewer(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Grudish Earchewer",
        "${monster.grudish_earchewer}",
        1420,
        0,
        4,
        3150,
        34,
        78,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        124,
        384,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of fury", 0.04f),
            new MonsterDef.LootDrop("Serious healing potion", 0.03f),
            new MonsterDef.LootDrop("Polished hand axe", 0.01f)),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        0,
        46,
        0,
        new int[] {103, 51, 77, 77, 77, 5000, 100, 100, 100, 100, 100, 100},
        35,
        150,
        0,
        1076953088,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        15,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d45+33", 430, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
