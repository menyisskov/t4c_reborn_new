package com.perso.T4C.tools;

import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.SpriteBinIO;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/** Rebuilds an original-format RT map from the four active 1.25 map/decor layers. */
public final class RtMap125Rebuild {
    private static final int SOURCE_WIDTH = 3072;
    private static final int SOURCE_HEIGHT = 3072;
    private static final int PIXELS_PER_TILE_X = 4;
    private static final int PIXELS_PER_TILE_Y = 2;
    private static final int RT_WIDTH = SOURCE_WIDTH * PIXELS_PER_TILE_X;
    private static final int RT_HEIGHT = SOURCE_HEIGHT * PIXELS_PER_TILE_Y;
    private static final int MAP_BYTES = RT_WIDTH * RT_HEIGHT;
    private static final int PALETTE_BYTES = 256 * 3;
    private static final int WORLD_COUNT = 8;
    private static final int SCALE = 8;
    private static final Pattern INSTANCE_SUFFIX = Pattern.compile("@[-+]?\\d+,[-+]?\\d+$");

    private RtMap125Rebuild() {
    }

    public static void main(String[] args) throws Exception {
        Path output = args.length == 0 ? Path.of(Paths.RT_MAP) : Path.of(args[0]);
        Set<String> requiredSprites = collectRequiredSprites();
        System.out.printf(Locale.ROOT, "RT map: %d sprite names referenced%n", requiredSprites.size());
        Map<String, SpriteAsset> sprites = loadReducedSprites(requiredSprites);
        System.out.printf(Locale.ROOT, "RT map: %d reduced sprites loaded%n", sprites.size());

        byte[][] compressedWorlds = new byte[WORLD_COUNT][];
        int uncompressedSize = MAP_BYTES + PALETTE_BYTES;
        for (MapDefinition definition : MapDefinition.values()) {
            compressedWorlds[definition.getZ()] = buildWorld(definition, sprites);
        }
        byte[] blankWorld = compress(blankPayload());
        for (int world = 0; world < WORLD_COUNT; world++) {
            if (compressedWorlds[world] == null) compressedWorlds[world] = blankWorld;
        }
        writeRtMap(output, uncompressedSize, compressedWorlds);
        System.out.printf(Locale.ROOT, "RT map rebuilt: %s (%,d bytes)%n",
                output.toAbsolutePath(), Files.size(output));
    }

    private static Set<String> collectRequiredSprites() throws Exception {
        Set<String> required = new HashSet<>();
        for (MapDefinition definition : MapDefinition.values()) {
            System.out.println("Scanning " + definition.name() + "...");
            try (MapReader reader = new MapReader(new File(definition.getMapPath()))) {
                validateDimensions(reader, definition);
                for (int y = 0; y < reader.getHeight(); y++) {
                    for (int x = 0; x < reader.getWidth(); x++) {
                        addCandidates(required, reader.getGroundSpriteName(x, y));
                        addCandidates(required, reader.getDecorSpriteName(x, y));
                    }
                }
            }
        }
        return required;
    }

    private static Map<String, SpriteAsset> loadReducedSprites(Set<String> required) throws IOException {
        Map<String, SpriteAsset> result = new HashMap<>();
        SpriteBinIO.readAll(Path.of(Paths.SPRITE_DIR), Paths.SPRITE_BIN_BASE, packed -> {
            String key = key(packed.name());
            if (!required.contains(key) || result.containsKey(key)) return;
            try {
                BufferedImage source = ImageIO.read(new ByteArrayInputStream(packed.png()));
                if (source == null) return;
                int reducedWidth = Math.max(1, (source.getWidth() + SCALE - 1) / SCALE);
                int reducedHeight = Math.max(1, (source.getHeight() + SCALE - 1) / SCALE);
                BufferedImage reduced = new BufferedImage(reducedWidth, reducedHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = reduced.createGraphics();
                try {
                    graphics.setComposite(AlphaComposite.Src);
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics.drawImage(source, 0, 0, reducedWidth, reducedHeight, null);
                } finally {
                    graphics.dispose();
                }
                result.put(key, new SpriteAsset(packed, reduced));
            } catch (IOException e) {
                throw new SpriteReadFailure(e);
            }
        });
        return result;
    }

    private static byte[] buildWorld(MapDefinition definition, Map<String, SpriteAsset> sprites) throws Exception {
        System.out.println("Rendering " + definition.name() + "...");
        BufferedImage image = new BufferedImage(RT_WIDTH, RT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        try (MapReader reader = new MapReader(new File(definition.getMapPath()))) {
            validateDimensions(reader, definition);
            renderGround(reader, sprites, pixels);
            renderDecor(reader, sprites, image);
        }

        System.out.println("  building adaptive palette...");
        IndexedWorld indexed = quantize(image);
        byte[] payload = new byte[MAP_BYTES + PALETTE_BYTES];
        System.arraycopy(indexed.pixels, 0, payload, 0, MAP_BYTES);
        System.arraycopy(indexed.palette, 0, payload, MAP_BYTES, PALETTE_BYTES);
        byte[] compressed = compress(payload);
        System.out.printf(Locale.ROOT, "%s compressed: %,d bytes%n", definition.name(), compressed.length);
        return compressed;
    }

    private static void renderGround(MapReader reader, Map<String, SpriteAsset> sprites,
                                     int[] pixels) {
        int black = 0xff000000;
        int[] previous = new int[PIXELS_PER_TILE_X * PIXELS_PER_TILE_Y];
        for (int y = 0; y < SOURCE_HEIGHT; y++) {
            java.util.Arrays.fill(previous, black);
            for (int x = 0; x < SOURCE_WIDTH; x++) {
                String groundName = reader.getGroundSpriteName(x, y);
                ResolvedAsset resolved = resolve(groundName, sprites);
                if (groundName == null || groundName.isBlank()) {
                    java.util.Arrays.fill(previous, black);
                } else if (resolved != null && !isTmpl(resolved.asset.packed.name())) {
                    int[] colors = resolved.asset.groundColors(resolved.mirror);
                    for (int i = 0; i < previous.length; i++) {
                        if (((colors[i] >>> 24) & 0xff) >= 32) previous[i] = colors[i] | 0xff000000;
                    }
                } else if (x == 0 && y > 0) {
                    for (int py = 0; py < PIXELS_PER_TILE_Y; py++) {
                        int above = ((y - 1) * PIXELS_PER_TILE_Y + py) * RT_WIDTH;
                        System.arraycopy(pixels, above, previous, py * PIXELS_PER_TILE_X, PIXELS_PER_TILE_X);
                    }
                }
                for (int py = 0; py < PIXELS_PER_TILE_Y; py++) {
                    int output = (y * PIXELS_PER_TILE_Y + py) * RT_WIDTH + x * PIXELS_PER_TILE_X;
                    System.arraycopy(previous, py * PIXELS_PER_TILE_X,
                            pixels, output, PIXELS_PER_TILE_X);
                }
            }
            if ((y & 255) == 0) System.out.printf(Locale.ROOT, "  ground %d/%d%n", y, SOURCE_HEIGHT);
        }
    }

    private static void renderDecor(MapReader reader, Map<String, SpriteAsset> sprites, BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            for (int y = 0; y < SOURCE_HEIGHT; y++) {
                List<DecorDraw> row = new ArrayList<>();
                for (int x = 0; x < SOURCE_WIDTH; x++) {
                    ResolvedAsset resolved = resolve(reader.getDecorSpriteName(x, y), sprites);
                    if (resolved == null) continue;
                    SpriteBinIO.Packed packed = resolved.asset.packed;
                    float scaleX = reader.getScaleXFast(x, y);
                    float scaleY = reader.getScaleYFast(x, y);
                    int offX = resolved.mirror ? packed.off2X() : packed.off1X();
                    int offY = resolved.mirror ? packed.off2Y() : packed.off1Y();
                    offX += Math.round(reader.getOffsetXFast(x, y));
                    offY += Math.round(reader.getOffsetYFast(x, y));
                    int drawX = Math.round((x * 32f + offX) / SCALE);
                    int drawY = Math.round((y * 16f + offY) / SCALE);
                    int drawWidth = Math.max(1, Math.round(packed.width() * scaleX / SCALE));
                    int drawHeight = Math.max(1, Math.round(packed.height() * scaleY / SCALE));
                    row.add(new DecorDraw(resolved.asset.image, resolved.mirror, drawX, drawY,
                            drawWidth, drawHeight, reader.getZOrderFast(x, y)));
                }
                row.sort(java.util.Comparator.comparingInt(draw -> draw.zOrder));
                for (DecorDraw draw : row) {
                    int x1 = draw.mirror ? draw.x + draw.width : draw.x;
                    int x2 = draw.mirror ? draw.x : draw.x + draw.width;
                    graphics.drawImage(draw.image, x1, draw.y, x2, draw.y + draw.height,
                            0, 0, draw.image.getWidth(), draw.image.getHeight(), null);
                }
                if ((y & 255) == 0) System.out.printf(Locale.ROOT, "  decor %d/%d%n", y, SOURCE_HEIGHT);
            }
        } finally {
            graphics.dispose();
        }
    }

    private static ResolvedAsset resolve(String rawName, Map<String, SpriteAsset> sprites) {
        if (rawName == null || rawName.isBlank()) return null;
        List<String> candidates = candidates(rawName);
        for (int i = 0; i < candidates.size(); i++) {
            SpriteAsset asset = sprites.get(candidates.get(i));
            if (asset != null) return new ResolvedAsset(asset, i > 0 && hasMirrorSuffix(rawName));
        }
        return null;
    }

    private static void addCandidates(Set<String> names, String rawName) {
        names.addAll(candidates(rawName));
    }

    private static List<String> candidates(String rawName) {
        if (rawName == null || rawName.isBlank()) return List.of();
        String value = rawName.trim();
        int bracket = value.indexOf('[');
        if (bracket >= 0) value = value.substring(0, bracket).trim();
        value = INSTANCE_SUFFIX.matcher(value).replaceFirst("").trim();
        List<String> result = new ArrayList<>(4);
        addUnique(result, key(value));
        if (hasMirrorSuffix(value)) addUnique(result, key(value.substring(0, value.length() - 1).trim()));
        String withoutPalette = stripPaletteSuffix(value);
        if (!withoutPalette.equals(value)) addUnique(result, key(withoutPalette));
        if (hasMirrorSuffix(withoutPalette)) {
            addUnique(result, key(withoutPalette.substring(0, withoutPalette.length() - 1).trim()));
        }
        return result;
    }

    private static String stripPaletteSuffix(String value) {
        if (value.length() < 3 || value.charAt(value.length() - 3) != ' ') return value;
        String suffix = value.substring(value.length() - 2);
        return suffix.equalsIgnoreCase("MA") || suffix.equalsIgnoreCase("GR")
                || suffix.equalsIgnoreCase("BR") || suffix.equalsIgnoreCase("BL")
                ? value.substring(0, value.length() - 3).trim() : value;
    }

    private static boolean hasMirrorSuffix(String value) {
        return value != null && value.length() > 1 && value.endsWith("M");
    }

    private static boolean isTmpl(String value) {
        return value != null && value.regionMatches(true, 0, "tmpl", 0, 4);
    }

    private static void addUnique(List<String> values, String value) {
        if (!value.isBlank() && !values.contains(value)) values.add(value);
    }

    private static String key(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static void validateDimensions(MapReader reader, MapDefinition definition) throws IOException {
        if (reader.getWidth() != SOURCE_WIDTH || reader.getHeight() != SOURCE_HEIGHT) {
            throw new IOException(definition + " must be " + SOURCE_WIDTH + "x" + SOURCE_HEIGHT
                    + ", got " + reader.getWidth() + "x" + reader.getHeight());
        }
    }

    private static IndexedWorld quantize(BufferedImage image) {
        int[] source = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int[] counts = new int[1 << 15];
        long[] sumRed = new long[counts.length];
        long[] sumGreen = new long[counts.length];
        long[] sumBlue = new long[counts.length];
        for (int argb : source) {
            int red = (argb >>> 16) & 0xff;
            int green = (argb >>> 8) & 0xff;
            int blue = argb & 0xff;
            int bin = colorBin(red, green, blue);
            counts[bin]++;
            sumRed[bin] += red;
            sumGreen[bin] += green;
            sumBlue[bin] += blue;
        }

        List<ColorBin> colors = new ArrayList<>();
        for (int bin = 0; bin < counts.length; bin++) {
            if (counts[bin] != 0) {
                colors.add(new ColorBin(bin, counts[bin], sumRed[bin], sumGreen[bin], sumBlue[bin]));
            }
        }
        List<ColorBox> boxes = new ArrayList<>();
        boxes.add(new ColorBox(colors));
        while (boxes.size() < 256) {
            ColorBox selected = boxes.stream().filter(ColorBox::canSplit)
                    .max(Comparator.comparingLong(ColorBox::score)).orElse(null);
            if (selected == null) break;
            boxes.remove(selected);
            boxes.addAll(selected.split());
        }
        boxes.sort(Comparator.comparingInt(ColorBox::luminance));

        byte[] palette = new byte[PALETTE_BYTES];
        int paletteSize = Math.min(256, boxes.size());
        for (int i = 0; i < paletteSize; i++) {
            int rgb = boxes.get(i).averageRgb();
            palette[i * 3] = (byte) (rgb >>> 16);
            palette[i * 3 + 1] = (byte) (rgb >>> 8);
            palette[i * 3 + 2] = (byte) rgb;
        }

        byte[] binToPalette = new byte[counts.length];
        for (int bin = 0; bin < counts.length; bin++) {
            if (counts[bin] == 0) continue;
            int red = (int) (sumRed[bin] / counts[bin]);
            int green = (int) (sumGreen[bin] / counts[bin]);
            int blue = (int) (sumBlue[bin] / counts[bin]);
            int bestIndex = 0;
            int bestDistance = Integer.MAX_VALUE;
            for (int i = 0; i < paletteSize; i++) {
                int dr = red - (palette[i * 3] & 0xff);
                int dg = green - (palette[i * 3 + 1] & 0xff);
                int db = blue - (palette[i * 3 + 2] & 0xff);
                int distance = dr * dr + dg * dg + db * db;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = i;
                }
            }
            binToPalette[bin] = (byte) bestIndex;
        }

        byte[] indexed = new byte[source.length];
        for (int i = 0; i < source.length; i++) {
            int argb = source[i];
            indexed[i] = binToPalette[colorBin((argb >>> 16) & 0xff, (argb >>> 8) & 0xff, argb & 0xff)];
        }
        return new IndexedWorld(indexed, palette);
    }

    private static int colorBin(int red, int green, int blue) {
        return ((red >>> 3) << 10) | ((green >>> 3) << 5) | (blue >>> 3);
    }

    private static byte[] blankPayload() {
        byte[] payload = new byte[MAP_BYTES + PALETTE_BYTES];
        return payload;
    }

    private static byte[] compress(byte[] payload) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(payload.length / 2);
        try (DeflaterOutputStream out = new DeflaterOutputStream(bytes,
                new Deflater(Deflater.BEST_COMPRESSION), 1 << 20)) {
            out.write(payload);
        }
        return bytes.toByteArray();
    }

    private static void writeRtMap(Path output, int uncompressedSize, byte[][] worlds) throws IOException {
        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = output.resolveSibling(output.getFileName() + ".tmp");
        int offset = 4 + WORLD_COUNT * 4 + WORLD_COUNT * 4;
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(temporary))) {
            BinaryIOUtils.writeIntLE(out, uncompressedSize);
            for (byte[] world : worlds) BinaryIOUtils.writeIntLE(out, world.length);
            for (byte[] world : worlds) {
                BinaryIOUtils.writeIntLE(out, offset);
                offset += world.length;
            }
            for (byte[] world : worlds) out.write(world);
        }
        Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
    }

    private record ResolvedAsset(SpriteAsset asset, boolean mirror) {
    }

    private record DecorDraw(BufferedImage image, boolean mirror, int x, int y,
                             int width, int height, int zOrder) {
    }

    private record IndexedWorld(byte[] pixels, byte[] palette) {
    }

    private record ColorBin(int bin, int count, long sumRed, long sumGreen, long sumBlue) {
        private int red() { return (int) (sumRed / count); }
        private int green() { return (int) (sumGreen / count); }
        private int blue() { return (int) (sumBlue / count); }
    }

    private static final class ColorBox {
        private final List<ColorBin> colors;
        private final long population;
        private final int minRed;
        private final int maxRed;
        private final int minGreen;
        private final int maxGreen;
        private final int minBlue;
        private final int maxBlue;

        private ColorBox(List<ColorBin> colors) {
            this.colors = new ArrayList<>(colors);
            long population = 0;
            int minRed = 255, minGreen = 255, minBlue = 255;
            int maxRed = 0, maxGreen = 0, maxBlue = 0;
            for (ColorBin color : colors) {
                population += color.count;
                minRed = Math.min(minRed, color.red());
                maxRed = Math.max(maxRed, color.red());
                minGreen = Math.min(minGreen, color.green());
                maxGreen = Math.max(maxGreen, color.green());
                minBlue = Math.min(minBlue, color.blue());
                maxBlue = Math.max(maxBlue, color.blue());
            }
            this.population = population;
            this.minRed = minRed;
            this.maxRed = maxRed;
            this.minGreen = minGreen;
            this.maxGreen = maxGreen;
            this.minBlue = minBlue;
            this.maxBlue = maxBlue;
        }

        private boolean canSplit() { return colors.size() > 1; }

        private long score() {
            return population * (1L + Math.max(maxRed - minRed,
                    Math.max(maxGreen - minGreen, maxBlue - minBlue)));
        }

        private List<ColorBox> split() {
            int redRange = maxRed - minRed;
            int greenRange = maxGreen - minGreen;
            int blueRange = maxBlue - minBlue;
            Comparator<ColorBin> comparator = redRange >= greenRange && redRange >= blueRange
                    ? Comparator.comparingInt(ColorBin::red)
                    : greenRange >= blueRange ? Comparator.comparingInt(ColorBin::green)
                    : Comparator.comparingInt(ColorBin::blue);
            colors.sort(comparator);
            long half = population / 2;
            long accumulated = 0;
            int split = 1;
            for (; split < colors.size(); split++) {
                accumulated += colors.get(split - 1).count;
                if (accumulated >= half) break;
            }
            split = Math.max(1, Math.min(colors.size() - 1, split));
            return List.of(new ColorBox(colors.subList(0, split)),
                    new ColorBox(colors.subList(split, colors.size())));
        }

        private int averageRgb() {
            long red = 0, green = 0, blue = 0, total = 0;
            for (ColorBin color : colors) {
                red += color.sumRed;
                green += color.sumGreen;
                blue += color.sumBlue;
                total += color.count;
            }
            if (total == 0) return 0;
            return ((int) (red / total) << 16) | ((int) (green / total) << 8) | (int) (blue / total);
        }

        private int luminance() {
            int rgb = averageRgb();
            return (((rgb >>> 16) & 0xff) * 3) + (((rgb >>> 8) & 0xff) * 6) + (rgb & 0xff);
        }
    }

    private static final class SpriteAsset {
        private final SpriteBinIO.Packed packed;
        private final BufferedImage image;
        private final Map<Boolean, int[]> groundColors = new HashMap<>();

        private SpriteAsset(SpriteBinIO.Packed packed, BufferedImage image) {
            this.packed = packed;
            this.image = image;
        }

        private int[] groundColors(boolean mirror) {
            return groundColors.computeIfAbsent(mirror, ignored -> {
                int[] colors = new int[PIXELS_PER_TILE_X * PIXELS_PER_TILE_Y];
                for (int y = 0; y < PIXELS_PER_TILE_Y; y++) {
                    int sampleY = Math.min(image.getHeight() - 1,
                            y * image.getHeight() / PIXELS_PER_TILE_Y);
                    for (int x = 0; x < PIXELS_PER_TILE_X; x++) {
                        int sampleX = Math.min(image.getWidth() - 1,
                                x * image.getWidth() / PIXELS_PER_TILE_X);
                        if (mirror) sampleX = image.getWidth() - 1 - sampleX;
                        colors[y * PIXELS_PER_TILE_X + x] = image.getRGB(sampleX, sampleY);
                    }
                }
                return colors;
            });
        }
    }

    private static final class SpriteReadFailure extends RuntimeException {
        private SpriteReadFailure(IOException cause) {
            super(cause);
        }
    }
}
