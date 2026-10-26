package com.perso.T4C.input;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.npc.core.NpcWorldFlags;
import com.perso.T4C.player.GmRank;
import com.perso.T4C.player.GmSeed;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.world.DayNightCycle;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GmCommandProcessorTest {
  private Player player;
  private AtomicInteger saves;
  private GmCommandProcessor commands;

  @BeforeEach
  void setUp() throws Exception {
    player = new Player();
    player.setGmRank(GmRank.SUPER_GM);
    saves = new AtomicInteger();
    commands = new GmCommandProcessor(null, null, null, ignored -> saves.incrementAndGet());
  }

  @Test
  void ignoresRegularChatAndConsumesEveryDotCommand() {
    assertFalse(commands.handleChatMessage("bonjour", player));
    assertTrue(commands.handleChatMessage("  .doesNotExist", player));
    assertTrue(commands.handleChatMessage(".", player));
  }

  @Test
  void supportsCaseInsensitiveLevelAndIntelligenceAliases() {
    assertTrue(commands.handleChatMessage(".SeTLeVeL 12", player));
    assertEquals(12, player.getLevel());
    commands.handleChatMessage(".setIntelect 41", player);
    assertEquals(41, player.getIntelligence());
    commands.handleChatMessage(".setIntellect 42", player);
    assertEquals(42, player.getIntelligence());
    commands.handleChatMessage(".setIntelligence 43", player);
    assertEquals(43, player.getIntelligence());
    assertEquals(4, saves.get());
  }

  @Test
  void rerollRestoresAllocationBudgetsAndReopensAlphanWithoutRepeatingRebirthRewards()
      throws Exception {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister();
    try {
      player.setGmRank(GmRank.PLAYER);
      player.setLevel(100);
      player.setCurrentXp(12345);
      player.setGold(42000);
      player.setRebirthCount(3);
      player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", 3);
      player.setQuestFlag("__FLAG_REMORT_PROCESS", 2);
      player.setQuestFlag("quest:keep", 7);
      player.setQuestFlag("legacy:power:fire", 30);
      player.setQuestFlag("legacy:resist:earth", 40);
      player.setStrength(900);
      player.setEndurance(800);
      player.setSkillLevel("attack", 400);
      player.setSkillLevel("armor_penetration", 100);
      player.setSpells(new java.util.ArrayList<>(java.util.List.of("Fireball")));
      player.setStatPoints(10);
      player.setSkillPoints(20);
      player.setMaxHp(5000);
      player.setMaxMana(3000);
      String weapon = ItemRegistry.findByKey("moonsteel_halberd").getKey();
      player.getEquippedItems().put(com.perso.T4C.player.BodyPart.WEAPON, weapon);
      player.getEquippedItems().put(com.perso.T4C.player.BodyPart.WEAPON2, weapon);
      player.getInventory().add("item.torch");
      commands.handleChatMessage(".reroll", player);
      assertEquals(100, player.getLevel());
      assertEquals(12345, player.getCurrentXp());
      assertEquals(3, player.getRebirthCount());
      assertEquals(35, player.getStrength());
      assertEquals(35, player.getDexterity());
      assertEquals(35, player.getEndurance());
      assertEquals(35, player.getIntelligence());
      assertEquals(35, player.getWisdom());
      assertEquals(495, player.getStatPoints());
      assertEquals(1485, player.getSkillPoints());
      assertEquals(45, player.getSkillLevel("attack"));
      assertEquals(45, player.getSkillLevel("archery"));
      assertEquals(0, player.getSkillLevel("armor_penetration"));
      assertTrue(player.getSpells().isEmpty());
      assertTrue(player.getEquippedItems().isEmpty());
      assertEquals(1, player.getInventory().stream().filter(weapon::equals).count());
      assertTrue(player.getInventory().contains("item.torch"));
      assertEquals(42000, player.getGold());
      assertEquals(7, player.getQuestFlag("quest:keep"));
      assertEquals(0, player.getQuestFlag("legacy:power:fire"));
      assertEquals(0, player.getQuestFlag("legacy:resist:earth"));
      assertEquals(20, player.getQuestFlag("__FLAG_REMORT_POINTS"));
      assertEquals(0, player.getQuestFlag("__FLAG_REMORT_PROCESS"));
      assertEquals(1315 * GRID_W, player.getCoordinates().getX());
      assertEquals(920 * GRID_H, player.getCoordinates().getY());
      assertEquals(1, player.getCoordinates().getZ());
      assertEquals(5000, player.getMaxHp());
      assertEquals(3000, player.getMaxMana());
      assertEquals(1, saves.get());
      var restored = new Player();
      com.perso.T4C.helper.PlayerStateMapper.applyToPlayer(
          com.perso.T4C.helper.PlayerStateMapper.fromPlayer(player), restored);
      var alphan = new com.perso.T4C.npc.RemortNPC1(new com.perso.T4C.npc.core.NpcContext(null));
      var context = new com.perso.T4C.npc.behavior.NpcBehaviorContext(alphan, restored);
      alphan.publicBehavior().onKeyword(context, "assist");
      assertEquals(1, restored.getQuestFlag("__FLAG_REMORT_PROCESS"));
      restored.setQuestFlag("__FLAG_REMORT_POINTS", 0);
      alphan.publicBehavior().onConversationStart(context);
      assertEquals(2, restored.getQuestFlag("__FLAG_REMORT_PROCESS"));
      alphan.publicBehavior().onKeyword(context, "ready");
      assertEquals(
          com.perso.T4C.config.GameConstants.REBIRTH_RETURN_Z, restored.getCoordinates().getZ());
      commands.handleChatMessage(".reroll", player);
      assertEquals(495, player.getStatPoints());
      assertEquals(1485, player.getSkillPoints());
      assertEquals(20, player.getQuestFlag("__FLAG_REMORT_POINTS"));
      assertEquals(1, player.getInventory().stream().filter(weapon::equals).count());
      assertEquals(2, saves.get());
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }

  @Test
  void rerollAtHumanLevelOneHasNoFreePointsAndRejectsArgumentsWithoutSaving() {
    player.setGmRank(GmRank.PLAYER);
    player.setLevel(1);
    commands.handleChatMessage(".reroll someone_else", player);
    assertEquals(0, saves.get());
    commands.handleChatMessage(".reroll", player);
    assertEquals(20, player.getStrength());
    assertEquals(0, player.getStatPoints());
    assertEquals(0, player.getSkillPoints());
    assertEquals(0, player.getQuestFlag("__FLAG_REMORT_POINTS"));
    assertEquals(0, player.getRebirthCount());
    assertEquals(1, saves.get());
  }

  @Test
  void teleportsUsingTileCoordinatesWithOrWithoutTo() {
    commands.handleChatMessage(".teleport to 10, 20, 3", player);
    assertEquals(10 * GRID_W, player.getCoordinates().getX());
    assertEquals(20 * GRID_H, player.getCoordinates().getY());
    assertEquals(3, player.getCoordinates().getZ());
    commands.handleChatMessage(".teleport 4,5,1", player);
    assertEquals(4 * GRID_W, player.getCoordinates().getX());
    assertEquals(5 * GRID_H, player.getCoordinates().getY());
    assertEquals(1, player.getCoordinates().getZ());
    assertEquals(2, saves.get());
  }

  @Test
  void noclipModesHaveNoclipSemanticsAndStayTemporary() {
    commands.handleChatMessage(".noclip on", player);
    assertFalse(player.isPlayerCollisionsEnabled());
    commands.handleChatMessage(".noclip off", player);
    assertTrue(player.isPlayerCollisionsEnabled());
    commands.handleChatMessage(".noclip toggle", player);
    assertFalse(player.isPlayerCollisionsEnabled());
    assertEquals(0, saves.get());
  }

  @Test
  void supportsLongAndHistoricalResourceAndStatCommands() {
    commands.handleChatMessage(".setGold -4", player);
    commands.handleChatMessage(".hp 80", player);
    commands.handleChatMessage(".setMana 35", player);
    commands.handleChatMessage(".str 17", player);
    commands.handleChatMessage(".setDexterity 18", player);
    commands.handleChatMessage(".setEndurance 19", player);
    commands.handleChatMessage(".setWisdom 20", player);
    commands.handleChatMessage(".setStatPoints -2", player);
    commands.handleChatMessage(".setSkillPoints 7", player);
    assertEquals(0, player.getGold());
    assertEquals(80, player.getCurrentHp());
    assertEquals(80, player.getMaxHp());
    assertEquals(35, player.getMana());
    assertEquals(17, player.getStrength());
    assertEquals(18, player.getDexterity());
    assertEquals(19, player.getEndurance());
    assertEquals(20, player.getWisdom());
    assertEquals(0, player.getStatPoints());
    assertEquals(7, player.getSkillPoints());
    assertEquals(9, saves.get());
  }

  @Test
  void invalidArgumentsDoNotMutateOrSave() {
    int level = player.getLevel();
    commands.handleChatMessage(".setLevel nope", player);
    commands.handleChatMessage(".teleport 1,2", player);
    commands.handleChatMessage(".noclip perhaps", player);
    assertEquals(level, player.getLevel());
    assertTrue(player.isPlayerCollisionsEnabled());
    assertEquals(0, saves.get());
  }

  @Test
  void summonMonsterSpawnsRequestedCountWithoutRespawning() {
    MonsterManager monsterManager = new MonsterManager(null);
    GmCommandProcessor withMonsters =
        new GmCommandProcessor(null, null, monsterManager, ignored -> saves.incrementAndGet());
    withMonsters.handleChatMessage(".summon monster Pack Wolf 5", player);
    assertEquals(5, monsterManager.getMonsters().size());
    for (var monster : monsterManager.getMonsters()) {
      assertFalse(monster.shouldRespawn());
    }
  }

  @Test
  void summonMonsterDefaultsToOneWithNoCount() {
    MonsterManager monsterManager = new MonsterManager(null);
    GmCommandProcessor withMonsters =
        new GmCommandProcessor(null, null, monsterManager, ignored -> saves.incrementAndGet());
    withMonsters.handleChatMessage(".summon monster Pack Wolf", player);
    assertEquals(1, monsterManager.getMonsters().size());
  }

  @Test
  void tpAcceptsNamedPlacesByPrefixAndCoordinates() {
    commands.handleChatMessage(".tp silver", player);
    assertEquals(1495 * GRID_W, player.getCoordinates().getX());
    assertEquals(2470 * GRID_H, player.getCoordinates().getY());
    commands.handleChatMessage(".tp 4,5,1", player);
    assertEquals(1, player.getCoordinates().getZ());
    commands.handleChatMessage(".tp nowhere-at-all", player);
    assertEquals(4 * GRID_W, player.getCoordinates().getX());
    assertEquals(2, saves.get());
  }

  @Test
  void tpNpcJumpsToThatNpcsSpawn() {
    SpawnDefinition spawn = SpawnRegistry.npcs().get(0);
    commands.handleChatMessage(".tp npc " + spawn.type().toLowerCase(), player);
    assertEquals(spawn.x() * GRID_W, player.getCoordinates().getX());
    assertEquals(spawn.y() * GRID_H, player.getCoordinates().getY());
    assertEquals(spawn.z(), player.getCoordinates().getZ());
  }

  @Test
  void flagCommandsSetReadAndClearQuestFlags() {
    commands.handleChatMessage(".flag quest:test 3", player);
    assertEquals(3, player.getQuestFlag("quest:test"));
    commands.handleChatMessage(".flag quest:test", player);
    commands.handleChatMessage(".unflag quest:test", player);
    assertFalse(player.getQuestFlags().containsKey("quest:test"));
    commands.handleChatMessage(".flag quest:test nope", player);
    commands.handleChatMessage(".flag a b c", player);
    assertEquals(0, player.getQuestFlag("quest:test"));
    assertEquals(2, saves.get());
  }

  @Test
  void globalFlagWritesWorldFlag() {
    commands.handleChatMessage(".gflag __GM_TEST_FLAG 7", player);
    assertEquals(7, NpcWorldFlags.get("__GM_TEST_FLAG"));
    commands.handleChatMessage(".gflag __GM_TEST_FLAG 0", player);
    assertEquals(0, NpcWorldFlags.get("__GM_TEST_FLAG"));
  }

  @Test
  void summonItemHonoursCount() {
    String key = ItemRegistry.load().get(0).getKey();
    int before = player.getInventory().size();
    commands.handleChatMessage(".summon item " + key + " 3", player);
    assertEquals(before + 3, player.getInventory().size());
  }

  @Test
  void summonBundlesResolveCanonicalKeysKeepQuantitiesAndSaveOncePerCommand() {
    commands.handleChatMessage(".summon AZURE_ARMOR_SET", player);
    assertEquals(6, player.getInventory().size());
    for (String suffix :
        java.util.List.of("armor", "boots", "gauntlets", "helm", "leggings", "protector")) {
      assertEquals(1, com.perso.T4C.item.InventoryService.count(player, "azure_plate_" + suffix));
    }
    commands.handleChatMessage(".summon good_seraph_quest", player);
    assertEquals(1, com.perso.T4C.item.InventoryService.count(player, "soul_of_artherk"));
    assertEquals(3, com.perso.T4C.item.InventoryService.count(player, "ring_of_pure_faith"));
    assertEquals(3, com.perso.T4C.item.InventoryService.count(player, "hammer_of_finality"));
    commands.handleChatMessage(".summon evil_seraph_quest", player);
    assertEquals(1, com.perso.T4C.item.InventoryService.count(player, "bones_of_ogrimar"));
    assertEquals(5, com.perso.T4C.item.InventoryService.count(player, "black_widow_venom"));
    assertEquals(3, saves.get());
    assertTrue(player.getQuestFlags().isEmpty());
    for (String key : player.getInventory()) {
      assertEquals(key, ItemRegistry.findByKey(key).getKey());
    }
  }

  @Test
  void empyreanBundlesResolveEveryVariantAndRejectMissingOrInvalidVariants() {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister();
    try {
      commands.handleChatMessage(".summon empyrean_armor_set", player);
      commands.handleChatMessage(".summon empyrean_armor_set unknown", player);
      assertTrue(player.getInventory().isEmpty());
      assertEquals(0, saves.get());
      // Every variant is a separate content join: all six registry references must resolve.
      for (String variant :
          java.util.List.of(
              "warrior", "archer", "fire", "water", "air", "earth", "light", "dark")) {
        player.getInventory().clear();
        commands.handleChatMessage(".summon empyrean_armor_set " + variant, player);
        assertEquals(6, player.getInventory().size(), variant);
        for (String piece :
            java.util.List.of("armor", "boots", "gauntlets", "helmet", "leggings", "protector"))
          assertEquals(
              1,
              com.perso.T4C.item.InventoryService.count(
                  player, "empyrean_" + variant + "_" + piece));
      }
      assertEquals(8, saves.get());
      player.getInventory().clear();
      commands.handleChatMessage(".summon ephymeral_armor_set ARCHER", player);
      assertEquals(6, player.getInventory().size());
      assertEquals(9, saves.get());
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }

  @Test
  void unlearnRemovesAKnownSpell() {
    player.setSpells(new java.util.ArrayList<>(java.util.List.of("Fireball")));
    commands.handleChatMessage(".unlearn fireball", player);
    assertTrue(player.getSpells().isEmpty());
    assertEquals(1, saves.get());
  }

  @Test
  void sanctuSetsAndClearsRespawnPoint() {
    commands.handleChatMessage(".sanctu 10,20,1", player);
    assertTrue(player.isRespawnPointDefined());
    assertEquals(10 * GRID_W, player.resolveRespawnWorldX());
    assertEquals(1, player.resolveRespawnWorldZ());
    commands.handleChatMessage(".sanctu clear", player);
    assertFalse(player.isRespawnPointDefined());
  }

  @Test
  void godModeBlocksDamageAndIsNotSaved() {
    player.setMaxHp(100);
    player.setCurrentHp(100);
    commands.handleChatMessage(".god on", player);
    player.takeDamage(50);
    assertEquals(100, player.getCurrentHp());
    commands.handleChatMessage(".god", player);
    player.takeDamage(50);
    assertTrue(player.getCurrentHp() < 100);
    assertEquals(0, saves.get());
  }

  @Test
  void peaceModeIsPerPlayerAndNotSaved() {
    commands.handleChatMessage(".peace on", player);
    assertTrue(player.isGmPeace());
    commands.handleChatMessage(".peace maybe", player);
    assertTrue(player.isGmPeace());
    assertFalse(new Player().isGmPeace());
    assertEquals(0, saves.get());
  }

  @Test
  void regularPlayersCannotRunGmCommands() {
    player.setGmRank(GmRank.PLAYER);
    int level = player.getLevel();
    assertTrue(commands.handleChatMessage(".setLevel 99", player));
    commands.handleChatMessage(".god on", player);
    assertEquals(level, player.getLevel());
    assertFalse(player.isGmInvulnerable());
    assertEquals(0, saves.get());
  }

  @Test
  void normalGmsCannotRunSuperGmCommands() {
    player.setGmRank(GmRank.GM);
    DayNightCycle cycle = new DayNightCycle(12f);
    commands.setDayNightCycle(cycle);
    commands.handleChatMessage(".night", player);
    assertFalse(cycle.isNight());
    commands.handleChatMessage(".gflag __GM_TEST_FLAG 5", player);
    assertEquals(0, NpcWorldFlags.get("__GM_TEST_FLAG"));
    commands.handleChatMessage(".setLevel 12", player);
    assertEquals(12, player.getLevel());
  }

  @Test
  void superGmPromotesAndDemotesOtherCharacters() {
    FakeDirectory directory = new FakeDirectory();
    directory.ranks.put("Jean Las", GmRank.PLAYER);
    player.setName("Boss");
    GmCommandProcessor withDirectory =
        new GmCommandProcessor(null, null, null, ignored -> saves.incrementAndGet(), directory);
    withDirectory.handleChatMessage(".gm jean las gm", player);
    assertEquals(GmRank.GM, directory.ranks.get("Jean Las"));
    withDirectory.handleChatMessage(".gm Jean Las super", player);
    assertEquals(GmRank.SUPER_GM, directory.ranks.get("Jean Las"));
    withDirectory.handleChatMessage(".gm Jean Las player", player);
    assertEquals(GmRank.PLAYER, directory.ranks.get("Jean Las"));
    withDirectory.handleChatMessage(".gm Jean Las wizard", player);
    withDirectory.handleChatMessage(".gm Nobody gm", player);
    assertEquals(GmRank.PLAYER, directory.ranks.get("Jean Las"));
    assertFalse(directory.ranks.containsKey("Nobody"));
  }

  @Test
  void rankCannotBeChangedOnYourselfOrAnOwner() {
    FakeDirectory directory = new FakeDirectory();
    directory.ranks.put("Owner", GmRank.PLAYER);
    player.setName("Boss");
    GmCommandProcessor withDirectory =
        new GmCommandProcessor(null, null, null, ignored -> {}, directory);
    try {
      GmSeed.overrideForTests(java.util.List.of("owner"));
      withDirectory.handleChatMessage(".gm Boss player", player);
      assertEquals(GmRank.SUPER_GM, player.getGmRank());
      withDirectory.handleChatMessage(".gm Owner player", player);
      assertEquals(GmRank.PLAYER, directory.ranks.get("Owner"));
      assertEquals(0, directory.writes);
      Player owner = new Player();
      owner.setName("OWNER");
      assertEquals(GmRank.SUPER_GM, owner.getGmRank());
      assertEquals(GmRank.PLAYER, owner.getStoredGmRank());
    } finally {
      GmSeed.overrideForTests(null);
    }
  }

  @Test
  void normalGmCannotChangeRanks() {
    FakeDirectory directory = new FakeDirectory();
    directory.ranks.put("Jean Las", GmRank.PLAYER);
    player.setGmRank(GmRank.GM);
    new GmCommandProcessor(null, null, null, ignored -> {}, directory)
        .handleChatMessage(".gm Jean Las super", player);
    assertEquals(GmRank.PLAYER, directory.ranks.get("Jean Las"));
  }

  private static final class FakeDirectory implements CharacterDirectory {
    final Map<String, GmRank> ranks = new LinkedHashMap<>();
    int writes;

    @Override
    public String findName(String name) {
      return ranks.keySet().stream().filter(n -> n.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public GmRank storedRank(String name) {
      String canonical = findName(name);
      return canonical == null ? null : ranks.get(canonical);
    }

    @Override
    public boolean setStoredRank(String name, GmRank rank) {
      String canonical = findName(name);
      if (canonical == null) return false;
      writes++;
      ranks.put(canonical, rank);
      return true;
    }

    @Override
    public Map<String, GmRank> storedRanks() {
      return ranks;
    }
  }

  @Test
  void timeCommandsMoveTheClock() {
    DayNightCycle cycle = new DayNightCycle();
    commands.setDayNightCycle(cycle);
    commands.handleChatMessage(".night", player);
    assertTrue(cycle.isNight());
    commands.handleChatMessage(".day", player);
    assertFalse(cycle.isNight());
    commands.handleChatMessage(".time 20.5", player);
    assertEquals(20.5f, cycle.getHour(), 0.01f);
    commands.handleChatMessage(".time 30", player);
    assertEquals(20.5f, cycle.getHour(), 0.01f);
  }

  @Test
  void readOnlyCommandsDoNotSave() {
    commands.handleChatMessage(".pos", player);
    commands.handleChatMessage(".stats", player);
    commands.handleChatMessage(".flags", player);
    assertEquals(0, saves.get());
    commands.handleChatMessage(".save", player);
    assertEquals(1, saves.get());
  }

  @Test
  void speedIsBoundedAndTemporary() {
    commands.handleChatMessage(".speed 100", player);
    assertEquals(10f, player.getGmSpeedMultiplier());
    commands.handleChatMessage(".speed reset", player);
    assertEquals(1f, player.getGmSpeedMultiplier());
    assertEquals(0, saves.get());
  }
}
