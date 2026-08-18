package com.perso.T4C.npc.classic;

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

public final class Fali extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Fali";

  public static final String DISPLAY_NAME = "${npc.fali}";

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
          "${npc.welcome.fali}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.fali.0.0}", "${npc.topic_keyword.fali.0.1}"),
                  "${npc.topic.fali.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.fali.1.0}",
                      "${npc.topic_keyword.fali.1.1}",
                      "${npc.topic_keyword.fali.1.2}"),
                  "${npc.topic.fali.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.fali.2.0}"), "${npc.topic.fali.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.fali.3.0}",
                      "${npc.topic_keyword.fali.3.1}",
                      "${npc.topic_keyword.fali.3.2}"),
                  "${npc.topic.fali.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.fali.4.0}"), "${npc.topic.fali.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.fali.5.0}", "${npc.topic_keyword.fali.5.1}"),
                  "${npc.topic.fali.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.fali.6.0}", "${npc.topic_keyword.fali.6.1}"),
                  "${npc.topic.fali.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.fali.7.0}"), "${npc.topic.fali.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.fali.8.0}",
                      "${npc.topic_keyword.fali.8.1}",
                      "${npc.topic_keyword.fali.8.2}",
                      "${npc.topic_keyword.fali.8.3}"),
                  "${npc.topic.fali.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.fali.9.0}",
                      "${npc.topic_keyword.fali.9.1}",
                      "${npc.topic_keyword.fali.9.2}",
                      "${npc.topic_keyword.fali.9.3}",
                      "${npc.topic_keyword.fali.9.4}"),
                  "${npc.topic.fali.9}",
                  List.of())),
          "FaliNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public Fali(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
