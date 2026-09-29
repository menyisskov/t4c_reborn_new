package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Writes one portrait PNG per monster (its idle walk-cycle frame, facing the "000" angle) into
 * {@code compendium/data/sprites/monsters/}, plus a {@code monsterSprites.json} sidecar mapping
 * each monster's exact name to its sprite's path relative to {@code compendium/data/}. Run before
 * {@link CompendiumExporter}, which reads that sidecar back in and merges it into each monster's
 * exported record - the same "image tool writes files, JSON tool bundles them in" split
 * {@link MinimapExporter} already uses for zone maps.
 *
 * <p>Reads sprite pixels straight out of the packed sprite database ({@link SpriteBinIO}, already
 * PNG-encoded per entry - see its {@code Packed.png()}) rather than through the game's LibGDX
 * texture atlas, so this runs headless with no GL context, exactly like MinimapExporter.
 *
 * <p>Only covers monsters with a named walk-cycle sprite sheet ({@code walkPattern}, e.g. Bat's
 * {@code "Bat#h"} -&gt; portrait key {@code "Bat000-a"} - see {@link #idleFrameKey}). Monsters
 * drawn through the appearance/item-layered puppet system (no walkPattern of their own) get no
 * portrait; the site simply omits the image for those rather than attempting to composite one.
 */
public final class MonsterSpriteExporter {
  private MonsterSpriteExporter() {}

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    Path outDir = Path.of(args.length > 0 ? args[0] : "compendium/data");
    Path spritesDir = outDir.resolve("sprites/monsters");
    Files.createDirectories(spritesDir);

    MonsterJsonLoader.loadAndRegister();

    Map<String, byte[]> pngByKey = new HashMap<>();
    SpriteBinIO.readAll(
        Path.of(Paths.SPRITE_DIR),
        Paths.SPRITE_BIN_BASE,
        packed -> pngByKey.putIfAbsent(SpriteBinIO.key(packed.name()), packed.png()));

    Map<String, String> spriteByMonster = new TreeMap<>();
    Set<String> usedFileNames = new HashSet<>();
    int written = 0;
    for (MonsterDef def : MonsterRegistry.load()) {
      String frameKey = idleFrameKey(def.getWalkPattern());
      if (frameKey == null) continue;
      byte[] png = pngByKey.get(SpriteBinIO.key(frameKey));
      if (png == null) continue;

      String fileName = uniqueFileName(def.getName(), usedFileNames);
      Files.write(spritesDir.resolve(fileName), png);
      spriteByMonster.put(def.getName(), "sprites/monsters/" + fileName);
      written++;
    }

    writeJson(outDir.resolve("monsterSprites.json"), spriteByMonster);
    System.out.println(
        "Wrote " + written + " monster portraits to " + spritesDir + " (" + outDir.resolve("monsterSprites.json") + ")");
  }

  /**
   * Reconstructs the exact sprite key MonsterAnimations#render draws for frame 0 of the walk
   * cycle, standing still - see its {@code PatternParts.from} and the
   * {@code patternBase + angle + "-" + letter} assembly. Angle "000" is the game's default facing
   * (matches EntityAnimationsBase#ANGLES[0]), used here as the single representative pose.
   */
  private static String idleFrameKey(String walkPattern) {
    if (walkPattern == null || walkPattern.isBlank()) return null;
    // "@" numeric-sequence patterns (e.g. some effect-only "monsters") have no standalone
    // "-a" frame name to look up - skip rather than guess.
    if (walkPattern.lastIndexOf('@') > 0) return null;
    int anglelessSeparator = walkPattern.lastIndexOf('!');
    if (anglelessSeparator > 0) {
      return walkPattern.substring(0, anglelessSeparator) + "-a";
    }
    int hashSeparator = walkPattern.indexOf('#');
    String baseName = hashSeparator >= 0 ? walkPattern.substring(0, hashSeparator) : walkPattern;
    boolean angleless = baseName.endsWith("000");
    return angleless ? baseName + "-a" : baseName + "000-a";
  }

  private static String uniqueFileName(String monsterName, Set<String> used) {
    String base = monsterName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    if (base.isEmpty()) base = "monster";
    String fileName = base + ".png";
    int suffix = 2;
    while (!used.add(fileName)) {
      fileName = base + "_" + suffix + ".png";
      suffix++;
    }
    return fileName;
  }

  private static void writeJson(Path path, Object data) throws java.io.IOException {
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    Files.writeString(path, gson.toJson(data), StandardCharsets.UTF_8);
    System.out.println("Wrote " + path);
  }
}
