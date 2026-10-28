package com.perso.T4C.session;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.player.Player;

/**
 * What one fixed simulation step hands the world driver: the registries (either may be absent), the
 * played character and its position, the step length and the tile window the screen shows. {@code
 * margin} widens the window on every side.
 */
public record EntityTick(
    NPCManager npcs,
    MonsterManager monsters,
    Player player,
    Vector2 playerPosition,
    float delta,
    int startX,
    int endX,
    int startY,
    int endY,
    int margin) {}
