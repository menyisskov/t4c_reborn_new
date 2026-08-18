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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "ZionThunderheart", x = 935, y = 1150, z = 0, stationary = false, aggressive = false)
public final class ZionThunderheart extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ZionThunderheart";

  public static final String DISPLAY_NAME = "${npc.zionthunderheart}";

  public static final String SPRITE_BASE = "64kCentaurWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.zionthunderheart}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.0.0}",
                      "${npc.topic_keyword.zionthunderheart.0.1}"),
                  "${npc.topic.zionthunderheart.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.1.0}",
                      "${npc.topic_keyword.zionthunderheart.1.1}",
                      "${npc.topic_keyword.zionthunderheart.1.2}"),
                  "${npc.topic.zionthunderheart.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zionthunderheart.2.0}"),
                  "${npc.topic.zionthunderheart.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.3.0}",
                      "${npc.topic_keyword.zionthunderheart.3.1}"),
                  "${npc.topic.zionthunderheart.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zionthunderheart.4.0}"),
                  "${npc.topic.zionthunderheart.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zionthunderheart.5.0}"),
                  "${npc.topic.zionthunderheart.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.zionthunderheart.6.0}"),
                  "${npc.topic.zionthunderheart.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.7.0}",
                      "${npc.topic_keyword.zionthunderheart.7.1}"),
                  "${npc.topic.zionthunderheart.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.8.0}",
                      "${npc.topic_keyword.zionthunderheart.8.1}"),
                  "${npc.topic.zionthunderheart.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.9.0}",
                      "${npc.topic_keyword.zionthunderheart.9.1}"),
                  "${npc.topic.zionthunderheart.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.10.0}",
                      "${npc.topic_keyword.zionthunderheart.10.1}"),
                  "${npc.topic.zionthunderheart.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.11.0}",
                      "${npc.topic_keyword.zionthunderheart.11.1}"),
                  "${npc.topic.zionthunderheart.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.12.0}",
                      "${npc.topic_keyword.zionthunderheart.12.1}"),
                  "${npc.topic.zionthunderheart.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.13.0}",
                      "${npc.topic_keyword.zionthunderheart.13.1}"),
                  "${npc.topic.zionthunderheart.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.14.0}",
                      "${npc.topic_keyword.zionthunderheart.14.1}"),
                  "${npc.topic.zionthunderheart.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.15.0}",
                      "${npc.topic_keyword.zionthunderheart.15.1}",
                      "${npc.topic_keyword.zionthunderheart.15.2}",
                      "${npc.topic_keyword.zionthunderheart.15.3}"),
                  "${npc.topic.zionthunderheart.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.zionthunderheart.16.0}",
                      "${npc.topic_keyword.zionthunderheart.16.1}",
                      "${npc.topic_keyword.zionthunderheart.16.2}",
                      "${npc.topic_keyword.zionthunderheart.16.3}",
                      "${npc.topic_keyword.zionthunderheart.16.4}"),
                  "${npc.topic.zionthunderheart.16}",
                  List.of())),
          "ZionThunderheartNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_USER_HAS_SKULL_OF_EVIL") >= 1
            && c.flag("__FLAG_USER_HAS_CROWN_OF_CORRUPTION") >= 1
            && c.flag("__FLAG_USER_HAS_ARCANE_SPELLBOOK") >= 2
            && c.flag("__FLAG_USER_KNOWS_WHERE_SKULL_OF_OGRIMAR_IS_HIDDEN") == 0) {

          c.takeItem("skull_of_evil");

          c.takeItem("crown_of_corruption");

          c.takeItem("arcane_spellbook");

          c.takeItem("arcane_spellbook");

          c.flag("__FLAG_USER_KNOWS_WHERE_SKULL_OF_OGRIMAR_IS_HIDDEN", 1);

          c.sayKey("npc.zion.skull.location");

        } else c.sayKey("npc.zion.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("SKULL") && k.contains("OGRIMAR")) {

          if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") != 1) c.sayKey("npc.zion.weakling");
          else if (c.flag("__FLAG_USER_HAS_SKULL_OF_OGRIMAR") == 1) c.sayKey("npc.zion.found");
          else if (c.flag("__FLAG_USER_KNOWS_WHERE_SKULL_OF_OGRIMAR_IS_HIDDEN") == 1)
            c.sayKey("npc.zion.already");
          else c.sayKey("npc.zion.cost");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public ZionThunderheart(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
