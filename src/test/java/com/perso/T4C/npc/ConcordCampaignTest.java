package com.perso.T4C.npc;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.ConcordCampaign;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.teleport.NamedLocations;
import java.io.File;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConcordCampaignTest {
  @BeforeEach
  void loadItems() {
    ItemJsonLoader.loadAndRegister();
  }

  @AfterEach
  void resetItems() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  private final AtomicInteger saves = new AtomicInteger();
  private final QuestService quests =
      new QuestService(XpCurve.loadDefault(), saves::incrementAndGet, null);
  private final NpcContext context = new NpcContext(quests);
  private static final List<String> SITES =
      List.of(
          "OathwoodArchivist",
          "MoonwakeBellkeeper",
          "EmberglassConfessor",
          "ThrenodyRefugee",
          "BellwardTuner");

  @Test
  void storyProgressesThroughEvidenceChoicesRitualAndBossesWithOneRewardPerCheckpoint()
      throws Exception {
    Player player = veteran();
    player.setLevel(399);
    player.setXpToNextLevel(Long.MAX_VALUE);
    var legacy = HollowDawnCampaign.avalon().getFirst();
    player.setQuestFlag(QuestService.statusFlag(legacy), QuestService.STATUS_ACTIVE);
    player.setQuestFlag(QuestService.killsFlag(legacy), 13);
    long startXp = player.getCurrentXp();
    int expectedGold = 0;
    long expectedXp = 0;
    for (int chapter = 0; chapter < ConcordCampaign.all().size(); chapter++) {
      var quest = ConcordCampaign.all().get(chapter);
      if (chapter == 7) {
        assertTrue(
            quest.getMinLevel() < com.perso.T4C.config.GameConstants.MAX_PLAYER_LEVEL,
            "the finale must be claimable while its XP can still help");
        player.setLevel(quest.getMinLevel());
        player.setCurrentXp(0);
        XpCurve.loadDefault().applyToPlayer(player);
      }
      var guide = new ArchivistAveline(context);
      say(guide, player, "accept");
      say(guide, player, "report");
      assertEquals(
          QuestService.STATUS_ACTIVE,
          QuestService.statusFor(player, quest),
          "cannot skip " + quest.getId());
      if (chapter < 5) investigate(player, chapter, chapter == 3 ? "reveal" : null);
      else if (chapter == 6) {
        assertFalse(hasTravel(player, "The Hollow Dawn"));
        for (int site : new int[] {4, 1, 2})
          investigate(player, site, site == 1 ? "shelter" : site == 2 ? "truth" : "choice");
      } else {
        quests.recordKill(player, quest.getTargetMonster(), 0, 4040, 1477);
        assertEquals(
            0, player.getQuestFlag(QuestService.killsFlag(quest)), "kill elsewhere does not count");
        quests.recordKill(
            player, quest.getTargetMonster(), 0, quest.getAreaCenterX(), quest.getAreaCenterY());
      }
      say(guide, player, "water");
      if (chapter == 7) say(guide, player, "restore");
      String reward = ConcordCampaign.reward(chapter, "water");
      say(guide, player, "report");
      assertEquals(
          QuestService.STATUS_COMPLETED, QuestService.statusFor(player, quest), quest.getId());
      assertEquals(1, InventoryService.count(player, reward));
      expectedGold += quest.getRewardGold();
      expectedXp +=
          Math.round(quest.getRewardXp() * com.perso.T4C.config.GameConstants.SERVER_XP_RATE);
      assertEquals(expectedGold, player.getGold());
      if (chapter == 7) {
        long remaining = Math.round(quest.getRewardXp() * com.perso.T4C.config.GameConstants.SERVER_XP_RATE);
        int expectedLevel = quest.getMinLevel();
        var curve = XpCurve.loadDefault();
        while (expectedLevel < com.perso.T4C.config.GameConstants.MAX_PLAYER_LEVEL
            && remaining >= curve.getXpToNextLevel(expectedLevel)) {
          remaining -= curve.getXpToNextLevel(expectedLevel++);
        }
        if (expectedLevel == com.perso.T4C.config.GameConstants.MAX_PLAYER_LEVEL) remaining = 0;
        assertEquals(expectedLevel, player.getLevel());
        assertEquals(remaining, player.getCurrentXp());
      } else assertEquals(startXp + expectedXp, player.getCurrentXp());
      if (chapter == 2) assertTrue(hasTravel(player, "Threnody Reach"));
      if (chapter == 6) assertTrue(hasTravel(player, "The Hollow Dawn"));
    }
    var guide = new ArchivistAveline(context);
    var inventory = List.copyOf(player.getInventory());
    say(guide, player, "report");
    say(guide, player, "release");
    assertEquals(inventory, player.getInventory());
    assertEquals(expectedGold, player.getGold());
    assertEquals(1, player.getQuestFlag("concord.ending"));
    assertEquals(2, player.getQuestFlag("concord.refuge"));
    assertEquals(
        13, player.getQuestFlag(QuestService.killsFlag(legacy)), "legacy progress is preserved");
  }

  @Test
  void evidenceRequiresLocalCluesAndPersistsPerPlayerAcrossNpcReloads() throws Exception {
    Player player = veteran();
    var guide = new ArchivistAveline(context);
    say(guide, player, "accept");
    var site = site(0, player);
    String proof = ConcordCampaign.all().getFirst().getRequiredItemKey();
    say(site, player, "names");
    assertFalse(player.getInventory().contains(proof));
    say(site, player, "record");
    Player reloaded = veteran();
    reloaded.setQuestFlags(player.getQuestFlags());
    site = site(0, reloaded);
    say(site, reloaded, "echo");
    say(site, reloaded, "names");
    assertEquals(1, InventoryService.count(reloaded, proof));
    assertFalse(player.getInventory().contains(proof));
    assertTrue(saves.get() >= 4, "clues are saved before quest completion");
    reloaded.getInventory().remove(proof);
    reloaded.setWorldPosition(0, 0, 0);
    say(site, reloaded, "names");
    assertFalse(reloaded.getInventory().contains(proof), "remote evidence cannot be claimed");
    site(0, reloaded);
    say(site, reloaded, "names");
    assertEquals(
        1,
        InventoryService.count(reloaded, proof),
        "lost evidence can be recovered without replaying rewards");
  }

  @Test
  void weaponRewardChecksCapacityAndLevelBeforeConsumingEvidenceOrOpeningTravel() throws Exception {
    Player player = veteran();
    for (int i = 0; i < 2; i++)
      player.setQuestFlag(QuestService.statusFlag(ConcordCampaign.all().get(i)), 2);
    var quest = ConcordCampaign.all().get(2);
    var guide = new ArchivistAveline(context);
    say(guide, player, "accept");
    investigate(player, 2, null);
    String proof = quest.getRequiredItemKey();
    say(guide, player, "archer");
    player.setStrength(0);
    say(guide, player, "report");
    assertEquals(1, InventoryService.count(player, proof));
    assertEquals(0, player.getGold());
    assertFalse(hasTravel(player, "Threnody Reach"));
    player.setStrength(1000);
    player.setLevel(quest.getMinLevel() - 1);
    say(guide, player, "report");
    assertEquals(1, InventoryService.count(player, proof));
    player.setLevel(400);
    say(guide, player, "report");
    assertEquals(0, InventoryService.count(player, proof));
    assertEquals(1, InventoryService.count(player, "item.concord_1_archer"));
    assertEquals(0, InventoryService.count(player, "item.concord_1_warrior"));
    assertTrue(hasTravel(player, "Threnody Reach"));
  }

  @Test
  void bellSequenceCannotBeSkippedAndRestartsAfterWrongNote() throws Exception {
    Player player = veteran();
    for (int i = 0; i < 4; i++)
      player.setQuestFlag(QuestService.statusFlag(ConcordCampaign.all().get(i)), 2);
    say(new ArchivistAveline(context), player, "accept");
    var tuner = site(4, player);
    say(tuner, player, "record");
    say(tuner, player, "echo");
    say(tuner, player, "remember");
    say(tuner, player, "choice");
    assertEquals(0, player.getQuestFlag(ConcordCampaign.flag(4, "sequence")));
    assertEquals(
        0, InventoryService.count(player, ConcordCampaign.all().get(4).getRequiredItemKey()));
    say(tuner, player, "remember");
    say(tuner, player, "shelter");
    say(tuner, player, "choice");
    assertEquals(
        1, InventoryService.count(player, ConcordCampaign.all().get(4).getRequiredItemKey()));
  }

  @Test
  void storySitesJoinRegisteredNpcsAndReachableCastleGatesAndRewardsResolve() throws Exception {
    var collision = new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
    for (String id : SITES) {
      assertNotNull(NpcFactoryRegistry.find(id));
      var spawn =
          SpawnRegistry.npcs().stream().filter(s -> id.equals(s.type())).findFirst().orElseThrow();
      assertFalse(collision.hasCollision(spawn.x(), spawn.y()), id);
      // Local flood must reach outside the fort; a walkable tile enclosed by a keep is
      // insufficient.
      Set<Long> seen = new HashSet<>();
      ArrayDeque<int[]> queue = new ArrayDeque<>();
      queue.add(new int[] {spawn.x(), spawn.y()});
      boolean escaped = false;
      while (!queue.isEmpty() && !escaped) {
        int[] at = queue.remove();
        if (Math.max(Math.abs(at[0] - spawn.x()), Math.abs(at[1] - spawn.y())) >= 55) {
          escaped = true;
          break;
        }
        for (int[] delta : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
          int x = at[0] + delta[0], y = at[1] + delta[1];
          long key = ((long) y << 32) | (x & 0xffffffffL);
          if (!collision.hasCollision(x, y) && seen.add(key)) queue.add(new int[] {x, y});
        }
      }
      assertTrue(escaped, id + " is enclosed");
    }
    for (var quest : ConcordCampaign.all()) {
      assertNotNull(QuestRegistry.findById(quest.getId()));
      assertNotNull(ItemRegistry.findByKey(quest.getRewardItemKey()));
      assertFalse(I18n.resolve(quest.getOfferText()).contains("${"));
      if (quest.getRequiredItemKey() != null)
        assertTrue(ItemRegistry.findByKey(quest.getRequiredItemKey()).isUndroppable());
    }
    for (String style : ConcordCampaign.STYLES) {
      var first = ItemRegistry.findByKey(ConcordCampaign.reward(2, style));
      var last = ItemRegistry.findByKey(ConcordCampaign.reward(7, style));
      assertNotNull(first);
      assertNotNull(last);
      assertNotEquals(first.getNumId(), last.getNumId());
    }
  }

  private void investigate(Player player, int index, String answer) throws Exception {
    var npc = site(index, player);
    say(npc, player, "record");
    say(npc, player, "echo");
    if (ConcordCampaign.chapter(player) == 4) {
      say(npc, player, "remember");
      say(npc, player, "shelter");
    }
    say(
        npc,
        player,
        answer != null
            ? answer
            : List.of("names", "shelter", "truth", "mercy", "choice").get(index));
  }

  private ScriptedNpc site(int index, Player player) throws Exception {
    String id = SITES.get(index);
    var npc = (ScriptedNpc) NpcFactoryRegistry.create(id, context);
    var spawn =
        SpawnRegistry.npcs().stream().filter(s -> id.equals(s.type())).findFirst().orElseThrow();
    npc.setSpawnPosition(spawn.x() * GRID_W, spawn.y() * GRID_H);
    player.setWorldPosition(spawn.x() * GRID_W, spawn.y() * GRID_H, 0);
    return npc;
  }

  private static Player veteran() {
    Player player = new Player();
    player.setLevel(400);
    player.setStrength(1000);
    player.setCurrentXp(0);
    return player;
  }

  private static void say(ScriptedNpc npc, Player player, String word) {
    assertTrue(npc.publicBehavior().onKeyword(new NpcBehaviorContext(npc, player), word));
  }

  private static boolean hasTravel(Player player, String name) {
    return NamedLocations.forPlayer(player).stream().anyMatch(p -> name.equals(p.displayName()));
  }
}
