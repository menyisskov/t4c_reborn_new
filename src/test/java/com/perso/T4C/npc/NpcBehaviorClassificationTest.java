package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NpcBehaviorClassificationTest {
  @Test
  void nonPortalNpcClassesNeverUseTeleportPortalBehavior() throws Exception {
    for (String id :
        new String[] {
          "Archibald",
          "ColosseumClerk",
          "ColosseumClerkXP",
          "ColosseumOwner",
          "ForestWanderer",
          "Safe",
          "SanctuaryGuardian",
          "Ramirgo",
          "RemortNPC1",
          "WorldWildHorse",
          "OlinHaad1"
        }) {
      Path file =
          Files.walk(Path.of("src/main/java/com/perso/T4C/npc"))
              .filter(path -> path.getFileName().toString().equals(id + ".java"))
              .findFirst()
              .orElseThrow();
      assertFalse(Files.readString(file).contains("PortalBehavior"), id);
    }
  }
}
