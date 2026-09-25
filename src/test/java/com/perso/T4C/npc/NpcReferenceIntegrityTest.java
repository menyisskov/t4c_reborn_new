package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NpcReferenceIntegrityTest {
  private static final Path NPC_ROOT = Path.of("src/main/java/com/perso/T4C/npc");

  private static final Pattern ITEM_REFERENCE =
      Pattern.compile("\\b(?:giveItem|takeItem|hasItem|itemCount)\\(\\s*\"([^\"+]+)\"");

  private static final Pattern MESSAGE_REFERENCE =
      Pattern.compile("\\b(?:sayKey|shoutKey|systemMessageKey)\\(\\s*\"([^\"+]+)\"");

  // JSON-authored items (e.g. Wyrm Scales, the Lost Keys of Kraanhold) only resolve through
  // ItemRegistry once loaded - real gameplay does this once at startup (MyGame), but a test class
  // run in isolation (or before any test that happens to load them) must not depend on another
  // test class having already done so first. Idempotent: repeat calls are harmless.
  @BeforeEach
  void loadJsonItems() {
    ItemJsonLoader.loadAndRegister("assets/items");
  }

  @Test
  void literalItemReferencesResolve() throws Exception {
    try (Stream<Path> files = Files.walk(NPC_ROOT)) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        String source = Files.readString(file);
        Matcher matcher = ITEM_REFERENCE.matcher(source);
        while (matcher.find()) {
          if (isConcatenated(source, matcher.end())) continue;
          String key = matcher.group(1);
          assertNotNull(ItemRegistry.findByKey(key), file + " references missing item " + key);
        }
      }
    }
  }

  @Test
  void literalMessageReferencesResolve() throws Exception {
    JsonObject messages =
        JsonParser.parseString(Files.readString(Path.of("assets/i18n/lang.json"))).getAsJsonObject();

    try (Stream<Path> files = Files.walk(NPC_ROOT)) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        String source = Files.readString(file);
        Matcher matcher = MESSAGE_REFERENCE.matcher(source);
        while (matcher.find()) {
          if (isConcatenated(source, matcher.end())) continue;
          String key = matcher.group(1);
          if (key.endsWith(".")) continue;
          if (key.startsWith("${") && key.endsWith("}")) key = key.substring(2, key.length() - 1);
          assertTrue(messages.has(key), file + " references missing message " + key);
        }
      }
    }
  }

  private static boolean isConcatenated(String source, int offset) {
    for (int i = offset; i < source.length(); i++) {
      char c = source.charAt(i);
      if (Character.isWhitespace(c) || c == '"') continue;
      return c == '+';
    }
    return false;
  }
}
