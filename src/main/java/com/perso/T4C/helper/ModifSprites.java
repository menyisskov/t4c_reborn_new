package com.perso.T4C.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class ModifSprites {
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Offset {
    public float x;
    public float y;
  }

  private static final Offset ZERO_OFFSET = new Offset(0f, 0f);
  private final Map<String, Offset> offsets = new HashMap<>();

  public ModifSprites(String filePath) {
    if (filePath == null || filePath.isBlank()) {
      return;
    }
    FileHandle file = Gdx.files.internal(filePath);
    if (!file.exists()) return;
    try (BufferedReader br = file.reader(256)) {
      Gson gson = new Gson();
      Type type = new TypeToken<Map<String, Offset>>() {}.getType();
      Map<String, Offset> data = gson.fromJson(br, type);
      if (data != null) {
        for (Map.Entry<String, Offset> e : data.entrySet()) {
          String key = e.getKey().toLowerCase(Locale.ROOT);
          offsets.put(key, e.getValue());
        }
      }
    } catch (Exception e) {
      Gdx.app.error("ModifSprites", "Error reading " + filePath, e);
    }
  }

  public Offset getOffset(String name) {
    if (name == null) return ZERO_OFFSET;
    return offsets.getOrDefault(name.toLowerCase(Locale.ROOT), ZERO_OFFSET);
  }

  public static ModifSprites empty() {
    return new ModifSprites(null);
  }
}
