package com.perso.T4C.npc.stoneheim;

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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "DelnarSteelblade", x = 315, y = 700, z = 0, stationary = false, aggressive = false)
public final class DelnarSteelblade extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DelnarSteelblade";

  public static final String DISPLAY_NAME = "${npc.delnarsteelblade}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupNormalSword"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.delnarsteelblade}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delnarsteelblade.0.0}",
                      "${npc.topic_keyword.delnarsteelblade.0.1}",
                      "${npc.topic_keyword.delnarsteelblade.0.2}"),
                  "${npc.topic.delnarsteelblade.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delnarsteelblade.1.0}",
                      "${npc.topic_keyword.delnarsteelblade.1.1}"),
                  "${npc.topic.delnarsteelblade.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delnarsteelblade.2.0}",
                      "${npc.topic_keyword.delnarsteelblade.2.1}",
                      "${npc.topic_keyword.delnarsteelblade.2.2}",
                      "${npc.topic_keyword.delnarsteelblade.2.3}",
                      "${npc.topic_keyword.delnarsteelblade.2.4}"),
                  "${npc.topic.delnarsteelblade.2}",
                  List.of())),
          "DelnarSteelbladeNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int roll = (int) (Math.random() * 21);

        if (roll <= 2) c.shoutKey("npc.delnarsteelblade.attack." + roll);
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int roll = (int) (Math.random() * 21);

        if (roll <= 2) c.shoutKey("npc.delnarsteelblade.attacked." + roll);
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if ((int) (Math.random() * 3) == 0) c.shoutKey("npc.delnarsteelblade.death");
      }
    };
  }

  public DelnarSteelblade(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
