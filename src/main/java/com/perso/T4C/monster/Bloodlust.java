package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class Bloodlust extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public Bloodlust(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player p) {

    if (p != null) {

      p.setQuestFlag("USER_HAS_SLAIN_BLOODLUST", 1);

      if (Math.random() < .75) {

        InventoryService.add(p, "bloodstone_ring");

        if (Math.random() < .25) InventoryService.add(p, "essence_of_bloodlust");

      } else if (Math.random() < .25) InventoryService.add(p, "essence_of_bloodlust");
    }

    return MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
         "BLOODLUST",
        "${monster.bloodlust}",
        100,
        0,
        1,
        100,
        1,
        2,
        30000L,
        "",
        "",
        "",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
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
        1,
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
