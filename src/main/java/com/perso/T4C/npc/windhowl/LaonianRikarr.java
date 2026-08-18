package com.perso.T4C.npc.windhowl;

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

public final class LaonianRikarr extends ScriptedNpc {

  public static final String ID = "LaonianRikarr";

  public static final String DISPLAY_NAME = "${npc.laonianrikarr}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupMorningStar")),
          0,
          List.of(),
          "${npc.welcome.laonianrikarr}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.laonianrikarr.0.0}",
                      "${npc.topic_keyword.laonianrikarr.0.1}"),
                  "${npc.topic.laonianrikarr.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.laonianrikarr.1.0}",
                      "${npc.topic_keyword.laonianrikarr.1.1}",
                      "${npc.topic_keyword.laonianrikarr.1.2}"),
                  "${npc.topic.laonianrikarr.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.laonianrikarr.2.0}",
                      "${npc.topic_keyword.laonianrikarr.2.1}",
                      "${npc.topic_keyword.laonianrikarr.2.2}"),
                  "${npc.topic.laonianrikarr.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laonianrikarr.3.0}"),
                  "${npc.topic.laonianrikarr.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laonianrikarr.4.0}"),
                  "${npc.topic.laonianrikarr.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.laonianrikarr.5.0}"),
                  "${npc.topic.laonianrikarr.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.laonianrikarr.6.0}",
                      "${npc.topic_keyword.laonianrikarr.6.1}",
                      "${npc.topic_keyword.laonianrikarr.6.2}",
                      "${npc.topic_keyword.laonianrikarr.6.3}",
                      "${npc.topic_keyword.laonianrikarr.6.4}"),
                  "${npc.topic.laonianrikarr.6}",
                  List.of())),
          "LaonianRikarrNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public LaonianRikarr(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
