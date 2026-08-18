package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.skill.SkillDefinition;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SkillDefBinaryIO {
  private static final byte[] MAGIC = "T4CSKL".getBytes(StandardCharsets.US_ASCII);
  private static final short VERSION = 1;
  private static final int MAX_STRING_BYTES = 4096;

  private SkillDefBinaryIO() {}

  public static List<SkillDefinition> read(File file) throws IOException, GameException {
    return BinaryCatalogueIO.read(
        file,
        MAGIC,
        "skill definition",
        version -> {
          if (version < 1 || version > VERSION) {
            throw new GameException("Unsupported skill definition version: " + version);
          }
        },
        (in, version) -> readDef(in));
  }

  public static void write(File file, List<SkillDefinition> defs) throws IOException {
    BinaryCatalogueIO.write(file, MAGIC, VERSION, defs, SkillDefBinaryIO::writeDef);
  }

  private static SkillDefinition readDef(DataInputStream in) throws IOException {
    String id = readString(in);
    int minimumLevel = BinaryIOUtils.readIntLE(in);
    int minimumStrength = BinaryIOUtils.readIntLE(in);
    int minimumEndurance = BinaryIOUtils.readIntLE(in);
    int minimumAgility = BinaryIOUtils.readIntLE(in);
    int minimumIntelligence = BinaryIOUtils.readIntLE(in);
    int minimumWisdom = BinaryIOUtils.readIntLE(in);
    int learningCost = BinaryIOUtils.readIntLE(in);
    int prerequisiteCount = BinaryIOUtils.readIntLE(in);
    Map<String, Integer> prerequisites = new LinkedHashMap<>();
    for (int i = 0; i < prerequisiteCount; i++) {
      String prereqId = readString(in);
      int prereqLevel = BinaryIOUtils.readIntLE(in);
      prerequisites.put(prereqId, prereqLevel);
    }
    long useCooldownMillis = BinaryIOUtils.readLongLE(in);
    return new SkillDefinition(
        id,
        minimumLevel,
        minimumStrength,
        minimumEndurance,
        minimumAgility,
        minimumIntelligence,
        minimumWisdom,
        learningCost,
        prerequisites,
        useCooldownMillis);
  }

  private static void writeDef(DataOutputStream out, SkillDefinition def) throws IOException {
    writeString(out, def == null ? "" : def.id());
    BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.minimumLevel());
    BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.minimumStrength());
    BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.minimumEndurance());
    BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.minimumAgility());
    BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.minimumIntelligence());
    BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.minimumWisdom());
    BinaryIOUtils.writeIntLE(out, def == null ? 0 : def.learningCost());
    Map<String, Integer> prerequisites = def == null ? Map.of() : def.prerequisites();
    BinaryIOUtils.writeIntLE(out, prerequisites.size());
    for (Map.Entry<String, Integer> entry : prerequisites.entrySet()) {
      writeString(out, entry.getKey());
      BinaryIOUtils.writeIntLE(out, entry.getValue());
    }
    BinaryIOUtils.writeLongLE(out, def == null ? 0L : def.useCooldownMillis());
  }

  private static String readString(DataInputStream in) throws IOException {
    return BinaryIOUtils.readString(in, MAX_STRING_BYTES);
  }

  private static void writeString(DataOutputStream out, String value) throws IOException {
    BinaryIOUtils.writeString(out, value);
  }
}
