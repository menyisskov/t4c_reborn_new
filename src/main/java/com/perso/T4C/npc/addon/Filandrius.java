package com.perso.T4C.npc.addon;

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
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Filandrius extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Filandrius";

  public static final String DISPLAY_NAME = "${npc.filandrius}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupRedRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupSimpleStaff")),
          0,
          List.of(),
          "${npc.welcome.filandrius}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.filandrius.0.0}", "${npc.topic_keyword.filandrius.0.1}"),
                  "${npc.topic.filandrius.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.1.0}"),
                  "${npc.topic.filandrius.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.2.0}"),
                  "${npc.topic.filandrius.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.3.0}"),
                  "${npc.topic.filandrius.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.filandrius.4.0}", "${npc.topic_keyword.filandrius.4.1}"),
                  "${npc.topic.filandrius.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.5.0}"),
                  "${npc.topic.filandrius.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.6.0}"),
                  "${npc.topic.filandrius.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.filandrius.7.0}", "${npc.topic_keyword.filandrius.7.1}"),
                  "${npc.topic.filandrius.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.8.0}"),
                  "${npc.topic.filandrius.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.9.0}"),
                  "${npc.topic.filandrius.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.10.0}"),
                  "${npc.topic.filandrius.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.filandrius.11.0}",
                      "${npc.topic_keyword.filandrius.11.1}",
                      "${npc.topic_keyword.filandrius.11.2}"),
                  "${npc.topic.filandrius.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.12.0}"),
                  "${npc.topic.filandrius.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.filandrius.13.0}"),
                  "${npc.topic.filandrius.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.filandrius.14.0}",
                      "${npc.topic_keyword.filandrius.14.1}",
                      "${npc.topic_keyword.filandrius.14.2}",
                      "${npc.topic_keyword.filandrius.14.3}",
                      "${npc.topic_keyword.filandrius.14.4}"),
                  "${npc.topic.filandrius.14}",
                  List.of())),
          "FilandriusNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_FILANDRIUS_QUEST_PROGRESS");

          if (p == 0) c.sayKey("npc.filandrius.progress.0");
          else if (p == 1) c.sayKey("npc.filandrius.progress.1");
          else if (p == 2 && c.hasItem("tome_of_arcane_knowledge")) {

            c.sayKey("npc.filandrius.progress.2");

            c.takeItem("tome_of_arcane_knowledge");

            c.giveItem("belt_of_unstable_protection");

            c.giveItem("gem_of_unstable_protection");

            c.flag("ADDON_FILANDRIUS_QUEST_PROGRESS", 3);

          } else if (p == 3)
            c.sayKey(
                c.hasItem("hel_soulstone")
                    ? "npc.filandrius.progress.3.soul"
                    : "npc.filandrius.progress.3");
          else if (p == 4) c.sayKey("npc.filandrius.progress.4");
          else if (p == 5) c.sayKey("npc.filandrius.progress.5");
          else c.sayKey("npc.filandrius.progress.done");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_FILANDRIUS_QUEST_PROGRESS");

          boolean veteran = c.player().getLevel() >= 50;

          if ((k.contains("GET") && k.contains("IT")) && p == 0 && veteran) {

            c.sayKey("npc.filandrius.get");

            c.flag("ADDON_FILANDRIUS_QUEST_PROGRESS", 1);

            return true;
          }

          if ((k.equals("NEED") || k.equals("FILNAR") || k.equals("DARKNESS"))
              && p == 0
              && !veteran) {

            c.sayKey("npc.filandrius.busy");

            return true;
          }

          if (k.equals("BROWSE") || k.equals("BROWS")) {

            c.sayKey(p >= 3 ? "npc.filandrius.browse" : "npc.filandrius.busy");

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.filandrius.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.filandrius.work");

            return true;
          }

          return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
        }
      };

  public Filandrius(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
