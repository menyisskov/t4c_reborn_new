package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Annabelle", x = 1551, y = 205, z = 0, stationary = false, aggressive = false)
public final class Annabelle extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Annabelle";

  public static final String DISPLAY_NAME = "${npc.annabelle}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.annabelle}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annabelle.0.0}", "${npc.topic_keyword.annabelle.0.1}"),
                  "${npc.topic.annabelle.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.1.0}"),
                  "${npc.topic.annabelle.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annabelle.2.0}",
                      "${npc.topic_keyword.annabelle.2.1}",
                      "${npc.topic_keyword.annabelle.2.2}"),
                  "${npc.topic.annabelle.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.3.0}"),
                  "${npc.topic.annabelle.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.4.0}"),
                  "${npc.topic.annabelle.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annabelle.5.0}", "${npc.topic_keyword.annabelle.5.1}"),
                  "${npc.topic.annabelle.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.6.0}"),
                  "${npc.topic.annabelle.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.7.0}"),
                  "${npc.topic.annabelle.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.8.0}"),
                  "${npc.topic.annabelle.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.9.0}"),
                  "${npc.topic.annabelle.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.10.0}"),
                  "${npc.topic.annabelle.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.11.0}"),
                  "${npc.topic.annabelle.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.12.0}"),
                  "${npc.topic.annabelle.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.13.0}"),
                  "${npc.topic.annabelle.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.14.0}"),
                  "${npc.topic.annabelle.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.15.0}"),
                  "${npc.topic.annabelle.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.16.0}"),
                  "${npc.topic.annabelle.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.17.0}"),
                  "${npc.topic.annabelle.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.18.0}"),
                  "${npc.topic.annabelle.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.19.0}"),
                  "${npc.topic.annabelle.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.20.0}"),
                  "${npc.topic.annabelle.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.21.0}"),
                  "${npc.topic.annabelle.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.22.0}"),
                  "${npc.topic.annabelle.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annabelle.23.0}"),
                  "${npc.topic.annabelle.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annabelle.24.0}",
                      "${npc.topic_keyword.annabelle.24.1}",
                      "${npc.topic_keyword.annabelle.24.2}",
                      "${npc.topic_keyword.annabelle.24.3}",
                      "${npc.topic_keyword.annabelle.24.4}"),
                  "${npc.topic.annabelle.24}",
                  List.of())),
          "Priestess_Annabelle",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          c.sayKey("npc.welcome.annabelle");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.contains("OLD HERMIT") || k.contains("THEORN LEMNEARAN")) {

            c.flag("ANNABELLE", 1);

            c.sayKey("npc.topic.annabelle.12");

            return true;
          }

          if (k.equals("CRAPO")) {

            c.flag("CHOICE", c.flag("CHOICE") == 1 ? 2 : 0);

            return true;
          }

          if (k.equals("GREGRE")) {

            c.flag("CHOICE", c.flag("CHOICE") == 0 ? 1 : 0);

            return true;
          }

          if (k.equals("BALORK")) {

            c.sayKey("npc.topic.annabelle.14");

            c.askYesNo("BALORK");

            return true;
          }

          if (k.equals("BRAND")) {

            c.sayKey(
                c.hasFlag("BALORK_BRAND", 1) ? "npc.topic.annabelle.20" : "npc.topic.annabelle.21");

            return true;
          }

          if (k.equals("HEAL")) {

            if (c.isWounded()) {

              c.healToHalf();

              c.systemMessage("npc.annabelle.heal.message");
            }

            c.sayKey("npc.topic.annabelle.22");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if ("BALORK".equals(state)) {

            c.sayKey(yes ? "npc.topic.annabelle.15" : "npc.topic.annabelle.16");

            return true;
          }

          return false;
        }
      };

  public Annabelle(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
