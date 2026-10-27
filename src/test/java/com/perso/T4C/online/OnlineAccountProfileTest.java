package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OnlineAccountProfileTest {
  private static final String SECRET = "a".repeat(64);
  @TempDir Path temp;

  @Test void existingCharacterProfileKeepsDirectLaunchAndCanPreferLobbySelection() throws Exception {
    UUID selected = UUID.randomUUID();
    Path file = write("{\"secret\":\"" + SECRET + "\",\"characterId\":\"" + selected
        + "\",\"accountId\":\"" + UUID.randomUUID() + "\"}");
    var profile = OnlineAccountProfile.load(file);
    assertEquals(OnlineLobbyLaunch.Mode.DIRECT, OnlineLobbyLaunch.select(false, true));
    assertEquals(selected, profile.directCredentials().characterId());
    assertTrue(profile.lobbyModel().confirm(0).isEmpty(), "An unpopulated lobby must not join");
    assertFalse(profile.toString().contains(SECRET));
  }

  @Test void accountOnlyProfileBootstrapsLobbyButCannotLaunchDirectly() throws Exception {
    var profile = OnlineAccountProfile.load(write("{\"secret\":\"" + SECRET
        + "\",\"accountId\":\"" + UUID.randomUUID() + "\"}"));
    assertEquals(OnlineLobbyLaunch.Mode.LOBBY, OnlineLobbyLaunch.select(true, true));
    assertTrue(profile.lobbyModel().selectedCharacterId().isEmpty());
    assertTrue(profile.lobbyModel().confirm(0).isEmpty(), "No character joins before Play");
    assertEquals("Could not read valid online profile",
        assertThrows(IllegalArgumentException.class, profile::directCredentials).getMessage());
    assertFalse(profile.toString().contains(SECRET));
  }

  @Test void malformedAndOversizedProfilesFailWithoutExposingSecretsOrPaths() throws Exception {
    Path large = write("{\"secret\":\"" + SECRET + "\",\"padding\":\"" + "x".repeat(4096) + "\"}");
    Path malformed = write("{\"secret\":\"" + SECRET + "\",\"characterId\":\"bad\"}");
    Path wrongKey = write("{\"secret\":\"" + "g".repeat(64) + "\"}");
    Path wrongType = write("{\"secret\":[\"" + SECRET + "\"]}");
    Path badUtf8 = temp.resolve("bad-utf8.json");
    Files.write(badUtf8, new byte[] {(byte) 0xc3, (byte) 0x28});
    for (Path file : new Path[] {large, malformed, wrongKey, wrongType, badUtf8}) {
      String message = assertThrows(IllegalArgumentException.class,
          () -> OnlineAccountProfile.load(file)).getMessage();
      assertEquals("Could not read valid online profile", message);
      assertFalse(message.contains(SECRET));
      assertFalse(message.contains(file.toString()));
    }
  }

  private Path write(String json) throws Exception {
    Path path = temp.resolve(UUID.randomUUID() + ".json");
    Files.writeString(path, json, StandardCharsets.UTF_8);
    return path;
  }
}
