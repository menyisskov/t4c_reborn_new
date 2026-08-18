package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Kederic extends ScriptedNpc {

  public static final String ID = "Kederic";

  public static final String DISPLAY_NAME = "${npc.kederic}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupMageRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.kederic}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kederic.0.0}", "${npc.topic_keyword.kederic.0.1}"),
                  "${npc.topic.kederic.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kederic.1.0}", "${npc.topic_keyword.kederic.1.1}"),
                  "${npc.topic.kederic.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kederic.2.0}"), "${npc.topic.kederic.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kederic.3.0}",
                      "${npc.topic_keyword.kederic.3.1}",
                      "${npc.topic_keyword.kederic.3.2}"),
                  "${npc.topic.kederic.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kederic.4.0}", "${npc.topic_keyword.kederic.4.1}"),
                  "${npc.topic.kederic.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kederic.5.0}"), "${npc.topic.kederic.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kederic.6.0}",
                      "${npc.topic_keyword.kederic.6.1}",
                      "${npc.topic_keyword.kederic.6.2}"),
                  "${npc.topic.kederic.6}",
                  List.of())),
          "Elder_Druid",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public Kederic(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
