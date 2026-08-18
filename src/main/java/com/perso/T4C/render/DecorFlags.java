package com.perso.T4C.render;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.DecorLayerRuleBinaryIO;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DecorFlags {
  private static final Logger log = LoggerFactory.getLogger(DecorFlags.class);

  private DecorFlags() {}

  public static Set<String> loadPlayerAlwaysAboveRules() {
    Set<String> names = new HashSet<>();
    File file = new File(Paths.DECOR_LAYER_RULES_BIN);
    if (!file.exists()) {
      return names;
    }
    try {
      names.addAll(DecorLayerRuleBinaryIO.read(file));
    } catch (Exception e) {
      log.warn("Could not load decor layer rules from {}: {}", file.getPath(), e.getMessage());
    }
    return names;
  }
}
