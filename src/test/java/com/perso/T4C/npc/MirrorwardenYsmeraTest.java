package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.mirror.MirrorTrials;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/** T4C-0042: Ysmera summons the player's Echo at their next trial. */
@ResourceLock("EchoOfSelf")
class MirrorwardenYsmeraTest {

  @AfterEach
  void clearSummonCallback() {
    NpcScriptRuntime.setSummonCallback(null);
  }

  @BeforeEach
  void dismissAnyLivingEcho() {
    com.perso.T4C.monster.EchoOfSelf.onMakerFell();
  }

  @Test
  void echoSummonsTheNextTrialNearThePlayer() throws Exception {
    List<String> summoned = new ArrayList<>();
    NpcScriptRuntime.setSummonCallback(
        (monster, x, y, z) -> {
          summoned.add(monster);
          return true;
        });
    MirrorwardenYsmera npc = new MirrorwardenYsmera(new NpcContext(null));
    Player player = new Player();
    player.setQuestFlag(MirrorTrials.FLAG_TIER, 2);
    NpcBehavior behavior = npc.javaBehavior();

    assertTrue(behavior.onKeyword(new NpcBehaviorContext(npc, player), "echo"));

    assertEquals(List.of(MirrorTrials.ECHO_MONSTER_NAME), summoned);
    assertEquals(3, player.getQuestFlag(MirrorTrials.FLAG_CHALLENGE));
  }

  @Test
  void theBoundEchoCannotBeCalledBeforeAllTenTrials() throws Exception {
    MirrorwardenYsmera npc = new MirrorwardenYsmera(new NpcContext(null));
    Player player = new Player();
    player.setQuestFlag(MirrorTrials.FLAG_TIER, 9);
    NpcBehavior behavior = npc.javaBehavior();

    assertTrue(behavior.onKeyword(new NpcBehaviorContext(npc, player), "call"));
    assertFalse(MirrorTrials.hasBoundEcho(player));
  }

  @Test
  void trialsAndGeneralTopicsAreHandled() throws Exception {
    MirrorwardenYsmera npc = new MirrorwardenYsmera(new NpcContext(null));
    NpcBehavior behavior = npc.javaBehavior();
    Player player = new Player();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "trials"));
    assertFalse(behavior.onKeyword(context, "mirror"), "lore falls back to the static topic");
  }

  @Test
  void everyDialogueKeyHasEnglishText() {
    NpcSpec spec = MirrorwardenYsmera.spec();
    List<String> keys = new ArrayList<>();
    keys.add(spec.displayName());
    keys.add(spec.welcomeText());
    for (NpcSpec.DialogueTopic topic : spec.topics()) {
      keys.add(topic.response());
      keys.addAll(topic.keywords());
    }
    for (String placeholder : keys) {
      assertFalse(I18n.resolve(placeholder).startsWith("${"), placeholder);
    }
    for (String key :
        new String[] {
          "npc.mirrorwardenysmera.greet.new",
          "npc.mirrorwardenysmera.greet.returning",
          "npc.mirrorwardenysmera.greet.bound",
          "npc.mirrorwardenysmera.echo.summoned",
          "npc.mirrorwardenysmera.echo.rematch",
          "npc.mirrorwardenysmera.echo.busy",
          "npc.mirrorwardenysmera.echo.failed",
          "npc.mirrorwardenysmera.trials.progress",
          "npc.mirrorwardenysmera.trials.done",
          "npc.mirrorwardenysmera.call.locked",
          "npc.mirrorwardenysmera.call.occupied",
          "npc.mirrorwardenysmera.call.done",
          "mirror.reward.trial",
          "mirror.reward.trial_gold",
          "mirror.reward.rematch",
          "mirror.reward.bound",
          "monster.echo_of_self"
        }) {
      assertTrue(I18n.has(key), key);
    }
    String progress = I18n.message("npc.mirrorwardenysmera.trials.progress", "A", 1, 2, 12, 3, 4);
    assertTrue(progress.contains("12%"), progress);
  }
}
