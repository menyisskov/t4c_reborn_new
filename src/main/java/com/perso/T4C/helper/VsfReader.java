package com.perso.T4C.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lecteur de la librairie graphique .VSF (format "T4CGameFile") de The 4th Coming.
 *
 * <p>Contrairement au format {@link DdaReader} .dda/.did/.dpd, le .vsf regroupe dans un seul
 * fichier le catalogue et les pixels. Ce lecteur travaille sur la forme <b>déchiffrée</b> du
 * fichier (extension .dsf produite par les outils VSF, ou copie déchiffrée d'un .vsf).
 *
 * <p>Structure :
 * <ul>
 *   <li>En-tête de 29 octets : signature {@code 0x1A} suivie d'une version, puis la fin de la table d'arbre
 *       (offset 0x0C) et le nombre de noeuds (offset 0x10). Le catalogue débute à 0x1D.</li>
 *   <li>Table 1 (arbre des catégories) : {@code u16 nameLen, char[nameLen] name, u8 flag,
 *       u32 selfId, u32 parentId}. Les identifiants commencent à 0x2710 (10000).
 *       Le dernier noeud a une queue tronquée dont le dernier {@code u32} marque la fin
 *       de la table 2.</li>
 *   <li>Table 2 (index des ressources) : {@code u16 nameLen, char[nameLen] name, u8 type,
 *       u32 offset}, où {@code type} vaut 1 pour un sprite et 2 pour une palette, et
 *       {@code offset} est un offset absolu dans le fichier.</li>
 *   <li>Record sprite : en-tête de 32 octets <b>non chiffré</b> (là où le .dda applique un XOR),
 *       suivi de {@code dataSize} octets encodés avec le même RLE que le .dda mais
 *       <b>sans couche zlib</b>, quelle que soit la taille du sprite.</li>
 * </ul>
 *
 * <p>Le champ {@code kind} de l'en-tête distingue les deux natures de pixels :
 * {@code 2} = index de palette (image couleur), {@code 4} = poids d'alpha 8 bits
 * (masque de transparence, cf. {@code TransAlphaMask2} du client d'origine).
 */
public class VsfReader {

    /**
     * Signature d'un fichier déchiffré, portée par le premier octet. Les trois octets suivants
     * sont un numéro de version qui varie d'une génération de librairie à l'autre
     * ({@code 0x00159A1A} pour les fichiers de 2005, {@code 0x0112001A} pour ceux de 1998-2000)
     * sans que la structure du catalogue ni celle des records ne change.
     */
    private static final int MAGIC_SIGNATURE = 0x1A;

    /** Offset de début de la table d'arbre des catégories. */
    private static final int TREE_START = 0x1D;

    /** Taille de l'en-tête d'un record sprite, en octets. */
    public static final int SPRITE_HEADER_SIZE = 32;

    /** {@code kind} d'un sprite dont les octets sont des index de palette. */
    public static final int KIND_INDEXED = 2;

    /** {@code kind} d'un sprite dont les octets sont des poids d'alpha 0-255. */
    public static final int KIND_ALPHA_MASK = 4;

    /** Type d'entrée de la table des ressources désignant un sprite. */
    private static final int RESOURCE_TYPE_SPRITE = 1;

    /**
     * Écart entre la fin de table d'arbre annoncée par l'en-tête et le début réel de la table
     * des ressources : la queue du dernier noeud est tronquée et déborde de 9 octets.
     */
    private static final int RESOURCE_TABLE_GAP = 9;

    /** Position, après le nom du dernier noeud, du {@code u32} portant la fin de la table 2. */
    private static final int LAST_NODE_END_FIELD = 5;

    /** Une catégorie de l'arbre : {@code Sprites > BoulderFire > Alpha}. */
    public record TreeNode(String name, int selfId, int parentId) {
    }

    /** Une entrée de l'index des ressources. */
    public record Resource(String name, int type, int offset) {
        public boolean isSprite() {
            return type == RESOURCE_TYPE_SPRITE;
        }
    }

    /** En-tête de 32 octets d'un record sprite. */
    public record SpriteHeader(int treeId, int ombre, int typeSprite, int width, int height,
                               int originX, int originY, int kind, int dataSize) {
        public boolean isAlphaMask() {
            return kind == KIND_ALPHA_MASK;
        }
    }

    /** Un sprite décodé : en-tête + un octet par pixel (index de palette ou poids d'alpha). */
    public record Sprite(SpriteHeader header, byte[] pixels) {
    }

    private final byte[] data;
    private final List<TreeNode> tree = new ArrayList<>();
    private final List<Resource> resources = new ArrayList<>();

    /** Charge un fichier .vsf déjà déchiffré (typiquement un .dsf). */
    public VsfReader(Path decryptedFile) throws IOException {
        this(Files.readAllBytes(decryptedFile));
    }

    public VsfReader(byte[] decryptedContent) throws IOException {
        this.data = decryptedContent;
        int magic = readLE32(0);
        if ((magic & 0xFF) != MAGIC_SIGNATURE) {
            throw new IOException(String.format("Not a decrypted VSF file (magic=0x%X)", magic));
        }
        int treeEnd = readLE32(0x0C);
        int treeCount = readLE32(0x10);
        int resourceEnd = readTree(treeCount);
        // La table des ressources suit immédiatement la table d'arbre. Le champ de fin porté
        // par le dernier noeud est le seul délimiteur : sans lui, on lirait les octets de
        // pixels comme des entrées d'index.
        readResources(treeEnd + RESOURCE_TABLE_GAP, resourceEnd);
    }

    public List<TreeNode> getTree() {
        return tree;
    }

    public List<Resource> getResources() {
        return resources;
    }

    /**
     * Lit la table d'arbre et renvoie la fin de la table des ressources, portée par le
     * dernier {@code u32} du noeud final (dont la queue est tronquée).
     */
    private int readTree(int treeCount) throws IOException {
        int p = TREE_START;
        int resourceEnd = data.length;
        for (int i = 0; i < treeCount; i++) {
            int nameLen = readLE16(p);
            p += 2;
            String name = readString(p, nameLen);
            p += nameLen;
            if (i == treeCount - 1) {
                // Le dernier noeud ne suit pas la structure des autres : sa queue porte la
                // fin de la table des ressources, seul délimiteur disponible.
                resourceEnd = readLE32(p + LAST_NODE_END_FIELD);
                tree.add(new TreeNode(name, 0, 0));
                break;
            }
            p++; // flag
            int selfId = readLE32(p);
            p += 4;
            int parentId = readLE32(p);
            p += 4;
            tree.add(new TreeNode(name, selfId, parentId));
        }
        return resourceEnd;
    }

    private void readResources(int start, int end) throws IOException {
        int p = start;
        int limit = Math.min(end, data.length);
        while (p + 7 <= limit) {
            int nameLen = readLE16(p);
            if (nameLen <= 0 || p + 2 + nameLen + 5 > limit) {
                break;
            }
            p += 2;
            String name = readString(p, nameLen);
            p += nameLen;
            int type = data[p] & 0xFF;
            p++;
            int offset = readLE32(p);
            p += 4;
            resources.add(new Resource(name, type, offset));
        }
    }

    /** Lit l'en-tête de 32 octets d'un record sprite (non chiffré). */
    public SpriteHeader readSpriteHeader(int offset) throws IOException {
        if (offset < 0 || offset + SPRITE_HEADER_SIZE > data.length) {
            throw new IOException("Sprite header out of bounds: " + offset);
        }
        int treeId = readLE32(offset);
        int ombre = data[offset + 4] & 0xFF;
        int typeSprite = data[offset + 5] & 0xFF;
        int width = readLE16(offset + 6);
        int height = readLE16(offset + 8);
        int originX = readLE16Signed(offset + 10);
        int originY = readLE16Signed(offset + 12);
        int kind = readLE32(offset + 20);
        int dataSize = readLE32(offset + 28);
        return new SpriteHeader(treeId, ombre, typeSprite, width, height, originX, originY,
                kind, dataSize);
    }

    /**
     * Décode le sprite situé à l'offset donné. Le RLE est celui du .dda, sans couche zlib :
     * un sprite de 464x301 est stocké en RLE brut là où le .dda aurait ajouté un zlib.
     */
    public Sprite readSprite(int offset) throws IOException {
        SpriteHeader header = readSpriteHeader(offset);
        int payloadStart = offset + SPRITE_HEADER_SIZE;
        int size = header.dataSize();
        if (size < 0 || payloadStart + size > data.length) {
            throw new IOException("Sprite payload out of bounds at " + offset);
        }
        byte[] payload = new byte[size];
        System.arraycopy(data, payloadStart, payload, 0, size);
        byte[] pixels = decompressRLE(payload, header.width(), header.height());
        return new Sprite(header, pixels);
    }

    /**
     * RLE T4C : {@code u16 x}, puis {@code hi*4+lo} pixels, un octet de contrôle, les données,
     * et un code de fin de run (0 = fin du sprite, 2 = ligne suivante). Le fond vaut 0, ce qui
     * correspond à « totalement transparent » pour un masque comme pour un sprite indexé de
     * cette librairie.
     */
    private static byte[] decompressRLE(byte[] src, int width, int height) {
        byte[] out = new byte[Math.max(0, width * height)];
        int p = 0;
        int y = 0;
        int len = src.length;
        while (p + 4 <= len) {
            int x = (src[p] & 0xFF) | ((src[p + 1] & 0xFF) << 8);
            p += 2;
            int nbPix = (src[p] & 0xFF) * 4 + (src[p + 1] & 0xFF);
            p += 2;
            if (p >= len) break;
            int control = src[p] & 0xFF;
            if (control != 1) {
                for (int i = 0; i < nbPix; i++) {
                    p++;
                    if (p >= len) break;
                    int dest = i + x + (y * width);
                    if (dest >= 0 && dest < out.length) {
                        out[dest] = src[p];
                    }
                    if ((i + x) == width - 1) break;
                }
            }
            p++;
            if (p >= len) break;
            int code = src[p] & 0xFF;
            if (code == 0) {
                break;
            } else if (code == 2) {
                y++;
            }
            p++;
            if (y >= height) break;
        }
        return out;
    }

    private String readString(int offset, int length) throws IOException {
        if (offset < 0 || offset + length > data.length) {
            throw new IOException("String out of bounds at " + offset);
        }
        return new String(data, offset, length, StandardCharsets.US_ASCII);
    }

    private int readLE16(int offset) throws IOException {
        if (offset < 0 || offset + 2 > data.length) {
            throw new IOException("16-bit value out of bounds at " + offset);
        }
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    private int readLE16Signed(int offset) throws IOException {
        return (short) readLE16(offset);
    }

    private int readLE32(int offset) throws IOException {
        if (offset < 0 || offset + 4 > data.length) {
            throw new IOException("32-bit value out of bounds at " + offset);
        }
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }
}
