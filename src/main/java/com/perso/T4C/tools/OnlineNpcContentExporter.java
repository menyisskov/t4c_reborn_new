package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.perso.T4C.npc.Kilhiam;
import com.perso.T4C.npc.WardenAelric;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.online.model.NpcInteractionContent;
import com.perso.T4C.spawn.Spawn;
import java.nio.file.*;
import java.util.*;

/** Export authored NPC data without loading desktop actors in the server process. */
public final class OnlineNpcContentExporter {
  private OnlineNpcContentExporter() {}
  public static NpcInteractionContent snapshot() {
    return new NpcInteractionContent(NpcInteractionContent.VERSION, List.of(
        npc(Kilhiam.class, Kilhiam.spec()), npc(WardenAelric.class, WardenAelric.spec())));
  }
  private static NpcInteractionContent.Npc npc(Class<?> type, NpcSpec spec) {
    var spawn = type.getAnnotation(Spawn.class);
    if (spawn == null || spawn.aggressive() || !spawn.type().equals(spec.id()))
      throw new IllegalArgumentException("Unsupported story NPC spawn");
    var parts = new TreeMap<String, String>();
    for (var part : spec.parts()) parts.put(part.bodyPart().name(), part.spriteBase());
    return new NpcInteractionContent.Npc(spec.id(), spec.displayName(), spawn.z(), spawn.x(), spawn.y(), spec.spriteBase(), parts);
  }
  public static String render(NpcInteractionContent content) {
    var gson = new Gson(); var tree = gson.toJsonTree(content).getAsJsonObject();
    for (int i=0;i<content.npcs().size();i++)
      tree.getAsJsonArray("npcs").get(i).getAsJsonObject().add("parts",gson.toJsonTree(new TreeMap<>(content.npcs().get(i).parts())));
    return gson.toJson(tree)+"\n";
  }
  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Expected output JSON path");
    var path=Path.of(args[0]); if(path.getParent()!=null) Files.createDirectories(path.getParent());
    Files.writeString(path,render(snapshot()));
  }
}
