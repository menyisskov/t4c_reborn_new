package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.BankBehavior;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class TalonIrongaze extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "TalonIrongaze";

  public static final String DISPLAY_NAME = "${npc.talonirongaze}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.talonirongaze}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.talonirongaze.0.0}",
                      "${npc.topic_keyword.talonirongaze.0.1}"),
                  "${npc.topic.talonirongaze.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.talonirongaze.1.0}",
                      "${npc.topic_keyword.talonirongaze.1.1}",
                      "${npc.topic_keyword.talonirongaze.1.2}"),
                  "${npc.topic.talonirongaze.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.talonirongaze.2.0}"),
                  "${npc.topic.talonirongaze.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.talonirongaze.3.0}"),
                  "${npc.topic.talonirongaze.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.talonirongaze.4.0}"),
                  "${npc.topic.talonirongaze.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.talonirongaze.5.0}"),
                  "${npc.topic.talonirongaze.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.talonirongaze.6.0}",
                      "${npc.topic_keyword.talonirongaze.6.1}",
                      "${npc.topic_keyword.talonirongaze.6.2}"),
                  "${npc.topic.talonirongaze.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.talonirongaze.7.0}",
                      "${npc.topic_keyword.talonirongaze.7.1}"),
                  "${npc.topic.talonirongaze.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.talonirongaze.8.0}",
                      "${npc.topic_keyword.talonirongaze.8.1}",
                      "${npc.topic_keyword.talonirongaze.8.2}",
                      "${npc.topic_keyword.talonirongaze.8.3}"),
                  "${npc.topic.talonirongaze.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.talonirongaze.9.0}"),
                  "${npc.topic.talonirongaze.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.talonirongaze.10.0}"),
                  "${npc.topic.talonirongaze.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.talonirongaze.11.0}"),
                  "${npc.topic.talonirongaze.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.talonirongaze.12.0}"),
                  "${npc.topic.talonirongaze.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.talonirongaze.13.0}"),
                  "${npc.topic.talonirongaze.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.talonirongaze.14.0}",
                      "${npc.topic_keyword.talonirongaze.14.1}",
                      "${npc.topic_keyword.talonirongaze.14.2}",
                      "${npc.topic_keyword.talonirongaze.14.3}",
                      "${npc.topic_keyword.talonirongaze.14.4}"),
                  "${npc.topic.talonirongaze.14}",
                  List.of())),
          "Warrior",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new BankBehavior("__FLAG_BANK_OF_WINDHOWL");
  }

  public TalonIrongaze(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
