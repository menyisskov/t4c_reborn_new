package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Rondy extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Rondy";

  public static final String DISPLAY_NAME = "${npc.rondy}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.rondy}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rondy.0.0}",
                      "${npc.topic_keyword.rondy.0.1}",
                      "${npc.topic_keyword.rondy.0.2}"),
                  "${npc.topic.rondy.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.1.0}", "${npc.topic_keyword.rondy.1.1}"),
                  "${npc.topic.rondy.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.2.0}"), "${npc.topic.rondy.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.3.0}"), "${npc.topic.rondy.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.4.0}"), "${npc.topic.rondy.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.5.0}"), "${npc.topic.rondy.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.6.0}"), "${npc.topic.rondy.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.7.0}"), "${npc.topic.rondy.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.8.0}"), "${npc.topic.rondy.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.9.0}"), "${npc.topic.rondy.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.10.0}"), "${npc.topic.rondy.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rondy.11.0}"), "${npc.topic.rondy.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rondy.12.0}",
                      "${npc.topic_keyword.rondy.12.1}",
                      "${npc.topic_keyword.rondy.12.2}",
                      "${npc.topic_keyword.rondy.12.3}"),
                  "${npc.topic.rondy.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rondy.13.0}",
                      "${npc.topic_keyword.rondy.13.1}",
                      "${npc.topic_keyword.rondy.13.2}",
                      "${npc.topic_keyword.rondy.13.3}",
                      "${npc.topic_keyword.rondy.13.4}"),
                  "${npc.topic.rondy.13}",
                  List.of())),
          "RondyNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.trim().toUpperCase(java.util.Locale.ROOT);

        if (!k.matches("GUESS\\s+[-+]?\\d+")) return false;

        int guess;

        try {

          guess = Integer.parseInt(k.split("\\s+")[1]);

        } catch (RuntimeException e) {

          guess = 0;
        }

        int answer = c.flag("__QUEST_VAULT_CODE_ONE");

        if (answer == 0) c.sayKey("npc.rondy.guess.unavailable");
        else if (guess < 1 || guess > 50) c.sayKey("npc.rondy.guess.range");
        else if (c.player().getGold() < 1000) c.sayKey("npc.rondy.guess.poor");
        else {

          c.player().addGold(-1000);

          c.sayKey(
              guess == answer
                  ? "npc.rondy.guess.correct"
                  : guess < answer ? "npc.rondy.guess.higher" : "npc.rondy.guess.lower");
        }

        return true;
      }
    };
  }

  public Rondy(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
