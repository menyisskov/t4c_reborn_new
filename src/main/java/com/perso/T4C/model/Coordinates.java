package com.perso.T4C.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Coordinates {
  private float x;
  private float y;
  private int z;

  public void set(float x, float y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}
