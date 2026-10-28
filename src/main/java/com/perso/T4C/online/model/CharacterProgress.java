package com.perso.T4C.online.model;

import java.util.*;

/** Versioned, immutable server-owned gameplay state. Never accept this object from a client. */
public record CharacterProgress(
    int version, String characterClass, String gender, Attributes attributes, Vitals vitals,
    Advancement advancement, List<Item> inventory, Map<String, UUID> equipment,
    Map<String, Integer> skills, Set<String> spells, Map<String, Integer> questFlags,
    List<Item> storage, long storageGold, Respawn respawn, List<Buff> buffs,
    Map<String, Long> cooldowns, long hiddenUntil, Companion companion) {
  /** Version 2 adds the resolved amounts and start time of each buff; version 1 records still load unchanged. */
  public static final int CURRENT_VERSION = 2;
  public static final int OLDEST_VERSION = 1;

  public CharacterProgress {
    if (version < OLDEST_VERSION || version > CURRENT_VERSION) throw new IllegalArgumentException("Unsupported character state version");
    key(characterClass);
    if (!Set.of("male","female").contains(Objects.requireNonNull(gender))) throw new IllegalArgumentException("Invalid gender");
    Objects.requireNonNull(attributes); Objects.requireNonNull(vitals); Objects.requireNonNull(advancement);
    inventory=List.copyOf(inventory); equipment=Map.copyOf(equipment); skills=Map.copyOf(skills);
    spells=Set.copyOf(spells); questFlags=Map.copyOf(questFlags); storage=List.copyOf(storage);
    buffs=List.copyOf(buffs); cooldowns=Map.copyOf(cooldowns);
    nonnegative(storageGold); nonnegative(hiddenUntil);
    var owned=new HashSet<UUID>();
    for (var item:inventory) if (!owned.add(item.id())) throw new IllegalArgumentException("Duplicate item instance");
    var worn=new HashSet<UUID>();
    equipment.forEach((slot,id) -> {
      key(slot);
      if (!owned.contains(id) || !worn.add(id)) throw new IllegalArgumentException("Equipment must reference one owned item");
    });
    for (var item:storage) if (!owned.add(item.id())) throw new IllegalArgumentException("Item cannot exist in two containers");
    skills.forEach((name,value) -> { key(name); nonnegative(value); });
    spells.forEach(name -> contentKey(name,"spell."));
    questFlags.keySet().forEach(CharacterProgress::key);
    cooldowns.forEach((name,until) -> { key(name); nonnegative(until); });
  }

  public record Attributes(int strength, int endurance, int dexterity, int wisdom, int intelligence) {
    public Attributes { positive(strength); positive(endurance); positive(dexterity); positive(wisdom); positive(intelligence); }
  }
  public record Vitals(int hp, int maxHp, int mana, int maxMana) {
    public Vitals {
      positive(maxHp); nonnegative(maxMana); nonnegative(hp); nonnegative(mana);
      if (hp>maxHp || mana>maxMana) throw new IllegalArgumentException("Vitals exceed maximum");
    }
  }
  public record Advancement(int level, int statPoints, int skillPoints, int rebirths, int karma) {
    public Advancement { positive(level); nonnegative(statPoints); nonnegative(skillPoints); nonnegative(rebirths); }
  }
  /** Individual identity survives equipment/storage moves; quantity supports stackable items. */
  public record Item(UUID id, String key, int quantity, int charges) {
    public Item { Objects.requireNonNull(id); contentKey(key,"item."); positive(quantity); nonnegative(charges); }
  }
  public record Respawn(int world, double x, double y) {
    public Respawn {
      nonnegative(world);
      if (!Double.isFinite(x) || !Double.isFinite(y) || x<0 || y<0) throw new IllegalArgumentException("Invalid respawn position");
    }
  }
  /**
   * Absolute UTC expiry prevents reconnecting from extending buffs or cooldowns. {@code modifiers} are the amounts
   * rolled when the spell was cast, keyed by normalised attribute; they are kept as rolled and never re-rolled.
   * {@code expiresAt} of {@link Long#MAX_VALUE} never expires. A version 1 buff has no amounts and no start time.
   */
  public record Buff(String spell, long expiresAt, int casterIntelligence, int casterWisdom,
      Map<String,Integer> modifiers, long appliedAt) {
    public Buff {
      contentKey(spell,"spell."); nonnegative(expiresAt); nonnegative(casterIntelligence); nonnegative(casterWisdom);
      nonnegative(appliedAt);
      modifiers=modifiers==null ? Map.of() : Map.copyOf(modifiers);
      modifiers.keySet().forEach(CharacterProgress::key);
    }
    public Buff(String spell, long expiresAt, int casterIntelligence, int casterWisdom) {
      this(spell,expiresAt,casterIntelligence,casterWisdom,Map.of(),0);
    }
  }
  public record Companion(String species, boolean tamed, int level, int hp, String mode) {
    public Companion { key(species); positive(level); nonnegative(hp); key(mode); }
  }

  private static void key(String value) {
    if (value==null || value.isBlank() || value.length()>256) throw new IllegalArgumentException("Invalid state key");
  }
  private static void contentKey(String value,String prefix) {
    key(value);
    if (!value.startsWith(prefix) || value.length()==prefix.length()) throw new IllegalArgumentException("Unnormalized content key");
  }
  private static void positive(long value) { if (value<1) throw new IllegalArgumentException("Expected positive value"); }
  private static void nonnegative(long value) { if (value<0) throw new IllegalArgumentException("Expected nonnegative value"); }
}
