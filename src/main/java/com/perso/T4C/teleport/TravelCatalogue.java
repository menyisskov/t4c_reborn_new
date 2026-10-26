package com.perso.T4C.teleport;

import com.google.gson.Gson;
import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.spawn.HuntingGrounds;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The full, visible travel catalogue. Access is based on exploration, not quest completion. */
public final class TravelCatalogue {
  private TravelCatalogue() {}

  public static final List<String> CATEGORIES =
      List.of(
          "cities", "hunting", "training", "good", "evil", "olin", "gods", "islands", "landmarks");

  public record Destination(
      String id,
      String category,
      String nameKey,
      String descriptionKey,
      Integer x,
      Integer y,
      Integer z) {
    public boolean mapped() {
      return x != null
          && y != null
          && z != null
          && com.perso.T4C.config.MapDefinition.fromZ(z).getZ() == z;
    }

    private static String text(String value) {
      return I18n.has(value) ? I18n.key(value) : I18n.resolve(value);
    }

    public String name() {
      return text(nameKey);
    }

    public String description() {
      return text(descriptionKey);
    }

    public String discoveryFlag() {
      return "travel.discovered." + z + "." + x + "." + y;
    }
  }

  private static final class Holder {
    private static final List<Destination> ALL = load();
  }

  public static List<Destination> all() {
    return Holder.ALL;
  }

  /**
   * Travel chapters retain their authored order except combat fields, which follow monster level.
   */
  public static List<Destination> forCategory(String category) {
    List<Destination> destinations =
        all().stream().filter(destination -> category.equals(destination.category())).toList();
    if (!"hunting".equals(category) && !"training".equals(category)) return destinations;
    return destinations.stream()
        .sorted(
            Comparator.comparingInt(TravelCatalogue::fieldMonsterLevel)
                .thenComparing(Destination::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Destination::id))
        .toList();
  }

  private static int fieldMonsterLevel(Destination destination) {
    String monster =
        "hunt.arch_drake".equals(destination.id())
            ? "Arch Drake"
            : HuntingGrounds.all().stream()
                .filter(ground -> ("base." + ground.locationKey()).equals(destination.id()))
                .map(HuntingGrounds.Ground::monster)
                .findFirst()
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "No monster for travel destination " + destination.id()));
    MonsterDef definition = MonsterRegistry.findByName(monster);
    if (definition == null)
      throw new IllegalStateException("Missing travel monster definition: " + monster);
    return definition.getLevel();
  }

  private static List<Destination> load() {
    List<Destination> result = new ArrayList<>();
    try {
      Destination[] quests =
          new Gson()
              .fromJson(
                  Files.readString(Path.of("assets/teleports/quest-destinations.json")),
                  Destination[].class);
      if (quests == null) throw new IllegalStateException("Missing quest travel catalogue");
      result.addAll(List.of(quests));
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Cannot load quest travel catalogue", e);
    }
    for (NamedLocation location : NamedLocations.all()) {
      boolean alreadyListed =
          result.stream()
              .anyMatch(
                  d ->
                      d.mapped()
                          && d.x() == location.tileX()
                          && d.y() == location.tileY()
                          && d.z() == location.worldZ());
      if (alreadyListed) continue;
      String slug = NamedLocations.catalogueKey(location);
      String category = "landmarks";
      if (List.of("Lighthaven", "Windhowl", "Silversky", "Stonecrest")
          .contains(location.displayName())) category = "cities";
      else if (slug.startsWith("hunting_")) {
        category =
            com.perso.T4C.spawn.HuntingGrounds.all().stream()
                    .anyMatch(
                        g ->
                            g.x() == location.tileX()
                                && g.y() == location.tileY()
                                && g.trainingTier() > 0)
                ? "training"
                : "hunting";
      } else if (location.unlockZoneId() != null) category = "islands";
      String key = "location." + slug;
      String name = I18n.has(key) ? "${" + key + "}" : location.displayName();
      String desc = I18n.has(key + ".desc") ? "${" + key + ".desc}" : "${travel.generic}";
      int tileX = location.tileX(), tileY = location.tileY();
      // The old Chancel centre is deep water. Its authored hunting field is the usable shore.
      if ("sunken_chancel".equals(slug)) {
        var shore =
            com.perso.T4C.spawn.HuntingGrounds.all().stream()
                .filter(g -> "Drowned Acolyte".equals(g.monster()))
                .findFirst()
                .orElseThrow();
        tileX = shore.x();
        tileY = shore.y();
      }
      result.add(
          new Destination("base." + slug, category, name, desc, tileX, tileY, location.worldZ()));
    }
    // Keep the highest-XP boss explicitly findable beside its dense hunting field.
    result.add(
        new Destination(
            "hunt.arch_drake",
            "hunting",
            "${travel.arch_drake}",
            "${travel.field}",
            2850,
            2780,
            0));
    for (var god : DivinePantheon.all()) {
      result.add(
          new Destination(
              "god." + god.id(),
              "gods",
              "${travel.god." + god.id() + "}",
              "${travel.god}",
              god.x(),
              god.y(),
              0));
    }
    Set<String> ids = new HashSet<>();
    for (var d : result) {
      if (!ids.add(d.id())
          || !CATEGORIES.contains(d.category())
          || d.nameKey() == null
          || d.descriptionKey() == null
          || (d.mapped() && (d.x() < 0 || d.y() < 0 || d.z() < 0)))
        throw new IllegalStateException("Invalid travel destination: " + d.id());
    }
    return List.copyOf(result);
  }
}
