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

public final class AnrakBrownbark extends ScriptedNpc {

  public static final String ID = "AnrakBrownbark";

  public static final String DISPLAY_NAME = "${npc.anrakbrownbark}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupElvenHat"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.anrakbrownbark}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.0.0}",
                      "${npc.topic_keyword.anrakbrownbark.0.1}"),
                  "${npc.topic.anrakbrownbark.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.1.0}",
                      "${npc.topic_keyword.anrakbrownbark.1.1}"),
                  "${npc.topic.anrakbrownbark.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.2.0}",
                      "${npc.topic_keyword.anrakbrownbark.2.1}",
                      "${npc.topic_keyword.anrakbrownbark.2.2}"),
                  "${npc.topic.anrakbrownbark.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.3.0}",
                      "${npc.topic_keyword.anrakbrownbark.3.1}"),
                  "${npc.topic.anrakbrownbark.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.anrakbrownbark.4.0}"),
                  "${npc.topic.anrakbrownbark.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.5.0}",
                      "${npc.topic_keyword.anrakbrownbark.5.1}",
                      "${npc.topic_keyword.anrakbrownbark.5.2}"),
                  "${npc.topic.anrakbrownbark.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.6.0}",
                      "${npc.topic_keyword.anrakbrownbark.6.1}"),
                  "${npc.topic.anrakbrownbark.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.7.0}",
                      "${npc.topic_keyword.anrakbrownbark.7.1}"),
                  "${npc.topic.anrakbrownbark.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.8.0}",
                      "${npc.topic_keyword.anrakbrownbark.8.1}"),
                  "${npc.topic.anrakbrownbark.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.9.0}",
                      "${npc.topic_keyword.anrakbrownbark.9.1}"),
                  "${npc.topic.anrakbrownbark.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.10.0}",
                      "${npc.topic_keyword.anrakbrownbark.10.1}"),
                  "${npc.topic.anrakbrownbark.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.11.0}",
                      "${npc.topic_keyword.anrakbrownbark.11.1}",
                      "${npc.topic_keyword.anrakbrownbark.11.2}",
                      "${npc.topic_keyword.anrakbrownbark.11.3}"),
                  "${npc.topic.anrakbrownbark.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anrakbrownbark.12.0}",
                      "${npc.topic_keyword.anrakbrownbark.12.1}",
                      "${npc.topic_keyword.anrakbrownbark.12.2}",
                      "${npc.topic_keyword.anrakbrownbark.12.3}",
                      "${npc.topic_keyword.anrakbrownbark.12.4}"),
                  "${npc.topic.anrakbrownbark.12}",
                  List.of())),
          "AnrakNPC",
          new NpcSpec.CombatProfile(100, 1000000, 25, 25, 25, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttacked(NpcBehaviorContext c) {

        c.summon("MOBGEHENNAREAVER", c.npcTileX() + 1, c.npcTileY() + 1, 0);
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        if (text == null
            || !text.toUpperCase(java.util.Locale.ROOT).contains("DRUM")
            || !text.toUpperCase(java.util.Locale.ROOT).contains("FATE")) return false;

        int quest = c.flag("QUEST_FLAG_WILL_OF_ARTHERK_QUEST");

        if (quest >= 16) c.sayKey("npc.anrak.drums.done");
        else if (quest == 15) {

          c.sayKey("npc.anrak.drums.offer");

          if (c.itemCount("finely_crafted_drum") >= 1
              && c.itemCount("gem_of_the_immortal") >= 4
              && c.itemCount("hourglass_of_essence") >= 4) c.askYesNo("make_drum");

        } else c.sayKey("npc.anrak.drums.denied");

        return true;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

        if (!"make_drum".equals(state)) return false;

        if (!yes) {

          c.sayKey("npc.anrak.drums.no");

          return true;
        }

        if (c.itemCount("finely_crafted_drum") < 1
            || c.itemCount("gem_of_the_immortal") < 4
            || c.itemCount("hourglass_of_essence") < 4) {

          c.sayKey("npc.anrak.drums.missing");

          return true;
        }

        if (c.player().getGold() < 60000) {

          c.sayKey("npc.anrak.drums.poor");

          return true;
        }

        c.player().addGold(-60000);

        c.takeItem("finely_crafted_drum");

        for (int i = 0; i < 4; i++) {

          c.takeItem("gem_of_the_immortal");

          c.takeItem("hourglass_of_essence");
        }

        c.giveItem("drum_of_fate");

        int count = c.flag("FLAG_COUNTER_DRUM_OF_FATE") + 1;

        c.flag("FLAG_COUNTER_DRUM_OF_FATE", count);

        c.sayKey("npc.anrak.drums.complete");

        if (count == 2) {

          c.flag("QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 16);

          c.summon("MOBGEHENNAREAVER", c.npcTileX() + 1, c.npcTileY() + 1, 0);
        }

        return true;
      }
    };
  }

  public AnrakBrownbark(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
