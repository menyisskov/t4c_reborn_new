package com.perso.T4C.npc.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class NpcSpawnOwnershipTest {
  @Test void exportedAliasConflictFailsInsteadOfChoosingClasspathOrder() {
    var first=new NpcFactoryRegistry.Registration("First","First","First",c->null,null,List.of("shared"));
    var second=new NpcFactoryRegistry.Registration("Second","Second","Second",c->null,null,List.of("SHARED"));
    assertThrows(IllegalStateException.class,
        ()->NpcSpawnOwnership.resolveStrict(" Shared ",List.of(first,second)));
    assertEquals("First",NpcSpawnOwnership.resolveStrict("shared",List.of(first)).id());
  }
}
