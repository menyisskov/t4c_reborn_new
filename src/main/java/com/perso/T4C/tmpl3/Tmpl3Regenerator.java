package com.perso.T4C.tmpl3;

import com.perso.T4C.helper.MapReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Tmpl3Regenerator {
  private static final int BEST_CANDIDATE_COUNT = 5;
  private static final int REFINEMENT_PASSES = 2;
  private static final double COMPATIBILITY_WEIGHT = 0.35d;
  private final List<Tmpl3Mask> masks;
  private final TerrainResolver terrainResolver;
  private final ExpectedMaskBuilder expectedMaskBuilder = new ExpectedMaskBuilder();
  private final Tmpl3Matcher matcher;
  private final String templateName;
  private final Tmpl3DebugExporter debugExporter = new Tmpl3DebugExporter();
  private File debugOutputDir;
  private java.util.function.DoubleConsumer progressCallback;

  public Tmpl3Regenerator(List<Tmpl3Mask> masks, TerrainResolver terrainResolver) {
    this(masks, terrainResolver, "Tmpl3");
  }

  public Tmpl3Regenerator(
      List<Tmpl3Mask> masks, TerrainResolver terrainResolver, String templateName) {
    this.masks = List.copyOf(masks);
    this.terrainResolver = terrainResolver;
    this.matcher = new Tmpl3Matcher(masks);
    this.templateName = templateName;
  }

  public void setDebugOutputDir(File debugOutputDir) {
    this.debugOutputDir = debugOutputDir;
  }

  public void setProgressCallback(java.util.function.DoubleConsumer progressCallback) {
    this.progressCallback = progressCallback;
  }

  private void reportProgress(double fraction) {
    if (progressCallback != null) {
      progressCallback.accept(Math.max(0d, Math.min(1d, fraction)));
    }
  }

  public Tmpl3RegenerationResult regenerateTmpl3(MapReader map) {
    List<Tmpl3TileSnapshot> positions = collectTmpl3Tiles(map);
    return regenerateTmpl3(map, positions);
  }

  public Tmpl3RegenerationResult regenerateTmpl3(MapReader map, List<Tmpl3TileSnapshot> positions) {
    return regenerateTmpl3(map, positions, Set.of(), true);
  }

  public Tmpl3RegenerationResult regenerateTmpl3(
      MapReader map,
      List<Tmpl3TileSnapshot> positions,
      Set<Tmpl3Point> baseRemovedTmpl3,
      boolean removeAllTargets) {
    Set<Tmpl3Point> removedTmpl3 = new HashSet<>();
    if (baseRemovedTmpl3 != null) {
      removedTmpl3.addAll(baseRemovedTmpl3);
    }
    for (Tmpl3TileSnapshot position : positions) {
      if (removeAllTargets) {
        removedTmpl3.add(new Tmpl3Point(position.x(), position.y()));
      }
    }
    Map<Tmpl3Point, Tmpl3Placement> placements = new LinkedHashMap<>();
    Map<Tmpl3Point, List<Tmpl3Candidate>> candidatesByPosition = new HashMap<>();
    int analysisTotal = Math.max(1, positions.size());
    int analysisDone = 0;
    reportProgress(0d);
    for (Tmpl3TileSnapshot snapshot : positions) {
      if ((++analysisDone & 0xFF) == 0) {
        reportProgress((analysisDone / (double) analysisTotal) * 0.5d);
      }
      Tmpl3Point point = new Tmpl3Point(snapshot.x(), snapshot.y());
      TerrainContext context =
          TerrainContext.analyze(
              point.x(),
              point.y(),
              map.getWidth(),
              map.getHeight(),
              new WorkingTerrainResolver(removedTmpl3, removeAllTargets ? null : point));
      if (!context.isValid()) {
        if (log.isDebugEnabled()) {
          log.debug(
              "Tmpl3 regeneration skipped ({}, {}): invalid context {}",
              point.x(),
              point.y(),
              describeContext(context));
        }
        continue;
      }
      MaskColor[][] expectedMask = expectedMaskBuilder.build(context);
      List<Tmpl3Candidate> candidates =
          matcher.findBestCandidates(expectedMask, BEST_CANDIDATE_COUNT);
      if (candidates.isEmpty()) {
        log.debug("Tmpl3 regeneration skipped ({}, {}): no candidates", point.x(), point.y());
        continue;
      }
      candidatesByPosition.put(point, candidates);
      placements.put(point, new Tmpl3Placement(point, context, expectedMask, candidates));
    }
    reportProgress(0.5d);
    refineWithNeighborCompatibility(placements, candidatesByPosition);
    enforceTmpl4DiagonalContinuity(placements, candidatesByPosition);
    reportProgress(0.9d);
    List<Tmpl3Change> changes = new ArrayList<>(placements.size());
    int assemblyTotal = Math.max(1, placements.size());
    int assemblyDone = 0;
    for (Tmpl3Placement placement : placements.values()) {
      if ((++assemblyDone & 0xFF) == 0) {
        reportProgress(0.9d + (assemblyDone / (double) assemblyTotal) * 0.1d);
      }
      TerrainContext context = placement.context();
      Tmpl3Candidate chosen = placement.chosen();
      String newName = templateName + " " + chosen.id();
      changes.add(new Tmpl3Change(placement.point().x(), placement.point().y(), newName));
      if (log.isDebugEnabled()) {
        log.debug(
            "Tmpl3 regeneration ({}, {}): {} top={} chosen={} terrainScore={} compatScore={} finalScore={}",
            placement.point().x(),
            placement.point().y(),
            describeContext(context),
            describeCandidates(placement.candidates()),
            chosen.id(),
            chosen.terrainScore(),
            chosen.compatibilityScore(),
            chosen.finalScore());
      }
      if (debugOutputDir != null) {
        debugExporter.exportComparison(
            debugOutputDir,
            placement.point(),
            context,
            placement.expectedMask(),
            placement.candidates(),
            chosen);
      }
    }
    changes.sort(Comparator.comparingInt(Tmpl3Change::y).thenComparingInt(Tmpl3Change::x));
    return new Tmpl3RegenerationResult(
        positions.size(), List.copyOf(changes), List.copyOf(placements.values()));
  }

  private List<Tmpl3TileSnapshot> collectTmpl3Tiles(MapReader map) {
    List<Tmpl3TileSnapshot> positions = new ArrayList<>();
    for (int y = 0; y < map.getHeight(); y++) {
      for (int x = 0; x < map.getWidth(); x++) {
        if (isTemplateName(map.getSpriteName(x, y))) {
          positions.add(new Tmpl3TileSnapshot(x, y));
        }
      }
    }
    return positions;
  }

  private void refineWithNeighborCompatibility(
      Map<Tmpl3Point, Tmpl3Placement> placements,
      Map<Tmpl3Point, List<Tmpl3Candidate>> candidatesByPosition) {
    long refineTotal = Math.max(1L, (long) REFINEMENT_PASSES * placements.size());
    long refineDone = 0L;
    for (int pass = 0; pass < REFINEMENT_PASSES; pass++) {
      for (Tmpl3Placement placement : placements.values()) {
        if ((++refineDone & 0xFF) == 0) {
          reportProgress(0.5d + (refineDone / (double) refineTotal) * 0.4d);
        }
        Tmpl3Candidate best = placement.chosen();
        double bestScore = Double.MAX_VALUE;
        for (Tmpl3Candidate candidate :
            candidatesByPosition.getOrDefault(placement.point(), List.of())) {
          double compatibilityScore =
              neighborCompatibilityScore(placement.point(), candidate, placements);
          double finalScore = candidate.terrainScore() + compatibilityScore * COMPATIBILITY_WEIGHT;
          Tmpl3Candidate scored = candidate.withCompatibility(compatibilityScore, finalScore);
          if (finalScore < bestScore
              || (Double.compare(finalScore, bestScore) == 0 && scored.id() < best.id())) {
            best = scored;
            bestScore = finalScore;
          }
        }
        placement.choose(best);
      }
    }
  }

  private void enforceTmpl4DiagonalContinuity(
      Map<Tmpl3Point, Tmpl3Placement> placements,
      Map<Tmpl3Point, List<Tmpl3Candidate>> candidatesByPosition) {
    if (!"Tmpl4".equalsIgnoreCase(templateName)) {
      return;
    }
    Map<Tmpl3Point, Integer> chosenIds = new HashMap<>();
    placements.forEach((point, placement) -> chosenIds.put(point, placement.chosen().id()));
    for (Tmpl3Placement placement : placements.values()) {
      Tmpl3Point point = placement.point();
      Integer descendingId =
          matchingOppositeNeighborId(point, chosenIds, Direction.NW, Direction.SE);
      Integer ascendingId =
          matchingOppositeNeighborId(point, chosenIds, Direction.NE, Direction.SW);
      Integer continuousId =
          descendingId != null && ascendingId != null
              ? (descendingId.equals(ascendingId) ? descendingId : null)
              : (descendingId != null ? descendingId : ascendingId);
      if (continuousId == null) {
        continue;
      }
      candidatesByPosition.getOrDefault(point, List.of()).stream()
          .filter(candidate -> candidate.id() == continuousId)
          .findFirst()
          .ifPresent(placement::choose);
    }
  }

  private Integer matchingOppositeNeighborId(
      Tmpl3Point point, Map<Tmpl3Point, Integer> chosenIds, Direction first, Direction second) {
    Integer firstId = chosenIds.get(new Tmpl3Point(point.x() + first.dx, point.y() + first.dy));
    Integer secondId = chosenIds.get(new Tmpl3Point(point.x() + second.dx, point.y() + second.dy));
    return firstId != null && firstId.equals(secondId) ? firstId : null;
  }

  public double neighborCompatibilityScore(
      Tmpl3Point point, Tmpl3Candidate candidate, Map<Tmpl3Point, Tmpl3Placement> placements) {
    double score = 0d;
    for (Direction direction : Direction.values()) {
      Tmpl3Point neighborPoint = new Tmpl3Point(point.x() + direction.dx, point.y() + direction.dy);
      Tmpl3Placement neighbor = placements.get(neighborPoint);
      if (neighbor == null || neighbor.chosen() == null) {
        continue;
      }
      score +=
          profileDifference(
              candidate.mask(), direction, neighbor.chosen().mask(), direction.opposite());
    }
    return score;
  }

  private double profileDifference(
      Tmpl3Mask current,
      Direction currentDirection,
      Tmpl3Mask neighbor,
      Direction neighborDirection) {
    List<MaskColor> left = current.edgeProfile(currentDirection);
    List<MaskColor> right = neighbor.edgeProfile(neighborDirection);
    int count = Math.min(left.size(), right.size());
    double score = 0d;
    for (int i = 0; i < count; i++) {
      MaskColor a = left.get(i);
      MaskColor b = right.get(i);
      if (a == MaskColor.UNKNOWN || b == MaskColor.UNKNOWN) {
        continue;
      }
      if (a != b) {
        score += currentDirection.cardinal ? 3d : 4d;
      }
    }
    return score;
  }

  private boolean isTemplateName(String rawName) {
    return rawName != null
        && rawName.trim().regionMatches(true, 0, templateName, 0, templateName.length());
  }

  private static String describeContext(TerrainContext context) {
    return "families="
        + context.familyA()
        + "|"
        + context.familyB()
        + " terrains="
        + context.terrainA()
        + "|"
        + context.terrainB()
        + " samples="
        + context.samples();
  }

  private static String describeCandidates(List<Tmpl3Candidate> candidates) {
    return candidates.stream()
        .limit(5)
        .map(candidate -> candidate.id() + ":" + candidate.terrainScore())
        .toList()
        .toString();
  }

  private final class WorkingTerrainResolver implements TerrainResolver {
    private final Set<Tmpl3Point> removedTmpl3;
    private final Tmpl3Point additionalRemoved;

    private WorkingTerrainResolver(Set<Tmpl3Point> removedTmpl3, Tmpl3Point additionalRemoved) {
      this.removedTmpl3 = removedTmpl3;
      this.additionalRemoved = additionalRemoved;
    }

    @Override
    public String resolveTerrainName(int x, int y) {
      Tmpl3Point point = new Tmpl3Point(x, y);
      if (removedTmpl3.contains(point) || point.equals(additionalRemoved)) {
        return null;
      }
      return terrainResolver.resolveTerrainName(x, y);
    }

    @Override
    public String familyName(String terrainName) {
      return terrainResolver.familyName(terrainName);
    }

    @Override
    public String extrapolateTerrainName(String terrainName, int targetX, int targetY) {
      return terrainResolver.extrapolateTerrainName(terrainName, targetX, targetY);
    }
  }
}
