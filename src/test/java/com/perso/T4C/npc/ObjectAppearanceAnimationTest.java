package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.npc.core.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ObjectAppearanceAnimationTest {
  private static String staticFrameName(String base, int frameIndex) throws Exception {
    Method method =
        NPCAnimations.class.getDeclaredMethod("staticFrameName", String.class, int.class);
    method.setAccessible(true);
    return (String) method.invoke(null, base, frameIndex);
  }

  private static boolean hasFrameSuffix(String spriteName) throws Exception {
    Method method = NPCAnimations.class.getDeclaredMethod("hasFrameSuffix", String.class);
    method.setAccessible(true);
    return (boolean) method.invoke(null, spriteName);
  }

  @Test
  void objectFramesResolveToTheirBareSpriteName() throws Exception {
    assertEquals("SimplePortal-a", staticFrameName("@static:SimplePortal-a", 0));
    assertEquals("SimplePortal-c", staticFrameName("@static:SimplePortal-a", 2));
    assertNotEquals("@static:SimplePortal-a000-a", staticFrameName("@static:SimplePortal-a", 0));
  }

  @Test
  void singleSpriteObjectsHaveNoFrameFamily() throws Exception {
    assertFalse(hasFrameSuffix("Chest"));
    assertFalse(hasFrameSuffix("Vault"));
    assertFalse(hasFrameSuffix("RockDoor1"));
    assertTrue(hasFrameSuffix("SimplePortal-a"));
    assertEquals("Chest", staticFrameName("@static:Chest", 0));
    assertEquals("Chest", staticFrameName("@static:Chest", 5));
    assertEquals("RockDoor1", staticFrameName("@static:RockDoor1", 3));
  }

  @Test
  void everyPortalFrameExistsInTheSpritePack() throws Exception {
    Set<String> names = new HashSet<>();
    for (SpriteBinIO.Packed sprite :
        SpriteBinIO.readAllToList(Path.of("assets/sprites"), "sprites")) {
      names.add(sprite.name());
    }
    int frames = 0;
    for (int i = 0; i < 26; i++) {
      String name = staticFrameName("@static:SimplePortal-a", i);
      if (!names.contains(name)) break;
      frames++;
    }
    assertEquals(26, frames, "the portal animation is twenty-six frames in the original client");
  }

  @Test
  void thePortalSpriteIsTallerThanTheHumanoidHitbox() throws Exception {
    SpriteBinIO.Packed frame =
        SpriteBinIO.readAllToList(Path.of("assets/sprites"), "sprites").stream()
            .filter(sprite -> "SimplePortal-a".equals(sprite.name()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("SimplePortal-a must exist"));
    assertEquals(64, frame.width());
    assertEquals(119, frame.height());
    assertTrue(
        frame.off1Y() < -60,
        "the portal is drawn well above its anchor, found off1Y=" + frame.off1Y());
    float humanoidTop = 60f;
    assertTrue(
        -frame.off1Y() > humanoidTop,
        "the portal reaches above the humanoid hitbox, so it needs its own bounds");
  }

  @Test
  void theDialogAnchorIsStableAcrossTheAnimation() throws Exception {
    List<SpriteBinIO.Packed> frames =
        SpriteBinIO.readAllToList(Path.of("assets/sprites"), "sprites").stream()
            .filter(sprite -> sprite.name().startsWith("SimplePortal-"))
            .toList();
    assertEquals(26, frames.size());
    Set<Integer> perFrameTops = new HashSet<>();
    int stableTop = Integer.MIN_VALUE;
    int stableBottom = Integer.MAX_VALUE;
    for (SpriteBinIO.Packed frame : frames) {
      int top = frame.off1Y() + frame.height();
      perFrameTops.add(top);
      stableTop = Math.max(stableTop, top);
      stableBottom = Math.min(stableBottom, frame.off1Y());
    }
    assertTrue(
        perFrameTops.size() > 1,
        "this guard is only meaningful because the frames differ in height");
    assertEquals(20, stableTop);
    assertEquals(-101, stableBottom);
    for (SpriteBinIO.Packed frame : frames) {
      assertTrue(
          stableTop >= frame.off1Y() + frame.height(), "stable top must cover " + frame.name());
      assertTrue(stableBottom <= frame.off1Y(), "stable bottom must cover " + frame.name());
    }
  }

  @Test
  void dialogTextAndKeywordBoundsShareTheSameAnchor() throws Exception {
    String source =
        java.nio.file.Files.readString(
            Path.of("src/main/java/com/perso/T4C/npc/core/NPCAnimations.java"));
    String dialogText =
        methodBody(
            source,
            "List<String> keywords)");
    String wordBounds = methodBody(source, "getDialogWordBounds(");
    assertTrue(
        dialogText.contains("calculateStableBounds"),
        "renderDialogText must anchor on the stable bounds");
    assertTrue(
        wordBounds.contains("calculateStableBounds"),
        "getDialogWordBounds must anchor on the same stable bounds");
    assertFalse(
        dialogText.contains("calculateBounds(pos"),
        "renderDialogText must not follow the animated frame");
    assertFalse(
        wordBounds.contains("calculateBounds(pos"),
        "getDialogWordBounds must not follow the animated frame");
  }

  private static String methodBody(String source, String signatureStart) {
    int start = source.indexOf(signatureStart);
    assertTrue(start >= 0, "method not found: " + signatureStart);
    int open = source.indexOf('{', start);
    int depth = 0;
    for (int i = open; i < source.length(); i++) {
      char c = source.charAt(i);
      if (c == '{') depth++;
      else if (c == '}' && --depth == 0) return source.substring(open, i + 1);
    }
    throw new AssertionError("unbalanced braces after " + signatureStart);
  }

  @Test
  void onlyObjectAppearancesAreTreatedAsScenery() throws Exception {
    Method method = NPCAnimations.class.getDeclaredMethod("isStaticBase", String.class);
    method.setAccessible(true);
    for (String base : List.of("@static:SimplePortal-a", "@static:Chest")) {
      assertTrue((boolean) method.invoke(null, base), base + " is scenery");
    }
    for (String base : List.of("Orc", "Demon", "Puppet", "@invisible")) {
      assertFalse((boolean) method.invoke(null, base), base + " is not scenery");
    }
  }
}
