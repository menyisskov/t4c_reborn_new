package com.perso.T4C.helper;

import java.util.List;

public final class GroundMosaicData {
  private GroundMosaicData() {}

  public record Definition(String id, int width, int height, List<String> frames) {
    public Definition {
      frames = frames == null ? List.of() : List.copyOf(frames);
    }
  }
}
