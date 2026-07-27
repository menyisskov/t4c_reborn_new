package com.perso.T4C.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Décrypteur du fichier index .DID (v2datai.did) de The 4th Coming.
 *
 * <p>Format :
 * <ul>
 *   <li>En-tête de 41 octets (voir {@link DpdReader.Hdr}).</li>
 *   <li>Flux zlib décompressé puis déchiffré par XOR octet par octet avec {@code 0x99}.</li>
 *   <li>Suite d'entrées de {@code 64 + 256 + 4 + 8 = 332} octets chacune :
 *       <ul>
 *         <li>nom du sprite : 64 octets (chaîne terminée par 0)</li>
 *         <li>chemin / atlas : 256 octets (chaîne terminée par 0)</li>
 *         <li>indexation : uint32 little-endian (offset dans le .dda = {@code 4 + indexation})</li>
 *         <li>numDda : int64 little-endian (numéro du fichier v2dataNN.dda)</li>
 *       </ul>
 *   </li>
 * </ul>
 */
public class DidReader {

    /** Clé XOR appliquée aux données décompressées du .DID. */
    private static final byte CLEF = (byte) 0x99;

    private static final int NAME_LEN = 64;
    private static final int PATH_LEN = 256;
    private static final int ENTRY_SIZE = NAME_LEN + PATH_LEN + 4 + 8; // 332

    /** Une entrée d'index : où trouver un sprite donné. */
    public static final class Entry {
        public final String name;
        public final String atlas;
        /** Offset du sprite dans le .dda = {@code 4 + indexation}. */
        public final long indexation;
        /** Numéro du fichier v2dataNN.dda contenant le sprite. */
        public final long numDda;

        Entry(String name, String atlas, long indexation, long numDda) {
            this.name = name;
            this.atlas = atlas;
            this.indexation = indexation;
            this.numDda = numDda;
        }

        /** Offset absolu de l'en-tête du sprite dans son fichier .dda. */
        public int spriteOffset() { return (int) (indexation + 4); }

        @Override
        public String toString() {
            return name + " (dda=" + numDda + ", off=" + spriteOffset() + ")";
        }
    }

    private final List<Entry> entries;

    public DidReader(Path didFile) throws IOException {
        byte[] data = DpdReader.Hdr.decompressXored(Files.readAllBytes(didFile), CLEF);

        int count = data.length / ENTRY_SIZE;
        entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int o = i * ENTRY_SIZE;
            String name = cString(data, o, NAME_LEN);
            String atlas = cString(data, o + NAME_LEN, PATH_LEN);
            int p = o + NAME_LEN + PATH_LEN;
            long indexation = readU32(data, p);
            long numDda = readI64(data, p + 4);
            entries.add(new Entry(name, atlas, indexation, numDda));
        }
    }

    public List<Entry> getEntries() { return entries; }

    public int size() { return entries.size(); }

    private static String cString(byte[] b, int off, int max) {
        int end = off;
        int limit = off + max;
        while (end < limit && b[end] != 0) end++;
        return new String(b, off, end - off, StandardCharsets.ISO_8859_1).trim();
    }

    private static long readU32(byte[] b, int o) {
        return (b[o] & 0xFFL)
                | ((b[o + 1] & 0xFFL) << 8)
                | ((b[o + 2] & 0xFFL) << 16)
                | ((b[o + 3] & 0xFFL) << 24);
    }

    private static long readI64(byte[] b, int o) {
        long v = 0;
        for (int i = 0; i < 8; i++) v |= (b[o + i] & 0xFFL) << (i * 8);
        return v;
    }
}
