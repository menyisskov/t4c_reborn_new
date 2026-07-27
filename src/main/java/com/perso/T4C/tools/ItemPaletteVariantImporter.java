package com.perso.T4C.tools;

import com.perso.T4C.helper.DdaExtractor;
import com.perso.T4C.helper.DidReader;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Imports every palette-qualified item sprite reference from the GoN DDA libraries. */
public final class ItemPaletteVariantImporter {
    private static final Pattern QUALIFIED = Pattern.compile("^(.+)__pal([0-9]+)$");

    private ItemPaletteVariantImporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException(
                "Usage: ItemPaletteVariantImporter <dda-directory> <sprites.bin>");
        Path ddaDir = Path.of(args[0]);
        Path output = Path.of(args[1]);
        Map<String, Integer> requested = new LinkedHashMap<>();
        Set<String> unqualified = new LinkedHashSet<>();
        for (ItemDefinition def : ItemRegistry.load()) {
            collect(def.getAppearanceInventory(), requested, unqualified);
            collect(def.getAppearanceEquippedPrimary(), requested, unqualified);
            collect(def.getAppearanceEquippedSecondary(), requested, unqualified);
        }

        DidReader baseDid = new DidReader(ddaDir.resolve("v2datai.did"));
        DidReader nmsDid = new DidReader(ddaDir.resolve("v2nmsdatai.did"));
        Map<String, Integer> base = new LinkedHashMap<>();
        Map<String, Integer> nms = new LinkedHashMap<>();
        Set<String> baseDefault = new LinkedHashSet<>();
        Set<String> nmsDefault = new LinkedHashSet<>();
        for (String source : unqualified) {
            boolean inBase = containsBase(baseDid, source);
            boolean inNms = containsBase(nmsDid, source);
            if (inNms && preferNmsSource(source)) nmsDefault.add(source);
            else if (inBase) baseDefault.add(source);
            else if (inNms) nmsDefault.add(source);
        }
        for (Map.Entry<String, Integer> variant : requested.entrySet()) {
            String source = sourceBase(variant.getKey(), variant.getValue());
            boolean inBase = containsBase(baseDid, source);
            boolean inNms = containsBase(nmsDid, source);
            if (inNms && preferNmsSource(source)) nms.put(variant.getKey(), variant.getValue());
            else if (inBase) base.put(variant.getKey(), variant.getValue());
            else if (inNms) nms.put(variant.getKey(), variant.getValue());
            else System.out.println("PALETTE_SOURCE_MISSING " + variant.getKey());
        }
        int importedBase;
        int importedNms;
        try (DdaExtractor extractor = new DdaExtractor(ddaDir, false)) {
            importedBase = extractor.mergeSpriteBin(output, 1,
                    entry -> belongsToAny(entry.name, baseDefault));
            importedBase += extractor.mergeSpriteBinPaletteVariants(output, base);
        }
        try (DdaExtractor extractor = new DdaExtractor(ddaDir, true)) {
            importedNms = extractor.mergeSpriteBin(output, 1,
                    entry -> belongsToAny(entry.name, nmsDefault));
            importedNms += extractor.mergeSpriteBinPaletteVariants(output, nms);
        }
        System.out.printf("Item sprites: defaults=%d variants=%d baseFamilies=%d nmsFamilies=%d frames=%d%n",
                unqualified.size(), requested.size(), baseDefault.size() + base.size(),
                nmsDefault.size() + nms.size(), importedBase + importedNms);
    }

    private static void collect(String reference, Map<String, Integer> variants, Set<String> unqualified) {
        if (reference == null) return;
        Matcher matcher = QUALIFIED.matcher(reference);
        if (matcher.matches()) variants.put(reference, Integer.parseInt(matcher.group(2)));
        else unqualified.add(reference);
    }

    private static boolean belongsToAny(String spriteName, Set<String> bases) {
        String lower = spriteName.toLowerCase(Locale.ROOT);
        return bases.stream().anyMatch(base -> belongsToBase(lower, base.toLowerCase(Locale.ROOT)));
    }

    private static boolean belongsToBase(String spriteName, String base) {
        if (!spriteName.startsWith(base)) return false;
        String suffix = spriteName.substring(base.length());
        return suffix.isEmpty() || (suffix.length() >= 3 && Character.isDigit(suffix.charAt(0)))
                || (suffix.length() >= 4 && (suffix.charAt(0) == 'a' || suffix.charAt(0) == 'b')
                && Character.isDigit(suffix.charAt(1)));
    }

    private static boolean preferNmsSource(String source) {
        String lower = source.toLowerCase(Locale.ROOT);
        return lower.startsWith("nms_") || lower.startsWith("nightmare");
    }

    private static String sourceBase(String qualified, int palette) {
        return qualified.substring(0, qualified.length() - ("__pal" + palette).length());
    }

    private static boolean containsBase(DidReader did, String base) {
        String lower = base.toLowerCase(Locale.ROOT);
        return did.getEntries().stream().anyMatch(entry -> {
            String name = entry.name.toLowerCase(Locale.ROOT);
            if (!name.startsWith(lower)) return false;
            String suffix = name.substring(lower.length());
            return suffix.isEmpty() || (suffix.length() >= 3 && Character.isDigit(suffix.charAt(0)))
                    || (suffix.length() >= 4 && (suffix.charAt(0) == 'a' || suffix.charAt(0) == 'b')
                    && Character.isDigit(suffix.charAt(1)));
        });
    }
}
