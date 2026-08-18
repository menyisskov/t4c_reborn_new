package com.perso.T4C.monster.core;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import java.util.List;

public class NamedEventMonster extends DataMonster implements MonsterLifecycle {

  public NamedEventMonster(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  @Override
  public MonsterScriptBridge.Effects onSpawn(Player p) {

    return MonsterScriptBridge.spawn(this, p);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return MonsterScriptBridge.attack(this, p);
  }

  @Override
  public MonsterScriptBridge.Effects onAttacked(Player p) {

    return MonsterScriptBridge.attacked(this, p);
  }

  @Override
  public MonsterScriptBridge.Effects onHit(Player p) {

    return MonsterScriptBridge.hit(this, p);
  }

  @Override
  public MonsterScriptBridge.Effects onAttackHit(Player p) {

    return MonsterScriptBridge.attackHit(this, p);
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player p) {

    return MonsterScriptBridge.death(this, p);
  }

  protected static MonsterScriptBridge.Effects message(String key) {

    return new MonsterScriptBridge.Effects(List.of("${" + key + "}"), List.of(), List.of());
  }

  protected static MonsterScriptBridge.Effects selfSpell(String spell) {

    return new MonsterScriptBridge.Effects(List.of(), List.of(spell), List.of());
  }

  protected static MonsterScriptBridge.Effects messageAndSpell(String key, String spell) {

    return new MonsterScriptBridge.Effects(List.of("${" + key + "}"), List.of(spell), List.of());
  }
}
