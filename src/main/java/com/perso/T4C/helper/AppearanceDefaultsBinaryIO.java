package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AppearanceDefaultsBinaryIO {
  private static final byte[] MAGIC = "T4CAPD".getBytes(StandardCharsets.US_ASCII);
  private static final short VERSION = 3;
  private static final int MAX_STRING_BYTES = 4096;

  private AppearanceDefaultsBinaryIO() {}

  public record NakedPart(String gender, String bodyPart, String sprite) {}

  public record ConcealmentRule(
      String triggerSlot, String appearance, List<String> hiddenParts, boolean hidesExplicit) {
    public ConcealmentRule {
      hiddenParts = hiddenParts == null ? List.of() : List.copyOf(hiddenParts);
    }
  }

  public record EquippedOverride(
      String gender,
      String sourceSlot,
      String sourceAppearance,
      String targetSlot,
      String targetAppearance) {}

  public record Defaults(
      List<NakedPart> nakedParts,
      List<ConcealmentRule> concealmentRules,
      List<EquippedOverride> equippedOverrides) {
    public Defaults(List<NakedPart> nakedParts, List<ConcealmentRule> concealmentRules) {
      this(nakedParts, concealmentRules, List.of());
    }

    public Defaults {
      nakedParts = nakedParts == null ? List.of() : List.copyOf(nakedParts);
      concealmentRules = concealmentRules == null ? List.of() : List.copyOf(concealmentRules);
      equippedOverrides = equippedOverrides == null ? List.of() : List.copyOf(equippedOverrides);
    }
  }

  public static Defaults read(File file) throws IOException, GameException {
    try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
      byte[] magic = new byte[MAGIC.length];
      in.readFully(magic);
      if (!Arrays.equals(magic, MAGIC)) {
        throw new GameException("Invalid appearance defaults binary file: wrong magic header");
      }
      short version = BinaryIOUtils.readShortLE(in);
      if (version < 1 || version > VERSION) {
        throw new GameException("Unsupported appearance defaults binary version: " + version);
      }
      int partCount = BinaryIOUtils.readIntLE(in);
      if (partCount < 0) {
        throw new GameException("Invalid naked part count: " + partCount);
      }
      List<NakedPart> parts = new ArrayList<>(partCount);
      for (int i = 0; i < partCount; i++) {
        String gender = BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim();
        String bodyPart = BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim();
        String sprite = BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim();
        if (!gender.isEmpty() && !bodyPart.isEmpty() && !sprite.isEmpty()) {
          parts.add(new NakedPart(gender, bodyPart, sprite));
        }
      }
      int ruleCount = BinaryIOUtils.readIntLE(in);
      if (ruleCount < 0) {
        throw new GameException("Invalid concealment rule count: " + ruleCount);
      }
      List<ConcealmentRule> rules = new ArrayList<>(ruleCount);
      for (int i = 0; i < ruleCount; i++) {
        String triggerSlot =
            version == 1 ? "HEAD" : BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim();
        String appearance = BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim();
        List<String> hiddenParts;
        boolean hidesExplicit;
        if (version == 1) {
          hiddenParts = new ArrayList<>(2);
          if (BinaryIOUtils.readIntLE(in) != 0) hiddenParts.add("HEAD");
          if (BinaryIOUtils.readIntLE(in) != 0) hiddenParts.add("HAIR");
          hidesExplicit = false;
        } else {
          String csv = BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim();
          hiddenParts =
              csv.isEmpty()
                  ? List.of()
                  : Arrays.stream(csv.split(","))
                      .map(String::trim)
                      .filter(value -> !value.isEmpty())
                      .toList();
          hidesExplicit = BinaryIOUtils.readIntLE(in) != 0;
        }
        if (!triggerSlot.isEmpty() && !appearance.isEmpty()) {
          rules.add(new ConcealmentRule(triggerSlot, appearance, hiddenParts, hidesExplicit));
        }
      }
      List<EquippedOverride> overrides = new ArrayList<>();
      if (version >= 3) {
        int overrideCount = BinaryIOUtils.readIntLE(in);
        if (overrideCount < 0) {
          throw new GameException("Invalid equipped override count: " + overrideCount);
        }
        for (int i = 0; i < overrideCount; i++) {
          overrides.add(
              new EquippedOverride(
                  BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim(),
                  BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim(),
                  BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim(),
                  BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim(),
                  BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim()));
        }
      }
      return new Defaults(parts, rules, overrides);
    }
  }

  public static void write(File file, Defaults defaults) throws IOException {
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }
    Defaults safe = defaults != null ? defaults : new Defaults(List.of(), List.of());
    try (DataOutputStream out =
        new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
      out.write(MAGIC);
      BinaryIOUtils.writeShortLE(out, VERSION);
      BinaryIOUtils.writeIntLE(out, safe.nakedParts().size());
      for (NakedPart part : safe.nakedParts()) {
        BinaryIOUtils.writeString(out, trimmed(part.gender()));
        BinaryIOUtils.writeString(out, trimmed(part.bodyPart()));
        BinaryIOUtils.writeString(out, trimmed(part.sprite()));
      }
      BinaryIOUtils.writeIntLE(out, safe.concealmentRules().size());
      for (ConcealmentRule rule : safe.concealmentRules()) {
        BinaryIOUtils.writeString(out, trimmed(rule.triggerSlot()));
        BinaryIOUtils.writeString(out, trimmed(rule.appearance()));
        BinaryIOUtils.writeString(
            out,
            rule.hiddenParts().stream()
                .map(AppearanceDefaultsBinaryIO::trimmed)
                .filter(value -> !value.isEmpty())
                .collect(java.util.stream.Collectors.joining(",")));
        BinaryIOUtils.writeIntLE(out, rule.hidesExplicit() ? 1 : 0);
      }
      BinaryIOUtils.writeIntLE(out, safe.equippedOverrides().size());
      for (EquippedOverride override : safe.equippedOverrides()) {
        BinaryIOUtils.writeString(out, trimmed(override.gender()));
        BinaryIOUtils.writeString(out, trimmed(override.sourceSlot()));
        BinaryIOUtils.writeString(out, trimmed(override.sourceAppearance()));
        BinaryIOUtils.writeString(out, trimmed(override.targetSlot()));
        BinaryIOUtils.writeString(out, trimmed(override.targetAppearance()));
      }
    }
  }

  private static String trimmed(String value) {
    return value != null ? value.trim() : "";
  }
}
