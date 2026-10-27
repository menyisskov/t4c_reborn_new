package com.perso.T4C.online;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
      // A fresh directory prevents two clients from replacing a DLL already loaded by the other.
      var directory = Files.createTempDirectory("t4c-online-glfw-");
      var path = directory.resolve("glfw3.dll");
      Files.write(path, library);
      directory.toFile().deleteOnExit();
      path.toFile().deleteOnExit();
      System.setProperty("org.lwjgl.glfw.libname", path.toAbsolutePath().toString());
    } catch (IOException failure) {
      throw new IllegalStateException("Could not prepare the online Windows native library", failure);
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
