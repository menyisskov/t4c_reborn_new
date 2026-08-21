package com.perso.T4C.monster.core;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import java.util.List;

public class NamedEventMonster extends DataMonster implements MonsterLifecycle {

  public NamedEventMonster(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  @Override
  public NpcScriptRuntime.Effects onSpawn(Player p) {

    return NpcScriptRuntime.spawn(this, p);
  }

  @Override
  public NpcScriptRuntime.Effects onAttack(Player p) {

    return NpcScriptRuntime.attack(this, p);
  }

  @Override
  public NpcScriptRuntime.Effects onAttacked(Player p) {

    return NpcScriptRuntime.attacked(this, p);
  }

  @Override
  public NpcScriptRuntime.Effects onHit(Player p) {

    return NpcScriptRuntime.hit(this, p);
  }

  @Override
  public NpcScriptRuntime.Effects onAttackHit(Player p) {

    return NpcScriptRuntime.attackHit(this, p);
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player p) {

    return NpcScriptRuntime.death(this, p);
  }

  protected static NpcScriptRuntime.Effects message(String key) {

    return new NpcScriptRuntime.Effects(List.of("${" + key + "}"), List.of(), List.of());
  }

  protected static NpcScriptRuntime.Effects selfSpell(String spell) {

    return new NpcScriptRuntime.Effects(List.of(), List.of(spell), List.of());
  }

  protected static NpcScriptRuntime.Effects messageAndSpell(String key, String spell) {

    return new NpcScriptRuntime.Effects(List.of("${" + key + "}"), List.of(spell), List.of());
  }
}
