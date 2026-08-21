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

@Spawn(type = "ChamberlainThomar", x = 2833, y = 227, z = 4, stationary = false, aggressive = false)
public final class ChamberlainThomar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ChamberlainThomar";

  public static final String DISPLAY_NAME = "${npc.chamberlainthomar}";

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
          "${npc.welcome.chamberlainthomar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.0.0}",
                      "${npc.topic_keyword.chamberlainthomar.0.1}"),
                  "${npc.topic.chamberlainthomar.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.1.0}",
                      "${npc.topic_keyword.chamberlainthomar.1.1}"),
                  "${npc.topic.chamberlainthomar.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.2.0}",
                      "${npc.topic_keyword.chamberlainthomar.2.1}"),
                  "${npc.topic.chamberlainthomar.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chamberlainthomar.3.0}"),
                  "${npc.topic.chamberlainthomar.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.4.0}",
                      "${npc.topic_keyword.chamberlainthomar.4.1}",
                      "${npc.topic_keyword.chamberlainthomar.4.2}"),
                  "${npc.topic.chamberlainthomar.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.5.0}",
                      "${npc.topic_keyword.chamberlainthomar.5.1}",
                      "${npc.topic_keyword.chamberlainthomar.5.2}",
                      "${npc.topic_keyword.chamberlainthomar.5.3}"),
                  "${npc.topic.chamberlainthomar.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.6.0}",
                      "${npc.topic_keyword.chamberlainthomar.6.1}"),
                  "${npc.topic.chamberlainthomar.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chamberlainthomar.7.0}"),
                  "${npc.topic.chamberlainthomar.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.8.0}",
                      "${npc.topic_keyword.chamberlainthomar.8.1}",
                      "${npc.topic_keyword.chamberlainthomar.8.2}"),
                  "${npc.topic.chamberlainthomar.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chamberlainthomar.9.0}"),
                  "${npc.topic.chamberlainthomar.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.10.0}",
                      "${npc.topic_keyword.chamberlainthomar.10.1}"),
                  "${npc.topic.chamberlainthomar.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chamberlainthomar.11.0}"),
                  "${npc.topic.chamberlainthomar.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.12.0}",
                      "${npc.topic_keyword.chamberlainthomar.12.1}"),
                  "${npc.topic.chamberlainthomar.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.13.0}",
                      "${npc.topic_keyword.chamberlainthomar.13.1}"),
                  "${npc.topic.chamberlainthomar.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chamberlainthomar.14.0}"),
                  "${npc.topic.chamberlainthomar.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chamberlainthomar.15.0}"),
                  "${npc.topic.chamberlainthomar.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.16.0}",
                      "${npc.topic_keyword.chamberlainthomar.16.1}",
                      "${npc.topic_keyword.chamberlainthomar.16.2}",
                      "${npc.topic_keyword.chamberlainthomar.16.3}"),
                  "${npc.topic.chamberlainthomar.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chamberlainthomar.17.0}",
                      "${npc.topic_keyword.chamberlainthomar.17.1}",
                      "${npc.topic_keyword.chamberlainthomar.17.2}",
                      "${npc.topic_keyword.chamberlainthomar.17.3}",
                      "${npc.topic_keyword.chamberlainthomar.17.4}"),
                  "${npc.topic.chamberlainthomar.17}",
                  List.of())),
          "ChamberlainThomarNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

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

          if (k.contains("NOBLE GESTURE")) {

            if (c.flag("__QUEST_ISLAND_ACCESS") == 0) {

              c.sayKey("npc.thomar.noaccess");

              return true;
            }

            c.sayKey(
                c.globalFlag("__QUEST_BISHOP_ILLNESS") == 0
                    ? "npc.thomar.cuthana.ask"
                    : "npc.thomar.cuthana.healthy");

            if (c.globalFlag("__QUEST_BISHOP_ILLNESS") == 0) c.askYesNo("cuthana");

            return true;
          }

          if (k.equals("ROYAL KEY")) {

            int q = c.flag("__QUEST_ROYAL_KEY2");

            if (c.flag("__QUEST_FIXED_ALIGNMENT") < 1) c.sayKey("npc.thomar.champion");
            else if (q <= 4) c.sayKey("npc.thomar.key.unknown");
            else if (q == 5) {

              c.sayKey("npc.thomar.oath");

              c.askYesNo("oath");

            } else if (q == 6) {

              c.flag("__QUEST_ROYAL_KEY2", 7);

              c.giveItem("royal_key_2");

              c.sayKey("npc.thomar.key.given");

            } else c.sayKey("npc.thomar.key.once");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if ("cuthana".equals(state)) {

            if (yes) c.flag("__QUEST_CUTHANA", 1);

            c.sayKey(yes ? "npc.thomar.cuthana.accept" : "npc.thomar.no");

            return true;
          }

          if ("oath".equals(state)) {

            if (yes) {

              c.flag("__QUEST_ROYAL_KEY2", 6);

              c.flag("__QUEST_FIXED_ALIGNMENT", 1);

              c.giveXp(5000);

              c.sayKey("npc.thomar.oath.accept");

            } else c.sayKey("npc.thomar.no");

            return true;
          }

          return false;
        }
      };

  public ChamberlainThomar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
