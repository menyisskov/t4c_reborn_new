package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ARENAMOBXP140 extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public ARENAMOBXP140(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ArenaMobXP140",
        "${monster.arenamobxp140}",
        5641,
        0,
        0,
        45172,
        1,
        4,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC#o",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        155,
        140,
        140,
        183,
        140,
        140,
        43,
        new int[] {94, 46, 70, 70, 70, 5025, 100, 100, 100, 100, 100, 100},
        140,
        570,
        0,
        1116471296,
        20001,
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
        java.util.List.of(new MonsterDef.Attack("1d 241 + 188 ", 1690, 50, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.ofEntries(
            java.util.Map.entry(
                "OnDeath",
                "\r\n    INIT_HANDLER\r\n	if( target != NULL )\r\n	{\r\n		IF(rnd.roll(dice(1, 100)) <= (170 - (USER_LEVEL)))\r\n			GiveItem(41702)\r\n			PRIVATE_SYSTEM_MESSAGE(INTL( 10682, \"You receive a battle token for your efforts.\"))\r\n		ENDIF\r\n	}\r\n    CLOSE_HANDLER\r\n\r\n	CastSpellSelf(\"spell.mob_arena_level_spell\")\r\n\r\n	SimpleMonster::OnDeath( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "OnDestroy",
                "\r\n	IF(CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) > 0)\r\n		GiveGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA, CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) - 1)\r\n	ENDIF\r\n\r\n	SimpleMonster::OnDestroy( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "@spell.spell.mob_arena_level_spell", "GiveFlag(__FLAG_ARENA_LEVEL,140)")));
  }
}
