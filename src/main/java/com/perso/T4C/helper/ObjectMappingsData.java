package com.perso.T4C.helper;

import com.perso.T4C.render.ObjectMapping;

public final class ObjectMappingsData {
  private ObjectMappingsData() {}

  public static final class Entry {
    public String logicalName;
    public ObjectMapping mapping;

    public Entry() {}

    public Entry(String logicalName, ObjectMapping mapping) {
      this.logicalName = logicalName;
      this.mapping = mapping;
    }
  }
}
