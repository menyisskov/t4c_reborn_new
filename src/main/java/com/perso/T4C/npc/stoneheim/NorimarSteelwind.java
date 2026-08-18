package com.perso.T4C.npc.stoneheim;

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

public final class NorimarSteelwind extends ScriptedNpc {

  public static final String ID = "NorimarSteelwind";

  public static final String DISPLAY_NAME = "${npc.norimarsteelwind}";

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
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.norimarsteelwind}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.norimarsteelwind.0.0}",
                      "${npc.topic_keyword.norimarsteelwind.0.1}"),
                  "${npc.topic.norimarsteelwind.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.norimarsteelwind.1.0}",
                      "${npc.topic_keyword.norimarsteelwind.1.1}"),
                  "${npc.topic.norimarsteelwind.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.norimarsteelwind.2.0}",
                      "${npc.topic_keyword.norimarsteelwind.2.1}"),
                  "${npc.topic.norimarsteelwind.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.norimarsteelwind.3.0}",
                      "${npc.topic_keyword.norimarsteelwind.3.1}",
                      "${npc.topic_keyword.norimarsteelwind.3.2}"),
                  "${npc.topic.norimarsteelwind.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.norimarsteelwind.4.0}",
                      "${npc.topic_keyword.norimarsteelwind.4.1}"),
                  "${npc.topic.norimarsteelwind.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.norimarsteelwind.5.0}",
                      "${npc.topic_keyword.norimarsteelwind.5.1}"),
                  "${npc.topic.norimarsteelwind.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.norimarsteelwind.6.0}"),
                  "${npc.topic.norimarsteelwind.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.norimarsteelwind.7.0}"),
                  "${npc.topic.norimarsteelwind.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.norimarsteelwind.8.0}"),
                  "${npc.topic.norimarsteelwind.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.norimarsteelwind.9.0}"),
                  "${npc.topic.norimarsteelwind.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.norimarsteelwind.10.0}"),
                  "${npc.topic.norimarsteelwind.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.norimarsteelwind.11.0}"),
                  "${npc.topic.norimarsteelwind.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.norimarsteelwind.12.0}",
                      "${npc.topic_keyword.norimarsteelwind.12.1}"),
                  "${npc.topic.norimarsteelwind.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.norimarsteelwind.13.0}",
                      "${npc.topic_keyword.norimarsteelwind.13.1}",
                      "${npc.topic_keyword.norimarsteelwind.13.2}",
                      "${npc.topic_keyword.norimarsteelwind.13.3}",
                      "${npc.topic_keyword.norimarsteelwind.13.4}"),
                  "${npc.topic.norimarsteelwind.13}",
                  List.of())),
          "NorimarSteelwindNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new BankBehavior("__FLAG_BANK_OF_WINDHOWL");
  }

  public NorimarSteelwind(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
