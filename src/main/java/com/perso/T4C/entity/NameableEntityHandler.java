package com.perso.T4C.entity;

import java.util.List;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NameableEntityHandler {
  public static <T extends Nameable> boolean handleRightClick(
      float mouseX, float mouseY, List<T> entities) {
    return handleRightClick(mouseX, mouseY, entities, null);
  }

  public static <T extends Nameable> boolean handleRightClick(
      float mouseX, float mouseY, List<T> entities, Predicate<T> filter) {
    for (T entity : entities) {
      if (!entity.isMouseOver(mouseX, mouseY)) {
        continue;
      }
      if (filter != null && !filter.test(entity)) {
        continue;
      }
      entity.showName();
      log.info("Showing name for {}: {}", entity.getClass().getSimpleName(), entity.getName());
      return true;
    }
    return false;
  }
}
