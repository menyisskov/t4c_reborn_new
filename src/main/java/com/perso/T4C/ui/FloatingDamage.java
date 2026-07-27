package com.perso.T4C.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FloatingDamage {

    private static final float LIFETIME = 1.6f;
    private static final float RISE_SPEED = 40f;
    private static final float WOBBLE_SPEED = 3.5f;
    private static final float WOBBLE_AMP = 6f;
    private static final float FADE_START = 0.5f;
    private static final Color COLOR_DAMAGE_PLAYER  = new Color(1f, 0.22f, 0.22f, 1f);
    private static final Color COLOR_DAMAGE_MONSTER = new Color(1f, 0.85f, 0.1f,  1f);
    private static final Color COLOR_CRIT_MONSTER   = new Color(1f, 0.45f, 0f,    1f);
    private static final Color COLOR_HEAL            = new Color(0.3f, 1f, 0.45f,  1f);
    private static final Color COLOR_MISS            = new Color(0.75f, 0.75f, 0.78f, 1f);
    private static final Color COLOR_MANA            = new Color(0.35f, 0.6f, 1f, 1f);

    public enum Type { PLAYER_RECEIVED, MONSTER_RECEIVED, HEAL, MISS, MANA }

    private static class Entry {
        String text;
        float worldX, worldY;
        float age;
        float wobbleOffset;
        Type type;
        boolean crit;

        Entry(String text, float worldX, float worldY, Type type, boolean crit) {
            this.text = text;
            this.worldX = worldX;
            this.worldY = worldY;
            this.age = 0f;
            this.wobbleOffset = MathUtils.random(0f, MathUtils.PI2);
            this.type = type;
            this.crit = crit;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private final BitmapFont fontNormal;
    private final BitmapFont fontCrit;
    private final GlyphLayout layout = new GlyphLayout();

    public FloatingDamage() {
        fontNormal = FontManager.getInstance().getT4CBeaulieuFont(16, Color.WHITE, 1.2f, Color.BLACK, 0, 0, Color.CLEAR);
        fontCrit   = FontManager.getInstance().getT4CBeaulieuFont(22, Color.WHITE, 1.5f, Color.BLACK, 0, 0, Color.CLEAR);
    }

    public void spawn(int amount, float worldX, float worldY, Type type) {
        spawn(amount, worldX, worldY, type, false);
    }

    public void spawn(int amount, float worldX, float worldY, Type type, boolean crit) {
        String prefix = type == Type.PLAYER_RECEIVED ? "-"
                : type == Type.HEAL || type == Type.MANA ? "+" : "";
        String text = prefix + amount;
        entries.add(new Entry(text, worldX, worldY, type, crit));
    }

    /**
     * Spawns an already-formatted label (miss, dodge, parry...) rather than a number.
     * The caller is responsible for localizing the text.
     */
    public void spawnText(String text, float worldX, float worldY, Type type) {
        if (text == null || text.isEmpty()) {
            return;
        }
        entries.add(new Entry(text, worldX, worldY, type, false));
    }

    public void update(float delta) {
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            e.age += delta;
            if (e.age >= LIFETIME) it.remove();
        }
    }

    public void render(SpriteBatch batch) {
        if (entries.isEmpty()) return;

        for (Entry e : entries) {
            float t = e.age / LIFETIME;
            float alpha = t > (1f - FADE_START) ? (1f - t) / FADE_START : 1f;
            if (alpha <= 0f) continue;

            float riseSpeed = e.crit ? RISE_SPEED * 1.5f : RISE_SPEED;
            float currentY = e.worldY - e.age * riseSpeed;
            float currentX = e.worldX + MathUtils.sin(e.age * WOBBLE_SPEED + e.wobbleOffset) * WOBBLE_AMP;

            Color base = colorForEntry(e);
            BitmapFont font = e.crit ? fontCrit : fontNormal;
            font.setColor(base.r, base.g, base.b, alpha);

            layout.setText(font, e.text);
            font.draw(batch, e.text, currentX - layout.width / 2f, currentY);
        }
    }

    private Color colorForEntry(Entry e) {
        if (e.crit && e.type == Type.MONSTER_RECEIVED) return COLOR_CRIT_MONSTER;
        return switch (e.type) {
            case PLAYER_RECEIVED -> COLOR_DAMAGE_PLAYER;
            case MONSTER_RECEIVED -> COLOR_DAMAGE_MONSTER;
            case HEAL -> COLOR_HEAL;
            case MISS -> COLOR_MISS;
            case MANA -> COLOR_MANA;
        };
    }

    public void dispose() {
        // fonts managed by FontManager
    }
}
