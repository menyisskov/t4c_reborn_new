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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "DelvarIrongrip", x = 2407, y = 807, z = 0, stationary = false, aggressive = false)
public final class DelvarIrongrip extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DelvarIrongrip";

  public static final String DISPLAY_NAME = "${npc.delvarirongrip}";

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
          "${npc.welcome.delvarirongrip}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delvarirongrip.0.0}",
                      "${npc.topic_keyword.delvarirongrip.0.1}"),
                  "${npc.topic.delvarirongrip.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delvarirongrip.1.0}",
                      "${npc.topic_keyword.delvarirongrip.1.1}"),
                  "${npc.topic.delvarirongrip.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.2.0}"),
                  "${npc.topic.delvarirongrip.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delvarirongrip.3.0}",
                      "${npc.topic_keyword.delvarirongrip.3.1}",
                      "${npc.topic_keyword.delvarirongrip.3.2}"),
                  "${npc.topic.delvarirongrip.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delvarirongrip.4.0}",
                      "${npc.topic_keyword.delvarirongrip.4.1}"),
                  "${npc.topic.delvarirongrip.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.5.0}"),
                  "${npc.topic.delvarirongrip.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delvarirongrip.6.0}",
                      "${npc.topic_keyword.delvarirongrip.6.1}",
                      "${npc.topic_keyword.delvarirongrip.6.2}"),
                  "${npc.topic.delvarirongrip.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.7.0}"),
                  "${npc.topic.delvarirongrip.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.8.0}"),
                  "${npc.topic.delvarirongrip.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.9.0}"),
                  "${npc.topic.delvarirongrip.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.10.0}"),
                  "${npc.topic.delvarirongrip.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.11.0}"),
                  "${npc.topic.delvarirongrip.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.12.0}"),
                  "${npc.topic.delvarirongrip.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.13.0}"),
                  "${npc.topic.delvarirongrip.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.14.0}"),
                  "${npc.topic.delvarirongrip.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.15.0}"),
                  "${npc.topic.delvarirongrip.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.16.0}"),
                  "${npc.topic.delvarirongrip.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delvarirongrip.17.0}",
                      "${npc.topic_keyword.delvarirongrip.17.1}"),
                  "${npc.topic.delvarirongrip.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.18.0}"),
                  "${npc.topic.delvarirongrip.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.19.0}"),
                  "${npc.topic.delvarirongrip.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.20.0}"),
                  "${npc.topic.delvarirongrip.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.21.0}"),
                  "${npc.topic.delvarirongrip.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.22.0}"),
                  "${npc.topic.delvarirongrip.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.23.0}"),
                  "${npc.topic.delvarirongrip.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.delvarirongrip.24.0}"),
                  "${npc.topic.delvarirongrip.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delvarirongrip.25.0}",
                      "${npc.topic_keyword.delvarirongrip.25.1}",
                      "${npc.topic_keyword.delvarirongrip.25.2}",
                      "${npc.topic_keyword.delvarirongrip.25.3}"),
                  "${npc.topic.delvarirongrip.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delvarirongrip.26.0}",
                      "${npc.topic_keyword.delvarirongrip.26.1}",
                      "${npc.topic_keyword.delvarirongrip.26.2}",
                      "${npc.topic_keyword.delvarirongrip.26.3}",
                      "${npc.topic_keyword.delvarirongrip.26.4}"),
                  "${npc.topic.delvarirongrip.26}",
                  List.of())),
          "DelvarIrongripNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (Math.random() < .4)
          c.shoutKey(Math.random() < .5 ? "npc.delvar.attack.die" : "npc.delvar.attack.bleed");
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (Math.random() < .4)
          c.shoutKey(Math.random() < .5 ? "npc.delvar.attacked.ouch" : "npc.delvar.attacked.ow");

        summonGoblinReinforcements(c);
      }

      private void summonGoblinReinforcements(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (java.util.concurrent.ThreadLocalRandom.current().nextInt(20) != 0) return;

        int[][] positions = {
          {2690, 905},
          {2691, 905},
          {2692, 905},
          {2687, 911},
          {2697, 913},
          {2761, 919},
          {2751, 908},
          {2745, 931},
          {2739, 940},
          {2702, 901},
          {2685, 895}
        };

        String[] monsters = {"DORKENROTSMELL", "NPCGOBLINSCOUT", "NPCGOBLINWARRIOR"};

        for (int i = 0; i < positions.length; i++)
          c.summon(monsters[Math.min(i, monsters.length - 1)], positions[i][0], positions[i][1], 0);
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        boolean handled = StaticDialogueBehavior.INSTANCE.onKeyword(c, text);

        String key = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT).trim();

        if ("FUCK".equals(key)
            || "SUCK".equals(key)
            || "ASSHOLE".equals(key)
            || "ASS".equals(key)) {

          c.npc().provoke();

          return true;
        }

        return handled;
      }
    };
  }

  public DelvarIrongrip(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
