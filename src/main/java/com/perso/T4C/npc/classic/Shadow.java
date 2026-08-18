package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.TrainingAndFleeBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Shadow", x = 2888, y = 1123, z = 0, stationary = false, aggressive = false)
public final class Shadow extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Shadow";

  public static final String DISPLAY_NAME = "${npc.shadow}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.shadow}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadow.0.0}", "${npc.topic_keyword.shadow.0.1}"),
                  "${npc.topic.shadow.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shadow.1.0}",
                      "${npc.topic_keyword.shadow.1.1}",
                      "${npc.topic_keyword.shadow.1.2}"),
                  "${npc.topic.shadow.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadow.2.0}"), "${npc.topic.shadow.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shadow.3.0}",
                      "${npc.topic_keyword.shadow.3.1}",
                      "${npc.topic_keyword.shadow.3.2}"),
                  "${npc.topic.shadow.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadow.4.0}"), "${npc.topic.shadow.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadow.5.0}"), "${npc.topic.shadow.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadow.6.0}"), "${npc.topic.shadow.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadow.7.0}", "${npc.topic_keyword.shadow.7.1}"),
                  "${npc.topic.shadow.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.shadow.8.0}"), "${npc.topic.shadow.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.shadow.9.0}",
                      "${npc.topic_keyword.shadow.9.1}",
                      "${npc.topic_keyword.shadow.9.2}",
                      "${npc.topic_keyword.shadow.9.3}",
                      "${npc.topic_keyword.shadow.9.4}"),
                  "${npc.topic.shadow.9}",
                  List.of())),
          "ShadowNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 65, 65, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new TrainingAndFleeBehavior(
        true, List.of(new LearnScreen.TrainingOffer("peek", 1, 500, true)), "npc.markam.attacked.");
  }

  public Shadow(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
