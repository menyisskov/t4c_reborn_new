package com.perso.T4C.editor.build;

import com.perso.T4C.helper.SpriteLoader;
import java.util.*;
import java.util.regex.Pattern;

/** Searchable metadata; never changes the opaque IDs used by the game. */
public final class AssetCatalog {
  public enum Category {
    ALL,
    WALLS,
    DOORS,
    FLOORS,
    ROOFS,
    VEGETATION,
    ROCKS,
    FURNITURE,
    CONTAINERS,
    GROUND,
    ACTORS,
    EFFECTS,
    OTHER
  }

  public enum Material {
    ALL,
    STONE,
    WOOD,
    METAL,
    NATURAL,
    OTHER
  }

  public record Asset(
      String name,
      String label,
      String family,
      Category category,
      Material material,
      String orientation,
      int width,
      int height,
      boolean ground,
      int offX,
      int offY) {}

  private final List<Asset> assets;

  public AssetCatalog(Collection<SpriteLoader.Sprite> sprites) {
    assets =
        sprites.stream()
            .filter(s -> s.name != null && !s.name.isBlank())
            .map(
                s ->
                    describe(
                        s.name, s.width, s.height, s.isGround(), s.drawOffset1X, s.drawOffset1Y))
            .sorted(Comparator.comparing(Asset::name, String.CASE_INSENSITIVE_ORDER))
            .toList();
  }

  public List<Asset> all() {
    return assets;
  }

  public static Asset describe(
      String name, int width, int height, boolean ground, int offX, int offY) {
    String n = name.toLowerCase(Locale.ROOT);
    Category category =
        ground
            ? (n.contains("floor") ? Category.FLOORS : Category.GROUND)
            : has(n, "door", "gate", "entrance")
                ? Category.DOORS
                : has(n, "wall", "rampart", "fence", "pillar", "column")
                    ? Category.WALLS
                    : has(n, "roof")
                        ? Category.ROOFS
                        : has(
                                n,
                                "tree",
                                "bush",
                                "plant",
                                "grass",
                                "flower",
                                "fern",
                                "mushroom",
                                "stump")
                            ? Category.VEGETATION
                            : has(n, "rock", "cave", "cliff", "stalag", "boulder")
                                ? Category.ROCKS
                                : has(n, "chest", "crate", "barrel", "box", "container")
                                    ? Category.CONTAINERS
                                    : has(
                                            n,
                                            "chair",
                                            "table",
                                            "bed",
                                            "bench",
                                            "shelf",
                                            "altar",
                                            "statue",
                                            "fountain",
                                            "carpet",
                                            "torch")
                                        ? Category.FURNITURE
                                        : has(n, "pup", "monster") || n.matches(".*\\d{3}-[a-z]+$")
                                            ? Category.ACTORS
                                            : has(
                                                    n,
                                                    "spell",
                                                    "effect",
                                                    "impact",
                                                    "projectile",
                                                    "vfx")
                                                ? Category.EFFECTS
                                                : Category.OTHER;
    Material material =
        has(n, "brick", "stone", "castle", "rock", "cave", "marble", "granite")
            ? Material.STONE
            : has(n, "wood", "timber", "log", "plank", "oak", "pine")
                ? Material.WOOD
                : has(n, "iron", "metal", "steel", "gold", "silver")
                    ? Material.METAL
                    : category == Category.GROUND || category == Category.VEGETATION
                        ? Material.NATURAL
                        : Material.OTHER;
    String family =
        name.replaceAll("\\s*\\(\\d+\\s*,\\s*\\d+\\)\\s*$", "")
            .replaceAll("\\d{3}-[a-zA-Z]+$", "")
            .replaceAll("\\s+\\d+M?$", "")
            .trim();
    String orientation = "";
    var facing =
        Pattern.compile("(000|045|090|135|180|225|270|315)-[a-z]+$", Pattern.CASE_INSENSITIVE)
            .matcher(name);
    if (facing.find()) orientation = facing.group(1);
    else if (n.startsWith("brickdarkwall")) orientation = "SE";
    else if (n.matches("brickwall \\d+")) orientation = "SW";
    else {
      var direction =
          Pattern.compile("(?:corner|wall)(NE|NW|SE|SW|N|S|E|W)$", Pattern.CASE_INSENSITIVE)
              .matcher(name);
      if (direction.find()) orientation = direction.group(1).toUpperCase(Locale.ROOT);
    }
    String label =
        name.replaceFirst("^(?:64k|32k)", "")
            .replaceAll("([a-z])([A-Z])", "$1 $2")
            .replace('_', ' ');
    return new Asset(
        name, label, family, category, material, orientation, width, height, ground, offX, offY);
  }

  public List<Asset> search(
      String query,
      Category category,
      Material material,
      boolean grouped,
      Set<String> favorites,
      boolean onlyFavorites) {
    String[] tokens = (query == null ? "" : query.trim().toLowerCase(Locale.ROOT)).split("\\s+");
    List<Asset> out = new ArrayList<>();
    Set<String> families = new HashSet<>();
    for (Asset a : assets) {
      if (category != Category.ALL && a.category != category
          || material != Material.ALL && a.material != material
          || onlyFavorites && !favorites.contains(a.name)) continue;
      String hay =
          (a.name + " " + a.label + " " + a.category + " " + a.material + " " + a.orientation)
              .toLowerCase(Locale.ROOT);
      if (Arrays.stream(tokens).anyMatch(t -> !hay.contains(t))) continue;
      if (!grouped || families.add(a.family.toLowerCase(Locale.ROOT))) out.add(a);
    }
    return out;
  }

  public List<Asset> variants(Asset asset) {
    return assets.stream().filter(a -> a.family.equalsIgnoreCase(asset.family)).toList();
  }

  private static boolean has(String n, String... words) {
    return Arrays.stream(words).anyMatch(n::contains);
  }
}
