package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ARENAMOBXP400 extends DataMonster {
  public static final String SOUND_ATTACK = "Taunting Attack.wav";
  public static final String SOUND_DEATH = "Taunting Dying.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public ARENAMOBXP400(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ArenaMobXP400",
        "${monster.arenamobxp400}",
        36217,
        0,
        0,
        6396077,
        1,
        4,
        30000L,
        "Taunting#h",
        "TauntingA#h",
        "TauntingC#m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        415,
        374,
        374,
        495,
        374,
        374,
        95,
        new int[] {46, 94, 70, 70, 70, 5000, 100, 100, 100, 100, 100, 100},
        400,
        1610,
        0,
        1128792064,
        20038,
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
        java.util.List.of(new MonsterDef.Attack("1d 687 + 540 ", 4810, 20, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.ofEntries(
            java.util.Map.entry(
                "OnPopup",
                "																	 \r\n  	CastSpellSelf(\"spell.mob_arena_major_regeneration_spell\") 	\r\n	SimpleMonster::OnPopup( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "OnDeath",
                "\r\n    INIT_HANDLER\r\n	if( target != NULL )\r\n	{\r\n		IF(rnd.roll(dice(1, 100)) <= (390 - (USER_LEVEL)))\r\n			GiveItem(41702)\r\n			PRIVATE_SYSTEM_MESSAGE(INTL( 10682, \"You receive a battle token for your efforts.\"))\r\n		ENDIF\r\n	}\r\n    CLOSE_HANDLER\r\n\r\n	CastSpellSelf(\"spell.mob_arena_level_spell\")\r\n\r\n	SimpleMonster::OnDeath( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "OnDestroy",
                "\r\n	IF(CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) > 0)\r\n		GiveGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA, CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) - 1)\r\n	ENDIF\r\n\r\n	SimpleMonster::OnDestroy( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "@spell.spell.mob_arena_level_spell", "GiveFlag(__FLAG_ARENA_LEVEL,400)")));
  }
}
