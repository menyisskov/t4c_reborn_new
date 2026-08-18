package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class Venadar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Venadar";

  public static final String DISPLAY_NAME = "${npc.venadar}";

  public static final String SPRITE_BASE = "Taunting";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.venadar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.venadar.0.0}", "${npc.topic_keyword.venadar.0.1}"),
                  "${npc.topic.venadar.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.venadar.1.0}"), "${npc.topic.venadar.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.venadar.2.0}",
                      "${npc.topic_keyword.venadar.2.1}",
                      "${npc.topic_keyword.venadar.2.2}"),
                  "${npc.topic.venadar.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.venadar.3.0}", "${npc.topic_keyword.venadar.3.1}"),
                  "${npc.topic.venadar.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.venadar.4.0}", "${npc.topic_keyword.venadar.4.1}"),
                  "${npc.topic.venadar.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.venadar.5.0}"), "${npc.topic.venadar.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.venadar.6.0}"), "${npc.topic.venadar.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.venadar.7.0}",
                      "${npc.topic_keyword.venadar.7.1}",
                      "${npc.topic_keyword.venadar.7.2}",
                      "${npc.topic_keyword.venadar.7.3}"),
                  "${npc.topic.venadar.7}",
                  List.of())),
          "VenadarNPC",
          new NpcSpec.CombatProfile(100, 1879, 85, 77, 77, 35, 850, 290, "1d110+84"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (c.flag("__FLAG_USER_HAS_KILLED_VENADAR") == 1
            && c.flag("__FLAG_QUEST_FOR_BONES") == 1) {

          c.sayKey("npc.venadar.ring_reward");

          c.giveItem("ancient_ring");

          c.flag("__FLAG_QUEST_FOR_BONES", 2);

          c.systemMessageKey("message.venadar.ring");

        } else {

          c.sayKey("npc.venadar.welcome");
        }
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if ((k.equals("TAINT") || k.equals("PRESENCE"))) {

          c.sayKey(
              c.flag("__FLAG_QUEST_FOR_BONES") >= 1
                  ? "npc.venadar.taint.bones"
                  : "npc.venadar.taint.relic");

          return true;
        }

        if (k.equals("BONES") || k.equals("OGRIMAR")) {

          if (c.flag("__FLAG_QUEST_FOR_BONES") >= 1) c.sayKey("npc.venadar.bones");
          else c.sayKey("npc.venadar.forbidden");

          return true;
        }

        if (k.equals("OBJECT")) {

          c.sayKey(
              c.flag("__FLAG_QUEST_FOR_BONES") >= 1
                  ? "npc.venadar.object"
                  : "npc.venadar.forbidden");

          return true;
        }

        if (k.equals("RING")) {

          if (c.flag("__FLAG_USER_HAS_KILLED_VENADAR") == 1
              && c.flag("__FLAG_QUEST_FOR_BONES") >= 1) c.sayKey("npc.venadar.ring_given");
          else if (c.flag("__FLAG_QUEST_FOR_BONES") == 1) {

            c.sayKey("npc.venadar.ring_fight");

            c.npc().provoke();

          } else c.sayKey("npc.venadar.forbidden");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public void onDeath(NpcBehaviorContext c) {

        c.flag("__FLAG_USER_HAS_KILLED_VENADAR", 1);
      }
    };
  }

  public Venadar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
