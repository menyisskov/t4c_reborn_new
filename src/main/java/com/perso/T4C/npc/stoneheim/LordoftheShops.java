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

@Spawn(type = "LordoftheShops", x = 715, y = 865, z = 0, stationary = false, aggressive = false)
public final class LordoftheShops extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "LordoftheShops";

  public static final String DISPLAY_NAME = "${npc.lordoftheshops}";

  public static final String SPRITE_BASE = "@invisible";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.lordoftheshops}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.0.0}",
                      "${npc.topic_keyword.lordoftheshops.0.1}"),
                  "${npc.topic.lordoftheshops.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.1.0}",
                      "${npc.topic_keyword.lordoftheshops.1.1}",
                      "${npc.topic_keyword.lordoftheshops.1.2}"),
                  "${npc.topic.lordoftheshops.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.2.0}",
                      "${npc.topic_keyword.lordoftheshops.2.1}"),
                  "${npc.topic.lordoftheshops.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.3.0}",
                      "${npc.topic_keyword.lordoftheshops.3.1}"),
                  "${npc.topic.lordoftheshops.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.4.0}",
                      "${npc.topic_keyword.lordoftheshops.4.1}"),
                  "${npc.topic.lordoftheshops.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.5.0}",
                      "${npc.topic_keyword.lordoftheshops.5.1}",
                      "${npc.topic_keyword.lordoftheshops.5.2}"),
                  "${npc.topic.lordoftheshops.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.6.0}",
                      "${npc.topic_keyword.lordoftheshops.6.1}"),
                  "${npc.topic.lordoftheshops.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.7.0}",
                      "${npc.topic_keyword.lordoftheshops.7.1}"),
                  "${npc.topic.lordoftheshops.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordoftheshops.8.0}"),
                  "${npc.topic.lordoftheshops.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.9.0}",
                      "${npc.topic_keyword.lordoftheshops.9.1}"),
                  "${npc.topic.lordoftheshops.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordoftheshops.10.0}"),
                  "${npc.topic.lordoftheshops.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordoftheshops.11.0}"),
                  "${npc.topic.lordoftheshops.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.12.0}",
                      "${npc.topic_keyword.lordoftheshops.12.1}"),
                  "${npc.topic.lordoftheshops.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordoftheshops.13.0}"),
                  "${npc.topic.lordoftheshops.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.14.0}",
                      "${npc.topic_keyword.lordoftheshops.14.1}",
                      "${npc.topic_keyword.lordoftheshops.14.2}",
                      "${npc.topic_keyword.lordoftheshops.14.3}",
                      "${npc.topic_keyword.lordoftheshops.14.4}"),
                  "${npc.topic.lordoftheshops.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordoftheshops.15.0}",
                      "${npc.topic_keyword.lordoftheshops.15.1}",
                      "${npc.topic_keyword.lordoftheshops.15.2}",
                      "${npc.topic_keyword.lordoftheshops.15.3}"),
                  "${npc.topic.lordoftheshops.15}",
                  List.of())),
          "LordoftheShopsNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.npcFlag("LORD_OF_THE_SHOPS_TIMER") == 0) {

          c.npcFlag("LORD_OF_THE_SHOPS_TIMER", 1);

          c.castSelfSpell(10725);

          c.sayKey("npc.lordoftheshops.short");

        } else c.sayKey("npc.lordoftheshops.hurry");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("ASSHOLE")
            || k.contains("FUCK")
            || k.contains("SUCK")
            || k.contains("WHORE")
            || k.contains(" ASS ")) {

          c.sayKey("npc.lordoftheshops.leave");

          c.castSelfSpell(10726);

          return true;
        }

        if (k.equals("FAREWELL") || k.equals("BYE") || k.equals("LEAVE") || k.equals("QUIT")) {

          c.sayKey("npc.lordoftheshops.goodbye");

          c.castSelfSpell(10726);

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public LordoftheShops(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
