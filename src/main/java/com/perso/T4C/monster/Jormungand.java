package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "JORMUNGAND", x = 480, y = 2008, z = 0, stationary = false, aggressive = true)
public final class Jormungand extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public Jormungand(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player p) {

    if (p != null) InventoryService.add(p, "jormungand_soulstone");

    return new MonsterScriptBridge.Effects(
        java.util.List.of("${message.jormungand.soulstone}"),
        java.util.List.of(),
        java.util.List.of());
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "JORMUNGAND",
        "${monster.jormungand}",
        6500,
        0,
        0,
        369905,
        135,
        306,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        115,
        104,
        104,
        135,
        104,
        104,
        35,
        new int[] {100, 100, 100, 100, 100, 5000, 100, 100, 100, 100, 100, 100},
        100,
        410,
        0,
        1112014848,
        10011,
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
        java.util.List.of(new MonsterDef.Attack("1d172+134", 1210, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
