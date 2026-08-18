package com.perso.T4C.render;

import com.perso.T4C.mapping.definition.ObjectMappingDefinitions;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ObjectMappings {
  private ObjectMappings() {}

  public static Map<String, ObjectMapping> load() {
    return new HashMap<>(ObjectMappingDefinitions.all());
  }

  public static void save(Map<String, ObjectMapping> mappings) throws IOException {
    throw new UnsupportedOperationException("Object mappings definitions are Java source");
  }
}
