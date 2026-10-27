package com.perso.T4C.online;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/** Headless integrity check for the unpacked online-only desktop distribution. */
public final class OnlinePackageCheck {
  private OnlinePackageCheck() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Usage: OnlinePackageCheck BUNDLE_DIRECTORY");
    check(Path.of(args[0]));
    System.out.println("Online package integrity OK");
  }

  static void check(Path root) throws Exception {
    if (!System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).startsWith("windows")
        || !(System.getProperty("os.arch", "").equalsIgnoreCase("amd64")
            || System.getProperty("os.arch", "").equalsIgnoreCase("x86_64"))
        || Runtime.version().feature() < 21)
      throw new IOException("Online package requires Windows x64 and Java 21 or newer x64");
    root = root.toAbsolutePath().normalize();
    if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(root))
      throw new IOException("Package directory is missing or linked");
    Path manifest = root.resolve("SHA256SUMS.tsv");
    if (Files.isSymbolicLink(manifest)) throw new IOException("Linked package manifest");
    List<String> lines = Files.readAllLines(manifest, StandardCharsets.UTF_8);
    if (lines.size() < 2 || !lines.getFirst().equals("T4C-ONLINE-PACKAGE\t1"))
      throw new IOException("Invalid package manifest");
    Set<String> listed = new HashSet<>();
    for (String line : lines.subList(1, lines.size())) {
      String[] columns = line.split("\t", -1);
      if (columns.length == 3 && columns[0].equals("file")) {
        String name = columns[1];
        if (!name.matches("[A-Za-z0-9_./ -]+") || name.startsWith("/")
            || name.contains("\\") || name.contains("..") || !listed.add(name)
            || !columns[2].matches("[0-9a-f]{64}"))
          throw new IOException("Invalid manifest file entry");
        Path file = root.resolve(name).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
            || linkedParent(root, file)) throw new IOException("Unsafe or missing package file: " + name);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(file)) {
          byte[] block = new byte[64 * 1024];
          for (int count; (count = input.read(block)) != -1; ) digest.update(block, 0, count);
        }
        String actual = HexFormat.of().formatHex(digest.digest());
        if (!actual.equals(columns[2])) throw new IOException("Package checksum mismatch: " + name);
      } else if (columns.length != 3 || !columns[0].equals("fact")
          || !columns[1].matches("[A-Za-z][A-Za-z0-9]*")
          || !columns[2].matches("[A-Za-z0-9._:-]+")) {
        throw new IOException("Invalid package manifest entry");
      }
    }
    for (String required : List.of("app/T4C.jar", "checker/online-package-check.jar",
        "launch-online.ps1", "launch-online.bat", "GLFW-LICENSE.txt",
        "assets/maps/worldmap/worldmap.mapbin", "assets/maps/dungeon/DungeonMap.mapbin",
        "assets/sprites/sprites_0.bin", "assets/sprites/sprites_1.bin",
        "assets/sprites/sprites_2.bin")) {
      if (!listed.contains(required)) throw new IOException("Missing required package entry: " + required);
    }
    if (listed.stream().noneMatch(s -> s.startsWith("lib/") && s.endsWith(".jar")))
      throw new IOException("Runtime libraries are missing");
    try (var app = new java.util.jar.JarFile(root.resolve("app/T4C.jar").toFile())) {
      for (String entry : List.of("com/perso/T4C/online/OnlineClient.class",
          "com/perso/T4C/online/OnlineNativeLibrary.class",
          "online-native/windows-x86_64/glfw3.dll",
          "online-native/windows-x86_64/glfw3.dll.sha256")) {
        if (app.getEntry(entry) == null) throw new IOException("Missing desktop JAR entry: " + entry);
      }
    }
    try (var paths = Files.walk(root)) {
      for (Path path : paths.toList()) {
        if (path.equals(root) || Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) continue;
        String relative = root.relativize(path).toString().replace('\\', '/');
        if (Files.isSymbolicLink(path)) throw new IOException("Linked package path: " + relative);
        if (!relative.equals("SHA256SUMS.tsv") && !relative.startsWith("logs/") && !listed.contains(relative))
          throw new IOException("Unlisted package file: " + relative);
      }
    }
    byte[] dll = OnlineNativeLibrary.verifiedLibrary();
    if (dll.length < 2 || dll[0] != 'M' || dll[1] != 'Z')
      throw new IOException("Invalid bundled Windows native library");
  }

  private static boolean linkedParent(Path root, Path file) {
    for (Path parent = file.getParent(); parent != null && !parent.equals(root); parent = parent.getParent()) {
      if (Files.isSymbolicLink(parent)) return true;
    }
    return false;
  }
}
