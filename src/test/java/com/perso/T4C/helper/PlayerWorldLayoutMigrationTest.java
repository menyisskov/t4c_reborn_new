package com.perso.T4C.helper;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.player.Player;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.Test;

class PlayerWorldLayoutMigrationTest {
  // Synthetic ownership contains exactly the tested Avalon tiles, never a broad rectangle.
  private static final BiPredicate<Integer, Integer> OWNED =
      (x, y) -> (x == 1340 && y == 1477) || (x == 1344 && y == 1462);

  @Test
  void legacyJsonMigratesPositionAndPixelRespawnWithoutChangingCharacterProgress() {
    Gson gson = new Gson();
    PlayerStateDto state =
        gson.fromJson("{\"x\":1340.25,\"y\":1477.5,\"z\":0}", PlayerStateDto.class);
    state.respawnPointDefined = true;
    state.respawnWorldX = 1344.5f * GRID_W;
    state.respawnWorldY = 1462.25f * GRID_H;
    state.inventory = List.of("item.caradocs_sundered_blade");
    state.questFlags = Map.of("avalon_wilds_vigil", 2);
    state.gold = 777;
    state.currentXp = 123456L;
    state.companion = new PlayerStateDto.CompanionState();
    state.companion.speciesName = "Wolf";
    state.companion.level = 8;
    PlayerStateDto expected = gson.fromJson(gson.toJson(state), PlayerStateDto.class);
    expected.x += 2700;
    expected.respawnWorldX += 2700 * GRID_W;
    expected.worldLayoutVersion = 1;
    assertSame(state, PlayerWorldLayoutMigration.migrate(state, OWNED));
    assertEquals(
        gson.toJson(expected),
        gson.toJson(state),
        "only layout version and owned X anchors change");
  }

  @Test
  void mainLandLibraryAndOtherLayersRemainUnchangedButOwnedRespawnMovesIndependently() {
    for (float[] position :
        List.of(
            new float[] {985, 1465, 0},
            new float[] {1080, 1400, 0},
            new float[] {2000, 1000, 0},
            new float[] {1340, 1477, 1})) {
      PlayerStateDto state = new PlayerStateDto();
      state.x = position[0];
      state.y = position[1];
      state.z = (int) position[2];
      state.respawnPointDefined = true;
      state.respawnWorldX = 1344 * GRID_W;
      state.respawnWorldY = 1462 * GRID_H;
      PlayerWorldLayoutMigration.migrate(state, OWNED);
      assertEquals(position[0], state.x);
      assertEquals(position[1], state.y);
      assertEquals((int) position[2], state.z);
      assertEquals(4044 * GRID_W, state.respawnWorldX);
      assertEquals(1462 * GRID_H, state.respawnWorldY);
    }
  }

  @Test
  void unrelatedOrOtherLayerRespawnAndUndefinedAnchorArePreserved() {
    for (int scenario = 0; scenario < 3; scenario++) {
      PlayerStateDto state = new PlayerStateDto();
      state.x = 1340;
      state.y = 1477;
      state.respawnPointDefined = scenario != 2;
      state.respawnWorldX = (scenario == 0 ? 985 : 1344) * GRID_W;
      state.respawnWorldY = (scenario == 0 ? 1465 : 1462) * GRID_H;
      state.respawnWorldZ = scenario == 1 ? 1 : 0;
      float original = state.respawnWorldX;
      PlayerWorldLayoutMigration.migrate(state, OWNED);
      assertEquals(4040, state.x);
      assertEquals(original, state.respawnWorldX);
    }
  }

  @Test
  void persistedVersionPreventsMovingPlayerWhoLaterVisitsOldCoordinates() {
    PlayerStateDto state = new PlayerStateDto();
    state.x = 1340;
    state.y = 1477;
    PlayerWorldLayoutMigration.migrate(state, OWNED);
    PlayerWorldLayoutMigration.migrate(state, OWNED);
    assertEquals(4040, state.x);
    state.x = 1340;
    Gson gson = new Gson();
    PlayerStateDto reloaded = gson.fromJson(gson.toJson(state), PlayerStateDto.class);
    PlayerWorldLayoutMigration.migrate(reloaded, OWNED);
    assertEquals(1340, reloaded.x);
    assertEquals(1, reloaded.worldLayoutVersion);
  }

  @Test
  void newlyWrittenPlayerSnapshotsUseCurrentLayout() throws Exception {
    assertEquals(
        AvalonWorldLayout.WORLD_LAYOUT_VERSION,
        PlayerStateMapper.fromPlayer(new Player()).worldLayoutVersion);
  }

  @Test
  void shippedOwnershipMaskSeparatesAvalonFromLibraryAndOtherLayers() {
    for (int[] position :
        List.of(
            new int[] {1340, 1477, 0, 4040},
            new int[] {985, 1465, 0, 985},
            new int[] {1080, 1400, 0, 1080},
            new int[] {1340, 1477, 1, 1340})) {
      PlayerStateDto state = new PlayerStateDto();
      state.x = position[0];
      state.y = position[1];
      state.z = position[2];
      PlayerWorldLayoutMigration.migrate(state);
      assertEquals(position[3], state.x);
      assertEquals(position[1], state.y);
      assertEquals(position[2], state.z);
    }
  }

  @Test
  void binaryMaskUsesBitSetOrderingAndValidatesHeaderDimensionsAndSize() {
    BitSet owned = new BitSet();
    owned.set(1477 * 3072 + 1340);
    byte[] payload = owned.toByteArray();
    byte[] bytes =
        ByteBuffer.allocate(16 + payload.length)
            .put("T4CAVAL1".getBytes(StandardCharsets.US_ASCII))
            .putInt(3072)
            .putInt(3072)
            .put(payload)
            .array();
    BitSet decoded = AvalonWorldLayout.decodeLegacyMask(bytes);
    assertTrue(decoded.get(1477 * 3072 + 1340));
    assertFalse(decoded.get(1465 * 3072 + 985));
    byte[] wrongMagic = bytes.clone();
    wrongMagic[0] = 'X';
    assertThrows(
        IllegalArgumentException.class, () -> AvalonWorldLayout.decodeLegacyMask(wrongMagic));
    byte[] wrongWidth = bytes.clone();
    ByteBuffer.wrap(wrongWidth).putInt(8, 5120);
    assertThrows(
        IllegalArgumentException.class, () -> AvalonWorldLayout.decodeLegacyMask(wrongWidth));
    assertThrows(
        IllegalArgumentException.class, () -> AvalonWorldLayout.decodeLegacyMask(new byte[15]));
  }
}
