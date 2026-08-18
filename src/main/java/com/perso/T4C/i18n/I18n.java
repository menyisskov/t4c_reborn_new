package com.perso.T4C.i18n;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class I18n {
  private static final Type CATALOGUE_TYPE = new TypeToken<Map<String, String>>() {}.getType();
  private static final Pattern PLACEHOLDER = Pattern.compile("^\\$\\{([^{}]+)}$");
  public static final String CATALOGUE_PATH = "assets/i18n/lang.json";
  private static volatile Map<String, String> catalogue;

  private I18n() {}

  public static String resolve(String value) {
    String key = placeholderKey(value);
    if (key == null) {
      if (value != null && catalogue().containsKey(value.trim())) {
        return displaySafe(catalogue().get(value.trim()));
      }
      return displaySafe(value);
    }
    String resolved = catalogue().getOrDefault(key, value);
    return displaySafe(resolved);
  }

  public static String key(String key) {
    return key(key, key);
  }

  public static String key(String key, String fallback) {
    if (key == null) return displaySafe(fallback);
    return displaySafe(catalogue().getOrDefault(key, fallback));
  }

  public static boolean has(String key) {
    return key != null && catalogue().containsKey(key);
  }

  public static String message(String key, Object... args) {
    String template = key(key);
    return args.length == 0 ? template : String.format(java.util.Locale.ROOT, template, args);
  }

  public static String placeholder(String key) {
    if (key == null || key.isBlank()) return null;
    return "${" + key + "}";
  }

  public static String placeholder(String namespace, String identity) {
    if (namespace == null || identity == null || identity.isBlank()) return null;
    return placeholder(namespace + "." + normalizedKey(identity));
  }

  public static String placeholderForKey(String key, String value) {
    if (value == null || value.isBlank()) return value;
    if (key == null || key.isBlank()) return value;
    if (placeholderKey(value) != null) return value;
    return placeholder(key);
  }

  public static String placeholderFor(String namespace, String identity, String value) {
    if (value == null || value.isBlank()) return value;
    if (placeholderKey(value) != null) return value;
    if (namespace == null || identity == null || identity.isBlank()) return value;
    return placeholder(namespace, identity);
  }

  public static String keyOf(String value) {
    return placeholderKey(value);
  }

  public static String normalizedKey(String value) {
    if (value == null) return "";
    return value
        .trim()
        .toLowerCase(java.util.Locale.ROOT)
        .replaceFirst("^\\s*\\[[^]]+]\\s*", "")
        .replaceFirst("^a\\s+", "")
        .replaceAll("[^a-z0-9]+", "_")
        .replaceAll("^_|_$", "");
  }

  public static synchronized void reload() {
    catalogue = load(new File(CATALOGUE_PATH));
  }

  public static synchronized void update(Map<String, String> updates) {
    if (updates == null || updates.isEmpty()) return;
    File file = new File(CATALOGUE_PATH);
    Map<String, String> merged = new TreeMap<>(load(file));
    for (Map.Entry<String, String> entry : updates.entrySet()) {
      if (entry.getValue() == null || entry.getValue().isBlank()) {
        merged.remove(entry.getKey());
      } else {
        merged.put(entry.getKey(), entry.getValue());
      }
    }
    try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
      new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(merged, writer);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to write translation catalogue: " + file.getAbsolutePath(), e);
    }
    catalogue = Collections.unmodifiableMap(merged);
  }

  private static Map<String, String> catalogue() {
    Map<String, String> loaded = catalogue;
    if (loaded != null) return loaded;
    synchronized (I18n.class) {
      if (catalogue == null) reload();
      return catalogue;
    }
  }

  private static Map<String, String> load(File file) {
    if (!file.isFile()) {
      throw new IllegalStateException("Missing translation catalogue: " + file.getAbsolutePath());
    }
    try (FileReader reader = new FileReader(file, java.nio.charset.StandardCharsets.UTF_8)) {
      Map<String, String> parsed = new Gson().fromJson(reader, CATALOGUE_TYPE);
      if (parsed == null || parsed.isEmpty()) {
        throw new IllegalStateException("Empty translation catalogue: " + file.getAbsolutePath());
      }
      return Collections.unmodifiableMap(new HashMap<>(parsed));
    } catch (IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException(
          "Unreadable translation catalogue: " + file.getAbsolutePath(), e);
    }
  }

  private static String placeholderKey(String value) {
    if (value == null) return null;
    Matcher matcher = PLACEHOLDER.matcher(value.trim());
    return matcher.matches() ? matcher.group(1) : null;
  }

  private static String displaySafe(String value) {
    if (value == null) return null;
    return value
        .replace('\u2018', '\'')
        .replace('\u2019', '\'')
        .replace('\u201C', '"')
        .replace('\u201D', '"')
        .replace('\u00A0', ' ');
  }
}
