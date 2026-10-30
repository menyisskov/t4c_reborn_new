package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.ShopOffersReply;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.world.MovementReconciler;
import com.perso.T4C.world.MovementRules;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

/**
 * Online commands: the player's intents become requests over the persistent connection, and the
 * server decides. Nothing here changes the game locally except the player's own steps, which are
 * predicted with the shared movement rule and reconciled with the server's snapshots.
 *
 * <p><b>Attack, pick up, interact</b> send the target's server identity only and apply nothing:
 * no target flag, no swing, no cooldown, no inventory change. Range, cooldown, damage and
 * ownership are the server's. The reply arrives on the network thread and is queued; the next
 * {@link #move} (the screen calls it every frame) or {@link #pump()} shows its message through the
 * host, the way single-player shows its own failure text. A routine swing-cooldown refusal stays
 * silent, as the text client already does.
 *
 * <p><b>Equipment</b> sends the committed item identity and requested body slot, or the body slot to
 * clear. The server validates both operations; this client never predicts the resulting inventory
 * or bonuses locally.
 *
 * <p><b>Movement</b> follows CLIENT_UNIFICATION_DESIGN 3.10 and 3.4:
 *
 * <ol>
 *   <li>The local {@link Player} moves at once through {@code Player.move} (the single-player
 *       rule, speed buffs and gates included), so the character never waits for the network.
 *   <li>The held direction (the sign of the input, or the direction actually walked when only a
 *       click path is moving; none while stunned, exhausted or mid-swing) is handed to the
 *       connection tagged with a frame number; the connection sends it with its own input
 *       sequence and remembers which frame was current.
 *   <li>Each new snapshot carries the last input sequence the server applied. It is translated
 *       back to a frame, the acknowledged frames are dropped, and the unacknowledged ones are
 *       replayed on the server position with {@link RemoteMovementRule} ({@link
 *       MovementReconciler}).
 *   <li><b>Tolerance.</b> The replayed position is where the server will be once it has our newer
 *       inputs. An error under {@value #EASE_FLOOR_TILES} tile between it and the player is
 *       ignored: the replay also counts the time the server already spent on the last input, so a
 *       small steady error of roughly speed times snapshot age (9.4 tiles per second) is normal
 *       and correcting it would only jitter. From {@value #EASE_FLOOR_TILES} up to {@value
 *       MovementReconciler#SNAP_DISTANCE_TILES} tile the player is eased over {@value
 *       #EASE_SECONDS} s; at one tile or more (a wall the server saw, a teleport, a stop that
 *       crossed a tile boundary on the server first, a long stall) it snaps. A new region epoch,
 *       or an acknowledged history that grows past {@value #MAX_PENDING_FRAMES} frames, restarts
 *       the replay from the server.
 * </ol>
 *
 * Bound by the screen once, like {@link LocalPlayerCommands#bind}; used before that it fails loudly.
 */
public final class RemotePlayerCommands implements PlayerCommands, RemoteIntents {
  /** What the screen provides: the player to predict and a place to show a message. */
  public interface Host {
    Player player();

    void showMessage(String text);

    /** Called by {@link #pump()} on the render thread when the server returns a shop listing. */
    default void showShop(String npc, ShopOffersReply offers) {}

    /** Called by {@link #pump()} after a greeting that opened no shop: offer the NPC's topics. */
    default void showTalk(String npc) {}

    /** Opens the read-only item selector for one owner-listed corpse. */
    default void showCorpse(UUID corpse) {}

    /** Adds one server-authored remote player chat line to the existing chat UI. */
    default void showChat(String speaker, String text) {}
  }

  /** The swing-cooldown refusal is routine and never shown (same key as the text client). */
  public static final String SILENT_COOLDOWN_KEY = "online.attack.cooldown";

  /** Errors below this many tiles are not corrected. */
  public static final double EASE_FLOOR_TILES = 0.5;

  /** Errors from the floor up to a snap are eased in over this long. */
  public static final float EASE_SECONDS = 0.1f;

  /** Unacknowledged frames kept before the replay restarts from the server (a dead link). */
  public static final int MAX_PENDING_FRAMES = 600;

  private record Outcome(
      String key,
      List<Integer> args,
      String shopNpc,
      ShopOffersReply shop,
      long shopGeneration,
      String talkNpc,
      Consumer<OnlineConnection.CommandResult> completion,
      OnlineConnection.CommandResult commandResult) {
    static Outcome message(String key, List<Integer> args) {
      return new Outcome(key, args, null, null, -1, null, null, null);
    }

    static Outcome shop(String npc, ShopOffersReply listing, long generation) {
      return new Outcome(null, List.of(), npc, listing, generation, null, null, null);
    }

    static Outcome talk(String npc) {
      return new Outcome(null, List.of(), null, null, -1, npc, null, null);
    }

    static Outcome completion(
        String key, List<Integer> args, Consumer<OnlineConnection.CommandResult> callback,
        OnlineConnection.CommandResult result) {
      return new Outcome(key, args, null, null, -1, null, callback, result);
    }
  }

  private final RemoteLink link;
  private final ToLongFunction<BaseMonster> creatureIds;
  private final RemoteMovementRule rule;
  private final Queue<Outcome> outcomes = new ConcurrentLinkedQueue<>();
  private final AtomicLong shopGeneration = new AtomicLong();
  private Host host;

  private MovementReconciler reconciler;
  private long lastTick = Long.MIN_VALUE;
  private long lastEpoch = Long.MIN_VALUE;
  private float easeX;
  private float easeY;

  /** Commands for the desktop: the real collision singleton answers the replay's terrain. */
  public RemotePlayerCommands(RemoteLink link, ToLongFunction<BaseMonster> creatureIds) {
    this(
        link,
        creatureIds,
        RemoteMovementRule.desktopTerrain(),
        () -> {
          var manager = com.perso.T4C.helper.CollisionManager.getInstance();
          return manager.isInitialized();
        });
  }

  /**
   * @param creatureIds the server creature id of a puppet monster, or a value below 1 for none
   * @param terrain what the replay asks of the world
   * @param collisions whether the replay checks collisions (also needs the player's own flag)
   */
  public RemotePlayerCommands(
      RemoteLink link,
      ToLongFunction<BaseMonster> creatureIds,
      MovementRules.Terrain terrain,
      BooleanSupplier collisions) {
    this.link = link;
    this.creatureIds = creatureIds;
    Supplier<Player> player = () -> host == null ? null : host.player();
    this.rule =
        new RemoteMovementRule(
            terrain,
            () -> {
              Player p = player.get();
              return (p == null || p.isPlayerCollisionsEnabled()) && collisions.getAsBoolean();
            },
            player);
  }

  /** Connects the commands to the screen; done once at screen build. */
  public void bind(Host host) {
    this.host = host;
  }

  // --- intents that change nothing locally ---

  @Override
  public void attack(BaseMonster target) {
    if (target == null) return;
    long id = creatureIds.applyAsLong(target);
    if (id <= 0) return;
    sendRetrying(() -> link.attack(id), "online.attack.busy", "online.attack.busy", true);
  }

  /** Sends one weapon intent and exposes its authoritative reply for bounded screen repetition. */
  public CompletionStage<OnlineConnection.CommandResult> attackWithResult(BaseMonster target) {
    if (target == null) return CompletableFuture.completedFuture(null);
    long id = creatureIds.applyAsLong(target);
    if (id <= 0) return CompletableFuture.completedFuture(null);
    try {
      CompletionStage<OnlineConnection.CommandResult> result = attackWithRetry(id, true);
      result.whenComplete(
          (reply, failure) ->
              outcomes.add(
                  Outcome.message(
                      failure != null || reply == null ? "online.attack.busy" : reply.messageKey(),
                      failure != null || reply == null ? List.of() : reply.args())));
      return result;
    } catch (IllegalStateException pendingOrOffline) {
      outcomes.add(Outcome.message("online.attack.busy", List.of()));
      return CompletableFuture.failedFuture(pendingOrOffline);
    } catch (IllegalArgumentException invalidIdentity) {
      return CompletableFuture.failedFuture(invalidIdentity);
    }
  }

  private CompletionStage<OnlineConnection.CommandResult> attackWithRetry(
      long creatureId, boolean mayRetry) {
    try {
      return link.attack(creatureId)
          .thenCompose(
              reply -> {
                if (mayRetry
                    && reply != null
                    && !reply.accepted()
                    && "online.attack.busy".equals(reply.messageKey())) {
                  return attackWithRetry(creatureId, false);
                }
                return CompletableFuture.completedFuture(reply);
              });
    } catch (IllegalStateException | IllegalArgumentException unavailable) {
      return CompletableFuture.failedFuture(unavailable);
    }
  }

  @Override
  public boolean pickUp(UUID loot) {
    if (loot == null) return false;
    return send(() -> link.pickUp(loot), "online.loot.refused");
  }

  /** Message shown when the server accepted the recovery (the belongings arrive with the next state). */
  static final String RECOVERED_KEY = "online.recover.done";

  @Override
  public boolean recover(UUID corpse) {
    if (corpse == null) return false;
    if (host == null) return false;
    host.showCorpse(corpse);
    return true;
  }

  /** Sends only the selected stack identity; a null item is used for a gold-only corpse. */
  @Override
  public boolean recover(UUID corpse, UUID item) {
    if (corpse == null) return false;
    try {
      link.recover(corpse, item)
          .whenComplete(
              (result, failure) ->
                  outcomes.add(
                      failure != null || result == null
                          ? Outcome.message("online.recover.refused", List.of())
                          : Outcome.message(
                              result.accepted() && result.messageKey() == null
                                  ? RECOVERED_KEY
                                  : result.messageKey(),
                              result.args())));
      return true;
    } catch (IllegalStateException pendingOrOffline) {
      outcomes.add(Outcome.message("online.recover.busy", List.of()));
      return true;
    } catch (IllegalArgumentException invalidIdentity) {
      return false;
    }
  }

  @Override
  public boolean interact(String npc) {
    if (npc == null) return false;
    long generation = shopGeneration.incrementAndGet();
    try {
      sendInteract(npc, generation, true);
      return true;
    } catch (IllegalStateException pendingOrOffline) {
      outcomes.add(Outcome.message("online.npc.refused", List.of()));
      return true;
    } catch (IllegalArgumentException invalidIdentity) {
      return false;
    }
  }

  /** The server's "busy / revision changed" refusal of a greeting (see {@code OnlineCommandRefusals}). */
  static final String NPC_BUSY_KEY = "online.npc.busy";

  /**
   * One greeting. A greeting refused as busy or stale was rejected before any change, and the connection has already
   * refreshed the committed character when that answer arrives (a position or regeneration checkpoint had just moved
   * the revision: the live runs of T4C-0312 saw it on the first click on a shopkeeper after walking up). So it is sent
   * once more with the fresh revision, which is the click the player would otherwise have to repeat; a second refusal
   * is shown. Nothing that was accepted is ever sent again.
   */
  private void sendInteract(String npc, long generation, boolean mayRetry) {
    link.interact(npc)
        .whenComplete(
            (result, failure) -> {
              if (failure != null || result == null) {
                outcomes.add(Outcome.message("online.npc.refused", List.of()));
              } else if (result.accepted()) {
                // The socket is single-flight. Queue offers only after interaction has completed.
                requestShopAfter(npc, result, generation);
              } else if (mayRetry
                  && NPC_BUSY_KEY.equals(result.messageKey())
                  && shopGeneration.get() == generation) {
                try {
                  sendInteract(npc, generation, false);
                } catch (IllegalStateException | IllegalArgumentException unavailable) {
                  outcomes.add(Outcome.message(result.messageKey(), result.args()));
                }
              } else {
                outcomes.add(Outcome.message(result.messageKey(), result.args()));
              }
            });
  }

  @Override
  public boolean interact(String npc, int topic) {
    if (npc == null || topic < 0 || topic > 99) return false;
    try {
      sendTopic(npc, topic, true);
      return true;
    } catch (IllegalStateException pendingOrOffline) {
      outcomes.add(Outcome.message("online.npc.refused", List.of()));
      return true;
    } catch (IllegalArgumentException invalidIdentity) {
      return false;
    }
  }

  /**
   * One topic question. Like a greeting, a topic refused as busy or stale was rejected before any change (T4C-0362: a live
   * rendered run lost the second window's quest acceptance to a revision moved by a checkpoint), so it is sent once more
   * with the refreshed revision; a second refusal is shown, and nothing accepted is ever sent again.
   */
  private void sendTopic(String npc, int topic, boolean mayRetry) {
    link.interact(npc, topic)
        .whenComplete(
            (result, failure) -> {
              if (failure != null || result == null) {
                outcomes.add(Outcome.message("online.npc.refused", List.of()));
              } else if (!result.accepted() && mayRetry && NPC_BUSY_KEY.equals(result.messageKey())) {
                try {
                  sendTopic(npc, topic, false);
                } catch (IllegalStateException | IllegalArgumentException unavailable) {
                  outcomes.add(Outcome.message(result.messageKey(), result.args()));
                }
              } else {
                outcomes.add(Outcome.message(result.messageKey(), result.args()));
              }
            });
  }

  /** The server's "busy / revision changed" refusal of an equipment change (see {@code OnlineCommandRefusals}). */
  static final String EQUIP_BUSY_KEY = "online.equip.busy";

  @Override
  public boolean equip(UUID instance, BodyPart slot) {
    if (instance == null || slot == null) return false;
    return sendRetrying(() -> link.equip(instance, slot), EQUIP_BUSY_KEY, "online.equip.refused", true);
  }

  @Override
  public boolean unequip(BodyPart slot) {
    if (slot == null) return false;
    return sendRetrying(() -> link.unequip(slot), EQUIP_BUSY_KEY, "online.equip.refused", true);
  }

  /**
   * One command that the server may refuse as busy or stale. Such a refusal changed nothing (a regeneration or position
   * checkpoint had just moved the committed revision: the T4C-0316 live run lost the second window's first unequip to it,
   * with only the generic refusal text, and about one attack click in five was answered "busy"), and the connection has
   * refreshed the character by the time that answer arrives, so the command is sent once more; a second refusal is shown.
   * A command that was accepted, or refused for any other reason (requirements, conflict, cooldown), is never sent again.
   */
  private boolean sendRetrying(
      Supplier<CompletionStage<OnlineConnection.CommandResult>> command,
      String busyKey,
      String failureKey,
      boolean mayRetry) {
    return sendRetrying(command, busyKey, failureKey, mayRetry, null);
  }

  private boolean sendRetrying(
      Supplier<CompletionStage<OnlineConnection.CommandResult>> command,
      String busyKey,
      String failureKey,
      boolean mayRetry,
      Consumer<OnlineConnection.CommandResult> completion) {
    try {
      command
          .get()
          .whenComplete(
              (result, failure) -> {
                if (failure != null || result == null) {
                  var failed = new OnlineConnection.CommandResult(false, null, failureKey);
                  outcomes.add(Outcome.completion(failureKey, List.of(), completion, failed));
                  return;
                }
                if (!result.accepted() && mayRetry && busyKey.equals(result.messageKey())) {
                  boolean retried = false;
                  try {
                    retried = sendRetrying(command, busyKey, failureKey, false, completion);
                  } catch (IllegalStateException | IllegalArgumentException unavailable) {
                    // fall through: show the refusal
                  }
                  if (retried) return;
                }
                outcomes.add(
                    Outcome.completion(result.messageKey(), result.args(), completion, result));
              });
      return true;
    } catch (IllegalStateException pendingOrOffline) {
      var failed = new OnlineConnection.CommandResult(false, null, failureKey);
      outcomes.add(Outcome.completion(failureKey, List.of(), completion, failed));
      return true;
    } catch (IllegalArgumentException invalidIdentity) {
      return false;
    }
  }

  @Override
  public boolean shopOffers(String npc) {
    if (npc == null) return false;
    return requestOffers(npc, "online.shop.busy", false, shopGeneration.get());
  }

  @Override
  public boolean shopBuy(String npc, String itemKey, int quantity) {
    return trade(npc, itemKey, quantity, true);
  }

  @Override
  public boolean shopSell(String npc, String itemKey, int quantity) {
    return trade(npc, itemKey, quantity, false);
  }

  /**
   * The server's "busy / revision changed" refusal of a cast or a potion. A live two-window run (T4C-0331) had a Protection cast
   * answered "busy" right after a Light Heal, because a regeneration checkpoint had just moved the committed revision; the refusal
   * changed nothing, so it is sent once more like an attack, a greeting or an equipment change.
   */
  static final String ABILITY_BUSY_KEY = "online.ability.busy";

  @Override
  public boolean cast(String spellKey) {
    if (spellKey == null) return false;
    return sendRetrying(() -> link.cast(spellKey), ABILITY_BUSY_KEY, ABILITY_BUSY_KEY, true);
  }

  @Override
  public boolean cast(String spellKey, BaseMonster target) {
    if (spellKey == null || target == null || target.isDead()) return false;
    long id = creatureIds.applyAsLong(target);
    if (id <= 0) return false;
    OnlineConnection.View view = link.view();
    if (view == null || view.snapshot() == null || view.snapshot().creatures() == null) return false;
    boolean liveInSnapshot =
        view.snapshot().creatures().stream().anyMatch(creature -> creature.id() == id && creature.health() > 0);
    if (!liveInSnapshot) return false;
    return sendRetrying(() -> link.cast(spellKey, id), ABILITY_BUSY_KEY, ABILITY_BUSY_KEY, true);
  }

  /** Cast a hostile spell and report its final server reply on the next render-thread pump. */
  public boolean castWithResult(
      String spellKey, BaseMonster target, Consumer<OnlineConnection.CommandResult> completion) {
    if (spellKey == null || target == null || target.isDead() || completion == null) return false;
    long id = creatureIds.applyAsLong(target);
    if (id <= 0) return false;
    OnlineConnection.View view = link.view();
    if (view == null || view.snapshot() == null || view.snapshot().creatures() == null) return false;
    boolean liveInSnapshot =
        view.snapshot().creatures().stream()
            .anyMatch(creature -> creature.id() == id && creature.health() > 0);
    if (!liveInSnapshot) return false;
    return sendRetrying(
        () -> link.cast(spellKey, id), ABILITY_BUSY_KEY, ABILITY_BUSY_KEY, true, completion);
  }

  @Override
  public boolean castAtPosition(String spellKey, int tileX, int tileY) {
    if (spellKey == null) return false;
    return sendRetrying(
        () -> link.castAtPosition(spellKey, tileX, tileY),
        ABILITY_BUSY_KEY,
        ABILITY_BUSY_KEY,
        true);
  }

  @Override
  public boolean castToPlayer(String spellKey,long actor) {
    if(spellKey==null || actor<=0) return false;
    OnlineConnection.View view=link.view();
    if(view==null || view.snapshot()==null || view.snapshot().players()==null
        || actor==view.ownId() || view.snapshot().players().stream()
            .noneMatch(player -> player.id()==actor && player.hp()>0)) return false;
    return sendRetrying(() -> link.castToPlayer(spellKey,actor),ABILITY_BUSY_KEY,ABILITY_BUSY_KEY,true);
  }

  @Override
  public boolean useItem(UUID itemInstance) {
    if (itemInstance == null) return false;
    return sendRetrying(() -> link.useItem(itemInstance), ABILITY_BUSY_KEY, ABILITY_BUSY_KEY, true);
  }

  @Override public boolean sneak() {
    if(link.view()==null) return false;
    try {
      link.sneak().whenComplete((result,failure) -> {
        if(failure!=null || result==null) outcomes.add(Outcome.message("online.sneak.refused",List.of()));
        else if(!result.accepted() && ABILITY_BUSY_KEY.equals(result.messageKey())) {
          try { link.sneak().whenComplete((retry,retryFailure) -> outcomes.add(Outcome.message(
              retryFailure!=null || retry==null || retry.messageKey()==null ? "online.sneak.refused" : retry.messageKey(),
              retryFailure!=null || retry==null ? List.of() : retry.args()))); }
          catch(IllegalStateException unavailable) { outcomes.add(Outcome.message(ABILITY_BUSY_KEY,List.of())); }
        } else if(result.accepted()) outcomes.add(Outcome.message("online.sneak.started",List.of()));
        else outcomes.add(Outcome.message(result.messageKey()==null ? "online.sneak.refused" : result.messageKey(),result.args()));
      });
      return true;
    } catch(IllegalStateException unavailable) { outcomes.add(Outcome.message(ABILITY_BUSY_KEY,List.of()));return true; }
    catch(IllegalArgumentException invalid) { return false; }
  }

  static final String STAT_BUSY_KEY = "online.stats.busy";

  @Override
  public boolean allocateStats(
      Map<String, Integer> allocation, Consumer<OnlineConnection.CommandResult> completion) {
    if (allocation == null || allocation.isEmpty() || completion == null) return false;
    final Map<String, Integer> request;
    try {
      request = Map.copyOf(allocation);
    } catch (IllegalArgumentException | NullPointerException invalid) {
      return false;
    }
    try {
      allocateStatsWithRetry(request, true)
          .whenComplete((result, failure) -> {
            String key = failure != null || result == null ? "online.stats.refused" : result.messageKey();
            List<Integer> args = failure != null || result == null ? List.of() : result.args();
            outcomes.add(Outcome.completion(key, args, completion,
                failure != null || result == null ? null : result));
          });
      return true;
    } catch (IllegalStateException pendingOrOffline) {
      outcomes.add(Outcome.completion(STAT_BUSY_KEY, List.of(), completion, null));
      return true;
    } catch (IllegalArgumentException invalid) {
      return false;
    }
  }

  private CompletionStage<OnlineConnection.CommandResult> allocateStatsWithRetry(
      Map<String, Integer> allocation, boolean mayRetry) {
    try {
      return link.allocateStats(allocation)
          .thenCompose(result -> {
            if (mayRetry && result != null && !result.accepted() && STAT_BUSY_KEY.equals(result.messageKey()))
              return allocateStatsWithRetry(allocation, false);
            return CompletableFuture.completedFuture(result);
          });
    } catch (IllegalStateException | IllegalArgumentException unavailable) {
      return CompletableFuture.failedFuture(unavailable);
    }
  }

  @Override
  public void closeShop() {
    shopGeneration.incrementAndGet();
  }

  private boolean trade(String npc, String itemKey, int quantity, boolean buying) {
    if (npc == null || itemKey == null) return false;
    long generation = shopGeneration.get();
    try {
      sendTrade(npc, itemKey, quantity, buying, generation, true);
      return true;
    } catch (IllegalStateException pendingOrOffline) {
      outcomes.add(Outcome.message("online.shop.busy", List.of()));
      return true;
    } catch (IllegalArgumentException invalidRequest) {
      return false;
    }
  }

  /** The server's "busy / revision changed / region epoch" refusal of a trade (see {@code OnlineCommandRefusals}). */
  static final String SHOP_BUSY_KEY = "online.shop.busy";

  /**
   * One trade request. A trade refused as busy or stale changed nothing (a checkpoint had just moved the revision),
   * so, like a greeting, it is sent once more with the refreshed revision; a second refusal is shown. A trade that
   * was accepted, or refused for any other reason (gold, weight, stock), is never sent again.
   */
  private void sendTrade(String npc, String itemKey, int quantity, boolean buying, long generation, boolean mayRetry) {
    var action = buying ? link.shopBuy(npc, itemKey, quantity) : link.shopSell(npc, itemKey, quantity);
    action.whenComplete(
        (result, failure) -> {
          if (failure != null || result == null) {
            outcomes.add(Outcome.message("online.shop.refused", List.of()));
            return;
          }
          if (!result.accepted()
              && mayRetry
              && SHOP_BUSY_KEY.equals(result.messageKey())
              && shopGeneration.get() == generation) {
            try {
              sendTrade(npc, itemKey, quantity, buying, generation, false);
              return;
            } catch (IllegalStateException | IllegalArgumentException unavailable) {
              // fall through: show the refusal
            }
          }
          outcomes.add(Outcome.message(result.messageKey(), result.args()));
          if (shopGeneration.get() == generation)
            requestOffers(npc, "online.shop.busy", true, generation);
        });
  }

  private void requestShopAfter(String npc, OnlineConnection.CommandResult interaction, long generation) {
    if (shopGeneration.get() != generation) return;
    try {
      link.shopOffers(npc)
          .whenComplete(
              (listing, failure) -> {
                if (shopGeneration.get() != generation) return;
                if (failure != null || listing == null) {
                  greeted(npc, interaction);
                } else if (listing.accepted() && listing.shop() != null) {
                  outcomes.add(Outcome.shop(npc, listing.shop(), generation));
                } else {
                  greeted(npc, interaction);
                }
              });
    } catch (IllegalStateException | IllegalArgumentException unavailable) {
      greeted(npc, interaction);
    }
  }

  /** A greeting with no shop behind it: say it, then offer the NPC's topics. */
  private void greeted(String npc, OnlineConnection.CommandResult interaction) {
    outcomes.add(Outcome.message(interaction.messageKey(), interaction.args()));
    outcomes.add(Outcome.talk(npc));
  }

  private boolean requestOffers(String npc, String failureKey, boolean quietIfBusy, long generation) {
    try {
      link.shopOffers(npc)
          .whenComplete(
              (result, failure) -> {
                if (shopGeneration.get() != generation) return;
                if (failure != null || result == null) {
                  if (!quietIfBusy) outcomes.add(Outcome.message(failureKey, List.of()));
                } else if (result.accepted() && result.shop() != null) {
                  outcomes.add(Outcome.shop(npc, result.shop(), generation));
                } else if (!quietIfBusy) {
                  outcomes.add(Outcome.message(result.messageKey() == null ? failureKey : result.messageKey(), result.args()));
                }
              });
      return true;
    } catch (IllegalStateException pendingOrOffline) {
      if (!quietIfBusy) outcomes.add(Outcome.message(failureKey, List.of()));
      return true;
    } catch (IllegalArgumentException invalidIdentity) {
      return false;
    }
  }

  private boolean send(
      Supplier<CompletionStage<OnlineConnection.CommandResult>> command, String failureKey) {
    try {
      command
          .get()
          .whenComplete(
              (result, failure) ->
                  outcomes.add(
                      failure != null || result == null
                  ? Outcome.message(failureKey, List.of())
                          : Outcome.message(result.messageKey(), result.args())));
      return true;
    } catch (IllegalStateException pendingOrOffline) {
      // Single-flight: a second click while one is in flight, or a link that is down. Say so.
      outcomes.add(Outcome.message(failureKey, List.of()));
      return true;
    } catch (IllegalArgumentException invalidIdentity) {
      return false;
    }
  }

  /** Shows the messages of replies that arrived since the last call; call on the render thread. */
  public void pump() {
    if (host != null) {
      for (var message : link.drainChatMessages()) host.showChat(message.speaker(), message.text());
    }
    Outcome outcome;
    while ((outcome = outcomes.poll()) != null) {
      if (outcome.completion() != null) outcome.completion().accept(outcome.commandResult());
      if (outcome.talkNpc() != null) {
        if (host != null) host.showTalk(outcome.talkNpc());
        continue;
      }
      if (outcome.shop() != null) {
        if (host != null && outcome.shopGeneration() == shopGeneration.get())
          host.showShop(outcome.shopNpc(), outcome.shop());
        continue;
      }
      String key = outcome.key();
      if (key == null || SILENT_COOLDOWN_KEY.equals(key) || host == null) continue;
      host.showMessage(
          outcome.args().isEmpty()
              ? withOwnName(I18n.key(key))
              : I18n.message(key, outcome.args().toArray()));
    }
  }

  /** Queues a chat line for server routing without blocking the render thread. */
  public boolean sendChat(String text) {
    return link.sendChat(text);
  }

  /**
   * The server's replies carry integer arguments only, so an NPC line that greets the player by
   * name (single-player fills it with the character's name) arrived with a literal {@code %s}.
   * A template that asks for a single text value gets the player's own character name, which
   * the client already holds; anything else is shown unchanged.
   */
  private String withOwnName(String template) {
    Player player = host == null ? null : host.player();
    String name = player == null ? null : player.getName();
    if (name == null || name.isBlank() || !template.contains("%s")) return template;
    try {
      return String.format(java.util.Locale.ROOT, template, name);
    } catch (java.util.IllegalFormatException unsupported) {
      return template;
    }
  }

  // --- movement ---

  @Override
  public void move(float dx, float dy, float delta) {
    if (host == null) {
      throw new IllegalStateException("remote commands used before the screen bound them");
    }
    pump();
    Player player = host.player();
    if (player == null) return;
    var at = player.getCoordinates();
    if (reconciler == null) {
      reconciler =
          new MovementReconciler(rule, new MovementReconciler.Position(at.getX(), at.getY()), 0);
    }
    reconcile(player);
    ease(player, delta);

    boolean gated =
        player.isStunned()
            || player.isPhysicallyExhausted()
            || player.getAnimations().isAttacking();
    float beforeX = at.getX();
    float beforeY = at.getY();
    player.move(dx, dy, delta);

    int sx = gated ? 0 : (int) Math.signum(dx);
    int sy = gated ? 0 : (int) Math.signum(dy);
    if (!gated && sx == 0 && sy == 0 && !player.getMovement().getPath().isEmpty()) {
      // Click-to-move: no key is held, so send the direction the path step actually took.
      sx = (int) Math.signum(at.getX() - beforeX);
      sy = (int) Math.signum(at.getY() - beforeY);
    }
    MovementReconciler.Input input = reconciler.predict(sx, sy, delta);
    link.direction(sx, sy, input.sequence());
  }

  private void reconcile(Player player) {
    OnlineConnection.View view = link.view();
    if (view == null || view.snapshot() == null || view.map() == null) return;
    var region = view.snapshot().region();
    if (region != null && !region.equals(view.map().region())) return;
    OnlineConnection.Actor own = null;
    for (OnlineConnection.Actor actor : view.snapshot().players()) {
      if (actor.id() == view.ownId()) own = actor;
    }
    if (own == null) return;
    long tick = view.snapshot().tick();
    long epoch = view.map().locationEpoch();
    MovementReconciler.Position server =
        new MovementReconciler.Position(
            (view.map().originX() + own.x() - 0.5) * GRID_W,
            (view.map().originY() + own.y() - 0.5) * GRID_H);

    if (epoch != lastEpoch) {
      // First view, teleport, respawn or map change: the server position is the truth.
      lastEpoch = epoch;
      lastTick = tick;
      reconciler.reset(server);
      easeX = 0;
      easeY = 0;
      if (tilesBetween(player, server) >= EASE_FLOOR_TILES) place(player, server);
      return;
    }
    if (tick == lastTick) return;
    lastTick = tick;
    if (reconciler.unacknowledged() > MAX_PENDING_FRAMES) reconciler.reset(server);

    MovementReconciler.Correction correction =
        reconciler.acknowledge(link.acknowledgedFrame(own.sequence()), server);
    double error = tilesBetween(player, correction.position());
    if (error >= MovementReconciler.SNAP_DISTANCE_TILES) {
      easeX = 0;
      easeY = 0;
      place(player, correction.position());
    } else if (error >= EASE_FLOOR_TILES) {
      easeX = (float) (correction.position().x() - player.getCoordinates().getX());
      easeY = (float) (correction.position().y() - player.getCoordinates().getY());
    } else {
      easeX = 0;
      easeY = 0;
    }
  }

  private void ease(Player player, float delta) {
    if (easeX == 0f && easeY == 0f) return;
    float fraction = Math.min(1f, Math.max(0f, delta) / EASE_SECONDS);
    float stepX = easeX * fraction;
    float stepY = easeY * fraction;
    var at = player.getCoordinates();
    player.setWorldPosition(at.getX() + stepX, at.getY() + stepY, at.getZ());
    easeX -= stepX;
    easeY -= stepY;
    if (Math.abs(easeX) < 0.5f && Math.abs(easeY) < 0.5f) {
      easeX = 0;
      easeY = 0;
    }
  }

  private static void place(Player player, MovementReconciler.Position position) {
    player.setWorldPosition(
        (float) position.x(), (float) position.y(), player.getCoordinates().getZ());
  }

  private static double tilesBetween(Player player, MovementReconciler.Position position) {
    var at = player.getCoordinates();
    return Math.hypot((position.x() - at.getX()) / GRID_W, (position.y() - at.getY()) / GRID_H);
  }
}
