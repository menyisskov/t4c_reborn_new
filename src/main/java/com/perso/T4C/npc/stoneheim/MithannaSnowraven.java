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

@Spawn(type = "MithannaSnowraven", x = 120, y = 795, z = 0, stationary = false, aggressive = false)
public final class MithannaSnowraven extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MithannaSnowraven";

  public static final String DISPLAY_NAME = "${npc.mithannasnowraven}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "WoLeatherLegs"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.mithannasnowraven}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.0.0}",
                      "${npc.topic_keyword.mithannasnowraven.0.1}"),
                  "${npc.topic.mithannasnowraven.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.1.0}",
                      "${npc.topic_keyword.mithannasnowraven.1.1}"),
                  "${npc.topic.mithannasnowraven.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.2.0}",
                      "${npc.topic_keyword.mithannasnowraven.2.1}",
                      "${npc.topic_keyword.mithannasnowraven.2.2}"),
                  "${npc.topic.mithannasnowraven.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.3.0}"),
                  "${npc.topic.mithannasnowraven.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.4.0}"),
                  "${npc.topic.mithannasnowraven.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.5.0}"),
                  "${npc.topic.mithannasnowraven.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.6.0}",
                      "${npc.topic_keyword.mithannasnowraven.6.1}"),
                  "${npc.topic.mithannasnowraven.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.7.0}",
                      "${npc.topic_keyword.mithannasnowraven.7.1}"),
                  "${npc.topic.mithannasnowraven.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.8.0}",
                      "${npc.topic_keyword.mithannasnowraven.8.1}"),
                  "${npc.topic.mithannasnowraven.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.9.0}",
                      "${npc.topic_keyword.mithannasnowraven.9.1}"),
                  "${npc.topic.mithannasnowraven.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.10.0}"),
                  "${npc.topic.mithannasnowraven.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.11.0}",
                      "${npc.topic_keyword.mithannasnowraven.11.1}"),
                  "${npc.topic.mithannasnowraven.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.12.0}",
                      "${npc.topic_keyword.mithannasnowraven.12.1}",
                      "${npc.topic_keyword.mithannasnowraven.12.2}"),
                  "${npc.topic.mithannasnowraven.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.13.0}",
                      "${npc.topic_keyword.mithannasnowraven.13.1}",
                      "${npc.topic_keyword.mithannasnowraven.13.2}",
                      "${npc.topic_keyword.mithannasnowraven.13.3}"),
                  "${npc.topic.mithannasnowraven.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.14.0}",
                      "${npc.topic_keyword.mithannasnowraven.14.1}"),
                  "${npc.topic.mithannasnowraven.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.15.0}",
                      "${npc.topic_keyword.mithannasnowraven.15.1}"),
                  "${npc.topic.mithannasnowraven.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.16.0}"),
                  "${npc.topic.mithannasnowraven.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.17.0}",
                      "${npc.topic_keyword.mithannasnowraven.17.1}",
                      "${npc.topic_keyword.mithannasnowraven.17.2}"),
                  "${npc.topic.mithannasnowraven.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.18.0}"),
                  "${npc.topic.mithannasnowraven.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.19.0}"),
                  "${npc.topic.mithannasnowraven.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.20.0}"),
                  "${npc.topic.mithannasnowraven.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.21.0}"),
                  "${npc.topic.mithannasnowraven.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.22.0}",
                      "${npc.topic_keyword.mithannasnowraven.22.1}"),
                  "${npc.topic.mithannasnowraven.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.23.0}"),
                  "${npc.topic.mithannasnowraven.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mithannasnowraven.24.0}"),
                  "${npc.topic.mithannasnowraven.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.25.0}",
                      "${npc.topic_keyword.mithannasnowraven.25.1}",
                      "${npc.topic_keyword.mithannasnowraven.25.2}",
                      "${npc.topic_keyword.mithannasnowraven.25.3}"),
                  "${npc.topic.mithannasnowraven.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mithannasnowraven.26.0}",
                      "${npc.topic_keyword.mithannasnowraven.26.1}",
                      "${npc.topic_keyword.mithannasnowraven.26.2}",
                      "${npc.topic_keyword.mithannasnowraven.26.3}",
                      "${npc.topic_keyword.mithannasnowraven.26.4}"),
                  "${npc.topic.mithannasnowraven.26}",
                  List.of())),
          "MithannaSnowravenNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int h = java.time.LocalTime.now().getHour();

        c.sayKey(h >= 22 || h < 6 ? "npc.mithanna.night" : "npc.mithanna.day");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("HEART") && k.contains("ARTHERK")) {

          if (c.flag("__QUEST_FLAG_ARTHERK_HEART") == 1) c.sayKey("npc.mithanna.heart.already");
          else if (c.flag("__QUEST_FIXED_ALIGNMENT") == -1) c.sayKey("npc.mithanna.heart.dark");
          else if (c.flag("__FLAG_LIGHTBRINGER_OF_ARTHERK") == 0)
            c.sayKey("npc.mithanna.heart.light");
          else if (c.itemCount("gem_of_courage") >= 2
              && c.itemCount("tome_of_valor") >= 4
              && c.itemCount("sword_of_majesty") >= 3) c.askYesNo("mithanna_heart");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"mithanna_heart".equals(s)) return false;

        if (yes) {

          for (int i = 0; i < 2; i++) c.takeItem("gem_of_courage");

          for (int i = 0; i < 4; i++) c.takeItem("tome_of_valor");

          for (int i = 0; i < 3; i++) c.takeItem("sword_of_majesty");

          c.giveItem("heart_of_artherk");

          c.flag("__QUEST_FLAG_ARTHERK_HEART", 1);

          c.giveXp(250000);

          c.sayKey("npc.mithanna.heart.ok");
        }

        return true;
      }
    };
  }

  public MithannaSnowraven(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
