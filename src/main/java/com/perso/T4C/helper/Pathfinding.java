package com.perso.T4C.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
/**
 * Class representing Pathfinding.
 */

public final class Pathfinding {
    private Pathfinding() {
    }
/**
 * Class representing GridPoint.
 */

    public static final class GridPoint {
        public final int x;
        public final int y;

        public GridPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static List<GridPoint> findPath(int startX, int startY, int goalX, int goalY, int width, int height, CollisionManager collisionManager) {
        return findPath(startX, startY, goalX, goalY, width, height, collisionManager, 0);
    }

    public static List<GridPoint> findPath(int startX, int startY, int goalX, int goalY, int width, int height, CollisionManager collisionManager, int clearanceTiles) {
        if (!isInBounds(startX, startY, width, height) || !isInBounds(goalX, goalY, width, height)) {
            return List.of();
        }

        if (startX == goalX && startY == goalY) {
            return List.of();
        }

        if (collisionManager != null && collisionManager.isInitialized()) {
            // Check goal without clearance: the goal tile itself must be passable,
            // but we don't require clearance around it (e.g. player standing next to a wall).
            if (isBlocked(goalX, goalY, collisionManager, 0)) {
                return List.of();
            }
        }

        int size = width * height;
        WorkArea work = WORK_AREA.get();
        work.ensureCapacity(size);
        int stamp = work.nextStamp();
        float[] gScore = work.gScore;
        int[] gStamp = work.gStamp;
        int[] cameFrom = work.cameFrom;
        int[] closedStamp = work.closedStamp;
        PriorityQueue<Node> open = work.open;
        open.clear();

        int startIdx = index(startX, startY, width);
        int goalIdx = index(goalX, goalY, width);
        gScore[startIdx] = 0f;
        gStamp[startIdx] = stamp;
        cameFrom[startIdx] = -1;

        open.add(new Node(startX, startY, 0f, heuristic(startX, startY, goalX, goalY)));

        int expanded = 0;
        int maxExpanded = Math.min(width * height, MAX_EXPANSIONS);
        long startTime = System.nanoTime();
        while (!open.isEmpty()) {
            Node current = open.poll();
            int currentIdx = index(current.x, current.y, width);
            if (closedStamp[currentIdx] == stamp) {
                continue;
            }
            closedStamp[currentIdx] = stamp;
            expanded++;
            if ((expanded & TIME_CHECK_MASK) == 0 && (System.nanoTime() - startTime) > MAX_NANOS) {
                return List.of();
            }
            if (expanded > maxExpanded) {
                return List.of();
            }

            if (currentIdx == goalIdx) {
                return buildPath(cameFrom, startIdx, goalIdx, width);
            }

            for (int i = 0; i < 8; i++) {
                int nx = current.x + DIR_X[i];
                int ny = current.y + DIR_Y[i];
                if (!isInBounds(nx, ny, width, height)) {
                    continue;
                }
                boolean isGoal = (nx == goalX && ny == goalY);
                int effectiveClearance = isGoal ? 0 : clearanceTiles;
                if (collisionManager != null && collisionManager.isInitialized() && isBlocked(nx, ny, collisionManager, effectiveClearance)) {
                    continue;
                }
                if (isDiagonal(i) && collisionManager != null && collisionManager.isInitialized()) {
                    int sideX = current.x + DIR_X[i];
                    int sideY = current.y;
                    int sideX2 = current.x;
                    int sideY2 = current.y + DIR_Y[i];
                    if (isBlocked(sideX, sideY, collisionManager, clearanceTiles) || isBlocked(sideX2, sideY2, collisionManager, clearanceTiles)) {
                        continue;
                    }
                }
                int neighborIdx = index(nx, ny, width);
                if (closedStamp[neighborIdx] == stamp) {
                    continue;
                }

                float currentG = gStamp[currentIdx] == stamp ? gScore[currentIdx] : Float.POSITIVE_INFINITY;
                float tentativeG = currentG + (isDiagonal(i) ? DIAGONAL_COST : 1f);
                float neighborG = gStamp[neighborIdx] == stamp ? gScore[neighborIdx] : Float.POSITIVE_INFINITY;
                if (tentativeG < neighborG) {
                    gScore[neighborIdx] = tentativeG;
                    gStamp[neighborIdx] = stamp;
                    cameFrom[neighborIdx] = currentIdx;
                    float f = tentativeG + heuristic(nx, ny, goalX, goalY);
                    open.add(new Node(nx, ny, tentativeG, f));
                }
            }
        }

        return List.of();
    }

    private static boolean isBlocked(int x, int y, CollisionManager collisionManager, int clearanceTiles) {
        // Use static-only check: door state changes are too costly to evaluate per A* node
        // (dynamicProvider iterates all door objects). Monsters/NPCs walk through closed doors.
        return clearanceTiles > 0
                ? collisionManager.hasStaticCollisionNearGrid(x, y, clearanceTiles)
                : collisionManager.hasStaticCollisionAtGrid(x, y);
    }

    private static List<GridPoint> buildPath(int[] cameFrom, int startIdx, int goalIdx, int width) {
        List<GridPoint> reversed = new ArrayList<>();
        int current = goalIdx;
        while (current != startIdx && current != -1) {
            int x = current % width;
            int y = current / width;
            reversed.add(new GridPoint(x, y));
            current = cameFrom[current];
        }
        List<GridPoint> path = new ArrayList<>(reversed.size());
        for (int i = reversed.size() - 1; i >= 0; i--) {
            path.add(reversed.get(i));
        }
        return path;
    }

    private static boolean isInBounds(int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private static int index(int x, int y, int width) {
        return x + y * width;
    }

    private static float heuristic(int x, int y, int goalX, int goalY) {
        int dx = Math.abs(goalX - x);
        int dy = Math.abs(goalY - y);
        int min = Math.min(dx, dy);
        int max = Math.max(dx, dy);
        return (DIAGONAL_COST * min) + (max - min);
    }

    private static final int[] DIR_X = {1, -1, 0, 0, 1, 1, -1, -1};
    private static final int[] DIR_Y = {0, 0, 1, -1, 1, -1, 1, -1};
    private static final float DIAGONAL_COST = 1.4142135f;
    private static final int MAX_EXPANSIONS = 8000;
    private static final long MAX_NANOS = 16_000_000L; // 16ms max — never block a frame
    private static final int TIME_CHECK_MASK = 0x1F; // check every 32 nodes instead of 256
    private static final ThreadLocal<WorkArea> WORK_AREA = ThreadLocal.withInitial(WorkArea::new);

    private static boolean isDiagonal(int dirIndex) {
        return dirIndex >= 4;
    }
/**
 * Class representing Node.
 */

    private static final class Node implements Comparable<Node> {
        private final int x;
        private final int y;
        private final float g;
        private final float f;

        private Node(int x, int y, float g, float f) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
        }

        @Override
        public int compareTo(Node other) {
            int cmp = Float.compare(this.f, other.f);
            if (cmp != 0) {
                return cmp;
            }
            return Float.compare(this.g, other.g);
        }
    }

    private static final class WorkArea {
        private float[] gScore = new float[0];
        private int[] gStamp = new int[0];
        private int[] cameFrom = new int[0];
        private int[] closedStamp = new int[0];
        private final PriorityQueue<Node> open = new PriorityQueue<>();
        private int stamp;

        private void ensureCapacity(int size) {
            if (gScore.length >= size) {
                return;
            }
            gScore = new float[size];
            gStamp = new int[size];
            cameFrom = new int[size];
            closedStamp = new int[size];
        }

        private int nextStamp() {
            stamp++;
            if (stamp == 0) {
                Arrays.fill(gStamp, 0);
                Arrays.fill(closedStamp, 0);
                stamp = 1;
            }
            return stamp;
        }
    }
}
