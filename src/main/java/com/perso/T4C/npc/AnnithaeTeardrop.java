package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "AnnithaeTeardrop", x = 300, y = 830, z = 0, stationary = false, aggressive = false)
public final class AnnithaeTeardrop extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "AnnithaeTeardrop";

  public static final String DISPLAY_NAME = "${npc.annithaeteardrop}";

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
          "${npc.welcome.annithaeteardrop}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annithaeteardrop.0.0}",
                      "${npc.topic_keyword.annithaeteardrop.0.1}"),
                  "${npc.topic.annithaeteardrop.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annithaeteardrop.1.0}",
                      "${npc.topic_keyword.annithaeteardrop.1.1}"),
                  "${npc.topic.annithaeteardrop.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annithaeteardrop.2.0}",
                      "${npc.topic_keyword.annithaeteardrop.2.1}",
                      "${npc.topic_keyword.annithaeteardrop.2.2}"),
                  "${npc.topic.annithaeteardrop.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annithaeteardrop.3.0}"),
                  "${npc.topic.annithaeteardrop.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annithaeteardrop.4.0}",
                      "${npc.topic_keyword.annithaeteardrop.4.1}",
                      "${npc.topic_keyword.annithaeteardrop.4.2}"),
                  "${npc.topic.annithaeteardrop.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annithaeteardrop.5.0}",
                      "${npc.topic_keyword.annithaeteardrop.5.1}"),
                  "${npc.topic.annithaeteardrop.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.annithaeteardrop.6.0}"),
                  "${npc.topic.annithaeteardrop.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annithaeteardrop.7.0}",
                      "${npc.topic_keyword.annithaeteardrop.7.1}",
                      "${npc.topic_keyword.annithaeteardrop.7.2}",
                      "${npc.topic_keyword.annithaeteardrop.7.3}",
                      "${npc.topic_keyword.annithaeteardrop.7.4}"),
                  "${npc.topic.annithaeteardrop.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annithaeteardrop.8.0}",
                      "${npc.topic_keyword.annithaeteardrop.8.1}"),
                  "${npc.topic.annithaeteardrop.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.annithaeteardrop.9.0}",
                      "${npc.topic_keyword.annithaeteardrop.9.1}",
                      "${npc.topic_keyword.annithaeteardrop.9.2}",
                      "${npc.topic_keyword.annithaeteardrop.9.3}"),
                  "${npc.topic.annithaeteardrop.9}",
                  List.of())),
          "AnnithaeTeardropNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int h = java.time.LocalTime.now().getHour();

        c.sayKey(h >= 22 || h < 6 ? "npc.annithae.night" : "npc.annithae.day");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String keyword) {

        String k = keyword == null ? "" : keyword.toUpperCase(java.util.Locale.ROOT);

        if ((k.contains("TEAR") && k.contains("PEARL")) || k.equals("TEARDROPPED PEARL")) {

          c.askYesNo("pearl");

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if (!"pearl".equals(state)) return false;

        if (yes && c.player().getGold() > 12000) {

          c.player().addGold(-12000);

          c.giveItem("tear_shaped_pearl");

          c.sayKey("npc.annithae.pearl.done");

        } else if (yes) c.sayKey("npc.annithae.pearl.poor");
        else c.sayKey("npc.annithae.pearl.no");

        return true;
      }
    };
  }

  public AnnithaeTeardrop(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
