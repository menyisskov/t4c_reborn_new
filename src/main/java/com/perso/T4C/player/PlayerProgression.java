package com.perso.T4C.player;

import com.perso.T4C.helper.XpCurve;
/**
 * Class representing PlayerProgression.
 */

final class PlayerProgression {
    void addXp(Player player, int amount, XpCurve xpCurve) {
        if (player == null || amount <= 0) {
            return;
        }
        amount = Math.round(amount * (1f + player.getBuffXpMultiplier()));
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
            player.setStatPoints(player.getStatPoints() + 5);
            player.setSkillPoints(player.getSkillPoints() + 15);
            player.showLevelUpMessage(player.getLevel());
            int next = xpCurve.getXpToNextLevel(player.getLevel());
            if (next > 0) {
                player.setXpToNextLevel(next);
            } else {
                break;
            }
        }
    }
}
