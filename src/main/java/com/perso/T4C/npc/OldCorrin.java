package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0046: the "Two Masters" pattern's first proof of concept, on the existing
// passage_to_kraanhold access quest (T4C-0024). Dockmaster Thessaly (a few steps away, same
// dock) is the immediate master - talk to her once you've cleared enough Toll Trolls and she
// pays gold + XP on the spot, same as before this pass. Old Corrin is the second master: instead
// of a lump payout, she'll vouch for you with the Kraanian trading houses - a permanent (until
// your next rebirth) XP trickle on Kraanian-clan kills. Whichever master you pick first
// completes the quest (see QuestService.completeWithAlternateReward) and locks the other one out
// - Corrin's dialogue says so plainly rather than silently offering a reward that can no longer
// be claimed.
@Spawn(type = "OldCorrin", x = 1626, y = 2631, z = 0, stationary = true, aggressive = false)
public final class OldCorrin extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "OldCorrin";
  public static final String DISPLAY_NAME = "${npc.oldcorrin}";
  public static final String SPRITE_BASE = null;

  /** Permanent-until-rebirth flag granting the Kraanhold favor XP trickle (see
   * {@code MainGameScreen.awardKraanholdFavorBonus}). Cleared like every other quest flag on
   * rebirth, so the favor is a per-life reward, not a permanent account perk. */
  public static final String FAVOR_FLAG = "quest.passage_to_kraanhold.favor";

  private static final String QUEST_ID = "passage_to_kraanhold";
  private static final String CONFIRM_FAVOR = "CONFIRM_FAVOR";

  private static final NpcSpec.DialogueTopic FAVOR_TOPIC =
      new NpcSpec.DialogueTopic(
          List.of("${npc.topic_keyword.oldcorrin.0.0}", "${npc.topic_keyword.oldcorrin.0.1}"),
          "${npc.topic.oldcorrin.0}",
          List.of());

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.oldcorrin}",
          List.of(FAVOR_TOPIC),
          "OldCorrinNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public OldCorrin(NpcContext context) throws GameException {
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
        if (!ScriptedNpc.matches(FAVOR_TOPIC, keyword)) {
          return false;
        }
        offerFavor(context);
        return true;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext context, String state, boolean answer) {
        if (!CONFIRM_FAVOR.equals(state)) {
          return false;
        }
        if (!answer) {
          context.say(I18n.resolve("${npc.oldcorrin.declined}"));
          return true;
        }
        QuestService quests = context.npc().questService();
        Player player = context.player();
        String completion =
            quests == null
                ? null
                : quests.completeWithAlternateReward(
                    QUEST_ID, player, () -> player.setQuestFlag(FAVOR_FLAG, 1));
        context.say(
            completion != null ? completion : I18n.resolve("${npc.oldcorrin.too_late}"));
        return true;
      }
    };
  }

  private static void offerFavor(NpcBehaviorContext context) {
    Player player = context.player();
    QuestDef definition = QuestRegistry.findById(QUEST_ID);
    int status = QuestService.statusFor(player, definition);
    if (status == QuestService.STATUS_COMPLETED) {
      context.say(I18n.resolve("${npc.oldcorrin.already_settled}"));
      return;
    }
    if (status != QuestService.STATUS_ACTIVE
        || player.getQuestFlag(QuestService.killsFlag(definition)) < definition.getRequiredKills()) {
      context.say(I18n.resolve("${npc.oldcorrin.not_ready}"));
      return;
    }
    context.say(I18n.resolve("${npc.oldcorrin.offer}"));
    context.askYesNo(CONFIRM_FAVOR);
  }
}
