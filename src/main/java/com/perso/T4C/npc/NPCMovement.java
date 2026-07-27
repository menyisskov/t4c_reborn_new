package com.perso.T4C.npc;

import com.perso.T4C.movement.BaseMovement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles NPC movement state (angle, moving flag, flip).
 */
@Slf4j
@Getter
public class NPCMovement extends BaseMovement {

    @Override
    protected void logDirectionChange(float dx, float worldDy) {
        // Intentionally muted.
    }
}
