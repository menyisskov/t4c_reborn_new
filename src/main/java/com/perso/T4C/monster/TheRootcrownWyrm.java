package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// First of the "Elder Wyrms" (T4C-0029): proto-drakes that predate the named Drake line
// (Ignarok/Mordrenn/Greater Drake/Arch Drake) and, unlike them, each embody one *class*
// archetype instead of one element - see DESIGN_GUIDELINES.md's Items section for the
// class/school rules this pilot follows. The Rootcrown Wyrm is the wisdom-mage exemplar
// (earth power/resistance, per its own gear below), sealed in the same lair as Arch Drake but
// away from his own spawn and the four Kraanian Dragonguards. Reuses the Agmorkian/Kraanian
// drake puppet+sound family already used by the rest of the Drake line.
@Spawn(
    type = "The Rootcrown Wyrm",
    x = 2950,
    y = 2650,
    z = 0,
    stationary = false,
    aggressive = true)
public final class TheRootcrownWyrm extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "The Rootcrown Wyrm";

  public TheRootcrownWyrm(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "The Rootcrown Wyrm",
        "${monster.the_rootcrown_wyrm}",
        95000,
        0,
        12,
        125000000,
        900,
        1900,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1350,
        3400,
        java.util.List.of(
            new MonsterDef.LootDrop("rootcrown_wyrms_verdant_sceptre", 0.01f),
            new MonsterDef.LootDrop("item.veiled_aether_shard", 0.05f),
            new MonsterDef.LootDrop("rootcrown_wyrms_ageless_mantle", 0.015f),
            new MonsterDef.LootDrop("rootcrown_wyrms_timeless_circlet", 0.015f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f)),
        false,
        0.0f,
        700,
        800,
        250,
        200,
        0,
        950,
        0,
        new int[] {100, 170, 100, 90, 60, 150, 100, 100, 100, 100, 100, 100},
        700,
        2100,
        0,
        700,
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
        java.util.List.of(new MonsterDef.Attack("1d1000+900", 1900, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("The Rootcrown Wyrm", "RootcrownWyrm"),
        java.util.Map.of());
  }
}
