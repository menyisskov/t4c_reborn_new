package com.perso.T4C.online;

import com.google.gson.Gson;
import com.perso.T4C.online.model.OnlineCharacterView;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.GroundLootView;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.online.model.OnlineRegion;
import com.perso.T4C.online.model.OnlineWindow;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.online.model.NpcInteractionReply;
import com.perso.T4C.online.model.ShopOffersReply;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Network-only adapter for the local Alpha server; never touches graphics or character files. */
public final class OnlineConnection implements AutoCloseable {
  private static final int MAX_CHAT_TEXT = 160;
  private static final int MAX_CHAT_MESSAGES_PER_POLL = 50;
  private static final int MAX_QUEUED_CHAT_MESSAGES = 16;
  private static final long CHAT_POLL_INTERVAL_NANOS = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(500);
  private static final long CHAT_SEND_INTERVAL_NANOS = java.util.concurrent.TimeUnit.SECONDS.toNanos(1);
  private static final int MAX_CORPSE_ITEMS_PER_CORPSE = 256;
  private static final int MAX_CORPSE_ITEM_DETAILS = 2048;
  public record MapInfo(int originX, int originY, int width, int height, boolean persistent, int protocolVersion, int gameSocketPort,
      int world,String key,long locationEpoch,boolean streamed) {
    public MapInfo(int originX, int originY, int width, int height, boolean persistent, int protocolVersion, int gameSocketPort,
        int world,String key,long locationEpoch) {
      this(originX,originY,width,height,persistent,protocolVersion,gameSocketPort,world,key,locationEpoch,false);
    }
    public OnlineRegion region() { return new OnlineRegion(world,key,originX,originY,width,height,locationEpoch); }
  }

  public record Credentials(String secret, java.util.UUID characterId) {
    public Credentials {
      if (secret == null || !secret.matches("[0-9a-f]{64}") || characterId == null)
        throw new IllegalArgumentException("Invalid online credentials");
    }

    @Override
    public String toString() {
      return "Credentials[characterId=" + characterId + ", secret=REDACTED]";
    }
  }

  /**
   * One player in a snapshot. {@code name}, {@code gender} and {@code equipment} (worn slot to item
   * key, visible items only) are the server-derived public look; a server that does not send them
   * (or an actor with no committed character) leaves them null and the actor draws in the default look.
   */
  public record Actor(long id, double x, double y, long sequence, long appearance, int hp, int maxHp,
      String name, String gender, java.util.Map<String, String> equipment,
      com.perso.T4C.world.MovementRules.Reservation glide) {
    public Actor {
      if (equipment != null) {
        equipment = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(equipment));
      }
    }

    public Actor(long id, double x, double y, long sequence, long appearance, int hp, int maxHp) {
      this(id, x, y, sequence, appearance, hp, maxHp, null, null, null, null);
    }

    public Actor(long id, double x, double y, long sequence, long appearance, int hp, int maxHp,
        String name, String gender, java.util.Map<String, String> equipment) {
      this(id, x, y, sequence, appearance, hp, maxHp, name, gender, equipment, null);
    }

    /** Whether the optional public look is well formed; bounded so a hostile server cannot flood the renderer. */
    boolean lookValid() {
      if (name != null && (name.isBlank() || name.length() > 64)) return false;
      if (gender != null && !gender.equals("male") && !gender.equals("female")) return false;
      if (equipment != null) {
        if (equipment.size() > com.perso.T4C.online.model.PublicLook.MAX_SLOTS) return false;
        for (var worn : equipment.entrySet()) {
          if (worn.getKey() == null || worn.getValue() == null || worn.getValue().length() > 256) return false;
          try { com.perso.T4C.player.BodyPart.valueOf(worn.getKey()); }
          catch (IllegalArgumentException unknown) { return false; }
        }
      }
      return true;
    }

    boolean glideValid(MapInfo map) {
      if (glide == null) return true;
      return Float.isFinite(glide.targetX()) && Float.isFinite(glide.targetY())
          && glide.targetX() >= 0 && glide.targetY() >= 0
          && glide.targetX() < map.width() * (float) com.perso.T4C.config.GameConstants.GRID_W
          && glide.targetY() < map.height() * (float) com.perso.T4C.config.GameConstants.GRID_H
          && glide.moveX() >= -1 && glide.moveX() <= 1
          && glide.moveY() >= -1 && glide.moveY() <= 1
          && (glide.moveX() != 0 || glide.moveY() != 0);
    }
  }

  /** Loot is region-local like creatures; it is published only with a snapshot whose region matches the map epoch. */
  public record Snapshot(long tick, List<Actor> players, Long characterRevision, List<CreatureView> creatures,
      OnlineRegion region, List<NpcView> npcs, List<GroundLootView> loot, OnlineWindow window, Boolean truncated) {
    /** A snapshot that is complete for the whole region (no streaming window). */
    public Snapshot(long tick, List<Actor> players, Long characterRevision, List<CreatureView> creatures,
        OnlineRegion region, List<NpcView> npcs, List<GroundLootView> loot) {
      this(tick,players,characterRevision,creatures,region,npcs,loot,null,null);
    }
  }

  public record CorpseItem(java.util.UUID id, String key, int quantity, int charges) {}

  public record Corpse(java.util.UUID id, int world, double x, double y, int itemCount, long gold,
      List<CorpseItem> items) {
    public Corpse {
      items = items == null ? List.of() : List.copyOf(items);
    }
    public Corpse(java.util.UUID id, int world, double x, double y, int itemCount, long gold) {
      this(id, world, x, y, itemCount, gold, List.of());
    }
  }

  public record View(long ownId, MapInfo map, Snapshot snapshot, OnlineCharacterView character,
      List<Corpse> corpses) {}

  /** One bounded, server-authored remote chat message. */
  public record ChatMessage(long id, String speaker, String text) {}

  private record ChatPollReply(Long cursor, List<ChatMessage> messages) {}
  private record ChatSendReply(Long cursor) {}

  private record Joined(long id, OnlineRegion region) {}

  /** {@code args} fill a dialogue message's authored placeholders; refusals carry a key and no args. */
  public record CommandResult(boolean accepted, OnlineCharacterView character, String messageKey, List<Integer> args,
      ShopOffersReply shop) {
    public CommandResult {
      args = args == null ? List.of() : List.copyOf(args);
    }
    public CommandResult(boolean accepted, OnlineCharacterView character, String messageKey, List<Integer> args) {
      this(accepted, character, messageKey, args, null);
    }
    public CommandResult(boolean accepted, OnlineCharacterView character, String messageKey) {
      this(accepted, character, messageKey, List.of(), null);
    }
    public CommandResult(boolean accepted, OnlineCharacterView character) { this(accepted,character,null,List.of(),null); }
  }
  private record GameplayCommand(String path, String body, long revision,
      java.util.concurrent.CompletableFuture<CommandResult> result) {}
  /** Cast and use replies also say what the server changed; absent fields read as zero. */
  private record Committed(
      Long revision, int healthGained, int manaSpent, int manaGained, List<SpellHit> hits) {}
  /** One server-committed creature strike reported by a spell cast. */
  private record SpellHit(Integer damage) {}
  private record StatAllocationCommitted(Long revision, Integer allocatedPoints) {}
  /** A committed vendor trade: units moved and the gold paid or received, both the server's numbers. */
  private record ShopCommitted(Long revision, int quantity, long total) {}
  private static final class Rejected extends IllegalStateException {
    private final int code;
    private final String reason;
    Rejected(int code, String reason) { super("Server rejected request: " + code); this.code = code; this.reason = reason; }
  }
  private record ErrorBody(String error) {}

  /** Server cap for ground loot in one snapshot. */
  private static final int MAX_GROUND_LOOT = 128;
  private static final int MAX_CREATURES = 256;
  private static final Set<String> STAT_IDS =
      Set.of("strength", "endurance", "dexterity", "wisdom", "intelligence");
  private static final Gson JSON = new Gson();
  private final URI endpoint;
  private final int configuredSocketPort;
  private volatile GameSocketClient gameSocket;
  private final Credentials credentials;
  private final AtomicInteger direction = new AtomicInteger(4);
  private final AtomicInteger pendingTap = new AtomicInteger(-1);
  /** Client input frame that set the held direction (prediction); -1 for the text client. */
  private volatile long frameTag = -1;
  /** Bounded record of which client frame was current when each /input sequence was sent. */
  private final java.util.concurrent.ConcurrentSkipListMap<Long, Long> inputFrames =
      new java.util.concurrent.ConcurrentSkipListMap<>();
  private static final int MAX_INPUT_FRAMES = 512;
  private final java.util.concurrent.atomic.AtomicLong readyRegion = new java.util.concurrent.atomic.AtomicLong(-1);
  private volatile long acknowledgedRegion = -1;
  private final java.util.concurrent.atomic.AtomicReference<GameplayCommand> gameplay =
      new java.util.concurrent.atomic.AtomicReference<>();
  private final java.util.concurrent.ConcurrentLinkedQueue<String> outgoingChat =
      new java.util.concurrent.ConcurrentLinkedQueue<>();
  private final java.util.concurrent.ConcurrentLinkedQueue<ChatMessage> incomingChat =
      new java.util.concurrent.ConcurrentLinkedQueue<>();
  private final java.util.concurrent.atomic.AtomicInteger queuedChatCount =
      new java.util.concurrent.atomic.AtomicInteger();
  private final Thread worker;
  private volatile boolean closed;
  private volatile View view;
  private volatile String status = "connecting";
  private volatile String failureText;

  public OnlineConnection(URI endpoint) {
    this(endpoint, null);
  }

  public OnlineConnection(URI endpoint, Credentials credentials) {
    this(endpoint,credentials,0);
  }

  OnlineConnection(URI endpoint, Credentials credentials, int socketPort) {
    this.endpoint = OnlineEndpoint.validate(endpoint);
    if(socketPort<0 || socketPort>65535) throw new IllegalArgumentException("Invalid game socket port");
    configuredSocketPort=socketPort;
    this.credentials = credentials;
    worker = new Thread(this::run, "online-client-network");
    // Keep the process alive for the bounded logout request after the render window closes.
    worker.start();
  }

  public void direction(int dx, int dy) {
    if (dx < -1 || dx > 1 || dy < -1 || dy > 1)
      throw new IllegalArgumentException("Invalid direction");
    direction.set((dx + 1) * 3 + dy + 1);
  }

  /**
   * As {@link #direction(int, int)}, naming the client input frame (a monotonically increasing
   * number of the predicting client) that set it. Each {@code /input} sent afterwards remembers
   * the newest frame, so {@link #acknowledgedFrame} can translate the snapshot's last applied
   * input sequence back into a client frame for reconciliation. The protocol is unchanged.
   */
  public void direction(int dx, int dy, long frame) {
    direction(dx, dy);
    frameTag = frame;
  }

  /**
   * The newest client frame that had been sent when the server's input {@code sequence} was
   * sent, or -1 when none is known (nothing sent yet, or the entry aged out of the bounded record).
   */
  public long acknowledgedFrame(long sequence) {
    var entry = inputFrames.floorEntry(sequence);
    return entry == null ? -1 : entry.getValue();
  }

  /** Preserve a brief key press even if press/release both happen between rendered frames. */
  public void tap(int dx, int dy) {
    if (dx < -1 || dx > 1 || dy < -1 || dy > 1)
      throw new IllegalArgumentException("Invalid direction");
    pendingTap.set((dx + 1) * 3 + dy + 1);
  }

  public void stopInput() {
    direction.set(4);
    pendingTap.set(-1);
  }

  public View view() {
    return view;
  }

  /** The renderer acknowledges only after the matching map assets are installed. */
  public void confirmRegionReady(long locationEpoch) {
    var current=view;
    if (current!=null && current.map().locationEpoch()==locationEpoch && acknowledgedRegion!=locationEpoch)
      readyRegion.set(locationEpoch);
  }

  public String status() {
    return status;
  }

  /** Enqueues one remote chat line for the connection worker; never performs socket I/O on the render thread. */
  public boolean sendChat(String text) {
    if (text == null || text.isBlank() || text.codePointCount(0, text.length()) > MAX_CHAT_TEXT || closed
        || !"connected".equals(status)) return false;
    int queued = queuedChatCount.incrementAndGet();
    if (queued > MAX_QUEUED_CHAT_MESSAGES) {
      queuedChatCount.decrementAndGet();
      return false;
    }
    outgoingChat.add(text);
    return true;
  }

  /** Drains newly received server chat on the render thread. */
  public List<ChatMessage> drainChatMessages() {
    var messages = new java.util.ArrayList<ChatMessage>();
    ChatMessage message;
    while ((message = incomingChat.poll()) != null) messages.add(message);
    return List.copyOf(messages);
  }

  /**
   * Plain-English reason the connection stopped without ever entering the game (refused join,
   * incompatible version, unreachable server), or null. Never carries the key or raw exception text.
   */
  public String failureText() {
    return failureText;
  }

  /** Nonblocking, single-flight command. No local equipment prediction or automatic replay. */
  public java.util.concurrent.CompletionStage<CommandResult> equip(
      java.util.UUID instance, com.perso.T4C.player.BodyPart slot) {
    java.util.Objects.requireNonNull(instance);
    java.util.Objects.requireNonNull(slot);
    return gameplayCommand("/equip", instance + " " + slot.name());
  }

  public java.util.concurrent.CompletionStage<CommandResult> unequip(com.perso.T4C.player.BodyPart slot) {
    java.util.Objects.requireNonNull(slot);
    return gameplayCommand("/unequip", slot.name());
  }

  /** The client supplies only a target identity and committed revision, never damage or rewards. */
  public java.util.concurrent.CompletionStage<CommandResult> attack(long target) {
    if (target<=0) throw new IllegalArgumentException("Invalid target");
    return gameplayCommand("/attack",Long.toString(target));
  }

  /** Reference-only recovery; ownership, range and contents remain server decisions. */
  public java.util.concurrent.CompletionStage<CommandResult> recover(java.util.UUID corpse) {
    return gameplayCommand("/recover",java.util.Objects.requireNonNull(corpse).toString());
  }

  /** Recovers exactly one server-owned corpse stack; ownership, reach and capacity stay server-side. */
  public java.util.concurrent.CompletionStage<CommandResult> recover(java.util.UUID corpse, java.util.UUID item) {
    return gameplayCommand("/recover",java.util.Objects.requireNonNull(corpse) + " "
        + java.util.Objects.requireNonNull(item));
  }

  /**
   * Ground loot pickup by identity only. The server decides range, owner window, capacity and the
   * item instance; refusals return a readable {@link CommandResult#messageKey()}. Never replayed.
   */
  public java.util.concurrent.CompletionStage<CommandResult> pickUp(java.util.UUID loot) {
    return gameplayCommand("/pickup", java.util.Objects.requireNonNull(loot).toString());
  }

  /**
   * Self-cast of a learned spell by authored key. Mana, cooldown, exhaustion and effect are server
   * decisions; refusals carry a readable {@link CommandResult#messageKey()}. Never replayed.
   */
  public java.util.concurrent.CompletionStage<CommandResult> cast(String spellKey) {
    if (spellKey == null || !spellKey.matches("spell\\.[a-z0-9_]{1,64}"))
      throw new IllegalArgumentException("Invalid spell key");
    return gameplayCommand("/cast", spellKey);
  }

  /**
   * Casts a learned spell at a creature by id (target token {@code u:<id>}). Damage, mana,
   * exhaustion, kills and rewards are server decisions; refusals carry a readable message key. Never replayed.
   */
  public java.util.concurrent.CompletionStage<CommandResult> cast(String spellKey, long creature) {
    if (spellKey == null || !spellKey.matches("spell\\.[a-z0-9_]{1,64}"))
      throw new IllegalArgumentException("Invalid spell key");
    if (creature <= 0) throw new IllegalArgumentException("Invalid target");
    return gameplayCommand("/cast", spellKey + " u:" + creature);
  }

  /** Casts a learned spell at a region-local tile position; all legality checks stay server-side. */
  public java.util.concurrent.CompletionStage<CommandResult> castAtPosition(
      String spellKey, int tileX, int tileY) {
    if (spellKey == null || !spellKey.matches("spell\\.[a-z0-9_]{1,64}"))
      throw new IllegalArgumentException("Invalid spell key");
    if (Math.abs((long) tileX) > 999_999 || Math.abs((long) tileY) > 999_999)
      throw new IllegalArgumentException("Invalid target position");
    return gameplayCommand("/cast", spellKey + " p:" + tileX + "," + tileY);
  }

  /** Casts a learned single-target spell at another online actor by snapshot id. */
  public java.util.concurrent.CompletionStage<CommandResult> castToPlayer(String spellKey,long actor) {
    if (spellKey == null || !spellKey.matches("spell\\.[a-z0-9_]{1,64}"))
      throw new IllegalArgumentException("Invalid spell key");
    if(actor<=0) throw new IllegalArgumentException("Invalid target");
    return gameplayCommand("/cast",spellKey+" a:"+actor);
  }

  /** Uses one carried consumable instance by identity only; the server owns charges and effects. Never replayed. */
  public java.util.concurrent.CompletionStage<CommandResult> useItem(java.util.UUID item) {
    return gameplayCommand("/use", java.util.Objects.requireNonNull(item).toString());
  }

  /** Sends point counts only; the server applies them to its committed attributes and point balance. */
  public java.util.concurrent.CompletionStage<CommandResult> allocateStats(Map<String, Integer> allocation) {
    if (allocation == null || allocation.isEmpty() || allocation.size() > STAT_IDS.size())
      throw new IllegalArgumentException("Invalid stat allocation");
    var ordered = new TreeMap<String, Integer>();
    long total = 0;
    for (var entry : allocation.entrySet()) {
      if (entry.getKey() == null || !STAT_IDS.contains(entry.getKey())
          || entry.getValue() == null || entry.getValue() <= 0)
        throw new IllegalArgumentException("Invalid stat allocation");
      total += entry.getValue();
      if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid stat allocation");
      ordered.put(entry.getKey(), entry.getValue());
    }
    String encoded = ordered.entrySet().stream()
        .map(entry -> entry.getKey() + ":" + entry.getValue())
        .collect(java.util.stream.Collectors.joining(","));
    return gameplayCommand("/allocate-stats", encoded);
  }

  /** Requests the learned Sneak skill; concealment and cooldown are committed by the server. */
  public java.util.concurrent.CompletionStage<CommandResult> sneak() {
    return gameplayCommand("/sneak", "");
  }

  /** Authored identity only; the server resolves dialogue, proximity and any item grant. */
  public java.util.concurrent.CompletionStage<CommandResult> interact(String npc) {
    return interact(npc, java.util.OptionalInt.empty());
  }

  /** As {@link #interact(String)}, with an optional authored topic index (0-99) that the server validates. */
  public java.util.concurrent.CompletionStage<CommandResult> interact(String npc, java.util.OptionalInt topic) {
    if(npc==null || !npc.matches("[A-Za-z][A-Za-z0-9_]{0,63}"))
      throw new IllegalArgumentException("Invalid NPC identity");
    java.util.Objects.requireNonNull(topic);
    if(topic.isPresent() && (topic.getAsInt()<0 || topic.getAsInt()>99))
      throw new IllegalArgumentException("Invalid topic");
    return gameplayCommand("/interact", topic.isEmpty() ? npc : npc+" "+topic.getAsInt());
  }

  /**
   * The shopkeeper's listing: stock with the server's buy prices and the character's sellable goods
   * with the server's payouts. A read; the committed revision is unchanged. Never replayed.
   */
  public java.util.concurrent.CompletionStage<CommandResult> shopOffers(String npc) {
    return gameplayCommand("/shop-offers", requireNpc(npc));
  }

  /** Buys {@code quantity} units by item identity only; the server prices and checks gold and weight. */
  public java.util.concurrent.CompletionStage<CommandResult> shopBuy(String npc, String itemKey, int quantity) {
    return gameplayCommand("/shop-buy", requireNpc(npc) + " " + requireTrade(itemKey, quantity));
  }

  /** Sells {@code quantity} unequipped units by item identity only; the server pays its own price. */
  public java.util.concurrent.CompletionStage<CommandResult> shopSell(String npc, String itemKey, int quantity) {
    return gameplayCommand("/shop-sell", requireNpc(npc) + " " + requireTrade(itemKey, quantity));
  }

  private static String requireNpc(String npc) {
    if (npc == null || !npc.matches("[A-Za-z][A-Za-z0-9_]{0,63}")) throw new IllegalArgumentException("Invalid NPC identity");
    return npc;
  }

  private static String requireTrade(String itemKey, int quantity) {
    if (itemKey == null || !itemKey.matches("item\\.[a-z0-9_]{1,120}")) throw new IllegalArgumentException("Invalid item key");
    if (quantity < 1 || quantity > 99) throw new IllegalArgumentException("Invalid quantity");
    return itemKey + " " + quantity;
  }

  private java.util.concurrent.CompletionStage<CommandResult> gameplayCommand(String path, String arguments) {
    View current = view;
    if (closed || current == null || current.character() == null || !status.equals("connected"))
      throw new IllegalStateException("Persistent session is not connected");
    long revision = current.character().revision();
    String body=revision+" "+current.map().locationEpoch()+(arguments.isEmpty() ? "" : " "+arguments);
    var command = new GameplayCommand(path, body, revision,
        new java.util.concurrent.CompletableFuture<>());
    if (!gameplay.compareAndSet(null, command)) throw new IllegalStateException("Gameplay command pending");
    // Close/disconnect may race publication of the command after the initial state check.
    if (closed || !status.equals("connected")) failCommand();
    return command.result().minimalCompletionStage();
  }

  private void failCommand() {
    var command = gameplay.getAndSet(null);
    if (command != null) command.result().completeExceptionally(
        new IllegalStateException("Gameplay result unavailable; reconnect and reload committed state"));
  }

  private void run() {
    boolean sessionJoined = false;
    try {
      try {
        gameSocket = GameSocketClient.connect(endpoint,
            configuredSocketPort==0 ? GameSocketClient.defaultPort(endpoint) : configuredSocketPort);
        MapInfo map = JSON.fromJson(sendRetryingBusy("/map", null), MapInfo.class);
        if (map == null || map.protocolVersion() != OnlineProtocol.VERSION)
          throw new OnlineJoinFailure.Incompatible("Incompatible online protocol; update client and server together");
        validMapSize(map);
        if (map.persistent() != (credentials != null))
          throw new IllegalStateException("Profile and server mode do not match");
        if (map.gameSocketPort() != gameSocket.port())
          throw new IllegalStateException("Server socket endpoint does not match this connection");
        map.region();
        String login = credentials == null ? "" : credentials.secret() + " " + credentials.characterId();
        Joined joined = JSON.fromJson(sendRetryingBusy("/join", login), Joined.class);
        if (joined == null || joined.id() <= 0 || joined.region()==null)
          throw new IllegalStateException("Invalid server session");
        sessionJoined = true;
        map=readMap(map.persistent());
        if (!map.region().equals(joined.region())) throw new IllegalStateException("Joined map changed unexpectedly");
        long sequence = 0;
        long chatCursor = 0;
        OnlineRegion chatRegion = joined.region();
        long nextChatPollNanos = 0;
        long nextChatSendNanos = 0;
        inputFrames.clear();
        OnlineCharacterView character = null;
        List<Corpse> corpses = List.of();
        while (!closed) {
          long ready=readyRegion.getAndSet(-1);
          if (ready==map.locationEpoch() && acknowledgedRegion!=ready) {
            try { sendSession("POST","/region-ready",Long.toString(ready)); acknowledgedRegion=ready; }
            catch (Rejected rejected) { if (rejected.code!=409) throw rejected; }
          }
          var command = gameplay.get();
          boolean accepted = false;
          String messageKey=null;
          List<Integer> messageArgs=List.of();
          ShopOffersReply shop=null;
          long requiredRevision = character == null ? 0 : character.revision();
          if (command != null) {
            try {
              var reply=sendSession("POST",command.path(),command.body());
              if(command.path().equals("/interact")) {
                var dialogue=JSON.fromJson(reply,NpcInteractionReply.class);
                if(dialogue==null || dialogue.revision()<command.revision()
                    || dialogue.changed() && dialogue.revision()==command.revision())
                  throw new IllegalStateException("Invalid dialogue acknowledgement");
                requiredRevision=dialogue.revision();messageKey=dialogue.messageKey();messageArgs=dialogue.args();
              } else if(command.path().equals("/shop-offers")) {
                shop=JSON.fromJson(reply,ShopOffersReply.class);
                if(shop==null || shop.revision()<command.revision() || shop.npc()==null)
                  throw new IllegalStateException("Invalid shop listing");
                requiredRevision=shop.revision();
              } else if(command.path().equals("/shop-buy") || command.path().equals("/shop-sell")) {
                var trade=JSON.fromJson(reply,ShopCommitted.class);
                if(trade==null || trade.revision()==null || trade.revision()<=command.revision()
                    || trade.quantity()<1 || trade.total()<0)
                  throw new IllegalStateException("Invalid shop acknowledgement");
                requiredRevision=trade.revision();
                messageKey=command.path().equals("/shop-buy") ? "online.shop.bought" : "online.shop.sold";
                messageArgs=List.of(trade.quantity(),(int)Math.min(Integer.MAX_VALUE,trade.total()));
              } else if (command.path().equals("/allocate-stats")) {
                var allocation = JSON.fromJson(reply, StatAllocationCommitted.class);
                if (allocation == null || allocation.revision() == null
                    || allocation.revision() <= command.revision()
                    || allocation.allocatedPoints() == null || allocation.allocatedPoints() <= 0)
                  throw new IllegalStateException("Invalid stat allocation acknowledgement");
                requiredRevision = allocation.revision();
                messageKey = "online.stats.applied";
                messageArgs = List.of(allocation.allocatedPoints());
              } else {
                var committed = JSON.fromJson(reply, Committed.class);
                if (committed == null || committed.revision() == null || committed.revision() <= command.revision())
                  throw new IllegalStateException("Invalid gameplay acknowledgement");
                requiredRevision = committed.revision();
                if (command.path().equals("/cast") || command.path().equals("/use")) {
                  // Net mana: a potion restores it, a spell spends it; both are the server's numbers.
                  int netMana = committed.manaGained() - committed.manaSpent();
                  int damage = committedSpellDamage(committed.hits());
                  if (command.path().equals("/cast") && damage > 0) {
                    messageKey = "online.ability.damage_result";
                    messageArgs = List.of(damage, committed.healthGained(), netMana);
                  } else {
                    messageKey = "online.ability.result";
                    messageArgs = List.of(committed.healthGained(), netMana);
                  }
                }
              }
              accepted = true;
            } catch (Rejected rejected) {
              if (rejected.code != 409) throw rejected;
              // A normal conflict leaves the session usable. Refresh, but never replay stale intent.
              messageKey = command.path().equals("/allocate-stats")
                  ? statAllocationRefusal(rejected.reason)
                  : OnlineCommandRefusals.messageKey(command.path(), rejected.reason);
              messageArgs = List.of();
            }
          }
          int intent = direction.get();
          int tap = pendingTap.getAndSet(-1);
          if (intent == 4 && tap >= 0) intent = tap;
          if (acknowledgedRegion!=map.locationEpoch()) intent=4;
          inputFrames.put(sequence, frameTag);
          if (inputFrames.size() > MAX_INPUT_FRAMES) inputFrames.pollFirstEntry();
          try { sendSession("POST","/input",map.locationEpoch()+" "+sequence++ + " " + (intent / 3 - 1) + " " + (intent % 3 - 1)); }
          catch (Rejected rejected) { if (rejected.code!=409) throw rejected; }
          int regionRetries=0;
          while (true) {
          Snapshot snapshot = JSON.fromJson(sendSession("GET","/state",null), Snapshot.class);
          if (snapshot==null || snapshot.region()==null) throw new IllegalStateException("Missing region state");
          if (!map.region().equals(snapshot.region())) {
            stopInput(); acknowledgedRegion=-1; readyRegion.set(-1);
            map=readMap(map.persistent());
            snapshot=JSON.fromJson(sendSession("GET","/state",null),Snapshot.class);
            if (snapshot==null || !map.region().equals(snapshot.region()))
              throw new IllegalStateException("Region state did not stabilize");
          }
          if (snapshot.players() == null || snapshot.players().size() > 20)
            throw new IllegalStateException("Invalid server snapshot");
          validWindow(map, snapshot);
          var window = snapshot.window();
          boolean hasSelf = false;
          for (Actor actor : snapshot.players()) {
            if (actor == null
                || actor.id() <= 0
                || !Double.isFinite(actor.x())
                || !Double.isFinite(actor.y())
                || actor.x() < 0
                || actor.y() < 0
                || actor.maxHp() <= 0 || actor.hp() < 0 || actor.hp() > actor.maxHp()
                || !actor.lookValid() || !actor.glideValid(map)
                || actor.x() >= map.width()
                || actor.y() >= map.height()
                || outsideWindow(window, actor.x(), actor.y()))
              throw new IllegalStateException("Invalid server position");
            hasSelf |= actor.id() == joined.id();
          }
          if (!hasSelf) throw new IllegalStateException("Session missing from snapshot");
          var creatures = snapshot.creatures() == null ? List.<CreatureView>of() : snapshot.creatures();
          if (creatures.size() > 256) throw new IllegalStateException("Invalid creature count");
          var creatureIds = new java.util.HashSet<Long>();
          for (var creature : creatures) {
            if (creature == null || creature.x() >= map.width() || creature.y() >= map.height()
                || outsideWindow(window, creature.x(), creature.y())
                || !creatureIds.add(creature.id()))
              throw new IllegalStateException("Invalid creature snapshot");
          }
          var npcs=snapshot.npcs()==null ? List.<NpcView>of() : snapshot.npcs();
          if(npcs.size()>64) throw new IllegalStateException("Invalid NPC count");
          var npcIds=new java.util.HashSet<String>();
          for(var npc:npcs)
            if(npc==null || npc.x()>=map.width() || npc.y()>=map.height()
                || outsideWindow(window,npc.x(),npc.y()) || !npcIds.add(npc.id()))
              throw new IllegalStateException("Invalid NPC snapshot");
          var loot = snapshot.loot()==null ? List.<GroundLootView>of() : snapshot.loot();
          if (loot.size() > MAX_GROUND_LOOT) throw new IllegalStateException("Invalid loot count");
          var lootIds = new java.util.HashSet<java.util.UUID>();
          for (var drop : loot)
            if (drop == null || drop.x() < 0 || drop.y() < 0 || drop.x() >= map.width() || drop.y() >= map.height()
                || outsideWindow(window, drop.x(), drop.y()) || !lootIds.add(drop.id()))
              throw new IllegalStateException("Invalid loot snapshot");
          if (map.persistent()) {
            if (snapshot.characterRevision() == null || snapshot.characterRevision() < 0)
              throw new IllegalStateException("Missing committed character revision");
            if (command != null || character == null || character.revision() < snapshot.characterRevision()
                || character.locationEpoch()!=map.locationEpoch()) {
              OnlineCharacterView updated =
                  JSON.fromJson(sendSession("GET","/character",null), OnlineCharacterView.class);
              if (updated == null || !credentials.characterId().equals(updated.id())
                  || updated.revision() < Math.max(requiredRevision, snapshot.characterRevision()))
                throw new IllegalStateException("Invalid private character state");
              if (command != null && command.path().equals("/allocate-stats") && updated.progress() == null)
                throw new IllegalStateException("Stat allocation response omitted authoritative progress");
              character = updated;
              var found = JSON.fromJson(sendSession("GET","/corpses",null),Corpse[].class);
              if (found==null || found.length>256) throw new IllegalStateException("Invalid corpse list");
              var identities=new java.util.HashSet<java.util.UUID>();
              int totalCorpseItems = 0;
              for (var corpse:found) {
                if (corpse==null || corpse.id()==null || !identities.add(corpse.id()) || corpse.world()<0
                    || !Double.isFinite(corpse.x()) || !Double.isFinite(corpse.y()) || corpse.x()<0 || corpse.y()<0
                    || corpse.itemCount()<0 || corpse.gold()<0)
                  throw new IllegalStateException("Invalid corpse state");
                if (corpse.items().size() != corpse.itemCount()
                    || corpse.items().size() > MAX_CORPSE_ITEMS_PER_CORPSE)
                  throw new IllegalStateException("Invalid corpse items");
                totalCorpseItems += corpse.items().size();
                if (totalCorpseItems > MAX_CORPSE_ITEM_DETAILS)
                  throw new IllegalStateException("Too many corpse items");
                var itemIds = new java.util.HashSet<java.util.UUID>();
                for (var item : corpse.items())
                  if (item == null || item.id() == null || !itemIds.add(item.id()) || item.key() == null
                      || item.key().isBlank() || item.key().length() > 256 || item.quantity() <= 0 || item.charges() < 0)
                    throw new IllegalStateException("Invalid corpse item");
              }
              corpses=List.of(found);
            }
            if (character.locationEpoch()!=map.locationEpoch()) {
              stopInput(); acknowledgedRegion=-1; readyRegion.set(-1);
              map=readMap(map.persistent()); character=null; corpses=List.of();
              if (++regionRetries>=3) throw new IllegalStateException("Private region state did not stabilize");
              continue; // Refresh reads only. Never replay the gameplay mutation already sent above.
            }
          }
          synchronized (this) {
            if (closed) break;
            view =
              new View(
                  joined.id(), map,
                  new Snapshot(snapshot.tick(), List.copyOf(snapshot.players()), snapshot.characterRevision(), List.copyOf(creatures),snapshot.region(),List.copyOf(npcs),List.copyOf(loot),window,snapshot.truncated()),
                  character,corpses);
            status = "connected";
          }
          break;
          }
          long now = System.nanoTime();
          if (!chatRegion.equals(map.region())) {
            chatRegion = map.region();
            chatCursor = 0;
            nextChatPollNanos = 0;
          }
          if (now >= nextChatSendNanos) {
            String chatText = outgoingChat.poll();
            if (chatText != null) {
              queuedChatCount.decrementAndGet();
              try {
                String sent = sendSession("POST", "/chat", "send " + chatText);
                var ack = JSON.fromJson(sent, ChatSendReply.class);
                if (ack == null || ack.cursor() == null || ack.cursor() <= 0)
                  throw new IllegalStateException("Invalid chat acknowledgement");
              } catch (Rejected refused) {
                // Chat is best-effort; a sender-specific refusal must not stop the game session.
              }
              nextChatSendNanos = now + CHAT_SEND_INTERVAL_NANOS;
            }
          }
          if (now >= nextChatPollNanos) {
            var polled = JSON.fromJson(sendRetryingBusy("/chat", "poll " + chatCursor), ChatPollReply.class);
            if (polled == null || polled.cursor() == null || polled.cursor() < chatCursor
                || polled.messages() == null || polled.messages().size() > MAX_CHAT_MESSAGES_PER_POLL)
              throw new IllegalStateException("Invalid chat poll response");
            long previousId = chatCursor;
            for (var message : polled.messages()) {
              if (message == null || message.id() <= previousId || message.id() > polled.cursor()
                  || message.speaker() == null || message.speaker().isBlank() || message.speaker().length() > 64
                  || message.text() == null || message.text().isBlank()
                  || message.text().codePointCount(0, message.text().length()) > MAX_CHAT_TEXT)
                throw new IllegalStateException("Invalid chat message");
              previousId = message.id();
            }
            if (polled.cursor() != (polled.messages().isEmpty() ? chatCursor : previousId))
              throw new IllegalStateException("Invalid chat cursor");
            if (!polled.messages().isEmpty()) incomingChat.addAll(polled.messages());
            chatCursor = polled.cursor();
            nextChatPollNanos = now + CHAT_POLL_INTERVAL_NANOS;
          }
          if (command != null && gameplay.compareAndSet(command, null))
            command.result().complete(new CommandResult(accepted, character,messageKey,messageArgs,shop));
          Thread.sleep(75);
        }
      } catch (Exception error) {
        // Recorded before the status flips, so the screen that sees "disconnected" can already read why.
        if (!closed && !sessionJoined)
          failureText = error instanceof Rejected rejected
              ? OnlineJoinFailure.forRefusal(rejected.code, rejected.reason) : OnlineJoinFailure.forFailure(error);
        throw error;
      } finally {
        status = "disconnected";
        view = null;
        failCommand();
        // Cleanup stays off the render thread. The server expires the session if it is unavailable.
        Thread.interrupted();
        if (sessionJoined) {
          try {
            sendSession("POST","/leave","");
          } catch (Exception ignored) {
          }
        }
        GameSocketClient socket=gameSocket;
        gameSocket=null;
        if (socket!=null) try { socket.close(); } catch (Exception ignored) { }
      }
    } catch (Exception error) {
      if (!closed) System.err.println("Online connection stopped: " + error.getMessage());
      status = "disconnected";
    } finally {
      if (!closed) status = "disconnected";
      view = null;
      failCommand();
    }
  }

  private static int committedSpellDamage(List<SpellHit> hits) {
    if (hits == null || hits.isEmpty()) return 0;
    if (hits.size() > MAX_CREATURES) throw new IllegalStateException("Invalid spell hit count");
    long total = 0;
    for (SpellHit hit : hits) {
      if (hit == null || hit.damage() == null || hit.damage() < 0)
        throw new IllegalStateException("Invalid spell hit");
      total += hit.damage();
    }
    return (int) Math.min(Integer.MAX_VALUE, total);
  }

  private static String statAllocationRefusal(String reason) {
    return switch (reason == null ? "" : reason) {
      case "busy", "revision", "region", "uninitialized_character" -> "online.stats.busy";
      case "insufficient_stat_points" -> "online.stats.no_points";
      default -> "online.stats.refused";
    };
  }

  private MapInfo readMap(boolean persistent) throws Exception {
    var loaded=JSON.fromJson(sendSession("GET","/map",null),MapInfo.class);
    if (loaded==null || loaded.protocolVersion()!=OnlineProtocol.VERSION || loaded.persistent()!=persistent)
      throw new IllegalStateException("Invalid region map");
    validMapSize(loaded);
    loaded.region(); // Validate the bounded descriptor before publishing actor coordinates.
    return loaded;
  }

  /** Largest edge of a map that is not streamed; a streamed map is bounded by {@link OnlineRegion#MAX_DIMENSION}. */
  static final int MAX_PLAIN_MAP_EDGE = 1024;

  private static void validMapSize(MapInfo map) {
    int limit = map.streamed() ? OnlineRegion.MAX_DIMENSION : MAX_PLAIN_MAP_EDGE;
    if (map.width() <= 0 || map.height() <= 0 || map.width() > limit || map.height() > limit)
      throw new IllegalStateException("Invalid server map");
  }

  /** A window is only valid on a streamed map, inside the region, and every listed thing must lie in it. */
  private static void validWindow(MapInfo map, Snapshot snapshot) {
    var window = snapshot.window();
    if (window == null) {
      if (map.streamed()) throw new IllegalStateException("Streamed map snapshot has no window");
      if (snapshot.truncated() != null) throw new IllegalStateException("Unexpected truncated flag");
      return;
    }
    if (!map.streamed() || !window.fitsIn(map.region())) throw new IllegalStateException("Invalid snapshot window");
  }

  private static boolean outsideWindow(OnlineWindow window, double x, double y) {
    return window != null && !window.contains(x, y);
  }

  /**
   * Idempotent start-up calls only: the map read and the join. The server answers a full public gate
   * with 503 "busy" before it touches the world, and a join that times out inside the world is
   * cancelled atomically, so a busy refusal never means the call was applied. Retry that refusal a
   * bounded number of times with jittered exponential backoff. Every other status, timeout or
   * transport failure is thrown at once, and no gameplay command or poll is ever retried here.
   */
  private String sendRetryingBusy(String path, String body) throws Exception {
    for (int retry = 0; ; retry++) {
      try {
        return sendSession(body == null ? "GET" : "POST",path,body);
      } catch (Rejected rejected) {
        if (!OnlineBusyBackoff.isBusy(rejected.code, rejected.reason) || retry >= OnlineBusyBackoff.MAX_RETRIES
            || closed)
          throw rejected;
        Thread.sleep(OnlineBusyBackoff.delayMillis(retry,
            java.util.concurrent.ThreadLocalRandom.current().nextDouble()));
        if (closed) throw rejected;
      }
    }
  }

  private String sendSession(String method,String path,String body) throws Exception {
    GameSocketClient socket=gameSocket;
    if(socket==null) throw new IllegalStateException("Game socket session is not connected");
    var reply=socket.request(method,path.startsWith("/") ? path.substring(1) : path,body);
    if(reply.status()!=200)
      throw new Rejected(reply.status(),refusalReason(reply.body().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    return reply.body();
  }

  private static String refusalReason(byte[] body) {
    try {
      var error = JSON.fromJson(new String(body, java.nio.charset.StandardCharsets.UTF_8), ErrorBody.class);
      return error == null ? null : error.error();
    } catch (RuntimeException malformed) {
      return null;
    }
  }

  @Override
  public synchronized void close() {
    closed = true;
    stopInput();
    status = "disconnected";
    view = null;
    failCommand();
    // Requests already have a whole-transfer deadline. Do not interrupt a logout that
    // may be running after a failed command; let the worker finish its bounded cleanup.
  }
}
