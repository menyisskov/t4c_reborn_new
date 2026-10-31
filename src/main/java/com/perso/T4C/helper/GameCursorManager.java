package com.perso.T4C.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.perso.T4C.MyGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameCursorManager {
  private static final Logger log = LoggerFactory.getLogger(GameCursorManager.class);
  private static final String DEFAULT_CURSOR_SPRITE = "64kInterfaceCursor";
  private static final String ATTACK_CURSOR_SPRITE = "64kCursorAttack";
  private static final String SPELL_CURSOR_PREFIX = "64kCursorSpell-";
  private static final String BOW_CURSOR_PREFIX = "64kCursorBow-";
  private static final String[] TALK_CURSOR_FRAMES = {
    "TalkCursor00", "TalkCursor00", "TalkCursor00", "TalkCursor00",
    "TalkCursor01", "TalkCursor01", "TalkCursor01", "TalkCursor01",
    "TalkCursor02", "TalkCursor02", "TalkCursor02", "TalkCursor02",
    "TalkCursor03", "TalkCursor03", "TalkCursor03", "TalkCursor03"
  };
  private static final float TALK_FRAME_TIME = 0.0875f;
  private static final float SPELL_FRAME_TIME = 0.08f;
  private static final float BOW_FRAME_TIME = 0.08f;
  private Cursor defaultCursor;
  private Cursor attackCursor;
  private final java.util.List<Cursor> talkCursors = new java.util.ArrayList<>();
  private final java.util.List<Cursor> spellCursors = new java.util.ArrayList<>();
  private final java.util.List<Cursor> bowCursors = new java.util.ArrayList<>();
  private final Cursor[] walkCursors = new Cursor[8];
  private int talkIndex = 0;
  private float talkTimer = 0f;
  private int spellIndex = 0;
  private float spellTimer = 0f;
  private int bowIndex = 0;
  private float bowTimer = 0f;

  private enum CursorMode {
    DEFAULT,
    ATTACK,
    TALK,
    SPELL,
    BOW
  }

  private CursorMode mode = CursorMode.DEFAULT;

  public void applyDefaultCursor(MyGame game) {
    if (game == null) {
      return;
    }
    ensureDefaultCursor(game);
    if (defaultCursor != null) {
      Gdx.graphics.setCursor(defaultCursor);
      mode = CursorMode.DEFAULT;
      resetTalkAnimation();
      resetSpellAnimation();
      resetBowAnimation();
    }
  }

  public void applyAttackCursor(MyGame game) {
    if (game == null) {
      return;
    }
    if (mode == CursorMode.ATTACK) {
      return;
    }
    if (attackCursor == null) {
      attackCursor = buildCursorFromSprite(ATTACK_CURSOR_SPRITE);
    }
    if (attackCursor != null) {
      Gdx.graphics.setCursor(attackCursor);
      mode = CursorMode.ATTACK;
      resetTalkAnimation();
      resetSpellAnimation();
      resetBowAnimation();
    }
  }

  public void applyTalkCursor(MyGame game, float delta) {
    if (game == null) {
      return;
    }
    ensureTalkCursors();
    if (talkCursors.isEmpty()) {
      return;
    }
    talkTimer += delta;
    if (talkTimer >= TALK_FRAME_TIME) {
      int steps = (int) (talkTimer / TALK_FRAME_TIME);
      talkTimer -= steps * TALK_FRAME_TIME;
      talkIndex = (talkIndex + steps) % talkCursors.size();
    }
    Cursor current = talkCursors.get(talkIndex);
    if (current != null && mode != CursorMode.TALK) {
      Gdx.graphics.setCursor(current);
      mode = CursorMode.TALK;
      resetSpellAnimation();
    } else if (current != null && mode == CursorMode.TALK) {
      Gdx.graphics.setCursor(current);
    }
  }

  public void applySpellCursor(MyGame game, float delta) {
    if (game == null) {
      return;
    }
    ensureSpellCursors();
    if (spellCursors.isEmpty()) {
      applyDefaultCursor(game);
      return;
    }
    spellTimer += delta;
    if (spellTimer >= SPELL_FRAME_TIME) {
      int steps = (int) (spellTimer / SPELL_FRAME_TIME);
      spellTimer -= steps * SPELL_FRAME_TIME;
      spellIndex = (spellIndex + steps) % spellCursors.size();
    }
    Cursor current = spellCursors.get(spellIndex);
    if (current != null && mode != CursorMode.SPELL) {
      Gdx.graphics.setCursor(current);
      mode = CursorMode.SPELL;
      resetTalkAnimation();
    } else if (current != null && mode == CursorMode.SPELL) {
      Gdx.graphics.setCursor(current);
    }
  }

  public void applyBowCursor(MyGame game, float delta) {
    if (game == null) {
      return;
    }
    ensureBowCursors();
    if (bowCursors.isEmpty()) {
      applyAttackCursor(game);
      return;
    }
    bowTimer += delta;
    if (bowTimer >= BOW_FRAME_TIME) {
      int steps = (int) (bowTimer / BOW_FRAME_TIME);
      bowTimer -= steps * BOW_FRAME_TIME;
      bowIndex = (bowIndex + steps) % bowCursors.size();
    }
    Cursor current = bowCursors.get(bowIndex);
    if (current != null && mode != CursorMode.BOW) {
      Gdx.graphics.setCursor(current);
      mode = CursorMode.BOW;
      resetTalkAnimation();
      resetSpellAnimation();
    } else if (current != null && mode == CursorMode.BOW) {
      Gdx.graphics.setCursor(current);
    }
  }

  /** Shows the walking arrow for steering octant 0-7 (0 = east, counted toward screen-down); the map is drawn at 2:1, so diagonals point along the tile diamond. */
  public void applyWalkCursor(MyGame game, int octant) {
    if (game == null || octant < 0 || octant > 7) return;
    try {
      if (walkCursors[octant] == null) walkCursors[octant] = buildWalkCursor(octant);
      Gdx.graphics.setCursor(walkCursors[octant]);
      mode = CursorMode.DEFAULT;
    } catch (Throwable t) {
      log.warn("Failed to show the walking cursor", t);
    }
  }

  private static Cursor buildWalkCursor(int octant) {
    final int size = 32, mid = size / 2;
    double angle = octant * Math.PI / 4;
    float ux = (float) Math.cos(angle), uy = (float) Math.sin(angle) * 0.5f;
    float length = (float) Math.sqrt(ux * ux + uy * uy);
    ux /= length;
    uy /= length;
    float px = -uy, py = ux;
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setColor(0, 0, 0, 0);
    pixmap.fill();
    float tipX = mid + ux * 14, tipY = mid + uy * 14;
    float tailX = mid - ux * 11, tailY = mid - uy * 11;
    float baseX = tipX - ux * 10, baseY = tipY - uy * 10;
    for (int pass = 0; pass < 2; pass++) {
      float grow = pass == 0 ? 2f : 0f;
      pixmap.setColor(pass == 0 ? 0f : 1f, pass == 0 ? 0f : 0.95f, pass == 0 ? 0f : 0.55f, 1f);
      int thickness = pass == 0 ? 2 : 1;
      for (int t = -thickness; t <= thickness; t++) {
        pixmap.drawLine(
            Math.round(tailX + px * t), Math.round(tailY + py * t),
            Math.round(baseX + px * t), Math.round(baseY + py * t));
      }
      pixmap.fillTriangle(
          Math.round(tipX + ux * grow), Math.round(tipY + uy * grow),
          Math.round(baseX + px * (7 + grow)), Math.round(baseY + py * (7 + grow)),
          Math.round(baseX - px * (7 + grow)), Math.round(baseY - py * (7 + grow)));
    }
    Cursor cursor = Gdx.graphics.newCursor(pixmap, mid, mid);
    pixmap.dispose();
    return cursor;
  }

  private void clearWalkCursors() {
    for (int i = 0; i < walkCursors.length; i++) {
      disposeCursor(walkCursors[i]);
      walkCursors[i] = null;
    }
  }

  public void ensureDefaultCursor(MyGame game) {
    if (defaultCursor != null) {
      return;
    }
    defaultCursor = buildCursorFromSprite(DEFAULT_CURSOR_SPRITE);
    if (game != null) {
      game.customCursor = defaultCursor;
    }
  }

  public void onSpriteReload(MyGame game) {
    disposeCursor(attackCursor);
    attackCursor = null;
    clearTalkCursors();
    clearSpellCursors();
    clearBowCursors();
    clearWalkCursors();
    disposeCursor(defaultCursor);
    defaultCursor = null;
    mode = CursorMode.DEFAULT;
    if (game != null) {
      game.customCursor = null;
    }
  }

  public void dispose(MyGame game) {
    disposeCursor(attackCursor);
    disposeCursor(defaultCursor);
    clearTalkCursors();
    clearSpellCursors();
    clearBowCursors();
    clearWalkCursors();
    attackCursor = null;
    defaultCursor = null;
    mode = CursorMode.DEFAULT;
    if (game != null) {
      game.customCursor = null;
    }
  }

  private Cursor buildCursorFromSprite(String spriteName) {
    try {
      Pixmap pixmap = SpriteLoader.getInstance().createPixmapForSprite(spriteName);
      if (pixmap == null) {
        var cursorFile = Gdx.files.internal("assets/cursors/" + spriteName + ".png");
        if (cursorFile.exists()) {
          pixmap = new Pixmap(cursorFile);
        }
      }
      if (pixmap == null) {
        log.warn("Cursor sprite not found: {}", spriteName);
        return null;
      }
      Pixmap cursorPixmap = ensurePowerOfTwoPixmap(pixmap);
      Cursor cursor = Gdx.graphics.newCursor(cursorPixmap, 0, 0);
      if (cursorPixmap != pixmap) {
        cursorPixmap.dispose();
      }
      pixmap.dispose();
      return cursor;
    } catch (Throwable t) {
      log.warn("Failed to build cursor from sprite {}", spriteName, t);
      return null;
    }
  }

  private Pixmap ensurePowerOfTwoPixmap(Pixmap source) {
    int w = source.getWidth();
    int h = source.getHeight();
    int potW = nextPowerOfTwo(w);
    int potH = nextPowerOfTwo(h);
    if (potW == w && potH == h) {
      return source;
    }
    Pixmap padded = new Pixmap(potW, potH, source.getFormat());
    padded.drawPixmap(source, 0, 0);
    return padded;
  }

  private int nextPowerOfTwo(int value) {
    int v = 1;
    while (v < value) {
      v <<= 1;
    }
    return v;
  }

  private void ensureTalkCursors() {
    if (!talkCursors.isEmpty()) {
      return;
    }
    for (String name : TALK_CURSOR_FRAMES) {
      Cursor cursor = buildCursorFromSprite(name);
      if (cursor == null) {
        clearTalkCursors();
        return;
      }
      talkCursors.add(cursor);
    }
    talkIndex = 0;
    talkTimer = 0f;
  }

  private void ensureSpellCursors() {
    if (!spellCursors.isEmpty()) {
      return;
    }
    for (char c = 'a'; c <= 'l'; c++) {
      Cursor cursor = buildCursorFromSprite(SPELL_CURSOR_PREFIX + c);
      if (cursor == null) {
        break;
      }
      spellCursors.add(cursor);
    }
    spellIndex = 0;
    spellTimer = 0f;
  }

  private void ensureBowCursors() {
    if (!bowCursors.isEmpty()) {
      return;
    }
    for (char c = 'a'; c <= 'k'; c++) {
      Cursor cursor = buildCursorFromSprite(BOW_CURSOR_PREFIX + c);
      if (cursor == null) {
        break;
      }
      bowCursors.add(cursor);
    }
    bowIndex = 0;
    bowTimer = 0f;
  }

  private void clearBowCursors() {
    for (Cursor cursor : bowCursors) {
      disposeCursor(cursor);
    }
    bowCursors.clear();
    resetBowAnimation();
  }

  private void resetBowAnimation() {
    bowIndex = 0;
    bowTimer = 0f;
  }

  private void clearTalkCursors() {
    for (Cursor cursor : talkCursors) {
      disposeCursor(cursor);
    }
    talkCursors.clear();
    resetTalkAnimation();
  }

  private void clearSpellCursors() {
    for (Cursor cursor : spellCursors) {
      disposeCursor(cursor);
    }
    spellCursors.clear();
    resetSpellAnimation();
  }

  private void resetTalkAnimation() {
    talkIndex = 0;
    talkTimer = 0f;
  }

  private void resetSpellAnimation() {
    spellIndex = 0;
    spellTimer = 0f;
  }

  private void disposeCursor(Cursor cursor) {
    if (cursor == null) {
      return;
    }
    try {
      cursor.dispose();
    } catch (Throwable ignored) {
    }
  }
}
