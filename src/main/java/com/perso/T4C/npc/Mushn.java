package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Mushn", x = 1755, y = 1319, z = 0, stationary = false, aggressive = false)
public final class Mushn extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Mushn";

  public static final String DISPLAY_NAME = "${npc.mushn}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.mushn}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mushn.0.0}", "${npc.topic_keyword.mushn.0.1}"),
                  "${npc.topic.mushn.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mushn.1.0}",
                      "${npc.topic_keyword.mushn.1.1}",
                      "${npc.topic_keyword.mushn.1.2}"),
                  "${npc.topic.mushn.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mushn.2.0}"), "${npc.topic.mushn.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mushn.3.0}",
                      "${npc.topic_keyword.mushn.3.1}",
                      "${npc.topic_keyword.mushn.3.2}"),
                  "${npc.topic.mushn.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mushn.4.0}"), "${npc.topic.mushn.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mushn.5.0}"), "${npc.topic.mushn.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mushn.6.0}",
                      "${npc.topic_keyword.mushn.6.1}",
                      "${npc.topic_keyword.mushn.6.2}",
                      "${npc.topic_keyword.mushn.6.3}",
                      "${npc.topic_keyword.mushn.6.4}"),
                  "${npc.topic.mushn.6}",
                  List.of())),
          "MushnNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_CURE_GEENA") == 1) {

          c.giveItem("healing_leaf");

          c.flag("__QUEST_CURE_GEENA", 0);

          c.sayKey("npc.mushn.leaf");

        } else c.sayKey("npc.mushn.welcome");
      }
    };
  }

  public Mushn(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
