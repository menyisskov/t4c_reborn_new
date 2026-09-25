package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

// The Windhowl War-Party (T4C-0045): a Centaur warband camped in its own clearing northwest of
// the existing Windhowl Marches spawns (CentaurWarrior/CentaurKing, T4C-0005), one tier below
// Centaur Warrior. See WarbandBannerBearer for the mini-boss that gates the camp's "cleared"
// reward and WarbandCampState for the shared encounter tracking.
@Spawn(type = "Warband Raider", x = 2260, y = 2320, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warband Raider", x = 2300, y = 2320, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warband Raider", x = 2260, y = 2360, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warband Raider", x = 2300, y = 2360, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warband Raider", x = 2280, y = 2300, z = 0, stationary = false, aggressive = true)
public final class WarbandRaider extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Taunting Hit.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public static final String CANONICAL_NAME = "Warband Raider";

  public WarbandRaider(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player p) {
    // Whether this kill actually clears the camp (banner-bearer down first) is decided by
    // WarbandCampState from MainGameScreen's death handling, which also owns summoning the
    // warlord - this hook is flavor only.
    return message(
        WarbandCampState.isScattered(WarbandCampState.Camp.WINDHOWL_WAR_PARTY)
            ? "npc.warband.raider.death_scattered"
            : "npc.warband.raider.death");
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Warband Raider",
        "${monster.warband_raider}",
        4200,
        0,
        7,
        18000,
        135,
        240,
        30000L,
        "64kCentaurWarrior#i",
        "64kCentaurWarriorA#i",
        "64kCentaurWarriorC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        220,
        650,
        java.util.List.of(
            new MonsterDef.LootDrop("item.raiders_notched_key", 0.05f),
            new MonsterDef.LootDrop("centaur_warband_ring", 0.04f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.25f),
            new MonsterDef.LootDrop("mana_elixir", 0.15f)),
        false,
        0.0f,
        128,
        118,
        122,
        150,
        0,
        118,
        0,
        new int[] {100, 130, 100, 90, 100, 100, 100, 100, 100, 100, 100, 100},
        118,
        470,
        0,
        95,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        70,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d200+150", 1400, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("WarbandRaider"),
        java.util.Map.of());
  }
}
