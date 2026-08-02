package com.perso.T4C.spell;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;

import java.util.concurrent.ThreadLocalRandom;

/** Validation and resource transaction shared by every player spell cast. */
public final class SpellCastingService {
    public static final int MAXIMUM_CAST_RANGE_TILES = 20;

    public enum TargetKind { SELF, HOSTILE_UNIT, FRIENDLY_UNIT, POSITION }
    public enum Failure {
        NONE, NOT_LEARNED, WRONG_TARGET, PVP_FORBIDDEN, OUT_OF_RANGE,
        NO_LINE_OF_SIGHT, COOLDOWN, EXHAUSTED, NOT_ENOUGH_MANA, ACTIVATION_FAILED
    }

    public record Request(SpellData spell, Player caster, TargetKind targetKind,
                          float distanceInTiles, boolean lineOfSightClear,
                          boolean pvpTarget, boolean knownSpellRequired) {
    }

    public record Result(boolean success, Failure failure, int manaSpent) {
        private static Result failure(Failure failure) { return new Result(false, failure, 0); }
        private static Result success(int mana) { return new Result(true, Failure.NONE, mana); }
    }

    private SpellCastingService() {
    }

    /** Mana and exhaustion are committed only after every validation and success roll. */
    public static Result begin(Request request) {
        return begin(request, true);
    }

    /** Executes an item-bound spell without interrupting the player's current combat action. */
    public static Result beginItemUse(Request request) {
        return begin(request, false);
    }

    private static Result begin(Request request, boolean interruptActions) {
        if (request == null || request.spell() == null || request.caster() == null) {
            return Result.failure(Failure.ACTIVATION_FAILED);
        }
        SpellData spell = request.spell();
        Player caster = request.caster();
        if (request.knownSpellRequired() && !hasLearnedSpell(caster, spell)) {
            return Result.failure(Failure.NOT_LEARNED);
        }
        if (!targetAccepted(spell.getTargetType(), request.targetKind())) return Result.failure(Failure.WRONG_TARGET);
        if (request.pvpTarget() && !spell.isPvp()) return Result.failure(Failure.PVP_FORBIDDEN);
        if (request.targetKind() != TargetKind.SELF && request.distanceInTiles() > MAXIMUM_CAST_RANGE_TILES) {
            return Result.failure(Failure.OUT_OF_RANGE);
        }
        if (spell.isLineOfSight() && request.targetKind() != TargetKind.SELF && !request.lineOfSightClear()) {
            return Result.failure(Failure.NO_LINE_OF_SIGHT);
        }
        if (caster.isSpellOnCooldown(spell.getName())) return Result.failure(Failure.COOLDOWN);
        if (caster.isMentallyExhausted() || caster.isStunned()) return Result.failure(Failure.EXHAUSTED);

        DiceFormula.Context context = context(caster);
        int manaCost = caster.hasUnlimitedResources() ? 0
                : Math.max(0, DiceFormula.of(spell.getManaCost()).evaluate(context));
        if (caster.getMana() < manaCost) return Result.failure(Failure.NOT_ENOUGH_MANA);
        int successRate = spell.getSuccessRate() == null ? 100
                : Math.max(0, Math.min(100, DiceFormula.of(spell.getSuccessRate()).evaluate(context)));
        if (successRate == 0 || ThreadLocalRandom.current().nextInt(101) > successRate) {
            if (interruptActions) caster.interruptActions();
            return Result.failure(Failure.ACTIVATION_FAILED);
        }

        if (interruptActions) caster.disturb();
        caster.setMana(caster.getMana() - manaCost);
        caster.applyExhaustion(
                evaluateMillis(spell.getMentalExhaustion(), context),
                evaluateMillis(spell.getPhysicalExhaustion(), context),
                evaluateMillis(spell.getAttackExhaustion(), context));
        caster.triggerSpellCooldown(spell.getName(), spell.getCooldownSeconds());
        return Result.success(manaCost);
    }

    /** The player state stores learned spells by their canonical key. */
    private static boolean hasLearnedSpell(Player player, SpellData spell) {
        if (player.getSpells() == null) return false;
        for (String learned : player.getSpells()) {
            if (learned != null && (learned.equals(spell.getKey())
                    || (SpellRegistry.findByName(learned) != null
                    && SpellRegistry.findByName(learned).getKey().equals(spell.getKey())))) {
                return true;
            }
        }
        return false;
    }

    public static String message(Failure failure) {
        return switch (failure) {
            case NOT_LEARNED -> I18n.message("message.spell_not_learned");
            case WRONG_TARGET -> I18n.message("message.spell_wrong_target");
            case PVP_FORBIDDEN -> I18n.message("message.spell_pvp_forbidden");
            case OUT_OF_RANGE -> I18n.message("message.target_too_far");
            case NO_LINE_OF_SIGHT -> I18n.message("message.target_no_line_of_sight");
            case COOLDOWN -> I18n.message("message.spell_cooldown");
            case EXHAUSTED -> I18n.message("message.spell_exhausted");
            case NOT_ENOUGH_MANA -> I18n.message("message.not_enough_mana");
            case ACTIVATION_FAILED -> I18n.message("message.spell_failed");
            case NONE -> "";
        };
    }

    /** Evaluates how long this cast keeps the caster busy, in milliseconds. */
    public static long evaluateCastDurationMillis(SpellData spell, Player caster) {
        if (spell == null || caster == null) return 0L;
        DiceFormula.Context context = context(caster);
        return Math.max(evaluateMillis(spell.getMentalExhaustion(), context),
                Math.max(evaluateMillis(spell.getPhysicalExhaustion(), context),
                        evaluateMillis(spell.getAttackExhaustion(), context)));
    }

    private static boolean targetAccepted(int type, TargetKind kind) {
        if (type <= 0) return true; // legacy hand-authored spells
        return switch (kind) {
            case SELF -> type == 0 || type == 1 || type == 3 || type == 4 || type == 5 || type == 12 || type == 13
                    || type == 14 || type == 15 || type == 18;
            case HOSTILE_UNIT -> type == 0 || type == 2 || type == 4 || type == 8 || type == 9
                    || type == 10 || type == 11 || type == 17 || type == 19;
            case FRIENDLY_UNIT -> type == 0 || type == 3 || type == 4 || type == 10 || type == 12
                    || type == 13 || type == 14 || type == 17;
            case POSITION -> type == 6 || type == 16 || type == 19;
        };
    }

    static DiceFormula.Context context(Player caster) {
        return new DiceFormula.Context(caster.getEffectiveStrength(), caster.getEffectiveEndurance(),
                caster.getEffectiveDexterity(), caster.getEffectiveIntelligence(), 0,
                caster.getEffectiveWisdom(), 0, caster.getLevel());
    }

    private static long evaluateMillis(String formula, DiceFormula.Context context) {
        return Math.max(0L, DiceFormula.of(formula).evaluate(context));
    }
}
