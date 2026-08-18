package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ARENAMOBXP60 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public ARENAMOBXP60(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ArenaMobXP60",
        "${monster.arenamobxp60}",
        1497,
        0,
        0,
        7304,
        1,
        4,
        30000L,
        "SmallWorm#m",
        "SmallWormA#k",
        "SmallWormC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        68,
        68,
        27,
        new int[] {97, 97, 64, 130, 97, 5000, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1106247680,
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
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d 90 + 69 ", 730, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.ofEntries(
            java.util.Map.entry(
                "OnDeath",
                "\r\n    INIT_HANDLER\r\n	if( target != NULL )\r\n	{\r\n		IF(rnd.roll(dice(1, 100)) <= (90 - (USER_LEVEL)))\r\n			GiveItem(41702)\r\n			PRIVATE_SYSTEM_MESSAGE(INTL( 10682, \"You receive a battle token for your efforts.\"))\r\n		ENDIF\r\n	}\r\n    CLOSE_HANDLER\r\n\r\n	CastSpellSelf(\"spell.mob_arena_level_spell\")\r\n\r\n	SimpleMonster::OnDeath( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "OnDestroy",
                "\r\n	IF(CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) > 0)\r\n		GiveGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA, CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) - 1)\r\n	ENDIF\r\n\r\n	SimpleMonster::OnDestroy( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "@spell.spell.mob_arena_level_spell", "GiveFlag(__FLAG_ARENA_LEVEL,60)")));
  }
}
