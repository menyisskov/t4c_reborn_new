package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpriteBinIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class SpellVisualAudioAuditTest {
  @Test
  void everySpellHasResolvableAnimationAndSoundAssets() throws Exception {
    Set<String> spriteNames = loadSpriteNames();
    Set<String> soundsExact = loadSoundFiles(false);
    Set<String> soundsLower = loadSoundFiles(true);
    List<String> errors = new ArrayList<>();
    for (SpellData spell : SpellRegistry.load()) {
      if (spell == null) continue;
      String id = spell.getSpellId() + " " + spell.getName();
      SpellVisualResolver.Visuals visuals = SpellVisualResolver.resolve(spell);
      String projectile = blankToNull(visuals.projectile());
      String impact = blankToNull(visuals.impact());
      if (projectile == null && impact == null) {
        errors.add(id + ": no projectile and no impact animation");
      } else {
        if (projectile != null && !spritePrefixExists(spriteNames, projectile)) {
          errors.add(id + ": missing projectile sprites for '" + projectile + "'");
        }
        if (impact != null && !spritePrefixExists(spriteNames, impact)) {
          errors.add(id + ": missing impact sprites for '" + impact + "'");
        }
      }
      checkSound(id, "launch", visuals.launchSound(), soundsExact, soundsLower, errors);
      checkSound(id, "impact", visuals.impactSound(), soundsExact, soundsLower, errors);
    }
    assertTrue(errors.isEmpty(), () -> "Spell VFX/audio audit failed:\n" + String.join("\n", errors));
  }

  private static void checkSound(
      String id,
      String kind,
      String sound,
      Set<String> exact,
      Set<String> lower,
      List<String> errors) {
    if (sound == null || sound.isBlank() || "0".equals(sound.trim())) {
      errors.add(id + ": missing " + kind + " sound");
      return;
    }
    String file = sound.trim();
    if (!file.toLowerCase(Locale.ROOT).endsWith(".wav")) {
      file = file + ".wav";
    }
    if (exact.contains(file) || lower.contains(file.toLowerCase(Locale.ROOT))) {
      return;
    }
    errors.add(id + ": missing " + kind + " sound '" + sound + "'");
  }

  private static boolean spritePrefixExists(Set<String> spriteNames, String baseName) {
    String resolved =
        switch (baseName.toLowerCase(Locale.ROOT)) {
          case "64kspellenergyballblue-",
                  "64kspellenergyballyellow-",
                  "64kspellenergyballblack-",
                  "64kspellenergyballpurple-" ->
              "64kSpellEnergyBall-";
          default -> baseName;
        };
    String base =
        resolved.endsWith("-") ? resolved.substring(0, resolved.length() - 1) : resolved;
    String lower = base.toLowerCase(Locale.ROOT);
    if (spriteNames.contains(lower)) {
      return true;
    }
    for (String name : spriteNames) {
      if (name.equals(lower) || name.startsWith(lower)) {
        return true;
      }
    }
    return false;
  }

  private static Set<String> loadSpriteNames() throws Exception {
    Set<String> names = new HashSet<>();
    Path dir = Path.of(Paths.SPRITE_DIR);
    SpriteBinIO.readAll(
        dir,
        Paths.SPRITE_BIN_BASE,
        packed -> {
          if (packed.name() != null) {
            names.add(packed.name().toLowerCase(Locale.ROOT));
          }
        });
    return names;
  }

  private static Set<String> loadSoundFiles(boolean lower) throws Exception {
    Set<String> names = new TreeSet<>();
    Path dir = Path.of(Paths.SOUNDS_DIR);
    try (var stream = Files.list(dir)) {
      stream
          .filter(Files::isRegularFile)
          .forEach(
              path -> {
                String name = path.getFileName().toString();
                names.add(lower ? name.toLowerCase(Locale.ROOT) : name);
              });
    }
    return names;
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() || "0".equals(value.trim()) ? null : value;
  }
}
