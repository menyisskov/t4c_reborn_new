package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Centaur King", x = 2380, y = 2430, z = 0, stationary = false, aggressive = true)
public final class CentaurKing extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Taunting Hit.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public static final String CANONICAL_NAME = "Centaur King";

  public CentaurKing(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Centaur King",
        "${monster.centaur_king}",
        14100,
        0,
        0,
        70000,
        180,
        320,
        30000L,
        "64kCentaurKing#i",
        "64kCentaurKingA#i",
        "64kCentaurKingC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        350,
        900,
        java.util.List.of(
            new MonsterDef.LootDrop("marchwardens_crown", 0.01f),
            new MonsterDef.LootDrop("bow_of_centaur_slaying", 0.015f),
            // T4C-0021: pre-existing item from the original content pipeline (T4C-0001) that
            // never got a source - fits this boss's existing archer theme.
            new MonsterDef.LootDrop("ring_of_the_archer", 0.02f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f),
            // T4C-0021: physical/archer-flavor source for the Ancient Celestial/Empyrean armor
            // sets (ArmorSetGenerator) - previously generated with zero acquisition path.
            new MonsterDef.LootDrop("ancient_celestial_archer_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_archer_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_protector", 0.012f)),
        false,
        0.0f,
        180,
        165,
        140,
        200,
        60,
        165,
        0,
        new int[] {100, 140, 100, 90, 110, 100, 100, 100, 100, 100, 100, 100},
        140,
        570,
        0,
        140,
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
        java.util.List.of(new MonsterDef.Attack("1d242+187", 2200, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("CentaurKing"),
        java.util.Map.of());
  }
}
