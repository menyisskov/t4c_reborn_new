package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.RebirthBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0035: a shortcut rebirth rite, found within the Avalon Wilds (1290,1500), worldZ 0 - for a
// character who's already proven themselves once and doesn't want to re-trek to the Oracle's
// dungeon (and its guardian gauntlet) for every subsequent rebirth. Once quest/definition/
// TheWakingRite.java is completed (level 125+, a one-time unlock that then sticks permanently -
// owner's call), Rowan can perform the same rebirth rite the Oracle does, without leaving Avalon.
// Deliberately independent of the Oracle's own "__FLAG_USER_HAS_DEFEATED_ASSISTANT" gate - this
// is an alternate proof of worth, not a duplicate of it.
@Spawn(type = "AnchoriteRowan", x = 1290, y = 1500, z = 0, stationary = false, aggressive = false)
public final class AnchoriteRowan extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "AnchoriteRowan";

  public static final String DISPLAY_NAME = "${npc.anchoriterowan}";

  public static final String SPRITE_BASE = null;

  private static final String QUEST_ID = "the_waking_rite";

  private static final String REBIRTH = "REBIRTH";

  // Declarative fallback/documentation entry for the compendium - real dispatch always happens
  // in javaBehavior() below, which intercepts this same keyword pair first.
  private static final NpcSpec.DialogueTopic REBIRTH_TOPIC =
      new NpcSpec.DialogueTopic(
          List.of(
              "${npc.topic_keyword.anchoriterowan.1.0}", "${npc.topic_keyword.anchoriterowan.1.1}"),
          "${npc.topic.anchoriterowan.1}",
          List.of());

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.anchoriterowan}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.anchoriterowan.0.0}",
                      "${npc.topic_keyword.anchoriterowan.0.1}"),
                  "${npc.topic.anchoriterowan.0}",
                  List.of(new NpcSpec.Action(ActionType.GIVE_QUEST, List.of(QUEST_ID)))),
              REBIRTH_TOPIC,
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.anchoriterowan.2.0}"),
                  "${npc.topic.anchoriterowan.2}",
                  List.of())),
          "AnchoriteRowanNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public AnchoriteRowan(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        if (!ScriptedNpc.matches(REBIRTH_TOPIC, keyword)) {
          return false;
        }
        offerRebirth(context);
        return true;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext context, String state, boolean answer) {
        if (!REBIRTH.equals(state)) {
          return false;
        }
        if (!answer) {
          context.say(I18n.resolve("${npc.anchoriterowan.declined}"));
          return true;
        }
        Player player = context.player();
        if (!RebirthBehavior.canRebirth(player)) {
          context.say(I18n.resolve("${npc.anchoriterowan.rebirth_limit}"));
          return true;
        }
        RebirthBehavior.perform(player);
        context.say(I18n.resolve("${npc.anchoriterowan.done}"));
        String letterReveal = com.perso.T4C.quest.UnsignedLetterQuest.onRebirth(player);
        if (letterReveal != null) context.systemMessage(letterReveal);
        // The remort energy points RebirthBehavior.perform() just granted can only be spent with
        // Alphan and the associates in their isolated allocation room - the same place Oracle
        // sends a player after every rebirth (Oracle.java's own onYesNo). Without this, a player
        // rebirthing via Rowan would be stranded in Avalon with unspent points that the next
        // rebirth silently overwrites.
        context.teleport(1315, 920, 1);
        return true;
      }
    };
  }

  private static void offerRebirth(NpcBehaviorContext context) {
    Player player = context.player();
    if (QuestService.statusFor(player, QuestRegistry.findById(QUEST_ID))
        != QuestService.STATUS_COMPLETED) {
      context.say(I18n.resolve("${npc.anchoriterowan.not_proven}"));
      return;
    }
    if (!RebirthBehavior.canRebirth(player)) {
      context.say(I18n.resolve("${npc.anchoriterowan.rebirth_limit}"));
      return;
    }
    int minimumLevel = RebirthBehavior.requiredLevelFor(player.getRebirthCount() + 1);
    if (player.getLevel() < minimumLevel) {
      context.say(I18n.resolve("${npc.anchoriterowan.too_weak}"));
      return;
    }
    context.say(I18n.resolve("${npc.anchoriterowan.confirm}"));
    context.askYesNo(REBIRTH);
  }
}
