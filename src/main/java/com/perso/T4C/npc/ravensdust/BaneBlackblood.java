package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class BaneBlackblood extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "BaneBlackblood";

  public static final String DISPLAY_NAME = "${npc.baneblackblood}";

  public static final String SPRITE_BASE = "BlackWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.baneblackblood}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.0.0}",
                      "${npc.topic_keyword.baneblackblood.0.1}"),
                  "${npc.topic.baneblackblood.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.1.0}"),
                  "${npc.topic.baneblackblood.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.2.0}",
                      "${npc.topic_keyword.baneblackblood.2.1}"),
                  "${npc.topic.baneblackblood.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.3.0}",
                      "${npc.topic_keyword.baneblackblood.3.1}",
                      "${npc.topic_keyword.baneblackblood.3.2}"),
                  "${npc.topic.baneblackblood.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.4.0}",
                      "${npc.topic_keyword.baneblackblood.4.1}"),
                  "${npc.topic.baneblackblood.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.5.0}",
                      "${npc.topic_keyword.baneblackblood.5.1}"),
                  "${npc.topic.baneblackblood.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.6.0}"),
                  "${npc.topic.baneblackblood.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.7.0}"),
                  "${npc.topic.baneblackblood.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.8.0}"),
                  "${npc.topic.baneblackblood.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.9.0}"),
                  "${npc.topic.baneblackblood.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.10.0}"),
                  "${npc.topic.baneblackblood.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.11.0}"),
                  "${npc.topic.baneblackblood.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.12.0}",
                      "${npc.topic_keyword.baneblackblood.12.1}"),
                  "${npc.topic.baneblackblood.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.13.0}",
                      "${npc.topic_keyword.baneblackblood.13.1}"),
                  "${npc.topic.baneblackblood.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.14.0}",
                      "${npc.topic_keyword.baneblackblood.14.1}"),
                  "${npc.topic.baneblackblood.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.15.0}",
                      "${npc.topic_keyword.baneblackblood.15.1}"),
                  "${npc.topic.baneblackblood.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.16.0}",
                      "${npc.topic_keyword.baneblackblood.16.1}"),
                  "${npc.topic.baneblackblood.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.17.0}"),
                  "${npc.topic.baneblackblood.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.18.0}"),
                  "${npc.topic.baneblackblood.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.19.0}",
                      "${npc.topic_keyword.baneblackblood.19.1}"),
                  "${npc.topic.baneblackblood.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.20.0}"),
                  "${npc.topic.baneblackblood.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.21.0}"),
                  "${npc.topic.baneblackblood.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.22.0}"),
                  "${npc.topic.baneblackblood.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.23.0}"),
                  "${npc.topic.baneblackblood.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.24.0}"),
                  "${npc.topic.baneblackblood.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.25.0}"),
                  "${npc.topic.baneblackblood.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.26.0}",
                      "${npc.topic_keyword.baneblackblood.26.1}"),
                  "${npc.topic.baneblackblood.26}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baneblackblood.27.0}"),
                  "${npc.topic.baneblackblood.27}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.28.0}",
                      "${npc.topic_keyword.baneblackblood.28.1}",
                      "${npc.topic_keyword.baneblackblood.28.2}",
                      "${npc.topic_keyword.baneblackblood.28.3}"),
                  "${npc.topic.baneblackblood.28}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baneblackblood.29.0}",
                      "${npc.topic_keyword.baneblackblood.29.1}",
                      "${npc.topic_keyword.baneblackblood.29.2}",
                      "${npc.topic_keyword.baneblackblood.29.3}",
                      "${npc.topic_keyword.baneblackblood.29.4}"),
                  "${npc.topic.baneblackblood.29}",
                  List.of())),
          "BlackBloodNPC",
          new NpcSpec.CombatProfile(80, 4602, 95, 86, 86, 40, 970, 330, "1d138+107"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onAttack(NpcBehaviorContext c) {

          int r = (int) (Math.random() * 9);

          if (r <= 4) c.shoutKey("npc.bane.attack." + r);
        }

        @Override
        public void onAttacked(NpcBehaviorContext c) {

          int r = (int) (Math.random() * 9);

          if (r <= 2) c.shoutKey("npc.bane.attacked." + r);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          if (c.flag("__QUEST_DWARTHON_STONEFACE") != 5) c.sayKey("npc.bane.unavailable");
          else c.sayKey("npc.welcome.baneblackblood");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.contains("INGREDIENT")) {

            if (c.hasItem("manastone") && c.hasItem("pouch_of_blue_cohosh")) {

              c.sayKey("npc.bane.ingredients.ask");

              c.askYesNo("ingredients");

            } else c.sayKey("npc.bane.ingredients.need");

            return true;
          }

          if (k.contains("KRAANIAN") && k.contains("EYE")) {

            if (c.flag("__QUEST_KRAANIAN_EYES") >= 2) {

              c.sayKey("npc.bane.eyes.ask");

              c.askYesNo("eyes");

            } else c.sayKey("npc.bane.eyes.none");

            return true;
          }

          if (k.equals("ROYAL KEY")) {

            int q = c.flag("__QUEST_ROYAL_KEY4");

            if (q >= 3 && q % 3 == 2) {

              if (!c.hasItem("royal_key_4")) c.giveItem("royal_key_4");

              c.sayKey("npc.bane.key.give");

            } else if (q >= 3) {

              c.flag("__QUEST_ROYAL_KEY4", q + 1);

              c.sayKey("npc.bane.key.fight");

              c.npc().provoke();

            } else c.sayKey("npc.bane.key.none");

            return true;
          }

          return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if (!yes) {

            c.sayKey("npc.bane.no");

            c.npc().provoke();

            return true;
          }

          if ("ingredients".equals(state)) {

            if (c.hasItem("manastone") && c.hasItem("pouch_of_blue_cohosh")) {

              c.takeItem("manastone");

              c.takeItem("pouch_of_blue_cohosh");

              c.giveItem("blood_dagger");

              c.giveXp(5000);

              c.sayKey("npc.bane.ingredients.done");

            } else c.sayKey("npc.bane.trick");

            return true;
          }

          if ("eyes".equals(state)) {

            if (c.player().getGold() >= 5000) {

              c.player().addGold(-5000);

              c.giveItem("kraanian_eyes");

              c.sayKey("npc.bane.eyes.give");

            } else c.sayKey("npc.bane.gold");

            return true;
          }

          return false;
        }
      };

  public BaneBlackblood(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
