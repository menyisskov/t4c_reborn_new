package com.perso.T4C.spell;

import com.perso.T4C.monster.MonsterDef;
import static com.perso.T4C.config.GameConstants.TAME_MAX_RANGE_TILES;

public final class TameValidator {
    public enum Failure { NONE, NOT_A_BEAST, NOT_TAMEABLE, TARGET_DEAD,
        LEVEL_TOO_HIGH, ALREADY_HAS_PET, TOO_FAR, ALREADY_CHANNELING }
    private TameValidator() {}
    public static Failure check(MonsterDef def, boolean dead, int playerLevel,
                                boolean hasCompanion, float distanceTiles, boolean channeling) {
        if (channeling) return Failure.ALREADY_CHANNELING;
        if (dead) return Failure.TARGET_DEAD;
        if (def == null) return Failure.NOT_A_BEAST;
        if (!def.isTameable()) return Failure.NOT_TAMEABLE;
        if (!def.canBeTamedBy(playerLevel)) return Failure.LEVEL_TOO_HIGH;
        if (hasCompanion) return Failure.ALREADY_HAS_PET;
        if (distanceTiles > TAME_MAX_RANGE_TILES) return Failure.TOO_FAR;
        return Failure.NONE;
    }
}
