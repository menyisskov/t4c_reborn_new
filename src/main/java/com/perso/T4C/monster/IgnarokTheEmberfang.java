package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Ignarok", x = 1900, y = 1600, z = 0, stationary = false, aggressive = true)
public final class IgnarokTheEmberfang extends DataMonster {
  // Reuses the "Agmorkian"/Kraanian drake puppet+sound family already used by
  // Lesser/Greater/Arch Drake — a distinct, lower-tier young drake, not a recolor of them.
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Ignarok the Emberfang";

  public IgnarokTheEmberfang(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Ignarok the Emberfang",
        "${monster.ignarok}",
        1950,
        0,
        0,
        74000,
        95,
        160,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        125,
        385,
        java.util.List.of(
            new MonsterDef.LootDrop("ignaroks_emberfang_claw", 0.008f),
            new MonsterDef.LootDrop("item.wyrmforged_ember", 0.05f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f),
            // T4C-0021: Ignarok as the fire-flavor source for the Ancient Celestial/Empyrean
            // armor sets (ArmorSetGenerator) - previously generated with zero acquisition path.
            new MonsterDef.LootDrop("ancient_celestial_fire_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_fire_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_fire_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_fire_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_fire_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_fire_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_fire_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_fire_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_fire_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_fire_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_fire_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_fire_protector", 0.012f)),
        false,
        0.0f,
        78,
        75,
        75,
        98,
        90,
        78,
        0,
        new int[] {100, 95, 45, 240, 120, 100, 100, 100, 100, 100, 100, 100},
        70,
        290,
        0,
        85,
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
        java.util.List.of(new MonsterDef.Attack("1d150+140", 1275, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Ignarok", "EmberfangDrake"),
        java.util.Map.of());
  }
}
