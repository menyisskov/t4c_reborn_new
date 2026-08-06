package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.SpriteBinWriter;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Creates the male mithril plate set from the complete {@code PupPlate*} animation set. */
public final class MithrilPlateSpriteSeed {
    private static final String SOURCE_PREFIX = "PupPlate";
    private static final String TARGET_PREFIX = "PupMithrilPlate";

    private MithrilPlateSpriteSeed() {
    }

    public static void main(String[] args) throws Exception {
        Path spritePack = args.length == 0
                ? Path.of("assets/sprites/sprites.bin")
                : Path.of(args[0]);
        Path directory = spritePack.getParent() != null ? spritePack.getParent() : Path.of(".");

        List<SpriteBinWriter.Entry> mithrilSprites = new ArrayList<>();
        List<SpriteBinIO.Packed> sprites = SpriteBinIO.readAllToList(directory, "sprites");
        Map<String, SpriteBinIO.Packed> byName = new HashMap<>();
        for (SpriteBinIO.Packed sprite : sprites) byName.put(sprite.name(), sprite);
        for (SpriteBinIO.Packed sprite : sprites) {
            if (!sprite.name().startsWith(SOURCE_PREFIX)
                    || sprite.name().startsWith(TARGET_PREFIX)) {
                continue;
            }
            try {
                BufferedImage source = ImageIO.read(new ByteArrayInputStream(sprite.png()));
                if (source == null) {
                    throw new IllegalArgumentException("Invalid PNG for " + sprite.name());
                }
                mithrilSprites.add(new SpriteBinWriter.Entry(
                        TARGET_PREFIX + sprite.name().substring(SOURCE_PREFIX.length()),
                        sprite.width(), sprite.height(), sprite.off1X(), sprite.off1Y(),
                        sprite.off2X(), sprite.off2Y(), sprite.type(),
                        recolor(source, sprite, byName)));
            } catch (Exception e) {
                throw new IllegalStateException("Unable to convert " + sprite.name(), e);
            }
        }

        if (mithrilSprites.isEmpty()) {
            throw new IllegalStateException("No " + SOURCE_PREFIX + " sprites found in " + directory);
        }
        SpriteBinWriter.merge(spritePack, mithrilSprites);
        System.out.println("Created " + mithrilSprites.size() + " mithril plate sprites.");
    }

    private static BufferedImage recolor(BufferedImage source, SpriteBinIO.Packed sprite,
            Map<String, SpriteBinIO.Packed> byName) {
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int alpha = argb >>> 24;
                if (alpha == 0) {
                    continue;
                }

                int red = argb >>> 16 & 0xff;
                int green = argb >>> 8 & 0xff;
                int blue = argb & 0xff;
                double luminance = (0.2126 * red + 0.7152 * green + 0.0722 * blue) / 255.0;
                int max = Math.max(red, Math.max(green, blue));
                int min = Math.min(red, Math.min(green, blue));
                double chroma = (max - min) / 255.0;
                boolean warmMetal = red > blue * 1.20 && green > blue * 1.08 && chroma > 0.14;

                int outRed;
                int outGreen;
                int outBlue;
                if (warmMetal) {
                    // Restrained bronze/gold accents from the concept art.
                    outRed = channel(25 + 205 * luminance);
                    outGreen = channel(18 + 150 * luminance);
                    outBlue = channel(14 + 86 * luminance);
                } else {
                    // Dark blue-steel shadows opening into bright, cool mithril highlights.
                    double shaped = Math.pow(luminance, 0.88);
                    outRed = channel(7 + 225 * shaped);
                    outGreen = channel(13 + 230 * shaped);
                    outBlue = channel(23 + 238 * shaped);
                    if (luminance > 0.72) {
                        double silver = (luminance - 0.72) / 0.28;
                        outRed = mix(outRed, 238, silver);
                        outGreen = mix(outGreen, 242, silver);
                        outBlue = mix(outBlue, 246, silver);
                    }
                }
                target.setRGB(x, y, alpha << 24 | outRed << 16 | outGreen << 8 | outBlue);
            }
        }
        if (sprite.name().startsWith(SOURCE_PREFIX + "Body")) {
            addWingedPauldrons(target, sprite, byName);
        }
        return target;
    }

    /** Paints three distinct feather blades on each animated shoulder joint. */
    private static void addWingedPauldrons(BufferedImage image, SpriteBinIO.Packed body,
            Map<String, SpriteBinIO.Packed> byName) {
        String suffix = body.name().substring((SOURCE_PREFIX + "Body").length());
        boolean bowAttack = suffix.startsWith("B");
        String armPrefix = bowAttack ? "PupNakedArm" : SOURCE_PREFIX + "Arm";
        SpriteBinIO.Packed leftArm = byName.get(armPrefix + "L" + suffix);
        SpriteBinIO.Packed rightArm = byName.get(armPrefix + "R" + suffix);
        if (leftArm == null && rightArm == null) return;

        String angle = angleOf(body.name());
        Point torso = torsoCenter(angle);
        Point leftShoulder = leftArm == null ? null : shoulderAnchor(leftArm, torso);
        Point rightShoulder = rightArm == null ? null : shoulderAnchor(rightArm, torso);
        if (leftShoulder == null && rightShoulder == null) return;

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        if (leftShoulder != null) drawPauldron(graphics, body, torso, leftShoulder, "L", angle);
        if (rightShoulder != null) drawPauldron(graphics, body, torso, rightShoulder, "R", angle);
        graphics.dispose();
    }

    private static void drawPauldron(Graphics2D graphics, SpriteBinIO.Packed body, Point torso,
            Point shoulder, String side, String angle) {
        int outward = shoulder.x < torso.x - 1.0 ? -1
                : shoulder.x > torso.x + 1.0 ? 1 : ("L".equals(side) ? 1 : -1);
        double scale = perspectiveScale(angle, outward);
        int rootX = (int) Math.round(shoulder.x - body.off1X());
        int rootY = (int) Math.round(shoulder.y - body.off1Y()) + 3;

        // Seat the oversized shell over the collar instead of balancing it on the arm.
        // This keeps the silhouette massive without making the pauldron look detached.
        rootX -= outward * Math.max(3, (int) Math.round(5 * scale));

        // Huge hooked shell inspired by the reference: roughly twice the former reach.
        int[] shellX = pauldronX(rootX, outward, scale,
                0, 6, 8, 10, 15, 13, 20, 15, 19, 11, 8, 4, 0);
        int[] shellY = pauldronY(rootY, scale,
                -4, -15, -8, -5, -11, -3, 0, 3, 10, 7, 15, 7, 5);
        graphics.setColor(new Color(54, 29, 22, 255));
        graphics.fillPolygon(shellX, shellY, shellX.length);

        int[] goldX = pauldronX(rootX, outward, scale,
                1, 6, 8, 10, 14, 12, 18, 14, 17, 10, 8, 4, 1);
        int[] goldY = pauldronY(rootY, scale,
                -3, -13, -7, -4, -9, -2, 0, 3, 8, 6, 13, 6, 4);
        graphics.setColor(new Color(181, 111, 35, 255));
        graphics.fillPolygon(goldX, goldY, goldX.length);

        // Dark recessed core surrounded by the gold blades.
        int cavityWidth = length(9, scale);
        int cavityHeight = length(11, scale);
        int cavityX = rootX + outward * length(5, scale);
        if (outward < 0) cavityX -= cavityWidth;
        graphics.setColor(new Color(38, 20, 28, 255));
        graphics.fillOval(cavityX, rootY - length(5, scale), cavityWidth, cavityHeight);

        // Hot ruby focus: exaggerated contrast keeps it visible at 1:1 scale.
        int orbSize = Math.max(4, length(6, scale));
        int orbX = rootX + outward * length(8, scale);
        if (outward < 0) orbX -= orbSize;
        int orbY = rootY - length(9, scale);
        graphics.setColor(new Color(75, 5, 24, 255));
        graphics.fillOval(orbX - 1, orbY - 1, orbSize + 2, orbSize + 2);
        graphics.setColor(new Color(220, 17, 58, 255));
        graphics.fillOval(orbX, orbY, orbSize, orbSize);
        graphics.setColor(new Color(255, 137, 156, 255));
        graphics.fillRect(orbX + Math.max(1, orbSize / 4), orbY + 1, 1, 1);

        graphics.setStroke(new BasicStroke(1f));
        graphics.setColor(new Color(247, 190, 72, 255));
        graphics.drawPolyline(goldX, goldY, goldX.length);
    }

    private static int[] pauldronX(int rootX, int outward, double scale, int... values) {
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) result[i] = rootX + outward * length(values[i], scale);
        return result;
    }

    private static int[] pauldronY(int rootY, double scale, int... values) {
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = rootY + (int) Math.round(values[i] * scale);
        }
        return result;
    }

    private static void drawFeather(Graphics2D graphics, int rootX, int rootY, int tipX, int tipY,
            double scale) {
        graphics.setStroke(new BasicStroke(Math.max(3f, (float) (4 * scale)),
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        graphics.setColor(new Color(62, 37, 22, 255));
        graphics.drawLine(rootX, rootY, tipX, tipY);
        graphics.setStroke(new BasicStroke(Math.max(2f, (float) (3 * scale)),
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        graphics.setColor(new Color(157, 99, 45, 255));
        graphics.drawLine(rootX, rootY, tipX, tipY);
        graphics.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        graphics.setColor(new Color(232, 178, 91, 255));
        graphics.drawLine(rootX, rootY, tipX, tipY);
    }

    private static int length(int pixels, double scale) {
        return Math.max(2, (int) Math.round(pixels * scale));
    }

    private static double perspectiveScale(String angle, int outward) {
        return switch (angle) {
            case "045" -> outward < 0 ? 0.72 : 1.0;
            case "090" -> outward < 0 ? 0.62 : 1.0;
            case "135" -> outward < 0 ? 1.0 : 0.72;
            default -> 1.0;
        };
    }

    private static Point torsoCenter(String angle) {
        return switch (angle) {
            case "045" -> new Point(21.5, -43.0);
            case "090" -> new Point(22.5, -41.0);
            case "135" -> new Point(19.5, -41.0);
            case "180" -> new Point(19.5, -42.0);
            default -> new Point(17.5, -44.0);
        };
    }

    private static Point shoulderAnchor(SpriteBinIO.Packed arm, Point torso) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(arm.png()));
            List<Point> pixels = new ArrayList<>();
            double centerX = 0.0;
            double centerY = 0.0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) >>> 24) < 32) continue;
                    Point point = new Point(arm.off1X() + x, arm.off1Y() + y);
                    pixels.add(point);
                    centerX += point.x;
                    centerY += point.y;
                }
            }
            if (pixels.isEmpty()) return null;
            centerX /= pixels.size();
            centerY /= pixels.size();
            double xx = 0.0;
            double xy = 0.0;
            double yy = 0.0;
            for (Point point : pixels) {
                double dx = point.x - centerX;
                double dy = point.y - centerY;
                xx += dx * dx;
                xy += dx * dy;
                yy += dy * dy;
            }
            double axis = 0.5 * Math.atan2(2.0 * xy, xx - yy);
            double axisX = Math.cos(axis);
            double axisY = Math.sin(axis);
            double minimum = Double.POSITIVE_INFINITY;
            double maximum = Double.NEGATIVE_INFINITY;
            for (Point point : pixels) {
                double projection = (point.x - centerX) * axisX + (point.y - centerY) * axisY;
                minimum = Math.min(minimum, projection);
                maximum = Math.max(maximum, projection);
            }
            double margin = Math.max(0.5, (maximum - minimum) * 0.22);
            Point first = endpoint(pixels, centerX, centerY, axisX, axisY, minimum, margin, true);
            Point second = endpoint(pixels, centerX, centerY, axisX, axisY, maximum, margin, false);
            return distanceSquared(first, torso) <= distanceSquared(second, torso) ? first : second;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot locate shoulder in " + arm.name(), e);
        }
    }

    private static Point endpoint(List<Point> pixels, double centerX, double centerY,
            double axisX, double axisY, double extreme, double margin, boolean minimum) {
        double x = 0.0;
        double y = 0.0;
        int count = 0;
        for (Point point : pixels) {
            double projection = (point.x - centerX) * axisX + (point.y - centerY) * axisY;
            if (minimum ? projection <= extreme + margin : projection >= extreme - margin) {
                x += point.x;
                y += point.y;
                count++;
            }
        }
        return new Point(x / Math.max(1, count), y / Math.max(1, count));
    }

    private static double distanceSquared(Point first, Point second) {
        double dx = first.x - second.x;
        double dy = first.y - second.y;
        return dx * dx + dy * dy;
    }

    private static String angleOf(String spriteName) {
        for (String angle : List.of("000", "045", "090", "135", "180")) {
            if (spriteName.contains(angle)) return angle;
        }
        return "000";
    }

    private record Point(double x, double y) {
    }

    private static int mix(int from, int to, double amount) {
        return channel(from + (to - from) * Math.max(0.0, Math.min(1.0, amount)));
    }

    private static int channel(double value) {
        return (int) Math.max(0, Math.min(255, Math.round(value)));
    }
}
