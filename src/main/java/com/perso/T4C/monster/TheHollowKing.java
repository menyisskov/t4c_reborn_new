package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "The Hollow King", x = 2500, y = 2700, z = 0, stationary = false, aggressive = true)
public final class TheHollowKing extends DataMonster {
  // Reuses the "Black Warrior" puppet family — a tall, armored fallen-knight silhouette,
  // distinct from the "Skeleton King" rig already used by Mordrenn.
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "The Hollow King";

  public TheHollowKing(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "The Hollow King",
        "${monster.the_hollow_king}",
        49650,
        0,
        0,
        180000,
        280,
        480,
        30000L,
        "BlackWarrior#m",
        "BlackWarriorA#l",
        "BlackWarriorC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        500,
        1400,
        java.util.List.of(
            new MonsterDef.LootDrop("crown_of_the_hollow_king", 0.008f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f),
            // T4C-0028: an undead king guarding a hollow, deathly barrow drops a dark-flavor
            // armor set alongside his signature crown, matching the multi-drop pattern used by
            // the other boss-tier monsters.
            new MonsterDef.LootDrop("ancient_celestial_dark_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_dark_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_protector", 0.012f)),
        false,
        0.0f,
        240,
        210,
        160,
        170,
        100,
        170,
        0,
        new int[] {100, 100, 100, 110, 240, 15, 100, 100, 100, 100, 100, 100},
        195,
        640,
        0,
        180,
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
        java.util.List.of(new MonsterDef.Attack("1d420+340", 3000, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("HollowKing", "TheHollowKing"),
        java.util.Map.of());
  }
}
