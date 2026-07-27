package com.perso.T4C.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.entity.NameRenderer;
import com.perso.T4C.entity.Nameable;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import lombok.Getter;
import lombok.Setter;

import static com.perso.T4C.config.GameConstants.GRID_H;

/**
 * An item lying on the ground, dropped by a monster on death.
 * Rendered as its ground sprite (the inventory sprite without the "64kInv" prefix) at its
 * world position, and picked up by clicking on it. {@link #getItemName()} stays the item
 * class name, so the inventory receives the correct entry on pickup.
 *
 * <p>Gold is never represented here: there is no gold sprite, so gold is granted directly
 * to the player at drop time (see {@link GroundItemManager#spawnFromLoot}).
 */
public class GroundItem {
    private static final float NAME_LABEL_ABOVE_ITEM = 18f;
    private static final float NAME_RENDERER_VERTICAL_OFFSET = 90f;

    @Getter
    private final Vector2 position = new Vector2();
    @Getter
    private final String itemName;
    @Getter
    private final long droppedAtMs;
    @Getter
    private final int remainingCharges;

    private TextureRegion region;
    private boolean regionResolved = false;
    @Getter
    @Setter
    private boolean hovered = false;
    private long nameDisplayUntil = 0L;

    private GroundItem(String itemName, float x, float y, int remainingCharges) {
        this.itemName = itemName;
        this.remainingCharges = remainingCharges;
        this.position.set(x, y);
        this.droppedAtMs = System.currentTimeMillis();
    }

    public static GroundItem ofItem(String itemName, float x, float y) {
        return new GroundItem(itemName, x, y, -1);
    }

    public static GroundItem ofItem(String itemName, float x, float y, int remainingCharges) {
        return new GroundItem(itemName, x, y, remainingCharges);
    }

    /** Depth-sort key, consistent with entities (tile Y). */
    public float getDepthY() {
        return position.y / GRID_H;
    }

    /** Human-readable label for pickup messages. */
    public String describe() {
        ItemDefinition def = ItemDefinition.get(itemName);
        return def != null ? def.getName() : itemName;
    }

    public void showName() {
        nameDisplayUntil = System.currentTimeMillis() + Nameable.NAME_DISPLAY_MS;
    }

    public boolean isNameVisible() {
        return System.currentTimeMillis() < nameDisplayUntil;
    }

    private TextureRegion resolveRegion() {
        if (regionResolved) {
            return region;
        }
        regionResolved = true;
        try {
            SpriteLoader loader = SpriteLoader.getInstance();
            ItemDefinition def = ItemDefinition.get(itemName);
            String inventorySprite = def != null ? def.getAppearanceInventory() : itemName;
            // The ground sprite is the inventory sprite without the "64kInv" prefix
            // (e.g. 64kInvLeatherArmorBody -> LeatherArmorBody).
            String groundSprite = groundSpriteName(inventorySprite);
            if (groundSprite != null && !groundSprite.isEmpty()) {
                region = loader.getRegionFromSpriteName(groundSprite);
            }
            // Fall back to the inventory sprite if the ground variant doesn't exist.
            if (region == null && inventorySprite != null && !inventorySprite.isEmpty()) {
                region = loader.getRegionFromSpriteName(inventorySprite);
            }
        } catch (GameException ignored) {
            region = null;
        }
        return region;
    }

    private static String groundSpriteName(String inventorySprite) {
        if (inventorySprite == null) {
            return null;
        }
        if (inventorySprite.startsWith("64kInv")) {
            return inventorySprite.substring("64kInv".length());
        }
        return inventorySprite;
    }

    /**
     * Pixel hit box half-extents used for mouse picking. Falls back to a tile-sized box
     * when no sprite is available.
     */
    public boolean isMouseOver(float worldX, float worldY) {
        TextureRegion r = resolveRegion();
        float halfW = r != null ? r.getRegionWidth() * 0.5f : 16f;
        float halfH = r != null ? r.getRegionHeight() * 0.5f : 16f;
        if (halfW < 12f) halfW = 12f;
        if (halfH < 12f) halfH = 12f;
        return worldX >= position.x - halfW && worldX <= position.x + halfW
                && worldY >= position.y - halfH && worldY <= position.y + halfH;
    }

    /**
     * Render this ground item in world space. The camera uses a y-down projection
     * (setToOrtho(true,...)), so the region is drawn flipped vertically like inventory icons.
     * When hovered, a yellow outline is applied via the shared outline shader, matching
     * the highlight used for NPCs and monsters.
     */
    public void render(SpriteBatch batch, ShaderProgram outlineShader) {
        TextureRegion r = resolveRegion();
        if (r == null) {
            return;
        }
        float w = r.getRegionWidth();
        float h = r.getRegionHeight();

        boolean outline = hovered && outlineShader != null && outlineShader.isCompiled();
        if (outline) {
            batch.setShader(outlineShader);
            outlineShader.setUniformf("u_texelSize", 1f / w, 1f / h);
            outlineShader.setUniformf("u_outlineColor", 1f, 1f, 0f, 1f); // Yellow outline
        }

        batch.draw(
                r.getTexture(),
                position.x - w * 0.5f, position.y - h * 0.5f,
                w, h,
                r.getRegionX(), r.getRegionY(),
                r.getRegionWidth(), r.getRegionHeight(),
                false, true);

        if (outline) {
            batch.setShader(null);
        }

        if (isNameVisible()) {
            NameRenderer.renderName(batch, I18n.item(describe()),
                    position.x - 15f,
                    position.y + NAME_RENDERER_VERTICAL_OFFSET - NAME_LABEL_ABOVE_ITEM,
                    w, h);
        }
    }
}
