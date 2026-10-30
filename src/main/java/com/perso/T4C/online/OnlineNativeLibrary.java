package com.perso.T4C.online;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/** Online-only native selection. The offline launcher never invokes this class. */
final class OnlineNativeLibrary {
  private static final String ROOT = "/online-native/windows-x86_64/";

  private OnlineNativeLibrary() {}

  /** Must precede any GLFW/LibGDX initialization, including creation of the client. */
  static void configure() {
    if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) return;
    String architecture = System.getProperty("os.arch", "");
    if (!architecture.equals("amd64") && !architecture.equals("x86_64"))
      throw new IllegalStateException("The online Windows client requires a 64-bit x86 Java runtime");
    try {
      byte[] library = verifiedLibrary();
      Path path = prepare(Path.of(System.getProperty("java.io.tmpdir")), library);
      System.setProperty("org.lwjgl.glfw.libname", path.toAbsolutePath().toString());
    } catch (IOException failure) {
      throw new IllegalStateException("Could not prepare the online Windows native library", failure);
    }
  }

  /**
   * Places the verified DLL where LWJGL can load it. The folder is named after the DLL's checksum, so
   * every launch of the same build reuses one folder instead of leaving a new one in TEMP (a loaded
   * DLL cannot be deleted on Windows, so deleteOnExit never removed the old per-launch folders).
   * An existing file is reused only when its bytes match exactly; otherwise a private fresh folder is
   * used, so a stale, damaged or planted file is never loaded.
   */
  static Path prepare(Path tempRoot, byte[] library) throws IOException {
    String fingerprint = fingerprint(library);
    Path directory = tempRoot.resolve("t4c-online-glfw-" + fingerprint.substring(0, 16));
    Path path = directory.resolve("glfw3.dll");
    try {
      Files.createDirectories(directory);
      if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && Arrays.equals(Files.readAllBytes(path), library)) return path;
      // Write beside the target and move it in, so a second client never reads a half-written DLL.
      Path partial = Files.createTempFile(directory, "glfw3-", ".part");
      try {
        Files.write(partial, library);
        Files.move(partial, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } finally {
        Files.deleteIfExists(partial);
      }
      if (Arrays.equals(Files.readAllBytes(path), library)) return path;
    } catch (IOException | RuntimeException unusable) {
      // fall through: the shared folder is not usable (for example a loaded, different DLL)
    }
    Path fresh = Files.createTempDirectory(tempRoot, "t4c-online-glfw-");
    Path freshPath = fresh.resolve("glfw3.dll");
    Files.write(freshPath, library);
    fresh.toFile().deleteOnExit();
    freshPath.toFile().deleteOnExit();
    return freshPath;
  }

  private static String fingerprint(byte[] library) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(library));
    } catch (NoSuchAlgorithmException impossible) {
      throw new AssertionError(impossible);
    }
  }

  static byte[] verifiedLibrary() throws IOException {
    byte[] library = resource("glfw3.dll");
    String expected = new String(resource("glfw3.dll.sha256"), StandardCharsets.US_ASCII).trim();
    try {
      String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(library));
      if (!expected.equals(actual)) throw new IOException("Online GLFW library checksum mismatch");
      return library;
    } catch (NoSuchAlgorithmException impossible) {
      throw new AssertionError(impossible);
    }
  }

  private static byte[] resource(String name) throws IOException {
    try (var input = OnlineNativeLibrary.class.getResourceAsStream(ROOT + name)) {
      if (input == null) throw new IOException("Missing online native resource: " + name);
      return input.readAllBytes();
    }
  }
}
