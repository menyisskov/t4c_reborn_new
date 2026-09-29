package com.perso.T4C.editor.build;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.MyGame;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.screens.MapEditorScreen;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;

/**
 * Optional real-input regression check. Requires OpenGL; run manually from the repository root.
 * Never saves map data. Restores palette preferences after the hidden application exits.
 */
public final class BuildWorkspaceInputSmoke {
  private static Object field(Object object, String name) throws Exception {
    var field = object.getClass().getDeclaredField(name);
    field.setAccessible(true);
    return field.get(object);
  }

  private static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  public static void main(String[] args) throws Exception {
    Path preferences = Path.of("editor_build_preferences.json");
    byte[] previous = Files.exists(preferences) ? Files.readAllBytes(preferences) : null;
    var config = new Lwjgl3ApplicationConfiguration();
    config.setWindowedMode(1280, 768);
    config.setInitialVisible(false);
    config.disableAudio(true);
    config.setForegroundFPS(30);
    try {
      new Lwjgl3Application(
          new ApplicationAdapter() {
            MapEditorScreen screen;
            int frames, ready;
            MapStamp before, after;

            private MapStamp snapshot() throws Exception {
              return MapStamp.capture(
                  (MapReader) field(screen, "mapReader"),
                  (byte[]) field(screen, "collisionData"),
                  "smoke",
                  "custom",
                  "memory",
                  4870,
                  1960,
                  60,
                  90,
                  null);
            }

            private long changed(MapStamp a, MapStamp b) {
              long count = 0;
              for (int i = 0; i < a.cells().size(); i++)
                if (!a.cells().get(i).equals(b.cells().get(i))) count++;
              return count;
            }

            private int history() throws Exception {
              return ((Deque<?>) field(screen, "undoStack")).size();
            }

            private void replay(String name) throws Exception {
              var method = screen.getClass().getDeclaredMethod(name);
              method.setAccessible(true);
              method.invoke(screen);
            }

            public void create() {
              try {
                screen = new MapEditorScreen(new MyGame());
                screen.resize(1280, 768);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }

            public void render() {
              try {
                screen.render(1f / 30);
                check(++frames < 600, "Editor failed to load");
                if ((boolean) field(screen, "isLoading")) return;
                ready++;
                var input = Gdx.input.getInputProcessor();
                if (ready == 2) {
                  ((Vector2) field(screen, "cameraPosition")).set(4900 * 32, 2000 * 16);
                  // An empty ocean region is our in-memory fixture. Allow moving walls here.
                  var map = (MapReader) field(screen, "mapReader");
                  replay("loadCollisionData");
                  var collision = (byte[]) field(screen, "collisionData");
                  for (int y = 1960; y < 2050; y++)
                    java.util.Arrays.fill(
                        collision, y * map.getWidth() + 4870, y * map.getWidth() + 4930, (byte) 0);
                  input.touchDown(970, 140, 0, 0);
                  for (char c : "BrickWall 1".toCharArray()) input.keyTyped(c);
                }
                if (ready == 4) {
                  // First canvas sample starts the stroke, not a trail from under the asset
                  // sidebar.
                  input.touchDown(970, 350, 0, 0);
                  input.touchDragged(897, 300, 0);
                  before = snapshot();
                  input.touchDragged(500, 300, 0);
                  input.touchUp(500, 300, 0, 0);
                  after = snapshot();
                  check(changed(before, after) > 2, "Palette drag placed only one sprite");
                  check(history() == 1, "Palette stroke must be one history entry");
                  replay("performUndo");
                  check(before.equals(snapshot()), "Undo did not restore the full stroke");
                  replay("performRedo");
                  check(after.equals(snapshot()), "Redo did not restore the full stroke");
                  System.out.println("PALETTE_REPEAT_AND_ATOMIC_UNDO_OK");
                }
                if (ready == 5) {
                  before = snapshot();
                  input.touchDown(220, 450, 0, 0);
                  input.touchDragged(500, 450, 0);
                  input.touchUp(500, 450, 0, 0);
                  check(
                      changed(before, snapshot()) > 2, "Selected sprite did not default to Paint");
                  check(history() == 2, "Canvas stroke must be one entry");
                  System.out.println("DEFAULT_CANVAS_REPEAT_OK");
                }
                if (ready == 6) {
                  ((BuildWorkspace) field(screen, "buildWorkspace"))
                      .selectTool(BuildWorkspace.Tool.STAMP);
                  before = snapshot();
                  input.touchDown(220, 550, 0, 0);
                  input.touchDragged(500, 550, 0);
                  input.touchUp(500, 550, 0, 0);
                  check(changed(before, snapshot()) == 1, "Explicit Stamp must place one sprite");
                  System.out.println("EXPLICIT_SINGLE_STAMP_OK");
                }
                if (ready == 7) {
                  before = snapshot();
                  input.touchDown(970, 350, 0, 0);
                  input.touchUp(100, 650, 0, 0);
                  check(
                      changed(before, snapshot()) == 1, "Quick palette drop must place one sprite");
                  System.out.println("QUICK_DROP_OK");
                }
                if (ready == 8) {
                  before = snapshot();
                  int history = history();
                  input.touchDown(970, 350, 0, 0);
                  input.touchDragged(200, 620, 0);
                  input.touchDragged(500, 620, 0);
                  input.touchUp(970, 350, 0, 0);
                  check(before.equals(snapshot()), "Drop over sidebar must cancel the stroke");
                  check(history == history(), "Canceled stroke changed history");
                  System.out.println("SIDEBAR_CANCEL_OK");
                }
                if (ready == 9) {
                  // Use the visible header button, then box-select the second painted row.
                  input.touchDown(1000, 75, 0, 0);
                  input.touchUp(1000, 75, 0, 0);
                  input.touchDown(190, 430, 0, 0);
                  input.touchDragged(530, 475, 0);
                  input.touchUp(530, 475, 0, 0);
                  var workspace = (BuildWorkspace) field(screen, "buildWorkspace");
                  check(workspace.multiSelecting(), "Multi-select button did not activate");
                  check(
                      ((java.util.Set<?>) field(workspace, "selection")).size() > 2,
                      "Box did not select multiple objects");
                  var selected = (java.util.Set<?>) field(workspace, "selection");
                  int selectedCount = selected.size();
                  Input nativeInput = Gdx.input;
                  Gdx.input =
                      (Input)
                          java.lang.reflect.Proxy.newProxyInstance(
                              Input.class.getClassLoader(),
                              new Class<?>[] {Input.class},
                              (proxy, method, arguments) ->
                                  method.getName().equals("isKeyPressed")
                                          && (int) arguments[0] == Input.Keys.SHIFT_LEFT
                                      ? true
                                      : method.invoke(nativeInput, arguments));
                  try {
                    input.touchDown(300, 450, 0, 0);
                    input.touchUp(300, 450, 0, 0);
                    check(
                        selected.size() == selectedCount - 1,
                        "Shift-click did not remove one object");
                    input.touchDown(300, 450, 0, 0);
                    input.touchUp(300, 450, 0, 0);
                    check(selected.size() == selectedCount, "Shift-click did not add one object");
                    input.touchDown(100, 280, 0, 0);
                    input.touchDragged(880, 320, 0);
                    input.touchUp(880, 320, 0, 0);
                    check(selected.size() > selectedCount, "Shift-box did not add objects");
                  } finally {
                    Gdx.input = nativeInput;
                  }
                  // Reselect just the second row for the movement checks.
                  input.touchDown(190, 430, 0, 0);
                  input.touchDragged(530, 475, 0);
                  input.touchUp(530, 475, 0, 0);
                  before = snapshot();
                  int history = history();
                  input.touchDown(300, 450, 0, 0);
                  input.touchDragged(332, 498, 0);
                  screen.render(1f / 30);
                  var capture =
                      com.badlogic.gdx.graphics.Pixmap.createFromFrameBuffer(0, 0, 1280, 768);
                  com.badlogic.gdx.graphics.PixmapIO.writePNG(
                      Gdx.files.local("target/multi-selection-preview.png"), capture, -1, true);
                  capture.dispose();
                  input.touchUp(332, 498, 0, 0);
                  after = snapshot();
                  check(changed(before, after) > 4, "Group drag did not move multiple objects");
                  check(history() == history + 1, "Group drag must be one history entry");
                  replay("performUndo");
                  check(before.equals(snapshot()), "Group undo lost scenery/collision");
                  check(
                      ((java.util.Set<?>) field(workspace, "selection")).isEmpty(),
                      "Undo left stale selected anchors");
                  replay("performRedo");
                  check(after.equals(snapshot()), "Group redo lost scenery/collision");
                  System.out.println("BOX_SELECTION_GROUP_MOVE_UNDO_REDO_OK");
                  check(
                      workspace.keyDown(Input.Keys.FORWARD_DEL),
                      "Legacy delete escaped multi-select");
                  for (int key :
                      new int[] {
                        Input.Keys.F11, Input.Keys.F8, Input.Keys.F9, Input.Keys.B, Input.Keys.W
                      }) check(!workspace.keyDown(key), "Global hotkey was swallowed: " + key);
                  System.out.println("MULTI_SELECT_GLOBAL_HOTKEY_PASSTHROUGH_OK");
                  Gdx.app.exit();
                }
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }
          },
          config);
    } finally {
      if (previous == null) Files.deleteIfExists(preferences);
      else Files.write(preferences, previous);
    }
  }
}
