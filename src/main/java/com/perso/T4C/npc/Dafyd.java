package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Dafyd", x = 440, y = 2285, z = 0, stationary = false, aggressive = false)
public final class Dafyd extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Dafyd";

  public static final String DISPLAY_NAME = "${npc.dafyd}";

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
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.dafyd}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dafyd.0.0}",
                      "${npc.topic_keyword.dafyd.0.1}",
                      "${npc.topic_keyword.dafyd.0.2}"),
                  "${npc.topic.dafyd.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.1.0}"), "${npc.topic.dafyd.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.2.0}", "${npc.topic_keyword.dafyd.2.1}"),
                  "${npc.topic.dafyd.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dafyd.3.0}",
                      "${npc.topic_keyword.dafyd.3.1}",
                      "${npc.topic_keyword.dafyd.3.2}"),
                  "${npc.topic.dafyd.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.4.0}"), "${npc.topic.dafyd.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.5.0}"), "${npc.topic.dafyd.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.6.0}"), "${npc.topic.dafyd.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.7.0}"), "${npc.topic.dafyd.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.8.0}"), "${npc.topic.dafyd.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.9.0}"), "${npc.topic.dafyd.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.10.0}"), "${npc.topic.dafyd.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.11.0}"), "${npc.topic.dafyd.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.12.0}"), "${npc.topic.dafyd.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.13.0}"), "${npc.topic.dafyd.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dafyd.14.0}"), "${npc.topic.dafyd.14}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dafyd.15.0}",
                      "${npc.topic_keyword.dafyd.15.1}",
                      "${npc.topic_keyword.dafyd.15.2}",
                      "${npc.topic_keyword.dafyd.15.3}"),
                  "${npc.topic.dafyd.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dafyd.16.0}",
                      "${npc.topic_keyword.dafyd.16.1}",
                      "${npc.topic_keyword.dafyd.16.2}",
                      "${npc.topic_keyword.dafyd.16.3}",
                      "${npc.topic_keyword.dafyd.16.4}"),
                  "${npc.topic.dafyd.16}",
                  List.of())),
          "DafydNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private void randomShout(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String prefix, int count) {

        int roll = 1 + (int) (Math.random() * 25);

        if (roll <= count) c.shoutKey(prefix + roll);
      }

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        randomShout(c, "npc.dafyd.attack.", 2);
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        randomShout(c, "npc.dafyd.attacked.", 3);
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("BUY")) {

          c.sayKey("npc.dafyd.buy.ask");

          c.askYesNo("browse");

          return true;
        }

        if (k.equals("SELL")) {

          c.openSellShop();

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if (!"browse".equals(state)) return false;

        if (yes)
          c.openShop(
              java.util.List.of(
                  "FineSteelShortSword2",
                  "FineSteelLongSword2",
                  "FineSteelBroadsword2",
                  "FineSteelScimitar2",
                  "HighMetalShortSword2",
                  "FineSteelHandAxe2",
                  "FineSteelBattleAxe2",
                  "FineSteelDagger2",
                  "HighMetalDagger2",
                  "FineSteelMace2",
                  "HighMetalFlail2",
                  "Bo2",
                  "RangKwan2",
                  "Tetsubo2",
                  "HickoryFlatbow2",
                  "HickoryLongbow2",
                  "HickoryReflexBow2",
                  "HickoryRecurveBow2",
                  "HickoryCompoundBow2"));
        else c.sayKey("npc.dafyd.buy.no");

        return true;
      }
    };
  }

  public Dafyd(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
