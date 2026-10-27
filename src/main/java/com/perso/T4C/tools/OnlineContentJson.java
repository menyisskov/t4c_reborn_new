package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.TreeMap;

/** Byte-stable JSON for packaged online content, so freshness checks compare exact output. */
final class OnlineContentJson {
  private OnlineContentJson() {}

  static String render(Object content) {
    var gson = new GsonBuilder().serializeNulls().create();
    // Map.copyOf has unspecified iteration order, so every object is re-sorted before serialization.
    return gson.toJson(canonical(gson.toJsonTree(content))) + "\n";
  }

  private static JsonElement canonical(JsonElement value) {
    if (value.isJsonObject()) {
      var result = new JsonObject();
      new TreeMap<>(value.getAsJsonObject().asMap()).forEach((key, child) -> result.add(key, canonical(child)));
      return result;
    }
    if (value.isJsonArray()) {
      var result = new JsonArray();
      value.getAsJsonArray().forEach(child -> result.add(canonical(child)));
      return result;
    }
    return value;
  }
}
