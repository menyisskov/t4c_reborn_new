package com.perso.T4C.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perso.T4C.config.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The account-wide storage vault: one shared stash and one shared gold pile for every character
 * on this local roster (see {@code characters.json}), so gear and gold can move between a
 * player's own characters without a mule run. Persisted to its own file, independent of any one
 * character's save file, and loaded once per game session.
 */
public final class AccountStorage {
  private static final class Dto {
    List<String> storage = new ArrayList<>();
    List<Double> storageDurability = new ArrayList<>();
    List<Integer> storageCharges = new ArrayList<>();
    int storageGold;
  }

  private static AccountStorage instance;

  private final List<String> storage;
  private final List<Double> storageDurability;
  private final List<Integer> storageCharges;
  private int storageGold;

  private AccountStorage(Dto dto) {
    this.storage = dto.storage != null ? dto.storage : new ArrayList<>();
    this.storageDurability = dto.storageDurability != null ? dto.storageDurability : new ArrayList<>();
    this.storageCharges = dto.storageCharges != null ? dto.storageCharges : new ArrayList<>();
    this.storageGold = dto.storageGold;
  }

  public static synchronized AccountStorage get() {
    if (instance == null) instance = new AccountStorage(load());
    return instance;
  }

  /** Test-only: forces the next {@link #get()} to reload from disk. */
  static synchronized void reset() {
    instance = null;
  }

  public List<String> storage() {
    return storage;
  }

  public List<Double> storageDurability() {
    return storageDurability;
  }

  public List<Integer> storageCharges() {
    return storageCharges;
  }

  public int storageGold() {
    return storageGold;
  }

  public void setStorageGold(int amount) {
    this.storageGold = Math.max(0, amount);
  }

  public void save() {
    Dto dto = new Dto();
    dto.storage = storage;
    dto.storageDurability = storageDurability;
    dto.storageCharges = storageCharges;
    dto.storageGold = storageGold;
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      Path outPath = Path.of(System.getProperty("user.dir"), Paths.ACCOUNT_STORAGE_FILE);
      Path parent = outPath.getParent();
      if (parent != null) Files.createDirectories(parent);
      Files.writeString(outPath, gson.toJson(dto), StandardCharsets.UTF_8);
    } catch (Exception ignored) {
      // Best-effort persistence, same as the rest of this package: a failed write here just
      // means the vault's next change tries again, not a crash mid-game.
    }
  }

  private static Dto load() {
    try {
      Path inPath = Path.of(System.getProperty("user.dir"), Paths.ACCOUNT_STORAGE_FILE);
      if (!Files.exists(inPath)) return new Dto();
      String json = Files.readString(inPath, StandardCharsets.UTF_8);
      Gson gson = new Gson();
      Dto dto = gson.fromJson(json, Dto.class);
      return dto == null ? new Dto() : dto;
    } catch (Exception e) {
      return new Dto();
    }
  }
}
