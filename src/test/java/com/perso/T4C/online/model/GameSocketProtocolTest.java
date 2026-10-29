package com.perso.T4C.online.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class GameSocketProtocolTest {
  @Test
  void framesRoundTripUtf8AndCleanEof() throws Exception {
    var bytes = new ByteArrayOutputStream();
    GameSocketProtocol.writeFrame(bytes, "{\"text\":\"héllo 世界\"}");
    var input = new ByteArrayInputStream(bytes.toByteArray());
    assertEquals("{\"text\":\"héllo 世界\"}", GameSocketProtocol.readFrame(input));
    assertNull(GameSocketProtocol.readFrame(input));
  }

  @Test
  void incompleteHeadersAndBodiesAreRejected() {
    assertThrows(EOFException.class, () -> GameSocketProtocol.readFrame(new ByteArrayInputStream(new byte[] {0, 0})));
    assertThrows(EOFException.class, () -> GameSocketProtocol.readFrame(new ByteArrayInputStream(new byte[] {0, 0, 0, 2, 1})));
  }

  @Test
  void invalidLengthsAndUtf8AreRejectedBeforeTheyCanBecomeMessages() throws Exception {
    var zero = new ByteArrayOutputStream();
    new DataOutputStream(zero).writeInt(0);
    assertThrows(IOException.class, () -> GameSocketProtocol.readFrame(new ByteArrayInputStream(zero.toByteArray())));
    var tooLarge = new ByteArrayOutputStream();
    new DataOutputStream(tooLarge).writeInt(GameSocketProtocol.MAX_FRAME_BYTES + 1);
    assertThrows(IOException.class, () -> GameSocketProtocol.readFrame(new ByteArrayInputStream(tooLarge.toByteArray())));
    var malformed = new ByteArrayOutputStream();
    var output = new DataOutputStream(malformed);
    output.writeInt(1);
    output.writeByte(0xff);
    assertThrows(IOException.class, () -> GameSocketProtocol.readFrame(new ByteArrayInputStream(malformed.toByteArray())));
  }

  @Test
  void oversizedAndMalformedUnicodeWritesAreRejected() {
    assertThrows(IOException.class, () -> GameSocketProtocol.writeFrame(new ByteArrayOutputStream(), "x".repeat(GameSocketProtocol.MAX_FRAME_BYTES + 1)));
    assertThrows(IOException.class, () -> GameSocketProtocol.writeFrame(new ByteArrayOutputStream(), "\ud800"));
  }
}
