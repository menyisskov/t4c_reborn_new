package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.WitnessStory;
import com.perso.T4C.quest.definition.PassageToAvalon;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WitnessStoryDialogueTest {
  private final NpcContext context =
      new NpcContext(new QuestService(XpCurve.loadDefault(), null, null));

  @Test
  void evidenceFollowsTheMainlandSpineAndSurvivesAPlayerReload() throws Exception {
    Player player = new Player();
    player.setQuestFlag("ADDON_STORYLINE_PROGRESS", 35);
    player.setQuestFlag("ADDON_CRIMSONSCALE_LETTER", 1);
    ScriptedNpc kilhiam = new Kilhiam(context);
    ScriptedNpc grove = new Lantalir(context);
    ScriptedNpc thomas = new Librarian1(context);
    ScriptedNpc jeremiah = new Librarian2(context);
    ScriptedNpc rangor = new HarbormasterRangor(context);

    say(thomas, player, "compare");
    say(jeremiah, player, "difference");
    say(grove, player, "testimony");
    say(rangor, player, "compare");
    assertEquals(2, player.getQuestFlags().size(), "out-of-order evidence cannot advance");

    say(kilhiam, player, "missing names");
    assertFalse(WitnessStory.has(player, WitnessStory.ARAKAS_RECORD));
    say(kilhiam, player, "record");
    assertEquals("npc.witness_story.route.grove", WitnessStory.nextRouteKey(player));
    say(grove, player, "testimony");
    assertEquals("npc.witness_story.route.war", WitnessStory.nextRouteKey(player));
    // The two library accounts can be heard in either order, but both are essential.
    say(jeremiah, player, "difference");
    say(thomas, player, "compare");
    assertFalse(WitnessStory.has(player, WitnessStory.LIBRARY_COMPARISON));
    say(thomas, player, "record");
    say(thomas, player, "compare");
    assertEquals("npc.witness_story.route.manifest", WitnessStory.nextRouteKey(player));

    Player reloaded = new Player();
    reloaded.setQuestFlags(player.getQuestFlags());
    say(new HarbormasterRangor(context), reloaded, "compare");
    assertTrue(WitnessStory.has(reloaded, WitnessStory.MANIFEST_COMPARED));
    assertEquals("npc.witness_story.route.isles", WitnessStory.nextRouteKey(reloaded));
    Map<String, Integer> completed = Map.copyOf(reloaded.getQuestFlags());
    say(rangor, reloaded, "compare");
    say(thomas, reloaded, "compare");
    assertEquals(completed, reloaded.getQuestFlags(), "repeat visits are idempotent");
    assertEquals(35, reloaded.getQuestFlag("ADDON_STORYLINE_PROGRESS"));
    assertEquals(1, reloaded.getQuestFlag("ADDON_CRIMSONSCALE_LETTER"));
    assertEquals(0, reloaded.getRebirthCount());
  }

  @Test
  void everyGuideOffersAReadOnlyRecapAndLorePreviewsDoNotGrantEvidence() throws Exception {
    Player player = new Player();
    List<ScriptedNpc> npcs =
        List.of(
            new Kilhiam(context),
            new Lantalir(context),
            new Librarian1(context),
            new Librarian2(context),
            new HarbormasterRangor(context),
            new Oracle(context));
    String[] previews = {
      "missing names", "remembrance", "missing names", "judgment", "manifest", "tremor"
    };
    for (int i = 0; i < npcs.size(); i++) {
      ScriptedNpc npc = npcs.get(i);
      say(npc, player, "story");
      say(npc, player, "investigation");
      say(npc, player, previews[i]);
      assertTrue(player.getQuestFlags().isEmpty());
      assertTrue(
          npc.specification().topics().stream().anyMatch(t -> ScriptedNpc.matches(t, "story")));
    }
    assertFalse(WitnessStory.record(player, "ADDON_STORYLINE_PROGRESS"));
    assertTrue(player.getQuestFlags().isEmpty());
    for (String suffix :
        List.of("arakas", "grove", "war", "judgment", "compare", "manifest", "isles")) {
      String key = "npc.witness_story.route." + suffix;
      String resolved = I18n.resolve("${" + key + "}");
      assertFalse(resolved.contains("${"), key);
      assertTrue(resolved.contains("\""), "every recap contains a quoted next keyword: " + key);
    }
  }

  @Test
  void oracleRecognizesConsumedVictoryStatesAndDoesNotRewriteThem() throws Exception {
    for (int state : new int[] {0, 1, 2, 3}) {
      Player player = new Player();
      player.setQuestFlag("__FLAG_USER_HAS_KILLED_MAKRSH_PTANGH", state);
      assertEquals(state > 0, WitnessStory.hasDefeatedMakrsh(player));
      ScriptedNpc oracle = new Oracle(context);
      say(oracle, player, "tremor");
      say(oracle, player, "witness");
      assertEquals(state, player.getQuestFlag("__FLAG_USER_HAS_KILLED_MAKRSH_PTANGH"));
      assertEquals(1, player.getQuestFlag(WitnessStory.ORACLE_TESTIMONY));
      assertEquals(0, player.getRebirthCount());
      assertEquals("npc.witness_story.route.arakas", WitnessStory.nextRouteKey(player));
    }
  }

  @Test
  void legacyQuestKeywordsAndPassageRemainIndependent() throws Exception {
    Player player = new Player();
    player.setQuestFlag("ADDON_STORYLINE_PROGRESS", 35);
    say(new Librarian1(context), player, "heart");
    assertEquals(36, player.getQuestFlag("ADDON_STORYLINE_PROGRESS"));
    say(new Lantalir(context), player, "errand");
    say(new Kilhiam(context), player, "sanctuary");
    assertFalse(WitnessStory.has(player, WitnessStory.ARAKAS_RECORD));
    player.setQuestFlag(
        QuestService.statusFlag(PassageToAvalon.definition()), QuestService.STATUS_ACTIVE);
    say(new HarbormasterRangor(context), player, "chart");
    assertEquals(
        QuestService.STATUS_ACTIVE, QuestService.statusFor(player, PassageToAvalon.definition()));
    assertFalse(WitnessStory.has(player, WitnessStory.MANIFEST_COMPARED));
  }

  private static void say(ScriptedNpc npc, Player player, String keyword) {
    assertTrue(
        npc.publicBehavior().onKeyword(new NpcBehaviorContext(npc, player), keyword), keyword);
  }
}
