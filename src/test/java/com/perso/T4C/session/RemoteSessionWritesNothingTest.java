package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.model.QuickSlotEntry;
import com.perso.T4C.player.Player;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A remote character driven through the same mutate-then-save sequences the single-player GUI
 * classes and input handlers perform must leave the save directory byte-for-byte untouched: no
 * state file, no roster, no account vault, no temp file. The sequences mirror the call sites
 * (Inventory use/equip, quick-slot swap, shop buy, spell learn, skill training, dispose, autosave).
 */
class RemoteSessionWritesNothingTest {
  private String originalUserDir;
  @TempDir Path dir;

  @BeforeEach
  void useTemporaryWorkingDirectory() throws IOException {
    originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", dir.toString());
    PlayerStateStore.clearCompanionSupplier();
    Files.writeString(dir.resolve("hero.json"), "{\"local\":\"hero\"}");
    Files.writeString(dir.resolve("characters.json"), "{\"roster\":true}");
    Files.writeString(dir.resolve("account_storage.json"), "{\"storage\":[]}");
    Files.createDirectories(dir.resolve("characters"));
    Files.writeString(dir.resolve("characters/slot-1.json"), "{\"slot\":1}");
    PlayerStateStore.setActiveFilename("characters/slot-1.json");
  }

  @AfterEach
  void restore() {
    System.setProperty("user.dir", originalUserDir);
    PlayerStateStore.resetActiveFilename();
    PlayerStateStore.clearCompanionSupplier();
  }

  private static Player remote() {
    Player p = new Player();
    p.setName("Remote");
    p.setStrength(41);
    p.setDexterity(32);
    p.setEndurance(33);
    p.setIntelligence(24);
    p.setWisdom(25);
    p.setLevel(12);
    p.setMaxHp(180);
    p.setCurrentHp(111);
    p.setRemote(true);
    return p;
  }

  private Map<String, byte[]> snapshot() throws IOException {
    Map<String, byte[]> all = new TreeMap<>();
    List<Path> files;
    try (Stream<Path> walk = Files.walk(dir)) {
      files = walk.filter(Files::isRegularFile).toList();
    }
    for (Path file : files) {
      all.put(dir.relativize(file).toString().replace('\\', '/'), Files.readAllBytes(file));
    }
    return all;
  }

  private void assertUntouched(Map<String, byte[]> before) throws IOException {
    Map<String, byte[]> after = snapshot();
    assertEquals(before.keySet(), after.keySet(), "no file may appear or vanish");
    for (String name : before.keySet()) {
      assertArrayEquals(before.get(name), after.get(name), name + " must not change");
    }
  }

  @Test
  void guiStyleMutateThenSaveSequencesWriteNothingForARemoteCharacter() throws IOException {
    Map<String, byte[]> before = snapshot();
    Player p = remote();
    GameSession session = new LocalGameSession();

    p.setInventory(new ArrayList<>(List.of("item.dagger")));
    PlayerStateStore.save(p);
    p.getInventory().remove("item.dagger");
    session.persistence().save(p, 7f);
    p.setQuickSlots(new ArrayList<>(List.of(new QuickSlotEntry(2, "spell.heal_light"))));
    PlayerStateStore.save(p);
    p.getQuickSlots().clear();
    PlayerStateStore.save(p, 12.5f);
    p.setGold(p.getGold() + 10);
    p.setSpells(new ArrayList<>(List.of("spell.heal_light")));
    p.setSkillLevel("attack", 120);
    PlayerStateStore.save(p);
    PlayerStateStore.save(p);
    session.persistence().save(p, 3f);

    assertUntouched(before);
  }

  @Test
  void switchingTheActiveCharacterFileDoesNotLetARemoteCharacterThrough() throws IOException {
    Map<String, byte[]> before = snapshot();
    Player p = remote();
    PlayerStateStore.setActiveFilename("characters/does-not-exist-yet.json");
    PlayerStateStore.save(p);
    PlayerStateStore.resetActiveFilename();
    PlayerStateStore.save(p);
    assertUntouched(before);
  }
}
