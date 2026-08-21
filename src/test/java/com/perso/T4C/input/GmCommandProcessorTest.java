package com.perso.T4C.input;

import static org.junit.jupiter.api.Assertions.*;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import com.perso.T4C.player.Player;
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
  void speedIsBoundedAndTemporary() {
    commands.handleChatMessage(".speed 100", player);
    assertEquals(10f, player.getGmSpeedMultiplier());
    commands.handleChatMessage(".speed reset", player);
    assertEquals(1f, player.getGmSpeedMultiplier());
    assertEquals(0, saves.get());
  }
}
