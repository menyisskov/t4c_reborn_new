package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import com.perso.T4C.session.GameSession;
import com.perso.T4C.session.LocalGameSession;
import com.perso.T4C.session.SessionKind;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A character a server owns must never reach the local save file, whichever save entry point a
 * forgotten call site uses (client unification design, section 3.7).
 */
class PlayerStateStoreRemoteGuardTest {
  private String originalUserDir;
  @TempDir Path dir;

  @BeforeEach
  void useTemporaryWorkingDirectory() {
    originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", dir.toString());
    PlayerStateStore.setActiveFilename("hero.json");
    PlayerStateStore.clearCompanionSupplier();
  }

  @AfterEach
  void restore() {
    System.setProperty("user.dir", originalUserDir);
    PlayerStateStore.resetActiveFilename();
    PlayerStateStore.clearCompanionSupplier();
  }

  private static Player hero(boolean remote) {
    Player p = new Player();
    p.setName("Goldie");
    p.setStrength(41);
    p.setDexterity(32);
    p.setEndurance(33);
    p.setIntelligence(24);
    p.setWisdom(25);
    p.setLevel(12);
    p.setMaxHp(180);
    p.setCurrentHp(111);
    p.setRemote(remote);
    return p;
  }

  @Test
  void playersAreLocalByDefault() {
    assertFalse(new Player().isRemote());
  }

  @Test
  void aRemoteCharacterIsNeverWritten() throws IOException {
    Path save = dir.resolve("hero.json");
    Files.writeString(save, "{\"untouched\":true}");
    Player remote = hero(true);

    PlayerStateStore.save(remote);
    PlayerStateStore.save(remote, 9f);

    assertEquals("{\"untouched\":true}", Files.readString(save));
    try (var files = Files.list(dir)) {
      assertEquals(1, files.count(), "no temp or extra file may appear");
    }
  }

  @Test
  void aRemoteCharacterIsNotWrittenWhenNoSaveExistsYet() throws IOException {
    PlayerStateStore.save(hero(true), 12f);
    try (var files = Files.list(dir)) {
      assertEquals(0, files.count());
    }
  }

  @Test
  void aLocalCharacterStillSaves() {
    PlayerStateStore.save(hero(false), 12f);
    assertTrue(Files.exists(dir.resolve("hero.json")));
  }

  @Test
  void theLocalSessionSavesThroughTheSeamAndRefusesRemoteToo() {
    GameSession session = new LocalGameSession();
    assertEquals(SessionKind.LOCAL, session.kind());

    session.persistence().save(hero(true), 5f);
    assertFalse(Files.exists(dir.resolve("hero.json")));

    session.persistence().save(hero(false), 5f);
    assertTrue(Files.exists(dir.resolve("hero.json")));
  }
}
