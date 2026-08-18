package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class SystemMessage {
  public static final Color MESSAGE_COLOR = Color.valueOf("4AA3FF");
  private static final float MESSAGE_DURATION = 3.0f;
  private static final int FONT_SIZE = 20;
  private static final float TOP_MARGIN = 50f;
  private static final float MAX_WIDTH_RATIO = 0.8f;
  private static final int MAX_QUEUED = 20;
  private final BitmapFont font;
  private final Deque<String> queue = new ArrayDeque<>();
  private String currentMessage = null;
  private float messageTimer = 0f;
  private static final float OUTLINE_WIDTH = 1.2f;
  private static final Color OUTLINE_COLOR = Color.BLACK;
  private static SystemMessage shared;
  private static Consumer<String> chatSink;

  public static void setShared(SystemMessage instance) {
    shared = instance;
  }

  public static void setChatSink(Consumer<String> sink) {
    chatSink = sink;
  }

  public static void showShared(String message) {
    if (shared != null) {
      shared.show(message);
    }
  }

  public static void showSharedLive(String message) {
    if (shared != null) {
      shared.showLive(message);
    }
  }

  public SystemMessage() {
    this.font =
        FontManager.getInstance()
            .getT4CBeaulieuFont(
                FONT_SIZE, MESSAGE_COLOR, OUTLINE_WIDTH, OUTLINE_COLOR, 0, 0, Color.CLEAR);
  }

  public void show(String message) {
    if (message == null) {
      return;
    }
    if (chatSink != null) {
      chatSink.accept(message);
      return;
    }
    if (currentMessage == null) {
      this.currentMessage = message;
      this.messageTimer = MESSAGE_DURATION;
      return;
    }
    if (queue.size() >= MAX_QUEUED) {
      queue.removeFirst();
    }
    queue.addLast(message);
  }

  public void showLive(String message) {
    if (message == null) {
      return;
    }
    queue.clear();
    if (message.isEmpty()) {
      currentMessage = null;
      messageTimer = 0f;
      return;
    }
    currentMessage = message;
    messageTimer = MESSAGE_DURATION;
  }

  public void update(float delta) {
    if (messageTimer > 0f) {
      messageTimer -= delta;
      if (messageTimer <= 0f) {
        currentMessage = queue.pollFirst();
        messageTimer = currentMessage != null ? MESSAGE_DURATION : 0f;
      }
    }
  }

  public void render(SpriteBatch batch) {
    if (currentMessage == null || messageTimer <= 0f) {
      return;
    }
    float screenWidth = Gdx.graphics.getWidth();
    float wrapWidth = Math.max(1f, screenWidth * MAX_WIDTH_RATIO);
    if (messageTimer < 0.5f) {
      float alpha = messageTimer / 0.5f;
      font.setColor(MESSAGE_COLOR.r, MESSAGE_COLOR.g, MESSAGE_COLOR.b, alpha);
    } else {
      font.setColor(MESSAGE_COLOR);
    }
    GlyphLayout layout =
        new GlyphLayout(font, currentMessage, font.getColor(), wrapWidth, Align.center, true);
    float x = (screenWidth - wrapWidth) / 2f;
    float y = TOP_MARGIN + layout.height;
    batch.begin();
    font.draw(batch, layout, x, y);
    batch.end();
  }

  public void dispose() {}
}
