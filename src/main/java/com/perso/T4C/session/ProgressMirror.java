package com.perso.T4C.session;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.OnlineCharacterView;
import com.perso.T4C.player.BodyPart;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Reads the server's committed character state, as the existing client model holds it, and writes
 * the {@link PlayerStateDto} that {@code PlayerStateMapper.applyToPlayer} already knows how to put
 * on a {@code Player}. Pure data conversion: no network, no files, no graphics.
 *
 * <p>Where the server model has no value for something the single-player state carries, the
 * single-player default is left in place (see the parity-debt list in PARITY.md); nothing is
 * invented. Conventions that differ between the two models:
 *
 * <ul>
 *   <li>The server keeps worn items inside the inventory (equipment names an owned instance);
 *       single-player moves a worn item out of the inventory. Worn instances are therefore left out
 *       of the inventory list, and a two-slot item fills both slots, as {@code InventoryService}
 *       does.
 *   <li>The server's cumulative experience is split into the level's progress and threshold with
 *       the same curve the server uses to build it.
 *   <li>Server maximum health includes the health granted by running buffs; single-player stores
 *       the base and re-adds the buff bonus when the buffs are applied.
 *   <li>Positions are region-local tile-centre doubles; single-player positions are the tile's
 *       top-left anchor in absolute tiles, so the region origin is added and half a tile removed.
 * </ul>
 */
public final class ProgressMirror {
  private ProgressMirror() {}

  /**
   * The mirrored state plus the server identities the GUI needs later. {@code inventoryIds} is
   * parallel to {@code state.inventory} (a stack of N is N entries sharing one id) and {@code
   * equipmentIds} maps the worn body-part name to the instance id; both are the seed of the
   * session's {@code ItemInstanceIndex}.
   */
  public record Mirrored(
      PlayerStateDto state,
      List<UUID> inventoryIds,
      Map<String, UUID> equipmentIds,
      long revision,
      long locationEpoch,
      UUID characterId) {}

  /** Mirrors the view now, using the wall clock for buff and hiding expiry. */
  public static Mirrored toStateDto(OnlineConnection.View view) {
    return toStateDto(view, System::currentTimeMillis);
  }

  /**
   * @throws IllegalStateException when the view is not ready: no character, a legacy movement-only
   *     character without progress, no snapshot or no own actor in it
   */
  public static Mirrored toStateDto(OnlineConnection.View view, LongSupplier clock) {
    if (view == null || view.character() == null || view.character().progress() == null) {
      throw new IllegalStateException("online character state is not available yet");
    }
    if (view.map() == null || view.snapshot() == null) {
      throw new IllegalStateException("online position is not available yet");
    }
    OnlineConnection.Actor self =
        view.snapshot().players().stream()
            .filter(actor -> actor.id() == view.ownId())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("own character is not in the snapshot"));
    OnlineCharacterView character = view.character();
    CharacterProgress progress = character.progress();
    long now = clock.getAsLong();
    PlayerStateDto state = new PlayerStateDto();

    state.worldLayoutVersion = com.perso.T4C.helper.AvalonWorldLayout.WORLD_LAYOUT_VERSION;
    state.name = character.name();
    state.gender = progress.gender().toUpperCase(Locale.ROOT);
    state.x = (float) (view.map().originX() + self.x() - 0.5);
    state.y = (float) (view.map().originY() + self.y() - 0.5);
    state.z = view.map().world();

    CharacterProgress.Attributes attributes = progress.attributes();
    state.strength = attributes.strength();
    state.dexterity = attributes.dexterity();
    state.endurance = attributes.endurance();
    state.intelligence = attributes.intelligence();
    state.wisdom = attributes.wisdom();
    state.gold = clampToInt(character.gold());
    state.karma = progress.advancement().karma();

    int buffMaxHp = 0;
    for (CharacterProgress.Buff buff : progress.buffs()) {
      buffMaxHp += buff.modifiers().getOrDefault("maxHp", 0);
    }
    CharacterProgress.Vitals vitals = progress.vitals();
    state.maxHp = Math.max(1, vitals.maxHp() - buffMaxHp);
    state.currentHp = vitals.hp();
    state.maxMana = vitals.maxMana();
    state.mana = vitals.mana();

    CharacterProgress.Advancement advancement = progress.advancement();
    XpCurve curve = XpCurve.loadDefault();
    state.level = advancement.level();
    state.currentXp = Math.max(0L, character.experience() - curve.getTotalXp(advancement.level()));
    state.xpToNextLevel = curve.getXpToNextLevel(advancement.level());
    state.statPoints = advancement.statPoints();
    state.skillPoints = advancement.skillPoints();
    state.rebirthCount = advancement.rebirths();
    state.gmRank = com.perso.T4C.player.GmRank.PLAYER.name();

    state.spells = new ArrayList<>(progress.spells().stream().sorted().toList());
    // Quick slots and macros are client preferences, stored per online character on this machine
    // (design 3.7); the server holds none.
    state.quickSlots = new ArrayList<>();
    state.macros = new ArrayList<>();
    state.activeBuffs = activeBuffs(progress, now);

    Map<UUID, CharacterProgress.Item> byId = new HashMap<>();
    for (CharacterProgress.Item item : progress.inventory()) byId.put(item.id(), item);
    state.equipment = new HashMap<>();
    Map<String, UUID> equipmentIds = new HashMap<>();
    for (Map.Entry<String, UUID> worn : progress.equipment().entrySet()) {
      CharacterProgress.Item item = byId.get(worn.getValue());
      if (item == null) continue;
      state.equipment.put(worn.getKey(), item.key());
      equipmentIds.put(worn.getKey(), item.id());
      ItemDefinition definition = ItemRegistry.findByKey(item.key());
      BodyPart secondary = definition == null ? null : definition.getSecondaryBodyPart();
      if (secondary != null) {
        state.equipment.put(secondary.name(), item.key());
        equipmentIds.put(secondary.name(), item.id());
      }
    }

    java.util.Set<UUID> wornIds = new java.util.HashSet<>(progress.equipment().values());
    state.inventory = new ArrayList<>();
    List<UUID> inventoryIds = new ArrayList<>();
    state.itemCharges = new HashMap<>();
    for (CharacterProgress.Item item : progress.inventory()) {
      if (wornIds.contains(item.id())) continue;
      for (int i = 0; i < item.quantity(); i++) {
        state.inventory.add(item.key());
        inventoryIds.add(item.id());
      }
      if (item.charges() > 0) state.itemCharges.merge(item.key(), item.charges(), Integer::sum);
    }

    state.storage = new ArrayList<>();
    state.storageCharges = new ArrayList<>();
    for (CharacterProgress.Item item : progress.storage()) {
      for (int i = 0; i < item.quantity(); i++) {
        state.storage.add(item.key());
        state.storageCharges.add(item.charges());
      }
    }
    state.storageGold = clampToInt(progress.storageGold());

    state.skills = new HashMap<>(progress.skills());
    state.questFlags = new HashMap<>(progress.questFlags());

    CharacterProgress.Respawn respawn = progress.respawn();
    if (respawn != null) {
      state.respawnPointDefined = true;
      state.respawnWorldX = (float) (respawn.x() * GameConstants.GRID_W);
      state.respawnWorldY = (float) (respawn.y() * GameConstants.GRID_H);
      state.respawnWorldZ = respawn.world();
    }

    long hiddenUntil = progress.hiddenUntil();
    state.hiddenRemainingMillis =
        hiddenUntil == Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(0L, hiddenUntil - now);

    CharacterProgress.Companion companion = progress.companion();
    if (companion != null) {
      state.companion = new PlayerStateDto.CompanionState();
      state.companion.speciesName = companion.species();
      state.companion.tamed = companion.tamed();
      state.companion.level = companion.level();
      state.companion.currentHp = companion.hp();
      state.companion.mode = companion.mode();
    }

    return new Mirrored(
        state,
        List.copyOf(inventoryIds),
        Map.copyOf(equipmentIds),
        character.revision(),
        character.locationEpoch(),
        character.id());
  }

  private static List<PlayerStateDto.ActiveBuffState> activeBuffs(
      CharacterProgress progress, long now) {
    List<PlayerStateDto.ActiveBuffState> buffs = new ArrayList<>();
    for (CharacterProgress.Buff buff : progress.buffs()) {
      PlayerStateDto.ActiveBuffState state = new PlayerStateDto.ActiveBuffState();
      state.spellName = buff.spell();
      state.casterIntelligence = buff.casterIntelligence();
      state.casterWisdom = buff.casterWisdom();
      if (buff.expiresAt() == Long.MAX_VALUE) {
        state.remainingSeconds = Long.MAX_VALUE;
        state.totalDurationSeconds = Long.MAX_VALUE;
      } else {
        if (buff.expiresAt() <= now) continue;
        state.remainingSeconds = (buff.expiresAt() - now + 999L) / 1000L;
        long total =
            buff.appliedAt() > 0 && buff.appliedAt() < buff.expiresAt()
                ? (buff.expiresAt() - buff.appliedAt() + 999L) / 1000L
                : state.remainingSeconds;
        state.totalDurationSeconds = Math.max(state.remainingSeconds, total);
      }
      buffs.add(state);
    }
    return buffs;
  }

  private static int clampToInt(long value) {
    return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, value));
  }
}
