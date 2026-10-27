package com.perso.T4C.online;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Private account profile for either an existing character or a fresh account's lobby. */
public final class OnlineAccountProfile {
  private static final int MAX_BYTES = 4096;
  private final String secret;
  private final UUID preferredCharacterId;

  private OnlineAccountProfile(String secret, UUID preferredCharacterId) {
    if (secret == null || !secret.matches("[0-9a-f]{64}")) throw invalid();
    this.secret = secret;
    this.preferredCharacterId = preferredCharacterId;
  }

  /** Reads at most 4097 bytes; every failure has the same credential-free message. */
  public static OnlineAccountProfile load(Path path) {
    try (var input = Files.newInputStream(path)) {
      byte[] bytes = input.readNBytes(MAX_BYTES + 1);
      if (bytes.length > MAX_BYTES) throw invalid();
      String json = StandardCharsets.UTF_8.newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(bytes)).toString();
      var root = JsonParser.parseString(json).getAsJsonObject();
      if (!root.has("secret") || !root.get("secret").isJsonPrimitive()
          || !root.getAsJsonPrimitive("secret").isString()) throw invalid();
      String secret = root.get("secret").getAsString();
      UUID preferred = null;
      if (root.has("characterId") && !root.get("characterId").isJsonNull()) {
        if (!root.get("characterId").isJsonPrimitive()
            || !root.getAsJsonPrimitive("characterId").isString()) throw invalid();
        preferred = UUID.fromString(root.get("characterId").getAsString());
      }
      return new OnlineAccountProfile(secret, preferred);
    } catch (IOException failure) {
      throw invalid();
    } catch (RuntimeException failure) {
      throw invalid();
    }
  }

  public String secret() { return secret; }

  public OnlineLobbyModel lobbyModel() { return new OnlineLobbyModel(secret, preferredCharacterId); }

  /** Direct launch remains character-bound; a fresh account must explicitly request the lobby. */
  public OnlineConnection.Credentials directCredentials() {
    if (preferredCharacterId == null) throw invalid();
    return new OnlineConnection.Credentials(secret, preferredCharacterId);
  }

  @Override public String toString() {
    return "OnlineAccountProfile[secret=REDACTED, preferredCharacterId=" + preferredCharacterId + "]";
  }

  private static IllegalArgumentException invalid() {
    return new IllegalArgumentException("Could not read valid online profile");
  }
}
