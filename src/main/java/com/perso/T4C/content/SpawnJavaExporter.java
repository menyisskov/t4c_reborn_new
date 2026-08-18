package com.perso.T4C.content;

import com.perso.T4C.spawn.SpawnDefinition;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpawnJavaExporter {
  private static final Path SOURCE_ROOT = Path.of("src/main/java/com/perso/T4C");
  private static final Pattern SPAWN_LINE =
      Pattern.compile("(?m)^@Spawn\\(type=\\\"([^\\\"]+)\\\"[^\\n]*\\)\\r?\\n?");

  private SpawnJavaExporter() {}

  public static void export(List<SpawnDefinition> definitions, boolean npc) throws IOException {
    List<SpawnDefinition> values =
        definitions == null ? List.of() : definitions.stream().filter(v -> v != null).toList();
    Map<String, List<SpawnDefinition>> byType = new LinkedHashMap<>();
    for (SpawnDefinition definition : values)
      byType.computeIfAbsent(definition.type(), ignored -> new ArrayList<>()).add(definition);
    Map<Path, List<String>> owners = locateOwners(byType.keySet());
    for (Map.Entry<Path, List<String>> entry : owners.entrySet()) {
      Path path = entry.getKey();
      String text = Files.readString(path, StandardCharsets.UTF_8);
      for (String type : entry.getValue()) {
        String escaped = Pattern.quote(type);
        text = text.replaceAll("(?m)^@Spawn\\(type=\\\"" + escaped + "\\\"[^\\n]*\\)\\r?\\n?", "");
        StringBuilder annotations = new StringBuilder();
        for (SpawnDefinition definition : byType.get(type)) {
          annotations
              .append("@Spawn(type=\\\"")
              .append(javaString(definition.type()))
              .append("\\\", x=")
              .append(definition.x())
              .append(", y=")
              .append(definition.y())
              .append(", z=")
              .append(definition.z())
              .append(", stationary=")
              .append(definition.stationary())
              .append(", aggressive=")
              .append(definition.aggressive())
              .append(
                  !npc && path.toString().replace('\\', '/').contains("/npc/")
                      ? ", kind=SpawnKind.MONSTER"
                      : "")
              .append(")\n");
        }
        text = insertBeforeClass(text, annotations.toString());
      }
      if (!text.contains("import com.perso.T4C.spawn.Spawn;")) {
        text =
            text.replaceFirst(
                "(?m)^(package [^;]+;\\r?\\n)", "$1\\nimport com.perso.T4C.spawn.Spawn;\\n");
      }
      if (!npc
          && path.toString().replace('\\', '/').contains("/npc/")
          && !text.contains("import com.perso.T4C.spawn.SpawnKind;")) {
        text =
            text.replaceFirst(
                "(?m)^(package [^;]+;\\r?\\n)", "$1\\nimport com.perso.T4C.spawn.SpawnKind;\\n");
      }
      Files.writeString(path, text, StandardCharsets.UTF_8);
    }
  }

  private static Map<Path, List<String>> locateOwners(Iterable<String> types) throws IOException {
    Map<Path, List<String>> result = new LinkedHashMap<>();
    List<Path> sources;
    try (var stream = Files.walk(SOURCE_ROOT)) {
      sources = stream.filter(path -> path.toString().endsWith(".java")).toList();
    }
    for (String type : types) {
      String marker = "@Spawn(type=\"" + type + "\"";
      for (Path source : sources) {
        if (Files.readString(source, StandardCharsets.UTF_8).contains(marker)) {
          result.computeIfAbsent(source, ignored -> new ArrayList<>()).add(type);
        }
      }
    }
    return result;
  }

  private static String insertBeforeClass(String text, String annotations) {
    Matcher matcher = Pattern.compile("(?m)^public (?:final )?class ").matcher(text);
    if (!matcher.find()) return text;
    return text.substring(0, matcher.start()) + annotations + text.substring(matcher.start());
  }

  private static String javaString(String value) {
    return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
