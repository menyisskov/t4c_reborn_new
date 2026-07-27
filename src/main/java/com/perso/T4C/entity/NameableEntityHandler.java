package com.perso.T4C.entity;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Predicate;

/**
 * Utility class for handling right-click interactions on nameable entities.
 */
@Slf4j
public class NameableEntityHandler {

    /**
     * Handle right-click on a list of nameable entities.
     * Shows the name of the first entity under the cursor.
     *
     * @param mouseX X coordinate in world space
     * @param mouseY Y coordinate in world space
     * @param entities List of nameable entities to check
     * @param <T> Type of nameable entity
     * @return true if an entity was clicked, false otherwise
     */
    public static <T extends Nameable> boolean handleRightClick(float mouseX, float mouseY, List<T> entities) {
        return handleRightClick(mouseX, mouseY, entities, null);
    }

    /**
     * Handle right-click on a list of nameable entities with an optional filter.
     * Shows the name of the first entity under the cursor that passes the filter.
     *
     * @param mouseX X coordinate in world space
     * @param mouseY Y coordinate in world space
     * @param entities List of nameable entities to check
     * @param filter Optional predicate to filter entities (null = no filter)
     * @param <T> Type of nameable entity
     * @return true if an entity was clicked, false otherwise
     */
    public static <T extends Nameable> boolean handleRightClick(float mouseX, float mouseY, List<T> entities, Predicate<T> filter) {
        for (T entity : entities) {
            // Check if entity is under cursor
            if (!entity.isMouseOver(mouseX, mouseY)) {
                continue;
            }

            // Apply filter if provided
            if (filter != null && !filter.test(entity)) {
                continue;
            }

            // Show name
            entity.showName();
            log.info("Showing name for {}: {}", entity.getClass().getSimpleName(), entity.getName());
            return true;
        }
        return false;
    }
}

