package com.perso.T4C.quest;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data-driven definition for a one-time monster-kill quest.
 *
 * <p>The first quest format deliberately supports one objective. Additional
 * objective types can be introduced through a versioned binary migration when
 * the game needs them.
 */
@Getter
@AllArgsConstructor
public final class QuestDef {
    private final String id;
    private final String title;
    private final String giverNpc;
    private final String targetMonster;
    private final int requiredKills;
    private final int targetWorldZ;
    private final int areaCenterX;
    private final int areaCenterY;
    private final int areaRadiusTiles;
    private final int rewardGold;
    private final int rewardXp;
    private final String offerText;
    private final String completionText;
    private final String completedText;
}
