package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.perso.T4C.monster.core.MonsterAnimations;
import com.perso.T4C.npc.core.NPCAnimations;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.PlayerAnimations;
import com.perso.T4C.exception.GameException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HeadlessAnimationMetadataTest {
  @TempDir Path temp;

  @AfterEach
  void clearLoadedSpriteFixture() throws Exception {
    SpriteBinIO.writePayload(temp.resolve("empty_0.bin"), List.of());
    SpriteLoader.getInstance().loadSpriteBin(temp.resolve("empty.bin"));
  }

  @Test
  void originalActorsDiscoverRealFramesAndAdvanceWithoutGraphics() {
    assertNull(Gdx.gl);
    SpriteLoader loader = SpriteLoader.getInstance();
    loader.loadSpriteBin(Path.of("assets/sprites/sprites.bin"));
    assertEquals(13, new PlayerAnimations(BodyPart.BODY, "PupNakedBody")
        .getAnimations().get("PupNakedBody").get("000").size());
    SpriteFrameMetadata body = loader.getFrameMetadata("PupNakedBody000-a");
    assertEquals("PupNakedBody000-a", body.name());
    assertTrue(body.width() > 0 && body.height() > 0);
    assertNull(loader.getRegionCacheById()[loader.getNameToId().get("pupnakedbody000-a")]);

    NPCAnimations npc = new NPCAnimations("TreeEnt");
    assertEquals(9, npc.getAnimations().get("TreeEnt").get("000").size());
    npc.update(1f, true);
    assertTrue(npc.getAnimTimer() > 0f);

    MonsterAnimations monster = new MonsterAnimations("Goblin#l", "GoblinA#i", null, null, null);
    assertEquals(12, monster.getWalkAnimations().get("000").size());
    assertEquals(9, monster.getAttackAnimations().get("000").size());
    assertTrue(monster.getAttackDuration() >= 0.5f);
    monster.startAttack();
    monster.update(monster.getAttackDuration(), false);
    assertFalse(monster.isAttacking());
    assertTrue(monster.isHoldingAttackPose());
    monster.update(1f / 60f, true);
    assertFalse(monster.isHoldingAttackPose());
    assertNull(Gdx.gl);
  }

  @Test
  void pathLoaderKeepsContiguousShardAndDuplicateIdentityPolicy() throws Exception {
    byte[] png = png();
    SpriteBinIO.writePayload(temp.resolve("sprites_0.bin"), List.of(
        new SpriteBinIO.Packed("Duplicate", 1, 1, 1, 2, 3, 4, 1, png),
        new SpriteBinIO.Packed("OnlyFirst", 1, 1, 0, 0, 0, 0, 1, png)));
    SpriteBinIO.writePayload(temp.resolve("sprites_1.bin"), List.of(
        new SpriteBinIO.Packed("duplicate", 9, 9, 5, 6, 7, 8, 1, png)));
    SpriteBinIO.writePayload(temp.resolve("sprites_3.bin"), List.of(
        new SpriteBinIO.Packed("GapSprite", 1, 1, 0, 0, 0, 0, 1, png)));
    SpriteBinIO.writePayload(temp.resolve("sprites.bin"), List.of(
        new SpriteBinIO.Packed("Legacy", 1, 1, 0, 0, 0, 0, 1, png)));
    SpriteLoader loader = SpriteLoader.getInstance();
    int before = loader.getTextureGeneration();
    loader.loadSpriteBin(temp.resolve("sprites.bin"));
    assertEquals(before + 1, loader.getTextureGeneration());
    assertEquals(0, loader.getNameToId().get("duplicate"));
    SpriteFrameMetadata frame = loader.getFrameMetadata("DUPLICATE");
    assertEquals("Duplicate", frame.name());
    assertEquals(1, frame.width());
    assertEquals(5, frame.drawOffset1X());
    assertNull(loader.getFrameMetadata("GapSprite"));
    assertNull(loader.getFrameMetadata("Legacy"));
    assertNull(loader.getRegionCacheById()[0]);
  }

  @Test
  void invalidImageStillFailsDuringAnimationDiscovery() throws Exception {
    SpriteBinIO.writePayload(temp.resolve("sprites_0.bin"), List.of(
        new SpriteBinIO.Packed("Broken000-a", 1, 1, 0, 0, 0, 0, 1, new byte[] {1, 2, 3})));
    SpriteLoader.getInstance().loadSpriteBin(temp.resolve("sprites.bin"));
    assertThrows(GameException.class,
        () -> new MonsterAnimations("Broken#b", null, null, null, null));
  }

  private static byte[] png() throws Exception {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bytes);
    return bytes.toByteArray();
  }
}
