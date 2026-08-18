package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Boreas extends ScriptedNpc {

  public static final String ID = "Boreas";

  public static final String DISPLAY_NAME = "${npc.boreas}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupMageRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.boreas}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.0.0}"), "${npc.topic.boreas.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.1.0}"), "${npc.topic.boreas.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.2.0}"), "${npc.topic.boreas.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.3.0}"), "${npc.topic.boreas.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.4.0}"), "${npc.topic.boreas.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.5.0}", "${npc.topic_keyword.boreas.5.1}"),
                  "${npc.topic.boreas.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.6.0}"), "${npc.topic.boreas.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.7.0}"), "${npc.topic.boreas.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.8.0}"), "${npc.topic.boreas.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.9.0}"), "${npc.topic.boreas.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.10.0}"), "${npc.topic.boreas.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.11.0}"), "${npc.topic.boreas.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.boreas.12.0}"), "${npc.topic.boreas.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.boreas.13.0}",
                      "${npc.topic_keyword.boreas.13.1}",
                      "${npc.topic_keyword.boreas.13.2}"),
                  "${npc.topic.boreas.13}",
                  List.of())),
          "BoreasNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("BUY") || k.equals("SALE")) {

          java.util.List<String> items =
              new java.util.ArrayList<>(
                  java.util.List.of(
                      "torch",
                      "light_healing_potion",
                      "potion_of_mana",
                      "healing_potion",
                      "mana_elixir",
                      "scroll_of_lighthaven",
                      "scroll_of_windhowl"));

          if (c.flag("__QUEST_ISLAND_ACCESS") == 1) items.add("scroll_of_silversky");

          if (c.flag("__QUEST_ISLAND_ACCESS") == 2) {

            items.add("scroll_of_silversky");

            items.add("scroll_of_stonecrest");
          }

          c.openShop(items);

          return true;
        }

        if (k.equals("SELL")) {

          c.openSellShop();

          return true;
        }

        return false;
      }
    };
  }

  public Boreas(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
