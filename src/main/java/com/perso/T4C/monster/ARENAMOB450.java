package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ARENAMOB450 extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public ARENAMOB450(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ArenaMob450",
        "${monster.arenamob450}",
        45097,
        0,
        0,
        0,
        1,
        4,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        465,
        419,
        419,
        555,
        419,
        419,
        105,
        new int[] {70, 70, 70, 70, 70, 5000, 100, 100, 100, 100, 100, 100},
        450,
        1810,
        0,
        1130430464,
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
        java.util.List.of(new MonsterDef.Attack("1d 772 + 608 ", 5410, 20, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.ofEntries(
            java.util.Map.entry(
                "OnPopup",
                "																	 \r\n  	CastSpellSelf(\"spell.mob_arena_major_regeneration_spell\") 	\r\n	SimpleMonster::OnPopup( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "OnDeath",
                "\r\n    INIT_HANDLER\r\n	if( target != NULL )\r\n	{\r\n		GiveItem(41702)\r\n		PRIVATE_SYSTEM_MESSAGE(INTL( 10682, \"You receive a battle token for your efforts.\"))\r\n	}\r\n    CLOSE_HANDLER\r\n\r\n	CastSpellSelf(\"spell.mob_arena_level_spell\")\r\n\r\n	SimpleMonster::OnDeath( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "OnDestroy",
                "\r\n	IF(CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) > 0)\r\n		GiveGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA, CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) - 1)\r\n	ENDIF\r\n\r\n	SimpleMonster::OnDestroy( UNIT_FUNC_PARAM );\r\n"),
            java.util.Map.entry(
                "@spell.spell.mob_arena_level_spell", "GiveFlag(__FLAG_ARENA_LEVEL,450)")));
  }
}
