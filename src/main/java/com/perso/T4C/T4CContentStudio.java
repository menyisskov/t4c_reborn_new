package com.perso.T4C;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.AppearanceDefaultsBinaryIO;
import com.perso.T4C.helper.AppearanceDefaultsCatalog;
import com.perso.T4C.helper.ClanRelationsBinaryIO;
import com.perso.T4C.helper.CollisionGenerationService;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.CollisionRuleBinaryIO;
import com.perso.T4C.helper.DecorLayerRuleBinaryIO;
import com.perso.T4C.helper.GroundMosaicBinaryIO;
import com.perso.T4C.helper.ItemIconBinaryIO;
import com.perso.T4C.helper.GroundMosaicCatalog;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.ObjectMappingsBinaryIO;
import com.perso.T4C.helper.ObjectPositionBinaryIO;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.TeleportBinaryIO;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemIconRegistry;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.MonsterClan;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.monster.MonsterRegistry;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.npc.NpcRegistry;
import com.perso.T4C.objects.ObjectPos;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.render.ObjectMapping;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;

import javax.imageio.ImageIO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A local web dashboard for editing T4C content binaries.
 */
public class T4CContentStudio {

    private static final int DEFAULT_PORT = 8082;

    private final Gson gson = new Gson();
    private final List<SpriteEntry> sprites = new ArrayList<>();
    private File currentFile;

    /**
     * Main entry point to start the T4C Content Studio.
     *
     * @param args Command line arguments (unused).
     * @throws IOException If an I/O error occurs during startup.
     */
    public static void main(String[] args) throws IOException {
        new T4CContentStudio().start(DEFAULT_PORT);
    }

    /**
     * Starts the HTTP server on the specified port.
     *
     * @param port The port number to listen on.
     * @throws IOException If an I/O error occurs while starting the server or loading sprites.
     */
    private void start(int port) throws IOException {
        loadSprites(new File(Paths.SPRITE_BIN));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || "/index.html".equals(path)) {
                serveResource(exchange, "web/content-studio/index.html", "text/html; charset=utf-8");
                return;
            }
            handleNotFound(exchange);
        });
        server.createContext("/app.js", exchange -> serveResource(exchange, "web/content-studio/app.js", "application/javascript; charset=utf-8"));
        server.createContext("/app.css", exchange -> serveResource(exchange, "web/content-studio/app.css", "text/css; charset=utf-8"));
        server.createContext("/api/sprites", this::handleSprites);
        server.createContext("/api/sprite", this::handleSpriteImage);
        server.createContext("/api/sounds", this::handleSounds);
        server.createContext("/api/delete", this::handleDelete);
        server.createContext("/api/upload", this::handleUpload);
        server.createContext("/api/replace", this::handleReplace);
        server.createContext("/api/export", this::handleExport);
        server.createContext("/api/rename", this::handleRename);
        server.createContext("/api/offsets", this::handleOffsetsUpdate);
        server.createContext("/api/reload", this::handleReload);
        server.createContext("/api/status", this::handleStatus);
        server.createContext("/api/sprite-bins", this::handleSpriteBins);
        server.createContext("/api/sprite-bin", this::handleSpriteBinSwitch);
        server.createContext("/api/npcs", this::handleNpcs);
        server.createContext("/api/npc-options", this::handleNpcOptions);
        server.createContext("/api/npc-save", this::handleNpcSave);
        server.createContext("/api/npc-delete", this::handleNpcDelete);
        server.createContext("/api/npc-reload", this::handleNpcReload);
        server.createContext("/api/maps", this::handleMaps);
        server.createContext("/api/monsters", this::handleMonsters);
        server.createContext("/api/items", this::handleItems);
        server.createContext("/api/spells", this::handleSpells);
        server.createContext("/api/spawns", this::handleSpawns);
        server.createContext("/api/valid-spawn-position", this::handleValidSpawnPosition);
        server.createContext("/api/map-preview", exchange -> {
            try {
                handleMapPreview(exchange);
            } catch (Throwable t) {
                t.printStackTrace();
                try {
                    sendBadRequest(exchange, "Map preview failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                } catch (Throwable ignored) {
                    exchange.close();
                }
            }
        });
        server.createContext("/api/mapPreview", exchange -> {
            try {
                handleMapPreview(exchange);
            } catch (Throwable t) {
                t.printStackTrace();
                try {
                    sendBadRequest(exchange, "Map preview failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                } catch (Throwable ignored) {
                    exchange.close();
                }
            }
        });
        server.createContext("/api/object-mappings", this::handleObjectMappings);
        server.createContext("/api/object-positions", this::handleObjectPositions);
        server.createContext("/api/teleports", this::handleTeleports);
        server.createContext("/api/clan-relations", this::handleClanRelations);
        server.createContext("/api/decor-layer-rules", this::handleDecorLayerRules);
        server.createContext("/api/item-icons", this::handleItemIcons);
        server.createContext("/api/ground-mosaics", this::handleGroundMosaics);
        server.createContext("/api/appearance-defaults", this::handleAppearanceDefaults);
        server.createContext("/api/concealment", this::handleConcealmentRules);
        server.createContext("/api/collision-rules", this::handleCollisionRules);
        server.createContext("/api/regenerate-collisions", this::handleRegenerateCollisions);

        server.start();
        System.out.println("T4C Content Studio running on http://localhost:" + port);
    }

    /**
     * Handles API requests to list all loaded sprites.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleSprites(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        synchronized (sprites) {
            for (SpriteEntry entry : sprites) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", entry.name);
                item.put("width", entry.width);
                item.put("height", entry.height);
                item.put("type", entry.type);
                item.put("off1X", entry.off1X);
                item.put("off1Y", entry.off1Y);
                item.put("off2X", entry.off2X);
                item.put("off2Y", entry.off2Y);
                items.add(item);
            }
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", items.size());
        response.put("file", currentFile != null ? currentFile.getPath() : "");
        response.put("absoluteFile", currentFile != null ? currentFile.getAbsolutePath() : "");
        response.put("items", items);
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to retrieve the raw PNG image of a specific sprite.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleSpriteImage(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        String path = exchange.getRequestURI().getRawPath();
        if (!path.startsWith("/api/sprite/") || path.length() <= "/api/sprite/".length()) {
            handleNotFound(exchange);
            return;
        }
        String rawName = path.substring("/api/sprite/".length());
        String name = URLDecoder.decode(rawName, StandardCharsets.UTF_8);
        SpriteEntry entry = findByName(name);
        if (entry == null) {
            handleNotFound(exchange);
            return;
        }
        byte[] png;
        synchronized (sprites) {
            png = entry.pngData;
        }
        exchange.getResponseHeaders().add("Content-Type", "image/png");
        exchange.sendResponseHeaders(200, png.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(png);
        }
    }

    private void handleSounds(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        List<String> sounds = new ArrayList<>();
        File dir = new File(Paths.SOUNDS_DIR);
        File[] files = dir.listFiles((parent, name) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            return lower.endsWith(".wav") || lower.endsWith(".mp3") || lower.endsWith(".ogg");
        });
        if (files != null) {
            for (File file : files) {
                sounds.add(file.getName());
            }
        }
        sounds.sort(String.CASE_INSENSITIVE_ORDER);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", sounds.size());
        response.put("items", sounds);
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to delete one or more sprites.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleDelete(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        DeleteRequest request;
        try {
            request = gson.fromJson(readBody(exchange), DeleteRequest.class);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON");
            return;
        }
        if (request == null || request.names == null || request.names.isEmpty()) {
            sendBadRequest(exchange, "No names provided");
            return;
        }
        Set<String> target = new HashSet<>();
        for (String name : request.names) {
            if (name != null) {
                target.add(name.toLowerCase(Locale.ROOT));
            }
        }
        int deleted;
        synchronized (sprites) {
            deleted = (int) sprites.stream()
                    .filter(entry -> target.contains(entry.name.toLowerCase(Locale.ROOT)))
                    .count();
            sprites.removeIf(entry -> target.contains(entry.name.toLowerCase(Locale.ROOT)));
            saveSprites(currentFile);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deleted", deleted);
        response.put("count", sprites.size());
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to upload new sprites.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleUpload(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        UploadRequest request;
        try {
            request = gson.fromJson(readBody(exchange), UploadRequest.class);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON");
            return;
        }
        if (request == null || request.items == null || request.items.isEmpty()) {
            sendBadRequest(exchange, "No items provided");
            return;
        }
        int added = 0;
        synchronized (sprites) {
            for (UploadItem item : request.items) {
                if (item == null || item.name == null || item.dataUrl == null) {
                    continue;
                }
                SpriteEntry entry = createEntryFromDataUrl(item.name, item.dataUrl);
                if (entry == null) {
                    continue;
                }
                addOrReplaceEntry(entry);
                added++;
            }
            if (added > 0) {
                sortSprites();
                saveSprites(currentFile);
            }
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("added", added);
        response.put("count", sprites.size());
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to replace the image data of an existing sprite.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleReplace(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        ReplaceRequest request;
        try {
            request = gson.fromJson(readBody(exchange), ReplaceRequest.class);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON");
            return;
        }
        if (request == null || request.name == null || request.dataUrl == null) {
            sendBadRequest(exchange, "Missing sprite data");
            return;
        }
        SpriteEntry updatedEntry = null;
        synchronized (sprites) {
            SpriteEntry existing = findByName(request.name);
            if (existing == null) {
                sendBadRequest(exchange, "Sprite not found");
                return;
            }
            ImageData imageData = decodeImageData(request.dataUrl, existing.width, existing.height);
            if (imageData == null) {
                sendBadRequest(exchange, "Invalid image");
                return;
            }
            updatedEntry = new SpriteEntry(
                    existing.name,
                    existing.width,
                    existing.height,
                    existing.off1X,
                    existing.off1Y,
                    existing.off2X,
                    existing.off2Y,
                    existing.type,
                    imageData.pngData
            );
            for (int i = 0; i < sprites.size(); i++) {
                if (sprites.get(i).name.equalsIgnoreCase(existing.name)) {
                    sprites.set(i, updatedEntry);
                    break;
                }
            }
            saveSprites(currentFile);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("updated", true);
        response.put("width", updatedEntry.width);
        response.put("height", updatedEntry.height);
        response.put("type", updatedEntry.type);
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to export selected sprites as a ZIP archive.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleExport(HttpExchange exchange) throws IOException {
        List<String> names = new ArrayList<>();
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            ExportRequest request;
            try {
                request = gson.fromJson(readBody(exchange), ExportRequest.class);
            } catch (JsonSyntaxException e) {
                sendBadRequest(exchange, "Invalid JSON");
                return;
            }
            if (request != null && request.names != null) {
                for (String name : request.names) {
                    if (name != null && !name.trim().isEmpty()) {
                        names.add(name.trim());
                    }
                }
            }
        } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getRawQuery();
            names.addAll(parseQueryList(query, "names"));
        } else {
            sendMethodNotAllowed(exchange);
            return;
        }
        if (names.isEmpty()) {
            sendBadRequest(exchange, "No names provided");
            return;
        }
        exchange.getResponseHeaders().add("Content-Type", "application/zip");
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"sprites.zip\"");
        exchange.sendResponseHeaders(200, 0);
        try (ZipOutputStream zip = new ZipOutputStream(exchange.getResponseBody())) {
            for (String name : names) {
                SpriteEntry entry = findByName(name);
                if (entry == null) {
                    continue;
                }
                ZipEntry zipEntry = new ZipEntry(entry.name + ".png");
                zip.putNextEntry(zipEntry);
                zip.write(entry.pngData);
                zip.closeEntry();
            }
        }
    }

    /**
     * Handles API requests to reload the sprites from disk.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleReload(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        loadSprites(currentFile);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", sprites.size());
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to rename an existing sprite.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleRename(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        RenameRequest request;
        try {
            request = gson.fromJson(readBody(exchange), RenameRequest.class);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON");
            return;
        }
        if (request == null || request.oldName == null || request.newName == null) {
            sendBadRequest(exchange, "Missing name");
            return;
        }
        String oldName = request.oldName.trim();
        String newName = request.newName.trim();
        if (oldName.isEmpty() || newName.isEmpty()) {
            sendBadRequest(exchange, "Empty name");
            return;
        }
        synchronized (sprites) {
            for (SpriteEntry entry : sprites) {
                if (entry.name.equalsIgnoreCase(newName)) {
                    sendBadRequest(exchange, "Name already exists");
                    return;
                }
            }
            boolean updated = false;
            for (int i = 0; i < sprites.size(); i++) {
                SpriteEntry entry = sprites.get(i);
                if (entry.name.equalsIgnoreCase(oldName)) {
                    SpriteEntry renamed = new SpriteEntry(
                            newName,
                            entry.width,
                            entry.height,
                            entry.off1X,
                            entry.off1Y,
                            entry.off2X,
                            entry.off2Y,
                            entry.type,
                            entry.pngData
                    );
                    sprites.set(i, renamed);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                sendBadRequest(exchange, "Sprite not found");
                return;
            }
            sortSprites();
            saveSprites(currentFile);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("renamed", true);
        response.put("newName", newName);
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to update the offsets of a specific sprite.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleOffsetsUpdate(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        OffsetsUpdateRequest request;
        try {
            request = gson.fromJson(readBody(exchange), OffsetsUpdateRequest.class);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON");
            return;
        }
        if (request == null || request.name == null) {
            sendBadRequest(exchange, "No sprite name provided");
            return;
        }
        boolean updated = false;
        synchronized (sprites) {
            for (int i = 0; i < sprites.size(); i++) {
                SpriteEntry entry = sprites.get(i);
                if (entry.name.equalsIgnoreCase(request.name)) {
                    SpriteEntry updatedEntry = new SpriteEntry(
                            entry.name,
                            entry.width,
                            entry.height,
                            request.off1X,
                            request.off1Y,
                            request.off2X,
                            request.off2Y,
                            entry.type,
                            entry.pngData
                    );
                    sprites.set(i, updatedEntry);
                    updated = true;
                    break;
                }
            }
            if (updated) {
                saveSprites(currentFile);
            }
        }
        if (!updated) {
            handleNotFound(exchange);
            return;
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("updated", true);
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to check the current status of the editor.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleStatus(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("file", currentFile != null ? currentFile.getPath() : "");
        response.put("absoluteFile", currentFile != null ? currentFile.getAbsolutePath() : "");
        response.put("count", sprites.size());
        writeJson(exchange, response);
    }

    private void handleSpriteBins(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        File spriteDir = new File(Paths.SPRITE_BIN).getParentFile();
        if (spriteDir == null) {
            spriteDir = new File(".");
        }
        File[] files = spriteDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".bin"));
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            // A sharded pack (sprites_0.bin, sprites_1.bin, …) is presented as a single logical
            // "sprites.bin" entry: its shards are not individually selectable packs.
            Map<String, long[]> shardedSizes = new LinkedHashMap<>();
            for (File file : files) {
                String base = shardBaseName(file.getName());
                if (base != null) {
                    shardedSizes.computeIfAbsent(base, k -> new long[1])[0] += file.length();
                }
            }
            for (File file : files) {
                if (shardBaseName(file.getName()) != null) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", file.getName());
                item.put("path", file.getPath());
                item.put("absolutePath", file.getAbsolutePath());
                item.put("size", file.length());
                item.put("active", currentFile != null && sameFile(currentFile, file));
                items.add(item);
            }
            for (Map.Entry<String, long[]> sharded : shardedSizes.entrySet()) {
                File logical = new File(spriteDir, sharded.getKey() + ".bin");
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", logical.getName());
                item.put("path", logical.getPath());
                item.put("absolutePath", logical.getAbsolutePath());
                item.put("size", sharded.getValue()[0]);
                item.put("active", currentFile != null && sameFile(currentFile, logical));
                items.add(item);
            }
            items.sort(Comparator.comparing(item -> String.valueOf(item.get("name")),
                    String.CASE_INSENSITIVE_ORDER));
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("current", currentFile != null ? currentFile.getPath() : "");
        response.put("items", items);
        writeJson(exchange, response);
    }

    private void handleSpriteBinSwitch(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        SpriteBinSwitchRequest request;
        try {
            request = gson.fromJson(readBody(exchange), SpriteBinSwitchRequest.class);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON");
            return;
        }
        File file = resolveSpriteBinFile(request != null ? request.path : null);
        // A sharded pack has no file at its logical path: loadSprites decides via the shards.
        if (file == null) {
            sendBadRequest(exchange, "Sprite bin not found");
            return;
        }
        try {
            loadSprites(file);
        } catch (FileNotFoundException e) {
            sendBadRequest(exchange, "Sprite bin not found");
            return;
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("file", currentFile.getPath());
        response.put("absoluteFile", currentFile.getAbsolutePath());
        response.put("count", sprites.size());
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to list NPC definitions.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleNpcs(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (NpcDef def : sortedNpcDefs()) {
                items.add(npcToMap(def));
            }
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("count", items.size());
            response.put("items", items);
            writeJson(exchange, response);
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<NpcDef> defs;
            try {
                defs = readItemsPayload(exchange).stream()
                        .map(this::npcFromMap).filter(Objects::nonNull)
                        .sorted(Comparator.comparing(NpcDef::getName, String.CASE_INSENSITIVE_ORDER))
                        .toList();
            } catch (DialogValidationException e) {
                sendBadRequest(exchange, e.getMessage());
                return;
            }
            NpcRegistry.save(defs);
            writeSaved(exchange, defs.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    /** Rejects a save whose dialogue graph is malformed (duplicate/dangling node ids, multiple greetings). */
    private static final class DialogValidationException extends RuntimeException {
        DialogValidationException(String message) {
            super(message);
        }
    }

    /**
     * Handles API requests for NPC editor dropdown data.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleNpcOptions(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("bodyParts", Arrays.stream(BodyPart.values()).map(Enum::name).toList());
        response.put("spriteBases", loadAnimatedSpriteBases());
        response.put("spells", loadSpellNames());
        response.put("itemKeys", ItemRegistry.load().stream()
                .filter(d -> d != null && d.getKey() != null && !d.getKey().isBlank())
                .map(ItemDefinition::getKey)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList());
        response.put("trainableStatIds", List.of(
                "attack", "archery", "dodge", "peek", "stun_blow", "powerful_blow",
                "rapid_healing", "first_aid", "parry", "critical_strike",
                "hide", "sneak", "search", "picklock", "armor_penetration", "two_weapons", "rob",
                "strength", "dexterity", "endurance", "intelligence", "wisdom"
        ));
        response.put("actionTypes", Arrays.stream(ActionType.values()).map(Enum::name).toList());
        response.put("questFlagNames", loadQuestFlagNames());
        writeJson(exchange, response);
    }

    /**
     * Suggestion list for the flag-name autocomplete in the dialogue editor,
     * built from every {@code requiredFlag} and {@code SET_FLAG} action already
     * in use across all NPCs. Not a validated registry — quest flags have no
     * central definition, so a name outside this list is still accepted.
     */
    private List<String> loadQuestFlagNames() {
        return List.of();
    }

    /**
     * Handles API requests to create or update an NPC definition.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleNpcSave(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        NpcSaveRequest request;
        try {
            request = gson.fromJson(readBody(exchange), NpcSaveRequest.class);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON");
            return;
        }
        NpcDef def = npcFromRequest(request);
        if (def == null) {
            sendBadRequest(exchange, "Invalid NPC definition");
            return;
        }
        List<NpcDef> defs = new ArrayList<>(NpcRegistry.load());
        String oldName = request.oldName == null ? "" : request.oldName.trim();
        boolean updated = false;
        for (int i = 0; i < defs.size(); i++) {
            NpcDef existing = defs.get(i);
            if (existing != null && existing.getName() != null
                    && (existing.getName().equalsIgnoreCase(def.getName())
                    || (!oldName.isEmpty() && existing.getName().equalsIgnoreCase(oldName)))) {
                defs.set(i, def);
                updated = true;
                break;
            }
        }
        if (!updated) {
            defs.add(def);
        }
        saveNpcDefs(defs);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("saved", true);
        response.put("npc", npcToMap(def));
        response.put("count", defs.size());
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to delete an NPC definition.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleNpcDelete(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        NpcDeleteRequest request;
        try {
            request = gson.fromJson(readBody(exchange), NpcDeleteRequest.class);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON");
            return;
        }
        if (request == null || request.name == null || request.name.trim().isEmpty()) {
            sendBadRequest(exchange, "Missing NPC name");
            return;
        }
        String name = request.name.trim();
        List<NpcDef> defs = new ArrayList<>(NpcRegistry.load());
        int before = defs.size();
        defs.removeIf(def -> def != null && def.getName() != null && def.getName().equalsIgnoreCase(name));
        saveNpcDefs(defs);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deleted", before - defs.size());
        response.put("count", defs.size());
        writeJson(exchange, response);
    }

    /**
     * Handles API requests to reload NPC definitions from disk.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleNpcReload(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        NpcRegistry.invalidate();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", NpcRegistry.load().size());
        writeJson(exchange, response);
    }

    private void handleMaps(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        List<Map<String, Object>> maps = new ArrayList<>();
        File root = new File(Paths.MAPS_DIR);
        if (root.exists()) {
            collectMapBins(root, maps);
        }
        maps.sort(Comparator.comparing(m -> String.valueOf(m.get("displayName")), String.CASE_INSENSITIVE_ORDER));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", maps);
        response.put("count", maps.size());
        writeJson(exchange, response);
    }

    private void handleMonsters(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, Object>> items = MonsterRegistry.load().stream()
                    .sorted(Comparator.comparing(MonsterDef::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(this::monsterToMap)
                    .toList();
            writeCollection(exchange, items);
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, Object>> items = readItemsPayload(exchange);
            List<MonsterDef> defs = items.stream().map(this::monsterFromMap).filter(Objects::nonNull)
                    .sorted(Comparator.comparing(MonsterDef::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            MonsterRegistry.save(defs);
            writeSaved(exchange, defs.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleItems(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, Object>> items = ItemRegistry.load().stream()
                    .sorted(Comparator.comparing(ItemDefinition::getKey, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .map(this::itemToMap)
                    .toList();
            writeCollection(exchange, items);
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, Object>> items = readItemsPayload(exchange);
            List<ItemDefinition> defs = items.stream().map(this::itemFromMap).filter(Objects::nonNull)
                    .sorted(Comparator.comparing(ItemDefinition::getKey, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList();
            ItemRegistry.save(defs);
            writeSaved(exchange, defs.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleSpells(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, Object>> items = SpellRegistry.load().stream()
                    .sorted(Comparator.comparing(SpellData::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(this::spellToMap)
                    .toList();
            writeCollection(exchange, items);
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, Object>> items = readItemsPayload(exchange);
            List<SpellData> defs = items.stream().map(this::spellFromMap).filter(Objects::nonNull)
                    .sorted(Comparator.comparing(SpellData::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            SpellRegistry.save(defs);
            writeSaved(exchange, defs.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleSpawns(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQueryMap(exchange.getRequestURI().getRawQuery());
        String mapPath = query.getOrDefault("map", Paths.MAP);
        String kind = query.getOrDefault("kind", "monster");
        File file = getSpawnFile(kind);
        int mapZ = resolveMapZ(mapPath);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<SpawnBinaryIO.Entry> entries = readSpawns(file).stream()
                    .filter(entry -> entry.z == mapZ)
                    .toList();
            writeCollection(exchange, entries.stream().map(this::spawnToMap).toList());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, Object>> items = readItemsPayload(exchange);
            List<SpawnBinaryIO.Entry> edited = items.stream().map(item -> spawnFromMap(item, kind)).filter(Objects::nonNull).toList();
            // Pin every edited spawn to the map being edited, so a new row left at the
            // default z=0 is not filed under worldmap and then dropped by the merge below.
            edited.forEach(entry -> entry.z = mapZ);
            // The file spans every layer: keep the other maps' spawns, replace only this one's.
            List<SpawnBinaryIO.Entry> entries = new ArrayList<>();
            for (SpawnBinaryIO.Entry entry : readSpawns(file)) {
                if (entry.z != mapZ) {
                    entries.add(entry);
                }
            }
            entries.addAll(edited);
            SpawnBinaryIO.write(file, entries);
            writeSaved(exchange, edited.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleValidSpawnPosition(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        Map<String, String> query = parseQueryMap(exchange.getRequestURI().getRawQuery());
        String mapPath = query.getOrDefault("map", Paths.MAP);
        int originX = integer(query.get("x"), 0);
        int originY = integer(query.get("y"), 0);
        int radius = Math.max(1, Math.min(10, integer(query.get("radius"), 10)));
        File mapFile = new File(mapPath == null || mapPath.isBlank() ? Paths.MAP : mapPath);
        if (!mapFile.exists()) {
            handleNotFound(exchange);
            return;
        }
        try (MapReader reader = MapReader.spriteNamesOnly(mapFile)) {
            CollisionReader collisionReader = null;
            File collisionFile = CollisionGenerationService.collisionFileFor(mapFile);
            if (collisionFile.exists()) {
                collisionReader = new CollisionReader(collisionFile);
            }
            int[] position = findValidSpawnPosition(reader, collisionReader, originX, originY, radius);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("x", position[0]);
            response.put("y", position[1]);
            response.put("fallback", position[0] == originX && position[1] == originY);
            writeJson(exchange, response);
        } catch (Exception e) {
            sendBadRequest(exchange, "Failed to find valid spawn position: " + e.getMessage());
        }
    }

    private void handleMapPreview(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }
            Map<String, String> query = parseQueryMap(exchange.getRequestURI().getRawQuery());
            String mapPath = query.getOrDefault("map", Paths.MAP);
            String kind = query.getOrDefault("kind", "monster");
            int selectedX = integer(query.get("x"), Integer.MIN_VALUE);
            int selectedY = integer(query.get("y"), Integer.MIN_VALUE);
            File mapFile = new File(mapPath == null || mapPath.isBlank() ? Paths.MAP : mapPath);
            if (!mapFile.exists()) {
                handleNotFound(exchange);
                return;
            }
            BufferedImage preview = createMapPreview(mapFile, kind, selectedX, selectedY);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(preview, "png", out);
            byte[] data = out.toByteArray();
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(data);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                sendBadRequest(exchange, "Failed to create map preview: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } catch (Throwable ignored) {
                exchange.close();
            }
        }
    }

    private void handleObjectMappings(HttpExchange exchange) throws IOException {
        File file = new File(Paths.OBJECT_MAPPINGS_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<ObjectMappingsBinaryIO.Entry> entries = readObjectMappings(file);
            writeCollection(exchange, entries.stream().map(this::objectMappingToMap).toList());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<ObjectMappingsBinaryIO.Entry> entries = readItemsPayload(exchange).stream()
                    .map(this::objectMappingFromMap).filter(Objects::nonNull)
                    .sorted(Comparator.comparing(e -> e.logicalName == null ? "" : e.logicalName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            ObjectMappingsBinaryIO.write(file, entries);
            writeSaved(exchange, entries.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleObjectPositions(HttpExchange exchange) throws IOException {
        File file = new File(Paths.OBJECT_POSITIONS_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<ObjectPos> entries = readObjectPositions(file);
            writeCollection(exchange, entries.stream().map(this::objectPosToMap).toList());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<ObjectPos> entries = readItemsPayload(exchange).stream().map(this::objectPosFromMap)
                    .filter(Objects::nonNull).toList();
            ObjectPositionBinaryIO.write(file, entries);
            writeSaved(exchange, entries.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleTeleports(HttpExchange exchange) throws IOException {
        File file = new File(Paths.TELEPORTS_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<TeleportBinaryIO.Entry> entries = readTeleports(file);
            writeCollection(exchange, entries.stream().map(this::teleportToMap).toList());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<TeleportBinaryIO.Entry> entries = readItemsPayload(exchange).stream()
                    .map(this::teleportFromMap).filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(e -> e.id)).toList();
            TeleportBinaryIO.write(file, entries);
            writeSaved(exchange, entries.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleClanRelations(HttpExchange exchange) throws IOException {
        File file = new File(Paths.CLAN_RELATIONS_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<ClanRelationsBinaryIO.Entry> entries = readClanRelations(file);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("items", entries.stream().map(this::clanRelationToMap).toList());
            response.put("count", entries.size());
            response.put("clans", Arrays.stream(MonsterClan.values()).map(Enum::name).toList());
            writeJson(exchange, response);
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<ClanRelationsBinaryIO.Entry> entries = readItemsPayload(exchange).stream()
                    .map(this::clanRelationFromMap).filter(Objects::nonNull).toList();
            ClanRelationsBinaryIO.write(file, entries);
            writeSaved(exchange, entries.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleDecorLayerRules(HttpExchange exchange) throws IOException {
        File file = new File(Paths.DECOR_LAYER_RULES_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Set<String> names = readDecorLayerRules(file);
            writeCollection(exchange, names.stream().map(name -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("sprite", name);
                return item;
            }).toList());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Set<String> names = new LinkedHashSet<>();
            for (Map<String, Object> item : readItemsPayload(exchange)) {
                String sprite = str(item.get("sprite"));
                if (!sprite.isBlank()) {
                    names.add(sprite);
                }
            }
            DecorLayerRuleBinaryIO.write(file, names);
            writeSaved(exchange, names.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleItemIcons(HttpExchange exchange) throws IOException {
        File file = new File(Paths.ITEM_ICONS_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeCollection(exchange, readItemIcons(file).entrySet().stream().map(entry -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("appearanceId", entry.getKey());
                item.put("sprite", entry.getValue());
                return item;
            }).toList());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<Integer, String> icons = new TreeMap<>();
            for (Map<String, Object> item : readItemsPayload(exchange)) {
                int appearanceId = integer(item.get("appearanceId"), 0);
                String sprite = str(item.get("sprite")).trim();
                if (appearanceId > 0 && !sprite.isBlank()) {
                    icons.put(appearanceId, sprite);
                }
            }
            ItemIconBinaryIO.write(file, icons);
            ItemIconRegistry.invalidate();
            writeSaved(exchange, icons.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private Map<Integer, String> readItemIcons(File file) {
        if (file == null || !file.exists()) {
            return new TreeMap<>();
        }
        try {
            return new TreeMap<>(ItemIconBinaryIO.read(file));
        } catch (Exception e) {
            return new TreeMap<>();
        }
    }

    private void handleGroundMosaics(HttpExchange exchange) throws IOException {
        File file = new File(Paths.GROUND_MOSAICS_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeCollection(exchange, readGroundMosaics(file).stream().map(definition -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", definition.id());
                item.put("width", definition.width());
                item.put("height", definition.height());
                item.put("frameCount", definition.frames().size());
                // One frame per line keeps the list editable in a plain textarea; the explicit
                // form needs width*height entries, the template form a single "&x"/"&y" name.
                item.put("frames", String.join("\n", definition.frames()));
                return item;
            }).toList());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<GroundMosaicBinaryIO.Definition> definitions = new ArrayList<>();
            for (Map<String, Object> item : readItemsPayload(exchange)) {
                String id = str(item.get("id")).trim();
                if (id.isBlank()) {
                    continue;
                }
                List<String> frames = Arrays.stream(str(item.get("frames")).split("\\R"))
                        .map(String::trim)
                        .filter(frame -> !frame.isEmpty())
                        .toList();
                definitions.add(new GroundMosaicBinaryIO.Definition(id,
                        integer(item.get("width"), 0), integer(item.get("height"), 0), frames));
            }
            GroundMosaicBinaryIO.write(file, definitions);
            GroundMosaicCatalog.invalidate();
            writeSaved(exchange, definitions.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private List<GroundMosaicBinaryIO.Definition> readGroundMosaics(File file) {
        if (file == null || !file.exists()) {
            return List.of();
        }
        try {
            return GroundMosaicBinaryIO.read(file);
        } catch (Exception e) {
            return List.of();
        }
    }

    private void handleAppearanceDefaults(HttpExchange exchange) throws IOException {
        File file = new File(Paths.APPEARANCE_DEFAULTS_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeCollection(exchange, readAppearanceDefaults(file).nakedParts().stream().map(part -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("gender", part.gender());
                item.put("bodyPart", part.bodyPart());
                item.put("sprite", part.sprite());
                return item;
            }).toList());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<AppearanceDefaultsBinaryIO.NakedPart> parts = new ArrayList<>();
            for (Map<String, Object> item : readItemsPayload(exchange)) {
                String gender = str(item.get("gender")).trim();
                String bodyPart = str(item.get("bodyPart")).trim();
                String sprite = str(item.get("sprite")).trim();
                if (!gender.isBlank() && !bodyPart.isBlank() && !sprite.isBlank()) {
                    parts.add(new AppearanceDefaultsBinaryIO.NakedPart(gender, bodyPart, sprite));
                }
            }
            // Both tables share one file: keep the hiding appearances this view does not edit.
            AppearanceDefaultsBinaryIO.write(file, new AppearanceDefaultsBinaryIO.Defaults(
                    parts, readAppearanceDefaults(file).concealmentRules()));
            AppearanceDefaultsCatalog.invalidate();
            writeSaved(exchange, parts.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleConcealmentRules(HttpExchange exchange) throws IOException {
        File file = new File(Paths.APPEARANCE_DEFAULTS_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeCollection(exchange, readAppearanceDefaults(file).concealmentRules().stream().map(rule -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("triggerSlot", rule.triggerSlot());
                item.put("appearance", rule.appearance());
                item.put("hiddenParts", String.join(",", rule.hiddenParts()));
                item.put("hidesExplicit", rule.hidesExplicit());
                return item;
            }).toList());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<AppearanceDefaultsBinaryIO.ConcealmentRule> rules = new ArrayList<>();
            for (Map<String, Object> item : readItemsPayload(exchange)) {
                String triggerSlot = validBodyPart(str(item.get("triggerSlot")));
                String appearance = str(item.get("appearance")).trim();
                List<String> hiddenParts = Arrays.stream(str(item.get("hiddenParts")).split("[,;\\s]+"))
                        .map(this::validBodyPart).filter(value -> !value.isEmpty()).distinct().toList();
                if (!triggerSlot.isEmpty() && !appearance.isBlank()) {
                    rules.add(new AppearanceDefaultsBinaryIO.ConcealmentRule(triggerSlot, appearance,
                            hiddenParts, bool(item.get("hidesExplicit"), false)));
                }
            }
            // Both tables share one file: keep the naked parts this view does not edit.
            AppearanceDefaultsBinaryIO.write(file, new AppearanceDefaultsBinaryIO.Defaults(
                    readAppearanceDefaults(file).nakedParts(), rules));
            AppearanceDefaultsCatalog.invalidate();
            writeSaved(exchange, rules.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private String validBodyPart(String value) {
        if (value == null || value.isBlank()) return "";
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            BodyPart.valueOf(normalized);
            return normalized;
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private AppearanceDefaultsBinaryIO.Defaults readAppearanceDefaults(File file) {
        if (file == null || !file.exists()) {
            return new AppearanceDefaultsBinaryIO.Defaults(List.of(), List.of());
        }
        try {
            return AppearanceDefaultsBinaryIO.read(file);
        } catch (Exception e) {
            return new AppearanceDefaultsBinaryIO.Defaults(List.of(), List.of());
        }
    }

    private void handleCollisionRules(HttpExchange exchange) throws IOException {
        File file = new File(Paths.COLLISION_RULES_BIN);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            CollisionRuleBinaryIO.CollisionRules rules = file.exists()
                    ? CollisionRuleBinaryIO.read(file)
                    : new CollisionRuleBinaryIO.CollisionRules();
            writeJson(exchange, collisionRulesToMap(rules));
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            CollisionRuleBinaryIO.CollisionRules rules = collisionRulesFromMap(readMapPayload(exchange));
            CollisionRuleBinaryIO.write(file, rules);
            writeSaved(exchange, rules.exactSprites.size() + rules.nameContainsRules.size());
            return;
        }
        sendMethodNotAllowed(exchange);
    }

    private void handleRegenerateCollisions(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }
        Map<String, Object> payload = readMapPayload(exchange);
        boolean all = bool(payload.get("all"), false);
        Map<String, CollisionGenerationService.SpriteMeta> metaByName = buildCollisionSpriteMeta();
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            if (all) {
                List<Map<String, Object>> maps = new ArrayList<>();
                collectMapBins(new File(Paths.MAPS_DIR), maps);
                for (Map<String, Object> map : maps) {
                    CollisionGenerationService.Result result = CollisionGenerationService.regenerate(
                            new File(str(map.get("path"))), metaByName);
                    results.add(collisionResultToMap(result));
                }
            } else {
                String mapPath = str(payload.get("map")).trim();
                if (mapPath.isEmpty()) {
                    mapPath = Paths.MAP;
                }
                CollisionGenerationService.Result result = CollisionGenerationService.regenerate(new File(mapPath), metaByName);
                results.add(collisionResultToMap(result));
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Collision regeneration failed: " + e.getMessage());
            return;
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("regenerated", true);
        response.put("count", results.size());
        response.put("items", results);
        writeJson(exchange, response);
    }

    private List<NpcDef> sortedNpcDefs() {
        List<NpcDef> defs = new ArrayList<>(NpcRegistry.load());
        defs.sort(Comparator.comparing(NpcDef::getName, String.CASE_INSENSITIVE_ORDER));
        return defs;
    }

    private void collectMapBins(File file, List<Map<String, Object>> maps) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    collectMapBins(child, maps);
                }
            }
            return;
        }
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".mapbin")) {
            return;
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("path", normalizePath(file.getPath()));
        item.put("fileName", file.getName());
        item.put("baseName", stripExtension(file.getName()));
        item.put("displayName", stripExtension(file.getName()));
        maps.add(item);
    }

    private void writeCollection(HttpExchange exchange, List<Map<String, Object>> items) throws IOException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", items);
        response.put("count", items.size());
        writeJson(exchange, response);
    }

    private void writeSaved(HttpExchange exchange, int count) throws IOException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("saved", true);
        response.put("count", count);
        writeJson(exchange, response);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readItemsPayload(HttpExchange exchange) throws IOException {
        Map<String, Object> payload = gson.fromJson(readBody(exchange), Map.class);
        Object rawItems = payload == null ? null : payload.get("items");
        if (!(rawItems instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                items.add((Map<String, Object>) map);
            }
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readMapPayload(HttpExchange exchange) throws IOException {
        Map<String, Object> payload = gson.fromJson(readBody(exchange), Map.class);
        return payload == null ? Map.of() : payload;
    }

    private Map<String, Object> monsterToMap(MonsterDef def) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", def.getName());
        item.put("displayName", I18n.placeholderFor("monster", def.getName(), def.getDisplayName()));
        item.put("health", def.getHealth());
        item.put("mana", def.getMana());
        item.put("xpPerHit", def.getXpPerHit());
        item.put("xpOnDeath", def.getXpOnDeath());
        item.put("hitDamageMin", def.getHitDamageMin());
        item.put("hitDamageMax", def.getHitDamageMax());
        item.put("respawnTime", def.getRespawnTime());
        item.put("walkPattern", def.getWalkPattern());
        item.put("attackPattern", def.getAttackPattern());
        item.put("deathPattern", def.getDeathPattern());
        item.put("soundAttack", def.getSoundAttack());
        item.put("soundDeath", def.getSoundDeath());
        item.put("soundHit", def.getSoundHit());
        item.put("goldMin", def.getGoldMin());
        item.put("goldMax", def.getGoldMax());
        item.put("defaultAggressive", def.isDefaultAggressive());
        item.put("animateWhileStationary", def.isAnimateWhileStationary());
        item.put("stationaryAnimationPauseSeconds", def.getStationaryAnimationPauseSeconds());
        item.put("loot", def.getLoot() == null ? List.of() : def.getLoot().stream().map(drop -> {
            Map<String, Object> loot = new LinkedHashMap<>();
            loot.put("item", drop.getItem());
            loot.put("chance", drop.getChance());
            return loot;
        }).toList());
        return item;
    }

    private MonsterDef monsterFromMap(Map<String, Object> item) {
        String name = str(item.get("name")).trim();
        if (name.isEmpty()) return null;
        List<MonsterDef.LootDrop> loot = new ArrayList<>();
        for (Map<String, Object> drop : listOfMaps(item.get("loot"))) {
            String dropItem = str(drop.get("item")).trim();
            if (!dropItem.isEmpty()) loot.add(new MonsterDef.LootDrop(dropItem, flt(drop.get("chance"), 0f)));
        }
        return new MonsterDef(name, str(item.get("displayName")), integer(item.get("health"), 0),
                integer(item.get("mana"), 0), integer(item.get("xpPerHit"), 0), integer(item.get("xpOnDeath"), 0),
                integer(item.get("hitDamageMin"), 0), integer(item.get("hitDamageMax"), 0),
                lng(item.get("respawnTime"), 0L), str(item.get("walkPattern")), emptyToNull(str(item.get("attackPattern"))),
                emptyToNull(str(item.get("deathPattern"))), emptyToNull(str(item.get("soundAttack"))),
                emptyToNull(str(item.get("soundDeath"))), emptyToNull(str(item.get("soundHit"))),
                integer(item.get("goldMin"), 0), integer(item.get("goldMax"), 0), loot,
                bool(item.get("animateWhileStationary"), false),
                flt(item.get("stationaryAnimationPauseSeconds"), 0f),
                0, 0, 0, 0, 0, 0, 0, new int[12],
                1, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                bool(item.get("defaultAggressive"), true) ? 50 : 0, 0, 0, true, new java.util.ArrayList<>());
    }

    private Map<String, Object> itemToMap(ItemDefinition def) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", def.getKey());
        item.put("name", I18n.placeholderFor("item", def.getKey(), def.getName()));
        item.put("bodyPart", def.getBodyPart() == null ? "" : def.getBodyPart().name());
        item.put("appearanceEquippedPrimary", def.getAppearanceEquippedPrimary());
        item.put("secondaryBodyPart", def.getSecondaryBodyPart() == null ? "" : def.getSecondaryBodyPart().name());
        item.put("appearanceEquippedSecondary", def.getAppearanceEquippedSecondary());
        item.put("appearanceInventory", def.getAppearanceInventory());
        item.put("price", def.getPrice());
        item.put("weight", def.getWeight());
        item.put("armorClass", def.getArmorClass());
        item.put("dodgeLost", def.getDodgeLost());
        item.put("minEnd", def.getMinEnd());
        item.put("reqAttack", def.getReqAttack());
        item.put("reqStr", def.getReqStr());
        item.put("reqAgi", def.getReqAgi());
        item.put("minInt", def.getMinInt());
        item.put("minWis", def.getMinWis());
        item.put("attackSpeed", def.getAttackSpeed());
        item.put("unique", def.isUnique());
        item.put("bow", def.isBow());
        item.put("unlimitedUse", def.isUnlimitedUse());
        item.put("dmgFormula", def.getDmgFormula());
        item.put("atkDelay", def.getAtkDelay());
        item.put("canSummon", def.isCanSummon());
        item.put("radiance", def.getRadiance());
        item.put("nbCharges", def.getNbCharges());
        item.put("lockName", def.getLockName());
        item.put("lockDiff", def.getLockDiff());
        item.put("signText", I18n.placeholderFor("item.sign", def.getKey(), def.getSignText()));
        item.put("containerGold", def.getContainerGold());
        item.put("globalRespawn", def.getGlobalRespawn());
        item.put("localRespawn", def.getLocalRespawn());
        return item;
    }

    private ItemDefinition itemFromMap(Map<String, Object> item) {
        String key = str(item.get("key")).trim();
        if (key.isEmpty()) return null;
        return new ItemDefinition(key, str(item.get("name")), parseEnum(BodyPart.class, str(item.get("bodyPart")), null),
                emptyToNull(str(item.get("appearanceEquippedPrimary"))),
                parseEnum(BodyPart.class, str(item.get("secondaryBodyPart")), null),
                emptyToNull(str(item.get("appearanceEquippedSecondary"))),
                emptyToNull(str(item.get("appearanceInventory"))), lng(item.get("price"), 0L), lng(item.get("weight"), 0L),
                dbl(item.get("armorClass"), 0d), lng(item.get("dodgeLost"), 0L), lng(item.get("minEnd"), 0L),
                lng(item.get("reqAttack"), 0L), lng(item.get("reqStr"), 0L), lng(item.get("reqAgi"), 0L),
                lng(item.get("minInt"), 0L), lng(item.get("minWis"), 0L), dbl(item.get("attackSpeed"), 1d),
                bool(item.get("unique"), false), bool(item.get("bow"), false), bool(item.get("unlimitedUse"), false),
                0, 0, 0,
                emptyToNull(str(item.get("dmgFormula"))), emptyToNull(str(item.get("atkDelay"))),
                (int) lng(item.get("radiance"), 0L), (int) lng(item.get("nbCharges"), 0L),
                bool(item.get("canSummon"), false),
                emptyToNull(str(item.get("lockName"))), (int) lng(item.get("lockDiff"), 0L),
                emptyToNull(str(item.get("signText"))), (int) lng(item.get("containerGold"), 0L),
                (int) lng(item.get("globalRespawn"), 0L), (int) lng(item.get("localRespawn"), 0L),
                java.util.Collections.emptyList());
    }

    private Map<String, Object> spellToMap(SpellData spell) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", spell.getKey());
        item.put("name", I18n.resolve(spell.getName()));
        item.put("description", I18n.resolve(spell.getDescription()));
        item.put("manaCost", spell.getManaCost());
        item.put("radius", spell.getRadius());
        item.put("minInt", spell.getMinInt());
        item.put("minWis", spell.getMinWis());
        item.put("minLevel", spell.getMinLevel());
        item.put("attack", spell.isAttack());
        item.put("lineOfSight", spell.isLineOfSight());
        item.put("iconId", spell.getIconId());
        item.put("projectileSpell", spell.getProjectileSpell());
        item.put("impactSpell", spell.getImpactSpell());
        item.put("minDamage", spell.getMinDamage());
        item.put("maxDamage", spell.getMaxDamage());
        item.put("sound", spell.getSound());
        item.put("soundImpact", spell.getSoundImpact());
        item.put("cooldownSeconds", spell.getCooldownSeconds());
        item.put("duration", spell.getDuration());
        item.put("frequency", spell.getFrequency());
        item.put("price", spell.getPrice());
        if (spell.getBuff() == null) {
            item.put("buff", null);
        } else {
            Map<String, Object> buff = new LinkedHashMap<>();
            buff.put("durationSeconds", spell.getBuff().getDurationSeconds());
            buff.put("unlimited", spell.getBuff().getUnlimited());
            List<Map<String, Object>> effects = new ArrayList<>();
            List<SpellData.SpellEffect> sourceEffects = spell.getBuff().getEffects();
            for (int i = 0; sourceEffects != null && i < sourceEffects.size(); i++) {
                SpellData.SpellEffect effect = sourceEffects.get(i);
                if (effect == null) continue;
                Map<String, Object> effectMap = new LinkedHashMap<>();
                effectMap.put("type", effect.getType());
                effectMap.put("attribute", effect.getAttribute());
                effectMap.put("amount", effect.getAmount());
                effectMap.put("description", I18n.placeholderForKey(
                        "spell.effect." + normalizedI18n(spell.getName()) + "." + i, effect.getDescription()));
                effects.add(effectMap);
            }
            buff.put("effects", effects);
            item.put("buff", buff);
        }
        item.put("spellId", spell.getSpellId());
        item.put("element", spell.getElement());
        item.put("targetType", spell.getTargetType());
        item.put("attackType", spell.getAttackType());
        item.put("successRate", spell.getSuccessRate());
        item.put("mentalExhaustion", spell.getMentalExhaustion());
        item.put("physicalExhaustion", spell.getPhysicalExhaustion());
        item.put("attackExhaustion", spell.getAttackExhaustion());
        item.put("visualEffect", spell.getVisualEffect());
        item.put("visualEffectTarget", spell.getVisualEffectTarget());
        item.put("pvp", spell.isPvp());
        item.put("t4cEffects", spell.getT4cEffects());
        return item;
    }

    private SpellData spellFromMap(Map<String, Object> item) {
        String name = str(item.get("name")).trim();
        if (name.isEmpty()) return null;
        SpellData.SpellBuff buff = null;
        if (item.get("buff") instanceof Map<?, ?> buffMap) {
            @SuppressWarnings("unchecked") Map<String, Object> b = (Map<String, Object>) buffMap;
            List<SpellData.SpellEffect> effects = new ArrayList<>();
            for (Map<String, Object> effect : listOfMaps(b.get("effects"))) {
                effects.add(new SpellData.SpellEffect(str(effect.get("type")), str(effect.get("attribute")),
                        str(effect.get("amount")), str(effect.get("description"))));
            }
            buff = new SpellData.SpellBuff(nullableInt(b.get("durationSeconds")), nullableBool(b.get("unlimited")), effects);
        }
        List<SpellData.T4cEffect> originalEffects = new ArrayList<>();
        for (Map<String, Object> effect : listOfMaps(item.get("t4cEffects"))) {
            List<SpellData.T4cEffect.EffectParam> parameters = new ArrayList<>();
            for (Map<String, Object> parameter : listOfMaps(effect.get("parameters"))) {
                parameters.add(new SpellData.T4cEffect.EffectParam(integer(parameter.get("paramId"), 0),
                        emptyToNull(str(parameter.get("expression")))));
            }
            originalEffects.add(new SpellData.T4cEffect(integer(effect.get("effectType"), 0), parameters));
        }
        return new SpellData(name, str(item.get("description")), str(item.get("manaCost")),
                integer(item.get("radius"), 0), integer(item.get("minInt"), 0), integer(item.get("minWis"), 0),
                integer(item.get("minLevel"), 0), bool(item.get("attack"), false), bool(item.get("lineOfSight"), false),
                str(item.get("iconId")), emptyToNull(str(item.get("projectileSpell"))), emptyToNull(str(item.get("impactSpell"))),
                integer(item.get("minDamage"), 0), integer(item.get("maxDamage"), 0), emptyToNull(str(item.get("sound"))),
                emptyToNull(str(item.get("soundImpact"))), integer(item.get("cooldownSeconds"), 0),
                emptyToNull(str(item.get("duration"))), emptyToNull(str(item.get("frequency"))),
                integer(item.get("price"), 0), buff,
                integer(item.get("spellId"), 0), integer(item.get("element"), 0),
                integer(item.get("targetType"), 0), integer(item.get("attackType"), 0),
                emptyToNull(str(item.get("successRate"))), emptyToNull(str(item.get("mentalExhaustion"))),
                emptyToNull(str(item.get("physicalExhaustion"))), emptyToNull(str(item.get("attackExhaustion"))),
                integer(item.get("visualEffect"), 0), integer(item.get("visualEffectTarget"), 0),
                bool(item.get("pvp"), false), originalEffects);
    }

    private Map<String, Object> spawnToMap(SpawnBinaryIO.Entry entry) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", entry.type);
        item.put("x", entry.x);
        item.put("y", entry.y);
        item.put("z", entry.z);
        item.put("stationary", entry.stationary);
        item.put("aggressive", entry.aggressive);
        return item;
    }

    private SpawnBinaryIO.Entry spawnFromMap(Map<String, Object> item, String kind) {
        String type = str(item.get("type")).trim();
        if (type.isEmpty()) return null;
        SpawnBinaryIO.Entry entry = new SpawnBinaryIO.Entry();
        entry.type = type;
        entry.x = integer(item.get("x"), 0);
        entry.y = integer(item.get("y"), 0);
        entry.z = integer(item.get("z"), 0);
        entry.stationary = bool(item.get("stationary"), false);
        if ("npc".equalsIgnoreCase(kind)) {
            entry.stationary = "Darkfang".equals(entry.type);
        }
        entry.aggressive = bool(item.get("aggressive"), false);
        return entry;
    }

    private BufferedImage createMapPreview(File mapFile, String kind, int selectedX, int selectedY) throws Exception {
        final int size = 640;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(15, 23, 42));
        g.fillRect(0, 0, size, size);
        try (MapReader reader = MapReader.spriteNamesOnly(mapFile)) {
            int width = reader.getWidth();
            int height = reader.getHeight();
            for (int py = 0; py < size; py++) {
                int y = Math.round(py / (float) Math.max(1, size - 1) * Math.max(0, height - 1));
                for (int px = 0; px < size; px++) {
                    int x = Math.round(px / (float) Math.max(1, size - 1) * Math.max(0, width - 1));
                    String sprite = reader.getSpriteName(x, y);
                    img.setRGB(px, py, mapColor(sprite).getRGB());
                }
            }
            g.setColor(new Color(255, 255, 255, 45));
            for (int line = 0; line <= size; line += 80) {
                g.drawLine(line, 0, line, size);
                g.drawLine(0, line, size, line);
            }
            if ("object".equalsIgnoreCase(kind) || "objects".equalsIgnoreCase(kind)) {
                for (ObjectPos pos : readObjectPositions(new File(Paths.OBJECT_POSITIONS_BIN))) {
                    drawMapMarker(g, pos.x(), pos.y(), selectedX, selectedY, width, height, size, new Color(59, 130, 246), true);
                }
            } else {
                int mapZ = resolveMapZ(mapFile.getPath());
                List<SpawnBinaryIO.Entry> spawns = readSpawns(getSpawnFile(kind)).stream()
                        .filter(spawn -> spawn.z == mapZ)
                        .toList();
                Color marker = "npc".equalsIgnoreCase(kind) || "npcs".equalsIgnoreCase(kind)
                        ? new Color(34, 197, 94)
                        : new Color(239, 68, 68);
                for (SpawnBinaryIO.Entry spawn : spawns) {
                    drawMapMarker(g, spawn.x, spawn.y, selectedX, selectedY, width, height, size, marker, false);
                }
            }
        } finally {
            g.dispose();
        }
        return img;
    }

    private int[] findValidSpawnPosition(MapReader reader, CollisionReader collisionReader, int originX, int originY, int radius) {
        List<int[]> candidates = new ArrayList<>();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int distance = Math.max(Math.abs(dx), Math.abs(dy));
                if (distance <= radius) {
                    candidates.add(new int[] { originX + dx, originY + dy, distance, Math.abs(dx) + Math.abs(dy) });
                }
            }
        }
        candidates.sort(Comparator
                .comparingInt((int[] item) -> item[2])
                .thenComparingInt(item -> item[3])
                .thenComparingInt(item -> item[1])
                .thenComparingInt(item -> item[0]));
        for (int[] candidate : candidates) {
            int x = candidate[0];
            int y = candidate[1];
            if (isValidSpawnTile(reader, collisionReader, x, y)) {
                return new int[] { x, y };
            }
        }
        if (isValidSpawnTile(reader, collisionReader, originX, originY)) {
            return new int[] { originX, originY };
        }
        return new int[] {
                Math.max(0, Math.min(originX, reader.getWidth() - 1)),
                Math.max(0, Math.min(originY, reader.getHeight() - 1))
        };
    }

    private boolean isValidSpawnTile(MapReader reader, CollisionReader collisionReader, int x, int y) {
        if (x < 0 || y < 0 || x >= reader.getWidth() || y >= reader.getHeight()) {
            return false;
        }
        if (collisionReader != null && collisionReader.hasCollision(x, y)) {
            return false;
        }
        String ground = reader.getGroundSpriteName(x, y);
        return ground != null && !ground.isBlank() && !isBlockedSpawnGround(ground);
    }

    private boolean isBlockedSpawnGround(String spriteName) {
        String normalized = spriteName.toLowerCase(Locale.ROOT);
        return normalized.contains("water")
                || normalized.contains("ocean")
                || normalized.contains("river")
                || normalized.contains("lake")
                || normalized.contains("lava")
                || normalized.contains("black")
                || normalized.contains("void")
                || normalized.contains("blank")
                || normalized.contains("empty");
    }

    private void drawMapMarker(Graphics2D g, long x, long y, int selectedX, int selectedY, int width, int height,
                               int size, Color marker, boolean square) {
        int px = Math.round(x / (float) Math.max(1, width - 1) * (size - 1));
        int py = Math.round(y / (float) Math.max(1, height - 1) * (size - 1));
        boolean selected = x == selectedX && y == selectedY;
        int radius = selected ? 6 : 4;
        int diameter = selected ? 12 : 8;
        g.setColor(selected ? new Color(250, 204, 21) : marker);
        if (square) {
            g.fillRect(px - radius, py - radius, diameter, diameter);
        } else {
            g.fillOval(px - radius, py - radius, diameter, diameter);
        }
        g.setColor(new Color(0, 0, 0, 180));
        g.setStroke(new BasicStroke(selected ? 2f : 1f));
        if (square) {
            g.drawRect(px - radius, py - radius, diameter, diameter);
        } else {
            g.drawOval(px - radius, py - radius, diameter, diameter);
        }
    }

    private Color mapColor(String spriteName) {
        if (spriteName == null || spriteName.isBlank()) {
            return new Color(30, 41, 59);
        }
        String lower = spriteName.toLowerCase(Locale.ROOT);
        if (lower.contains("water")) return new Color(30, 86, 140);
        if (lower.contains("sand") || lower.contains("desert")) return new Color(161, 133, 83);
        if (lower.contains("grass") || lower.contains("forest") || lower.contains("tree")) return new Color(50, 111, 69);
        if (lower.contains("stone") || lower.contains("rock") || lower.contains("wall")) return new Color(91, 98, 112);
        if (lower.contains("snow")) return new Color(187, 198, 207);
        int hash = lower.hashCode();
        int r = 45 + Math.floorMod(hash, 70);
        int gr = 60 + Math.floorMod(hash >> 8, 80);
        int b = 55 + Math.floorMod(hash >> 16, 70);
        return new Color(r, gr, b);
    }

    private Map<String, Object> objectMappingToMap(ObjectMappingsBinaryIO.Entry entry) {
        ObjectMapping mapping = entry.mapping;
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("logicalName", entry.logicalName);
        item.put("id", mapping == null ? 0 : mapping.id);
        item.put("sprite", mapping == null ? "" : mapping.sprite);
        item.put("clickAnimate", mapping != null && mapping.clickAnimate);
        item.put("mirror", mapping != null && mapping.mirror);
        item.put("animateSound", mapping == null ? "" : mapping.animateSound);
        item.put("reverseAnimateSound", mapping == null ? "" : mapping.reverseAnimateSound);
        item.put("alwaysBehindEntities", mapping != null && mapping.alwaysBehindEntities);
        item.put("displayName", mapping == null ? "" : I18n.placeholderFor("object", entry.logicalName, mapping.displayName));
        item.put("depthTileOffsetY", mapping == null ? 0 : mapping.depthTileOffsetY);
        return item;
    }

    private ObjectMappingsBinaryIO.Entry objectMappingFromMap(Map<String, Object> item) {
        String logicalName = str(item.get("logicalName")).trim();
        if (logicalName.isEmpty()) return null;
        ObjectMapping mapping = new ObjectMapping(integer(item.get("id"), 0), str(item.get("sprite")),
                bool(item.get("clickAnimate"), false), bool(item.get("mirror"), false),
                str(item.get("animateSound")), str(item.get("reverseAnimateSound")),
                bool(item.get("alwaysBehindEntities"), false), str(item.get("displayName")),
                integer(item.get("depthTileOffsetY"), 0));
        return new ObjectMappingsBinaryIO.Entry(logicalName, mapping);
    }

    private Map<String, Object> objectPosToMap(ObjectPos pos) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", pos.name());
        item.put("x", pos.x());
        item.put("y", pos.y());
        item.put("z", pos.z());
        return item;
    }

    private ObjectPos objectPosFromMap(Map<String, Object> item) {
        String name = str(item.get("name")).trim();
        if (name.isEmpty()) return null;
        return new ObjectPos(name, lng(item.get("x"), 0L), lng(item.get("y"), 0L), lng(item.get("z"), 0L));
    }

    private Map<String, Object> teleportToMap(TeleportBinaryIO.Entry entry) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", entry.id);
        item.put("sourceZ", entry.sourceZ);
        item.put("sourceX", entry.sourceX);
        item.put("sourceY", entry.sourceY);
        item.put("targetZ", entry.targetZ);
        item.put("targetX", entry.targetX);
        item.put("targetY", entry.targetY);
        return item;
    }

    private TeleportBinaryIO.Entry teleportFromMap(Map<String, Object> item) {
        TeleportBinaryIO.Entry entry = new TeleportBinaryIO.Entry();
        entry.id = integer(item.get("id"), 0);
        entry.sourceZ = integer(item.get("sourceZ"), 0);
        entry.sourceX = integer(item.get("sourceX"), 0);
        entry.sourceY = integer(item.get("sourceY"), 0);
        entry.targetZ = integer(item.get("targetZ"), 0);
        entry.targetX = integer(item.get("targetX"), 0);
        entry.targetY = integer(item.get("targetY"), 0);
        return entry;
    }

    private Map<String, Object> clanRelationToMap(ClanRelationsBinaryIO.Entry entry) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("source", entry.source == null ? MonsterClan.NEUTRAL.name() : entry.source.name());
        item.put("target", entry.target == null ? MonsterClan.NEUTRAL.name() : entry.target.name());
        return item;
    }

    private ClanRelationsBinaryIO.Entry clanRelationFromMap(Map<String, Object> item) {
        return new ClanRelationsBinaryIO.Entry(
                parseEnum(MonsterClan.class, str(item.get("source")), MonsterClan.NEUTRAL),
                parseEnum(MonsterClan.class, str(item.get("target")), MonsterClan.NEUTRAL));
    }

    private Map<String, Object> collisionRulesToMap(CollisionRuleBinaryIO.CollisionRules rules) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("defaultDecorCollision", rules.defaultDecorCollision);
        response.put("defaultCollisionValue", rules.defaultCollisionValue);
        response.put("ignoredSprites", rules.ignoredSprites);
        response.put("exactSprites", rules.exactSprites.entrySet().stream().map(entry -> {
            Map<String, Object> item = ruleToMap(entry.getValue());
            item.put("sprite", entry.getKey());
            return item;
        }).toList());
        response.put("nameContainsRules", rules.nameContainsRules.stream().map(nameRule -> {
            Map<String, Object> item = ruleToMap(nameRule.rule);
            item.put("contains", nameRule.contains);
            return item;
        }).toList());
        return response;
    }

    private CollisionRuleBinaryIO.CollisionRules collisionRulesFromMap(Map<String, Object> map) {
        CollisionRuleBinaryIO.CollisionRules rules = new CollisionRuleBinaryIO.CollisionRules();
        rules.defaultDecorCollision = bool(map.get("defaultDecorCollision"), false);
        rules.defaultCollisionValue = integer(map.get("defaultCollisionValue"), 1);
        rules.ignoredSprites = stringList(map.get("ignoredSprites"));
        rules.exactSprites = new LinkedHashMap<>();
        for (Map<String, Object> item : listOfMaps(map.get("exactSprites"))) {
            String sprite = str(item.get("sprite")).trim();
            if (!sprite.isEmpty()) rules.exactSprites.put(sprite, ruleFromMap(item));
        }
        rules.nameContainsRules = new ArrayList<>();
        for (Map<String, Object> item : listOfMaps(map.get("nameContainsRules"))) {
            CollisionRuleBinaryIO.CollisionNameRule nameRule = new CollisionRuleBinaryIO.CollisionNameRule();
            nameRule.contains = stringList(item.get("contains"));
            nameRule.rule = ruleFromMap(item);
            rules.nameContainsRules.add(nameRule);
        }
        return rules;
    }

    private Map<String, Object> ruleToMap(CollisionRuleBinaryIO.CollisionRule rule) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("value", rule == null ? 1 : rule.value);
        item.put("tiles", tilesToList(rule == null ? null : rule.tiles));
        item.put("clearTiles", tilesToList(rule == null ? null : rule.clearTiles));
        return item;
    }

    private CollisionRuleBinaryIO.CollisionRule ruleFromMap(Map<String, Object> item) {
        CollisionRuleBinaryIO.CollisionRule rule = new CollisionRuleBinaryIO.CollisionRule();
        rule.value = integer(item.get("value"), 1);
        rule.tiles = tilesFromList(item.get("tiles"));
        rule.clearTiles = tilesFromList(item.get("clearTiles"));
        return rule;
    }

    private List<Map<String, Object>> tilesToList(List<int[]> tiles) {
        if (tiles == null) return List.of();
        List<Map<String, Object>> items = new ArrayList<>();
        for (int[] tile : tiles) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("x", tile != null && tile.length > 0 ? tile[0] : 0);
            item.put("y", tile != null && tile.length > 1 ? tile[1] : 0);
            items.add(item);
        }
        return items;
    }

    private List<int[]> tilesFromList(Object raw) {
        List<int[]> tiles = new ArrayList<>();
        for (Map<String, Object> item : listOfMaps(raw)) {
            tiles.add(new int[] { integer(item.get("x"), 0), integer(item.get("y"), 0) });
        }
        return tiles;
    }

    private Map<String, CollisionGenerationService.SpriteMeta> buildCollisionSpriteMeta() {
        Map<String, CollisionGenerationService.SpriteMeta> meta = new HashMap<>();
        synchronized (sprites) {
            for (SpriteEntry sprite : sprites) {
                if (sprite != null && sprite.name != null) {
                    meta.put(sprite.name.toLowerCase(Locale.ROOT),
                            new CollisionGenerationService.SpriteMeta(sprite.name, sprite.width, sprite.height,
                                    sprite.off1X, sprite.off1Y, sprite.off2X, sprite.off2Y));
                }
            }
        }
        return meta;
    }

    private Map<String, Object> collisionResultToMap(CollisionGenerationService.Result result) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("file", normalizePath(result.file()));
        item.put("width", result.width());
        item.put("height", result.height());
        item.put("collisionCount", result.collisionCount());
        return item;
    }

    private void saveNpcDefs(List<NpcDef> defs) throws IOException {
        defs.sort(Comparator.comparing(NpcDef::getName, String.CASE_INSENSITIVE_ORDER));
        NpcRegistry.save(defs);
    }

    private Map<String, Object> npcToMap(NpcDef def) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", def.getName());
        // The editor should show the human-readable value; persistence still
        // converts it back to the localized placeholder when the NPC is saved.
        item.put("displayName", I18n.resolve(def.getDisplayName()));
        item.put("spriteBase", def.getSpriteBase());
        item.put("patrolRadiusTiles", def.getPatrolRadiusTiles());
        List<Map<String, Object>> parts = new ArrayList<>();
        for (NpcDef.Part part : def.getParts()) {
            Map<String, Object> partItem = new LinkedHashMap<>();
            partItem.put("bodyPart", part.getBodyPart() == null ? BodyPart.BODY.name() : part.getBodyPart().name());
            partItem.put("spriteBase", part.getSpriteBase());
            parts.add(partItem);
        }
        item.put("parts", parts);
        List<String> fleeShouts = new ArrayList<>();
        for (int i = 0; i < def.getFleeShouts().size(); i++) {
            fleeShouts.add(I18n.placeholderForKey(
                    "npc.flee_shout." + I18n.normalizedKey(def.getName()) + "." + i,
                    def.getFleeShouts().get(i)));
        }
        item.put("fleeShouts", fleeShouts);
        item.put("welcomeText", I18n.resolve(def.getWelcomeText()));
        List<Map<String, Object>> topics = new ArrayList<>();
        for (NpcDef.DialogTopic topic : def.getTopics()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("keywords", topic.getKeywords().stream().map(I18n::resolve).toList());
            row.put("response", I18n.resolve(topic.getResponse()));
            List<Map<String, Object>> actions = new ArrayList<>();
            for (NpcDef.Action action : topic.getActions()) {
                Map<String, Object> actionRow = new LinkedHashMap<>();
                actionRow.put("type", action.getType() == null ? null : action.getType().name());
                actionRow.put("targets", action.getTargets());
                actions.add(actionRow);
            }
            row.put("actions", actions);
            topics.add(row);
        }
        item.put("topics", topics);
        return item;
    }

    private NpcDef npcFromRequest(NpcSaveRequest request) {
        if (request == null || request.name == null || request.name.trim().isEmpty()) {
            return null;
        }
        List<NpcDef.Part> parts = new ArrayList<>();
        if (request.parts != null) {
            for (NpcPartRequest part : request.parts) {
                if (part == null || part.spriteBase == null || part.spriteBase.trim().isEmpty()) {
                    continue;
                }
                BodyPart bodyPart = parseEnum(BodyPart.class, part.bodyPart, BodyPart.BODY);
                parts.add(new NpcDef.Part(bodyPart, part.spriteBase.trim()));
            }
        }
        // The web editor has no flee-shout field yet, so it is carried over
        // instead of letting an unrelated edit clear it. The dialogue graph is
        // not reachable through this request type (see npcFromMap instead).
        NpcDef existing = NpcRegistry.findByName(request.name.trim());
        return new NpcDef(
                request.name.trim(),
                // Saved as sent: the editor round-trips the ${...} placeholder, and
                // resolving here would bake display text back into npcs.bin.
                trimToEmpty(request.displayName),
                parts,
                emptyToNull(request.spriteBase),
                Math.max(0, request.patrolRadiusTiles),
                existing == null ? List.of() : existing.getFleeShouts(),
                existing == null ? "" : existing.getWelcomeText(),
                existing == null ? List.of() : existing.getTopics()
        );
    }

    private NpcDef npcFromMap(Map<String, Object> item) {
        String name = str(item.get("name")).trim();
        if (name.isEmpty()) {
            return null;
        }
        if (str(item.get("welcomeText")).isBlank()) {
            throw new DialogValidationException("NPC '" + name + "': welcomeText is required");
        }
        List<NpcDef.Part> parts = new ArrayList<>();
        for (Map<String, Object> part : listOfMaps(item.get("parts"))) {
            String sprite = str(part.get("spriteBase")).trim();
            if (!sprite.isEmpty()) {
                parts.add(new NpcDef.Part(parseEnum(BodyPart.class, str(part.get("bodyPart")), BodyPart.BODY), sprite));
            }
        }
        NpcDef existing = NpcRegistry.findByName(name);
        List<NpcDef.DialogTopic> topics = topicsFromMap(name,
                existing == null ? List.of() : existing.getTopics(), listOfMaps(item.get("topics")));
        Map<String, String> welcomeUpdate = new LinkedHashMap<>();
        String welcome = resolveEditedText(existing == null ? null : existing.getWelcomeText(),
                str(item.get("welcomeText")), "npc.welcome." + I18n.normalizedKey(name), welcomeUpdate);
        if (!welcomeUpdate.isEmpty()) I18n.update(welcomeUpdate);
        return new NpcDef(name, str(item.get("displayName")), parts, emptyToNull(str(item.get("spriteBase"))),
                integer(item.get("patrolRadiusTiles"), 0), stringList(item.get("fleeShouts")),
                welcome, topics);
    }

    private List<NpcDef.DialogTopic> topicsFromMap(String npcName, List<NpcDef.DialogTopic> existing,
                                                    List<Map<String, Object>> rows) {
        List<NpcDef.DialogTopic> topics = new ArrayList<>();
        Map<String, String> catalogueUpdates = new LinkedHashMap<>();
        for (int index = 0; index < rows.size(); index++) {
            Map<String, Object> row = rows.get(index);
            NpcDef.DialogTopic old = index < existing.size() ? existing.get(index) : null;
            List<String> keywords = new ArrayList<>();
            List<String> rawKeywords = stringList(row.get("keywords"));
            for (int k = 0; k < rawKeywords.size(); k++) {
                String oldKeyword = old != null && k < old.getKeywords().size() ? old.getKeywords().get(k) : null;
                keywords.add(resolveEditedText(oldKeyword, rawKeywords.get(k),
                        "npc.topic_keyword." + I18n.normalizedKey(npcName) + "." + index + "." + k,
                        catalogueUpdates));
            }
            List<NpcDef.Action> actions = new ArrayList<>();
            for (Map<String, Object> actionRow : listOfMaps(row.get("actions"))) {
                ActionType type = parseEnum(ActionType.class, str(actionRow.get("type")), null);
                if (type == null) throw new DialogValidationException("NPC '" + npcName + "': invalid action type");
                List<String> targets = stringList(actionRow.get("targets")).stream()
                        .map(String::trim).filter(value -> !value.isEmpty()).toList();
                validateActionTargets(npcName, type, targets);
                actions.add(new NpcDef.Action(type, targets));
            }
            String response = resolveEditedText(old == null ? null : old.getResponse(), str(row.get("response")),
                    "npc.topic." + I18n.normalizedKey(npcName) + "." + index, catalogueUpdates);
            topics.add(new NpcDef.DialogTopic(keywords, response, actions));
        }
        if (!catalogueUpdates.isEmpty()) I18n.update(catalogueUpdates);
        return topics;
    }

    private void validateActionTargets(String npcName, ActionType type, List<String> targets) {
        if (type == ActionType.GIVE_ITEM && targets.size() != 1) {
            throw new DialogValidationException("NPC '" + npcName + "': GIVE_ITEM requires exactly one item");
        }
        if ((type == ActionType.HEAL || type == ActionType.END_CONVERSATION) && !targets.isEmpty()) {
            throw new DialogValidationException("NPC '" + npcName + "': " + type + " accepts no target");
        }
        if (type == ActionType.OPEN_SPELL_LEARNING) {
            for (String target : targets) if (SpellRegistry.findByName(target) == null)
                throw new DialogValidationException("NPC '" + npcName + "': unknown spell '" + target + "'");
        } else if (type == ActionType.OPEN_SHOP || type == ActionType.GIVE_ITEM) {
            for (String target : targets) if (ItemRegistry.findByKey(target) == null)
                throw new DialogValidationException("NPC '" + npcName + "': unknown item '" + target + "'");
        } else if (type == ActionType.OPEN_SKILL_LEARNING) {
            Set<String> valid = Set.of("attack", "archery", "dodge", "peek", "stun_blow", "powerful_blow",
                    "rapid_healing", "first_aid", "parry", "critical_strike", "hide", "sneak", "search",
                    "picklock", "armor_penetration", "two_weapons", "rob", "strength", "dexterity",
                    "endurance", "intelligence", "wisdom");
            for (String target : targets) if (!valid.contains(target))
                throw new DialogValidationException("NPC '" + npcName + "': unknown skill '" + target + "'");
        }
    }

    /**
     * Builds the dialogue graph from the editor payload, validating structural
     * integrity and writing edited reply/keyword text into the translation
     * catalogue under each node's id-based key.
     *
     * <p>Unlike the other NPC fields, the graph editor can create, delete, and
     * reconnect nodes, so the full node list is rebuilt from the payload rather
     * than carried over from {@code existingNodes} — that list is consulted only
     * to detect which response/keyword text actually changed.
     *
     * @throws DialogValidationException if a node id is missing/duplicated, a
     *         GOTO_NODE/fallbackNode target does not resolve within the same
     *         payload, or more than one node is marked as the greeting.
     */
    private List<NpcDef.DialogNode> dialogNodesFromMap(String npcName, List<NpcDef.DialogNode> existingNodes,
                                                         List<Map<String, Object>> payloadNodes) {
        if (payloadNodes.isEmpty()) {
            return List.of();
        }
        Map<String, NpcDef.DialogNode> existingById = new LinkedHashMap<>();
        for (NpcDef.DialogNode node : existingNodes) {
            existingById.put(node.getId(), node);
        }
        Set<String> ids = new LinkedHashSet<>();
        Set<String> greetings = new LinkedHashSet<>();
        List<NpcDef.DialogNode> nodes = new ArrayList<>();
        Map<String, String> catalogueUpdates = new LinkedHashMap<>();
        for (Map<String, Object> row : payloadNodes) {
            String id = str(row.get("id")).trim();
            if (id.isEmpty()) {
                throw new DialogValidationException("NPC '" + npcName + "': every dialogue node needs an id");
            }
            if (!ids.add(id)) {
                throw new DialogValidationException("NPC '" + npcName + "': duplicate dialogue node id '" + id + "'");
            }
            boolean greeting = bool(row.get("greeting"), false);
            if (greeting) {
                greetings.add(id);
            }
            NpcDef.DialogNode existingNode = existingById.get(id);
            String responseKey = "npc.topic." + I18n.normalizedKey(npcName) + "." + id;
            String editedResponse = str(row.get("response"));
            String response = resolveEditedText(existingNode == null ? null : existingNode.getResponse(),
                    editedResponse, responseKey, catalogueUpdates);
            List<String> keywords = new ArrayList<>();
            List<String> rawKeywords = stringList(row.get("keywords"));
            List<String> existingKeywords = existingNode == null ? List.of() : existingNode.getKeywords();
            for (int k = 0; k < rawKeywords.size(); k++) {
                String keywordKey = "npc.topic_keyword." + I18n.normalizedKey(npcName) + "." + id + "." + k;
                String existingKeyword = k < existingKeywords.size() ? existingKeywords.get(k) : null;
                keywords.add(resolveEditedText(existingKeyword, rawKeywords.get(k), keywordKey, catalogueUpdates));
            }
            List<NpcDef.Action> actions = new ArrayList<>();
            for (Map<String, Object> actionRow : listOfMaps(row.get("actions"))) {
                ActionType type = parseEnum(ActionType.class, str(actionRow.get("type")), null);
                if (type == null) continue;
                actions.add(new NpcDef.Action(type,
                        emptyToNull(str(actionRow.get("stringParam1"))),
                        emptyToNull(str(actionRow.get("stringParam2"))),
                        integer(actionRow.get("intParam1"), 0),
                        integer(actionRow.get("intParam2"), 0)));
            }
            nodes.add(new NpcDef.DialogNode(id, keywords, response,
                    emptyToNull(str(row.get("requiredFlag"))), integer(row.get("requiredFlagValue"), 0),
                    emptyToNull(str(row.get("requiredItem"))), greeting,
                    emptyToNull(str(row.get("fallbackNode"))), actions));
        }
        if (greetings.size() > 1) {
            throw new DialogValidationException("NPC '" + npcName + "': only one dialogue node may be the greeting");
        }
        for (NpcDef.DialogNode node : nodes) {
            if (node.getFallbackNode() != null && !ids.contains(node.getFallbackNode())) {
                throw new DialogValidationException("NPC '" + npcName + "': node '" + node.getId()
                        + "' has a fallbackNode that does not exist: '" + node.getFallbackNode() + "'");
            }
        }
        if (!catalogueUpdates.isEmpty()) {
            I18n.update(catalogueUpdates);
        }
        return nodes;
    }

    /**
     * Resolves what a node's response/keyword text should be saved as: if the
     * edited text differs from what the existing placeholder currently resolves
     * to, the existing key is queued for a catalogue update (or a fresh key is
     * minted when there was no placeholder yet); otherwise the original
     * placeholder is kept untouched.
     */
    private String resolveEditedText(String existingPlaceholder, String editedText, String freshKey,
                                      Map<String, String> catalogueUpdates) {
        String existingKey = I18n.keyOf(existingPlaceholder);
        if (existingKey != null) {
            if (!editedText.equals(I18n.resolve(existingPlaceholder))) {
                catalogueUpdates.put(existingKey, editedText);
            }
            return existingPlaceholder;
        }
        if (editedText == null || editedText.isBlank()) {
            return editedText;
        }
        catalogueUpdates.put(freshKey, editedText);
        return I18n.placeholder(freshKey);
    }

    private List<String> loadAnimatedSpriteBases() {
        Set<String> bases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        bases.add("");
        String[] angleSuffixes = {"000-", "045-", "090-", "135-", "180-", "225-", "270-", "315-"};
        synchronized (sprites) {
            for (SpriteEntry sprite : sprites) {
                if (sprite == null || sprite.name == null || sprite.name.length() < 5) {
                    continue;
                }
                String name = sprite.name;
                char frame = name.charAt(name.length() - 1);
                if (frame < 'a' || frame > 'z') {
                    continue;
                }
                for (String suffix : angleSuffixes) {
                    int idx = name.lastIndexOf(suffix);
                    if (idx > 0 && idx == name.length() - suffix.length() - 1) {
                        bases.add(name.substring(0, idx));
                        break;
                    }
                }
            }
        }
        return new ArrayList<>(bases);
    }

    private List<String> loadSpellNames() {
        List<String> names = new ArrayList<>();
        for (SpellData spell : SpellRegistry.load()) {
            if (spell != null && spell.getName() != null && !spell.getName().isBlank()) {
                String key = I18n.keyOf(spell.getName());
                names.add(key == null ? spell.getName() : key);
            }
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> type, String name, T fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, name.trim());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String emptyToNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Loads sprites from a given binary file into memory.
     *
     * @param file The file to load sprites from.
     * @throws IOException If an I/O error or decoding error occurs.
     */
    private void loadSprites(File file) throws IOException {
        if (file == null) {
            throw new FileNotFoundException("Sprite bin not found: null");
        }
        // The monolith no longer exists once the pack is sharded: shard presence is what counts.
        if (SpriteBinIO.resolveShards(spriteBinDir(file), spriteBinBaseName(file)).isEmpty()) {
            throw new FileNotFoundException("Sprite bin not found: " + file.getPath());
        }
        List<SpriteEntry> loaded = new ArrayList<>();
        // A pack may be split into shards: read the whole set, in the global logical order.
        SpriteBinIO.readAll(spriteBinDir(file), spriteBinBaseName(file),
                packed -> loaded.add(new SpriteEntry(packed.name(), packed.width(), packed.height(),
                        packed.off1X(), packed.off1Y(), packed.off2X(), packed.off2Y(),
                        packed.type(), packed.png())));
        synchronized (sprites) {
            sprites.clear();
            sprites.addAll(loaded);
            sortSprites();
            currentFile = file;
        }
    }

    /**
     * Saves the current list of sprites to the specified binary file.
     *
     * @param file The file to save the sprites to.
     * @throws IOException If an I/O error occurs.
     */
    private void saveSprites(File file) throws IOException {
        if (file == null) {
            throw new FileNotFoundException("No sprite bin file selected");
        }
        List<SpriteBinIO.Packed> packed = new ArrayList<>(sprites.size());
        for (SpriteEntry entry : sprites) {
            packed.add(new SpriteBinIO.Packed(entry.name, entry.width, entry.height,
                    entry.off1X, entry.off1Y, entry.off2X, entry.off2Y, entry.type, entry.pngData));
        }
        SpriteBinIO.writeSharded(spriteBinDir(file), spriteBinBaseName(file), packed);
    }

    /**
     * Base name of a shard, or {@code null} when the file is not one.
     *
     * <p>{@code sprites_0.bin} → {@code sprites}; {@code sprites2.bin} and
     * {@code sprites_backup.bin} are not shards.
     */
    private static String shardBaseName(String fileName) {
        if (!fileName.endsWith(".bin")) {
            return null;
        }
        String stem = fileName.substring(0, fileName.length() - ".bin".length());
        int underscore = stem.lastIndexOf('_');
        if (underscore <= 0 || underscore == stem.length() - 1) {
            return null;
        }
        String digits = stem.substring(underscore + 1);
        for (int i = 0; i < digits.length(); i++) {
            if (!Character.isDigit(digits.charAt(i))) {
                return null;
            }
        }
        return stem.substring(0, underscore);
    }

    /** Directory holding the sprite pack designated by {@code file}. */
    private static java.nio.file.Path spriteBinDir(File file) {
        File parent = file.getParentFile();
        return (parent != null ? parent : new File(".")).toPath();
    }

    /** {@code sprites.bin} → {@code sprites}, the base name of the shards. */
    private static String spriteBinBaseName(File file) {
        String name = file.getName();
        return name.endsWith(".bin") ? name.substring(0, name.length() - ".bin".length()) : name;
    }

    /**
     * Creates a new SpriteEntry from a provided data URL.
     *
     * @param name    The name of the new sprite.
     * @param dataUrl The base64 encoded data URL of the image.
     * @return The created SpriteEntry, or null if decoding failed.
     */
    private SpriteEntry createEntryFromDataUrl(String name, String dataUrl) {
        ImageData imageData = decodeImageData(dataUrl, null, null);
        if (imageData == null) {
            return null;
        }
        return new SpriteEntry(stripExtension(name), imageData.width, imageData.height, 0, 0, 0, 0, imageData.type, imageData.pngData);
    }

    /**
     * Decodes a base64 data URL into an ImageData object, optionally resizing it.
     *
     * @param dataUrl      The base64 encoded data URL.
     * @param targetWidth  The target width, or null to keep original.
     * @param targetHeight The target height, or null to keep original.
     * @return The decoded ImageData, or null if an error occurred.
     */
    private ImageData decodeImageData(String dataUrl, Integer targetWidth, Integer targetHeight) {
        try {
            String base64 = dataUrl;
            int commaIndex = dataUrl.indexOf(',');
            if (dataUrl.startsWith("data:") && commaIndex >= 0) {
                base64 = dataUrl.substring(commaIndex + 1);
            }
            byte[] raw = Base64.getDecoder().decode(base64);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(raw));
            if (img == null) {
                return null;
            }
            int width = img.getWidth();
            int height = img.getHeight();
            if (targetWidth != null && targetHeight != null && (width != targetWidth || height != targetHeight)) {
                img = resizeImage(img, targetWidth, targetHeight);
                width = targetWidth;
                height = targetHeight;
            }
            int type = (width == 32 && height == 16) ? 0 : 1;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "png", out);
            return new ImageData(width, height, type, out.toByteArray());
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Resizes a given BufferedImage to the specified dimensions using bilinear interpolation.
     *
     * @param source The original image.
     * @param width  The target width.
     * @param height The target height.
     * @return The resized image.
     */
    private BufferedImage resizeImage(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(source, 0, 0, width, height, null);
        g2d.dispose();
        return resized;
    }

    /**
     * Adds a new SpriteEntry to the list or replaces an existing one with the same name.
     *
     * @param entry The SpriteEntry to add or replace.
     */
    private void addOrReplaceEntry(SpriteEntry entry) {
        for (int i = 0; i < sprites.size(); i++) {
            if (sprites.get(i).name.equalsIgnoreCase(entry.name)) {
                // Preserve existing offsets when replacing image data via upload.
                // Offsets are edited separately and should not be reset to 0.
                SpriteEntry existing = sprites.get(i);
                SpriteEntry merged = new SpriteEntry(
                        existing.name,
                        entry.width,
                        entry.height,
                        existing.off1X,
                        existing.off1Y,
                        existing.off2X,
                        existing.off2Y,
                        entry.type,
                        entry.pngData
                );
                sprites.set(i, merged);
                return;
            }
        }
        sprites.add(entry);
    }

    /**
     * Sorts the loaded sprites alphabetically by their names.
     */
    private void sortSprites() {
        sprites.sort(Comparator.comparing(e -> e.name.toLowerCase(Locale.ROOT)));
    }

    /**
     * Finds a loaded sprite by its exact name, ignoring case.
     *
     * @param name The name of the sprite to search for.
     * @return The found SpriteEntry, or null if not found.
     */
    private SpriteEntry findByName(String name) {
        synchronized (sprites) {
            for (SpriteEntry entry : sprites) {
                if (entry.name.equalsIgnoreCase(name)) {
                    return entry;
                }
            }
        }
        return null;
    }

    /**
     * Removes the file extension from a given file name.
     *
     * @param name The original file name.
     * @return The file name without its extension.
     */
    private String stripExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(0, idx) : name;
    }

    /**
     * Serves an embedded resource file via the HTTP exchange.
     *
     * @param exchange    The HTTP exchange.
     * @param resource    The internal path to the resource file.
     * @param contentType The Content-Type header to set.
     * @throws IOException If an I/O error occurs while reading the resource or writing the response.
     */
    private void serveResource(HttpExchange exchange, String resource, String contentType) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                handleNotFound(exchange);
                return;
            }
            byte[] data = in.readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(data);
            }
        }
    }

    /**
     * Serializes an object to JSON and writes it to the HTTP exchange response.
     *
     * @param exchange The HTTP exchange.
     * @param payload  The object to serialize.
     * @throws IOException If an I/O error occurs.
     */
    private void writeJson(HttpExchange exchange, Object payload) throws IOException {
        byte[] data = gson.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    /**
     * Reads the entire request body from an HTTP exchange into a String.
     *
     * @param exchange The HTTP exchange.
     * @return The string representation of the request body.
     * @throws IOException If an I/O error occurs.
     */
    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Parses a query string to extract a list of values for a given key.
     *
     * @param rawQuery The raw query string.
     * @param key      The key to search for.
     * @return A list of extracted values.
     */
    private List<String> parseQueryList(String rawQuery, String key) {
        List<String> values = new ArrayList<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return values;
        }
        String[] parts = rawQuery.split("&");
        for (String part : parts) {
            int idx = part.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String k = part.substring(0, idx);
            if (!k.equals(key)) {
                continue;
            }
            String v = part.substring(idx + 1);
            String decoded = URLDecoder.decode(v, StandardCharsets.UTF_8);
            if (!decoded.isEmpty()) {
                values.addAll(Arrays.asList(decoded.split(",")));
            }
        }
        return values;
    }

    private Map<String, String> parseQueryMap(String rawQuery) {
        Map<String, String> values = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return values;
        }
        for (String part : rawQuery.split("&")) {
            int idx = part.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = URLDecoder.decode(part.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(part.substring(idx + 1), StandardCharsets.UTF_8);
            values.put(key, value);
        }
        return values;
    }

    private static boolean isNpcKind(String kind) {
        return "npc".equalsIgnoreCase(kind) || "npcs".equalsIgnoreCase(kind);
    }

    /**
     * The shared multi-map spawn file the game reads, as in {@code NPCManager.readSpawnsForMap}.
     * Entries for every layer live in one file, so callers must scope reads and writes by z.
     */
    private File getSpawnFile(String kind) {
        return new File(isNpcKind(kind) ? Paths.NPC_SPAWNS_BIN : Paths.MONSTER_SPAWNS_BIN);
    }

    /** Layer index of {@code mapPath}, matching {@code NPCManager.resolveMapZ}. */
    private int resolveMapZ(String mapPath) {
        String normalized = new File(mapPath == null || mapPath.isBlank() ? Paths.MAP : mapPath)
                .getPath().replace('\\', '/');
        for (MapDefinition map : MapDefinition.values()) {
            if (new File(map.getMapPath()).getPath().replace('\\', '/').equals(normalized)) {
                return map.getZ();
            }
        }
        return 0;
    }

    private List<SpawnBinaryIO.Entry> readSpawns(File file) {
        if (file == null || !file.exists()) {
            return List.of();
        }
        try {
            return SpawnBinaryIO.read(file);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<ObjectMappingsBinaryIO.Entry> readObjectMappings(File file) {
        if (file == null || !file.exists()) {
            return List.of();
        }
        try {
            return ObjectMappingsBinaryIO.read(file);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<ObjectPos> readObjectPositions(File file) {
        if (file == null || !file.exists()) {
            return List.of();
        }
        try {
            return ObjectPositionBinaryIO.read(file);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<TeleportBinaryIO.Entry> readTeleports(File file) {
        if (file == null || !file.exists()) {
            return List.of();
        }
        try {
            return TeleportBinaryIO.read(file);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<ClanRelationsBinaryIO.Entry> readClanRelations(File file) {
        if (file == null || !file.exists()) {
            return List.of();
        }
        try {
            return ClanRelationsBinaryIO.read(file);
        } catch (Exception e) {
            return List.of();
        }
    }

    private Set<String> readDecorLayerRules(File file) {
        if (file == null || !file.exists()) {
            return new LinkedHashSet<>();
        }
        try {
            return DecorLayerRuleBinaryIO.read(file);
        } catch (Exception e) {
            return new LinkedHashSet<>();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                items.add((Map<String, Object>) map);
            }
        }
        return items;
    }

    private List<String> stringList(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return new ArrayList<>();
        }
        List<String> values = new ArrayList<>();
        for (Object item : list) {
            String value = str(item).trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String normalizedI18n(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replaceFirst("^\\s*\\[[^]]+]\\s*", "").replaceFirst("^a\\s+", "")
                .replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    }

    private int integer(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(str(value).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private long longVal(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(str(value).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private Integer nullableInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            String text = str(value).trim();
            return text.isEmpty() ? null : Integer.parseInt(text);
        } catch (Exception e) {
            return null;
        }
    }

    private long lng(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(str(value).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private double dbl(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(str(value).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private float flt(Object value, float fallback) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        try {
            return Float.parseFloat(str(value).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean b) {
            return b;
        }
        String text = str(value).trim();
        return text.isEmpty() ? fallback : Boolean.parseBoolean(text);
    }

    private Boolean nullableBool(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        String text = str(value).trim();
        return text.isEmpty() ? null : Boolean.parseBoolean(text);
    }

    private String normalizePath(String path) {
        return path == null ? "" : path.replace('\\', '/');
    }

    private File resolveSpriteBinFile(String path) {
        String trimmed = path == null ? "" : path.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        File file = new File(trimmed);
        if (file.isAbsolute()) {
            return file;
        }
        File direct = new File(trimmed);
        if (direct.exists()) {
            return direct;
        }
        File spriteDir = new File(Paths.SPRITE_BIN).getParentFile();
        if (spriteDir == null) {
            spriteDir = new File(".");
        }
        return new File(spriteDir, trimmed);
    }

    private boolean sameFile(File left, File right) {
        try {
            return left.getCanonicalFile().equals(right.getCanonicalFile());
        } catch (IOException e) {
            return left.getAbsoluteFile().equals(right.getAbsoluteFile());
        }
    }

    /**
     * Responds to the client with a 404 Not Found error.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void handleNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }

    /**
     * Responds to the client with a 405 Method Not Allowed error.
     *
     * @param exchange The HTTP exchange.
     * @throws IOException If an I/O error occurs.
     */
    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }

    /**
     * Responds to the client with a 400 Bad Request error and a JSON payload containing the error message.
     *
     * @param exchange The HTTP exchange.
     * @param message  The error message to send.
     * @throws IOException If an I/O error occurs.
     */
    private void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", message);
        byte[] data = gson.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(400, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    /**
     * Request payload for deleting sprites.
     */
    private static class DeleteRequest {
        private List<String> names;
    }

    /**
     * Request payload for uploading sprites.
     */
    private static class UploadRequest {
        private List<UploadItem> items;
    }

    /**
     * Represents a single sprite item within an upload request.
     */
    private static class UploadItem {
        private String name;
        private String dataUrl;
    }

    /**
     * Request payload for updating the offsets of a sprite.
     */
    private static class OffsetsUpdateRequest {
        private String name;
        private int off1X;
        private int off1Y;
        private int off2X;
        private int off2Y;
    }

    /**
     * Request payload for replacing the image data of a sprite.
     */
    private static class ReplaceRequest {
        private String name;
        private String dataUrl;
    }

    /**
     * Request payload for renaming a sprite.
     */
    private static class RenameRequest {
        private String oldName;
        private String newName;
    }

    /**
     * Request payload for exporting sprites.
     */
    private static class ExportRequest {
        private List<String> names;
    }

    private static class SpriteBinSwitchRequest {
        private String path;
    }

    /**
     * Request payload for creating or updating an NPC definition.
     */
    private static class NpcSaveRequest {
        private String oldName;
        private String name;
        private String displayName;
        private String spriteBase;
        private int patrolRadiusTiles;
        private List<NpcPartRequest> parts;
    }

    /**
     * Request payload for deleting an NPC definition.
     */
    private static class NpcDeleteRequest {
        private String name;
    }

    /**
     * Represents a body part row in an NPC save request.
     */
    private static class NpcPartRequest {
        private String bodyPart;
        private String spriteBase;
    }

    /**
     * Internal representation of a loaded sprite containing its metadata and image data.
     */
    private static class SpriteEntry {
        private final String name;
        private final int width;
        private final int height;
        private final int off1X;
        private final int off1Y;
        private final int off2X;
        private final int off2Y;
        private final int type;
        private final byte[] pngData;

        /**
         * Constructs a new SpriteEntry.
         *
         * @param name    The name of the sprite.
         * @param width   The width of the sprite in pixels.
         * @param height  The height of the sprite in pixels.
         * @param off1X   The X offset 1.
         * @param off1Y   The Y offset 1.
         * @param off2X   The X offset 2.
         * @param off2Y   The Y offset 2.
         * @param type    The type of the sprite.
         * @param pngData The raw PNG data representing the image.
         */
        private SpriteEntry(String name, int width, int height, int off1X, int off1Y, int off2X, int off2Y, int type, byte[] pngData) {
            this.name = Objects.requireNonNull(name);
            this.width = width;
            this.height = height;
            this.off1X = off1X;
            this.off1Y = off1Y;
            this.off2X = off2X;
            this.off2Y = off2Y;
            this.type = type;
            this.pngData = pngData;
        }
    }

    /**
     * Internal class representing raw image data and properties obtained after decoding.
     */
    private static class ImageData {
        private final int width;
        private final int height;
        private final int type;
        private final byte[] pngData;

        /**
         * Constructs a new ImageData object.
         *
         * @param width   The width of the decoded image.
         * @param height  The height of the decoded image.
         * @param type    The determined type of the image.
         * @param pngData The decoded PNG byte data.
         */
        private ImageData(int width, int height, int type, byte[] pngData) {
            this.width = width;
            this.height = height;
            this.type = type;
            this.pngData = pngData;
        }
    }
}
