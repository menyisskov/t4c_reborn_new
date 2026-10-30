package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.i18n.I18n;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * A live two-window run (T4C-0331) showed the raw keys {@code online.spell.refused} and
 * {@code online.item.use.refused} in the chat: a cast or potion pressed while the previous command was still
 * in flight used a failure key that no catalogue entry defined. Every message key the remote command class
 * can hand to the screen must exist in the English catalogue.
 */
class RemoteCommandMessageKeysTest {
  private static final Pattern KEY = Pattern.compile("\"(online\\.[a-z_]+(?:\\.[a-z_]+)*)\"");

  @Test
  void everyMessageKeyNamedInRemotePlayerCommandsExistsInTheCatalogue() throws IOException {
    String source = Files.readString(Path.of("src/main/java/com/perso/T4C/session/RemotePlayerCommands.java"));
    var missing = new TreeSet<String>();
    var found = new TreeSet<String>();
    var matcher = KEY.matcher(source);
    while (matcher.find()) {
      found.add(matcher.group(1));
      if (!I18n.has(matcher.group(1))) missing.add(matcher.group(1));
    }
    assertTrue(found.size() > 10, "the scan should find the command class's keys: " + found);
    assertTrue(missing.isEmpty(), "message keys with no catalogue entry (the player would read the raw key): " + missing);
  }
}
