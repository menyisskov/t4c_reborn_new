package com.perso.T4C.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.SystemMessage;

/**
 * Keyboard handler that activates a GM command line when the player types {@code #}.
 * While active, characters accumulate in a buffer; pressing Enter submits the
 * command and Escape cancels it. The buffer is shown live via the system-message
 * overlay so the player can see what they are typing.
 */
public final class GmInputHandler extends InputAdapter {

    private static final char TRIGGER = '#';

    private final Player player;
    private final GmCommandProcessor processor;
    private final StringBuilder buffer = new StringBuilder();
    private boolean active = false;

    public GmInputHandler(Player player, GmCommandProcessor processor) {
        this.player = player;
        this.processor = processor;
    }

    @Override
    public boolean keyTyped(char character) {
        if (!active) {
            if (character == TRIGGER) {
                active = true;
                buffer.setLength(0);
                SystemMessage.showSharedLive("#_");
                return true;
            }
            return false;
        }

        // Active mode: accumulate input.
        if (character == '\r' || character == '\n') {
            // Submit.
            String cmd = buffer.toString().trim();
            active = false;
            buffer.setLength(0);
            if (!cmd.isEmpty()) {
                processor.execute(cmd, player);
            } else {
                SystemMessage.showSharedLive("");
            }
            return true;
        }

        // Backspace is handled in keyDown; ignore the '\b' char here.
        if (character == '\b') {
            return true;
        }

        buffer.append(character);
        SystemMessage.showSharedLive("#" + buffer + "_");
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!active) {
            return false;
        }
        if (keycode == Input.Keys.ESCAPE) {
            active = false;
            buffer.setLength(0);
            SystemMessage.showShared("");
            return true;
        }
        if (keycode == Input.Keys.BACKSPACE && buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
            SystemMessage.showSharedLive("#" + buffer + "_");
            return true;
        }
        // Consume all keys while GM input is active so they don't move the player.
        return true;
    }

    public boolean isActive() {
        return active;
    }
}
