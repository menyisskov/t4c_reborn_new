package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.definition.LostKeysOfKraanhold;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0049, "Lost Keys of Kraanhold": buried just outside the Windhowl War-Party's camp (T4C-0045) - the raiders never trusted their own banner-bearer with the whole hoard. Accepts any of the three keys the warband (Raider/Banner-Bearer/Warlord) rarely drops. The Warlord's key is the one that pays a real reward instead of gold and potions.
@Spawn(type = "WarbandsBuriedChest", x = 2295, y = 2325, z = 0, stationary = true, aggressive = false)
public final class WarbandsBuriedChest extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "WarbandsBuriedChest";
  public static final String DISPLAY_NAME = "${npc.warbandsburiedchest}";
  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.warbandsburiedchest}",
          List.of(),
          "WarbandsBuriedChestNPC",
          new NpcSpec.CombatProfile(100, 1000000, 10, 10, 10, 100000, 0, 65535, "1d3"));

  public WarbandsBuriedChest(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public void onConversationStart(NpcBehaviorContext c) {
        if (c.hasItem(LostKeysOfKraanhold.RAIDERS_NOTCHED_KEY)) {
          c.takeItem(LostKeysOfKraanhold.RAIDERS_NOTCHED_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.warbandsburiedchest.raiders_notched_key.found");
          return;
        }
        if (c.hasItem(LostKeysOfKraanhold.BANNER_BEARERS_KEY)) {
          c.takeItem(LostKeysOfKraanhold.BANNER_BEARERS_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.warbandsburiedchest.banner_bearers_key.found");
          return;
        }
        if (c.hasItem(LostKeysOfKraanhold.WARLORDS_SIGNET_KEY)) {
          c.takeItem(LostKeysOfKraanhold.WARLORDS_SIGNET_KEY);
          c.giveItem("warlords_iron_signet");
          c.giveGold(3000);
          c.systemMessageKey("npc.warbandsburiedchest.warlords_signet_key.found");
          return;
        }
        c.systemMessageKey("npc.warbandsburiedchest.locked");
      }
    };
  }
}
