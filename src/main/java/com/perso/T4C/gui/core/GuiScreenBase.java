package com.perso.T4C.gui.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiInventory;
import com.perso.T4C.gui.widget.GuiPlayerPart;
import com.perso.T4C.gui.widget.GuiPlayerPreview;
import com.perso.T4C.gui.widget.GuiText;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class GuiScreenBase {
  protected TextureRegion background;
  protected float x;
  protected float y;
  protected final List<GuiButton> buttons = new ArrayList<>();
  protected final List<GuiText> labels = new ArrayList<>();
  protected final List<GuiAnimatedSprite> animatedSprites = new ArrayList<>();
  protected final List<GuiPlayerPreview> previews = new ArrayList<>();
  protected final List<GuiPlayerPart> playerParts = new ArrayList<>();
  protected final List<GuiInventory> inventories = new ArrayList<>();
  private GuiElement draggedElement;
  private final GuiBoxedInteraction boxedInteraction = new GuiBoxedInteraction();
  private GuiResizable resizedElement;
  private float dragOffsetX;
  private float dragOffsetY;
  private boolean resizeLeft;
  private boolean resizeRight;
  private boolean resizeTop;
  private boolean resizeBottom;
  private float resizeFixedRight;
  private float resizeFixedBottom;
  private static final float MIN_BOX_SIZE = 8f;

  protected void centerOnScreen() {
    if (background == null) {
      return;
    }
    float w = background.getRegionWidth();
    float h = background.getRegionHeight();
    x = (Gdx.graphics.getWidth() - w) / 2f;
    y = (Gdx.graphics.getHeight() - h) / 2f;
  }

  public void render(SpriteBatch batch) {
    if (background != null) {
      GuiDraw.drawOverlayRegionFlipped(batch, background, x, y);
    }
    for (GuiElement element : orderedElements()) {
      element.render(batch);
    }
  }

  public void dispose() {}

  protected void addCloseButton(float dx, float dy) {
    TextureRegion normal = GuiSprites.load("GUI_X_ButtonDown");
    TextureRegion hover = GuiSprites.load("GUI_X_ButtonHUp");
    TextureRegion pressed = GuiSprites.load("GUI_X_ButtonUp");
    if (background == null || normal == null || hover == null || pressed == null) {
      return;
    }
    buttons.add(new GuiButton(normal, hover, pressed, x + dx, y + dy, GuiManager::close));
  }

  public void onTouchDown(float screenX, float screenY) {
    if (boxedInteraction.touchDown(orderedElements(), screenX, screenY)) {
      return;
    }
    if (isResizeModifierDown()) {
      GuiElement candidate = findTopmostElement(screenX, screenY);
      if (candidate instanceof GuiResizable resizable) {
        beginResizeFromNearestEdge(resizable, screenX, screenY);
        return;
      }
    }
    if (isDragModifierDown()) {
      GuiElement candidate = findTopmostElement(screenX, screenY);
      if (candidate != null) {
        draggedElement = candidate;
        dragOffsetX = screenX - candidate.getX();
        dragOffsetY = screenY - candidate.getY();
        return;
      }
    }
    for (GuiElement element : new ArrayList<>(orderedElements())) {
      element.onTouchDown(screenX, screenY);
    }
  }

  public void onTouchDown(float screenX, float screenY, int button) {
    onTouchDown(screenX, screenY);
  }

  public void onTouchUp(float screenX, float screenY) {
    if (releaseDraggedElement()) {
      return;
    }
    for (GuiElement element : new ArrayList<>(orderedElements())) {
      element.onTouchUp(screenX, screenY);
    }
  }

  public void onMouseMove(float screenX, float screenY) {
    if (boxedInteraction.dragged(screenX, screenY)) {
      return;
    }
    if (resizedElement != null) {
      resizeTo(screenX, screenY);
      return;
    }
    if (draggedElement != null) {
      draggedElement.setPosition(screenX - dragOffsetX, screenY - dragOffsetY);
      return;
    }
    for (GuiElement element : orderedElements()) {
      element.onMouseMove(screenX, screenY);
    }
  }

  protected List<GuiElement> orderedElements() {
    List<GuiElement> elements = new ArrayList<>();
    elements.addAll(animatedSprites);
    elements.addAll(previews);
    elements.addAll(playerParts);
    elements.addAll(inventories);
    elements.addAll(labels);
    elements.addAll(buttons);
    elements.addAll(extraElements());
    return elements;
  }

  protected List<GuiElement> extraElements() {
    return List.of();
  }

  public boolean isPointerOver(float screenX, float screenY) {
    if (isBackgroundHit(screenX, screenY)) {
      return true;
    }
    for (GuiElement element : orderedElements()) {
      if (element.contains(screenX, screenY)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isBackgroundHit(float screenX, float screenY) {
    return background != null
        && screenX >= x
        && screenX <= x + background.getRegionWidth()
        && screenY >= y
        && screenY <= y + background.getRegionHeight();
  }

  private GuiElement findTopmostElement(float screenX, float screenY) {
    List<GuiElement> elements = orderedElements();
    for (int i = elements.size() - 1; i >= 0; i--) {
      GuiElement element = elements.get(i);
      if (element.contains(screenX, screenY)) {
        return element;
      }
    }
    return null;
  }

  private boolean isDragModifierDown() {
    return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
  }

  private boolean isResizeModifierDown() {
    return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
  }

  protected boolean releaseDraggedElement() {
    if (boxedInteraction.touchUp()) {
      return true;
    }
    if (resizedElement != null) {
      try {
        Gdx.app.log(
            "GuiScreen",
            String.format(
                Locale.ROOT,
                "Element resized (window-relative x + %.1f, y + %.1f; screen x %.1f, y %.1f), %.1f x %.1f",
                resizedElement.getX() - x,
                resizedElement.getY() - y,
                resizedElement.getX(),
                resizedElement.getY(),
                resizedElement.getWidth(),
                resizedElement.getHeight()));
      } catch (Throwable ignored) {
      }
      resizedElement = null;
      clearResizeEdges();
      return true;
    }
    if (draggedElement == null) {
      return false;
    }
    try {
      float baseX = x;
      float baseY = y;
      float offsetX = draggedElement.getX() - baseX;
      float offsetY = draggedElement.getY() - baseY;
      Gdx.app.log(
          "GuiScreen",
          String.format(
              Locale.ROOT,
              "Element moved (window-relative x + %.1f, y + %.1f; screen x %.1f, y %.1f)",
              offsetX,
              offsetY,
              draggedElement.getX(),
              draggedElement.getY()));
    } catch (Throwable ignored) {
    }
    draggedElement = null;
    return true;
  }

  private void beginResizeFromNearestEdge(GuiResizable element, float screenX, float screenY) {
    float left = element.getX();
    float top = element.getY();
    float right = left + element.getWidth();
    float bottom = top + element.getHeight();
    float leftDistance = Math.abs(screenX - left);
    float rightDistance = Math.abs(right - screenX);
    float topDistance = Math.abs(screenY - top);
    float bottomDistance = Math.abs(bottom - screenY);
    float nearest =
        Math.min(Math.min(leftDistance, rightDistance), Math.min(topDistance, bottomDistance));
    clearResizeEdges();
    if (nearest == leftDistance) {
      resizeLeft = true;
    } else if (nearest == rightDistance) {
      resizeRight = true;
    } else if (nearest == topDistance) {
      resizeTop = true;
    } else {
      resizeBottom = true;
    }
    resizedElement = element;
    resizeFixedRight = right;
    resizeFixedBottom = bottom;
  }

  private void resizeTo(float screenX, float screenY) {
    float newX = resizedElement.getX();
    float newY = resizedElement.getY();
    float newWidth = resizedElement.getWidth();
    float newHeight = resizedElement.getHeight();
    if (resizeLeft) {
      newX = Math.min(screenX, resizeFixedRight - MIN_BOX_SIZE);
      newWidth = resizeFixedRight - newX;
    } else if (resizeRight) {
      newWidth = Math.max(MIN_BOX_SIZE, screenX - newX);
    }
    if (resizeTop) {
      newY = Math.min(screenY, resizeFixedBottom - MIN_BOX_SIZE);
      newHeight = resizeFixedBottom - newY;
    } else if (resizeBottom) {
      newHeight = Math.max(MIN_BOX_SIZE, screenY - newY);
    }
    resizedElement.setPosition(newX, newY);
    resizedElement.setSize(newWidth, newHeight);
  }

  private void clearResizeEdges() {
    resizeLeft = false;
    resizeRight = false;
    resizeTop = false;
    resizeBottom = false;
  }

  public void onScroll(float amountY, float screenX, float screenY) {
    for (GuiInventory inventory : inventories) {
      if (inventory.contains(screenX, screenY)) {
        inventory.onScroll(amountY);
        return;
      }
    }
  }

  public boolean onKeyDown(int keycode) {
    return false;
  }

  public boolean onKeyTyped(char character) {
    return false;
  }
}
