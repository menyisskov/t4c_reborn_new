package com.perso.T4C.npc.ravensdust;

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

@Spawn(type = "BrotherThorkas", x = 1565, y = 2398, z = 0, stationary = false, aggressive = false)
public final class BrotherThorkas extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "BrotherThorkas";

  public static final String DISPLAY_NAME = "${npc.brotherthorkas}";

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
          "${npc.welcome.brotherthorkas}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherthorkas.0.0}",
                      "${npc.topic_keyword.brotherthorkas.0.1}"),
                  "${npc.topic.brotherthorkas.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherthorkas.1.0}",
                      "${npc.topic_keyword.brotherthorkas.1.1}",
                      "${npc.topic_keyword.brotherthorkas.1.2}"),
                  "${npc.topic.brotherthorkas.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherthorkas.2.0}",
                      "${npc.topic_keyword.brotherthorkas.2.1}"),
                  "${npc.topic.brotherthorkas.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.3.0}"),
                  "${npc.topic.brotherthorkas.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.4.0}"),
                  "${npc.topic.brotherthorkas.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.5.0}"),
                  "${npc.topic.brotherthorkas.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.6.0}"),
                  "${npc.topic.brotherthorkas.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherthorkas.7.0}",
                      "${npc.topic_keyword.brotherthorkas.7.1}"),
                  "${npc.topic.brotherthorkas.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.8.0}"),
                  "${npc.topic.brotherthorkas.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.9.0}"),
                  "${npc.topic.brotherthorkas.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.10.0}"),
                  "${npc.topic.brotherthorkas.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.11.0}"),
                  "${npc.topic.brotherthorkas.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.12.0}"),
                  "${npc.topic.brotherthorkas.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.13.0}"),
                  "${npc.topic.brotherthorkas.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.14.0}"),
                  "${npc.topic.brotherthorkas.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherthorkas.15.0}",
                      "${npc.topic_keyword.brotherthorkas.15.1}",
                      "${npc.topic_keyword.brotherthorkas.15.2}",
                      "${npc.topic_keyword.brotherthorkas.15.3}"),
                  "${npc.topic.brotherthorkas.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherthorkas.16.0}",
                      "${npc.topic_keyword.brotherthorkas.16.1}",
                      "${npc.topic_keyword.brotherthorkas.16.2}",
                      "${npc.topic_keyword.brotherthorkas.16.3}",
                      "${npc.topic_keyword.brotherthorkas.16.4}"),
                  "${npc.topic.brotherthorkas.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherthorkas.17.0}",
                      "${npc.topic_keyword.brotherthorkas.17.1}",
                      "${npc.topic_keyword.brotherthorkas.17.2}"),
                  "${npc.topic.brotherthorkas.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brotherthorkas.18.0}"),
                  "${npc.topic.brotherthorkas.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brotherthorkas.19.0}",
                      "${npc.topic_keyword.brotherthorkas.19.1}",
                      "${npc.topic_keyword.brotherthorkas.19.2}"),
                  "${npc.topic.brotherthorkas.19}",
                  List.of())),
          "BrotherThorkasNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("HOLY WATER")) {

          c.sayKey("npc.thorkas.holy.ask");

          c.askYesNo("holy_water");

          return true;
        }

        if (k.equals("HEAL")) {

          int missing = c.player().getMaxHp() - c.player().getCurrentHp();

          if (missing <= 0) c.sayKey("npc.thorkas.heal.none");
          else if (c.player().getLevel() < 5) {

            c.player().applyHeal(c.player().getMaxHp(), c.player().getMaxHp());

            c.sayKey("npc.thorkas.heal.free");

            c.castTargetSpell(10767);

          } else {

            c.sayKey("npc.thorkas.heal.ask");

            c.askYesNo("thorkas_heal");
          }

          return true;
        }

        if (k.equals("CHALICE")) {

          if (c.hasItem("golden_chalice")) {

            c.sayKey("npc.thorkas.chalice.ask");

            c.askYesNo("chalice");

          } else c.sayKey("npc.thorkas.chalice.none");

          return true;
        }

        if (k.equals("RING")) {

          if (c.flag("__FLAG_QUEST_FOR_BONES") == 4) {

            c.flag("__FLAG_QUEST_FOR_BONES", 5);

            c.sayKey("npc.thorkas.ring.evil");

          } else if (c.flag("__FLAG_QUEST_FOR_BONES") >= 5) c.sayKey("npc.thorkas.ring.done");

          return true;
        }

        if (k.contains("SPELL") || k.equals("LEARN") || k.equals("TEACH")) {

          c.sayKey("npc.thorkas.spells");

          c.openSpellLearning(java.util.List.of("turn_undead", "tranquility"));

          return true;
        }

        if (k.equals("SANCTUARY")) {

          c.sayKey("npc.thorkas.sanctuary.ask");

          c.askYesNo("sanctuary");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!yes) {

          return true;
        }

        if ("holy_water".equals(s)) {

          if (c.player().getGold() >= 1000 && c.hasItem("flask_of_crystal_water")) {

            c.player().addGold(-1000);

            c.takeItem("flask_of_crystal_water");

            c.giveItem("flask_of_holy_water");

            c.sayKey("npc.thorkas.holy.done");

          } else c.sayKey("npc.thorkas.holy.missing");

          return true;
        }

        if ("chalice".equals(s)) {

          if (c.hasItem("golden_chalice")) {

            c.takeItem("golden_chalice");

            c.giveItem("ring_of_faith");

            c.giveXp(5000);

            c.sayKey("npc.thorkas.chalice.done");
          }

          return true;
        }

        if ("thorkas_heal".equals(s)) {

          int missing = c.player().getMaxHp() - c.player().getCurrentHp(), cost = missing / 2;

          if (c.player().getGold() < cost) {

            int gold = c.player().getGold();

            if (gold > 0) {

              c.player().addGold(-gold);

              c.player().applyHeal(c.player().getCurrentHp() + gold * 2, c.player().getMaxHp());

            } else c.sayKey("npc.thorkas.heal.poor");

          } else {

            c.player().addGold(-cost);

            c.player().applyHeal(c.player().getMaxHp(), c.player().getMaxHp());
          }

          c.castTargetSpell(10767);

          return true;
        }

        if ("sanctuary".equals(s)) {

          if (c.player().getGold() >= 15000) {

            c.player().addGold(-15000);

            c.sayKey("npc.thorkas.sanctuary.done");

          } else c.sayKey("npc.thorkas.sanctuary.poor");

          return true;
        }

        return false;
      }
    };
  }

  public BrotherThorkas(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
