package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MainGameScreenRemoteGuiKeyboardTest {
  @AfterEach
  void closeGui() {
    GuiManager.close();
  }

  @Test
  void openSharedGuiReceivesKeysAndConsumesRemoteShortcuts() {
    var screen = new RecordingScreen();
    GuiManager.open(screen);

    int[] shortcutKeys = {
      Input.Keys.C, Input.Keys.U, Input.Keys.I, Input.Keys.R, Input.Keys.K, Input.Keys.TAB
    };
    for (int key : shortcutKeys) {
      assertTrue(MainGameScreen.routeRemoteGuiKeyDown(key));
    }

    assertEquals(shortcutKeys.length, screen.keyDowns.size());
    for (int i = 0; i < shortcutKeys.length; i++) {
      assertEquals(shortcutKeys[i], screen.keyDowns.get(i));
    }
  }

  @Test
  void openSharedGuiReceivesTypedInputAndConsumesUnhandledCharacters() {
    var screen = new RecordingScreen();
    GuiManager.open(screen);

    assertTrue(MainGameScreen.routeRemoteGuiKeyTyped('x'));
    assertEquals(List.of('x'), screen.typedCharacters);
  }

  @Test
  void closedSharedGuiLeavesRemoteShortcutHandlingAvailable() {
    GuiManager.close();

    assertFalse(MainGameScreen.routeRemoteGuiKeyDown(Input.Keys.C));
    assertFalse(MainGameScreen.routeRemoteGuiKeyTyped('x'));
  }

  private static final class RecordingScreen extends GuiScreenBase {
    private final List<Integer> keyDowns = new ArrayList<>();
    private final List<Character> typedCharacters = new ArrayList<>();

    @Override
    public boolean onKeyDown(int keycode) {
      keyDowns.add(keycode);
      return false;
    }

    @Override
    public boolean onKeyTyped(char character) {
      typedCharacters.add(character);
      return false;
    }
  }
}
