package com.perso.T4C.monster;

import com.perso.T4C.movement.BaseMovement;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles movement logic for monsters (direction, facing, flip).
 */
@Slf4j
public class MonsterMovement extends BaseMovement {

    @Override
    protected void logDirectionChange(float dx, float worldDy) {
        // Intentionally muted.
    }
}

