package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpawnBinaryIO;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Restores the stationary flag on NPC spawns whose legacy source forbids movement.
 *
 * <p>{@code npc_spawns.bin} predates the {@code SetCanMove(FALSE)} detection now performed by
 * {@link LegacyNpcSourceMigration}, so it still lets scenery walk around: portals drift away from
 * their arch, and so do doors, chests, bookshelves and nexus stones. Re-running the full NPC
 * migration would repair it, but would also rewrite {@code npcs.bin} and every NPC translation;
 * this tool touches the one flag that is wrong.
 *
 * <p>In the original server the rule is a lifecycle call rather than a template trait — every
 * portal shares {@code PortalNPC}, yet each one independently calls {@code SetCanMove(FALSE)} in
 * {@code OnInitialise}. Movement is therefore read from the source of each NPC, exactly as
 * {@link LegacyNpcSourceMigration} does, instead of being inferred from a name or a template.
 */
public final class StaticNpcSpawnMigration {

    private static final Charset SOURCE_CHARSET = Charset.forName("windows-1252");
    private static final Pattern CANNOT_MOVE = Pattern.compile("SetCanMove\\s*\\(\\s*FALSE\\s*\\)");

    /**
     * Fallback for spawns with no legacy source file, such as those imported from the map data.
     *
     * <p>Every one of the hundred portal NPCs in the original server calls {@code
     * SetCanMove(FALSE)}, without a single exception, so a portal that reached the catalogue by
     * another route is stationary too. Without this, {@code MakrshPortal} would drift while
     * {@code MakrshPortal2} through {@code 5}, which do have sources, stay put.
     */
    private static final Pattern SCENERY_BY_NAME = Pattern.compile("(?i).*portal.*");

    private StaticNpcSpawnMigration() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException(
                    "Usage: StaticNpcSpawnMigration <ServeurLib_NPC_DLL_SRC root> [--dry-run]");
        }
        boolean dryRun = "--dry-run".equals(args[args.length - 1]);
        String[] pathParts = dryRun ? java.util.Arrays.copyOf(args, args.length - 1) : args;
        Path root = Path.of(String.join(" ", pathParts));
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("NPC source root not found: " + root);
        }

        Map<String, Boolean> cannotMove = readMovementFromSources(root);
        File spawnFile = new File(Paths.NPC_SPAWNS_BIN);
        List<SpawnBinaryIO.Entry> spawns = SpawnBinaryIO.read(spawnFile);

        List<String> repaired = new ArrayList<>();
        List<String> repairedByName = new ArrayList<>();
        int unknown = 0;
        for (SpawnBinaryIO.Entry spawn : spawns) {
            Boolean isStatic = cannotMove.get(spawn.type);
            boolean fromSource = isStatic != null;
            if (!fromSource) {
                unknown++;
                isStatic = spawn.type != null && SCENERY_BY_NAME.matcher(spawn.type).matches();
            }
            // Only ever adds the flag: an NPC the source lets move must keep moving.
            if (isStatic && !spawn.stationary) {
                spawn.stationary = true;
                (fromSource ? repaired : repairedByName).add(spawn.type);
            }
        }

        System.out.println("spawns=" + spawns.size() + " sources=" + cannotMove.size()
                + " withoutSource=" + unknown);
        System.out.println("made stationary from source: " + repaired.size());
        repaired.forEach(type -> System.out.println("   " + type));
        System.out.println("made stationary by name, no source: " + repairedByName.size());
        repairedByName.forEach(type -> System.out.println("   " + type));

        if (dryRun) {
            System.out.println("(dry run, nothing written)");
            return;
        }
        SpawnBinaryIO.write(spawnFile, spawns);
        System.out.println("Wrote " + spawnFile);
    }

    /** Maps each legacy NPC class name to whether its source forbids movement. */
    private static Map<String, Boolean> readMovementFromSources(Path root) throws Exception {
        Map<String, Boolean> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        try (var projects = Files.list(root)) {
            for (Path project : projects.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("Dll Npcs")).toList()) {
                try (var files = Files.list(project)) {
                    for (Path file : files
                            .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".cpp"))
                            .toList()) {
                        String className = file.getFileName().toString()
                                .replaceAll("(?i)\\.cpp$", "");
                        String source = Files.readString(file, SOURCE_CHARSET);
                        // Comments are blanked first, like LegacyNpcSourceMigration does, so a
                        // disabled call cannot freeze an NPC that is meant to move.
                        result.put(className, CANNOT_MOVE.matcher(withoutComments(source)).find());
                    }
                }
            }
        }
        return result;
    }

    /** Blanks line and block comments, keeping offsets so the regex still matches real code. */
    private static String withoutComments(String source) {
        StringBuilder out = new StringBuilder(source.length());
        boolean line = false;
        boolean block = false;
        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : 0;
            if (line) {
                if (current == '\n') {
                    line = false;
                    out.append(current);
                } else {
                    out.append(' ');
                }
            } else if (block) {
                if (current == '*' && next == '/') {
                    block = false;
                    out.append("  ");
                    i++;
                } else {
                    out.append(current == '\n' ? '\n' : ' ');
                }
            } else if (current == '/' && next == '/') {
                line = true;
                out.append("  ");
                i++;
            } else if (current == '/' && next == '*') {
                block = true;
                out.append("  ");
                i++;
            } else {
                out.append(current);
            }
        }
        return out.toString();
    }
}
