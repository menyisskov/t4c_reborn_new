package com.perso.T4C.gui.core;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;

class GuiLayoutEditingTest {
  @Test
  void macroModifiersCannotMoveOrResizeUiUnlessLayoutEditingWasExplicitlyEnabled() {
    Input previousInput = Gdx.input;
    String previous = System.getProperty("t4c.gui.layoutEditing");
    try {
      Gdx.input =
          (Input)
              Proxy.newProxyInstance(
                  Input.class.getClassLoader(),
                  new Class<?>[] {Input.class},
                  (proxy, method, args) ->
                      method.getName().equals("isKeyPressed")
                          && ((int) args[0] == Input.Keys.CONTROL_LEFT
                              || (int) args[0] == Input.Keys.SHIFT_LEFT));
      System.clearProperty("t4c.gui.layoutEditing");
      var screen = new Screen();
      screen.onTouchDown(12, 12);
      screen.onMouseMove(100, 100);
      assertEquals(1, screen.cell.clicks, "A modified click is still a normal UI click");
      assertEquals(10f, screen.cell.x);
      assertEquals(40f, screen.cell.width);

      System.setProperty("t4c.gui.layoutEditing", "true");
      screen.onTouchDown(12, 12);
      screen.onMouseMove(100, 100);
      assertNotEquals(
          40f, screen.cell.width, "Explicit developer mode still supports layout editing");
      screen.onTouchUp(100, 100);
    } finally {
      Gdx.input = previousInput;
      if (previous == null) System.clearProperty("t4c.gui.layoutEditing");
      else System.setProperty("t4c.gui.layoutEditing", previous);
    }
  }

  private static final class Screen extends GuiScreenBase {
    final Cell cell = new Cell();

    @Override
    protected List<GuiElement> extraElements() {
      return List.of(cell);
    }
  }

  private static final class Cell implements GuiResizable {
    float x = 10, y = 10, width = 40, height = 40;
    int clicks;

    @Override
    public void render(SpriteBatch batch) {}

    @Override
    public boolean contains(float sx, float sy) {
      return sx >= x && sx <= x + width && sy >= y && sy <= y + height;
    }

    @Override
    public void onTouchDown(float sx, float sy) {
      clicks++;
    }

    @Override
    public void setPosition(float x, float y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public float getX() {
      return x;
    }

    @Override
    public float getY() {
      return y;
    }

    @Override
    public float getWidth() {
      return width;
    }

    @Override
    public float getHeight() {
      return height;
    }

    @Override
    public GuiResizable setSize(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
    }
  }
}
