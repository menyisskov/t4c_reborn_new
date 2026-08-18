package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class WitchDoctorKwarlgloth extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "WitchDoctorKwarlgloth";

  public static final String DISPLAY_NAME = "${npc.witchdoctorkwarlgloth}";

  public static final String SPRITE_BASE = "64kSkavenShaman";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.witchdoctorkwarlgloth}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.0.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.0.1}"),
                  "${npc.topic.witchdoctorkwarlgloth.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.1.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.1.1}"),
                  "${npc.topic.witchdoctorkwarlgloth.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.2.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.2.1}"),
                  "${npc.topic.witchdoctorkwarlgloth.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.3.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.3.1}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.3.2}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.3.3}"),
                  "${npc.topic.witchdoctorkwarlgloth.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.witchdoctorkwarlgloth.4.0}"),
                  "${npc.topic.witchdoctorkwarlgloth.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.5.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.5.1}"),
                  "${npc.topic.witchdoctorkwarlgloth.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.6.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.6.1}"),
                  "${npc.topic.witchdoctorkwarlgloth.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.7.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.7.1}"),
                  "${npc.topic.witchdoctorkwarlgloth.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.witchdoctorkwarlgloth.8.0}"),
                  "${npc.topic.witchdoctorkwarlgloth.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.9.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.9.1}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.9.2}"),
                  "${npc.topic.witchdoctorkwarlgloth.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.10.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.10.1}"),
                  "${npc.topic.witchdoctorkwarlgloth.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.witchdoctorkwarlgloth.11.0}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.11.1}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.11.2}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.11.3}",
                      "${npc.topic_keyword.witchdoctorkwarlgloth.11.4}"),
                  "${npc.topic.witchdoctorkwarlgloth.11}",
                  List.of())),
          "WitchDoctorKwarlglothNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("DEMONIC BLOOD") || k.contains("DEEMONIK BLUD")) {

          if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") != 1) {

            c.sayKey("npc.kwarlgloth.bash");

            c.askYesNo("kwarlgloth_bash");

          } else {

            c.sayKey(
                c.flag("__FLAG_USER_ASKED_ABOUT_DEMONIC_BLOOD") == 0
                    ? "npc.kwarlgloth.blood.ask"
                    : "npc.kwarlgloth.blood.need");

            c.flag("__FLAG_USER_ASKED_ABOUT_DEMONIC_BLOOD", 1);

            c.askYesNo("kwarlgloth_blood");
          }

          return true;
        }

        if (k.contains("BLOODSTONE RING")
            || k.contains("BLOODLUST")
            || k.contains("DAGGER OF BLEEDING")) {

          c.sayKey(
              c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1
                  ? "npc.kwarlgloth.lore"
                  : "npc.kwarlgloth.bash");

          return true;
        }

        if (k.equals("INTELLIGENT")
            || k.equals("HINTELJENT")
            || k.equals("WORK")
            || k.equals("OCCUPATION")) {

          c.sayKey("npc.kwarlgloth.shop.ask");

          c.askYesNo("shop_browse");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if ("kwarlgloth_bash".equals(state)) {

          if (yes) c.npc().provoke();

          return true;
        }

        if ("kwarlgloth_blood".equals(state)) {

          if (yes
              && c.itemCount("bloodstone_ring") >= 4
              && c.itemCount("sacrificial_dagger_of_bleeding") >= 3
              && c.player().getGold() >= 20000) {

            for (int i = 0; i < 4; i++) c.takeItem("bloodstone_ring");

            for (int i = 0; i < 3; i++) c.takeItem("sacrificial_dagger_of_bleeding");

            c.player().addGold(-20000);

            c.giveItem("demonic_blood");

            c.sayKey("npc.kwarlgloth.blood.done");
          }

          return true;
        }

        return false;
      }
    };
  }

  public WitchDoctorKwarlgloth(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
