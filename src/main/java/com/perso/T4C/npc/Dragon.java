package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Dragon", x = 2835, y = 1113, z = 0, stationary = true, aggressive = false)
public final class Dragon extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public static final String ID = "Dragon";

  public static final String DISPLAY_NAME = "${npc.dragon}";

  public static final String SPRITE_BASE = "DragonSTMOV";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.dragon}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.0.0}", "${npc.topic_keyword.dragon.0.1}"),
                  "${npc.topic.dragon.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dragon.1.0}",
                      "${npc.topic_keyword.dragon.1.1}",
                      "${npc.topic_keyword.dragon.1.2}"),
                  "${npc.topic.dragon.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.2.0}", "${npc.topic_keyword.dragon.2.1}"),
                  "${npc.topic.dragon.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.3.0}"), "${npc.topic.dragon.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.4.0}"), "${npc.topic.dragon.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.5.0}"), "${npc.topic.dragon.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.6.0}", "${npc.topic_keyword.dragon.6.1}"),
                  "${npc.topic.dragon.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.7.0}"), "${npc.topic.dragon.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.8.0}"), "${npc.topic.dragon.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.9.0}", "${npc.topic_keyword.dragon.9.1}"),
                  "${npc.topic.dragon.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.10.0}", "${npc.topic_keyword.dragon.10.1}"),
                  "${npc.topic.dragon.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.11.0}"), "${npc.topic.dragon.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.12.0}"), "${npc.topic.dragon.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.13.0}"), "${npc.topic.dragon.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.14.0}", "${npc.topic_keyword.dragon.14.1}"),
                  "${npc.topic.dragon.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.15.0}"), "${npc.topic.dragon.15}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.16.0}"), "${npc.topic.dragon.16}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.17.0}"), "${npc.topic.dragon.17}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.18.0}"), "${npc.topic.dragon.18}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.19.0}"), "${npc.topic.dragon.19}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.20.0}"), "${npc.topic.dragon.20}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.21.0}", "${npc.topic_keyword.dragon.21.1}"),
                  "${npc.topic.dragon.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.22.0}"), "${npc.topic.dragon.22}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dragon.23.0}"), null, List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dragon.24.0}",
                      "${npc.topic_keyword.dragon.24.1}",
                      "${npc.topic_keyword.dragon.24.2}",
                      "${npc.topic_keyword.dragon.24.3}",
                      "${npc.topic_keyword.dragon.24.4}"),
                  "${npc.topic.dragon.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dragon.25.0}",
                      "${npc.topic_keyword.dragon.25.1}",
                      "${npc.topic_keyword.dragon.25.2}",
                      "${npc.topic_keyword.dragon.25.3}",
                      "${npc.topic_keyword.dragon.25.4}"),
                  "${npc.topic.dragon.25}",
                  List.of())),
          "DragonNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          if (c.flag("__DRAGON_IS_SILENT") == 1) {

            c.sayKey("npc.dragon.silent");

            return;
          }

          if (c.player().getLevel() >= 30) {

            c.flag("__DRAGON_IS_SILENT", 0);

            c.sayKey("npc.dragon.mature");

          } else c.sayKey("npc.dragon.young");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.contains("CRYPT") || k.contains("TOMB RAIDER")) {

            int q = c.flag("__QUEST_DRAGON_QUEST_COMPLETED");

            if (q == 2) c.sayKey("npc.dragon.quest.done");
            else if (q == 1 && c.flag("__FLAG_KILLED_TOMB_RAIDER") > 0) {

              c.giveXp(5000);

              c.flag("__FLAG_KILLED_TOMB_RAIDER", 0);

              c.flag("__QUEST_DRAGON_QUEST_COMPLETED", 2);

              c.sayKey("npc.dragon.quest.reward");

            } else {

              c.flag("__FLAG_KILLED_TOMB_RAIDER", 0);

              c.flag("__QUEST_DRAGON_QUEST_COMPLETED", 1);

              c.sayKey("npc.dragon.quest.ask");
            }

            return true;
          }

          return false;
        }
      };

  public Dragon(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
