package com.perso.T4C.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

final class MenuItem {
  final String label;
  final Runnable action;
  final BooleanSupplier activeSupplier;
  final List<MenuItem> children = new ArrayList<>();

  MenuItem(String label, Runnable action) {
    this(label, action, null);
  }

  MenuItem(String label, Runnable action, BooleanSupplier activeSupplier) {
    this.label = label;
    this.action = action;
    this.activeSupplier = activeSupplier;
  }

  MenuItem(String label) {
    this(label, null, null);
  }

  MenuItem add(MenuItem item) {
    children.add(item);
    return this;
  }

  boolean hasChildren() {
    return !children.isEmpty();
  }
}
