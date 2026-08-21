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

@Spawn(type = "Kilhiam", x = 2961, y = 1058, z = 0, stationary = false, aggressive = false)
public final class Kilhiam extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Kilhiam";

  public static final String DISPLAY_NAME = "${npc.kilhiam}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.kilhiam}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kilhiam.0.0}", "${npc.topic_keyword.kilhiam.0.1}"),
                  "${npc.topic.kilhiam.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kilhiam.1.0}"), "${npc.topic.kilhiam.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kilhiam.2.0}",
                      "${npc.topic_keyword.kilhiam.2.1}",
                      "${npc.topic_keyword.kilhiam.2.2}"),
                  "${npc.topic.kilhiam.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kilhiam.3.0}"), "${npc.topic.kilhiam.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kilhiam.4.0}"), "${npc.topic.kilhiam.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kilhiam.5.0}"), "${npc.topic.kilhiam.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kilhiam.6.0}", "${npc.topic_keyword.kilhiam.6.1}"),
                  "${npc.topic.kilhiam.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kilhiam.7.0}"), "${npc.topic.kilhiam.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kilhiam.8.0}"), "${npc.topic.kilhiam.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kilhiam.9.0}",
                      "${npc.topic_keyword.kilhiam.9.1}",
                      "${npc.topic_keyword.kilhiam.9.2}"),
                  "${npc.topic.kilhiam.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kilhiam.10.0}"),
                  "${npc.topic.kilhiam.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kilhiam.11.0}",
                      "${npc.topic_keyword.kilhiam.11.1}",
                      "${npc.topic_keyword.kilhiam.11.2}"),
                  "${npc.topic.kilhiam.11}",
                  List.of())),
          "KilhiamNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

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
        public void onConversationStart(NpcBehaviorContext c) {

          if (c.flag("ADDON_CRIMSONSCALE_LETTER") == 0) {

            c.sayKey("npc.kilhiam.letter");

            c.giveItem("letter_from_crimsonscale");

            c.flag("ADDON_CRIMSONSCALE_LETTER", 1);

            return;
          }

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          c.sayKey(
              p == 19
                  ? "npc.kilhiam.highpriest"
                  : p == 20
                      ? "npc.kilhiam.brotherkiran"
                      : p < 42 ? "npc.kilhiam.temple" : "npc.kilhiam.welcome");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("SANCTUARY")) {

            c.sayKey("npc.kilhiam.sanctuary.ask");

            c.askYesNo("sanctuary");

            return true;
          }

          if (k.equals("TEACH") || k.equals("LEARN")) {

            c.sayKey("npc.kilhiam.light.ask");

            c.askYesNo("light");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if ("light".equals(state)) {

            if (yes) c.openSpellLearning(java.util.List.of("light"));
            else c.sayKey("npc.kilhiam.no");

            return true;
          }

          if ("donation".equals(state)) {

            if (yes && c.player().getGold() >= 1500) {

              c.player().addGold(-1500);

              c.flag("__FLAG_DEATH_LOCATION", (2941 << 20) | (1062 << 8));

              c.sayKey("npc.kilhiam.sanctuary.done");

            } else if (yes) c.sayKey("npc.kilhiam.sanctuary.poor");
            else c.sayKey("npc.kilhiam.no");

            return true;
          }

          if (!"sanctuary".equals(state)) return false;

          if (!yes) {

            c.sayKey("npc.kilhiam.no");

            return true;
          }

          if (c.player().getGold() < 1500) {

            c.sayKey("npc.kilhiam.sanctuary.poor");

            return true;
          }

          c.sayKey("npc.kilhiam.sanctuary.confirm");

          c.askYesNo("donation");

          return true;
        }
      };

  public Kilhiam(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
