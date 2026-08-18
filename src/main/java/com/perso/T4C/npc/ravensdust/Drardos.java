package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Drardos extends ScriptedNpc {

  public static final String ID = "Drardos";

  public static final String DISPLAY_NAME = "${npc.drardos}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.drardos}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.0.0}", "${npc.topic_keyword.drardos.0.1}"),
                  "${npc.topic.drardos.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.1.0}"), "${npc.topic.drardos.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.2.0}"), "${npc.topic.drardos.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.3.0}"), "${npc.topic.drardos.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.4.0}", "${npc.topic_keyword.drardos.4.1}"),
                  "${npc.topic.drardos.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.drardos.5.0}",
                      "${npc.topic_keyword.drardos.5.1}",
                      "${npc.topic_keyword.drardos.5.2}"),
                  "${npc.topic.drardos.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.6.0}"), "${npc.topic.drardos.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.7.0}"), "${npc.topic.drardos.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.8.0}"), "${npc.topic.drardos.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.9.0}"), "${npc.topic.drardos.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.10.0}", "${npc.topic_keyword.drardos.10.1}"),
                  "${npc.topic.drardos.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drardos.11.0}"),
                  "${npc.topic.drardos.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.drardos.12.0}",
                      "${npc.topic_keyword.drardos.12.1}",
                      "${npc.topic_keyword.drardos.12.2}",
                      "${npc.topic_keyword.drardos.12.3}",
                      "${npc.topic_keyword.drardos.12.4}"),
                  "${npc.topic.drardos.12}",
                  List.of())),
          "DrardosNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.sayKey(
            c.flag("__FLAG_QUEST_FOR_BONES") == 5 ? "npc.drardos.bones" : "npc.drardos.welcome");

        if (c.flag("__FLAG_QUEST_FOR_BONES") == 5) c.flag("__FLAG_QUEST_FOR_BONES", 6);
      }
    };
  }

  public Drardos(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
