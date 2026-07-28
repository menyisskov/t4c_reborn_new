package com.perso.T4C.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.Inventory;
import com.perso.T4C.gui.screen.SpellBook;
import com.perso.T4C.gui.screen.Statistics;
import com.perso.T4C.gui.screen.QuestScreen;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.player.Player;
import com.perso.T4C.screens.MapRenderer;
import com.perso.T4C.ui.PlayerHUD;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/**
 * Handles keyboard and mouse input and transforms it into player and application actions.
 */
@Getter
public class GameInputHandler {

    private final Logger log;
    private final MapRenderer mapRenderer;
    private final Player player;
    private Supplier<PlayerHUD> hudSupplier;
    private Runnable debugOverlayToggle;
    private Runnable teleportOverlayToggle;
    private Runnable coordsHudToggle;
    private GmInputHandler gmInputHandler;
    private BooleanSupplier textInputActiveSupplier;
    private final Vector2 movementInput = new Vector2();

    public GameInputHandler(Logger log, MapRenderer mapRenderer, Player player) {
        this.log = log;
        this.mapRenderer = mapRenderer;
        this.player = player;
    }

    /**
     * Poll and handle input each frame.
     */
    public void setGmInputHandler(GmInputHandler gmInputHandler) {
        this.gmInputHandler = gmInputHandler;
    }

    public void setTextInputActiveSupplier(BooleanSupplier textInputActiveSupplier) {
        this.textInputActiveSupplier = textInputActiveSupplier;
    }

    public void handleInput(float delta, Player player) {
        int gridDx = 0, gridDy = 0;
        if ((gmInputHandler == null || !gmInputHandler.isActive()) && !isTextInputActive()) {
            boolean left = Gdx.input.isKeyPressed(Input.Keys.A)
                    || Gdx.input.isKeyPressed(Input.Keys.Q)
                    || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            boolean right = Gdx.input.isKeyPressed(Input.Keys.D)
                    || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
            boolean up = Gdx.input.isKeyPressed(Input.Keys.W)
                    || Gdx.input.isKeyPressed(Input.Keys.Z)
                    || Gdx.input.isKeyPressed(Input.Keys.UP);
            boolean down = Gdx.input.isKeyPressed(Input.Keys.S)
                    || Gdx.input.isKeyPressed(Input.Keys.DOWN);
            gridDx = (right ? 1 : 0) - (left ? 1 : 0);
            gridDy = (down ? 1 : 0) - (up ? 1 : 0);
        }

        if (!isTextInputActive()) {
            handleGlobalKeys();
        }
        projectGridDirection(gridDx, gridDy, movementInput);
        player.move(movementInput.x, movementInput.y, delta);
    }

    /**
     * Converts an original T4C eight-way grid direction to render-space pixels.
     * A grid tile is 32x16, so a true (1,1) diagonal has a 2:1 screen slope.
     */
    static Vector2 projectGridDirection(int gridDx, int gridDy, Vector2 out) {
        if (out == null) out = new Vector2();
        int x = Integer.compare(gridDx, 0);
        int y = Integer.compare(gridDy, 0);
        if (x == 0 && y == 0) return out.setZero();
        float tileAspectY = (float) GRID_H / (float) GRID_W;
        // TFCSocket.cpp sends one complete grid step on each active axis.
        // Diagonal movement is therefore intentionally not Euclidean-normalized.
        return out.set(x, y * tileAspectY);
    }

    private boolean isTextInputActive() {
        return textInputActiveSupplier != null && textInputActiveSupplier.getAsBoolean();
    }

    /**
     * Handle application-level global key shortcuts (fullscreen, reload, debug effects).
     */
    public void handleGlobalKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.T) &&
            (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
            if (GuiManager.isOpen()) {
                GuiManager.close();
            } else {
                GuiManager.open(new Statistics(player));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) &&
            (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
            if (GuiManager.isCurrent(SpellBook.class)) {
                GuiManager.close();
            } else {
                PlayerHUD hud = hudSupplier == null ? null : hudSupplier.get();
                GuiManager.open(new SpellBook(player, hud));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I) &&
            (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
            if (GuiManager.isCurrent(Inventory.class)) {
                GuiManager.close();
            } else {
                PlayerHUD hud = hudSupplier == null ? null : hudSupplier.get();
                GuiManager.open(new Inventory(player, hud));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) &&
            (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
            if (GuiManager.isCurrent(QuestScreen.class)) {
                GuiManager.close();
            } else {
                GuiManager.open(new QuestScreen(player));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            if (debugOverlayToggle != null) {
                debugOverlayToggle.run();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            if (teleportOverlayToggle != null) {
                teleportOverlayToggle.run();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            if (coordsHudToggle != null) {
                coordsHudToggle.run();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reloadResources();
        }
    }

    /**
     * Reload ID/mapper resources and notify the map renderer and player animations.
     */
    private void reloadResources() {
        try {
            ModifSprites modifSprites = ModifSprites.empty();
            mapRenderer.reload(modifSprites);
            player.onResourcesReloaded();
            log.info("Reloaded map resources");
        } catch (Exception e) {
            log.error("Error while reloading map resources", e);
        }
    }

    public void setDebugOverlayToggle(Runnable debugOverlayToggle) {
        this.debugOverlayToggle = debugOverlayToggle;
    }

    public void setTeleportOverlayToggle(Runnable teleportOverlayToggle) {
        this.teleportOverlayToggle = teleportOverlayToggle;
    }

    public void setCoordsHudToggle(Runnable coordsHudToggle) {
        this.coordsHudToggle = coordsHudToggle;
    }

    public void setHudSupplier(Supplier<PlayerHUD> hudSupplier) {
        this.hudSupplier = hudSupplier;
    }
}
