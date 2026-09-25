package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.definition.LostKeysOfKraanhold;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0049, "Lost Keys of Kraanhold": sits on the worldmap near Drake's Lair, where Kraanian Wyrmlings and Dragonguards roam and the Toll Trolls guard the coast road. The Dragonguard's key is worth the hunt - it's the one that pays a real reward instead of gold and potions.
@Spawn(type = "WyrmlingsHoardCasket", x = 2350, y = 2900, z = 0, stationary = true, aggressive = false)
public final class WyrmlingsHoardCasket extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "WyrmlingsHoardCasket";
  public static final String DISPLAY_NAME = "${npc.wyrmlingshoardcasket}";
  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.wyrmlingshoardcasket}",
          List.of(),
          "WyrmlingsHoardCasketNPC",
          new NpcSpec.CombatProfile(100, 1000000, 10, 10, 10, 100000, 0, 65535, "1d3"));

  public WyrmlingsHoardCasket(NpcContext context) throws GameException {
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
        if (c.hasItem(LostKeysOfKraanhold.WYRMLINGS_TARNISHED_KEY)) {
          if (!canGrantAll(c.player(), "serious_healing_potion", "mana_elixir")) {
            c.systemMessageKey("message.chest_reward_blocked");
            return;
          }
          c.takeItem(LostKeysOfKraanhold.WYRMLINGS_TARNISHED_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.wyrmlingshoardcasket.wyrmlings_tarnished_key.found");
          return;
        }
        if (c.hasItem(LostKeysOfKraanhold.DRAGONGUARDS_SEALED_KEY)) {
          if (!canGrantAll(c.player(), "sealed_signet_of_the_dragonguard")) {
            c.systemMessageKey("message.chest_reward_blocked");
            return;
          }
          c.takeItem(LostKeysOfKraanhold.DRAGONGUARDS_SEALED_KEY);
          c.giveGold(3000);
          c.giveItem("sealed_signet_of_the_dragonguard");
          c.systemMessageKey("npc.wyrmlingshoardcasket.dragonguards_sealed_key.found");
          return;
        }
        if (c.hasItem(LostKeysOfKraanhold.TOLL_TROLLS_RUSTED_KEY)) {
          if (!canGrantAll(c.player(), "serious_healing_potion", "mana_elixir")) {
            c.systemMessageKey("message.chest_reward_blocked");
            return;
          }
          c.takeItem(LostKeysOfKraanhold.TOLL_TROLLS_RUSTED_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.wyrmlingshoardcasket.toll_trolls_rusted_key.found");
          return;
        }
        c.systemMessageKey("npc.wyrmlingshoardcasket.locked");
      }
    };
  }

  /** True only if every one of these item keys could actually be added right now - checked
   * before the matching key is consumed, so a chest opened with a full backpack (or one that
   * already owns a unique reward) never destroys the key for nothing. */
  private static boolean canGrantAll(Player player, String... itemKeys) {
    for (String key : itemKeys) {
      if (!InventoryService.canAdd(player, key)) return false;
    }
    return true;
  }
}
