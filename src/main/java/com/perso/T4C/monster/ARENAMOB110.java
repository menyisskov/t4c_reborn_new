package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ARENAMOB110 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public ARENAMOB110(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ArenaMob110",
        "${monster.arenamob110}",
        3797,
        0,
        0,
        0,
        1,
        4,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        125,
        113,
        113,
        147,
        113,
        113,
        37,
        new int[] {70, 70, 94, 46, 5025, 70, 100, 100, 100, 100, 100, 100},
        110,
        450,
        0,
        1113325568,
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
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d 190 + 147 ", 1330, 25, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.ofEntries(
            java.util.Map.entry(
                "OnDeath",
                "\r\n	INIT_HANDLER\r\n\r\n	if( target != NULL )\r\n	{\r\n		IF(rnd.roll(dice(1, 100)) <= (140 - (USER_LEVEL)))\r\n			GiveItem(41702)\r\n			PRIVATE_SYSTEM_MESSAGE(INTL( 10682, \"You receive a battle token for your efforts.\"))\r\n		ENDIF\r\n	}\r\n	CLOSE_HANDLER\r\n\r\n	CastSpellSelf(\"spell.mob_arena_level_spell\")\r\n\r\n	SimpleMonster::OnDeath( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "OnDestroy",
                "\r\n	IF(CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) > 0)\r\n		GiveGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA, CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) - 1)\r\n	ENDIF\r\n\r\n	SimpleMonster::OnDestroy( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "@spell.spell.mob_arena_level_spell", "GiveFlag(__FLAG_ARENA_LEVEL,110)")));
  }
}
