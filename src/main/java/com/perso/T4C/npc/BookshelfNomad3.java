package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "BookshelfNomad3", x = 0, y = 0, z = 0, stationary = true, aggressive = false)
public final class BookshelfNomad3 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "BookshelfNomad3";

  public static final String DISPLAY_NAME = "${npc.bookshelfnomad3}";

  public static final String SPRITE_BASE = "@static:RockDoor1";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.bookshelfnomad3}",
          List.of(),
          "BookshelfNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("ADDON_BOOKSHELF3_SEARCHED") == 0) {

          c.flag("ADDON_BOOKSHELF3_SEARCHED", 1);

          c.giveItem("exquisitely_crafted_gold_ring");

          c.giveItem("sparkling_ruby");

          c.giveItem("gem_encrusted_silver_pendant");

          c.sayKey("npc.bookshelf3.found");

        } else c.sayKey("npc.bookshelf3.empty");
      }
    };
  }

  public BookshelfNomad3(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
