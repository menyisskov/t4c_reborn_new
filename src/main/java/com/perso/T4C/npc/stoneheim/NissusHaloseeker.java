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
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "NissusHaloseeker", x = 210, y = 675, z = 0, stationary = false, aggressive = false)
public final class NissusHaloseeker extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "NissusHaloseeker";

  public static final String DISPLAY_NAME = "${npc.nissushaloseeker}";

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
          "${npc.welcome.nissushaloseeker}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.0.0}"),
                  "${npc.topic.nissushaloseeker.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.1.0}"),
                  "${npc.topic.nissushaloseeker.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.2.0}"),
                  "${npc.topic.nissushaloseeker.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.3.0}",
                      "${npc.topic_keyword.nissushaloseeker.3.1}"),
                  "${npc.topic.nissushaloseeker.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.4.0}",
                      "${npc.topic_keyword.nissushaloseeker.4.1}",
                      "${npc.topic_keyword.nissushaloseeker.4.2}"),
                  "${npc.topic.nissushaloseeker.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.5.0}"),
                  "${npc.topic.nissushaloseeker.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.6.0}",
                      "${npc.topic_keyword.nissushaloseeker.6.1}"),
                  "${npc.topic.nissushaloseeker.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.7.0}",
                      "${npc.topic_keyword.nissushaloseeker.7.1}"),
                  "${npc.topic.nissushaloseeker.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.8.0}",
                      "${npc.topic_keyword.nissushaloseeker.8.1}"),
                  "${npc.topic.nissushaloseeker.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.9.0}",
                      "${npc.topic_keyword.nissushaloseeker.9.1}",
                      "${npc.topic_keyword.nissushaloseeker.9.2}"),
                  "${npc.topic.nissushaloseeker.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.10.0}",
                      "${npc.topic_keyword.nissushaloseeker.10.1}"),
                  "${npc.topic.nissushaloseeker.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.11.0}",
                      "${npc.topic_keyword.nissushaloseeker.11.1}"),
                  "${npc.topic.nissushaloseeker.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.12.0}",
                      "${npc.topic_keyword.nissushaloseeker.12.1}"),
                  "${npc.topic.nissushaloseeker.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.13.0}",
                      "${npc.topic_keyword.nissushaloseeker.13.1}",
                      "${npc.topic_keyword.nissushaloseeker.13.2}"),
                  "${npc.topic.nissushaloseeker.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.14.0}"),
                  "${npc.topic.nissushaloseeker.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.15.0}",
                      "${npc.topic_keyword.nissushaloseeker.15.1}"),
                  "${npc.topic.nissushaloseeker.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.16.0}",
                      "${npc.topic_keyword.nissushaloseeker.16.1}",
                      "${npc.topic_keyword.nissushaloseeker.16.2}"),
                  "${npc.topic.nissushaloseeker.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.17.0}"),
                  "${npc.topic.nissushaloseeker.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.18.0}"),
                  "${npc.topic.nissushaloseeker.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.19.0}",
                      "${npc.topic_keyword.nissushaloseeker.19.1}"),
                  "${npc.topic.nissushaloseeker.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.20.0}"),
                  "${npc.topic.nissushaloseeker.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.21.0}"),
                  "${npc.topic.nissushaloseeker.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.22.0}"),
                  "${npc.topic.nissushaloseeker.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.23.0}"),
                  "${npc.topic.nissushaloseeker.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.24.0}",
                      "${npc.topic_keyword.nissushaloseeker.24.1}",
                      "${npc.topic_keyword.nissushaloseeker.24.2}"),
                  "${npc.topic.nissushaloseeker.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.25.0}"),
                  "${npc.topic.nissushaloseeker.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.26.0}",
                      "${npc.topic_keyword.nissushaloseeker.26.1}",
                      "${npc.topic_keyword.nissushaloseeker.26.2}",
                      "${npc.topic_keyword.nissushaloseeker.26.3}"),
                  "${npc.topic.nissushaloseeker.26}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nissushaloseeker.27.0}",
                      "${npc.topic_keyword.nissushaloseeker.27.1}",
                      "${npc.topic_keyword.nissushaloseeker.27.2}",
                      "${npc.topic_keyword.nissushaloseeker.27.3}",
                      "${npc.topic_keyword.nissushaloseeker.27.4}"),
                  "${npc.topic.nissushaloseeker.27}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nissushaloseeker.28.0}"),
                  "${npc.topic.nissushaloseeker.28}",
                  List.of())),
          "NissusHaloseekerNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1) c.sayKey("npc.nissus.ogrimar");
        else if (c.flag("__QUEST_FIXED_ALIGNMENT") == -1) c.sayKey("npc.nissus.evil");
        else c.sayKey("npc.nissus.good");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("SOUL") && k.contains("ARTHERK")) {

          if (c.hasItem("key_of_artherk")) c.sayKey("npc.nissus.key");
          else if (c.flag("__QUEST_FLAG_ARTHERK_SOUL") == 1) c.sayKey("npc.nissus.already");
          else if (c.flag("__QUEST_FIXED_ALIGNMENT") == -1) c.sayKey("npc.nissus.malevolent");
          else if (c.flag("__FLAG_LIGHTBRINGER_OF_ARTHERK") == 0)
            c.sayKey("npc.nissus.lightbringer");
          else if (c.itemCount("ring_of_pure_faith") >= 3
              && c.itemCount("staff_of_hope") >= 2
              && c.itemCount("pearl_of_wisdom") >= 2) c.askYesNo("nissus_soul");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"nissus_soul".equals(s)) return false;

        if (yes
            && c.itemCount("ring_of_pure_faith") >= 3
            && c.itemCount("staff_of_hope") >= 2
            && c.itemCount("pearl_of_wisdom") >= 2) {

          for (int i = 0; i < 3; i++) c.takeItem("ring_of_pure_faith");

          for (int i = 0; i < 2; i++) c.takeItem("staff_of_hope");

          for (int i = 0; i < 2; i++) c.takeItem("pearl_of_wisdom");

          c.giveItem("soul_of_artherk");

          c.flag("__QUEST_FLAG_ARTHERK_SOUL", 1);

          c.giveXp(250000);

          c.sayKey("npc.nissus.soul.ok");
        }

        return true;
      }
    };
  }

  public NissusHaloseeker(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
