package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Sole guardian of the hidden shoal path to Avalon (1560,1290, worldZ 0), within the same coastal
// strip TidewornReaver's warband holds — see quest/definition/PassageToAvalon.java. Reuses the
// "MonsDraconianPlate" armored-knight puppet family (Sir Caradoc precedent) for a lone
// knight-errant bound to the crossing, distinct from Caradoc's own undead-bound version of it.
@Spawn(type = "Coastwarden Ithrak", x = 1560, y = 1290, z = 0, stationary = false, aggressive = true)
public final class CoastwardenIthrak extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Coastwarden Ithrak";

  public CoastwardenIthrak(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Coastwarden Ithrak",
        "${monster.coastwarden_ithrak}",
        40000,
        0,
        6,
        20000000,
        450,
        1000,
        30000L,
        "MonsDraconianPlate#k",
        "MonsDraconianPlateA#k",
        "MonsDraconianPlateC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        700,
        1800,
        java.util.List.of(
            new MonsterDef.LootDrop("tideworn_avalon_chart", 0.02f),
            new MonsterDef.LootDrop("healing_potion", 0.2f)),
        false,
        0.0f,
        360,
        340,
        220,
        160,
        0,
        160,
        0,
        new int[] {90, 130, 100, 95, 180, 25, 100, 100, 100, 100, 100, 100},
        300,
        1300,
        0,
        320,
        0,
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
        java.util.List.of(new MonsterDef.Attack("1d500+430", 4000, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("CoastwardenIthrak"),
        java.util.Map.of());
  }
}
