package com.perso.T4C.npc.arakas;

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
import java.util.List;

public final class Totak extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Totak";

  public static final String DISPLAY_NAME = "${npc.totak}";

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
              new NpcSpec.Part(BodyPart.HEAD, "PupLeatherHelm"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.totak}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.totak.0.0}", "${npc.topic_keyword.totak.0.1}"),
                  "${npc.topic.totak.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.totak.1.0}",
                      "${npc.topic_keyword.totak.1.1}",
                      "${npc.topic_keyword.totak.1.2}"),
                  "${npc.topic.totak.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.totak.2.0}"), "${npc.topic.totak.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.totak.3.0}", "${npc.topic_keyword.totak.3.1}"),
                  "${npc.topic.totak.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.totak.4.0}",
                      "${npc.topic_keyword.totak.4.1}",
                      "${npc.topic_keyword.totak.4.2}",
                      "${npc.topic_keyword.totak.4.3}"),
                  "${npc.topic.totak.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.totak.5.0}",
                      "${npc.topic_keyword.totak.5.1}",
                      "${npc.topic_keyword.totak.5.2}",
                      "${npc.topic_keyword.totak.5.3}",
                      "${npc.topic_keyword.totak.5.4}"),
                  "${npc.topic.totak.5}",
                  List.of())),
          "Male_Brigand",
          new NpcSpec.CombatProfile(100, 1000000, 65, 65, 65, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.startsWith("GAMBLE")) {

          java.util.regex.Matcher m =
              java.util.regex.Pattern.compile("GAMBLE\\s+(-?\\d+)").matcher(k);

          if (!m.find()) {

            c.sayKey("npc.totak.gamble.ask");

            return true;
          }

          int wager;

          try {

            wager = Integer.parseInt(m.group(1));

          } catch (NumberFormatException e) {

            c.sayKey("npc.totak.gamble.ask");

            return true;
          }

          if (wager < 0) {

            c.sayKey("npc.totak.gamble.negative");

            if (c.player().getGold() > 1) c.player().addGold(-(c.player().getGold() / 4));

          } else if (c.player().getGold() < wager) c.sayKey("npc.totak.gamble.poor");
          else if (wager >= c.player().getLevel() * 100) c.sayKey("npc.totak.gamble.high");
          else {

            c.player().addGold(-wager);

            if ((int) (Math.random() * 9) < 4) {

              c.player().addGold(wager * 2);

              c.sayKey("npc.totak.gamble.win");

            } else c.sayKey("npc.totak.gamble.lose");
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public Totak(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
