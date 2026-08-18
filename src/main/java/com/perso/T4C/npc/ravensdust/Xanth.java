package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Xanth", x = 690, y = 1610, z = 2, stationary = false, aggressive = false)
public final class Xanth extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Xanth";

  public static final String DISPLAY_NAME = "${npc.xanth}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.xanth}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.xanth.0.0}"), "${npc.topic.xanth.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.xanth.1.0}", "${npc.topic_keyword.xanth.1.1}"),
                  "${npc.topic.xanth.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.xanth.2.0}",
                      "${npc.topic_keyword.xanth.2.1}",
                      "${npc.topic_keyword.xanth.2.2}"),
                  "${npc.topic.xanth.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.xanth.3.0}", "${npc.topic_keyword.xanth.3.1}"),
                  "${npc.topic.xanth.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.xanth.4.0}"), "${npc.topic.xanth.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.xanth.5.0}"), "${npc.topic.xanth.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.xanth.6.0}"), "${npc.topic.xanth.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.xanth.7.0}"), "${npc.topic.xanth.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.xanth.8.0}", "${npc.topic_keyword.xanth.8.1}"),
                  "${npc.topic.xanth.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.xanth.9.0}"), "${npc.topic.xanth.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.xanth.10.0}",
                      "${npc.topic_keyword.xanth.10.1}",
                      "${npc.topic_keyword.xanth.10.2}",
                      "${npc.topic_keyword.xanth.10.3}",
                      "${npc.topic_keyword.xanth.10.4}"),
                  "${npc.topic.xanth.10}",
                  List.of())),
          "Mage",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int f = c.flag("__FLAG_ARAKNOR_APPRENTICE");

        if (f == 1) c.askYesNo("xanth_apprentice");
        else
          c.sayKey(
              f == 2 ? "npc.xanth.returned" : f == 3 ? "npc.xanth.power" : "npc.xanth.stranger");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("FOUL POTION")) {

          if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1
              && c.hasItem("essence_of_bloodlust")
              && c.itemCount("necromantic_scroll") >= 2) c.askYesNo("xanth_potion");

          return true;
        }

        if (k.equals("RING")) {

          int q = c.flag("__QUEST_FOR_BONES");

          if (q == 6) c.askYesNo("xanth_ring");
          else if (q == 7 && c.hasItem("bloodstone_ring") && c.hasItem("essence_of_bloodlust")) {

            c.takeItem("bloodstone_ring");

            c.takeItem("essence_of_bloodlust");

            c.flag("__FLAG_USER_KNOWS_MORDENTHAL_HAS_BONES", 1);

            c.sayKey("npc.xanth.ring.done");
          }

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("xanth_apprentice".equals(s)) {

          if (yes) {

            c.flag("__FLAG_ARAKNOR_APPRENTICE", 2);

            c.sayKey("npc.xanth.tutelage");
          }

          return true;
        }

        if ("xanth_potion".equals(s)) {

          if (yes && c.player().getGold() >= 25000) {

            c.takeItem("essence_of_bloodlust");

            c.takeItem("necromantic_scroll");

            c.takeItem("necromantic_scroll");

            c.player().addGold(-25000);

            c.giveItem("foul_potion");

            c.sayKey("npc.xanth.potion.ok");
          }

          return true;
        }

        if ("xanth_ring".equals(s)) {

          if (yes) {

            c.flag("__QUEST_FOR_BONES", 7);

            c.sayKey("npc.xanth.ring.ask");
          }

          return true;
        }

        return false;
      }
    };
  }

  public Xanth(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
