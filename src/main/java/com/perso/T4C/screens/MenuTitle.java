package com.perso.T4C.screens;

import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

final class MenuTitle {
  final String name;
  final Rectangle bounds = new Rectangle();
  final List<MenuItem> items = new ArrayList<>();

  MenuTitle(String name) {
    this.name = name;
  }
}
