package com.perso.T4C.npc.windhowl;

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
import java.util.Locale;

public final class Drunk extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Drunk";

  public static final String DISPLAY_NAME = "${npc.drunk}";

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
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1")),
          0,
          List.of(),
          "${npc.welcome.drunk}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drunk.0.0}", "${npc.topic_keyword.drunk.0.1}"),
                  "${npc.topic.drunk.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drunk.1.0}"), "${npc.topic.drunk.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.drunk.2.0}",
                      "${npc.topic_keyword.drunk.2.1}",
                      "${npc.topic_keyword.drunk.2.2}"),
                  "${npc.topic.drunk.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drunk.3.0}"), "${npc.topic.drunk.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drunk.4.0}"), "${npc.topic.drunk.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.drunk.5.0}"), "${npc.topic.drunk.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.drunk.6.0}",
                      "${npc.topic_keyword.drunk.6.1}",
                      "${npc.topic_keyword.drunk.6.2}"),
                  "${npc.topic.drunk.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.drunk.7.0}",
                      "${npc.topic_keyword.drunk.7.1}",
                      "${npc.topic_keyword.drunk.7.2}",
                      "${npc.topic_keyword.drunk.7.3}",
                      "${npc.topic_keyword.drunk.7.4}"),
                  "${npc.topic.drunk.7}",
                  List.of())),
          "DrunkNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.trim().toUpperCase(Locale.ROOT);

        if (!k.matches("GIVE\\s+[-+]?\\d+\\s+GOLD")) return false;

        int amount;

        try {

          amount = Integer.parseInt(k.split("\\s+")[1]);

        } catch (RuntimeException e) {

          amount = 0;
        }

        if (amount < 1) {

          c.sayKey("npc.drunk.give.invalid");

          return true;
        }

        if (c.player().getGold() < amount) {

          c.sayKey("npc.drunk.give.poor");

          return true;
        }

        c.player().addGold(-amount);

        if (amount <= 100) c.sayKey("npc.drunk.give.100");
        else if (amount <= 250) c.sayKey("npc.drunk.give.250");
        else if (amount <= 1000) c.sayKey("npc.drunk.give.1000");
        else if (amount <= 2000) c.sayKey("npc.drunk.give.2000");
        else c.sayKey("npc.drunk.give.more");

        return true;
      }
    };
  }

  public Drunk(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
