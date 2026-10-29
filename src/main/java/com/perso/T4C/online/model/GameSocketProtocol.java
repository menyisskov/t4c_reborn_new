package com.perso.T4C.online.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/** Bounded, length-prefixed UTF-8 framing for the native online game socket. */
public final class GameSocketProtocol {
  public static final int VERSION = 2;
  public static final int MAX_FRAME_BYTES = 1_048_576;

  private GameSocketProtocol() {}

  public record Hello(int protocolVersion) {}
  public record HelloReply(int protocolVersion, boolean accepted) {}
  public record Request(long id, String method, String command, String query, String authorization, String body) {
    public Request(long id, String method, String command, String body) {
      this(id, method, command, "", null, body);
    }

    public Request {
      if (id <= 0 || !("GET".equals(method) || "POST".equals(method)) || command == null
          || !(command.matches("[a-z][a-z0-9-]{0,31}") || command.equals("account/characters"))
          || query == null || query.length() > 64
          || !(query.isEmpty() || command.equals("account/characters")
              && query.matches("after=[0-9a-fA-F-]{36}"))
          || authorization != null && !authorization.matches("Bearer [0-9a-f]{64}") || body == null)
        throw new IllegalArgumentException("Invalid game socket request");
    }
  }
  public record Reply(long id, int status, String body) {
    public Reply {
      if (id <= 0 || status < 100 || status > 599 || body == null)
        throw new IllegalArgumentException("Invalid game socket reply");
    }
  }

  /** Returns null only when the stream ends cleanly before a new frame begins. */
  public static String readFrame(InputStream source) throws IOException {
    var input = source instanceof DataInputStream data ? data : new DataInputStream(source);
    int first = input.read();
    if (first < 0) return null;
    int length;
    try {
      length = (first << 24) | (input.readUnsignedByte() << 16)
          | (input.readUnsignedByte() << 8) | input.readUnsignedByte();
    } catch (EOFException truncatedHeader) {
      throw new EOFException("Truncated game socket frame header");
    }
    if (length <= 0 || length > MAX_FRAME_BYTES) throw new IOException("Invalid game socket frame length");
    byte[] bytes = new byte[length];
    try { input.readFully(bytes); }
    catch (EOFException truncatedBody) { throw new EOFException("Truncated game socket frame body"); }
    try {
      return StandardCharsets.UTF_8.newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(bytes)).toString();
    } catch (CharacterCodingException invalidUtf8) {
      throw new IOException("Invalid UTF-8 game socket frame", invalidUtf8);
    }
  }

  public static void writeFrame(OutputStream destination, String frame) throws IOException {
    if (frame == null) throw new IllegalArgumentException("Missing game socket frame");
    final ByteBuffer encoded;
    try {
      encoded = StandardCharsets.UTF_8.newEncoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .encode(java.nio.CharBuffer.wrap(frame));
    } catch (CharacterCodingException invalidUnicode) {
      throw new IOException("Invalid Unicode game socket frame", invalidUnicode);
    }
    int length = encoded.remaining();
    if (length <= 0 || length > MAX_FRAME_BYTES) throw new IOException("Invalid game socket frame length");
    var output = destination instanceof DataOutputStream data ? data : new DataOutputStream(destination);
    output.writeInt(length);
    if (encoded.hasArray()) output.write(encoded.array(), encoded.arrayOffset() + encoded.position(), length);
    else {
      byte[] bytes = new byte[length];
      encoded.get(bytes);
      output.write(bytes);
    }
    output.flush();
  }
}
