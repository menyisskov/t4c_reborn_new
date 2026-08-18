package com.perso.T4C.helper;

import com.badlogic.gdx.graphics.Color;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CollisionType {
  NONE(0, "None", false, false, new Color(0f, 0f, 0f, 0f)),
  ABSOLUTE(1, "Absolute", true, true, new Color(0.95f, 0.12f, 0.12f, 0.65f)),
  FLY_OVER(2, "Fly over", true, true, new Color(0.95f, 0.45f, 0.10f, 0.65f)),
  DEEP_WATER(3, "Deep water", true, false, new Color(0.08f, 0.28f, 0.95f, 0.65f)),
  SHALLOW_WATER(4, "Shallow water", true, false, new Color(0.10f, 0.72f, 0.95f, 0.65f)),
  BUILDING(5, "Building", false, false, new Color(0.55f, 0.38f, 0.20f, 0.65f)),
  SAFE_HAVEN(6, "Safe haven", false, false, new Color(0.12f, 0.82f, 0.25f, 0.65f)),
  INDOOR_SAFE_HAVEN(7, "Indoor safe haven", false, false, new Color(0.10f, 0.58f, 0.22f, 0.65f)),
  FULL_PVP_AREA(8, "Full PvP area", false, false, new Color(0.75f, 0.12f, 0.72f, 0.65f)),
  FORCE_FIELD(9, "Force field", false, true, new Color(0.86f, 0.15f, 0.92f, 0.65f)),
  FULL_PVP_NO_DROP(10, "Full PvP no drop", false, false, new Color(0.72f, 0.15f, 0.42f, 0.65f)),
  HARDCORE_2(11, "Hardcore 2", false, false, new Color(0.48f, 0.08f, 0.08f, 0.65f)),
  ARENA_FULL_PVP(12, "Arena full PvP", false, false, new Color(0.92f, 0.42f, 0.72f, 0.65f)),
  HARDCORE(13, "Hardcore", false, false, new Color(0.65f, 0.05f, 0.05f, 0.65f)),
  FULL_PVP(14, "Full PvP", false, false, new Color(0.92f, 0.08f, 0.42f, 0.65f)),
  FULL_PVP_NO_DROP_CAST(
      15, "Full PvP no drop/cast", false, false, new Color(0.52f, 0.05f, 0.32f, 0.65f));
  private static final CollisionType[] BY_VALUE = new CollisionType[16];

  static {
    Arrays.stream(values()).forEach(type -> BY_VALUE[type.value] = type);
  }

  private final int value;
  private final String displayName;
  private final boolean blocksMovement;
  private final boolean blocksLineOfSight;
  private final Color editorColor;

  public static CollisionType fromValue(int value) {
    return value >= 0 && value < BY_VALUE.length ? BY_VALUE[value] : ABSOLUTE;
  }
}
