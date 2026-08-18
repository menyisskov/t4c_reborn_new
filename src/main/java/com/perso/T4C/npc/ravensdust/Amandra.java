package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Amandra", x = 1584, y = 2518, z = 0, stationary = false, aggressive = false)
public final class Amandra extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Amandra";

  public static final String DISPLAY_NAME = "${npc.amandra}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoPlateBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoPlateBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "WoPlateLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "WoPlateHelm"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "WoPlateGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "WoPlateGloveL")),
          0,
          List.of(),
          "${npc.welcome.amandra}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amandra.0.0}"), "${npc.topic.amandra.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amandra.1.0}"), "${npc.topic.amandra.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amandra.2.0}"), "${npc.topic.amandra.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amandra.3.0}"), "${npc.topic.amandra.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amandra.4.0}"), "${npc.topic.amandra.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amandra.5.0}"), "${npc.topic.amandra.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amandra.6.0}"), "${npc.topic.amandra.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amandra.7.0}"), "${npc.topic.amandra.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amandra.8.0}", "${npc.topic_keyword.amandra.8.1}"),
                  "${npc.topic.amandra.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.amandra.9.0}",
                      "${npc.topic_keyword.amandra.9.1}",
                      "${npc.topic_keyword.amandra.9.2}"),
                  "${npc.topic.amandra.9}",
                  List.of())),
          "AmandraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 50, 46, 46, 1000000, 370, 65535, "1d36+27"));

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
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (!(k.equals("BROTHER") || k.equals("VISITOR"))) return false;

          int p = c.flag("__QUEST_VISITOR_SPOTTED");

          if (p < 5) {

            c.sayKey("npc.amandra.no");

            return true;
          }

          if (p == 5) {

            c.flag("__QUEST_VISITOR_SPOTTED", 6);

            c.flag("__FLAG_ASKED_ABOUT_SWORD", 2);

            c.sayKey("npc.amandra.sword.start");

            return true;
          }

          if (p == 6) {

            int n = c.itemCount("fake_blade_of_ruin");

            if (n == 0) {

              c.sayKey("npc.amandra.sword.missing");

              return true;
            }

            while (c.hasItem("fake_blade_of_ruin")) c.takeItem("fake_blade_of_ruin");

            c.flag("__QUEST_VISITOR_SPOTTED", 7);

            c.flag("__FLAG_ASKED_ABOUT_SWORD", 7);

            c.sayKey("npc.amandra.sword.fake");

            c.askYesNo("sword");

            return true;
          }

          c.sayKey("npc.amandra.sword.search");

          return true;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if (!"sword".equals(state)) return false;

          c.flag("__QUEST_VISITOR_SPOTTED", 7);

          c.flag("__FLAG_ASKED_ABOUT_SWORD", 7);

          c.sayKey(yes ? "npc.amandra.sword.lie" : "npc.amandra.sword.no");

          return true;
        }
      };

  public Amandra(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
