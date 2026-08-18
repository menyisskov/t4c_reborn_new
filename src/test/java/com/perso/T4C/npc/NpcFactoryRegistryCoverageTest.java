package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.npc.arakas.LighthavenSamaritan;
import com.perso.T4C.npc.core.*;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NpcFactoryRegistryCoverageTest {
  private static final int ORIGINAL_CATALOGUE_SIZE = 460;

  @Test
  void everyHistoricalDefinitionIsAUniqueConcreteJavaNpc() throws Exception {
    var registrations = NpcFactoryRegistry.registrations();
    assertEquals(ORIGINAL_CATALOGUE_SIZE, registrations.size());

    Set<String> ids = new HashSet<>();
    for (NpcFactoryRegistry.Registration registration : registrations) {
      assertTrue(
          ids.add(registration.id().toLowerCase(Locale.ROOT)),
          () -> "Duplicate NPC id " + registration.id());
      BaseNPC npc = registration.factory().create(new NpcContext(null));
      assertNotNull(npc, registration.id());
      assertEquals(registration.id(), npc.getTypeId());
      assertEquals(registration.id(), npc.getClass().getSimpleName());
      if (registration.id().equals(LighthavenSamaritan.ID)) {
        assertInstanceOf(LighthavenSamaritan.class, npc);
        assertNull(registration.specification());
      } else {
        ScriptedNpc scripted = assertInstanceOf(ScriptedNpc.class, npc);
        assertNotNull(registration.specification(), registration.id());
        assertEquals(registration.id(), scripted.getSpec().id());
        assertTrue(
            scripted.usesJavaBehavior()
                || (scripted.getSpec().sourceScript() != null
                    && !scripted.getSpec().sourceScript().isBlank()),
            registration.id());
      }
    }
  }

  @Test
  void everyShippedSpawnTypeHasAJavaFactoryAndNoBinaryDefinitionCatalogueRemains()
      throws Exception {
    for (SpawnBinaryIO.Entry spawn : SpawnBinaryIO.read(new File("assets/spawns/npc_spawns.bin"))) {
      assertNotNull(NpcFactoryRegistry.find(spawn.type), () -> "Missing spawn type " + spawn.type);
    }
    assertFalse(new File("assets/npcs/npcs.bin").exists());
  }

  @Test
  void everyRegisteredNpcUsesNativeJavaSpecWithoutLegacyScriptPayloads() {
    for (NpcFactoryRegistry.Registration registration : NpcFactoryRegistry.registrations()) {
      if (registration.id().equals(LighthavenSamaritan.ID)) continue;
      NpcSpec specification = NpcFactoryRegistry.specification(registration.id());
      assertNotNull(specification, registration.id());
      assertNull(specification.sourceScript(), registration.id());
      assertTrue(specification.sourceEvents().isEmpty(), registration.id());
      BaseNPC npc = registration.factory().create(new NpcContext(null));
      assertInstanceOf(ScriptedNpc.class, npc);
      assertTrue(((ScriptedNpc) npc).usesJavaBehavior(), registration.id());
    }
  }
}
