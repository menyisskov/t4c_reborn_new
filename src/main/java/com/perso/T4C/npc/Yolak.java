package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Yolak", x = 1649, y = 1243, z = 0, stationary = false, aggressive = false)
public final class Yolak extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Yolak";

  public static final String DISPLAY_NAME = "${npc.yolak}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.yolak}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yolak.0.0}", "${npc.topic_keyword.yolak.0.1}"),
                  "${npc.topic.yolak.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.yolak.1.0}",
                      "${npc.topic_keyword.yolak.1.1}",
                      "${npc.topic_keyword.yolak.1.2}"),
                  "${npc.topic.yolak.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.yolak.2.0}",
                      "${npc.topic_keyword.yolak.2.1}",
                      "${npc.topic_keyword.yolak.2.2}"),
                  "${npc.topic.yolak.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yolak.3.0}"), "${npc.topic.yolak.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yolak.4.0}"), "${npc.topic.yolak.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yolak.5.0}"), "${npc.topic.yolak.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.yolak.6.0}",
                      "${npc.topic_keyword.yolak.6.1}",
                      "${npc.topic_keyword.yolak.6.2}",
                      "${npc.topic_keyword.yolak.6.3}",
                      "${npc.topic_keyword.yolak.6.4}"),
                  "${npc.topic.yolak.6}",
                  List.of())),
          "YolakNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

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
                      "mana_prism",
                      "critical_healing_potion",
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

  public Yolak(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
