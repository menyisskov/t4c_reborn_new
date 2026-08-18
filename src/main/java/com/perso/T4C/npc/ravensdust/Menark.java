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

public final class Menark extends ScriptedNpc {

  public static final String ID = "Menark";

  public static final String DISPLAY_NAME = "${npc.menark}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.menark}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.0.0}", "${npc.topic_keyword.menark.0.1}"),
                  "${npc.topic.menark.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.1.0}"), "${npc.topic.menark.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.menark.2.0}",
                      "${npc.topic_keyword.menark.2.1}",
                      "${npc.topic_keyword.menark.2.2}"),
                  "${npc.topic.menark.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.3.0}", "${npc.topic_keyword.menark.3.1}"),
                  "${npc.topic.menark.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.4.0}"), "${npc.topic.menark.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.5.0}", "${npc.topic_keyword.menark.5.1}"),
                  "${npc.topic.menark.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.6.0}"), "${npc.topic.menark.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.7.0}"), "${npc.topic.menark.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.8.0}"), "${npc.topic.menark.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.9.0}"), "${npc.topic.menark.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.10.0}"), "${npc.topic.menark.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.11.0}"), "${npc.topic.menark.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.12.0}", "${npc.topic_keyword.menark.12.1}"),
                  "${npc.topic.menark.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.menark.13.0}"), "${npc.topic.menark.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.menark.14.0}",
                      "${npc.topic_keyword.menark.14.1}",
                      "${npc.topic_keyword.menark.14.2}"),
                  "${npc.topic.menark.14}",
                  List.of())),
          "Mage",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (c.globalFlag("GLOBAL_BANK_HAS_BEEN_ROBBED") == c.flag("__FLAG_SHADEEN_PLAYER_B")
            && c.flag("__FLAG_SHADEEN_PLAYER_B") != 0) c.sayKey("npc.menark.thief");
        else if (c.flag("__QUEST_DAMIEN_SUBPLOT") == 2) c.sayKey("npc.menark.visitor");
        else c.sayKey("npc.menark.welcome");
      }
    };
  }

  public Menark(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
