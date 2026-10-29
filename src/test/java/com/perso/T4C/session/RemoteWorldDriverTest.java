package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundSpy;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.LighthavenSamaritan;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.npc.core.PuppetNpc;
import com.perso.T4C.objects.GroundItem;
import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.GroundLootView;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.online.model.OnlineRegion;
import com.perso.T4C.player.Player;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/** Fake snapshots in, real puppet monsters, NPCs, loot and players out. No network. */
@ResourceLock("sound.manager")
class RemoteWorldDriverTest {
  static final long OWN = 7L;
  static final OnlineConnection.MapInfo MAP =
      new OnlineConnection.MapInfo(100, 200, 80, 60, true, 6, 0, 0, "avalon", 4L);

  private OnlineConnection.View view;
  private GroundItemManager ground;
  private RemoteWorldDriver driver;
  private WorldEntities entities;

  static CreatureView creature(long id, String definition, double x, double y, int health, int max) {
    return new CreatureView(id, definition, x, y, health, max, "walk", null, null);
  }

  static OnlineConnection.View view(
      long tick,
      OnlineRegion region,
      List<OnlineConnection.Actor> players,
      List<CreatureView> creatures,
      List<NpcView> npcs,
      List<GroundLootView> loot) {
    var snapshot = new OnlineConnection.Snapshot(tick, players, 1L, creatures, region, npcs, loot);
    return new OnlineConnection.View(OWN, MAP, snapshot, null, List.of());
  }

  static OnlineConnection.Actor actor(long id, double x, double y, int hp, int max) {
    return new OnlineConnection.Actor(id, x, y, 1L, 0L, hp, max);
  }

  private OnlineConnection.View snapshot(
      long tick, List<CreatureView> creatures, List<NpcView> npcs, List<GroundLootView> loot) {
    return view(tick, MAP.region(), List.of(actor(OWN, 3.5, 5.5, 100, 100)), creatures, npcs, loot);
  }

  @BeforeEach
  void build() {
    ground = new GroundItemManager();
    driver = new RemoteWorldDriver(() -> view, ground);
    view = null;
    entities =
        driver.loadEntities(new WorldEntityInputs(null, null, null, () -> null, new Player()));
  }

  @AfterEach
  void removeSoundListener() {
    SoundSpy.remove();
  }

  private void tick(float delta) {
    driver.tickEntities(
        new EntityTick(
            entities.npcs(),
            entities.monsters(),
            new Player(),
            new Vector2(),
            delta,
            0,
            1000,
            0,
            1000,
            5));
  }

  private String skeleton() {
    return MonsterRegistry.findByName("Skeleton").getName();
  }

  @SuppressWarnings("unchecked")
  private List<GroundItem> groundItems() throws Exception {
    var field = GroundItemManager.class.getDeclaredField("items");
    field.setAccessible(true);
    return (List<GroundItem>) field.get(ground);
  }

  // --- the registries ---

  @Test
  void loadingBuildsEmptyPuppetRegistriesWithNothingQueuedFromTheLocalMap() {
    assertTrue(entities.monsters().isPuppetMode());
    assertTrue(entities.npcs().isPuppetMode());
    tick(1f);
    assertTrue(entities.monsters().getMonsters().isEmpty());
    assertTrue(entities.npcs().getNPCs().isEmpty());
  }

  @Test
  void aTickBeforeAnyViewDoesNothingAndALaterViewIsApplied() {
    tick(0.1f);
    view = snapshot(1, List.of(creature(1, skeleton(), 3, 4, 10, 10)), List.of(), List.of());
    tick(0.1f);
    assertEquals(1, entities.monsters().getMonsters().size());
  }

  @Test
  void loadingWithAViewAlreadyPresentFillsTheRegistriesAtOnce() {
    view = snapshot(1, List.of(creature(1, skeleton(), 3, 4, 10, 10)), List.of(), List.of());
    entities = driver.loadEntities(new WorldEntityInputs(null, null, null, () -> null, null));
    assertEquals(1, entities.monsters().getMonsters().size());
  }

  // --- monsters ---

  @Test
  void creaturesBecomeRealPuppetMonstersAtTheirAnchors() {
    view =
        snapshot(
            1,
            List.of(creature(1, skeleton(), 3, 4, 7, 10), creature(2, skeleton(), 5, 6, 10, 10)),
            List.of(),
            List.of());
    tick(0.016f);

    List<BaseMonster> monsters = entities.monsters().getMonsters();
    assertEquals(2, monsters.size());
    BaseMonster first = monsters.get(0);
    assertTrue(first.isPuppet());
    assertEquals(
        MonsterRegistry.create(MonsterRegistry.findByName("Skeleton"), 0, 0).getClass(),
        first.getClass(),
        "built by the same factory single-player uses");
    assertEquals((100 + 3) * GRID_W, first.getPosition().x);
    assertEquals((200 + 4) * GRID_H, first.getPosition().y);
    assertEquals(1L, driver.creatureId(first));
    assertEquals(2L, driver.creatureId(monsters.get(1)));
    assertEquals(-1L, driver.creatureId(null));
    assertEquals(7, first.getHealth());
    assertEquals(10, first.getMaxHealth());
  }

  @Test
  void updatesMoveHealAndRemoveWhileKnownCreaturesKeepTheirObjects() {
    view =
        snapshot(
            1,
            List.of(creature(1, skeleton(), 3, 4, 10, 10), creature(2, skeleton(), 5, 6, 10, 10)),
            List.of(),
            List.of());
    tick(0.016f);
    BaseMonster first = entities.monsters().getMonsters().get(0);

    view = snapshot(2, List.of(creature(1, skeleton(), 4, 4, 6, 10)), List.of(), List.of());
    tick(0.016f);

    assertEquals(1, entities.monsters().getMonsters().size());
    assertSame(first, entities.monsters().getMonsters().get(0));
    assertEquals(6, first.getHealth());
    assertTrue(first.getMovement().isMoving(), "turned toward the move");
    assertTrue(first.getPosition().x < (100 + 4) * GRID_W, "still gliding after one frame");
    tick(2f);
    assertEquals((100 + 4) * GRID_W, first.getPosition().x);
  }

  @Test
  void aDeathSnapshotKillsSilentlyAndARespawnSnapshotRevives() {
    view = snapshot(1, List.of(creature(1, skeleton(), 3, 4, 10, 10)), List.of(), List.of());
    tick(0.016f);
    BaseMonster monster = entities.monsters().getMonsters().get(0);
    int[] calls = {0};
    entities.monsters().setDeathCallback(m -> calls[0]++);
    entities.monsters().setLootCallback(m -> calls[0]++);
    entities.monsters().setPlayerKillCallback(m -> calls[0]++);
    SoundSpy sounds = SoundSpy.install();

    view = snapshot(2, List.of(creature(1, skeleton(), 3, 4, 0, 10)), List.of(), List.of());
    tick(0.016f);
    tick(5f);

    assertTrue(monster.isDead());
    assertEquals(1, entities.monsters().getMonsters().size(), "the corpse stays until the server drops it");
    assertEquals(0, calls[0]);
    assertTrue(sounds.asked().isEmpty());

    view = snapshot(3, List.of(creature(1, skeleton(), 3, 4, 10, 10)), List.of(), List.of());
    tick(0.016f);
    assertFalse(monster.isDead());
    assertSame(monster, entities.monsters().getMonsters().get(0));
  }

  @Test
  void anUnknownDefinitionIsSkippedAndRecordedNotFatal() {
    view =
        snapshot(
            1,
            List.of(creature(1, "No Such Creature", 3, 4, 10, 10), creature(2, skeleton(), 5, 6, 10, 10)),
            List.of(),
            List.of());
    tick(0.016f);
    assertEquals(1, entities.monsters().getMonsters().size());
    assertEquals(java.util.Set.of("No Such Creature"), driver.unknownDefinitions());
  }

  @Test
  void aSnapshotWithTheSameTickIsNotAppliedTwice() {
    view = snapshot(1, List.of(creature(1, skeleton(), 3, 4, 10, 10)), List.of(), List.of());
    tick(0.016f);
    view = snapshot(1, List.of(), List.of(), List.of());
    tick(0.016f);
    assertEquals(1, entities.monsters().getMonsters().size());
  }

  @Test
  void aSnapshotOfAnotherRegionEpochIsNeverMixedIn() {
    view = snapshot(1, List.of(creature(1, skeleton(), 3, 4, 10, 10)), List.of(), List.of());
    tick(0.016f);
    var stale = new OnlineRegion(0, "avalon", 100, 200, 80, 60, 3L);
    view =
        view(2, stale, List.of(actor(OWN, 3.5, 5.5, 1, 1)), List.of(), List.of(), List.of());
    tick(0.016f);
    assertEquals(1, entities.monsters().getMonsters().size(), "previous state kept");
  }

  @Test
  void aNewRegionGenerationReplacesEverythingOfTheOldOne() {
    view = snapshot(1, List.of(creature(1, skeleton(), 3, 4, 10, 10)), List.of(), List.of());
    tick(0.016f);
    BaseMonster old = entities.monsters().getMonsters().get(0);
    var next = new OnlineConnection.MapInfo(100, 200, 80, 60, true, 6, 0, 0, "avalon", 5L);
    var snapshot =
        new OnlineConnection.Snapshot(
            2,
            List.of(actor(OWN, 3.5, 5.5, 1, 1)),
            1L,
            List.of(creature(1, skeleton(), 9, 9, 10, 10)),
            next.region(),
            List.of(),
            List.of());
    view = new OnlineConnection.View(OWN, next, snapshot, null, List.of());
    tick(0.016f);

    assertEquals(1, entities.monsters().getMonsters().size());
    assertNotSame(old, entities.monsters().getMonsters().get(0));
    assertEquals((100 + 9) * GRID_W, entities.monsters().getMonsters().get(0).getPosition().x);
  }

  // --- NPCs ---

  @Test
  void aRegisteredNpcIdBecomesTheRealNpcClassAndAnAppearanceOnlyIdAPuppetNpc() {
    var registered =
        new NpcView(
            LighthavenSamaritan.ID,
            "Samaritan",
            2,
            3,
            LighthavenSamaritan.SPRITE_BASE,
            Map.of());
    var unregistered =
        new NpcView(
            "N0123456789", "${npc.aaronbrownbark}", 4, 5, LighthavenSamaritan.SPRITE_BASE, Map.of());
    view = snapshot(1, List.of(), List.of(registered, unregistered), List.of());
    tick(0.016f);

    List<BaseNPC> npcs = entities.npcs().getNPCs();
    assertEquals(2, npcs.size());
    assertInstanceOf(LighthavenSamaritan.class, npcs.get(0));
    assertInstanceOf(PuppetNpc.class, npcs.get(1));
    assertTrue(npcs.get(0).isPuppet());
    assertTrue(npcs.get(1).isPuppet());
    assertEquals(LighthavenSamaritan.ID, driver.npcId(npcs.get(0)));
    assertEquals("N0123456789", driver.npcId(npcs.get(1)));
    assertEquals((100 + 2) * GRID_W, npcs.get(0).getPosition().x);
    assertEquals((200 + 5) * GRID_H, npcs.get(1).getPosition().y);

    view = snapshot(2, List.of(), List.of(registered), List.of());
    tick(0.016f);
    assertEquals(1, entities.npcs().getNPCs().size());
  }

  // --- loot ---

  @Test
  void groundLootAppearsMovesAndLeavesWithTheSnapshot() throws Exception {
    UUID one = UUID.randomUUID();
    UUID two = UUID.randomUUID();
    view =
        snapshot(
            1,
            List.of(),
            List.of(),
            List.of(
                new GroundLootView(one, 3, 4, "item.dagger", null, 0),
                new GroundLootView(two, 5, 6, "item.healing_potion", null, 0)));
    tick(0.016f);
    assertEquals(2, groundItems().size());
    GroundItem dagger = groundItems().get(0);
    assertEquals("item.dagger", dagger.getItemName());
    assertEquals((100 + 3) * GRID_W, dagger.getPosition().x);
    assertEquals((200 + 4) * GRID_H, dagger.getPosition().y);

    view =
        snapshot(2, List.of(), List.of(), List.of(new GroundLootView(one, 4, 4, "item.dagger", null, 0)));
    tick(0.016f);
    assertEquals(1, groundItems().size());
    assertSame(dagger, groundItems().get(0));
    assertEquals((100 + 4) * GRID_W, dagger.getPosition().x);
  }

  // --- other players ---

  @Test
  void otherPlayersArePuppetPlayersAndTheOwnActorIsLeftToTheMirror() {
    view =
        view(
            1,
            MAP.region(),
            List.of(actor(OWN, 3.5, 5.5, 100, 100), actor(9, 10.5, 12.5, 40, 80)),
            List.of(),
            List.of(),
            List.of());
    tick(0.016f);

    List<Player> others = driver.otherPlayers();
    assertEquals(1, others.size());
    Player other = others.get(0);
    assertTrue(other.isRemote());
    assertEquals((100 + 10.5 - 0.5) * GRID_W, other.getPositionVector().x, 0.01f);
    assertEquals((200 + 12.5 - 0.5) * GRID_H, other.getPositionVector().y, 0.01f);
    assertEquals(40, other.getCurrentHp());
    assertEquals(80, other.getMaxHp());

    view = view(2, MAP.region(), List.of(actor(OWN, 3.5, 5.5, 100, 100)), List.of(), List.of(), List.of());
    tick(0.016f);
    assertTrue(driver.otherPlayers().isEmpty());
  }

  @Test
  void anotherPlayerWalksWhileItGlidesToANewPositionAndRestsWhenItArrives() {
    view =
        view(1, MAP.region(), List.of(actor(OWN, 3.5, 5.5, 100, 100), actor(9, 10.5, 12.5, 40, 80)),
            List.of(), List.of(), List.of());
    tick(0.016f);
    Player other = driver.otherPlayers().get(0);
    assertFalse(other.getMovement().isMoving(), "placed, not walking");

    view =
        view(2, MAP.region(), List.of(actor(OWN, 3.5, 5.5, 100, 100), actor(9, 13.5, 12.5, 40, 80)),
            List.of(), List.of(), List.of());
    tick(0.016f);
    assertTrue(other.getMovement().isMoving(), "on its way to the new tile");
    for (int i = 0; i < 100; i++) tick(0.05f);
    assertFalse(other.getMovement().isMoving(), "arrived");
  }

  // --- the session ---

  @Test
  void theRemoteSessionHandsOutThisDriver() {
    var session = new RemoteGameSession(() -> view, ground);
    assertInstanceOf(RemoteWorldDriver.class, session.world());
  }
}
