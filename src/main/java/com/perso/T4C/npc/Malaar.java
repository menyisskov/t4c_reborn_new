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

@Spawn(type = "Malaar", x = 1690, y = 1165, z = 0, stationary = false, aggressive = false)
public final class Malaar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Malaar";

  public static final String DISPLAY_NAME = "${npc.malaar}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.malaar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.malaar.0.0}", "${npc.topic_keyword.malaar.0.1}"),
                  "${npc.topic.malaar.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malaar.1.0}",
                      "${npc.topic_keyword.malaar.1.1}",
                      "${npc.topic_keyword.malaar.1.2}"),
                  "${npc.topic.malaar.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malaar.2.0}",
                      "${npc.topic_keyword.malaar.2.1}",
                      "${npc.topic_keyword.malaar.2.2}"),
                  "${npc.topic.malaar.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.malaar.3.0}"), "${npc.topic.malaar.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.malaar.4.0}"), "${npc.topic.malaar.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.malaar.5.0}"), "${npc.topic.malaar.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.malaar.6.0}"), "${npc.topic.malaar.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.malaar.7.0}"), "${npc.topic.malaar.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.malaar.8.0}"), "${npc.topic.malaar.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.malaar.9.0}"), "${npc.topic.malaar.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.malaar.10.0}"), "${npc.topic.malaar.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malaar.11.0}",
                      "${npc.topic_keyword.malaar.11.1}",
                      "${npc.topic_keyword.malaar.11.2}",
                      "${npc.topic_keyword.malaar.11.3}",
                      "${npc.topic_keyword.malaar.11.4}"),
                  "${npc.topic.malaar.11}",
                  List.of())),
          "MalaarNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("DONATION")) {

          if (c.player().getGold() > 10000) {

            c.sayKey("npc.malaar.donation.ask");

            c.askYesNo("malaar_donation");

          } else c.sayKey("npc.malaar.donation.rich");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("malaar_donation".equals(s)) {

          if (yes) {

            if (c.player().getGold() >= 10000) {

              c.player().addGold(-10000);

              if (c.karma() < 100 * (c.flag("__QUEST_ISLAND_ACCESS") + 1))
                c.karma(c.karma() + 20 * (500 - c.karma()) / 500);

              c.sayKey("npc.malaar.donation.done");

            } else c.sayKey("npc.malaar.donation.gold");

          } else {

            c.sayKey("npc.malaar.donation.small");

            c.askYesNo("malaar_donation2");
          }

          return true;
        }

        if ("malaar_donation2".equals(s)) {

          if (yes) {

            if (c.player().getGold() >= 1000) {

              c.player().addGold(-1000);

              if (c.karma() < 75 * (c.flag("__QUEST_ISLAND_ACCESS") + 1))
                c.karma(c.karma() + 10 * (500 - c.karma()) / 500);

              c.sayKey("npc.malaar.donation.done.small");

            } else c.sayKey("npc.malaar.donation.gold");

          } else {

            c.sayKey("npc.malaar.donation.tiny");

            c.askYesNo("malaar_donation3");
          }

          return true;
        }

        if ("malaar_donation3".equals(s)) {

          if (yes) {

            if (c.player().getGold() >= 250) {

              c.player().addGold(-250);

              if (c.karma() < 50 * (c.flag("__QUEST_ISLAND_ACCESS") + 1))
                c.karma(c.karma() + 5 * (500 - c.karma()) / 500);

              c.sayKey("npc.malaar.donation.done.tiny");

            } else c.sayKey("npc.malaar.donation.gold");
          }

          return true;
        }

        return false;
      }
    };
  }

  public Malaar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
