package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

// The Windhowl War-Party's mini-boss (T4C-0045): carries the banner that keeps its raiders
// (WarbandRaider) rallied. Its death opens the camp's "scattered" window (WarbandCampState) -
// raiders felled outside that window don't count toward clearing the camp and summoning
// the on-demand Warband Warlord monster.
@Spawn(
    type = "Warband Banner-Bearer",
    x = 2280,
    y = 2340,
    z = 0,
    stationary = false,
    aggressive = true)
public final class WarbandBannerBearer extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Taunting Hit.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public static final String CANONICAL_NAME = "Warband Banner-Bearer";

  public WarbandBannerBearer(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player p) {
    // Actually opening the scatter window happens in MainGameScreen's death handling (it also
    // owns summoning the warlord), so a respawn hook can reset the window cleanly. This is
    // flavor only.
    return message("npc.warband.banner.fallen");
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Warband Banner-Bearer",
        "${monster.warband_banner_bearer}",
        9200,
        0,
        0,
        42000,
        170,
        300,
        30000L,
        "64kCentaurKing#i",
        "64kCentaurKingA#i",
        "64kCentaurKingC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        700,
        1900,
        java.util.List.of(
            new MonsterDef.LootDrop("centaur_warband_ring", 0.08f),
            new MonsterDef.LootDrop("ring_of_the_archer", 0.02f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.35f),
            new MonsterDef.LootDrop("mana_elixir", 0.25f)),
        false,
        0.0f,
        155,
        142,
        130,
        175,
        30,
        142,
        0,
        new int[] {100, 135, 100, 90, 105, 100, 100, 100, 100, 100, 100, 100},
        130,
        520,
        0,
        115,
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
        java.util.List.of(new MonsterDef.Attack("1d220+170", 1800, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("WarbandBannerBearer"),
        java.util.Map.of());
  }
}
