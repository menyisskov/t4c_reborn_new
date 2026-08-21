package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Fugar", x = 640, y = 2225, z = 2, stationary = false, aggressive = true)
public final class Fugar extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Fugar";

  public Fugar(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Fugar",
        "${monster.fugar}",
        706,
        0,
        3,
        1086,
        17,
        39,
        30000L,
        "GoblinBoss#l",
        "GoblinBossA#i",
        "GoblinBossC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        70,
        220,
        java.util.List.of(
            new MonsterDef.LootDrop("Studded leather armor", 0.02f),
            new MonsterDef.LootDrop("Studded leather gloves", 0.03f),
            new MonsterDef.LootDrop("Studded leather pants", 0.03f),
            new MonsterDef.LootDrop("Studded leather helmet", 0.03f),
            new MonsterDef.LootDrop("Studded leather boots", 0.03f),
            new MonsterDef.LootDrop("Goblin Blade", 0.02f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Torch", 0.05f)),
        false,
        0.0f,
        35,
        32,
        32,
        39,
        0,
        32,
        0,
        new int[] {112, 56, 84, 84, 84, 5025, 100, 100, 100, 100, 100, 100},
        20,
        90,
        0,
        1076101120,
        20041,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d23+16", 250, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
