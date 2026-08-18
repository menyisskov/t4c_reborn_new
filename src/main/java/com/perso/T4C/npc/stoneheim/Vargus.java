package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Vargus extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Vargus";

  public static final String DISPLAY_NAME = "${npc.vargus}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.BACK, "PupSeraphDarkWings")),
          0,
          List.of(),
          "${npc.welcome.vargus}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.0.0}"), "${npc.topic.vargus.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.1.0}"), "${npc.topic.vargus.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.2.0}"), "${npc.topic.vargus.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.3.0}"), "${npc.topic.vargus.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.4.0}"), "${npc.topic.vargus.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.5.0}"), "${npc.topic.vargus.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.6.0}", "${npc.topic_keyword.vargus.6.1}"),
                  "${npc.topic.vargus.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.7.0}"), "${npc.topic.vargus.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.8.0}", "${npc.topic_keyword.vargus.8.1}"),
                  "${npc.topic.vargus.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.9.0}", "${npc.topic_keyword.vargus.9.1}"),
                  "${npc.topic.vargus.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.10.0}"), "${npc.topic.vargus.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vargus.11.0}"), "${npc.topic.vargus.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.vargus.12.0}",
                      "${npc.topic_keyword.vargus.12.1}",
                      "${npc.topic_keyword.vargus.12.2}",
                      "${npc.topic_keyword.vargus.12.3}"),
                  "${npc.topic.vargus.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.vargus.13.0}",
                      "${npc.topic_keyword.vargus.13.1}",
                      "${npc.topic_keyword.vargus.13.2}",
                      "${npc.topic_keyword.vargus.13.3}",
                      "${npc.topic_keyword.vargus.13.4}"),
                  "${npc.topic.vargus.13}",
                  List.of())),
          "VargusNPC",
          new NpcSpec.CombatProfile(
              200, 1000000, 500, 500, 500, 1000000, 1210, 65535, "1d172+134"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_USER_HAS_BLOOD_OF_OGRIMAR") == 0
            && c.itemCount("demonic_blood") >= 3
            && c.itemCount("foul_potion") >= 2
            && c.itemCount("black_widow_venom") >= 5) c.askYesNo("vargus_blood");
        else if (c.flag("__FLAG_USER_KNOWS_WHERE_SKULL_OF_OGRIMAR_IS_HIDDEN") == 1) {

          c.flag("__FLAG_USER_KNOWS_WHERE_SKULL_OF_OGRIMAR_IS_HIDDEN", 2);

          c.sayKey("npc.vargus.visitor");

        } else
          c.sayKey(
              c.flag("__QUEST_FIXED_ALIGNMENT") == -1
                  ? "npc.vargus.welcome.dark"
                  : "npc.vargus.welcome");
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"vargus_blood".equals(s)) return false;

        if (yes
            && c.itemCount("demonic_blood") >= 3
            && c.itemCount("foul_potion") >= 2
            && c.itemCount("black_widow_venom") >= 5) {

          for (int i = 0; i < 3; i++) c.takeItem("demonic_blood");

          for (int i = 0; i < 2; i++) c.takeItem("foul_potion");

          for (int i = 0; i < 5; i++) c.takeItem("black_widow_venom");

          c.giveItem("blood_of_ogrimar");

          c.flag("__FLAG_USER_HAS_BLOOD_OF_OGRIMAR", 1);

          c.sayKey("npc.vargus.blood.ok");
        }

        return true;
      }
    };
  }

  public Vargus(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
