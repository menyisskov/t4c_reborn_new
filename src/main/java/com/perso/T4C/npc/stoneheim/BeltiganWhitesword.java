package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(
    type = "BeltiganWhitesword",
    x = 395,
    y = 1265,
    z = 0,
    stationary = false,
    aggressive = false)
public final class BeltiganWhitesword extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "BeltiganWhitesword";

  public static final String DISPLAY_NAME = "${npc.beltiganwhitesword}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupPlateBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupPlateLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupPlateHelm"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupPlateGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupPlateGloveL")),
          0,
          List.of(),
          "${npc.welcome.beltiganwhitesword}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.beltiganwhitesword.0.0}",
                      "${npc.topic_keyword.beltiganwhitesword.0.1}"),
                  "${npc.topic.beltiganwhitesword.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.beltiganwhitesword.1.0}",
                      "${npc.topic_keyword.beltiganwhitesword.1.1}",
                      "${npc.topic_keyword.beltiganwhitesword.1.2}"),
                  "${npc.topic.beltiganwhitesword.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.2.0}"),
                  "${npc.topic.beltiganwhitesword.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.3.0}"),
                  "${npc.topic.beltiganwhitesword.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.beltiganwhitesword.4.0}",
                      "${npc.topic_keyword.beltiganwhitesword.4.1}"),
                  "${npc.topic.beltiganwhitesword.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.beltiganwhitesword.5.0}",
                      "${npc.topic_keyword.beltiganwhitesword.5.1}"),
                  "${npc.topic.beltiganwhitesword.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.6.0}"),
                  "${npc.topic.beltiganwhitesword.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.7.0}"),
                  "${npc.topic.beltiganwhitesword.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.beltiganwhitesword.8.0}",
                      "${npc.topic_keyword.beltiganwhitesword.8.1}",
                      "${npc.topic_keyword.beltiganwhitesword.8.2}"),
                  "${npc.topic.beltiganwhitesword.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.9.0}"),
                  "${npc.topic.beltiganwhitesword.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.10.0}"),
                  "${npc.topic.beltiganwhitesword.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.11.0}"),
                  "${npc.topic.beltiganwhitesword.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.12.0}"),
                  "${npc.topic.beltiganwhitesword.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.13.0}"),
                  "${npc.topic.beltiganwhitesword.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.14.0}"),
                  "${npc.topic.beltiganwhitesword.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.15.0}"),
                  "${npc.topic.beltiganwhitesword.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.16.0}"),
                  "${npc.topic.beltiganwhitesword.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.17.0}"),
                  "${npc.topic.beltiganwhitesword.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.18.0}"),
                  "${npc.topic.beltiganwhitesword.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.19.0}"),
                  "${npc.topic.beltiganwhitesword.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.20.0}"),
                  "${npc.topic.beltiganwhitesword.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.21.0}"),
                  "${npc.topic.beltiganwhitesword.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.22.0}"),
                  "${npc.topic.beltiganwhitesword.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.23.0}"),
                  "${npc.topic.beltiganwhitesword.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.24.0}"),
                  "${npc.topic.beltiganwhitesword.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.beltiganwhitesword.25.0}",
                      "${npc.topic_keyword.beltiganwhitesword.25.1}"),
                  "${npc.topic.beltiganwhitesword.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.26.0}"),
                  "${npc.topic.beltiganwhitesword.26}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.27.0}"),
                  "${npc.topic.beltiganwhitesword.27}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.28.0}"),
                  "${npc.topic.beltiganwhitesword.28}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.29.0}"),
                  "${npc.topic.beltiganwhitesword.29}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.30.0}"),
                  "${npc.topic.beltiganwhitesword.30}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.31.0}"),
                  "${npc.topic.beltiganwhitesword.31}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.32.0}"),
                  "${npc.topic.beltiganwhitesword.32}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.beltiganwhitesword.33.0}",
                      "${npc.topic_keyword.beltiganwhitesword.33.1}",
                      "${npc.topic_keyword.beltiganwhitesword.33.2}",
                      "${npc.topic_keyword.beltiganwhitesword.33.3}"),
                  "${npc.topic.beltiganwhitesword.33}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.beltiganwhitesword.34.0}",
                      "${npc.topic_keyword.beltiganwhitesword.34.1}",
                      "${npc.topic_keyword.beltiganwhitesword.34.2}",
                      "${npc.topic_keyword.beltiganwhitesword.34.3}"),
                  "${npc.topic.beltiganwhitesword.34}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.beltiganwhitesword.35.0}"),
                  "${npc.topic.beltiganwhitesword.35}",
                  List.of())),
          "BeltiganWhiteswordNPC",
          new NpcSpec.CombatProfile(100, 1000000, 50, 46, 46, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int roll = (int) (Math.random() * 10);

        if (roll < 2) c.shoutKey("npc.beltiganwhitesword.attacked." + roll);
      }
    };
  }

  public BeltiganWhitesword(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
