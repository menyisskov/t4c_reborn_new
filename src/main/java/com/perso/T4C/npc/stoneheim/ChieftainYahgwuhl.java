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

public final class ChieftainYahgwuhl extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ChieftainYahgwuhl";

  public static final String DISPLAY_NAME = "${npc.chieftainyahgwuhl}";

  public static final String SPRITE_BASE = "64kSkavenWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.chieftainyahgwuhl}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainyahgwuhl.0.0}",
                      "${npc.topic_keyword.chieftainyahgwuhl.0.1}"),
                  "${npc.topic.chieftainyahgwuhl.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainyahgwuhl.1.0}",
                      "${npc.topic_keyword.chieftainyahgwuhl.1.1}",
                      "${npc.topic_keyword.chieftainyahgwuhl.1.2}"),
                  "${npc.topic.chieftainyahgwuhl.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainyahgwuhl.2.0}",
                      "${npc.topic_keyword.chieftainyahgwuhl.2.1}",
                      "${npc.topic_keyword.chieftainyahgwuhl.2.2}"),
                  "${npc.topic.chieftainyahgwuhl.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainyahgwuhl.3.0}",
                      "${npc.topic_keyword.chieftainyahgwuhl.3.1}"),
                  "${npc.topic.chieftainyahgwuhl.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainyahgwuhl.4.0}",
                      "${npc.topic_keyword.chieftainyahgwuhl.4.1}"),
                  "${npc.topic.chieftainyahgwuhl.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainyahgwuhl.5.0}",
                      "${npc.topic_keyword.chieftainyahgwuhl.5.1}",
                      "${npc.topic_keyword.chieftainyahgwuhl.5.2}",
                      "${npc.topic_keyword.chieftainyahgwuhl.5.3}"),
                  "${npc.topic.chieftainyahgwuhl.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chieftainyahgwuhl.6.0}",
                      "${npc.topic_keyword.chieftainyahgwuhl.6.1}",
                      "${npc.topic_keyword.chieftainyahgwuhl.6.2}",
                      "${npc.topic_keyword.chieftainyahgwuhl.6.3}",
                      "${npc.topic_keyword.chieftainyahgwuhl.6.4}"),
                  "${npc.topic.chieftainyahgwuhl.6}",
                  List.of())),
          "ChieftainYahgwuhlNPC",
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

          c.sayKey("${npc.topic.chieftainyahgwuhl.5}");

          c.npc().provoke();

          return true;
        }

        if ((k.contains("VILLAIN") || k.contains("SKULL"))
            && c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") != 1) {

          c.sayKey("${npc.topic.chieftainyahgwuhl.3}");

          c.npc().provoke();

          return true;
        }

        return false;
      }
    };
  }

  public ChieftainYahgwuhl(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
