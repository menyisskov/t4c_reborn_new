package com.perso.T4C.player;

import com.perso.T4C.helper.XpCurve;

import java.util.random.RandomGenerator;
/**
 * Class representing PlayerProgression.
 */

public final class PlayerProgression {
    private final RandomGenerator random;

    PlayerProgression() {
        this(RandomGenerator.getDefault());
    }

    PlayerProgression(RandomGenerator random) {
        this.random = random;
    }

    void addXp(Player player, int amount, XpCurve xpCurve, boolean applyMultiplier) {
        if (player == null || amount <= 0) {
            return;
        }
        if (applyMultiplier) {
            amount = Math.round(amount * (1f + player.getBuffXpMultiplier()));
        }
        player.setCurrentXp(player.getCurrentXp() + amount);
        if (xpCurve == null) {
            return;
        }
        if (player.getLevel() <= 0) {
            player.setLevel(1);
        }
        while (player.getXpToNextLevel() > 0 && player.getCurrentXp() >= player.getXpToNextLevel()) {
            player.setCurrentXp(player.getCurrentXp() - player.getXpToNextLevel());
            player.setLevel(player.getLevel() + 1);
            int[] gains = applyOriginalT4cResourceGains(player);
            player.setStatPoints(player.getStatPoints() + 5);
            player.setSkillPoints(player.getSkillPoints() + 15);
            player.showLevelUpMessage(player.getLevel(), gains[0], gains[1]);
            int next = xpCurve.getXpToNextLevel(player.getLevel());
            if (next > 0) {
                player.setXpToNextLevel(next);
            } else {
                break;
            }
        }
    }

    /**
     * Original T4C level-up rolls, ported from Character.cpp's training block.
     *
     * <p>The rolls read the permanent attributes only, exactly as GoN reads
     * {@code GetTrueEND}, {@code GetTrueINT} and {@code GetTrueWIS}: temporary
     * and equipment boosts never contribute. Both the maximum and the current
     * value gain the very same amount, uncapped, so a level-up always heals by
     * exactly what it grants.
     *
     * @return the hit point gain followed by the mana gain
     */
    private int[] applyOriginalT4cResourceGains(Player player) {
        int hpGain = hitPointGainBase(player.getEndurance()) + random.nextInt(3);
        int manaGain = manaGainBase(player.getIntelligence(), player.getWisdom()) + random.nextInt(3);

        player.setMaxHp(player.getMaxHp() + hpGain);
        player.setCurrentHp(player.getCurrentHp() + hpGain);
        player.setMaxMana(player.getMaxMana() + manaGain);
        player.setMana(player.getMana() + manaGain);

        return new int[] { hpGain, manaGain };
    }

    public static int hitPointGainBase(int endurance) {
        return 6 + Math.max(0, endurance) / 20;
    }

    public static int manaGainBase(int intelligence, int wisdom) {
        return 3 + Math.max(0, intelligence) / 30 + Math.max(0, wisdom) / 60;
    }
}
