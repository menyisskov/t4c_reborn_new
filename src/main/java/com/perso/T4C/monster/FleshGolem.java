package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBFLESHGOLEM", x = 74, y = 1569, z = 0, stationary = false, aggressive = false)
public final class FleshGolem extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public FleshGolem(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onSpawn(Player p) {

    return selfSpell("spell.npc_cantrip_red_wipe");
  }

  @Override
  public MonsterScriptBridge.Effects onAttacked(Player p) {

    if (getHealth() < 181 && Math.random() < .25)
      return new MonsterScriptBridge.Effects(
          java.util.List.of("${npc.fleshgolem.shout.last_breath}"),
          java.util.List.of("spell.mob_ai_spell_blaze_of_glory"),
          java.util.List.of());

    return MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBFLESHGOLEM",
        "${monster.mobfleshgolem}",
        6012,
        0,
        44,
        133443,
        128,
        291,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        340,
        1044,
        java.util.List.of(),
        false,
        0.0f,
        110,
        100,
        100,
        129,
        100,
        100,
        34,
        new int[] {49, 49, 49, 49, 65, 5000, 150, 150, 150, 150, 150, 150},
        95,
        390,
        0,
        1111228416,
        20009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d164+127", 1150, 33, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
