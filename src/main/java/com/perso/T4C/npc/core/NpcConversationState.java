package com.perso.T4C.npc.core;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.List;
import java.util.Map;

/** A participant's transient dialogue state on one original NPC instance. */
public record NpcConversationState(
    boolean interacting,
    Vector2 savedPatrolTarget,
    float savedPauseTimer,
    boolean wasPaused,
    List<String> dialogLines,
    int dialogPageIndex,
    boolean dialogActive,
    String dialogKeyword,
    List<Rectangle> keywordBounds,
    Map<String, List<Rectangle>> linkBounds,
    long lastInteractionRequestAt,
    long shoutUntil,
    Object authoredState) {}
