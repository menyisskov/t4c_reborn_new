package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "ThorGlarefire", x = 900, y = 1070, z = 0, stationary = false, aggressive = false)
public final class ThorGlarefire extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ThorGlarefire";

  public static final String DISPLAY_NAME = "${npc.thorglarefire}";

  public static final String SPRITE_BASE = "64kCentaurWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.thorglarefire}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.0.0}",
                      "${npc.topic_keyword.thorglarefire.0.1}"),
                  "${npc.topic.thorglarefire.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorglarefire.1.0}"),
                  "${npc.topic.thorglarefire.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorglarefire.2.0}"),
                  "${npc.topic.thorglarefire.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.3.0}",
                      "${npc.topic_keyword.thorglarefire.3.1}"),
                  "${npc.topic.thorglarefire.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorglarefire.4.0}"),
                  "${npc.topic.thorglarefire.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.5.0}",
                      "${npc.topic_keyword.thorglarefire.5.1}"),
                  "${npc.topic.thorglarefire.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.6.0}",
                      "${npc.topic_keyword.thorglarefire.6.1}"),
                  "${npc.topic.thorglarefire.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.7.0}",
                      "${npc.topic_keyword.thorglarefire.7.1}"),
                  "${npc.topic.thorglarefire.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.8.0}",
                      "${npc.topic_keyword.thorglarefire.8.1}"),
                  "${npc.topic.thorglarefire.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.9.0}",
                      "${npc.topic_keyword.thorglarefire.9.1}"),
                  "${npc.topic.thorglarefire.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.10.0}",
                      "${npc.topic_keyword.thorglarefire.10.1}"),
                  "${npc.topic.thorglarefire.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.11.0}",
                      "${npc.topic_keyword.thorglarefire.11.1}",
                      "${npc.topic_keyword.thorglarefire.11.2}"),
                  "${npc.topic.thorglarefire.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.12.0}",
                      "${npc.topic_keyword.thorglarefire.12.1}"),
                  "${npc.topic.thorglarefire.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.13.0}",
                      "${npc.topic_keyword.thorglarefire.13.1}"),
                  "${npc.topic.thorglarefire.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.14.0}",
                      "${npc.topic_keyword.thorglarefire.14.1}"),
                  "${npc.topic.thorglarefire.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.15.0}",
                      "${npc.topic_keyword.thorglarefire.15.1}"),
                  "${npc.topic.thorglarefire.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorglarefire.16.0}"),
                  "${npc.topic.thorglarefire.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.17.0}",
                      "${npc.topic_keyword.thorglarefire.17.1}",
                      "${npc.topic_keyword.thorglarefire.17.2}",
                      "${npc.topic_keyword.thorglarefire.17.3}"),
                  "${npc.topic.thorglarefire.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorglarefire.18.0}",
                      "${npc.topic_keyword.thorglarefire.18.1}",
                      "${npc.topic_keyword.thorglarefire.18.2}",
                      "${npc.topic_keyword.thorglarefire.18.3}",
                      "${npc.topic_keyword.thorglarefire.18.4}"),
                  "${npc.topic.thorglarefire.18}",
                  List.of())),
          "ThorGlarefireNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int q = c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST");

        if (q == 9) {

          c.sayKey("npc.thor.friend");

          c.summon("MOBSHADOWSTALKER", c.npcTileX() + 1, c.npcTileY(), 0);

          c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 10);

        } else if (q == 10) {

          c.sayKey("npc.thor.mordenthal");

          c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 11);

        } else c.sayKey("npc.thor.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("HAMMER") && k.contains("FINALITY")) {

          int q = c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST");

          if (q == 11) {

            c.giveItem("silversmith_letter");

            c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 12);

            c.sayKey("npc.thor.hammer.letter");

          } else if (q == 12) c.sayKey("npc.thor.hammer.repeat");
          else c.sayKey("npc.thor.hammer.no");

          return true;
        }

        if (k.contains("SWORD")
            && k.contains("MAJESTY")
            && c.itemCount("red_spellbook") >= 5
            && c.itemCount("blade_of_heroism") >= 3) {

          c.askYesNo("thor_sword");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"thor_sword".equals(s)) return false;

        if (yes && c.itemCount("red_spellbook") >= 5 && c.itemCount("blade_of_heroism") >= 3) {

          for (int i = 0; i < 5; i++) c.takeItem("red_spellbook");

          for (int i = 0; i < 3; i++) c.takeItem("blade_of_heroism");

          c.giveItem("sword_of_majesty");

          c.sayKey("npc.thor.sword.ok");
        }

        return true;
      }
    };
  }

  public ThorGlarefire(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
