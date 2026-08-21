package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "GhundargRumblefoot", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
public final class GhundargRumblefoot extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public static final String ID = "GhundargRumblefoot";

  public static final String DISPLAY_NAME = "${npc.ghundargrumblefoot}";

  public static final String SPRITE_BASE = "Orc";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.ghundargrumblefoot}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ghundargrumblefoot.0.0}",
                      "${npc.topic_keyword.ghundargrumblefoot.0.1}"),
                  "${npc.topic.ghundargrumblefoot.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ghundargrumblefoot.1.0}",
                      "${npc.topic_keyword.ghundargrumblefoot.1.1}",
                      "${npc.topic_keyword.ghundargrumblefoot.1.2}"),
                  "${npc.topic.ghundargrumblefoot.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ghundargrumblefoot.2.0}"),
                  "${npc.topic.ghundargrumblefoot.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ghundargrumblefoot.3.0}",
                      "${npc.topic_keyword.ghundargrumblefoot.3.1}",
                      "${npc.topic_keyword.ghundargrumblefoot.3.2}"),
                  "${npc.topic.ghundargrumblefoot.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ghundargrumblefoot.4.0}"),
                  "${npc.topic.ghundargrumblefoot.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ghundargrumblefoot.5.0}",
                      "${npc.topic_keyword.ghundargrumblefoot.5.1}"),
                  "${npc.topic.ghundargrumblefoot.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ghundargrumblefoot.6.0}"),
                  "${npc.topic.ghundargrumblefoot.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ghundargrumblefoot.7.0}"),
                  "${npc.topic.ghundargrumblefoot.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ghundargrumblefoot.8.0}"),
                  "${npc.topic.ghundargrumblefoot.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ghundargrumblefoot.9.0}",
                      "${npc.topic_keyword.ghundargrumblefoot.9.1}",
                      "${npc.topic_keyword.ghundargrumblefoot.9.2}",
                      "${npc.topic_keyword.ghundargrumblefoot.9.3}"),
                  "${npc.topic.ghundargrumblefoot.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ghundargrumblefoot.10.0}",
                      "${npc.topic_keyword.ghundargrumblefoot.10.1}"),
                  "${npc.topic.ghundargrumblefoot.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ghundargrumblefoot.11.0}",
                      "${npc.topic_keyword.ghundargrumblefoot.11.1}",
                      "${npc.topic_keyword.ghundargrumblefoot.11.2}"),
                  "${npc.topic.ghundargrumblefoot.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ghundargrumblefoot.12.0}"),
                  "${npc.topic.ghundargrumblefoot.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ghundargrumblefoot.13.0}"),
                  "${npc.topic.ghundargrumblefoot.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ghundargrumblefoot.14.0}",
                      "${npc.topic_keyword.ghundargrumblefoot.14.1}",
                      "${npc.topic_keyword.ghundargrumblefoot.14.2}",
                      "${npc.topic_keyword.ghundargrumblefoot.14.3}",
                      "${npc.topic_keyword.ghundargrumblefoot.14.4}"),
                  "${npc.topic.ghundargrumblefoot.14}",
                  List.of())),
          "GhundargRumblefootNPC",
          new NpcSpec.CombatProfile(62, 3142, 77, 70, 70, 31, 754, 258, "1d94+72"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onDeath(NpcBehaviorContext c) {

        if (c.flag("TITLE_MORDRICK") != 2) {

          c.flag("KILLED_THE_ORC_LEADER", 3);

          c.giveItem("plans");
        }
      }

      @Override
      public void onAttacked(NpcBehaviorContext c) {

        int roll = 1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(65);

        int count;

        if (roll == 2 || roll == 3) {

          count = 1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(2);

          String monster = roll == 2 ? "Orc Guardian" : "Failed Summon";

          for (int i = 0; i < count; i++) c.summon(monster, c.npcTileX() - 4, c.npcTileY() - 4, 0);

        } else if (roll == 4) c.summon("Poisonous Snake", c.npcTileX() - 3, c.npcTileY() - 3, 0);
        else if (roll == 5 || roll == 6) {

          count =
              roll == 5
                  ? 1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(4)
                  : 1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(3);

          for (int i = 0; i < count; i++)
            c.summon("Poisonous Snake", c.npcTileX() - 3, c.npcTileY() - 3, 0);
        }
      }

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        int flag = c.flag("KILLED_THE_ORC_LEADER");

        if (flag > 0) {

          c.sayKey("npc.ghundarg.prepare");

          c.flag("KILLED_THE_ORC_LEADER", flag - 1);

          c.npc().provoke();

        } else c.sayKey("npc.ghundarg.welcome");
      }
    };
  }

  public GhundargRumblefoot(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
