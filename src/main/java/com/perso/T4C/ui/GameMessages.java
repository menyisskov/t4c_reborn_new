package com.perso.T4C.ui;
import com.perso.T4C.i18n.I18n;
/**
 * Class representing GameMessages.
 */

public final class GameMessages {
    private GameMessages() {
    }

    public static void showActionPrevented(SystemMessage systemMessage) {
        if (systemMessage != null) {
            systemMessage.show(I18n.message("message.action_prevented"));
        }
    }
}
