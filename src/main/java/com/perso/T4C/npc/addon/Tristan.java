package com.perso.T4C.npc.addon;

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

@Spawn(type = "Tristan", x = 295, y = 738, z = 0, stationary = false, aggressive = false)
public final class Tristan extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Tristan";

  public static final String DISPLAY_NAME = "${npc.tristan}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupRedRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupLichStaff")),
          0,
          List.of(),
          "${npc.welcome.tristan}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tristan.0.0}"), "${npc.topic.tristan.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tristan.1.0}"), "${npc.topic.tristan.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tristan.2.0}"), "${npc.topic.tristan.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tristan.3.0}"), "${npc.topic.tristan.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tristan.4.0}"), "${npc.topic.tristan.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tristan.5.0}", "${npc.topic_keyword.tristan.5.1}"),
                  "${npc.topic.tristan.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.tristan.6.0}",
                      "${npc.topic_keyword.tristan.6.1}",
                      "${npc.topic_keyword.tristan.6.2}",
                      "${npc.topic_keyword.tristan.6.3}",
                      "${npc.topic_keyword.tristan.6.4}"),
                  "${npc.topic.tristan.6}",
                  List.of())),
          "WandererNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int q = c.flag("ADDON_SERAPH_ARMOR_QUEST");

          if (q == 1) c.sayKey("npc.tristan.progress.quest");
          else if (c.flag("NUMBER_OF_REMORTS") >= 1) {

            c.sayKey("npc.tristan.progress.winged");

            c.askYesNo("angel");

          } else c.sayKey("npc.tristan.hello");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String s, boolean yes) {

          if ("angel".equals(s)) {

            c.sayKey("npc.tristan.wings");

            c.askYesNo("touch");

            return true;
          }

          if ("touch".equals(s)) {

            c.sayKey(yes ? "npc.tristan.touch.yes" : "npc.tristan.touch.no");

            return true;
          }

          return false;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("RECENTLY")) {

            c.sayKey("npc.tristan.recently");

            return true;
          }

          if (k.equals("DIGGING")) {

            c.sayKey("npc.tristan.digging");

            if (c.flag("NUMBER_OF_REMORTS") >= 1) c.flag("ADDON_SERAPH_ARMOR_QUEST", 1);

            return true;
          }

          if (k.equals("MORDRED")) {

            c.sayKey("npc.tristan.mordred");

            return true;
          }

          return false;
        }
      };

  public Tristan(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
