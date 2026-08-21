package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Atrocity", x = 132, y = 497, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 134, y = 597, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 138, y = 576, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 186, y = 536, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 186, y = 694, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 193, y = 701, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 195, y = 484, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 201, y = 423, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 207, y = 659, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 215, y = 413, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 239, y = 450, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 248, y = 717, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 261, y = 583, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 280, y = 604, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 283, y = 731, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 284, y = 557, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 285, y = 485, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 290, y = 557, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 293, y = 631, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 294, y = 443, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 298, y = 706, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 299, y = 749, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 301, y = 290, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 303, y = 353, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 310, y = 650, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 318, y = 294, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 324, y = 407, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 324, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 338, y = 328, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 351, y = 574, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 356, y = 315, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 359, y = 451, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 362, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 370, y = 582, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 381, y = 505, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 391, y = 246, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 392, y = 284, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 396, y = 453, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 408, y = 593, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 409, y = 559, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 423, y = 314, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 425, y = 335, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 432, y = 408, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 438, y = 260, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 453, y = 585, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 457, y = 603, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 484, y = 287, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 485, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 486, y = 433, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 500, y = 307, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 506, y = 197, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 508, y = 512, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 517, y = 218, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 523, y = 469, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 535, y = 222, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 564, y = 471, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 571, y = 558, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 587, y = 570, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 596, y = 468, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Atrocity", x = 625, y = 530, z = 2, stationary = false, aggressive = true)
public final class Atrocity extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Atrocity";

  public Atrocity(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Atrocity",
        "${monster.atrocity}",
        84,
        0,
        1,
        77,
        5,
        12,
        30000L,
        "Atrocity#h",
        "AtrocityA#h",
        "AtrocityC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        8,
        27,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.03f),
            new MonsterDef.LootDrop("Light healing potion", 0.05f),
            new MonsterDef.LootDrop("Iron key", 0.005f)),
        false,
        0.0f,
        20,
        20,
        20,
        20,
        0,
        18,
        0,
        new int[] {91, 91, 60, 121, 91, 5000, 100, 100, 100, 100, 100, 100},
        5,
        30,
        0,
        1073741824,
        20026,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        55,
        10,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d8+4", 70, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
