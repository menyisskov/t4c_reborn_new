package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "KirlorDhul", x = 2967, y = 1111, z = 0, stationary = false, aggressive = false)
public final class KirlorDhul extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "KirlorDhul";

  public static final String DISPLAY_NAME = "${npc.kirlordhul}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.kirlordhul}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kirlordhul.0.0}", "${npc.topic_keyword.kirlordhul.0.1}"),
                  "${npc.topic.kirlordhul.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kirlordhul.1.0}",
                      "${npc.topic_keyword.kirlordhul.1.1}",
                      "${npc.topic_keyword.kirlordhul.1.2}",
                      "${npc.topic_keyword.kirlordhul.1.3}"),
                  "${npc.topic.kirlordhul.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.2.0}"),
                  "${npc.topic.kirlordhul.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kirlordhul.3.0}",
                      "${npc.topic_keyword.kirlordhul.3.1}",
                      "${npc.topic_keyword.kirlordhul.3.2}"),
                  "${npc.topic.kirlordhul.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.4.0}"),
                  "${npc.topic.kirlordhul.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.5.0}"),
                  "${npc.topic.kirlordhul.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.6.0}"),
                  "${npc.topic.kirlordhul.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.7.0}"),
                  "${npc.topic.kirlordhul.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.8.0}"),
                  "${npc.topic.kirlordhul.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.9.0}"),
                  "${npc.topic.kirlordhul.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.10.0}"),
                  "${npc.topic.kirlordhul.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kirlordhul.11.0}",
                      "${npc.topic_keyword.kirlordhul.11.1}"),
                  "${npc.topic.kirlordhul.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.12.0}"),
                  "${npc.topic.kirlordhul.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.13.0}"),
                  "${npc.topic.kirlordhul.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.14.0}"),
                  "${npc.topic.kirlordhul.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.15.0}"),
                  "${npc.topic.kirlordhul.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.16.0}"),
                  "${npc.topic.kirlordhul.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kirlordhul.17.0}"),
                  "${npc.topic.kirlordhul.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kirlordhul.18.0}",
                      "${npc.topic_keyword.kirlordhul.18.1}",
                      "${npc.topic_keyword.kirlordhul.18.2}",
                      "${npc.topic_keyword.kirlordhul.18.3}",
                      "${npc.topic_keyword.kirlordhul.18.4}"),
                  "${npc.topic.kirlordhul.18}",
                  List.of())),
          "Lord_Kirlor",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_ADDON_STORYLINE_PROGRESS") == 21) c.sayKey("npc.kirlor.after");
        else c.sayKey("npc.kirlor.welcome");
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (java.util.concurrent.ThreadLocalRandom.current().nextInt(20) != 0) return;

        int[][] positions = {
          {2690, 905},
          {2691, 905},
          {2692, 905},
          {2687, 911},
          {2697, 913},
          {2761, 919},
          {2751, 908},
          {2745, 931},
          {2739, 940},
          {2702, 901},
          {2685, 895}
        };

        String[] monsters = {"DORKENROTSMELL", "NPCGOBLINSCOUT", "NPCGOBLINWARRIOR"};

        for (int i = 0; i < positions.length; i++)
          c.summon(monsters[Math.min(i, monsters.length - 1)], positions[i][0], positions[i][1], 0);
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("REGISTER") || k.equals("PROBLEM")) {

          c.askYesNo("kirlor_register");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"kirlor_register".equals(s)) return false;

        if (yes) {

          if (c.flag("__QUEST_MERCHANT") == 1 && c.hasItem("merchant_letter"))
            c.sayKey("npc.kirlor.letter");
          else if (c.player().getLevel() <= 4) c.sayKey("npc.kirlor.lowlevel");
          else {

            c.giveItem("merchant_letter");

            c.flag("__QUEST_MERCHANT", 1);

            c.sayKey("npc.kirlor.registered");
          }
        }

        return true;
      }
    };
  }

  public KirlorDhul(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
