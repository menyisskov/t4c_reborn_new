package com.perso.T4C.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class BinaryIOUtils {
    private static final byte[] COMPRESSED_MAGIC = "T4CBIN".getBytes(StandardCharsets.US_ASCII);
    private static final int COMPRESSED_VERSION = 1;
    private static final int METHOD_DEFLATE = 1;

    private BinaryIOUtils() {
    }

    public static InputStream openInputStream(File file, int bufferSize) throws IOException {
        return openInputStream(new BufferedInputStream(new FileInputStream(file), bufferSize));
    }

    public static InputStream openInputStream(InputStream raw) throws IOException {
        PushbackInputStream in = new PushbackInputStream(raw, COMPRESSED_MAGIC.length);
        byte[] magic = in.readNBytes(COMPRESSED_MAGIC.length);
        if (magic.length != COMPRESSED_MAGIC.length) {
            in.unread(magic);
            return in;
        }
        for (int i = 0; i < COMPRESSED_MAGIC.length; i++) {
            if (magic[i] != COMPRESSED_MAGIC[i]) {
                in.unread(magic);
                return in;
            }
        }
        int version = in.read();
        int method = in.read();
        if (version != COMPRESSED_VERSION || method != METHOD_DEFLATE) {
            throw new IOException("Unsupported compressed binary container: version=" + version + ", method=" + method);
        }
        return new InflaterInputStream(in, new java.util.zip.Inflater(), 1 << 16);
    }

    public static OutputStream openOutputStream(File file, int bufferSize) throws IOException {
        OutputStream raw = new BufferedOutputStream(new FileOutputStream(file), bufferSize);
        raw.write(COMPRESSED_MAGIC);
        raw.write(COMPRESSED_VERSION);
        raw.write(METHOD_DEFLATE);
        return new DeflaterOutputStream(raw, new Deflater(Deflater.BEST_COMPRESSION), 1 << 16, true);
    }

    public static void writeCompressedFile(File source, File target) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(source), 1 << 20);
                OutputStream out = openOutputStream(target, 1 << 20)) {
            in.transferTo(out);
        }
    }

    public static short readShortLE(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        return (short) ((b2 << 8) | b1);
    }

    public static int readIntLE(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    public static void writeShortLE(DataOutputStream out, short value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >>> 8) & 0xFF);
    }

    public static void writeIntLE(DataOutputStream out, int value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >>> 8) & 0xFF);
        out.writeByte((value >>> 16) & 0xFF);
        out.writeByte((value >>> 24) & 0xFF);
    }

    public static long readLongLE(DataInputStream in) throws IOException {
        long low = readIntLE(in) & 0xFFFFFFFFL;
        long high = readIntLE(in) & 0xFFFFFFFFL;
        return (high << 32) | low;
    }

    public static void writeLongLE(DataOutputStream out, long value) throws IOException {
        writeIntLE(out, (int) (value & 0xFFFFFFFFL));
        writeIntLE(out, (int) ((value >>> 32) & 0xFFFFFFFFL));
    }

    public static String readString(DataInputStream in, int maxBytes) throws IOException {
        int length = readIntLE(in);
        if (length < 0 || length > maxBytes) {
            throw new IOException("Invalid string length: " + length);
        }
        byte[] bytes = in.readNBytes(length);
        if (bytes.length != length) {
            throw new EOFException("Unexpected EOF while reading string");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = (value != null ? value : "").getBytes(StandardCharsets.UTF_8);
        writeIntLE(out, bytes.length);
        out.write(bytes);
    }
}
