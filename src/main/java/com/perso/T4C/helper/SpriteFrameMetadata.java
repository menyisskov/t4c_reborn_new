package com.perso.T4C.helper;

/** Frame data read from an authored sprite binary without creating a graphics resource. */
public record SpriteFrameMetadata(
    String name,
    int width,
    int height,
    int type,
    int drawOffset1X,
    int drawOffset1Y,
    int drawOffset2X,
    int drawOffset2Y) {
  static SpriteFrameMetadata from(SpriteLoader.Sprite texture, SpriteLoader.Sprite offsets,
      int width, int height) {
    return new SpriteFrameMetadata(
        texture.name,
        width,
        height,
        texture.type,
        offsets.drawOffset1X,
        offsets.drawOffset1Y,
        offsets.drawOffset2X,
        offsets.drawOffset2Y);
  }
}
