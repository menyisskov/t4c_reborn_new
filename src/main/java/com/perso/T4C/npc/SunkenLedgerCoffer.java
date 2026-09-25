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

// T4C-0049, "Lost Keys of Kraanhold": sits in the cavern near the Kraanian Flyer/Worker/Milipede cluster. Accepts any of the three keys those creatures rarely drop.
@Spawn(type = "SunkenLedgerCoffer", x = 115, y = 1400, z = 2, stationary = true, aggressive = false)
public final class SunkenLedgerCoffer extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SunkenLedgerCoffer";
  public static final String DISPLAY_NAME = "${npc.sunkenledgercoffer}";
  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.sunkenledgercoffer}",
          List.of(),
          "SunkenLedgerCofferNPC",
          new NpcSpec.CombatProfile(100, 1000000, 10, 10, 10, 100000, 0, 65535, "1d3"));

  public SunkenLedgerCoffer(NpcContext context) throws GameException {
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
        if (c.hasItem(LostKeysOfKraanhold.FLYERS_BARBED_KEY)) {
          c.takeItem(LostKeysOfKraanhold.FLYERS_BARBED_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.sunkenledgercoffer.flyers_barbed_key.found");
          return;
        }
        if (c.hasItem(LostKeysOfKraanhold.WORKERS_CALLOUSED_KEY)) {
          c.takeItem(LostKeysOfKraanhold.WORKERS_CALLOUSED_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.sunkenledgercoffer.workers_calloused_key.found");
          return;
        }
        if (c.hasItem(LostKeysOfKraanhold.MILIPEDES_CHITIN_KEY)) {
          c.takeItem(LostKeysOfKraanhold.MILIPEDES_CHITIN_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.sunkenledgercoffer.milipedes_chitin_key.found");
          return;
        }
        c.systemMessageKey("npc.sunkenledgercoffer.locked");
      }
    };
  }
}
