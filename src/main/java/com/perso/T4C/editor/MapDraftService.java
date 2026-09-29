package com.perso.T4C.editor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Creates an independently editable copy of a map and its file-backed layers. */
public final class MapDraftService {
  private static final List<String> SIDECARS =
      List.of(
          ".decorbin",
          ".colbin",
          ".musiczones.bin",
          ".musiczones.json",
          ".monsters.json",
          ".npcs.json");

  private MapDraftService() {}

  public static Path duplicate(Path sourceMap, Path mapsRoot, String name) throws IOException {
    String safeName = name == null ? "" : name.trim();
    if (!safeName.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,63}")) {
      throw new IllegalArgumentException(
          "Use 1-64 letters, digits, underscores, or hyphens; start with a letter or digit.");
    }
    Path source = sourceMap.toAbsolutePath().normalize();
    Path root = mapsRoot.toAbsolutePath().normalize();
    if (!source.startsWith(root) || !source.getFileName().toString().endsWith(".mapbin")) {
      throw new IllegalArgumentException("Source must be a map in " + root);
    }
    if (!Files.isRegularFile(source)) {
      throw new IOException("Map file not found: " + source);
    }
    Path draftsRoot = root.resolve("drafts");
    Path destination = draftsRoot.resolve(safeName);
    if (Files.exists(destination)) {
      throw new IOException("A map named " + safeName + " already exists.");
    }
    Files.createDirectories(draftsRoot);
    Path staging = Files.createTempDirectory(draftsRoot, ".new-map-");
    try {
      String sourceBase = source.getFileName().toString().replaceFirst("\\.mapbin$", "");
      Path sourceDirectory = source.getParent();
      Files.copy(source, staging.resolve(safeName + ".mapbin"));
      for (String extension : SIDECARS) {
        Path sidecar = sourceDirectory.resolve(sourceBase + extension);
        if (Files.isRegularFile(sidecar)) {
          Files.copy(sidecar, staging.resolve(safeName + extension));
        }
      }
      Files.move(staging, destination);
      return destination.resolve(safeName + ".mapbin");
    } finally {
      if (Files.exists(staging)) {
        try (var children = Files.list(staging)) {
          for (Path child : children.toList()) {
            Files.deleteIfExists(child);
          }
        }
        Files.deleteIfExists(staging);
      }
    }
  }
}
