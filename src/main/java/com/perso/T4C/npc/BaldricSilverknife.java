package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(
    type = "BaldricSilverknife",
    x = 1573,
    y = 2551,
    z = 0,
    stationary = false,
    aggressive = false)
public final class BaldricSilverknife extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "BaldricSilverknife";

  public static final String DISPLAY_NAME = "${npc.baldricsilverknife}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupStuddedBodyArmor"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger")),
          0,
          List.of(),
          "${npc.welcome.baldricsilverknife}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.0.0}",
                      "${npc.topic_keyword.baldricsilverknife.0.1}"),
                  "${npc.topic.baldricsilverknife.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.1.0}",
                      "${npc.topic_keyword.baldricsilverknife.1.1}"),
                  "${npc.topic.baldricsilverknife.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.2.0}"),
                  "${npc.topic.baldricsilverknife.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.3.0}",
                      "${npc.topic_keyword.baldricsilverknife.3.1}",
                      "${npc.topic_keyword.baldricsilverknife.3.2}"),
                  "${npc.topic.baldricsilverknife.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.4.0}"),
                  "${npc.topic.baldricsilverknife.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.5.0}"),
                  "${npc.topic.baldricsilverknife.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.6.0}"),
                  "${npc.topic.baldricsilverknife.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.7.0}",
                      "${npc.topic_keyword.baldricsilverknife.7.1}"),
                  "${npc.topic.baldricsilverknife.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.8.0}"),
                  "${npc.topic.baldricsilverknife.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.9.0}",
                      "${npc.topic_keyword.baldricsilverknife.9.1}"),
                  "${npc.topic.baldricsilverknife.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.10.0}"),
                  "${npc.topic.baldricsilverknife.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.11.0}"),
                  "${npc.topic.baldricsilverknife.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.12.0}"),
                  "${npc.topic.baldricsilverknife.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.13.0}"),
                  "${npc.topic.baldricsilverknife.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.14.0}"),
                  "${npc.topic.baldricsilverknife.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.15.0}"),
                  "${npc.topic.baldricsilverknife.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.16.0}"),
                  "${npc.topic.baldricsilverknife.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.17.0}"),
                  "${npc.topic.baldricsilverknife.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.18.0}",
                      "${npc.topic_keyword.baldricsilverknife.18.1}"),
                  "${npc.topic.baldricsilverknife.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.19.0}",
                      "${npc.topic_keyword.baldricsilverknife.19.1}"),
                  "${npc.topic.baldricsilverknife.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.20.0}",
                      "${npc.topic_keyword.baldricsilverknife.20.1}"),
                  "${npc.topic.baldricsilverknife.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.21.0}"),
                  "${npc.topic.baldricsilverknife.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.22.0}"),
                  "${npc.topic.baldricsilverknife.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.23.0}"),
                  "${npc.topic.baldricsilverknife.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.24.0}"),
                  "${npc.topic.baldricsilverknife.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.25.0}"),
                  "${npc.topic.baldricsilverknife.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.26.0}",
                      "${npc.topic_keyword.baldricsilverknife.26.1}"),
                  "${npc.topic.baldricsilverknife.26}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.27.0}",
                      "${npc.topic_keyword.baldricsilverknife.27.1}"),
                  "${npc.topic.baldricsilverknife.27}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.baldricsilverknife.28.0}"),
                  "${npc.topic.baldricsilverknife.28}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.29.0}",
                      "${npc.topic_keyword.baldricsilverknife.29.1}",
                      "${npc.topic_keyword.baldricsilverknife.29.2}",
                      "${npc.topic_keyword.baldricsilverknife.29.3}"),
                  "${npc.topic.baldricsilverknife.29}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.baldricsilverknife.30.0}",
                      "${npc.topic_keyword.baldricsilverknife.30.1}",
                      "${npc.topic_keyword.baldricsilverknife.30.2}",
                      "${npc.topic_keyword.baldricsilverknife.30.3}",
                      "${npc.topic_keyword.baldricsilverknife.30.4}"),
                  "${npc.topic.baldricsilverknife.30}",
                  List.of())),
          "Thief",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("SOMETHING")) {

          if (c.hasItem("blood_dagger") && c.hasItem("gleaming_shard")) {

            c.sayKey("npc.baldric.drum.offer");

            c.askYesNo("drum");

          } else c.sayKey("npc.baldric.drum.need");

          return true;
        }

        if (k.contains("GLEAMING") || k.contains("SHARD")) {

          c.sayKey("npc.baldric.shard");

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if (!"drum".equals(state)) return false;

        if (yes && c.hasItem("blood_dagger") && c.hasItem("gleaming_shard")) {

          c.takeItem("blood_dagger");

          c.takeItem("gleaming_shard");

          c.giveItem("baldric_drum");

          c.sayKey("npc.baldric.drum.given");

        } else if (yes) c.sayKey("npc.baldric.drum.missing");
        else c.sayKey("npc.baldric.drum.no");

        return true;
      }
    };
  }

  public BaldricSilverknife(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
