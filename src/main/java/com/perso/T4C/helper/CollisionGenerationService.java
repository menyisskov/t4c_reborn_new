package com.perso.T4C.helper;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.config.Paths;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.CollisionRuleBinaryIO.CollisionNameRule;
import com.perso.T4C.helper.CollisionRuleBinaryIO.CollisionRule;
import com.perso.T4C.helper.CollisionRuleBinaryIO.CollisionRules;
import com.perso.T4C.objects.ObjectPos;
import com.perso.T4C.render.ObjectMapping;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CollisionGenerationService {
  private static final int COLLISION_VALUE_RED = 1;

  private CollisionGenerationService() {}

  public static Result regenerate(File mapFile, Map<String, SpriteMeta> spriteMetaByLowerName)
      throws IOException, GameException {
    if (mapFile == null || !mapFile.exists()) {
      throw new IOException(
          "Map file not found: " + (mapFile == null ? "null" : mapFile.getPath()));
    }
    CollisionRules rules =
        new File(Paths.COLLISION_RULES_BIN).exists()
            ? CollisionRuleBinaryIO.read(new File(Paths.COLLISION_RULES_BIN))
            : new CollisionRules();
    try (MapReader reader = new MapReader(mapFile, true)) {
      byte[] data = new byte[reader.getWidth() * reader.getHeight()];
      int count = generateForMap(reader, data, rules, spriteMetaByLowerName);
      count +=
          generateForObjects(
              data, reader.getWidth(), reader.getHeight(), rules, spriteMetaByLowerName);
      File output = collisionFileFor(mapFile);
      writeCollisionData(output, reader.getWidth(), reader.getHeight(), data);
      return new Result(output.getPath(), reader.getWidth(), reader.getHeight(), count);
    }
  }

  private static int generateForMap(
      MapReader reader, byte[] data, CollisionRules rules, Map<String, SpriteMeta> metaByName) {
    Set<String> ignoredSprites = normalizedIgnoredSprites(rules);
    Map<String, CollisionRule> exactRules = normalizedExactSprites(rules);
    int count = 0;
    for (int y = 0; y < reader.getHeight(); y++) {
      for (int x = 0; x < reader.getWidth(); x++) {
        Resolved resolved = resolveSprite(reader.getSpriteName(x, y), metaByName);
        if (resolved == null || resolved.name == null) {
          continue;
        }
        int originX = x;
        int originY = y;
        SpriteMeta meta =
            metaByName != null ? metaByName.get(resolved.name.toLowerCase(Locale.ROOT)) : null;
        if (meta != null) {
          originX += Math.round(reader.getOffsetXFast(x, y) / GameConstants.GRID_W);
          originY +=
              Math.round(
                  (reader.getOffsetYFast(x, y) + meta.height - GameConstants.GRID_H)
                      / GameConstants.GRID_H);
        }
        if (resolved.mirror) {
          int[] shift = getMirrorCollisionOriginShift(resolved.name, metaByName);
          originX += shift[0];
          originY += shift[1];
        }
        count +=
            applyRulesForSpriteNameAt(
                data,
                reader.getWidth(),
                reader.getHeight(),
                originX,
                originY,
                resolved.name,
                resolved.mirror,
                rules,
                ignoredSprites,
                exactRules);
      }
    }
    return count;
  }

  private static int generateForObjects(
      byte[] data,
      int width,
      int height,
      CollisionRules rules,
      Map<String, SpriteMeta> metaByName) {
    File positionsFile = new File(Paths.OBJECT_POSITIONS_BIN);
    if (!positionsFile.exists()) {
      return 0;
    }
    Map<String, ObjectMapping> mappings = new HashMap<>();
    File mappingsFile = new File(Paths.OBJECT_MAPPINGS_BIN);
    if (mappingsFile.exists()) {
      try {
        for (ObjectMappingsBinaryIO.Entry entry : ObjectMappingsBinaryIO.read(mappingsFile)) {
          if (entry != null && entry.logicalName != null && entry.mapping != null) {
            mappings.put(entry.logicalName.toUpperCase(Locale.ROOT), entry.mapping);
          }
        }
      } catch (Exception ignored) {
      }
    }
    Set<String> ignoredSprites = normalizedIgnoredSprites(rules);
    Map<String, CollisionRule> exactRules = normalizedExactSprites(rules);
    int count = 0;
    try {
      for (ObjectPos pos : ObjectPositionBinaryIO.read(positionsFile)) {
        if (pos == null) {
          continue;
        }
        int x = (int) pos.x();
        int y = (int) pos.y();
        if (x < 0 || x >= width || y < 0 || y >= height) {
          continue;
        }
        String objectName = pos.name();
        String mappedSpriteName = null;
        if (objectName != null) {
          ObjectMapping mapping = mappings.get(objectName.toUpperCase(Locale.ROOT));
          if (mapping != null) {
            mappedSpriteName = mapping.sprite;
          }
        }
        count +=
            applyRulesForSpriteNameAt(
                data, width, height, x, y, objectName, false, rules, ignoredSprites, exactRules);
        count +=
            applyRulesForSpriteNameAt(
                data,
                width,
                height,
                x,
                y,
                mappedSpriteName,
                false,
                rules,
                ignoredSprites,
                exactRules);
      }
    } catch (Exception ignored) {
    }
    return count;
  }

  private static int applyRulesForSpriteNameAt(
      byte[] data,
      int width,
      int height,
      int x,
      int y,
      String sourceName,
      boolean mirrored,
      CollisionRules rules,
      Set<String> ignoredSprites,
      Map<String, CollisionRule> exactRules) {
    if (sourceName == null || sourceName.isBlank()) {
      return 0;
    }
    String resolvedName = normalizeSpriteKey(sourceName);
    if (isIgnoredSprite(rules, resolvedName, ignoredSprites)) {
      return 0;
    }
    CollisionRule exactRule = exactRules.get(resolvedName);
    if (exactRule != null) {
      return applyCollisionRule(data, width, height, x, y, exactRule, mirrored);
    }
    int count = 0;
    if (rules.nameContainsRules != null) {
      for (CollisionNameRule nameRule : rules.nameContainsRules) {
        if (nameRule != null && matchesNameRule(nameRule, resolvedName)) {
          count += applyCollisionRule(data, width, height, x, y, nameRule.rule, mirrored);
        }
      }
    }
    return count;
  }

  private static int applyCollisionRule(
      byte[] data,
      int width,
      int height,
      int originX,
      int originY,
      CollisionRule rule,
      boolean mirrored) {
    if (rule == null) {
      return 0;
    }
    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    List<int[]> tiles = rule.tiles != null ? rule.tiles : List.of();
    List<int[]> clearTiles = rule.clearTiles != null ? rule.clearTiles : List.of();
    if (mirrored) {
      for (int[] tile : tiles) {
        if (tile != null && tile.length >= 2) {
          minX = Math.min(minX, tile[0]);
          maxX = Math.max(maxX, tile[0]);
        }
      }
      for (int[] tile : clearTiles) {
        if (tile != null && tile.length >= 2) {
          minX = Math.min(minX, tile[0]);
          maxX = Math.max(maxX, tile[0]);
        }
      }
      if (minX == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE) {
        mirrored = false;
      }
    }
    int count = 0;
    for (int[] tile : tiles) {
      if (tile == null || tile.length < 2) continue;
      int dx = mirrored ? (minX + maxX - tile[0]) : tile[0];
      count +=
          setCollisionIfEmpty(data, width, height, originX + dx, originY + tile[1], rule.value);
    }
    for (int[] tile : clearTiles) {
      if (tile == null || tile.length < 2) continue;
      int dx = mirrored ? (minX + maxX - tile[0]) : tile[0];
      clearCollision(data, width, height, originX + dx, originY + tile[1]);
    }
    return count;
  }

  private static int setCollisionIfEmpty(
      byte[] data, int width, int height, int x, int y, int value) {
    if (x < 0 || x >= width || y < 0 || y >= height) return 0;
    int index = y * width + x;
    if (data[index] != 0) return 0;
    data[index] = (byte) (value <= 0 ? COLLISION_VALUE_RED : value);
    return 1;
  }

  private static void clearCollision(byte[] data, int width, int height, int x, int y) {
    if (x < 0 || x >= width || y < 0 || y >= height) return;
    data[y * width + x] = 0;
  }

  private static void writeCollisionData(File file, int width, int height, byte[] data)
      throws IOException {
    File parent = file.getParentFile();
    if (parent != null) parent.mkdirs();
    try (OutputStream out = BinaryIOUtils.openOutputStream(file, 1 << 20)) {
      ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
      buffer.putInt(width);
      buffer.putInt(height);
      out.write(buffer.array());
      out.write(data);
    }
  }

  public static File collisionFileFor(File mapFile) {
    File parent = mapFile.getParentFile();
    String name = mapFile.getName();
    int idx = name.lastIndexOf('.');
    String baseName = idx >= 0 ? name.substring(0, idx) : name;
    return new File(parent != null ? parent : new File("."), baseName + ".colbin");
  }

  private static Resolved resolveSprite(String rawName, Map<String, SpriteMeta> metaByName) {
    if (rawName == null) return null;
    String trimmed = rawName.trim();
    if (trimmed.isEmpty()) return null;
    String base = trimmed;
    int openIdx = base.indexOf('[');
    int closeIdx = base.indexOf(']');
    if (openIdx != -1 && closeIdx > openIdx) {
      base = base.substring(0, openIdx).trim();
      trimmed = base;
    }
    boolean mirror = false;
    if (trimmed.endsWith("M")) {
      String candidate = stripTrailingInstance(trimmed.substring(0, trimmed.length() - 1).trim());
      if (!candidate.isEmpty()
          && (metaByName == null || metaByName.containsKey(candidate.toLowerCase(Locale.ROOT)))) {
        base = candidate;
        mirror = true;
      }
    }
    base = normalizeTmplBase(stripTrailingInstance(base));
    return new Resolved(base, mirror);
  }

  private static int[] getMirrorCollisionOriginShift(
      String spriteName, Map<String, SpriteMeta> metaByName) {
    if (spriteName == null || metaByName == null) return new int[] {0, 0};
    SpriteMeta meta = metaByName.get(spriteName.toLowerCase(Locale.ROOT));
    if (meta == null) return new int[] {0, 0};
    int dx = Math.round((meta.off2X - meta.off1X) / (float) GameConstants.GRID_W);
    int dy = Math.round((meta.off2Y - meta.off1Y) / (float) GameConstants.GRID_H);
    return new int[] {dx, dy};
  }

  private static Map<String, CollisionRule> normalizedExactSprites(CollisionRules rules) {
    Map<String, CollisionRule> normalized = new LinkedHashMap<>();
    if (rules.exactSprites == null) return normalized;
    for (Map.Entry<String, CollisionRule> entry : rules.exactSprites.entrySet()) {
      if (entry.getKey() != null && entry.getValue() != null) {
        normalized.put(normalizeSpriteKey(entry.getKey()), entry.getValue());
      }
    }
    return normalized;
  }

  private static Set<String> normalizedIgnoredSprites(CollisionRules rules) {
    Set<String> ignored = new HashSet<>();
    if (rules.ignoredSprites == null) return ignored;
    for (String sprite : rules.ignoredSprites) {
      if (sprite != null && !sprite.isBlank() && !sprite.contains("*")) {
        ignored.add(normalizeSpriteKey(sprite));
      }
    }
    return ignored;
  }

  private static boolean isIgnoredSprite(
      CollisionRules rules, String spriteName, Set<String> exactIgnored) {
    if (spriteName == null) return false;
    if (exactIgnored.contains(spriteName)) return true;
    if (rules.ignoredSprites == null) return false;
    for (String ignoredSprite : rules.ignoredSprites) {
      if (matchesWildcard(ignoredSprite, spriteName)) return true;
    }
    return false;
  }

  private static boolean matchesNameRule(CollisionNameRule nameRule, String spriteName) {
    if (spriteName == null || nameRule.contains == null || nameRule.rule == null) return false;
    String normalizedSprite = normalizeSpriteKey(spriteName);
    String compactSprite = compactSpriteKey(normalizedSprite);
    for (String token : nameRule.contains) {
      if (token == null || token.isBlank()) continue;
      String normalizedToken = normalizeSpriteKey(token);
      if (normalizedSprite.contains(normalizedToken)
          || compactSprite.contains(compactSpriteKey(normalizedToken))) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesWildcard(String pattern, String value) {
    if (pattern == null || pattern.isBlank() || !pattern.contains("*")) return false;
    String normalizedPattern = normalizeSpriteKey(pattern);
    StringBuilder regex = new StringBuilder();
    for (int i = 0; i < normalizedPattern.length(); i++) {
      char c = normalizedPattern.charAt(i);
      regex.append(c == '*' ? ".*" : java.util.regex.Pattern.quote(String.valueOf(c)));
    }
    return value.matches(regex.toString());
  }

  private static String normalizeSpriteKey(String value) {
    if (value == null) return "";
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "")
        .toLowerCase(Locale.ROOT)
        .replace('_', ' ')
        .replace('-', ' ')
        .trim()
        .replaceAll("\\s+", " ");
  }

  private static String compactSpriteKey(String value) {
    return value == null ? "" : value.replace(" ", "");
  }

  private static String normalizeTmplBase(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    if (!trimmed.regionMatches(true, 0, "tmpl", 0, 4)) return trimmed;
    int idx = 4;
    int start = idx;
    while (idx < trimmed.length() && trimmed.charAt(idx) == ' ') idx++;
    if (idx > start && idx < trimmed.length() && Character.isDigit(trimmed.charAt(idx))) {
      trimmed = "Tmpl" + trimmed.substring(idx);
    }
    return trimmed.replaceAll(" +", " ");
  }

  private static String stripTrailingInstance(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    int comma = trimmed.lastIndexOf(',');
    if (comma < 0 || comma == trimmed.length() - 1) return trimmed;
    int at = trimmed.lastIndexOf('@', comma);
    if (at < 0) return trimmed;
    if (isSignedInteger(trimmed, at + 1, comma)
        && isSignedInteger(trimmed, comma + 1, trimmed.length())) {
      return trimmed.substring(0, at).trim();
    }
    return trimmed;
  }

  private static boolean isSignedInteger(String value, int start, int end) {
    if (start >= end) return false;
    if (value.charAt(start) == '-') start++;
    if (start >= end) return false;
    for (int i = start; i < end; i++) {
      if (!Character.isDigit(value.charAt(i))) return false;
    }
    return true;
  }

  private record Resolved(String name, boolean mirror) {}

  public record SpriteMeta(
      String name, int width, int height, int off1X, int off1Y, int off2X, int off2Y) {}

  public record Result(String file, int width, int height, int collisionCount) {}
}
