package com.perso.T4C.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.npc.NPCManager;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.SystemMessage;

/**
 * Captures normal text while the player has an active NPC conversation.
 * This mirrors the legacy directed-talk flow: typing addresses the selected
 * NPC, Enter sends the line, and Escape cancels input or ends the conversation.
 */
public final class NPCConversationInputHandler extends InputAdapter {

    private static final int MAX_TEXT_LENGTH = 255;

    private final NPCManager npcManager;
    private final Player player;
    private final StringBuilder buffer = new StringBuilder();
    private boolean active;

    public NPCConversationInputHandler(NPCManager npcManager, Player player) {
        this.npcManager = npcManager;
        this.player = player;
    }

    @Override
    public boolean keyTyped(char character) {
        if (GuiManager.isOpen()) {
            return false;
        }
        if (!npcManager.hasActiveConversation()) {
            cancelInput();
            return false;
        }
        if (character == '\r' || character == '\n' || character == '\b') {
            return active;
        }
        if (Character.isISOControl(character)) {
            return active;
        }
        if (!active) {
            active = true;
            buffer.setLength(0);
        }
        if (buffer.length() < MAX_TEXT_LENGTH) {
            buffer.append(character);
            showBuffer();
        }
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (GuiManager.isOpen()) {
            return false;
        }
        if (!npcManager.hasActiveConversation()) {
            cancelInput();
            return false;
        }
        if (keycode == Input.Keys.ESCAPE) {
            if (active) {
                cancelInput();
            } else {
                npcManager.endActiveConversation();
            }
            return true;
        }
        if (!active) {
            return false;
        }
        if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
            String text = buffer.toString().trim();
            active = false;
            buffer.setLength(0);
            SystemMessage.showSharedLive("");
            if (!text.isEmpty()) {
                // PacketHandling::IndirectTalk calls MainObject::SetTalkText
                // for the local player's directed speech as well.
                player.showTalkText(text);
                npcManager.talkToActiveNpc(text, player);
            }
            return true;
        }
        if (keycode == Input.Keys.BACKSPACE) {
            if (buffer.length() > 0) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
            showBuffer();
            return true;
        }
        return true;
    }

    public boolean isActive() {
        if (active && !npcManager.hasActiveConversation()) {
            cancelInput();
        }
        return active;
    }

    private void showBuffer() {
        SystemMessage.showSharedLive("> " + buffer + "_");
    }

    private void cancelInput() {
        if (active) {
            SystemMessage.showSharedLive("");
        }
        active = false;
        buffer.setLength(0);
    }
}
