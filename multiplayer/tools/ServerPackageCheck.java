import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

/** Headless integrity check for a local QA server distribution. */
public final class ServerPackageCheck {
  private ServerPackageCheck() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Usage: ServerPackageCheck PACKAGE_DIRECTORY");
    check(Path.of(args[0]));
    System.out.println("Server package integrity OK");
  }

  static void check(Path packageRoot) throws Exception {
    if (Runtime.version().feature() < 21) throw new IOException("Java 21 or newer is required");
    Path root = packageRoot.toAbsolutePath().normalize();
    if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(root))
      throw new IOException("Package directory is missing or linked");
    Path manifest = root.resolve("SHA256SUMS.tsv");
    if (!Files.isRegularFile(manifest, LinkOption.NOFOLLOW_LINKS))
      throw new IOException("Package manifest is missing or linked");
    List<String> lines = Files.readAllLines(manifest, StandardCharsets.UTF_8);
    if (lines.size() < 3 || !lines.getFirst().equals("T4C-ONLINE-SERVER-PACKAGE\t1"))
      throw new IOException("Invalid server manifest header");
    Map<String, String> facts = new HashMap<>();
    Set<String> listed = new HashSet<>();
    Set<String> folded = new HashSet<>();
    for (String line : lines.subList(1, lines.size())) {
      String[] columns = line.split("\t", -1);
      if (columns.length != 3) throw new IOException("Invalid manifest entry");
      if (columns[0].equals("fact")) {
        if (!columns[1].matches("[A-Za-z][A-Za-z0-9]*")
            || !columns[2].matches("[A-Za-z0-9._-]+")
            || facts.putIfAbsent(columns[1], columns[2]) != null)
          throw new IOException("Invalid or duplicate manifest fact");
      } else if (columns[0].equals("file")) {
        String name = columns[1];
        if (!validPath(name) || !listed.add(name)
            || !folded.add(name.toLowerCase(Locale.ROOT))
            || !columns[2].matches("[0-9a-f]{64}"))
          throw new IOException("Invalid or duplicate manifest file path");
        Path file = root.resolve(name).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
            || linkedParent(root, file))
          throw new IOException("Unsafe or missing package file: " + name);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(file)) {
          byte[] block = new byte[64 * 1024];
          for (int count; (count = input.read(block)) != -1; ) digest.update(block, 0, count);
        }
        if (!HexFormat.of().formatHex(digest.digest()).equals(columns[2]))
          throw new IOException("Package checksum mismatch: " + name);
      } else throw new IOException("Unknown manifest entry");
    }
    if (!facts.keySet().equals(Set.of("sourceRevision", "artifact", "javaMinimum"))
        || !facts.get("sourceRevision").matches("[0-9a-f]{40}")
        || !facts.get("artifact").equals("t4c-server.jar")
        || !facts.get("javaMinimum").equals("21"))
      throw new IOException("Missing or invalid package facts");
    for (String required : List.of("t4c-server.jar", "checker/server-package-check.jar",
        "check-server.ps1", "start-server.ps1", "stop-server.ps1",
        "assets/maps/worldmap/worldmap.colbin", "assets/maps/dungeon/DungeonMap.colbin")) {
      if (!listed.contains(required)) throw new IOException("Missing required package file: " + required);
    }
    long libraries = listed.stream().filter(s -> s.startsWith("lib/") && s.endsWith(".jar")
        && s.indexOf('/', 4) < 0).count();
    if (libraries < 1 || listed.size() != 7 + libraries
        || listed.stream().anyMatch(s -> s.startsWith("lib/") && !s.matches("lib/[A-Za-z0-9_.-]+\\.jar")))
      throw new IOException("Invalid runtime dependency set");
    try (var jar = new JarFile(root.resolve("t4c-server.jar").toFile())) {
      for (String entry : List.of("com/perso/T4C/server/ServerMain.class",
          "content/items-v1.json", "content/monsters-v1.json", "content/portals-v1.json")) {
        if (jar.getEntry(entry) == null) throw new IOException("Missing bundled server resource: " + entry);
      }
    }
    try (var paths = Files.walk(root)) {
      for (Path path : paths.toList()) {
        if (path.equals(root)) continue;
        String relative = root.relativize(path).toString().replace('\\', '/');
        if (Files.isSymbolicLink(path)) throw new IOException("Linked package path: " + relative);
        if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
          if (!relative.equals("SHA256SUMS.tsv") && !listed.contains(relative))
            throw new IOException("Unlisted package file: " + relative);
        } else if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
          throw new IOException("Unsupported package path: " + relative);
        }
      }
    }
  }

  private static boolean validPath(String name) {
    if (name.isEmpty() || !name.matches("[A-Za-z0-9_./ -]+") || name.contains("\\")) return false;
    for (String segment : name.split("/", -1)) {
      if (segment.isEmpty() || segment.equals(".") || segment.equals("..")
          || segment.endsWith(".") || segment.endsWith(" ")) return false;
    }
    return true;
  }

  private static boolean linkedParent(Path root, Path file) {
    for (Path parent = file.getParent(); parent != null && !parent.equals(root); parent = parent.getParent())
      if (Files.isSymbolicLink(parent)) return true;
    return false;
  }
}
