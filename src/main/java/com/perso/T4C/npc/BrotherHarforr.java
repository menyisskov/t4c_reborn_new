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

@Spawn(type = "BrotherHarforr", x = 1653, y = 1171, z = 0, stationary = false, aggressive = false)
public final class BrotherHarforr extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "BrotherHarforr";

  public static final String DISPLAY_NAME = "${npc.brotherharforr}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupMorningStar")),
          0,
          List.of(),
          "${npc.welcome.brotherharforr}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherharforr.0.0}",
                      "${npc.topic_keyword.brotherharforr.0.1}"),
                  "${npc.topic.brotherharforr.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherharforr.1.0}"),
                  "${npc.topic.brotherharforr.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherharforr.2.0}",
                      "${npc.topic_keyword.brotherharforr.2.1}",
                      "${npc.topic_keyword.brotherharforr.2.2}"),
                  "${npc.topic.brotherharforr.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherharforr.3.0}",
                      "${npc.topic_keyword.brotherharforr.3.1}",
                      "${npc.topic_keyword.brotherharforr.3.2}"),
                  "${npc.topic.brotherharforr.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherharforr.4.0}"),
                  "${npc.topic.brotherharforr.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherharforr.5.0}"),
                  "${npc.topic.brotherharforr.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherharforr.6.0}",
                      "${npc.topic_keyword.brotherharforr.6.1}",
                      "${npc.topic_keyword.brotherharforr.6.2}"),
                  "${npc.topic.brotherharforr.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherharforr.7.0}"),
                  "${npc.topic.brotherharforr.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherharforr.8.0}",
                      "${npc.topic_keyword.brotherharforr.8.1}",
                      "${npc.topic_keyword.brotherharforr.8.2}",
                      "${npc.topic_keyword.brotherharforr.8.3}",
                      "${npc.topic_keyword.brotherharforr.8.4}"),
                  "${npc.topic.brotherharforr.8}",
                  List.of())),
          "BrotherHarforrNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("HEAL")) {

          if (c.player().getCurrentHp() >= c.player().getMaxHp()) c.sayKey("npc.harforr.heal.none");
          else if (c.player().getLevel() < 5) {

            c.player().setCurrentHp(c.player().getMaxHp());

            c.sayKey("npc.harforr.heal.free");

          } else {

            c.sayKey("npc.harforr.heal.ask");

            c.askYesNo("harforr_heal");
          }

          return true;
        }

        if (k.equals("SANCTUARY")) {

          c.sayKey("npc.harforr.sanctuary.ask");

          c.askYesNo("harforr_sanctuary");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("harforr_heal".equals(s)) {

          if (yes) {

            int missing = c.player().getMaxHp() - c.player().getCurrentHp(),
                cost = missing / 2,
                gold = c.player().getGold();

            if (gold == 0) c.sayKey("npc.harforr.heal.poor");
            else {

              int paid = Math.min(cost, gold);

              c.player().addGold(-paid);

              c.player()
                  .setCurrentHp(
                      Math.min(c.player().getMaxHp(), c.player().getCurrentHp() + paid * 2));

              c.sayKey(paid < cost ? "npc.harforr.heal.partial" : "npc.harforr.heal.done");
            }
          }

          return true;
        }

        if ("harforr_sanctuary".equals(s)) {

          if (yes) {

            if (c.player().getGold() < 5000) c.sayKey("npc.harforr.sanctuary.poor");
            else {

              c.player().addGold(-5000);

              c.setRespawnPoint(1682, 1163, 0);

              c.flag("__SANCTUARY_WINDHOWL", 1);

              c.sayKey("npc.harforr.sanctuary.done");
            }
          }

          return true;
        }

        return false;
      }
    };
  }

  public BrotherHarforr(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
