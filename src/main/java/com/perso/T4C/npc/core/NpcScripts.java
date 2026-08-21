package com.perso.T4C.npc.core;

import com.perso.T4C.npc.script.original.OriginalNpcScriptSources;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** NPC catalogue from Java factory registrations, plus spell/skill macro tables. */
public final class NpcScripts {
  public static final int FORMAT_VERSION = 1;

  private static volatile Catalogue catalogue;

  private NpcScripts() {}

  public static Entry find(String npcId) {
    if (npcId == null || npcId.isBlank()) return null;
    Catalogue loaded = catalogue();
    Entry exact = loaded.entries().get(npcId);
    if (exact != null) return exact;
    return loaded.byLowerId().get(npcId.toLowerCase(Locale.ROOT));
  }

  public static Map<String, Entry> entries() {
    return catalogue().entries();
  }

  public static String sourceCommit() {
    return catalogue().sourceCommit();
  }

  public static int macro(String name) {
    if (name == null || name.isBlank()) return 0;
    Integer value = catalogue().macros().get(name);
    return value == null ? 0 : value;
  }

  public static boolean hasMacro(String name) {
    return name != null && catalogue().macros().containsKey(name);
  }

  public static String skillName(String macro) {
    if (macro == null) return null;
    String mapped = catalogue().skillNames().get(macro);
    if (mapped != null) return mapped;
    if (macro.startsWith("__SKILL_")) {
      return macro.substring("__SKILL_".length()).toLowerCase(Locale.ROOT).replace("powerfull", "powerful")
          .replace("twoweapons", "two_weapons");
    }
    return null;
  }

  public static synchronized void reload() {
    catalogue = load();
  }

  private static Catalogue catalogue() {
    Catalogue loaded = catalogue;
    if (loaded != null) return loaded;
    synchronized (NpcScripts.class) {
      if (catalogue == null) reload();
      return catalogue;
    }
  }

  private static Catalogue load() {
    Map<String, Entry> entries = new LinkedHashMap<>();
    Map<String, Entry> lower = new LinkedHashMap<>();
    for (NpcFactoryRegistry.Registration registration : NpcFactoryRegistry.registrations()) {
      NpcSpec spec =
          registration.specification() == null ? null : registration.specification().get();
      if (spec == null) continue;
      index(
          entries,
          lower,
          registration.id(),
          new Entry(
              registration.id(),
              "",
              spec.sourceTemplate(),
              spec.sourceScript(),
              spec.sourceEvents()));
    }
    return new Catalogue(
        FORMAT_VERSION,
        "",
        "",
        OriginalNpcScriptSources.macros(),
        OriginalNpcScriptSources.skillNames(),
        Collections.unmodifiableMap(entries),
        Collections.unmodifiableMap(lower));
  }

  private static void index(
      Map<String, Entry> entries, Map<String, Entry> lower, String id, Entry entry) {
    if (id == null || id.isBlank() || entry == null) return;
    Entry normalized = entry.normalized();
    entries.put(id, normalized);
    lower.put(id.toLowerCase(Locale.ROOT), normalized);
    if (normalized.sourceClass() != null && !normalized.sourceClass().isBlank()) {
      lower.putIfAbsent(normalized.sourceClass().toLowerCase(Locale.ROOT), normalized);
    }
  }

  private record Catalogue(
      int formatVersion,
      String sourceRepository,
      String sourceCommit,
      Map<String, Integer> macros,
      Map<String, String> skillNames,
      Map<String, Entry> entries,
      Map<String, Entry> byLowerId) {}

  public record Entry(
      String sourceClass,
      String sourceFile,
      String sourceTemplate,
      String sourceScript,
      Map<String, String> sourceEvents) {
    private Entry normalized() {
      return new Entry(
          sourceClass,
          sourceFile,
          sourceTemplate,
          sourceScript,
          Map.copyOf(sourceEvents == null ? Map.of() : sourceEvents));
    }

    public String event(String name) {
      return sourceEvents.get(name);
    }

    public boolean hasConversation() {
      return sourceScript != null && !sourceScript.isBlank();
    }

    public boolean hasEvent(String name) {
      String script = event(name);
      return script != null && !script.isBlank();
    }
  }
}
