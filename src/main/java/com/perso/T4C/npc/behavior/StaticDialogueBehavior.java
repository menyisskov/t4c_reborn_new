package com.perso.T4C.npc.behavior;

import com.perso.T4C.npc.registry.*;
import java.util.Locale;

public final class StaticDialogueBehavior implements NpcBehavior {

  public static final StaticDialogueBehavior INSTANCE = new StaticDialogueBehavior();

  public static final StaticDialogueBehavior STATIONARY = new StaticDialogueBehavior(true);

  private final boolean stationary;

  private StaticDialogueBehavior() {

    this(false);
  }

  private StaticDialogueBehavior(boolean stationary) {

    this.stationary = stationary;
  }

  @Override
  public void onInitialise(NpcBehaviorContext context) {

    if (stationary) context.npc().setStationary(true);
  }

  @Override
  public void onConversationStart(NpcBehaviorContext context) {

    String welcome = context.npc().getSpec().welcomeText();

    if (welcome != null && !welcome.isBlank()) context.say(welcome);
  }

  @Override
  public boolean onKeyword(NpcBehaviorContext context, String text) {

    if (text == null) return false;

    String keyword = text.trim().toUpperCase(Locale.ROOT);

    if (keyword.equals("BYE")
        || keyword.equals("LEAVE")
        || keyword.equals("QUIT")
        || keyword.equals("FAREWELL")
        || keyword.equals("EXIT")) {

      context.endConversation();

      return true;
    }

    return false;
  }

  public static NpcBehavior randomShardChest() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (c.hasItem("violet_crystal_shard")) {

          c.sayKey("npc.randomchest.empty");

          return;
        }

        int state = c.flag("ADDON_RANDOM_CHEST");

        if (state == 0) {

          state = 1 + (int) (Math.random() * 7);

          c.flag("ADDON_RANDOM_CHEST", state);
        }

        if (state == 1) {

          c.giveItem("violet_crystal_shard");

          c.sayKey("npc.randomchest.found");

          c.flag("ADDON_RANDOM_CHEST", 1 + (int) (Math.random() * 7));

        } else c.sayKey("npc.randomchest.empty");
      }
    };
  }
}
