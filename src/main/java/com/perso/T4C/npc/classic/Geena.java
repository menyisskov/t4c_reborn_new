package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Geena", x = 2864, y = 1058, z = 0, stationary = false, aggressive = false)
public final class Geena extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Geena";

  public static final String DISPLAY_NAME = "${npc.geena}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.geena}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geena.0.0}", "${npc.topic_keyword.geena.0.1}"),
                  "${npc.topic.geena.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.geena.1.0}",
                      "${npc.topic_keyword.geena.1.1}",
                      "${npc.topic_keyword.geena.1.2}"),
                  "${npc.topic.geena.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geena.2.0}"), "${npc.topic.geena.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geena.3.0}", "${npc.topic_keyword.geena.3.1}"),
                  "${npc.topic.geena.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.geena.4.0}",
                      "${npc.topic_keyword.geena.4.1}",
                      "${npc.topic_keyword.geena.4.2}"),
                  "${npc.topic.geena.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geena.5.0}"), "${npc.topic.geena.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geena.6.0}"), "${npc.topic.geena.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geena.7.0}"), "${npc.topic.geena.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.geena.8.0}",
                      "${npc.topic_keyword.geena.8.1}",
                      "${npc.topic_keyword.geena.8.2}",
                      "${npc.topic_keyword.geena.8.3}",
                      "${npc.topic_keyword.geena.8.4}"),
                  "${npc.topic.geena.8}",
                  List.of())),
          "GeenaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.globalFlag("GLOBAL_QUEST_WHO_IS_INFECTED") == 2) {

          c.npcFlag(
              "GEENA_INFECTED",
              (int) (System.currentTimeMillis() / 1000) + 60 * (4 + (int) (Math.random() * 9)));

          c.globalFlag("GLOBAL_QUEST_WHO_IS_INFECTED", 0);
        }

        int infected = c.npcFlag("GEENA_INFECTED"), now = (int) (System.currentTimeMillis() / 1000);

        if (infected > now) {

          if (c.hasItem("healing_leaf")) {

            c.takeItem("healing_leaf");

            c.flag("__QUEST_GOBLIN_GEENA_GO_SEE_HALAM", 1);

            c.npcFlag("GEENA_INFECTED", 0);

            c.sayKey("npc.geena.cured");

          } else {

            c.flag("__QUEST_CURE_GEENA", 1);

            c.sayKey("npc.geena.ill");
          }

        } else c.sayKey("npc.geena.well");
      }
    };
  }

  public Geena(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
