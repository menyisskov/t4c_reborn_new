package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "WoundedGuard", x = 1787, y = 1293, z = 0, stationary = false, aggressive = false)
public final class WoundedGuard extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "WoundedGuard";

  public static final String DISPLAY_NAME = "${npc.woundedguard}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.woundedguard}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.woundedguard.0.0}",
                      "${npc.topic_keyword.woundedguard.0.1}"),
                  "${npc.topic.woundedguard.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.woundedguard.1.0}"),
                  "${npc.topic.woundedguard.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.woundedguard.2.0}",
                      "${npc.topic_keyword.woundedguard.2.1}",
                      "${npc.topic_keyword.woundedguard.2.2}"),
                  "${npc.topic.woundedguard.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.woundedguard.3.0}",
                      "${npc.topic_keyword.woundedguard.3.1}"),
                  "${npc.topic.woundedguard.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.woundedguard.4.0}"),
                  "${npc.topic.woundedguard.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.woundedguard.5.0}",
                      "${npc.topic_keyword.woundedguard.5.1}",
                      "${npc.topic_keyword.woundedguard.5.2}"),
                  "${npc.topic.woundedguard.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.woundedguard.6.0}",
                      "${npc.topic_keyword.woundedguard.6.1}",
                      "${npc.topic_keyword.woundedguard.6.2}",
                      "${npc.topic_keyword.woundedguard.6.3}",
                      "${npc.topic_keyword.woundedguard.6.4}"),
                  "${npc.topic.woundedguard.6}",
                  List.of())),
          "Guard_One",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 310, 65535, "1d29+21"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public WoundedGuard(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
