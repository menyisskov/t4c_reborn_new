package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.SpellContent;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * The client copy of the spell allowlist must be byte-identical to the server's bundled content,
 * which {@code OnlineSpellQuestContentFreshnessTest} already pins to the exporter's real output.
 */
class OnlineSpellAllowlistFreshnessTest {
  @Test
  void clientCopyMatchesTheServerBundleAndLoads() throws Exception {
    String server = Files.readString(Path.of("multiplayer/server/src/main/resources/content/spells-v1.json"))
        .replace("\r\n", "\n");
    String client = Files.readString(Path.of("src/main/resources/online/spells-v1.json")).replace("\r\n", "\n");
    assertEquals(server, client, "Client allowlist is stale: copy the server spells-v1.json to src/main/resources/online/");

    var bundled = OnlineSpellAllowlist.bundled();
    assertNotNull(bundled, "The packaged resource must load from the classpath");
    assertEquals(SpellContent.Use.CAST, bundled.spells().get("spell.heal_light").use());
  }
}
