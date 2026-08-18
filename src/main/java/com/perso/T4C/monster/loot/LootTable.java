package com.perso.T4C.monster.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LootTable {
  private static final LootTable EMPTY = new LootTable(0, 0, Collections.emptyList());
  @Getter private final int goldMin;
  @Getter private final int goldMax;
  private final List<LootEntry> entries;

  public static LootTable empty() {
    return EMPTY;
  }

  public List<LootEntry> getEntries() {
    return Collections.unmodifiableList(entries);
  }

  public static Builder builder() {
    return new Builder();
  }

  public LootResult roll(Random random) {
    Random rng = random != null ? random : new Random();
    int gold = goldMax <= goldMin ? goldMin : goldMin + rng.nextInt(goldMax - goldMin + 1);
    List<String> items = new ArrayList<>();
    for (LootEntry entry : entries) {
      if (entry == null || entry.itemName() == null || entry.itemName().isEmpty()) {
        continue;
      }
      if (entry.chance() >= 1f || rng.nextFloat() < entry.chance()) {
        items.add(entry.itemName());
      }
    }
    return new LootResult(Math.max(0, gold), items);
  }

  public static final class Builder {
    private int goldMin = 0;
    private int goldMax = 0;
    private final List<LootEntry> entries = new ArrayList<>();

    public Builder gold(int min, int max) {
      this.goldMin = Math.max(0, Math.min(min, max));
      this.goldMax = Math.max(0, Math.max(min, max));
      return this;
    }

    public Builder item(String itemName, float chance) {
      if (itemName != null && !itemName.isEmpty()) {
        entries.add(new LootEntry(itemName, Math.max(0f, Math.min(1f, chance))));
      }
      return this;
    }

    public LootTable build() {
      if (goldMin == 0 && goldMax == 0 && entries.isEmpty()) {
        return EMPTY;
      }
      return new LootTable(goldMin, goldMax, List.copyOf(entries));
    }
  }
}
