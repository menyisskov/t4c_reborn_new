package com.perso.T4C.player;

import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_BELOW_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_MOVEMENT_RESERVATION_STEP_TILES;
import static com.perso.T4C.config.GameConstants.PLAYER_SPEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock("CollisionManager")
class PlayerMovementCollisionTest {

    @Test
    void movementReservationMatchesOriginalT4cUserSpeed() {
        assertEquals(1f, PLAYER_MOVEMENT_RESERVATION_STEP_TILES);
    }

    @TempDir
    Path tempDir;

    @AfterEach
    void clearCollisionManager() {
        CollisionManager.getInstance().clear();
    }

    @Test
    void blockedWestInputFallsBackToSouthwestLikeOriginalT4c() throws Exception {
        initializeCollisionMap(4, 4, 0, 0);
        Player player = createPlayerAt(GRID_W, 0f);

        player.move(-1f, 0f, 0.001f);

        // West is direction 7. Character::MoveUnit tries direction 6
        // (southwest) first when the exact west destination is blocked.
        assertTrue(player.getCoordinates().getX() < GRID_W);
        assertTrue(player.getCoordinates().getY() > 0f);
        assertTrue(player.getMovement().isMoving());
    }

    @Test
    void heldCardinalMovementAgainstCollisionUsesAdjacentDirection() throws Exception {
        int blockedX = 4 + leadingCollisionCellOffset();
        initializeCollisionMap(10, 10, blockedX, 3);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 10, 10);

        for (int frame = 0; frame < 10; frame++) {
            player.move(1f, 0f, 0.01f);
        }

        assertTrue(player.getCoordinates().getX() > 3f * GRID_W);
        assertTrue(player.getCoordinates().getY() < 3f * GRID_H);
        assertFalse(CollisionManager.getInstance().hasCollision(
                player.getCoordinates().getX(), player.getCoordinates().getY()));
        assertTrue(player.getMovement().isMoving());
    }

    @Test
    void playerFootprintStopsBeforeWallSpriteOverhang() throws Exception {
        int blockedX = 4 + leadingCollisionCellOffset();
        initializeCollisionMap(10, 10, blockedX, 3);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 10, 10);

        player.move(1f, 0f, 0.01f);

        assertTrue(player.getCoordinates().getX() > 3f * GRID_W);
        assertTrue(player.getCoordinates().getY() < 3f * GRID_H);
        assertFalse(CollisionManager.getInstance().hasCollision(
                player.getCoordinates().getX(), player.getCoordinates().getY()));
        assertTrue(player.getMovement().isMoving());
    }

    @Test
    void diagonalMovementCannotSkipCollisionOnHorizontalLeadingEdge() throws Exception {
        int blockedX = 4 + leadingCollisionCellOffset();
        int blockedY = 4;
        initializeCollisionMap(12, 12, blockedX, blockedY);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 12, 12);

        for (int frame = 0; frame < 10; frame++) {
            player.move(1f, 0.5f, 0.01f);
        }

        // SE tries east before south in Character::MoveUnit. The diagonal
        // corner would introduce a collision, so only X is interpolated.
        assertEquals(3f * GRID_W + 10f * PLAYER_SPEED * 0.01f,
                player.getCoordinates().getX(), 0.001f);
        assertEquals(3f * GRID_H, player.getCoordinates().getY());
        assertTrue(player.getMovement().isMoving());
    }

    @Test
    void diagonalMovementCannotSkipCollisionOnVerticalLeadingEdge() throws Exception {
        int blockedX = 4 + leadingCollisionCellOffset();
        int blockedY = 4 - leadingCollisionCellOffset();
        initializeCollisionMap(12, 12, blockedX, blockedY);
        Player player = createPlayerAt(3f * GRID_W, 5f * GRID_H, 12, 12);

        player.move(1f, -0.5f, 0.01f);

        // The requested diagonal is blocked by its vertical leading edge.
        // NE's first fallback is north, whose full footprint is clear.
        assertEquals(3f * GRID_W, player.getCoordinates().getX());
        assertEquals(5f * GRID_H - PLAYER_SPEED * 0.01f * GRID_H / GRID_W,
                player.getCoordinates().getY(), 0.001f);
    }

    @Test
    void diagonalMovementNeverPlacesLogicalAnchorOnBlockedDestination() throws Exception {
        initializeCollisionMap(5, 5, 1, 1);
        Player player = createPlayerAt(0f, 0f);
        player.setMapBounds(5 * GRID_W, 5 * GRID_H);

        // The large frame lands on (1,1), while every one-tile lookahead
        // destination is free. This used to bypass the missing base probe.
        player.move(1f, 0.5f, 0.2f);

        assertFalse(CollisionManager.getInstance().hasCollision(
                player.getCoordinates().getX(), player.getCoordinates().getY()));
    }

    @Test
    void diagonalSlideFollowsTheEdgeWhenBothAdjacentDirectionsAreBlocked() throws Exception {
        int lookahead = leadingCollisionCellOffset();
        initializeCollisionMap(
                12,
                12,
                4 + lookahead, 4,
                4 + lookahead, 3,
                3, 4);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 12, 12);

        for (int frame = 0; frame < 40; frame++) {
            player.move(1f, 0.5f, 0.01f);
        }

        // SE and both of its ring neighbours are blocked. Rather than halting
        // against the wall below, the puppet keeps following the obstacle edge
        // and ends on a free cell well away from its start.
        assertTrue(player.getCoordinates().getX() > 3f * GRID_W);
        assertFalse(CollisionManager.getInstance().hasCollision(
                player.getCoordinates().getX(), player.getCoordinates().getY()));
    }

    @Test
    void southInputSlidesAlongAWallSpanningTheWholeRowBelow() throws Exception {
        // A wall covering the destination and both diagonal fallbacks below.
        initializeCollisionMap(10, 10, 2, 4, 3, 4, 4, 4);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 10, 10);

        player.move(0f, 0.5f, 0.01f);

        // South, SW and SE are all blocked; the puppet follows the wall edge
        // horizontally instead of stopping dead against it.
        assertNotEquals(3f * GRID_W, player.getCoordinates().getX());
        assertEquals(3f * GRID_H, player.getCoordinates().getY());
        assertTrue(player.getMovement().isMoving());
    }

    @Test
    void blockedNorthInputSlidesNorthwestLikeOriginalT4cDirectionRing() throws Exception {
        initializeCollisionMap(10, 10, 3, 2);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 10, 10);

        player.move(0f, -0.5f, 0.01f);

        assertTrue(player.getCoordinates().getX() < 3f * GRID_W);
        assertTrue(player.getCoordinates().getY() < 3f * GRID_H);
        assertTrue(player.getMovement().isMoving());
    }

    @Test
    void blockedEastInputTriesNortheastFirst() throws Exception {
        initializeCollisionMap(10, 10, 4, 3);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 10, 10);

        player.move(1f, 0f, 0.01f);

        assertTrue(player.getCoordinates().getX() > 3f * GRID_W);
        assertTrue(player.getCoordinates().getY() < 3f * GRID_H);
        assertTrue(player.getMovement().isMoving());
    }

    @Test
    void eastInputKeepsOneCellBelowAnUpperDiagonalWall() throws Exception {
        initializeCollisionMap(10, 10, 4, 2);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 10, 10);

        player.move(1f, 0f, 0.01f);

        // East would put the fixed upper footprint on (4,2). NE is blocked
        // too, so the C++ ring falls back to SE while retaining one clear row.
        assertTrue(player.getCoordinates().getX() > 3f * GRID_W);
        assertTrue(player.getCoordinates().getY() > 3f * GRID_H);
        assertFalse(CollisionManager.getInstance().hasCollisionAtGrid(4, 3));
    }

    @Test
    void diagonalInputKeepsPaddingWhenItReachesANewUpperCollisionTip() throws Exception {
        initializeCollisionMap(10, 10, 4, 2, 3, 4, 4, 4);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 10, 10);

        player.move(1f, 0.5f, 0.01f);

        // SE and both its ring neighbours are blocked by the (4,2) tip and the
        // (3,4)/(4,4) row below. Instead of stopping, the puppet follows the
        // edge westwards. The upper padding still holds: (4,2) is never entered.
        assertTrue(player.getCoordinates().getX() < 3f * GRID_W);
        assertTrue(player.getCoordinates().getY() > 3f * GRID_H);
        assertFalse(CollisionManager.getInstance().hasCollision(
                player.getCoordinates().getX(), player.getCoordinates().getY()));
    }

    @Test
    void diagonalInputCanContinueAlongAnAlreadyTouchedUpperContour() throws Exception {
        initializeCollisionMap(10, 10, 3, 1, 4, 2);
        Player player = createPlayerAt(3f * GRID_W, 2f * GRID_H, 10, 10);

        player.move(1f, 0.5f, 0.01f);

        assertTrue(player.getCoordinates().getX() > 3f * GRID_W);
        assertTrue(player.getCoordinates().getY() > 2f * GRID_H);
    }

    @Test
    void diagonalStaircaseSlideCannotIntroduceANewLeadingCollision() throws Exception {
        // A stair moves one cell closer to the leading edge during the
        // cardinal fallback. The old slide checked only the player anchor and
        // entered that new orange cell.
        int lookahead = leadingCollisionCellOffset();
        initializeCollisionMap(
                12,
                12,
                4 + lookahead, 4 - lookahead,
                3 + lookahead, 4 - lookahead,
                4 + lookahead, 5 - lookahead);
        Player player = createPlayerAt(3f * GRID_W, 5f * GRID_H, 12, 12);
        float sourceX = player.getCoordinates().getX();
        float sourceY = player.getCoordinates().getY();

        player.move(1f, -0.5f, 0.01f);

        // A half-cell reservation may advance within the currently clear
        // region, but it must never introduce the staircase collision.
        assertNoNewLeadingFootprintCollision(
                sourceX,
                sourceY,
                player.getCoordinates().getX(),
                player.getCoordinates().getY(),
                1f,
                -1f);
    }

    @Test
    void slidesAlongRealWorldWallWithoutIntroducingAnyFootprintCollision() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        Player player = new Player();
        player.setMapBounds(3072 * GRID_W, 3072 * GRID_H);
        // This is the last completely clear footprint before the diagonal
        // BrickDarkWall sequence shown in the reported screenshot.
        player.setWorldPosition(2935f * GRID_W, 1074f * GRID_H, 0);

        assertTrue(CollisionManager.getInstance().hasCollisionAtGrid(2942, 1075));
        float previousX = player.getCoordinates().getX();
        float previousY = player.getCoordinates().getY();
        boolean slidVertically = false;
        for (int frame = 0; frame < 80; frame++) {
            player.move(1f, -0.5f, 0.01f);
            float currentX = player.getCoordinates().getX();
            float currentY = player.getCoordinates().getY();
            assertLeadingFootprintIsFree(currentX, currentY, 1f, -1f);
            if (currentY < previousY && Math.abs(currentX - previousX) < 0.001f) {
                slidVertically = true;
            }
            previousX = currentX;
            previousY = currentY;
        }
        assertLeadingFootprintIsFree(
                player.getCoordinates().getX(),
                player.getCoordinates().getY(),
                1f,
                -1f);
    }

    @Test
    void firstRowOfATwoRowWallBandIsWalkableWhileTheRowBehindStillBlocks() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        CollisionManager collisionManager = CollisionManager.getInstance();

        // Reported spot: the player stands on the safe-haven tile (2861,1094)
        // with a diagonal wall band running underneath. Rows 1095..1097 are all
        // authored as ABSOLUTE, so the band is three rows thick here.
        assertFalse(collisionManager.hasCollisionAtGrid(2861, 1094));
        assertFalse(collisionManager.hasCollisionAtGrid(2861, 1095));
        assertTrue(collisionManager.hasCollisionAtGrid(2861, 1096));
        assertTrue(collisionManager.hasCollisionAtGrid(2861, 1097));
    }

    @Test
    void isolatedSingleRowObstaclesKeepBlocking() throws Exception {
        // One blocking row with free cells above and below is a real barrier,
        // not a wall front, so the wall-foot rule must leave it alone.
        initializeCollisionMap(10, 10, 2, 4, 3, 4, 4, 4);

        assertTrue(CollisionManager.getInstance().hasCollisionAtGrid(3, 4));
    }

    @Test
    void reportedWallAbovePositionCannotBeReservedFromTheSouth() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        Player player = new Player();
        player.setMapBounds(3072 * GRID_W, 3072 * GRID_H);
        player.setWorldPosition(2888f * GRID_W, 1140f * GRID_H, 0);

        boolean enteredReportedPosition = false;
        for (int frame = 0; frame < 80; frame++) {
            player.move(0f, -0.5f, 0.01f);
            enteredReportedPosition |= Math.abs(player.getCoordinates().getX() - 2888f * GRID_W) < 0.001f
                    && Math.abs(player.getCoordinates().getY() - 1137f * GRID_H) < 0.001f;
        }

        assertFalse(enteredReportedPosition);
    }

    @Test
    void reportedWallBelowPositionCanBeReachedByThePlayersFeet() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        Player player = new Player();
        player.setMapBounds(3072 * GRID_W, 3072 * GRID_H);
        player.setWorldPosition(2892f * GRID_W, 1136f * GRID_H, 0);

        float closestDistance = Float.MAX_VALUE;
        for (int frame = 0; frame < 80; frame++) {
            player.move(0f, 0.5f, 0.01f);
            float distanceX = Math.abs(player.getCoordinates().getX() - 2892f * GRID_W) / GRID_W;
            float distanceY = Math.abs(player.getCoordinates().getY() - 1139f * GRID_H) / GRID_H;
            closestDistance = Math.min(closestDistance, distanceX + distanceY);
        }

        assertTrue(closestDistance < 0.2f,
                "the foot anchor should reach the lower wall boundary");
    }

    @Test
    void reportedPositionIsKeptWhenOnlyItsHorizontalNeighbourIsBlocked() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        Player player = new Player();
        player.setMapBounds(3072 * GRID_W, 3072 * GRID_H);
        // Position left by the former faulty slide in player_state.json.
        player.setWorldPosition(2937f * GRID_W, 1072f * GRID_H, 0);

        assertFalse(player.getMovement().recoverLoadedCollisionPosition(player));
        for (int frame = 0; frame < 80; frame++) {
            player.move(1f, -0.5f, 0.01f);
            assertLeadingFootprintIsFree(
                    player.getCoordinates().getX(),
                    player.getCoordinates().getY(),
                    1f,
                    -1f);
        }

        assertFalse(CollisionManager.getInstance().hasCollision(
                player.getCoordinates().getX(),
                player.getCoordinates().getY()));
    }

    @Test
    void loadedPositionIsKeptWhenAHorizontalNeighbourCellIsBlocked() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        Player player = new Player();
        player.setMapBounds(3072 * GRID_W, 3072 * GRID_H);
        player.setWorldPosition(2937f * GRID_W, 1072f * GRID_H, 0);

        assertFalse(player.getMovement().recoverLoadedCollisionPosition(player));
        assertPlayerFootprintIsFree(player);
    }

    @Test
    void latestSavedPositionIsKeptWhenOnlyItsHorizontalNeighbourIsBlocked() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        Player player = new Player();
        player.setMapBounds(3072 * GRID_W, 3072 * GRID_H);
        player.setWorldPosition(2959f * GRID_W, 1068f * GRID_H, 0);

        assertFalse(player.getMovement().recoverLoadedCollisionPosition(player));
        assertPlayerFootprintIsFree(player);
    }

    @Test
    void validLoadedPositionIsNotMoved() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        Player player = new Player();
        player.setMapBounds(3072 * GRID_W, 3072 * GRID_H);
        player.setWorldPosition(2935f * GRID_W, 1073f * GRID_H, 0);

        assertFalse(player.getMovement().recoverLoadedCollisionPosition(player));
        assertEquals(2935f * GRID_W, player.getCoordinates().getX());
        assertEquals(1073f * GRID_H, player.getCoordinates().getY());
    }

    @Test
    void realWorldDoorWallMovementNeverPlacesAnchorOnBlockingTile() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        Player player = new Player();
        player.setMapBounds(3072 * GRID_W, 3072 * GRID_H);
        player.setWorldPosition(2936f * GRID_W, 1070f * GRID_H, 0);

        for (int frame = 0; frame < 80; frame++) {
            player.move(1f, -0.5f, 0.01f);
            assertFalse(CollisionManager.getInstance().hasCollision(
                    player.getCoordinates().getX(), player.getCoordinates().getY()));
        }
    }

    @Test
    void realWorldDiagonalsAt2937x1072NeverEnterCollisionTile() throws Exception {
        File collisionFile = new File("assets/maps/worldmap/worldmap.colbin");
        CollisionManager.getInstance().initialize(new CollisionReader(collisionFile));
        float[][] directions = {
                {1f, -0.5f},
                {1f, 0.5f},
                {-1f, -0.5f},
                {-1f, 0.5f}
        };

        for (float[] direction : directions) {
            Player player = new Player();
            player.setMapBounds(3072 * GRID_W, 3072 * GRID_H);
            player.setWorldPosition(2937f * GRID_W, 1072f * GRID_H, 0);
            for (int frame = 0; frame < 200; frame++) {
                player.move(direction[0], direction[1], 0.01f);
                assertFalse(
                        CollisionManager.getInstance().hasCollision(
                                player.getCoordinates().getX(), player.getCoordinates().getY()),
                        "Collision entered at frame " + frame + " for direction ("
                                + direction[0] + "," + direction[1] + ")");
            }
        }
    }

    @Test
    void clickPathTargetsTileOriginUsedByEveryOtherPositionSystem() throws Exception {
        initializeCollisionMap(4, 4);
        Player player = createPlayerAt(0f, 0f);
        player.getMovement().setPath(List.of(new PlayerMovement.PathNode(1, 1)));

        player.move(0f, 0f, 1f);

        assertEquals(GRID_W, player.getCoordinates().getX());
        assertEquals(GRID_H, player.getCoordinates().getY());
    }

    @Test
    void stopCancelsReservedGridStepWithoutGhostMovement() throws Exception {
        initializeCollisionMap(10, 10);
        Player player = createPlayerAt(3f * GRID_W, 3f * GRID_H, 10, 10);

        player.move(1f, 0f, 0.01f);
        float stoppedX = player.getCoordinates().getX();
        float stoppedY = player.getCoordinates().getY();

        player.getMovement().stop();
        player.move(0f, 0f, 0.01f);

        assertEquals(stoppedX, player.getCoordinates().getX());
        assertEquals(stoppedY, player.getCoordinates().getY());
        assertFalse(player.getMovement().isMoving());
    }

    @Test
    void authoritativePositionChangesInvalidateReservedGridStep() throws Exception {
        initializeCollisionMap(10, 10);
        float sourceX = 3f * GRID_W;
        float sourceY = 3f * GRID_H;

        Player correctedToSource = createPlayerAt(sourceX, sourceY, 10, 10);
        correctedToSource.move(1f, 0f, 0.01f);
        correctedToSource.setWorldPosition(sourceX, sourceY, 0);
        correctedToSource.move(0f, 0f, 0.01f);

        assertEquals(sourceX, correctedToSource.getCoordinates().getX());
        assertEquals(sourceY, correctedToSource.getCoordinates().getY());
        assertFalse(correctedToSource.getMovement().isMoving());

        Player correctedToMiddle = createPlayerAt(sourceX, sourceY, 10, 10);
        correctedToMiddle.move(1f, 0f, 0.01f);
        float middleX = sourceX + GRID_W * 0.5f;
        correctedToMiddle.setWorldPosition(middleX, sourceY, 0);
        correctedToMiddle.move(0f, 0f, 0.01f);

        assertEquals(middleX, correctedToMiddle.getCoordinates().getX());
        assertEquals(sourceY, correctedToMiddle.getCoordinates().getY());
        assertFalse(correctedToMiddle.getMovement().isMoving());
    }

    @Test
    void firstInputFromFractionalPositionNeverMovesOppositeRequestedDirection() throws Exception {
        initializeCollisionMap(10, 10);
        float tileX = 3f * GRID_W;
        float tileY = 3f * GRID_H;

        float rightStartX = tileX + GRID_W - 1f;
        Player movingRight = createPlayerAt(rightStartX, tileY, 10, 10);
        movingRight.move(1f, 0f, 0.01f);
        assertTrue(
                movingRight.getCoordinates().getX() >= rightStartX,
                "Right input must not snap the player backward");

        float leftStartX = tileX + 1f;
        Player movingLeft = createPlayerAt(leftStartX, tileY, 10, 10);
        movingLeft.move(-1f, 0f, 0.01f);
        assertTrue(
                movingLeft.getCoordinates().getX() <= leftStartX,
                "Left input must not snap the player forward");
    }

    @Test
    void movementDistanceIsIndependentOfDeltaPartitioning() throws Exception {
        initializeCollisionMap(20, 20);
        float startX = 3f * GRID_W;
        float startY = 3f * GRID_H;
        Player oneLargeFrame = createPlayerAt(startX, startY, 20, 20);
        Player twentySmallFrames = createPlayerAt(startX, startY, 20, 20);

        oneLargeFrame.move(1f, 0f, 0.20f);
        for (int frame = 0; frame < 20; frame++) {
            twentySmallFrames.move(1f, 0f, 0.01f);
        }

        assertEquals(
                twentySmallFrames.getCoordinates().getX(),
                oneLargeFrame.getCoordinates().getX(),
                0.001f);
        assertEquals(
                twentySmallFrames.getCoordinates().getY(),
                oneLargeFrame.getCoordinates().getY(),
                0.001f);
    }

    @Test
    void highSpeedMovementPreservesLargeFrameTravelBudget() throws Exception {
        initializeCollisionMap(512, 32);
        float startX = 100f * GRID_W;
        float startY = 10f * GRID_H;
        Player oneLargeFrame = createPlayerAt(startX, startY, 512, 32);
        Player oneHundredSmallFrames = createPlayerAt(startX, startY, 512, 32);
        oneLargeFrame.setGmSpeedMultiplier(10f);
        oneHundredSmallFrames.setGmSpeedMultiplier(10f);

        oneLargeFrame.move(1f, 0f, 1f);
        for (int frame = 0; frame < 100; frame++) {
            oneHundredSmallFrames.move(1f, 0f, 0.01f);
        }

        assertEquals(
                oneHundredSmallFrames.getCoordinates().getX(),
                oneLargeFrame.getCoordinates().getX(),
                0.001f);
        assertEquals(
                oneHundredSmallFrames.getCoordinates().getY(),
                oneLargeFrame.getCoordinates().getY(),
                0.001f);
    }

    @Test
    void slowFrameCannotTunnelThroughSixteenPixelCollisionRow() throws Exception {
        initializeCollisionMap(4, 4, 0, 1);

        boolean canMove = CollisionManager.getInstance().canMove(0f, 0f, 0f, 2.5f * GRID_H);

        assertFalse(canMove);
    }

    private Player createPlayerAt(float x, float y) throws Exception {
        return createPlayerAt(x, y, 4, 4);
    }

    private Player createPlayerAt(float x, float y, int width, int height) throws Exception {
        Player player = new Player();
        player.setMapBounds(width * GRID_W, height * GRID_H);
        player.setWorldPosition(x, y, 0);
        return player;
    }

    private void initializeCollisionMap(int width, int height, int... blockedCoordinates) throws Exception {
        byte[] data = new byte[width * height];
        for (int i = 0; i < blockedCoordinates.length; i += 2) {
            int x = blockedCoordinates[i];
            int y = blockedCoordinates[i + 1];
            data[y * width + x] = 1;
        }
        File file = tempDir.resolve("movement.colbin").toFile();
        CollisionMapIO.write(file, width, height, data);
        CollisionManager.getInstance().initialize(new CollisionReader(file));
    }

    private void assertLeadingFootprintIsFree(
            float worldX,
            float worldY,
            float directionX,
            float directionY) {
        CollisionManager collisionManager = CollisionManager.getInstance();
        assertFalse(collisionManager.hasCollision(worldX, worldY), "logical anchor");
        float signX = Math.signum(directionX);
        float signY = Math.signum(directionY);
        int horizontalTiles = signX == 0f ? 0 : PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
        int verticalTiles = PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
        int maxTiles = Math.max(horizontalTiles, verticalTiles);
        for (int tile = 1; tile <= maxTiles; tile++) {
            float offsetX = signX * GRID_W * tile;
            float offsetY = -GRID_H * tile;
            if (tile <= horizontalTiles) {
                assertFalse(collisionManager.hasCollision(worldX + offsetX, worldY),
                        "horizontal leading footprint " + tile);
            }
            if (tile <= verticalTiles) {
                assertFalse(collisionManager.hasCollision(worldX, worldY + offsetY),
                        "vertical leading footprint " + tile);
            }
            if (tile <= horizontalTiles && tile <= verticalTiles) {
                assertFalse(collisionManager.hasCollision(worldX + offsetX, worldY + offsetY),
                        "corner leading footprint " + tile);
            }
        }
    }

    private int leadingCollisionCellOffset() {
        return PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
    }

    private void assertNoNewLeadingFootprintCollision(
            float fromX,
            float fromY,
            float toX,
            float toY,
            float directionX,
            float directionY) {
        CollisionManager collisionManager = CollisionManager.getInstance();
        assertFalse(collisionManager.hasCollision(toX, toY), "logical anchor");
        float signX = Math.signum(directionX);
        float signY = Math.signum(directionY);
        int horizontalTiles = signX == 0f ? 0 : PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
        int verticalTiles = PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
        int maxTiles = Math.max(horizontalTiles, verticalTiles);
        for (int tile = 1; tile <= maxTiles; tile++) {
            float offsetX = signX * GRID_W * tile;
            float offsetY = -GRID_H * tile;
            if (tile <= horizontalTiles) {
                assertFalse(
                        !collisionManager.hasCollision(fromX + offsetX, fromY)
                                && collisionManager.hasCollision(toX + offsetX, toY),
                        "new horizontal footprint collision " + tile);
            }
            if (tile <= verticalTiles) {
                assertFalse(
                        !collisionManager.hasCollision(fromX, fromY + offsetY)
                                && collisionManager.hasCollision(toX, toY + offsetY),
                        "new vertical footprint collision " + tile);
            }
            if (tile <= horizontalTiles && tile <= verticalTiles) {
                assertFalse(
                        !collisionManager.hasCollision(fromX + offsetX, fromY + offsetY)
                                && collisionManager.hasCollision(toX + offsetX, toY + offsetY),
                        "new corner footprint collision " + tile);
            }
        }
    }

    private void assertPlayerFootprintIsFree(Player player) {
        int gridX = Math.round(player.getCoordinates().getX() / GRID_W);
        int gridY = Math.round(player.getCoordinates().getY() / GRID_H);
        CollisionManager collisionManager = CollisionManager.getInstance();
        for (int x = gridX - PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
                x <= gridX + PLAYER_COLLISION_FOOTPRINT_HORIZONTAL_TILES;
                x++) {
            for (int y = gridY - PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
                    y <= gridY + PLAYER_COLLISION_FOOTPRINT_BELOW_TILES;
                    y++) {
                assertFalse(collisionManager.hasCollisionAtGrid(x, y),
                        "player footprint at (" + x + "," + y + ")");
            }
        }
    }
}
