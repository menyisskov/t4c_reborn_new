package com.perso.T4C.helper;

import com.perso.T4C.config.Paths;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class DdaToSpriteBin {
  private static final String DEFAULT_FILTER = "*manarmor*";

  private DdaToSpriteBin() {}

  public static void main(String[] args) throws Exception {
    if (args.length > 0 && "png".equalsIgnoreCase(args[0])) {
      Path ddaDir = Path.of(args.length > 1 ? args[1] : Paths.DDA_DIR);
      Path outputDir = Path.of(args.length > 2 ? args[2] : Paths.SPRITE_PNG_DIR);
      int palette = args.length > 3 ? Integer.parseInt(args[3]) : 1;
      String filter = args.length > 4 ? args[4] : DEFAULT_FILTER;
      generatePngFiles(ddaDir, outputDir, palette, filter);
      return;
    }
    Path ddaDir = Path.of(args.length > 0 ? args[0] : Paths.DDA_DIR);
    Path output = Path.of(args.length > 1 ? args[1] : Paths.SPRITE_BIN);
    int palette = args.length > 2 ? Integer.parseInt(args[2]) : 1;
    String filter = args.length > 3 ? args[3] : DEFAULT_FILTER;
    boolean nms = args.length > 4 && "--nms".equalsIgnoreCase(args[4]);
    generateSpriteBin(ddaDir, output, palette, filter, nms);
  }

  public static int generateSpriteBin(Path ddaDir, Path output, int paletteIndex) throws Exception {
    return generateSpriteBin(ddaDir, output, paletteIndex, "*");
  }

  public static int generateSpriteBin(
      Path ddaDir, Path output, int paletteIndex, String filterPattern) throws Exception {
    return generateSpriteBin(ddaDir, output, paletteIndex, filterPattern, false);
  }

  public static int generateSpriteBin(
      Path ddaDir, Path output, int paletteIndex, String filterPattern, boolean nmsLibrary)
      throws Exception {
    long t0 = System.currentTimeMillis();
    try (DdaExtractor extractor = new DdaExtractor(ddaDir, nmsLibrary)) {
      int n = extractor.mergeSpriteBin(output, paletteIndex, nameFilter(filterPattern));
      System.out.printf(
          "Done: %d sprites matching '%s' merged into %s (%.1fs)%n",
          n, filterPattern, output, (System.currentTimeMillis() - t0) / 1000.0);
      return n;
    }
  }

  public static int generatePngFiles(Path ddaDir, Path outputDir, int paletteIndex)
      throws Exception {
    return generatePngFiles(ddaDir, outputDir, paletteIndex, "*");
  }

  public static int generatePngFiles(
      Path ddaDir, Path outputDir, int paletteIndex, String filterPattern) throws Exception {
    long t0 = System.currentTimeMillis();
    try (DdaExtractor extractor = new DdaExtractor(ddaDir)) {
      int n = extractor.extractAll(outputDir, paletteIndex, nameFilter(filterPattern));
      System.out.printf(
          "Done: %d sprites matching '%s' exported as PNG into %s (%.1fs)%n",
          n, filterPattern, outputDir, (System.currentTimeMillis() - t0) / 1000.0);
      return n;
    }
  }

  private static Predicate<DidReader.Entry> nameFilter(String filterPattern) {
    if ("ALL".equalsIgnoreCase(filterPattern)) {
      return entry -> true;
    }
    Pattern[] patterns =
        java.util.Arrays.stream(filterPattern.split(";"))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .map(value -> Pattern.compile(globToRegex(value), Pattern.CASE_INSENSITIVE))
            .toArray(Pattern[]::new);
    return entry ->
        java.util.Arrays.stream(patterns)
            .anyMatch(
                pattern ->
                    pattern.matcher(entry.name).matches()
                        || pattern.matcher(entry.atlas).matches());
  }

  private static String globToRegex(String glob) {
    StringBuilder regex = new StringBuilder("^");
    for (int i = 0; i < glob.length(); i++) {
      char c = glob.charAt(i);
      switch (c) {
        case '*' -> regex.append(".*");
        case '?' -> regex.append('.');
        case '.', '\\', '+', '(', ')', '^', '$', '{', '}', '[', ']', '|' ->
            regex.append('\\').append(c);
        default -> regex.append(c);
      }
    }
    return regex.append('$').toString();
  }
}
