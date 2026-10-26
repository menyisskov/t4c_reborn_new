package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBPURIFIER", x = 1846, y = 2869, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1881, y = 2862, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1919, y = 2939, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1920, y = 2745, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1929, y = 2817, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1948, y = 2787, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1954, y = 2844, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1989, y = 2811, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 2031, y = 2732, z = 0, stationary = false, aggressive = false)
public final class Purifier extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public Purifier(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onAttack(Player p) {

    return Math.random() < 1.0 / 11
        ? message("npc.purifier.shout")
        : NpcScriptRuntime.Effects.empty();
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player p) {
    p.setQuestFlag(
        "__FLAG_COUNTER_PURIFIER_KILLED", p.getQuestFlag("__FLAG_COUNTER_PURIFIER_KILLED") + 1);
    return super.onDeath(p);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBPURIFIER",
        "${monster.mobpurifier}",
        3259,
        0,
        9,
        14831,
        85,
        160,
        30000L,
        "",
        "",
        "",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(
            new MonsterDef.LootDrop("Talisman of Protection from Evil", 0.002f),
            new MonsterDef.LootDrop("morningstar_of_the_sun", 0.002f),
            new MonsterDef.LootDrop("Critical Healing Potion", 0.05f),
            new MonsterDef.LootDrop("Key of Ascension", 0.005f),
            new MonsterDef.LootDrop("Rough Amethyst", 0.02f),
            new MonsterDef.LootDrop("Potion of Tranquility", 0.01f),
            new MonsterDef.LootDrop("Mana Prism", 0.01f),
            new MonsterDef.LootDrop("Rough Aquamarine", 0.01f),
            new MonsterDef.LootDrop("Scroll of Recall", 0.01f),
            new MonsterDef.LootDrop("Robe of the Purified Souls", 0.001f)),
        false,
        0.0f,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        100,
        0,
        0,
        0,
        10003,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        true,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
