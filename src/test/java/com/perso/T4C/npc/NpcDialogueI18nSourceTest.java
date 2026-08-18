package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NpcDialogueI18nSourceTest {
  @Test
  void convertedNpcBehavioursDoNotEmbedDialogueLiterals() throws IOException {
    Path root = Path.of("src/main/java/com/perso/T4C/npc");
    try (Stream<Path> files = Files.walk(root)) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        String source = Files.readString(file);
        assertFalse(source.matches("(?s).*\\bc\\.(?:say|shout)\\(\\s*\\\".*"), file.toString());
      }
    }
  }
}
