package com.perso.T4C.npc.remort;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Archibald extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Archibald";

  public static final String DISPLAY_NAME = "${npc.archibald}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1")),
          0,
          List.of(),
          "${npc.welcome.archibald}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archibald.0.0}", "${npc.topic_keyword.archibald.0.1}"),
                  "${npc.topic.archibald.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.archibald.1.0}"),
                  "${npc.topic.archibald.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archibald.2.0}",
                      "${npc.topic_keyword.archibald.2.1}",
                      "${npc.topic_keyword.archibald.2.2}"),
                  "${npc.topic.archibald.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archibald.3.0}",
                      "${npc.topic_keyword.archibald.3.1}",
                      "${npc.topic_keyword.archibald.3.2}"),
                  "${npc.topic.archibald.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.archibald.4.0}",
                      "${npc.topic_keyword.archibald.4.1}",
                      "${npc.topic_keyword.archibald.4.2}"),
                  "${npc.topic.archibald.4}",
                  List.of())),
          "ArchibaldNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public Archibald(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          c.sayKey(
              c.flag("__FLAG_NUMBER_OF_REMORTS") >= 1
                  ? "npc.welcome.archibald"
                  : "npc.archibald.noSeraph");
        }

        @Override
        public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (c.flag("__FLAG_NUMBER_OF_REMORTS") < 1) {

            c.sayKey("npc.archibald.noSeraph");

            return true;
          }

          if (k.contains("FOREST") && k.contains("NO") && k.contains("RETURN")) {

            c.sayKey("npc.topic.archibald.3");

            c.askYesNo("forest");

            return true;
          }

          if (k.equals("BYE") || k.equals("FAREWELL") || k.equals("LEAVE")) {

            c.sayKey("npc.topic.archibald.4");

            c.endConversation();

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(
            com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

          if (!"forest".equals(state)) return false;

          if (yes) {

            c.sayKey("npc.archibald.forest.yes");

            c.teleport(2778, 2006, 0);

          } else c.sayKey("npc.archibald.forest.no");

          return true;
        }
      };
}
