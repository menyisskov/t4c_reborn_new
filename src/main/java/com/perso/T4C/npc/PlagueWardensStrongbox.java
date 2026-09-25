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

// T4C-0049, "Lost Keys of Kraanhold": sits in the dungeon near the Kraanian Plague/Reaper/Stomper cluster. Accepts any of the three keys those creatures rarely drop.
@Spawn(type = "PlagueWardensStrongbox", x = 2080, y = 2220, z = 1, stationary = true, aggressive = false)
public final class PlagueWardensStrongbox extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "PlagueWardensStrongbox";
  public static final String DISPLAY_NAME = "${npc.plaguewardensstrongbox}";
  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.plaguewardensstrongbox}",
          List.of(),
          "PlagueWardensStrongboxNPC",
          new NpcSpec.CombatProfile(100, 1000000, 10, 10, 10, 100000, 0, 65535, "1d3"));

  public PlagueWardensStrongbox(NpcContext context) throws GameException {
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
        if (c.hasItem(LostKeysOfKraanhold.PLAGUE_EATEN_KEY)) {
          c.takeItem(LostKeysOfKraanhold.PLAGUE_EATEN_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.plaguewardensstrongbox.plague_eaten_key.found");
          return;
        }
        if (c.hasItem(LostKeysOfKraanhold.REAPERS_IRON_KEY)) {
          c.takeItem(LostKeysOfKraanhold.REAPERS_IRON_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.plaguewardensstrongbox.reapers_iron_key.found");
          return;
        }
        if (c.hasItem(LostKeysOfKraanhold.STOMPERS_CRACKED_KEY)) {
          c.takeItem(LostKeysOfKraanhold.STOMPERS_CRACKED_KEY);
          c.giveGold(1500);
          c.giveItem("serious_healing_potion");
          c.giveItem("mana_elixir");
          c.systemMessageKey("npc.plaguewardensstrongbox.stompers_cracked_key.found");
          return;
        }
        c.systemMessageKey("npc.plaguewardensstrongbox.locked");
      }
    };
  }
}
