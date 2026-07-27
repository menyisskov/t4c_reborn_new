package com.perso.T4C.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Shared reader/writer for the editable one-byte-per-tile .colbin format.
 */
public final class CollisionMapIO {
    private CollisionMapIO() {
    }

    public static CollisionMap read(File file) throws IOException {
        try (InputStream raw = BinaryIOUtils.openInputStream(file, 1 << 20);
             DataInputStream in = new DataInputStream(raw)) {
            int width = BinaryIOUtils.readIntLE(in);
            int height = BinaryIOUtils.readIntLE(in);
            validateDimensions(width, height);
            int size = Math.multiplyExact(width, height);
            byte[] data = in.readNBytes(size);
            if (data.length != size) {
                throw new IOException("Invalid collision file: expected " + size + " bytes, got " + data.length);
            }
            return new CollisionMap(width, height, data);
        } catch (ArithmeticException e) {
            throw new IOException("Invalid collision map dimensions", e);
        }
    }

    public static void write(File file, int width, int height, byte[] data) throws IOException {
        validateDimensions(width, height);
        int expected;
        try {
            expected = Math.multiplyExact(width, height);
        } catch (ArithmeticException e) {
            throw new IOException("Invalid collision map dimensions", e);
        }
        if (data == null || data.length != expected) {
            throw new IOException("Collision data length does not match " + width + "x" + height);
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create directory " + parent);
        }
        try (OutputStream out = BinaryIOUtils.openOutputStream(file, 1 << 20)) {
            byte[] header = new byte[8];
            putIntLE(header, 0, width);
            putIntLE(header, 4, height);
            out.write(header);
            out.write(data);
        }
    }

    private static void validateDimensions(int width, int height) throws IOException {
        if (width <= 0 || height <= 0 || width > 32768 || height > 32768) {
            throw new IOException("Invalid collision dimensions: " + width + "x" + height);
        }
    }

    private static void putIntLE(byte[] target, int offset, int value) {
        target[offset] = (byte) value;
        target[offset + 1] = (byte) (value >>> 8);
        target[offset + 2] = (byte) (value >>> 16);
        target[offset + 3] = (byte) (value >>> 24);
    }

    @Getter
    @RequiredArgsConstructor
    public static final class CollisionMap {
        private final int width;
        private final int height;
        private final byte[] data;
    }
}
