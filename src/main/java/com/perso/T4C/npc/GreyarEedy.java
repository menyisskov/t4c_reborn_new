package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.BankBehavior;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "GreyarEedy", x = 1620, y = 1209, z = 0, stationary = false, aggressive = false)
public final class GreyarEedy extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "GreyarEedy";

  public static final String DISPLAY_NAME = "${npc.greyareedy}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.greyareedy}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.greyareedy.0.0}", "${npc.topic_keyword.greyareedy.0.1}"),
                  "${npc.topic.greyareedy.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.greyareedy.1.0}",
                      "${npc.topic_keyword.greyareedy.1.1}",
                      "${npc.topic_keyword.greyareedy.1.2}"),
                  "${npc.topic.greyareedy.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyareedy.2.0}"),
                  "${npc.topic.greyareedy.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.greyareedy.3.0}",
                      "${npc.topic_keyword.greyareedy.3.1}",
                      "${npc.topic_keyword.greyareedy.3.2}"),
                  "${npc.topic.greyareedy.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyareedy.4.0}"),
                  "${npc.topic.greyareedy.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyareedy.5.0}"),
                  "${npc.topic.greyareedy.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyareedy.6.0}"),
                  "${npc.topic.greyareedy.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyareedy.7.0}"),
                  "${npc.topic.greyareedy.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyareedy.8.0}"),
                  "${npc.topic.greyareedy.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyareedy.9.0}"),
                  "${npc.topic.greyareedy.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.greyareedy.10.0}",
                      "${npc.topic_keyword.greyareedy.10.1}",
                      "${npc.topic_keyword.greyareedy.10.2}",
                      "${npc.topic_keyword.greyareedy.10.3}",
                      "${npc.topic_keyword.greyareedy.10.4}"),
                  "${npc.topic.greyareedy.10}",
                  List.of())),
          "GreyarEedyNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new BankBehavior("__FLAG_BANK_OF_WINDHOWL");
  }

  public GreyarEedy(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
