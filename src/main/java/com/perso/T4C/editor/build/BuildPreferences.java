package com.perso.T4C.editor.build;

import com.google.gson.Gson;
import java.nio.file.*;
import java.util.*;

public final class BuildPreferences {
  public Set<String> favorites = new LinkedHashSet<>();
  public List<String> recent = new ArrayList<>();
  public float panelWidth = 380;

  public static BuildPreferences load(Path path) {
    try {
      var result = new Gson().fromJson(Files.readString(path), BuildPreferences.class);
      if (result != null
          && result.favorites != null
          && result.recent != null
          && Float.isFinite(result.panelWidth)) return result;
    } catch (Exception ignored) {
    }
    return new BuildPreferences();
  }

  public void remember(String name) {
    recent.remove(name);
    recent.addFirst(name);
    while (recent.size() > 24) recent.removeLast();
  }

  public void save(Path path) throws java.io.IOException {
    Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
    Files.writeString(tmp, new Gson().toJson(this));
    try {
      Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
