package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class OnlineNpcAppearanceExporterTest {
  @Test void lighthavenSamaritanAppearanceIsRegisteredAndDeployable() throws Exception {
    var appearance=OnlineNpcAppearanceExporter.snapshot(Path.of("assets/sprites")).spawns().stream()
        .filter(spawn->spawn.type().equals("LighthavenSamaritan") && spawn.x()==2931 && spawn.y()==1072)
        .findFirst().orElseThrow();
    assertEquals("LighthavenSamaritan",appearance.canonicalNpcId());
    assertTrue(appearance.interactionSupported());
    assertTrue(appearance.deployable());
  }
}
