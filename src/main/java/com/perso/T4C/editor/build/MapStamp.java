package com.perso.T4C.editor.build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perso.T4C.helper.MapReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiPredicate;

/** A portable scenery selection. Coordinates are relative; actors and travel links are excluded. */
public record MapStamp(
    int version,
    String name,
    String category,
    String source,
    int width,
    int height,
    List<Cell> cells) {
  public static final int MAX_CELLS = 65_536;

  public record Tile(
      String ground,
      String decor,
      float scaleX,
      float scaleY,
      float offsetX,
      float offsetY,
      int depth,
      int collision) {
    public static Tile read(MapReader map, byte[] collision, int x, int y) {
      return new Tile(
          map.getGroundSpriteName(x, y),
          map.getDecorSpriteName(x, y),
          map.getScaleX(x, y),
          map.getScaleY(x, y),
          map.getOffsetX(x, y),
          map.getOffsetY(x, y),
          map.getZOrder(x, y),
          collision[y * map.getWidth() + x] & 255);
    }

    public void write(MapReader map, byte[] collisions, int x, int y) {
      map.setGroundSpriteName(x, y, ground);
      map.setDecorSpriteName(x, y, decor);
      map.setScale(x, y, scaleX, scaleY);
      map.setOffset(x, y, offsetX, offsetY);
      map.setZOrder(x, y, depth);
      collisions[y * map.getWidth() + x] = (byte) collision;
    }
  }

  public record Cell(int x, int y, Tile tile) {}

  public MapStamp {
    if (version != 1
        || name == null
        || name.isBlank()
        || name.length() > 120
        || width < 1
        || height < 1
        || (long) width * height > MAX_CELLS
        || cells == null
        || cells.isEmpty()
        || cells.size() > MAX_CELLS)
      throw new IllegalArgumentException("Invalid template dimensions or name");
    Set<Long> seen = new HashSet<>();
    for (Cell c : cells) {
      if (c == null
          || c.tile == null
          || c.x < 0
          || c.y < 0
          || c.x >= width
          || c.y >= height
          || !seen.add(((long) c.x << 32) | (c.y & 0xffffffffL)))
        throw new IllegalArgumentException("Invalid template cell");
      Tile t = c.tile;
      if (!Float.isFinite(t.scaleX)
          || !Float.isFinite(t.scaleY)
          || t.scaleX <= 0
          || t.scaleY <= 0
          || !Float.isFinite(t.offsetX)
          || !Float.isFinite(t.offsetY)
          || t.collision < 0
          || t.collision > 255) throw new IllegalArgumentException("Invalid template tile");
    }
    cells = List.copyOf(cells);
    category = category == null ? "custom" : category;
    source = source == null ? "" : source;
  }

  public static MapStamp capture(
      MapReader map,
      byte[] collision,
      String name,
      String category,
      String source,
      int x,
      int y,
      int width,
      int height,
      BiPredicate<Integer, Integer> mask) {
    if (x < 0
        || y < 0
        || width < 1
        || height < 1
        || (long) x + width > map.getWidth()
        || (long) y + height > map.getHeight()
        || (long) width * height > MAX_CELLS
        || collision.length != (long) map.getWidth() * map.getHeight())
      throw new IllegalArgumentException("Selection outside map or too large");
    List<Cell> cells = new ArrayList<>();
    for (int dy = 0; dy < height; dy++)
      for (int dx = 0; dx < width; dx++)
        if (mask == null || mask.test(x + dx, y + dy))
          cells.add(new Cell(dx, dy, Tile.read(map, collision, x + dx, y + dy)));
    return new MapStamp(1, name, category, source, width, height, cells);
  }

  public static MapStamp load(Path path) throws IOException {
    if (Files.size(path) > 32L * 1024 * 1024) throw new IOException("Template file is too large");
    try {
      return new Gson().fromJson(Files.readString(path, StandardCharsets.UTF_8), MapStamp.class);
    } catch (RuntimeException e) {
      throw new IOException("Invalid template: " + path, e);
    }
  }

  public Path saveNew(Path directory) throws IOException {
    Files.createDirectories(directory);
    String id = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    if (id.isBlank()) id = "template";
    Path target =
        directory.resolve(id + "-" + UUID.randomUUID().toString().substring(0, 8) + ".json");
    Files.writeString(
        target,
        new GsonBuilder().setPrettyPrinting().create().toJson(this),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE_NEW);
    return target;
  }
}
