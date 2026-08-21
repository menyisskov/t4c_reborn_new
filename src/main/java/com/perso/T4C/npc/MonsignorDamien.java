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

@Spawn(type = "MonsignorDamien", x = 1030, y = 2090, z = 0, stationary = false, aggressive = false)
public final class MonsignorDamien extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MonsignorDamien";

  public static final String DISPLAY_NAME = "${npc.monsignordamien}";

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
          "${npc.welcome.monsignordamien}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.0.0}"),
                  "${npc.topic.monsignordamien.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.1.0}"),
                  "${npc.topic.monsignordamien.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.monsignordamien.2.0}",
                      "${npc.topic_keyword.monsignordamien.2.1}"),
                  "${npc.topic.monsignordamien.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.monsignordamien.3.0}",
                      "${npc.topic_keyword.monsignordamien.3.1}",
                      "${npc.topic_keyword.monsignordamien.3.2}"),
                  "${npc.topic.monsignordamien.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.4.0}"),
                  "${npc.topic.monsignordamien.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.monsignordamien.5.0}",
                      "${npc.topic_keyword.monsignordamien.5.1}"),
                  "${npc.topic.monsignordamien.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.6.0}"),
                  "${npc.topic.monsignordamien.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.7.0}"),
                  "${npc.topic.monsignordamien.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.monsignordamien.8.0}",
                      "${npc.topic_keyword.monsignordamien.8.1}"),
                  "${npc.topic.monsignordamien.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.9.0}"),
                  "${npc.topic.monsignordamien.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.10.0}"),
                  "${npc.topic.monsignordamien.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.11.0}"),
                  "${npc.topic.monsignordamien.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.12.0}"),
                  "${npc.topic.monsignordamien.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.monsignordamien.13.0}",
                      "${npc.topic_keyword.monsignordamien.13.1}"),
                  "${npc.topic.monsignordamien.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.14.0}"),
                  "${npc.topic.monsignordamien.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.15.0}"),
                  "${npc.topic.monsignordamien.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.16.0}"),
                  "${npc.topic.monsignordamien.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.17.0}"),
                  "${npc.topic.monsignordamien.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.18.0}"),
                  "${npc.topic.monsignordamien.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.19.0}"),
                  "${npc.topic.monsignordamien.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.20.0}"),
                  "${npc.topic.monsignordamien.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.21.0}"),
                  "${npc.topic.monsignordamien.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.monsignordamien.22.0}"),
                  "${npc.topic.monsignordamien.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.monsignordamien.23.0}",
                      "${npc.topic_keyword.monsignordamien.23.1}",
                      "${npc.topic_keyword.monsignordamien.23.2}",
                      "${npc.topic_keyword.monsignordamien.23.3}",
                      "${npc.topic_keyword.monsignordamien.23.4}"),
                  "${npc.topic.monsignordamien.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.monsignordamien.24.0}",
                      "${npc.topic_keyword.monsignordamien.24.1}",
                      "${npc.topic_keyword.monsignordamien.24.2}",
                      "${npc.topic_keyword.monsignordamien.24.3}",
                      "${npc.topic_keyword.monsignordamien.24.4}"),
                  "${npc.topic.monsignordamien.24}",
                  List.of())),
          "MonsignorDamienNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("ASSASSIN_SUMMONED") == 0) {

          c.summon("SHADEENMASKEDASSASSIN", c.npcTileX(), c.npcTileY(), 0);

          c.flag("ASSASSIN_SUMMONED", 1);
        }
      }

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int subplot = c.flag("__QUEST_DAMIEN_SUBPLOT");

        if ((subplot == 1 || subplot == 2) && c.flag("__QUEST_SEEK_SHADEEN") == 8)
          c.flag("__QUEST_DAMIEN_SUBPLOT", 3);

        if (subplot == 1 && c.hasItem("letter_from_damien_to_xanth"))
          c.flag("__QUEST_DAMIEN_SUBPLOT", 2);

        c.sayKey("npc.welcome.monsignordamien");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("ELVENBANE")) {

          if (c.flag("__FLAG_EVIL_QUEST_COMPLETED_ON_RD") == 1
              && c.flag("__DAMIEN_HAS_REMOVED_ELVENBANE") == 0) {

            c.sayKey("npc.damien.elvenbane.ask");

            c.askYesNo("elvenbane");

          } else if (c.flag("__DAMIEN_HAS_REMOVED_ELVENBANE") == 1)
            c.sayKey("npc.damien.elvenbane.gone");
          else
            c.sayKey(
                c.flag("__QUEST_FIXED_ALIGNMENT") == -1
                    ? "npc.damien.elvenbane.info"
                    : "npc.damien.none");

          return true;
        }

        if (k.equals("SHADEEN") || k.contains("KILL SHADEEN") || k.equals("HELP")) {

          if (c.flag("__QUEST_FIXED_ALIGNMENT") == 1) c.sayKey("npc.damien.aligned");
          else {

            int q = c.flag("__QUEST_DAMIEN_SUBPLOT");

            c.sayKey("npc.damien.shadeen." + Math.min(q, 4));

            if (q == 1) c.askYesNo("price");
          }

          return true;
        }

        if (k.equals("GIFT") || k.equals("TOKEN") || k.equals("ABANDON")) {

          if (c.flag("__QUEST_DAMIEN_SUBPLOT") != 1) {

            c.sayKey("npc.damien.none");

            return true;
          }

          int mult = k.equals("GIFT") ? 2 : k.equals("TOKEN") ? 3 : 5,
              p = c.flag("__FLAG_DAMIEN_PRICE");

          c.flag("__FLAG_DAMIEN_PRICE", p == 0 ? mult : (p % mult == 0 ? p : p * mult));

          if (c.flag("__FLAG_DAMIEN_PRICE") % 30 == 0) c.askYesNo("price2");
          else c.sayKey("npc.damien.price." + k.toLowerCase(java.util.Locale.ROOT));

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if ("elvenbane".equals(state)) {

          if (yes && c.hasItem("elvenbane")) {

            c.takeItem("elvenbane");

            c.flag("__DAMIEN_HAS_REMOVED_ELVENBANE", 1);

            c.systemMessageKey("message.damien.elvenbane");

            c.sayKey("npc.damien.elvenbane.done");

          } else if (yes) c.sayKey("npc.damien.elvenbane.missing");

          return true;
        }

        if ("price".equals(state)) {

          c.sayKey(yes ? "npc.damien.price.explain" : "npc.damien.price.no");

          return true;
        }

        if ("price2".equals(state)) {

          if (yes && c.player().getGold() >= 50000) {

            c.player().addGold(-50000);

            c.flag("__FLAG_PAID_DAMIEN_PRICE", 1);

            c.sayKey("npc.damien.price.paid");

          } else if (yes) c.sayKey("npc.damien.price.poor");
          else c.sayKey("npc.damien.price.partial");

          return true;
        }

        return false;
      }
    };
  }

  public MonsignorDamien(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
