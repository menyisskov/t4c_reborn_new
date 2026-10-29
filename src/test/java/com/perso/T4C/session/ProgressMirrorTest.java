package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.OnlineCharacterView;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * A fixture server character, as the client model holds it, goes through the mirror and the
 * existing mapper onto a real Player. Every field the task lists is checked on the Player, which
 * ends up remote, and the single-player save file refuses it.
 */
@ResourceLock("user.dir")
class ProgressMirrorTest {
  static final long NOW = 2_000_000_000_000L;
  static final String TWO_SLOT = "item.ancient_celestial_air_gauntlets";
  static final UUID DAGGER = UUID.fromString("00000000-0000-0000-0000-0000000000d1");
  static final UUID GAUNTLETS = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
  static final UUID POTIONS = UUID.fromString("00000000-0000-0000-0000-0000000000b3");
  static final UUID TORCH = UUID.fromString("00000000-0000-0000-0000-0000000000b4");
  static final UUID STORED = UUID.fromString("00000000-0000-0000-0000-0000000000c5");
  static final UUID CHARACTER = UUID.fromString("00000000-0000-0000-0000-000000000001");
  static final int LEVEL = 5;

  @org.junit.jupiter.api.BeforeEach
  void loadJsonItems() {
    // The two-slot fixture gauntlets are JSON-authored gear, registered by the game bootstrap.
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister("assets/items");
  }

  @org.junit.jupiter.api.AfterEach
  void unloadJsonItems() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  static CharacterProgress progress(List<CharacterProgress.Buff> buffs, Map<String, UUID> worn) {
    return new CharacterProgress(
        CharacterProgress.CURRENT_VERSION,
        "warrior",
        "female",
        new CharacterProgress.Attributes(41, 33, 32, 25, 24),
        new CharacterProgress.Vitals(111, 180, 20, 60),
        new CharacterProgress.Advancement(LEVEL, 7, 3, 2, -15),
        List.of(
            new CharacterProgress.Item(DAGGER, "item.dagger", 1, 0),
            new CharacterProgress.Item(GAUNTLETS, TWO_SLOT, 1, 0),
            new CharacterProgress.Item(POTIONS, "item.heal_light_potion", 2, 2),
            new CharacterProgress.Item(TORCH, "item.torch", 1, 0)),
        worn,
        Map.of("attack", 40, "dodge", 33),
        Set.of("spell.heal_light", "spell.light"),
        Map.of("quest.rats", 2),
        List.of(new CharacterProgress.Item(STORED, "item.dagger", 2, 0)),
        1234L,
        new CharacterProgress.Respawn(0, 150.0, 220.0),
        buffs,
        Map.of(),
        0L,
        new CharacterProgress.Companion("wolf", true, 3, 17, "follow"));
  }

  static OnlineConnection.View view(CharacterProgress progress) {
    long experience = XpCurve.loadDefault().getTotalXp(LEVEL) + 17;
    OnlineCharacterView character =
        new OnlineCharacterView(CHARACTER, "Aelwyn", 987L, experience, 12L, progress, 4L);
    var map = new OnlineConnection.MapInfo(100, 200, 80, 60, true, 6, 0, 0, "avalon", 4L);
    var snapshot =
        new OnlineConnection.Snapshot(
            9L,
            List.of(new OnlineConnection.Actor(7L, 3.5, 5.5, 1L, 0L, 111, 180)),
            12L,
            List.of(),
            map.region(),
            List.of(),
            List.of());
    return new OnlineConnection.View(7L, map, snapshot, character, List.of());
  }

  static CharacterProgress fixture() {
    return progress(
        List.of(), Map.of(BodyPart.WEAPON.name(), DAGGER, BodyPart.LEFT_HAND.name(), GAUNTLETS));
  }

  @Test
  void everyFieldReachesTheRemotePlayer() {
    var definition = ItemRegistry.findByKey(TWO_SLOT);
    assertNotNull(definition, "fixture item must exist");
    assertEquals(BodyPart.RIGHT_HAND, definition.getSecondaryBodyPart());
    var mirrored = ProgressMirror.toStateDto(view(fixture()), () -> NOW);
    PlayerStateDto state = mirrored.state();
    Player player = new Player();
    RemoteCharacterSource source = new RemoteCharacterSource(() -> view(fixture()), () -> NOW);
    assertFalse(player.isRemote());

    source.applyTo(state, player);
    player.setWorldPosition(state.x * GRID_W, state.y * GRID_H, state.z);

    assertTrue(player.isRemote());
    assertEquals("Aelwyn", player.getName());
    assertEquals("FEMALE", player.getGender());
    assertEquals(41, player.getStrength());
    assertEquals(33, player.getEndurance());
    assertEquals(32, player.getDexterity());
    assertEquals(25, player.getWisdom());
    assertEquals(24, player.getIntelligence());
    assertEquals(987, player.getGold());
    assertEquals(-15, player.getKarma());
    assertEquals(LEVEL, player.getLevel());
    assertEquals(17, player.getCurrentXp());
    assertEquals(XpCurve.loadDefault().getXpToNextLevel(LEVEL), player.getXpToNextLevel());
    assertEquals(7, player.getStatPoints());
    assertEquals(3, player.getSkillPoints());
    assertEquals(2, player.getRebirthCount());
    assertEquals(180, player.getBaseMaxHp());
    assertEquals(111, player.getCurrentHp());
    assertEquals(60, player.getMaxMana());
    assertEquals(20, player.getMana());
    assertTrue(player.getSpells().containsAll(List.of("spell.heal_light", "spell.light")));
    assertEquals(2, player.getSpells().size());
    assertEquals(40, player.getSkills().get("attack"));
    assertEquals(33, player.getSkills().get("dodge"));
    assertEquals(2, player.getQuestFlags().get("quest.rats"));
    assertEquals(
        2, player.getInventory().stream().filter("item.heal_light_potion"::equals).count());
    assertEquals(1, player.getInventory().stream().filter("item.torch"::equals).count());
    assertFalse(player.getInventory().contains("item.dagger"), "a worn item leaves the inventory");
    assertFalse(player.getInventory().contains(TWO_SLOT));
    assertEquals("item.dagger", player.getEquippedItems().get(BodyPart.WEAPON));
    assertEquals(TWO_SLOT, player.getEquippedItems().get(BodyPart.LEFT_HAND));
    assertEquals(TWO_SLOT, player.getEquippedItems().get(BodyPart.RIGHT_HAND), "both hands");
    assertEquals(2, player.getItemCharges().get("item.heal_light_potion"));
    assertEquals(List.of("item.dagger", "item.dagger"), player.getStorage());
    assertEquals(1234, player.getStorageGold());
    assertTrue(player.isRespawnPointDefined());
    assertEquals(150f * GRID_W, player.getRespawnWorldX());
    assertEquals(220f * GRID_H, player.getRespawnWorldY());
    assertEquals(0, player.getRespawnWorldZ());
    // Region origin (100,200) + tile centre (3.5,5.5) - half a tile: tile (103,205), world 0.
    assertEquals(103f * GRID_W, player.getCoordinates().getX());
    assertEquals(205f * GRID_H, player.getCoordinates().getY());
    assertEquals(0, player.getCoordinates().getZ());
    assertEquals(com.perso.T4C.player.GmRank.PLAYER, player.getGmRank());
    assertTrue(player.getQuickSlots().isEmpty());
    assertTrue(player.getMacros().isEmpty());
  }

  @Test
  void theServerItemIdentitiesStayAvailableForLaterIntents() {
    var mirrored = ProgressMirror.toStateDto(view(fixture()), () -> NOW);
    assertEquals(List.of(POTIONS, POTIONS, TORCH), mirrored.inventoryIds());
    assertEquals(mirrored.state().inventory.size(), mirrored.inventoryIds().size());
    assertEquals(DAGGER, mirrored.equipmentIds().get("WEAPON"));
    assertEquals(GAUNTLETS, mirrored.equipmentIds().get("LEFT_HAND"));
    assertEquals(GAUNTLETS, mirrored.equipmentIds().get("RIGHT_HAND"));
    assertEquals(12L, mirrored.revision());
    assertEquals(4L, mirrored.locationEpoch());
    assertEquals(CHARACTER, mirrored.characterId());
  }

  @Test
  void buffsComeBackWithTheirRemainingTimeAndTheirHealthIsTakenOutOfTheBase() {
    var buff =
        new CharacterProgress.Buff(
            "spell.light", NOW + 90_000L, 30, 20, Map.of("maxHp", 25), NOW - 30_000L);
    var mirrored =
        ProgressMirror.toStateDto(
            view(progress(List.of(buff), Map.of(BodyPart.WEAPON.name(), DAGGER))), () -> NOW);
    PlayerStateDto state = mirrored.state();

    assertEquals(155, state.maxHp, "base health: the server figure minus the buff's 25");
    assertEquals(1, state.activeBuffs.size());
    assertEquals("spell.light", state.activeBuffs.get(0).spellName);
    assertEquals(90L, state.activeBuffs.get(0).remainingSeconds);
    assertEquals(120L, state.activeBuffs.get(0).totalDurationSeconds);
    assertEquals(30, state.activeBuffs.get(0).casterIntelligence);
    assertEquals(20, state.activeBuffs.get(0).casterWisdom);

    Player player = new Player();
    new RemoteCharacterSource(() -> null).applyTo(state, player);
    assertTrue(player.hasRadianceBuff(), "the buff is running on the Player");
  }

  @Test
  void expiredBuffsAreDroppedAndEndlessOnesStayEndless() {
    var expired = new CharacterProgress.Buff("spell.light", NOW - 1L, 0, 0);
    var forever = new CharacterProgress.Buff("spell.bless", Long.MAX_VALUE, 0, 0);
    var mirrored =
        ProgressMirror.toStateDto(view(progress(List.of(expired, forever), Map.of())), () -> NOW);
    assertEquals(1, mirrored.state().activeBuffs.size());
    assertEquals(Long.MAX_VALUE, mirrored.state().activeBuffs.get(0).remainingSeconds);
  }

  @Test
  void aCharacterWithoutServerStateOrPositionIsRefusedNotInvented() {
    OnlineConnection.View ready = view(fixture());
    assertThrows(IllegalStateException.class, () -> ProgressMirror.toStateDto(null));
    var legacy = new OnlineCharacterView(CHARACTER, "Old", 0, 0, 0, null);
    assertThrows(
        IllegalStateException.class,
        () ->
            ProgressMirror.toStateDto(
                new OnlineConnection.View(7L, ready.map(), ready.snapshot(), legacy, List.of())));
    assertThrows(
        IllegalStateException.class,
        () ->
            ProgressMirror.toStateDto(
                new OnlineConnection.View(
                    99L, ready.map(), ready.snapshot(), ready.character(), List.of())));
    assertThrows(
        IllegalStateException.class,
        () ->
            ProgressMirror.toStateDto(
                new OnlineConnection.View(7L, ready.map(), null, ready.character(), List.of())));
  }

  @Test
  void anExperienceBelowTheLevelFloorClampsAtZero() {
    var progress = fixture();
    var character = new OnlineCharacterView(CHARACTER, "Aelwyn", 0, 0, 1, progress, 0);
    var ready = view(progress);
    var mirrored =
        ProgressMirror.toStateDto(
            new OnlineConnection.View(7L, ready.map(), ready.snapshot(), character, List.of()),
            () -> NOW);
    assertEquals(0L, mirrored.state().currentXp);
  }

  @Test
  void theSingleplayerSaveRefusesTheMirroredPlayer(@TempDir Path dir) throws IOException {
    String original = System.getProperty("user.dir");
    System.setProperty("user.dir", dir.toString());
    try {
      PlayerStateStore.resetActiveFilename();
      var source = new RemoteCharacterSource(() -> view(fixture()), () -> NOW);
      PlayerStateDto state = source.loadInitialState();
      Player player = new Player();
      source.applyTo(state, player);

      PlayerStateStore.save(player);
      PlayerStateStore.save(player, 3f);

      try (var files = Files.list(dir)) {
        assertEquals(0, files.count(), "nothing may be written for an online character");
      }
    } finally {
      System.setProperty("user.dir", original);
    }
  }

  @Test
  void thePlayerIsMarkedRemoteEvenWhenThereIsNoState() {
    Player player = new Player();
    new RemoteCharacterSource(() -> null).applyTo(null, player);
    assertTrue(player.isRemote());
  }
}
