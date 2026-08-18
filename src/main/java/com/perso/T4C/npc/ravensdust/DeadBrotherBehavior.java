package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;

final class DeadBrotherBehavior implements NpcBehavior {

  private final int number;

  DeadBrotherBehavior(int n) {

    number = n;
  }

  @Override
  public void onConversationStart(NpcBehaviorContext c) {

    if (number == 1
        && (c.flag("__QUEST_DEAD_BROTHERS") == 0 || c.flag("__QUEST_DEAD_BROTHERS") == 1))
      c.flag("__QUEST_DEAD_BROTHERS", 1);
  }

  @Override
  public boolean onKeyword(NpcBehaviorContext c, String text) {

    String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

    int q = c.flag("__QUEST_DEAD_BROTHERS");

    if (number == 12 && (k.contains("I HAVE THE SIX KEY") || k.contains("SIX KEY"))) {

      boolean keys = true;

      for (int i = 1; i <= 6; i++) keys &= c.hasItem("royal_key_" + i);

      if (q >= 12 && keys) {

        c.sayKey("npc.brother12.ready");

        c.askYesNo("dead_brother_dungeon");

      } else c.sayKey(q < 12 ? "npc.brother12.need_brothers" : "npc.brother12.need_keys");

      return true;
    }

    if (k.equals("TORMENT")) {

      if (number == 1 || q == number - 1 || q == number) {

        c.sayKey("npc.topic.brother" + number + ".0");

        c.flag("__QUEST_DEAD_BROTHERS", number);

      } else {

        c.sayKey("npc.brother.wrong");

        c.flag("__QUEST_DEAD_BROTHERS", 0);
      }

      return true;
    }

    if (k.contains("TOURNAMENT")
        || k.contains("THEODORE")
        || k.contains("POISON")
        || k.contains("CUTHANA")
        || k.contains("ROYAL DOOR")
        || k.contains("LOWER DUNGEON")
        || k.equals("DEATH")
        || k.equals("CURSE")
        || k.equals("HEAL")) {

      c.sayKey("npc.brother.wrong");

      c.flag("__QUEST_DEAD_BROTHERS", 0);

      return true;
    }

    if (k.contains("FUCK") || k.contains("SUCK") || k.contains("ASSHOLE") || k.contains(" ASS ")) {

      c.sayKey("npc.brother.insult");

      if (c.player().getCurrentHp() >= 2) c.player().takeDamage(c.player().getCurrentHp() / 2);

      return true;
    }

    return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
  }

  @Override
  public boolean onYesNo(NpcBehaviorContext c, String s, boolean yes) {

    if (!"dead_brother_dungeon".equals(s)) return false;

    if (yes) {

      boolean keys = true;

      for (int i = 1; i <= 6; i++) keys &= c.hasItem("royal_key_" + i);

      if (keys) {

        c.sayKey("npc.brother12.go");

        c.teleport(595, 300, 2);

      } else c.sayKey("npc.brother12.lost_keys");

    } else c.sayKey("npc.brother12.no");

    return true;
  }
}
