package com.perso.T4C.player;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The owner list: characters named in {@code gm_seed.json} are always Super GMs and cannot be
 * demoted in game. This is how the first Super GM exists at all; on a future server this file is
 * the server operator's admin list and never ships to clients.
 *
 * <pre>{ "superGms": ["Lynania"] }</pre>
 */
public final class GmSeed {
  public static final String FILE = "gm_seed.json";

  private static volatile Set<String> override;

  private GmSeed() {}

  public static boolean isOwner(String characterName) {
    return characterName != null
        && owners().contains(characterName.trim().toLowerCase(Locale.ROOT));
  }

  /** Test hook: replaces the file's contents; {@code null} goes back to reading the file. */
  public static void overrideForTests(List<String> names) {
    override = names == null ? null : normalize(names);
  }

  private static Set<String> owners() {
    Set<String> forced = override;
    if (forced != null) return forced;
    try {
      Path path = Path.of(System.getProperty("user.dir"), FILE);
      if (!Files.isRegularFile(path)) return Set.of();
      Seed seed = new Gson().fromJson(Files.readString(path, StandardCharsets.UTF_8), Seed.class);
      return seed == null || seed.superGms == null ? Set.of() : normalize(seed.superGms);
    } catch (Exception e) {
      return Set.of();
    }
  }

  private static Set<String> normalize(List<String> names) {
    return names.stream()
        .filter(name -> name != null && !name.isBlank())
        .map(name -> name.trim().toLowerCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableSet());
  }

  private static final class Seed {
    List<String> superGms;
  }
}
