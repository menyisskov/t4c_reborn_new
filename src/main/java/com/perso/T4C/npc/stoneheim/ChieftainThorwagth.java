package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class ChieftainThorwagth extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ChieftainThorwagth";

  public static final String DISPLAY_NAME = "${npc.chieftainthorwagth}";

  public static final String SPRITE_BASE = "64kSkavenSkavenger";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.chieftainthorwagth}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainthorwagth.0.0}",
                      "${npc.topic_keyword.chieftainthorwagth.0.1}",
                      "${npc.topic_keyword.chieftainthorwagth.0.2}"),
                  "${npc.topic.chieftainthorwagth.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainthorwagth.1.0}",
                      "${npc.topic_keyword.chieftainthorwagth.1.1}",
                      "${npc.topic_keyword.chieftainthorwagth.1.2}"),
                  "${npc.topic.chieftainthorwagth.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainthorwagth.2.0}",
                      "${npc.topic_keyword.chieftainthorwagth.2.1}",
                      "${npc.topic_keyword.chieftainthorwagth.2.2}",
                      "${npc.topic_keyword.chieftainthorwagth.2.3}"),
                  "${npc.topic.chieftainthorwagth.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainthorwagth.3.0}",
                      "${npc.topic_keyword.chieftainthorwagth.3.1}",
                      "${npc.topic_keyword.chieftainthorwagth.3.2}"),
                  "${npc.topic.chieftainthorwagth.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainthorwagth.4.0}",
                      "${npc.topic_keyword.chieftainthorwagth.4.1}"),
                  "${npc.topic.chieftainthorwagth.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainthorwagth.5.0}",
                      "${npc.topic_keyword.chieftainthorwagth.5.1}",
                      "${npc.topic_keyword.chieftainthorwagth.5.2}",
                      "${npc.topic_keyword.chieftainthorwagth.5.3}",
                      "${npc.topic_keyword.chieftainthorwagth.5.4}"),
                  "${npc.topic.chieftainthorwagth.5}",
                  List.of())),
          "ChieftainThorwagthNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        StaticDialogueBehavior.INSTANCE.onConversationStart(c);
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("FUCK") || k.equals("SUCK") || k.equals("ASSHOLE") || k.trim().equals("ASS")) {

          c.sayKey("${npc.topic.chieftainthorwagth.2}");

          c.npc().provoke();

          return true;
        }

        if ((k.contains("VILLAIN") || k.contains("SKULL"))
            && c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") != 1) {

          c.sayKey("${npc.topic.chieftainthorwagth.4}");

          c.npc().provoke();

          return true;
        }

        return false;
      }
    };
  }

  public ChieftainThorwagth(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
