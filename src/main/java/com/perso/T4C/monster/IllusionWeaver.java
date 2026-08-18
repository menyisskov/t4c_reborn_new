package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBILLUSIONWEAVER", x = 1001, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 1001, y = 784, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 1009, y = 706, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 1010, y = 598, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 1019, y = 799, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 1026, y = 804, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 1030, y = 737, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 1067, y = 784, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 795, y = 738, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 815, y = 750, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 820, y = 718, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 822, y = 566, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 860, y = 731, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 863, y = 561, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 885, y = 763, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 886, y = 604, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 893, y = 626, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 912, y = 643, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 915, y = 718, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 922, y = 706, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 940, y = 725, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 941, y = 642, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 942, y = 569, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 946, y = 550, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 947, y = 622, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 965, y = 785, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 967, y = 725, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 968, y = 702, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 972, y = 572, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 978, y = 709, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBILLUSIONWEAVER", x = 982, y = 764, z = 2, stationary = false, aggressive = true)
public final class IllusionWeaver extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public IllusionWeaver(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBILLUSIONWEAVER",
        "${monster.mobillusionweaver}",
        100,
        0,
        1,
        100,
        1,
        2,
        30000L,
        "",
        "",
        "",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        true,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
