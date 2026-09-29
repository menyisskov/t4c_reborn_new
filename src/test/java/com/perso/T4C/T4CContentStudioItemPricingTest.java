package com.perso.T4C;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.content.ItemJavaExporter;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemSalePricing;
import com.perso.T4C.item.json.ItemJsonDef;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class T4CContentStudioItemPricingTest {
  @TempDir Path output;

  @Test
  void editorAndGeneratedJavaPreserveBothPricingModes() throws Exception {
    var studio = new T4CContentStudio();
    var json = new ItemJsonDef();
    json.key = "resale_export_test";
    json.bodyPart = "BODY";
    var original = json.toItemDefinition();
    for (boolean enabled : List.of(true, false)) {
      original.setRarityPriced(enabled);
      var data = studio.itemToMap(original);
      var restored = studio.itemFromMap(data);
      assertEquals(enabled, restored.isRarityPriced());
      assertEquals(ItemSalePricing.sellPrice(original), ItemSalePricing.sellPrice(restored));
      Path dir = output.resolve(Boolean.toString(enabled));
      ItemJavaExporter.export(List.of(restored), dir);
      Path source;
      try (var paths = Files.list(dir)) {
        source =
            paths
                .filter(p -> !p.getFileName().toString().equals("ItemDefinitions.java"))
                .findFirst()
                .orElseThrow();
      }
      int result =
          ToolProvider.getSystemJavaCompiler()
              .run(
                  null,
                  null,
                  null,
                  "-proc:none",
                  "-classpath",
                  System.getProperty("java.class.path"),
                  "-d",
                  dir.toString(),
                  source.toString());
      assertEquals(0, result, "Generated definition must compile");
      try (var loader =
          new URLClassLoader(
              new java.net.URL[] {dir.toUri().toURL()}, getClass().getClassLoader())) {
        String name = source.getFileName().toString().replace(".java", "");
        var regenerated =
            (ItemDefinition)
                loader
                    .loadClass("com.perso.T4C.item.definition." + name)
                    .getMethod("definition")
                    .invoke(null);
        assertEquals(enabled, regenerated.isRarityPriced());
        assertEquals(ItemSalePricing.sellPrice(original), ItemSalePricing.sellPrice(regenerated));
      }
    }
    var oldData = studio.itemToMap(original);
    oldData.remove("rarityPriced");
    assertFalse(studio.itemFromMap(oldData).isRarityPriced());
  }
}
