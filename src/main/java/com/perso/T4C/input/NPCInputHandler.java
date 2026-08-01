package com.perso.T4C.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.perso.T4C.npc.BaseNPC;
import com.perso.T4C.npc.NPCManager;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.SystemMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * Handles input for NPC interactions (hover and click).
 */
@Slf4j
public class NPCInputHandler extends InputAdapter {

    private final NPCManager npcManager;
    private final OrthographicCamera camera;
    private final Player player;
    private final Function<BaseNPC, Boolean> offensiveSpellCastHandler;
    private final SystemMessage systemMessage;
    private final Vector3 worldCoordsTemp = new Vector3();

    public NPCInputHandler(NPCManager npcManager, OrthographicCamera camera, Player player) {
        this(npcManager, camera, player, null, null);
    }

    public NPCInputHandler(NPCManager npcManager, OrthographicCamera camera, Player player,
                           Function<BaseNPC, Boolean> offensiveSpellCastHandler, SystemMessage systemMessage) {
        this.npcManager = npcManager;
        this.camera = camera;
        this.player = player;
        this.offensiveSpellCastHandler = offensiveSpellCastHandler;
        this.systemMessage = systemMessage;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector2 worldPos = screenToWorld(screenX, screenY);
        npcManager.onMouseMove(worldPos.x, worldPos.y);
        return false; // Don't consume event
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 worldPos = screenToWorld(screenX, screenY);
        if (button == 0) { // Left click - interaction
            BaseNPC npc = npcManager.findNpcAt(worldPos.x, worldPos.y);
            if (npc != null && offensiveSpellCastHandler != null && offensiveSpellCastHandler.apply(npc)) {
                return true;
            }
            // A declined attack (an ally companion) falls through to conversation,
            // so the player can still give it orders while in combat mode.
            if (player != null && player.isCombatMode() && npc != null
                    && npcManager.attackNpc(worldPos.x, worldPos.y, player, systemMessage)) {
                return true;
            }
            if (npcManager.handleDialogClick(worldPos.x, worldPos.y, player)) {
                return true;
            }
            return npcManager.onClick(worldPos.x, worldPos.y, player);
        } else if (button == 1) { // Right click - show name
            return npcManager.onRightClick(worldPos.x, worldPos.y);
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER) {
            return npcManager.advanceDialog();
        }
        return false;
    }

    /**
     * Convert screen coordinates to world coordinates.
     */
    private Vector2 screenToWorld(int screenX, int screenY) {
        worldCoordsTemp.set(screenX, screenY, 0);
        camera.unproject(worldCoordsTemp);
        return new Vector2(worldCoordsTemp.x, worldCoordsTemp.y);
    }
}
