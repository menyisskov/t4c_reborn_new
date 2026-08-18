package com.perso.T4C.helper;

import com.perso.T4C.config.Paths;
import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GroundMosaicCatalog {
  private static final Logger log = LoggerFactory.getLogger(GroundMosaicCatalog.class);
  private static List<GroundMosaicBinaryIO.Definition> cachedDefinitions;
  private static final Pattern COMPACT_COORDINATES =
      Pattern.compile("^(Dtm|DungeonFloorTorch)(\\d+)\\s+(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern GRID_COORDINATES =
      Pattern.compile(
          "^(.+?)\\(\\s*(?:\\d+|&x)\\s*,\\s*(?:\\d+|&y)\\s*\\)\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern NUMERIC_VARIANT =
      Pattern.compile("^(.*?)\\s+\\d+M?$", Pattern.CASE_INSENSITIVE);
  private final Map<String, List<MosaicPattern>> patternsByBase;
  private final Map<String, List<MosaicPattern>> patternsByFrame;
  private final Map<String, String> baseByFrame;
  private final Set<String> baseNames;

  private GroundMosaicCatalog(List<MosaicPattern> patterns) {
    Map<String, List<MosaicPattern>> byBase = new LinkedHashMap<>();
    Map<String, List<MosaicPattern>> byFramePatterns = new LinkedHashMap<>();
    Map<String, String> byFrame = new LinkedHashMap<>();
    Set<String> bases = new LinkedHashSet<>();
    for (MosaicPattern pattern : patterns) {
      for (String base : pattern.familyCounts().keySet()) {
        byBase.computeIfAbsent(key(base), ignored -> new ArrayList<>()).add(pattern);
        bases.add(base);
      }
      for (String frame : pattern.frames()) {
        String base = familyName(frame);
        if (base != null) {
          byFrame.putIfAbsent(key(frame), base);
        }
        if (!pattern.coordinateTemplate()) {
          byFramePatterns.computeIfAbsent(key(frame), ignored -> new ArrayList<>()).add(pattern);
        }
      }
    }
    byBase.replaceAll((ignored, value) -> List.copyOf(value));
    byFramePatterns.replaceAll((ignored, value) -> List.copyOf(value));
    this.patternsByBase = Collections.unmodifiableMap(byBase);
    this.patternsByFrame = Collections.unmodifiableMap(byFramePatterns);
    this.baseByFrame = Collections.unmodifiableMap(byFrame);
    this.baseNames = Collections.unmodifiableSet(bases);
  }

  public static GroundMosaicCatalog load(Collection<String> availableSpriteNames) {
    Map<String, String> actualNames = new LinkedHashMap<>();
    if (availableSpriteNames != null) {
      for (String name : availableSpriteNames) {
        if (name != null && !name.isBlank()) {
          actualNames.putIfAbsent(normalizedName(name), name);
        }
      }
    }
    List<MosaicPattern> patterns =
        definitions().stream()
            .map(definition -> pattern(definition, actualNames))
            .filter(Objects::nonNull)
            .toList();
    return new GroundMosaicCatalog(patterns);
  }

  public static GroundMosaicCatalog load() {
    return load(List.of());
  }

  public static synchronized void invalidate() {
    cachedDefinitions = null;
  }

  private static synchronized List<GroundMosaicBinaryIO.Definition> definitions() {
    if (cachedDefinitions != null) {
      return cachedDefinitions;
    }
    List<GroundMosaicBinaryIO.Definition> definitions = List.of();
    File file = new File(Paths.GROUND_MOSAICS_BIN);
    if (file.exists()) {
      try {
        definitions = GroundMosaicBinaryIO.read(file);
      } catch (Exception e) {
        log.warn("Could not load ground mosaics from {}: {}", file.getPath(), e.getMessage());
      }
    } else {
      log.warn("Missing ground mosaic catalog: {}", file.getPath());
    }
    cachedDefinitions = definitions;
    return cachedDefinitions;
  }

  public Set<String> baseNames() {
    return baseNames;
  }

  public String baseNameForFrame(String frameName) {
    if (frameName == null || frameName.isBlank()) {
      return null;
    }
    String exact = baseByFrame.get(key(frameName));
    if (exact != null) {
      return exact;
    }
    String family = familyName(frameName);
    return family != null && patternsByBase.containsKey(key(family)) ? family : null;
  }

  public Dimensions dimensions(String base) {
    MosaicPattern pattern = bestPattern(base);
    return pattern == null ? null : new Dimensions(pattern.width(), pattern.height());
  }

  public String tileName(String base, int x, int y) {
    MosaicPattern pattern = bestPattern(base);
    return pattern == null ? null : pattern.tileName(x, y);
  }

  public String tileNameForExistingFrame(String frameName, int x, int y, TileLookup tiles) {
    if (frameName == null || frameName.isBlank()) {
      return null;
    }
    List<MosaicPattern> candidates = patternsByFrame.get(key(frameName));
    if (candidates == null || candidates.isEmpty()) {
      return tileName(baseNameForFrame(frameName), x, y);
    }
    if (candidates.size() == 1) {
      return candidates.get(0).tileName(x, y);
    }
    MosaicPattern best = null;
    int bestScore = -1;
    int bestFamilyScore = -1;
    String family = baseNameForFrame(frameName);
    for (MosaicPattern candidate : candidates) {
      int score = candidate.neighbourhoodScore(x, y, tiles);
      int familyScore = candidate.familyScore(family);
      if (score > bestScore || (score == bestScore && familyScore > bestFamilyScore)) {
        best = candidate;
        bestScore = score;
        bestFamilyScore = familyScore;
      }
    }
    return best == null ? null : best.tileName(x, y);
  }

  private MosaicPattern bestPattern(String base) {
    if (base == null || base.isBlank()) {
      return null;
    }
    List<MosaicPattern> candidates = patternsByBase.get(key(base));
    if (candidates == null || candidates.isEmpty()) {
      return null;
    }
    MosaicPattern best = null;
    int bestScore = -1;
    for (MosaicPattern candidate : candidates) {
      int score = candidate.familyScore(base);
      if (score > bestScore) {
        best = candidate;
        bestScore = score;
      }
    }
    return best;
  }

  private static MosaicPattern pattern(
      GroundMosaicBinaryIO.Definition definition, Map<String, String> actualNames) {
    if (definition.width() <= 0 || definition.height() <= 0 || definition.frames().isEmpty()) {
      log.warn(
          "Skipping ground mosaic {}: invalid {}x{} block with {} frame(s)",
          definition.id(),
          definition.width(),
          definition.height(),
          definition.frames().size());
      return null;
    }
    int expectedFrames = definition.width() * definition.height();
    boolean coordinateTemplate =
        definition.frames().size() == 1
            && (definition.frames().get(0).contains("&x")
                || definition.frames().get(0).contains("&y"));
    if (!coordinateTemplate && definition.frames().size() != expectedFrames) {
      log.warn(
          "Skipping ground mosaic {}: expected {} frames, got {}",
          definition.id(),
          expectedFrames,
          definition.frames().size());
      return null;
    }
    List<String> frames =
        definition.frames().stream()
            .map(
                name ->
                    coordinateTemplate
                        ? name
                        : actualNames.getOrDefault(normalizedName(name), name))
            .toList();
    Map<String, Integer> familyCounts = new LinkedHashMap<>();
    for (String frame : frames) {
      String familySource = frame;
      if (coordinateTemplate) {
        String sampleName = frame.replace("&x", "1").replace("&y", "1");
        familySource = actualNames.getOrDefault(normalizedName(sampleName), sampleName);
      }
      String family = familyName(familySource);
      if (family != null) {
        familyCounts.merge(family, 1, Integer::sum);
      }
    }
    return new MosaicPattern(
        definition.id(),
        definition.width(),
        definition.height(),
        frames,
        coordinateTemplate,
        Map.copyOf(familyCounts),
        actualNames);
  }

  private static String familyName(String name) {
    if (name == null || name.isBlank()) {
      return null;
    }
    String trimmed = name.trim();
    Matcher compact = COMPACT_COORDINATES.matcher(trimmed);
    if (compact.matches()) {
      return compact.group(1).equalsIgnoreCase("Dtm") ? "Dtm" : "DungeonFloorTorch";
    }
    Matcher grid = GRID_COORDINATES.matcher(trimmed);
    if (grid.matches()) {
      return grid.group(1).trim();
    }
    Matcher numeric = NUMERIC_VARIANT.matcher(trimmed);
    return numeric.matches() && !numeric.group(1).isBlank() ? numeric.group(1).trim() : trimmed;
  }

  private static String normalizedName(String value) {
    String ascii = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    return ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
  }

  private static String key(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
  }

  public record Dimensions(int width, int height) {}

  @FunctionalInterface
  public interface TileLookup {
    String get(int x, int y);
  }

  private record MosaicPattern(
      String id,
      int width,
      int height,
      List<String> frames,
      boolean coordinateTemplate,
      Map<String, Integer> familyCounts,
      Map<String, String> actualNames) {
    String tileName(int x, int y) {
      int tileX = Math.floorMod(x, width);
      int tileY = Math.floorMod(y, height);
      String name;
      if (coordinateTemplate) {
        name =
            frames
                .get(0)
                .replace("&x", Integer.toString(tileX + 1))
                .replace("&y", Integer.toString(tileY + 1));
        return actualNames.getOrDefault(normalizedName(name), name);
      }
      name = frames.get(tileX * height + tileY);
      return name;
    }

    int neighbourhoodScore(int x, int y, TileLookup tiles) {
      if (tiles == null) {
        return 0;
      }
      int score = 0;
      for (int dy = -(height / 2); dy <= height / 2; dy++) {
        for (int dx = -(width / 2); dx <= width / 2; dx++) {
          String actual = tiles.get(x + dx, y + dy);
          String expected = tileName(x + dx, y + dy);
          if (actual != null && actual.equalsIgnoreCase(expected)) {
            score++;
          }
        }
      }
      return score;
    }

    int familyScore(String family) {
      if (family == null) {
        return 0;
      }
      return familyCounts.entrySet().stream()
          .filter(entry -> entry.getKey().equalsIgnoreCase(family))
          .mapToInt(Map.Entry::getValue)
          .findFirst()
          .orElse(0);
    }
  }
}
