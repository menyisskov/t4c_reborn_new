package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

// T4C-00XX: Bloodlust had no @Spawn at all - Bloodstone Ring/Essence of Bloodlust (both required
// by Xanth's quests) had a loot entry but the monster carrying it never actually appeared
// anywhere in the world. Placed adjacent to Xanth's own verified-walkable position since no
// other placement reference exists for this monster.
@Spawn(type = "Bloodlust", x = 691, y = 1610, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Bloodlust", x = 690, y = 1611, z = 2, stationary = false, aggressive = true)
public final class Bloodlust extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Worm Attack.wav";
  public static final String SOUND_DEATH = "Worm Dying.wav";
  public static final String SOUND_HIT = "Worm Hit.wav";

  public Bloodlust(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player p) {

    if (p != null) {

      p.setQuestFlag("USER_HAS_SLAIN_BLOODLUST", 1);

      if (Math.random() < .75) {

        InventoryService.add(p, "bloodstone_ring");

        if (Math.random() < .25) InventoryService.add(p, "essence_of_bloodlust");

      } else if (Math.random() < .25) InventoryService.add(p, "essence_of_bloodlust");
    }

    return NpcScriptRuntime.Effects.empty();
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
        java.util.List.of(
            new MonsterDef.LootDrop("bloodstone_ring", 0.3f),
            new MonsterDef.LootDrop("essence_of_bloodlust", 0.45f)),
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
