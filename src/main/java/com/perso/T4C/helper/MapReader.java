package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapReader implements AutoCloseable {
  private static final byte[] EXPECTED_MAGIC = "T4CMAP".getBytes(StandardCharsets.US_ASCII);
  private static final byte[] COMPRESSED_MAGIC = "T4CBIN".getBytes(StandardCharsets.US_ASCII);
  private static final short SUPPORTED_VERSION_V2 = 2;
  private static final short SUPPORTED_VERSION_V3 = 3;
  private static final short SUPPORTED_VERSION_V4 = 4;
  private static final short SUPPORTED_VERSION_V5 = 5;
  private static final short SUPPORTED_VERSION_V6 = 6;
  private static final int LEGACY_MIN_GENERATED_ID = 0x0350;
  @Getter private final int width;
  @Getter private final int height;
  private final String[] spriteNames;
  private final String[] decorNames;
  private final float[] scaleX;
  private final float[] scaleY;
  private final float[] offsetX;
  private final float[] offsetY;
  private final int[] zOrder;
  private final Map<String, Integer> nameToId = new HashMap<>();
  private final Map<Integer, String> idToName = new HashMap<>();
  private final boolean registerSpriteIds;
  private final boolean loadTileMetadata;

  public MapReader(File mapFile) throws GameException {
    this(mapFile, true, true, true);
  }

  public MapReader(File mapFile, boolean readOnly) throws GameException {
    this(mapFile, readOnly, true, true);
  }

  public static MapReader spriteNamesOnly(File mapFile) throws GameException {
    return new MapReader(mapFile, true, false, false);
  }

  private MapReader(
      File mapFile, boolean readOnly, boolean registerSpriteIds, boolean loadTileMetadata)
      throws GameException {
    this.registerSpriteIds = registerSpriteIds;
    this.loadTileMetadata = loadTileMetadata;
    String filePath = mapFile.getAbsolutePath();
    String binaryPath =
        filePath.endsWith(".mapbin") ? filePath : filePath.replace(".json.gz", ".mapbin");
    File binaryFile = new File(binaryPath);
    if (!binaryFile.exists()) {
      throw new GameException(
          "Binary map file not found: "
              + binaryPath
              + "\n"
              + "Expected file location: "
              + binaryFile.getAbsolutePath());
    }
    log.info(
        "Loading binary map: {} ({})",
        binaryFile.getAbsolutePath(),
        readOnly ? "READ-ONLY" : "READ-WRITE");
    long startTime = System.currentTimeMillis();
    try {
      MapData data = loadSpriteNameMap(binaryFile);
      this.width = data.width;
      this.height = data.height;
      this.spriteNames = data.spriteNames;
      this.decorNames = new String[this.spriteNames.length];
      this.scaleX = data.scaleX;
      this.scaleY = data.scaleY;
      this.offsetX = data.offsetX;
      this.offsetY = data.offsetY;
      this.zOrder = data.zOrder;
      loadDecorLayerIfPresent(binaryFile);
      long elapsed = System.currentTimeMillis() - startTime;
      log.info("Binary map loaded in {}ms ({}x{} tiles)", elapsed, width, height);
    } catch (IOException e) {
      throw new GameException("Failed to load binary map file: " + binaryFile.getAbsolutePath(), e);
    }
  }

  private MapData loadSpriteNameMap(File binaryFile) throws IOException, GameException {
    BinaryReader in = new BinaryReader(mapFile(binaryFile));
    byte[] magic = in.readBytes(EXPECTED_MAGIC.length);
    if (!Arrays.equals(magic, EXPECTED_MAGIC)) {
      throw new GameException("Invalid binary map file: wrong magic header");
    }
    short version = in.readShortLE();
    if (version != SUPPORTED_VERSION_V2
        && version != SUPPORTED_VERSION_V3
        && version != SUPPORTED_VERSION_V4
        && version != SUPPORTED_VERSION_V5
        && version != SUPPORTED_VERSION_V6) {
      throw new GameException("Unsupported binary map version: " + version);
    }
    int width = in.readIntLE();
    int height = in.readIntLE();
    if (version == SUPPORTED_VERSION_V6) {
      return loadCompactMap(in, width, height);
    }
    int tileCount = in.readIntLE();
    int total = checkedTotalTiles(width, height);
    String[] spriteNames = new String[total];
    float[] scaleX = loadTileMetadata ? new float[total] : null;
    float[] scaleY = loadTileMetadata ? new float[total] : null;
    float[] offsetX = loadTileMetadata ? new float[total] : null;
    float[] offsetY = loadTileMetadata ? new float[total] : null;
    int[] zOrder = loadTileMetadata ? new int[total] : null;
    if (loadTileMetadata) {
      Arrays.fill(scaleX, 1f);
      Arrays.fill(scaleY, 1f);
    }
    int readEntries = 0;
    for (int i = 0; i < tileCount; i++) {
      try {
        int x = in.readIntLE();
        int y = in.readIntLE();
        int nameLen = in.readIntLE();
        if (nameLen < 0) {
          throw new GameException("Invalid tile name length: " + nameLen);
        }
        String name = in.readUtf8(nameLen);
        boolean inside = x >= 0 && y >= 0 && x < width && y < height;
        int idx = inside ? y * width + x : -1;
        if (inside) {
          spriteNames[idx] = name;
          if (registerSpriteIds) {
            registerNameFast(name);
          }
        }
        if (version >= SUPPORTED_VERSION_V3) {
          float sx = in.readFloatLE();
          float sy = in.readFloatLE();
          if (inside && loadTileMetadata) {
            scaleX[idx] = sanitizeScale(sx);
            scaleY[idx] = sanitizeScale(sy);
          }
        }
        if (version >= SUPPORTED_VERSION_V4) {
          float ox = in.readFloatLE();
          float oy = in.readFloatLE();
          if (inside && loadTileMetadata) {
            offsetX[idx] = ox;
            offsetY[idx] = oy;
          }
        }
        if (version >= SUPPORTED_VERSION_V5) {
          int zo = in.readIntLE();
          if (inside && loadTileMetadata) {
            zOrder[idx] = zo;
          }
        }
        readEntries++;
      } catch (EOFException eof) {
        log.warn(
            "Reached EOF after {} entries (expected {}). Map file may be truncated.",
            readEntries,
            tileCount);
        break;
      }
    }
    return new MapData(width, height, spriteNames, scaleX, scaleY, offsetX, offsetY, zOrder);
  }

  private void loadDecorLayerIfPresent(File mapFile) throws IOException, GameException {
    File decorFile = decorFileFor(mapFile);
    if (!decorFile.exists()) {
      return;
    }
    MapData decorData = loadSpriteNameMap(decorFile);
    if (decorData.width != width || decorData.height != height) {
      throw new GameException(
          "Decor layer dimensions do not match map: " + decorFile.getAbsolutePath());
    }
    System.arraycopy(decorData.spriteNames, 0, decorNames, 0, decorNames.length);
    if (loadTileMetadata && decorData.scaleX != null) {
      for (int i = 0; i < decorNames.length; i++) {
        if (decorNames[i] == null || decorNames[i].isBlank()) {
          continue;
        }
        scaleX[i] = decorData.scaleX[i];
        scaleY[i] = decorData.scaleY[i];
        offsetX[i] = decorData.offsetX[i];
        offsetY[i] = decorData.offsetY[i];
        zOrder[i] = decorData.zOrder[i];
      }
    }
  }

  private MapData loadCompactMap(BinaryReader in, int width, int height)
      throws EOFException, GameException {
    int total = checkedTotalTiles(width, height);
    String[] spriteNames = new String[total];
    float[] scaleX = loadTileMetadata ? new float[total] : null;
    float[] scaleY = loadTileMetadata ? new float[total] : null;
    float[] offsetX = loadTileMetadata ? new float[total] : null;
    float[] offsetY = loadTileMetadata ? new float[total] : null;
    int[] zOrder = loadTileMetadata ? new int[total] : null;
    if (loadTileMetadata) {
      Arrays.fill(scaleX, 1f);
      Arrays.fill(scaleY, 1f);
    }
    int dictionaryCount = in.readIntLE();
    if (dictionaryCount < 0) {
      throw new GameException("Invalid map dictionary count: " + dictionaryCount);
    }
    String[] dictionary = new String[dictionaryCount + 1];
    for (int i = 1; i <= dictionaryCount; i++) {
      int nameLen = in.readIntLE();
      if (nameLen <= 0) {
        throw new GameException("Invalid dictionary sprite name length: " + nameLen);
      }
      String name = in.readUtf8(nameLen);
      dictionary[i] = name;
      if (registerSpriteIds) {
        registerNameFast(name);
      }
    }
    int runCount = in.readIntLE();
    if (runCount < 0) {
      throw new GameException("Invalid map run count: " + runCount);
    }
    for (int i = 0; i < runCount; i++) {
      int startIndex = in.readIntLE();
      int runLength = in.readIntLE();
      int spriteId = in.readIntLE();
      if (runLength < 0 || startIndex < 0 || startIndex > total || startIndex + runLength > total) {
        throw new GameException("Invalid map run: start=" + startIndex + ", length=" + runLength);
      }
      if (spriteId < 0 || spriteId >= dictionary.length) {
        throw new GameException("Invalid map sprite id: " + spriteId);
      }
      if (spriteId != 0) {
        Arrays.fill(spriteNames, startIndex, startIndex + runLength, dictionary[spriteId]);
      }
    }
    readScaleOverrides(in, total, scaleX, scaleY);
    readOffsetOverrides(in, total, offsetX, offsetY);
    readZOrderOverrides(in, total, zOrder);
    return new MapData(width, height, spriteNames, scaleX, scaleY, offsetX, offsetY, zOrder);
  }

  private void readScaleOverrides(BinaryReader in, int total, float[] scaleX, float[] scaleY)
      throws EOFException, GameException {
    int count = in.readIntLE();
    if (count < 0) {
      throw new GameException("Invalid scale override count: " + count);
    }
    for (int i = 0; i < count; i++) {
      int idx = readTileIndex(in, total);
      float sx = in.readFloatLE();
      float sy = in.readFloatLE();
      if (loadTileMetadata) {
        scaleX[idx] = sanitizeScale(sx);
        scaleY[idx] = sanitizeScale(sy);
      }
    }
  }

  private void readOffsetOverrides(BinaryReader in, int total, float[] offsetX, float[] offsetY)
      throws EOFException, GameException {
    int count = in.readIntLE();
    if (count < 0) {
      throw new GameException("Invalid offset override count: " + count);
    }
    for (int i = 0; i < count; i++) {
      int idx = readTileIndex(in, total);
      float ox = in.readFloatLE();
      float oy = in.readFloatLE();
      if (loadTileMetadata) {
        offsetX[idx] = ox;
        offsetY[idx] = oy;
      }
    }
  }

  private void readZOrderOverrides(BinaryReader in, int total, int[] zOrder)
      throws EOFException, GameException {
    int count = in.readIntLE();
    if (count < 0) {
      throw new GameException("Invalid z-order override count: " + count);
    }
    for (int i = 0; i < count; i++) {
      int idx = readTileIndex(in, total);
      int value = in.readIntLE();
      if (loadTileMetadata) {
        zOrder[idx] = value;
      }
    }
  }

  private static int readTileIndex(BinaryReader in, int total) throws EOFException, GameException {
    int idx = in.readIntLE();
    if (idx < 0 || idx >= total) {
      throw new GameException("Invalid tile index: " + idx);
    }
    return idx;
  }

  public void writeCompact(File outputFile) throws IOException {
    writeCompactLayer(outputFile, spriteNames);
    writeCompactLayer(decorFileFor(outputFile), decorNames);
  }

  private void writeCompactLayer(File outputFile, String[] names) throws IOException {
    if (!loadTileMetadata
        || scaleX == null
        || scaleY == null
        || offsetX == null
        || offsetY == null
        || zOrder == null) {
      throw new IOException("Cannot write compact map from a metadata-light MapReader");
    }
    int total = totalTilesUnchecked(width, height);
    Map<String, Integer> dictionary = buildDictionary(names);
    int runCount = countRuns(names, dictionary);
    int scaleOverrideCount = 0;
    int offsetOverrideCount = 0;
    int zOrderOverrideCount = 0;
    for (int i = 0; i < total; i++) {
      if (Float.floatToIntBits(scaleX[i]) != Float.floatToIntBits(1f)
          || Float.floatToIntBits(scaleY[i]) != Float.floatToIntBits(1f)) {
        scaleOverrideCount++;
      }
      if (Float.floatToIntBits(offsetX[i]) != 0 || Float.floatToIntBits(offsetY[i]) != 0) {
        offsetOverrideCount++;
      }
      if (zOrder[i] != 0) {
        zOrderOverrideCount++;
      }
    }
    try (DataOutputStream out =
        new DataOutputStream(BinaryIOUtils.openOutputStream(outputFile, 1 << 20))) {
      out.write(EXPECTED_MAGIC);
      writeShortLE(out, SUPPORTED_VERSION_V6);
      writeIntLE(out, width);
      writeIntLE(out, height);
      writeIntLE(out, dictionary.size());
      for (String name : dictionary.keySet()) {
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        writeIntLE(out, bytes.length);
        out.write(bytes);
      }
      writeIntLE(out, runCount);
      writeRuns(out, names, dictionary);
      writeIntLE(out, scaleOverrideCount);
      for (int i = 0; i < total; i++) {
        if (Float.floatToIntBits(scaleX[i]) != Float.floatToIntBits(1f)
            || Float.floatToIntBits(scaleY[i]) != Float.floatToIntBits(1f)) {
          writeIntLE(out, i);
          writeFloatLE(out, scaleX[i]);
          writeFloatLE(out, scaleY[i]);
        }
      }
      writeIntLE(out, offsetOverrideCount);
      for (int i = 0; i < total; i++) {
        if (Float.floatToIntBits(offsetX[i]) != 0 || Float.floatToIntBits(offsetY[i]) != 0) {
          writeIntLE(out, i);
          writeFloatLE(out, offsetX[i]);
          writeFloatLE(out, offsetY[i]);
        }
      }
      writeIntLE(out, zOrderOverrideCount);
      for (int i = 0; i < total; i++) {
        if (zOrder[i] != 0) {
          writeIntLE(out, i);
          writeIntLE(out, zOrder[i]);
        }
      }
    }
  }

  private Map<String, Integer> buildDictionary(String[] names) {
    Map<String, Integer> dictionary = new LinkedHashMap<>();
    for (String name : names) {
      if (name == null || name.isBlank() || dictionary.containsKey(name)) {
        continue;
      }
      dictionary.put(name, dictionary.size() + 1);
    }
    return dictionary;
  }

  private int countRuns(String[] names, Map<String, Integer> dictionary) {
    int total = totalTilesUnchecked(width, height);
    if (total == 0) {
      return 0;
    }
    int runs = 0;
    int previousId = -1;
    for (int i = 0; i < total; i++) {
      int id = spriteIdAt(names, i, dictionary);
      if (i == 0 || id != previousId) {
        runs++;
        previousId = id;
      }
    }
    return runs;
  }

  private void writeRuns(DataOutputStream out, String[] names, Map<String, Integer> dictionary)
      throws IOException {
    int total = totalTilesUnchecked(width, height);
    int runStart = 0;
    int previousId = -1;
    for (int i = 0; i <= total; i++) {
      int id = i < total ? spriteIdAt(names, i, dictionary) : -1;
      if (i == 0) {
        previousId = id;
        continue;
      }
      if (id != previousId) {
        writeIntLE(out, runStart);
        writeIntLE(out, i - runStart);
        writeIntLE(out, previousId);
        runStart = i;
        previousId = id;
      }
    }
  }

  private int spriteIdAt(String[] names, int index, Map<String, Integer> dictionary) {
    String name = names[index];
    if (name == null || name.isBlank()) {
      return 0;
    }
    Integer id = dictionary.get(name);
    if (id == null) {
      throw new IllegalStateException("Sprite missing from dictionary: " + name);
    }
    return id;
  }

  private static int checkedTotalTiles(int width, int height) throws GameException {
    try {
      return Math.multiplyExact(width, height);
    } catch (ArithmeticException e) {
      throw new GameException("Invalid map dimensions: " + width + "x" + height, e);
    }
  }

  private static int totalTilesUnchecked(int width, int height) {
    return Math.multiplyExact(width, height);
  }

  private static ByteBuffer mapFile(File file) throws IOException {
    if (isCompressed(file)) {
      try (InputStream in = BinaryIOUtils.openInputStream(file, 1 << 20)) {
        return ByteBuffer.wrap(in.readAllBytes());
      }
    }
    try (FileChannel ch = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
      return ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
    }
  }

  private static boolean isCompressed(File file) throws IOException {
    try (InputStream in = new FileInputStream(file)) {
      byte[] head = in.readNBytes(COMPRESSED_MAGIC.length);
      return Arrays.equals(head, COMPRESSED_MAGIC);
    }
  }

  public boolean isInside(int x, int y) {
    return x < 0 || y < 0 || x >= width || y >= height;
  }

  public boolean usesSpriteNames() {
    return true;
  }

  public int get(int x, int y) {
    String name = getSpriteName(x, y);
    return resolveOrRegisterId(name);
  }

  public void set(int x, int y, int tileId) {
    String name = idToName.get(tileId);
    if (name == null) {
      throw new IllegalArgumentException("Unknown tile id: " + tileId);
    }
    setSpriteName(x, y, name);
  }

  public String getSpriteName(int x, int y) {
    int idx = indexOf(x, y);
    String decor = decorNames[idx];
    if (decor != null && !decor.isBlank()) {
      return decor;
    }
    return spriteNames[idx];
  }

  public String getGroundSpriteName(int x, int y) {
    return spriteNames[indexOf(x, y)];
  }

  public String getDecorSpriteName(int x, int y) {
    return decorNames[indexOf(x, y)];
  }

  public void setSpriteName(int x, int y, String spriteName) {
    int idx = indexOf(x, y);
    spriteNames[idx] = spriteName;
    decorNames[idx] = null;
    registerName(spriteName);
  }

  public void setGroundSpriteName(int x, int y, String spriteName) {
    int idx = indexOf(x, y);
    spriteNames[idx] = spriteName;
    registerName(spriteName);
  }

  public void setDecorSpriteName(int x, int y, String spriteName) {
    int idx = indexOf(x, y);
    decorNames[idx] = spriteName;
    registerName(spriteName);
  }

  public void setSpriteNameFast(int x, int y, String spriteName) {
    setSpriteName(x, y, spriteName);
  }

  public float getScaleX(int x, int y) {
    return scaleX[indexOf(x, y)];
  }

  public float getScaleY(int x, int y) {
    return scaleY[indexOf(x, y)];
  }

  public float getScaleXFast(int x, int y) {
    return scaleX[y * width + x];
  }

  public float getScaleYFast(int x, int y) {
    return scaleY[y * width + x];
  }

  public void setScale(int x, int y, float newScaleX, float newScaleY) {
    int idx = indexOf(x, y);
    scaleX[idx] = sanitizeScale(newScaleX);
    scaleY[idx] = sanitizeScale(newScaleY);
  }

  public float getOffsetX(int x, int y) {
    return offsetX[indexOf(x, y)];
  }

  public float getOffsetY(int x, int y) {
    return offsetY[indexOf(x, y)];
  }

  public float getOffsetXFast(int x, int y) {
    return offsetX[y * width + x];
  }

  public float getOffsetYFast(int x, int y) {
    return offsetY[y * width + x];
  }

  public void setOffset(int x, int y, float newOffsetX, float newOffsetY) {
    int idx = indexOf(x, y);
    offsetX[idx] = newOffsetX;
    offsetY[idx] = newOffsetY;
  }

  public int getZOrder(int x, int y) {
    return zOrder[indexOf(x, y)];
  }

  public int getZOrderFast(int x, int y) {
    return zOrder[y * width + x];
  }

  public void setZOrder(int x, int y, int newZOrder) {
    zOrder[indexOf(x, y)] = newZOrder;
  }

  @Override
  public void close() {}

  private void registerName(String spriteName) {
    if (spriteName == null || spriteName.isEmpty()) return;
    Integer id = nameToId.get(spriteName);
    if (id != null) {
      return;
    }
    id = generateIdForName(spriteName);
    nameToId.put(spriteName, id);
    idToName.putIfAbsent(id, spriteName);
  }

  private void registerNameFast(String spriteName) {
    if (spriteName == null || spriteName.isEmpty()) return;
    if (nameToId.containsKey(spriteName)) return;
    int id = generateIdForName(spriteName);
    if (nameToId.putIfAbsent(spriteName, id) == null) {
      idToName.putIfAbsent(id, spriteName);
    }
  }

  private int resolveOrRegisterId(String spriteName) {
    if (spriteName == null || spriteName.isEmpty()) return 0;
    Integer id = nameToId.get(spriteName);
    if (id != null) return id;
    id = generateIdForName(spriteName);
    nameToId.put(spriteName, id);
    idToName.putIfAbsent(id, spriteName);
    return id;
  }

  private int generateIdForName(String spriteName) {
    int id = spriteName.hashCode() & 0x7FFFFFFF;
    if (id < LEGACY_MIN_GENERATED_ID) {
      id += LEGACY_MIN_GENERATED_ID;
    }
    while (idToName.containsKey(id)) {
      id++;
      if (id < 0) {
        id = LEGACY_MIN_GENERATED_ID;
      }
    }
    return id;
  }

  private static float sanitizeScale(float value) {
    if (!Float.isFinite(value) || value <= 0f) {
      return 1f;
    }
    return value;
  }

  private int indexOf(int x, int y) {
    if (x < 0 || y < 0 || x >= width || y >= height) {
      throw new IndexOutOfBoundsException("Out of bounds: (" + x + "," + y + ")");
    }
    return y * width + x;
  }

  public static File decorFileFor(File mapFile) {
    String path = mapFile.getAbsolutePath();
    if (path.endsWith(".mapbin")) {
      return new File(path.substring(0, path.length() - ".mapbin".length()) + ".decorbin");
    }
    return new File(path + ".decorbin");
  }

  private record MapData(
      int width,
      int height,
      String[] spriteNames,
      float[] scaleX,
      float[] scaleY,
      float[] offsetX,
      float[] offsetY,
      int[] zOrder) {}

  private static final class BinaryReader {
    private final ByteBuffer buf;
    private final byte[] tmpBytes = new byte[256];

    private BinaryReader(ByteBuffer buf) {
      this.buf = buf.order(ByteOrder.LITTLE_ENDIAN);
    }

    private byte[] readBytes(int length) throws EOFException {
      ensureAvailable(length);
      byte[] bytes = new byte[length];
      buf.get(bytes);
      return bytes;
    }

    private String readUtf8(int length) throws EOFException {
      ensureAvailable(length);
      if (length == 0) {
        return null;
      }
      byte[] bytes = length <= tmpBytes.length ? tmpBytes : new byte[length];
      buf.get(bytes, 0, length);
      return new String(bytes, 0, length, StandardCharsets.UTF_8).intern();
    }

    private short readShortLE() throws EOFException {
      ensureAvailable(2);
      return buf.getShort();
    }

    private int readIntLE() throws EOFException {
      ensureAvailable(4);
      return buf.getInt();
    }

    private float readFloatLE() throws EOFException {
      ensureAvailable(4);
      return buf.getFloat();
    }

    private void ensureAvailable(int length) throws EOFException {
      if (length < 0 || buf.remaining() < length) {
        throw new EOFException("Unexpected EOF while reading binary map");
      }
    }
  }

  private static void writeShortLE(DataOutputStream out, short value) throws IOException {
    out.writeByte(value & 0xFF);
    out.writeByte((value >>> 8) & 0xFF);
  }

  private static void writeIntLE(DataOutputStream out, int value) throws IOException {
    out.writeByte(value & 0xFF);
    out.writeByte((value >>> 8) & 0xFF);
    out.writeByte((value >>> 16) & 0xFF);
    out.writeByte((value >>> 24) & 0xFF);
  }

  private static void writeFloatLE(DataOutputStream out, float value) throws IOException {
    writeIntLE(out, Float.floatToIntBits(value));
  }
}
