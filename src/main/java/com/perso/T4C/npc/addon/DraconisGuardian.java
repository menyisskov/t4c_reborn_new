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
import java.util.List;

public final class DraconisGuardian extends ScriptedNpc {

  public static final String ID = "DraconisGuardian";

  public static final String DISPLAY_NAME = "${npc.draconisguardian}";

  public static final String SPRITE_BASE = "MonsDraconianLeather";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.draconisguardian}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.draconisguardian.0.0}"),
                  "${npc.topic.draconisguardian.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.draconisguardian.1.0}"),
                  "${npc.topic.draconisguardian.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.draconisguardian.2.0}"),
                  "${npc.topic.draconisguardian.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.draconisguardian.3.0}",
                      "${npc.topic_keyword.draconisguardian.3.1}"),
                  "${npc.topic.draconisguardian.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.draconisguardian.4.0}"),
                  "${npc.topic.draconisguardian.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.draconisguardian.5.0}"),
                  "${npc.topic.draconisguardian.5}",
                  List.of())),
          "DraconisGuardianNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 0, 0, 50000, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onPopup(NpcBehaviorContext c) {

          c.castSelfSpell(10762);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          c.sayKey(p == 41 ? "npc.draconis.progress.41" : "npc.draconis.silence");

          if (p == 41) c.askYesNo("efnisien");
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if (!"efnisien".equals(state)) return false;

          c.sayKey(yes ? "npc.draconis.efnisien.yes" : "npc.draconis.efnisien.no");

          return true;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("DRACONIS")
              || k.equals("PROPHECY")
              || k.equals("STOP")
              || k.contains("BURIAL")
              || k.contains("CHAMBER")) {

            c.sayKey(
                c.flag("ADDON_STORYLINE_PROGRESS") == 41
                    ? "npc.draconis.lore"
                    : "npc.draconis.silence");

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.draconis.name");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.draconis.work");

            return true;
          }

          return false;
        }

        @Override
        public void onAttacked(NpcBehaviorContext c) {

          c.castSelfSpell(10762);

          c.shoutKey("npc.draconis.attacked");
        }
      };

  public DraconisGuardian(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
