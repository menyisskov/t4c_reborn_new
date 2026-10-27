package com.perso.T4C.online.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Build-time inventory; it does not register conversations or publish NPCs to a hosted world. */
public record NpcAppearanceContent(int version, List<Spawn> spawns) {
  public static final int VERSION=1;
  public NpcAppearanceContent {
    if(version!=VERSION) throw new IllegalArgumentException("Unsupported NPC appearance version");
    spawns=List.copyOf(spawns);
    var keys=new java.util.HashSet<String>();
    for(var spawn:spawns)
      if(!keys.add(spawn.spawnKey())) throw new IllegalArgumentException("Duplicate NPC spawn identity");
  }

  public enum FrameEvidence { FRAME_NAME_PRESENT, NO_FRAME, UNKNOWN }
  public record Spawn(String spawnKey,String type,String canonicalNpcId,int world,int x,int y,
      boolean stationary,boolean aggressive,boolean deployable,String displayName,
      String spriteBase,Map<String,String> parts,boolean interactionSupported,
      FrameEvidence frameEvidence,String appearanceDiagnostic) {
    public Spawn {
      if(type==null || type.isBlank() || canonicalNpcId==null || canonicalNpcId.isBlank()
          || world<0 || x<0 || y<0 || displayName==null || displayName.isBlank())
        throw new IllegalArgumentException("Invalid NPC appearance spawn");
      String expected=new MonsterContent.Spawn(type,null,x,y,world,stationary,aggressive).persistenceKey();
      if(!expected.equals(spawnKey)) throw new IllegalArgumentException("NPC spawn identity mismatch");
      parts=Map.copyOf(parts);Objects.requireNonNull(frameEvidence);
      if(appearanceDiagnostic==null || appearanceDiagnostic.isBlank())
        throw new IllegalArgumentException("Missing appearance verification diagnostic");
    }
  }
}
