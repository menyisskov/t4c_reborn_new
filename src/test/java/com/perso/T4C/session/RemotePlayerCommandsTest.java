package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.LighthavenSamaritan;
import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.GroundLootView;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.online.model.ShopOffersReply;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * The online commands against a fake link (T4C-0292): every command maps to the right request,
 * refusals reach the host as messages, the local player never changes on an attack, pickup or
 * interaction, and own movement is predicted and reconciled.
 */
@ResourceLock("CollisionManager")
class RemotePlayerCommandsTest {
  private static final long OWN = 7;
  private static final OnlineConnection.MapInfo MAP =
      new OnlineConnection.MapInfo(100, 200, 80, 60, true, 6, 0, 0, "avalon", 4L);

  /** Records every request and answers with whatever the test queued. */
  private static final class FakeLink implements RemoteLink {
    OnlineConnection.View view;
    final List<String> calls = new ArrayList<>();
    final List<int[]> directions = new ArrayList<>();
    final List<Long> frames = new ArrayList<>();
    final Map<Long, Long> acknowledged = new HashMap<>();
    CompletableFuture<OnlineConnection.CommandResult> next = new CompletableFuture<>();
    CompletableFuture<OnlineConnection.CommandResult> offers = new CompletableFuture<>();
    CompletableFuture<OnlineConnection.CommandResult> trade = new CompletableFuture<>();
    RuntimeException throwOnSend;

    @Override
    public OnlineConnection.View view() {
      return view;
    }

    private CompletionStage<OnlineConnection.CommandResult> reply(String call) {
      if (throwOnSend != null) throw throwOnSend;
      calls.add(call);
      return next;
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> attack(long creature) {
      return reply("attack " + creature);
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> pickUp(UUID loot) {
      return reply("pickup " + loot);
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> interact(String npc) {
      return reply("interact " + npc);
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> equip(UUID instance, BodyPart slot) {
      return reply("equip " + instance + " " + slot);
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> unequip(BodyPart slot) {
      return reply("unequip " + slot);
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> cast(String spellKey) {
      return reply("cast " + spellKey);
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> cast(String spellKey, long creature) {
      return reply("cast " + spellKey + " " + creature);
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> useItem(UUID itemInstance) {
      return reply("use " + itemInstance);
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> shopOffers(String npc) {
      calls.add("shopOffers " + npc);
      return offers;
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> shopBuy(String npc, String itemKey, int quantity) {
      calls.add("shopBuy " + npc + " " + itemKey + " " + quantity);
      return trade;
    }

    @Override
    public CompletionStage<OnlineConnection.CommandResult> shopSell(String npc, String itemKey, int quantity) {
      calls.add("shopSell " + npc + " " + itemKey + " " + quantity);
      return trade;
    }

    @Override
    public void direction(int dx, int dy, long frame) {
      directions.add(new int[] {dx, dy});
      frames.add(frame);
    }

    @Override
    public long acknowledgedFrame(long sequence) {
      return acknowledged.getOrDefault(sequence, -1L);
    }

    void answer(String key) {
      next.complete(new OnlineConnection.CommandResult(key == null, null, key));
    }
  }

  @Test
  void equipAndUnequipAreSentToServerWithoutChangingLocalPlayer() {
    UUID item = UUID.fromString("00000000-0000-0000-0000-000000000042");
    Player player = playerAtTile(100, 205);
    var beforeInventory = new ArrayList<>(player.getInventory());
    var beforeEquipment = new HashMap<>(player.getEquippedItems());
    FakeLink link = new FakeLink();
    var commands = new RemoteGameSession(link, new GroundItemManager()).commands();
    commands.bind(new TestHost(player));

    assertTrue(commands.equip(item, BodyPart.WEAPON));
    assertEquals(List.of("equip " + item + " WEAPON"), link.calls);
    assertTrue(commands.unequip(BodyPart.WEAPON));
    assertEquals(List.of("equip " + item + " WEAPON", "unequip WEAPON"), link.calls);
    assertEquals(beforeInventory, player.getInventory());
    assertEquals(beforeEquipment, player.getEquippedItems());
  }

  @Test
  void castAndPotionUseAreSentToServerWithoutLocalCostOrInventoryChanges() {
    UUID potion = UUID.fromString("00000000-0000-0000-0000-000000000043");
    Player player = playerAtTile(100, 205);
    var beforeInventory = new ArrayList<>(player.getInventory());
    int beforeMana = player.getMana();
    int beforeHealth = player.getCurrentHp();
    FakeLink link = new FakeLink();
    var commands = new RemoteGameSession(link, new GroundItemManager()).commands();
    commands.bind(new TestHost(player));

    assertTrue(commands.cast("spell.light"));
    assertTrue(commands.useItem(potion));
    assertEquals(List.of("cast spell.light", "use " + potion), link.calls);
    assertEquals(beforeInventory, player.getInventory());
    assertEquals(beforeMana, player.getMana());
    assertEquals(beforeHealth, player.getCurrentHp());
  }

  private static final class TestHost implements RemotePlayerCommands.Host {
    final Player player;
    final List<String> messages = new ArrayList<>();
    final List<String> shops = new ArrayList<>();

    TestHost(Player player) {
      this.player = player;
    }

    @Override
    public Player player() {
      return player;
    }

    @Override
    public void showMessage(String text) {
      messages.add(text);
    }

    @Override
    public void showShop(String npc, ShopOffersReply offers) {
      shops.add(npc + ":" + offers.gold());
    }
  }

  private FakeLink link;
  private GroundItemManager ground;
  private RemoteGameSession session;
  private WorldEntities entities;
  private Player player;
  private TestHost host;

  @BeforeAll
  static void items() {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister();
  }

  private static Player playerAtTile(int tileX, int tileY) {
    Player player = new Player();
    player.setMapBounds(500 * GRID_W, 500 * GRID_H);
    player.setWorldPosition(tileX * GRID_W, tileY * GRID_H, 0);
    return player;
  }

  @BeforeEach
  void build() {
    link = new FakeLink();
    ground = new GroundItemManager();
    session = new RemoteGameSession(link, ground);
    player = playerAtTile(100, 205);
    host = new TestHost(player);
    session.commands().bind(host);
    entities =
        session.world().loadEntities(new WorldEntityInputs(null, null, null, () -> null, player));
  }

  @AfterEach
  void clear() {
    CollisionManager.getInstance().clear();
  }

  private static OnlineConnection.Actor actor(double x, double y, long sequence) {
    return new OnlineConnection.Actor(OWN, x, y, sequence, 0L, 100, 100);
  }

  /** The region-local tile centre of a tile of the absolute world, as the server reports it. */
  private static double localX(double worldTile) {
    return worldTile - MAP.originX() + 0.5;
  }

  private static double localY(double worldTile) {
    return worldTile - MAP.originY() + 0.5;
  }

  private static OnlineConnection.View view(
      long tick,
      long epoch,
      OnlineConnection.Actor own,
      List<CreatureView> creatures,
      List<NpcView> npcs,
      List<GroundLootView> loot) {
    var map =
        new OnlineConnection.MapInfo(
            MAP.originX(), MAP.originY(), 80, 60, true, 6, 0, 0, "avalon", epoch);
    var snapshot =
        new OnlineConnection.Snapshot(
            tick, List.of(own), 1L, creatures, map.region(), npcs, loot);
    return new OnlineConnection.View(OWN, map, snapshot, null, List.of());
  }

  private void tickWorld() {
    session
        .world()
        .tickEntities(
            new EntityTick(
                entities.npcs(),
                entities.monsters(),
                player,
                new Vector2(),
                0.016f,
                0,
                100000,
                0,
                100000,
                5));
  }

  private BaseMonster skeleton() {
    return entities.monsters().getMonsters().get(0);
  }

  private void showSkeleton(long id) {
    String name = MonsterRegistry.findByName("Skeleton").getName();
    link.view =
        view(
            1,
            4,
            actor(localX(100), localY(205), 0),
            List.of(new CreatureView(id, name, 3, 4, 30, 40, "walk", null, null)),
            List.of(),
            List.of());
    tickWorld();
  }

  // --- attack ---

  @Test
  void attackSendsTheServerCreatureIdAndChangesNothingLocally() {
    showSkeleton(41);
    BaseMonster monster = skeleton();
    float x = player.getCoordinates().getX();
    float y = player.getCoordinates().getY();
    int hp = player.getCurrentHp();

    session.commands().attack(monster);

    assertEquals(List.of("attack 41"), link.calls);
    assertEquals(30, monster.getHealth(), "damage is the server's: the puppet is untouched");
    assertEquals(x, player.getCoordinates().getX());
    assertEquals(y, player.getCoordinates().getY());
    assertEquals(hp, player.getCurrentHp());
    assertTrue(player.isAttackReady(), "no local cooldown starts");
    assertFalse(player.getAnimations().isAttacking(), "no local swing starts");
    assertTrue(link.directions.isEmpty(), "an attack sends nothing about movement");
  }

  @Test
  void attackingNothingOrAnObjectTheServerDidNotSendSendsNothing() {
    session.commands().attack(null);
    BaseMonster stranger = MonsterRegistry.create(MonsterRegistry.findByName("Skeleton"), 0, 0);
    session.commands().attack(stranger);
    assertTrue(link.calls.isEmpty());
  }

  @Test
  void aRefusalComesBackAsTheSinglePlayerStyleMessage() {
    showSkeleton(41);
    session.commands().attack(skeleton());
    assertTrue(host.messages.isEmpty(), "nothing is shown before the reply");

    link.answer("online.attack.out_of_reach");
    session.commands().pump();

    assertEquals(List.of(I18n.key("online.attack.out_of_reach")), host.messages);
  }

  @Test
  void theRoutineCooldownRefusalAndAnAcceptedSwingStaySilent() {
    showSkeleton(41);
    session.commands().attack(skeleton());
    link.answer(RemotePlayerCommands.SILENT_COOLDOWN_KEY);
    session.commands().pump();
    link.next = new CompletableFuture<>();
    session.commands().attack(skeleton());
    link.answer(null);
    session.commands().pump();
    assertTrue(host.messages.isEmpty());
  }

  @Test
  void aBusyOrDownLinkAndAFailedReplyAreSaidOutLoud() {
    showSkeleton(41);
    link.throwOnSend = new IllegalStateException("Gameplay command pending");
    session.commands().attack(skeleton());
    session.commands().pump();
    assertEquals(List.of(I18n.key("online.attack.busy")), host.messages);

    link.throwOnSend = null;
    host.messages.clear();
    session.commands().attack(skeleton());
    link.next.completeExceptionally(new IllegalStateException("connection lost"));
    session.commands().pump();
    assertEquals(List.of(I18n.key("online.attack.busy")), host.messages);
  }

  @Test
  void aReplyWithArgumentsFillsThePlaceholders() {
    showSkeleton(41);
    session.commands().attack(skeleton());
    link.next.complete(
        new OnlineConnection.CommandResult(true, null, "online.shop.bought", List.of(2, 18)));
    session.commands().pump();
    assertEquals(List.of(I18n.message("online.shop.bought", 2, 18)), host.messages);
  }

  // --- pickup and interaction by click ---

  @Test
  void clickingLootSendsItsServerIdAndTakesNothingLocally() throws Exception {
    UUID id = UUID.randomUUID();
    link.view =
        view(
            1,
            4,
            actor(localX(100), localY(205), 0),
            List.of(),
            List.of(),
            List.of(new GroundLootView(id, 3, 4, "item.dagger", null, 0)));
    tickWorld();
    player.setStrength(500);
    float lootX = (MAP.originX() + 3) * GRID_W;
    float lootY = (MAP.originY() + 4) * GRID_H;

    GroundItemManager.PickupResult result = ground.pickUpAt(lootX, lootY, player, 99, 3);

    assertEquals(GroundItemManager.PickupStatus.REQUESTED, result.status);
    assertEquals(List.of("pickup " + id), link.calls);
    link.next = new CompletableFuture<>();
    assertEquals(0, InventoryService.count(player, "item.dagger"));
    assertEquals(
        GroundItemManager.PickupStatus.REQUESTED,
        ground.pickUpAt(lootX, lootY, player, 99, 3).status,
        "the item is still on the ground until a snapshot removes it");

    link.answer("online.loot.out_of_range");
    session.commands().pump();
    assertEquals(List.of(I18n.key("online.loot.out_of_range")), host.messages);
  }

  @Test
  void clickingAnNpcSendsItsPublicIdAndStartsNoLocalConversation() {
    link.view =
        view(
            1,
            4,
            actor(localX(100), localY(205), 0),
            List.of(),
            List.of(
                new NpcView(
                    LighthavenSamaritan.ID,
                    "Samaritan",
                    3,
                    4,
                    LighthavenSamaritan.SPRITE_BASE,
                    Map.of())),
            List.of());
    tickWorld();
    var npc = entities.npcs().getNPCs().get(0);

    boolean handled =
        entities.npcs().onClick(npc.getPosition().x, npc.getPosition().y, player);

    assertTrue(handled);
    assertEquals(List.of("interact " + LighthavenSamaritan.ID), link.calls);
    assertFalse(entities.npcs().talkToActiveNpc("hello", player));

    link.answer("online.npc.unavailable");
    session.commands().pump();
    assertEquals(List.of(I18n.key("online.npc.unavailable")), host.messages);
  }

  @Test
  void shopkeeperClickOpensServerListingAndTradeRefreshesItWithoutLocalMutation() {
    String npc = LighthavenSamaritan.ID;
    var listing = new ShopOffersReply(4, 80, npc,
        List.of(new ShopOffersReply.Line("item.dagger", 12, 2)), List.of());
    int goldBefore = player.getGold();
    int daggersBefore = InventoryService.count(player, "item.dagger");

    assertTrue(session.commands().interact(npc));
    link.next.complete(new OnlineConnection.CommandResult(true, null, "npc.shop"));
    assertEquals(List.of("interact " + npc, "shopOffers " + npc), link.calls);
    link.offers.complete(new OnlineConnection.CommandResult(true, null, null, List.of(), listing));
    assertTrue(host.shops.isEmpty(), "network replies are delivered by the render-thread pump");
    session.commands().pump();
    assertEquals(List.of(npc + ":80"), host.shops);

    link.offers = new CompletableFuture<>();
    link.trade = new CompletableFuture<>();
    assertTrue(session.commands().shopBuy(npc, "item.dagger", 1));
    link.trade.complete(new OnlineConnection.CommandResult(true, null, "online.shop.bought", List.of(1, 12)));
    assertEquals("shopOffers " + npc, link.calls.get(link.calls.size() - 1));
    link.offers.complete(new OnlineConnection.CommandResult(true, null, null, List.of(),
        new ShopOffersReply(5, 68, npc, List.of(new ShopOffersReply.Line("item.dagger", 12, 1)), List.of())));
    session.commands().pump();
    assertEquals(List.of(npc + ":80", npc + ":68"), host.shops);
    assertEquals(List.of(I18n.message("online.shop.bought", 1, 12)), host.messages);
    assertEquals(goldBefore, player.getGold());
    assertEquals(daggersBefore, InventoryService.count(player, "item.dagger"));

    link.next = new CompletableFuture<>();
    link.offers = new CompletableFuture<>();
    assertTrue(session.commands().interact(npc));
    link.next.complete(new OnlineConnection.CommandResult(true, null, "npc.shop"));
    session.commands().closeShop();
    link.offers.complete(new OnlineConnection.CommandResult(true, null, null, List.of(), listing));
    session.commands().pump();
    assertEquals(List.of(npc + ":80", npc + ":68"), host.shops, "closing ignores a late listing reply");
  }

  // --- movement ---

  @Test
  void movementIsPredictedWithTheSinglePlayerRuleAndTheDirectionIsSentTagged() {
    Player twin = playerAtTile(100, 205);
    for (int i = 0; i < 20; i++) {
      session.commands().move(1, 0.5f, 0.02f);
      twin.move(1, 0.5f, 0.02f);
      assertEquals(twin.getCoordinates().getX(), player.getCoordinates().getX());
      assertEquals(twin.getCoordinates().getY(), player.getCoordinates().getY());
    }
    assertTrue(player.getCoordinates().getX() > 100 * GRID_W, "it moved at once");
    assertEquals(20, link.directions.size());
    assertEquals(1, link.directions.get(0)[0]);
    assertEquals(1, link.directions.get(0)[1]);
    for (int i = 0; i < link.frames.size(); i++) assertEquals(i, link.frames.get(i));

    session.commands().move(0, 0, 0.02f);
    assertEquals(0, link.directions.get(20)[0]);
    assertEquals(0, link.directions.get(20)[1]);
  }

  @Test
  void aStunnedPlayerSendsNoDirectionBecauseTheLocalGateStopsIt() {
    player.stunFor(60_000);
    float x = player.getCoordinates().getX();
    session.commands().move(1, 0, 0.05f);
    assertEquals(x, player.getCoordinates().getX());
    assertEquals(0, link.directions.get(0)[0]);
  }

  @Test
  void movingBeforeTheScreenBoundTheCommandsFailsLoudly() {
    var unbound = new RemotePlayerCommands(new FakeLink(), monster -> -1);
    assertThrows(IllegalStateException.class, () -> unbound.move(1, 0, 0.016f));
  }

  /** A snapshot that carries the server position and acknowledges input {@code sequence}. */
  private void serverAt(long tick, double tileX, double tileY, long sequence, long frame) {
    link.acknowledged.put(sequence, frame);
    link.view = view(tick, 4, actor(tileX, tileY, sequence), List.of(), List.of(), List.of());
  }

  @Test
  void aFirstSnapshotFarFromThePlayerSnapsAndOneNearItLeavesItAlone() {
    // The first view only restarts the replay from the server.
    serverAt(1, localX(100), localY(205), 0, -1);
    session.commands().move(0, 0, 0.016f);
    assertEquals(100 * GRID_W, player.getCoordinates().getX());

    // Same epoch, same tile: nothing to correct.
    serverAt(2, localX(100) + 0.2, localY(205), 1, 0);
    session.commands().move(0, 0, 0.016f);
    assertEquals(100 * GRID_W, player.getCoordinates().getX(), "under the tolerance: ignored");
  }

  @Test
  void anErrorOfOneTileOrMoreSnapsToTheServerPosition() {
    serverAt(1, localX(100), localY(205), 0, -1);
    session.commands().move(0, 0, 0.016f);

    // The server says we are three tiles to the right and two down (a wall, a teleport).
    serverAt(2, localX(103), localY(207), 1, 0);
    session.commands().move(0, 0, 0.016f);

    assertEquals(103 * GRID_W, player.getCoordinates().getX(), 0.01f);
    assertEquals(207 * GRID_H, player.getCoordinates().getY(), 0.01f);
  }

  @Test
  void anErrorBetweenTheToleranceAndATileIsEasedInOverAboutAHundredMilliseconds() {
    serverAt(1, localX(100), localY(205), 0, -1);
    session.commands().move(0, 0, 0.016f);

    serverAt(2, localX(100) + 0.75, localY(205), 1, 0);
    session.commands().move(0, 0, 0.016f);
    float first = player.getCoordinates().getX();
    assertTrue(first > 100 * GRID_W && first < 100.75f * GRID_W, "started, not snapped");
    for (int i = 0; i < 40; i++) session.commands().move(0, 0, 0.02f);
    assertEquals(100.75f * GRID_W, player.getCoordinates().getX(), 0.6f);
  }

  @Test
  void unacknowledgedInputsAreReplayedOnTheServerPosition() {
    serverAt(1, localX(100), localY(205), 0, -1);
    session.commands().move(0, 0, 0.016f);
    float startX = player.getCoordinates().getX();
    // Walk right for ten frames; frame k is sent as input k+1 (frame 0 was the idle one above).
    for (int i = 0; i < 10; i++) session.commands().move(1, 0, 0.02f);
    float predicted = player.getCoordinates().getX();
    assertTrue(predicted > startX);

    // The server has applied the first five walking frames only and is at the matching place.
    RemoteMovementRule rule =
        new RemoteMovementRule(RemoteMovementRule.desktopTerrain(), () -> false, () -> player);
    var at = new com.perso.T4C.world.MovementReconciler.Position(startX, player.getCoordinates().getY());
    for (int frame = 1; frame <= 5; frame++) {
      at = rule.apply(at, new com.perso.T4C.world.MovementReconciler.Input(frame, 1, 0, 0.02));
    }
    double serverTileX = at.x() / GRID_W - MAP.originX() + 0.5;
    serverAt(2, serverTileX, localY(205), 5, 5);
    session.commands().move(0, 0, 0.016f);

    // Replaying frames 6..10 on that position lands where the player already is: nothing moves.
    assertEquals(predicted, player.getCoordinates().getX(), 8f);
  }

  @Test
  void aNewRegionEpochRestartsFromTheServerAndAMismatchedSnapshotIsIgnored() {
    serverAt(1, localX(100), localY(205), 0, -1);
    session.commands().move(0, 0, 0.016f);

    // A snapshot of another region than the map's epoch is never mixed in.
    var stale = view(2, 4, actor(localX(120), localY(205), 1), List.of(), List.of(), List.of());
    var other =
        new OnlineConnection.Snapshot(
            2,
            stale.snapshot().players(),
            1L,
            List.of(),
            new com.perso.T4C.online.model.OnlineRegion(0, "elsewhere", 0, 0, 80, 60, 4L),
            List.of(),
            List.of());
    link.view = new OnlineConnection.View(OWN, stale.map(), other, null, List.of());
    session.commands().move(0, 0, 0.016f);
    assertEquals(100 * GRID_W, player.getCoordinates().getX());

    // A new epoch (teleport, map change) snaps to wherever the server says.
    link.view = view(3, 5, actor(localX(120), localY(210), 0), List.of(), List.of(), List.of());
    session.commands().move(0, 0, 0.016f);
    assertEquals(120 * GRID_W, player.getCoordinates().getX(), 0.01f);
    assertEquals(210 * GRID_H, player.getCoordinates().getY(), 0.01f);
  }
}
