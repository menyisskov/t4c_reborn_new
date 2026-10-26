package com.perso.T4C.monster.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.perso.T4C.exception.GameException;

/** Larger silhouette and matching hit target distinguish a champion from its surrounding brood. */
public class TrainingChampion extends DataMonster {
  public static final float SIZE = 1.6f;
  private final Matrix4 original = new Matrix4();
  private final Matrix4 enlarged = new Matrix4();

  public TrainingChampion(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public void render(SpriteBatch batch, ShaderProgram outline) {
    original.set(batch.getTransformMatrix());
    enlarged
        .set(original)
        .translate(position.x, position.y, 0)
        .scale(SIZE, SIZE, 1)
        .translate(-position.x, -position.y, 0);
    batch.setTransformMatrix(enlarged);
    try {
      super.render(batch, outline);
    } finally {
      batch.setTransformMatrix(original);
    }
  }

  @Override
  public boolean isMouseOver(float x, float y) {
    return super.isMouseOver(
        position.x + (x - position.x) / SIZE, position.y + (y - position.y) / SIZE);
  }
}
