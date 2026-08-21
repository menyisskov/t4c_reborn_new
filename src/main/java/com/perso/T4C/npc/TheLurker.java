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

@Spawn(type = "TheLurker", x = 1714, y = 1174, z = 0, stationary = false, aggressive = false)
public final class TheLurker extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "TheLurker";

  public static final String DISPLAY_NAME = "${npc.thelurker}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.thelurker}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thelurker.0.0}", "${npc.topic_keyword.thelurker.0.1}"),
                  "${npc.topic.thelurker.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thelurker.1.0}",
                      "${npc.topic_keyword.thelurker.1.1}",
                      "${npc.topic_keyword.thelurker.1.2}"),
                  "${npc.topic.thelurker.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thelurker.2.0}"),
                  "${npc.topic.thelurker.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thelurker.3.0}",
                      "${npc.topic_keyword.thelurker.3.1}",
                      "${npc.topic_keyword.thelurker.3.2}"),
                  "${npc.topic.thelurker.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thelurker.4.0}", "${npc.topic_keyword.thelurker.4.1}"),
                  "${npc.topic.thelurker.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thelurker.5.0}"),
                  "${npc.topic.thelurker.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thelurker.6.0}"),
                  "${npc.topic.thelurker.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thelurker.7.0}"),
                  "${npc.topic.thelurker.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thelurker.8.0}",
                      "${npc.topic_keyword.thelurker.8.1}",
                      "${npc.topic_keyword.thelurker.8.2}",
                      "${npc.topic_keyword.thelurker.8.3}",
                      "${npc.topic_keyword.thelurker.8.4}"),
                  "${npc.topic.thelurker.8}",
                  List.of())),
          "Thief_TheLurker",
          new NpcSpec.CombatProfile(100, 1000000, 65, 65, 65, 1000000, 310, 65535, "1d29+21"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_CURE_KALASTOR") == 1) c.askYesNo("lurker_cure");
        else c.sayKey("npc.lurker.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("TRAIN")) {

          c.sayKey("npc.lurker.train");

          return true;
        }

        if (k.equals("MARKET OF SHADOW")) {

          int f = c.flag("__BLACK_MARKET");

          if (f == 1) {

            c.flag("__BLACK_MARKET", 2);

            c.sayKey("npc.lurker.market");

          } else if (f == 2) c.sayKey("npc.lurker.market.done");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"lurker_cure".equals(s)) return false;

        c.flag("__QUEST_CURE_KALASTOR", 0);

        if (yes && c.player().getGold() >= 800) {

          c.player().addGold(-800);

          c.giveItem("elixir_of_purity");

          c.sayKey("npc.lurker.cure.ok");

        } else if (yes) c.sayKey("npc.lurker.cure.poor");

        return true;
      }
    };
  }

  public TheLurker(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
