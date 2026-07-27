package com.perso.T4C.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

public class EditorContextMenu extends EditorComponent {
    public static final class Item {
        private final String label;
        private final Runnable action;
        private final List<Item> children = new ArrayList<>();

        public Item(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }

        public Item add(String label, Runnable action) {
            children.add(new Item(label, action));
            return this;
        }

        private boolean hasChildren() {
            return !children.isEmpty();
        }
    }

    private static final float ROW_HEIGHT = 26f;
    private static final float HEADER_HEIGHT = 24f;
    private final String title;
    private final Color accent;
    private final List<Item> items = new ArrayList<>();
    private Runnable closeHandler;
    private int openSubmenuIndex = -1;
    private float submenuX;
    private float submenuY;
    private float submenuWidth;
    private float submenuHeight;

    public EditorContextMenu(String title, Color accent) {
        this.title = title;
        this.accent = accent;
    }

    public EditorContextMenu add(String label, Runnable action) {
        items.add(new Item(label, action));
        return this;
    }

    public Item addSubmenu(String label) {
        Item item = new Item(label, null);
        items.add(item);
        return item;
    }

    public EditorContextMenu onClose(Runnable closeHandler) {
        this.closeHandler = closeHandler;
        return this;
    }

    public void openAt(int screenX, int screenY, float width, float menuBarHeight) {
        float height = HEADER_HEIGHT + items.size() * ROW_HEIGHT;
        float uiY = Gdx.graphics.getHeight() - screenY;
        float x = Math.min(screenX, Gdx.graphics.getWidth() - width - 8f);
        float y = Math.max(8f, Math.min(uiY - height, Gdx.graphics.getHeight() - menuBarHeight - height));
        bounds.set(x, y, width, height);
        visible = true;
        openSubmenuIndex = -1;
    }

    public boolean isOpen() {
        return visible;
    }

    @Override
    public void close() {
        visible = false;
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
        if (!visible) {
            return;
        }
        updateOpenSubmenuFromMouse();
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(EditorTheme.SURFACE);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.setColor(accent);
        shapeRenderer.rect(bounds.x, bounds.y, 3f, bounds.height);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(EditorTheme.BORDER);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.end();

        batch.begin();
        font.setColor(EditorTheme.TEXT_MUTED);
        font.draw(batch, title, bounds.x + 12f, bounds.y + bounds.height - 9f);
        font.setColor(EditorTheme.TEXT);
        float y = bounds.y + bounds.height - HEADER_HEIGHT - 10f;
        for (Item item : items) {
            font.draw(batch, item.label, bounds.x + 14f, y);
            if (item.hasChildren()) {
                font.draw(batch, ">", bounds.x + bounds.width - 18f, y);
            }
            y -= ROW_HEIGHT;
        }
        batch.end();

        renderSubmenu(batch, shapeRenderer, font);
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    private void renderSubmenu(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
        if (openSubmenuIndex < 0 || openSubmenuIndex >= items.size()) {
            return;
        }
        Item parent = items.get(openSubmenuIndex);
        if (!parent.hasChildren()) {
            return;
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(EditorTheme.SURFACE);
        shapeRenderer.rect(submenuX, submenuY, submenuWidth, submenuHeight);
        shapeRenderer.setColor(accent);
        shapeRenderer.rect(submenuX, submenuY, 3f, submenuHeight);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(EditorTheme.BORDER);
        shapeRenderer.rect(submenuX, submenuY, submenuWidth, submenuHeight);
        shapeRenderer.end();

        batch.begin();
        font.setColor(EditorTheme.TEXT);
        float y = submenuY + submenuHeight - 10f;
        for (Item child : parent.children) {
            font.draw(batch, child.label, submenuX + 14f, y);
            y -= ROW_HEIGHT;
        }
        batch.end();
    }

    private void updateOpenSubmenuFromMouse() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        int row = rowAt(mouseX, mouseY);
        if (row >= 0 && row < items.size() && items.get(row).hasChildren()) {
            openSubmenuIndex = row;
            layoutSubmenu(items.get(row));
        }
    }

    private void layoutSubmenu(Item parent) {
        submenuWidth = Math.max(92f, preferredWidth(parent.children));
        submenuHeight = parent.children.size() * ROW_HEIGHT;
        float rowTop = bounds.y + bounds.height - HEADER_HEIGHT - openSubmenuIndex * ROW_HEIGHT;
        submenuX = Math.min(bounds.x + bounds.width - 1f, Gdx.graphics.getWidth() - submenuWidth - 8f);
        submenuY = Math.max(8f, Math.min(rowTop - submenuHeight, Gdx.graphics.getHeight() - submenuHeight - 8f));
    }

    private float preferredWidth(List<Item> values) {
        float width = 0f;
        for (Item item : values) {
            width = Math.max(width, item.label.length() * 8f + 32f);
        }
        return width;
    }

    private int rowAt(int screenX, int uiY) {
        if (!bounds.contains(screenX, uiY)) {
            return -1;
        }
        return (int) ((bounds.y + bounds.height - HEADER_HEIGHT - uiY) / ROW_HEIGHT);
    }

    private int submenuRowAt(int screenX, int uiY) {
        if (openSubmenuIndex < 0 || screenX < submenuX || screenX > submenuX + submenuWidth
                || uiY < submenuY || uiY > submenuY + submenuHeight) {
            return -1;
        }
        return (int) ((submenuY + submenuHeight - uiY) / ROW_HEIGHT);
    }

    @Override
    public boolean handleClick(int screenX, int screenY, int button) {
        if (!visible) {
            return false;
        }
        if (button != Input.Buttons.LEFT) {
            close();
            return true;
        }
        int y = Gdx.graphics.getHeight() - screenY;
        int submenuRow = submenuRowAt(screenX, y);
        if (submenuRow >= 0 && openSubmenuIndex >= 0 && openSubmenuIndex < items.size()) {
            Item parent = items.get(openSubmenuIndex);
            if (submenuRow < parent.children.size()) {
                Runnable action = parent.children.get(submenuRow).action;
                close();
                if (action != null) {
                    action.run();
                }
                return true;
            }
        }
        if (!bounds.contains(screenX, y)) {
            close();
            return true;
        }
        int row = (int) ((bounds.y + bounds.height - HEADER_HEIGHT - y) / ROW_HEIGHT);
        if (row >= 0 && row < items.size()) {
            if (items.get(row).hasChildren()) {
                openSubmenuIndex = row;
                layoutSubmenu(items.get(row));
                return true;
            }
            Runnable action = items.get(row).action;
            close();
            if (action != null) {
                action.run();
            }
        }
        return true;
    }
}
