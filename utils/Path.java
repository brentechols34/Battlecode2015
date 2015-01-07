/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package team163.utils;

import java.util.Arrays;

/**
 *
 * @author Alex
 */
public class Path {
    private final boolean[][] map;
    private final int width;
    private final int height;
    private static final float sqrt2 = 1.414f;
    private final int[][] prev;
    private final float[][] cost;
    private final boolean[][] closed_set;

    private final Point[] q;
    private final float[] costs;
    private int index;

    private Point dest;
    private static final float octile_constant = sqrt2 - 1;
    public static final int MAX_PATH_LENGTH = 100;

    public Path(boolean[][] map) {
        this.map = map;
        width = map.length;
        height = map[0].length;
        this.prev = new int[map.length][map[0].length];
        this.closed_set = new boolean[map.length][map[0].length];
        this.cost = new float[map.length][map[0].length];
        this.q = new Point[5000];
        this.costs = new float[5000];
    }

    /**
     * Finds a path from some start to some finish.
     *
     * @param start
     * @param finish
     * @return An array of points representing a path that does not intersect
     * any obstacles.
     */
    public Point[] pathfind(Point start, Point finish) {
        init();
        index = 0;
        Point current;
        dest = start;
        for (int i = 7; i != -1; i--) {
            check(finish, i);
        }
        closed_set[finish.x][finish.y] = true;
        final int desx = dest.x;
        final int desy = dest.y;
        while (index != 0) {
            current = q[--index];
            if (current.x == desx && current.y == desy) {
                return reconstruct();
            }
            expand(current);
        }
        return null;
    }

    /**
     * Initialize class variables for use in pathfinding
     */
    public void init() {
        for (int i = 0; i < width; i++) {
            Arrays.fill(prev[i],-1);
            Arrays.fill(closed_set[i],false);
        }
    }

    /**
     * Reconstruct the path.
     *
     * @return
     */
    private Point[] reconstruct() {
        Point current = dest;
        final Point[] path_temp = new Point[MAX_PATH_LENGTH];
        int count = 0;
        int dir = -1;
        do {
            int next = (prev[current.x][current.y] + 4) & 7;
            if (dir == -1 || next != dir) {
                path_temp[count++] = current;
                dir = next;
            }
            current = moveTo(current, next);   
        } while (prev[current.x][current.y] != -1);
        path_temp[count++] = current;
        final Point[] path = new Point[count];
        System.arraycopy(path_temp, 0, path, 0, count);
        return path;
    }

    private void expand(Point p) {
        final int dir = prev[p.x][p.y];
        if ((dir & 1) == 1) {
            check(p, (dir + 6) & 7);
            check(p, (dir + 2) & 7);
        }
        check(p, (dir + 7) & 7);
        check(p, (dir + 1) & 7);
        check(p, dir);
    }

    private void check(Point parent, int dir) {
        final Point n = moveTo(parent, dir);
        if (!validMove(n) || closed_set[n.x][n.y]) {
            return;
        }
        final float potential_cost = cost[parent.x][parent.y] + distance(parent, n);
        if (prev[n.x][n.y] == -1 || cost[n.x][n.y] > potential_cost) {
            add(n, potential_cost + octile(n, dest) * 1.5f); 
            prev[n.x][n.y] = dir;
            cost[n.x][n.y] = potential_cost;
        }
    }

    /**
     * Get the practical distance between 2 points
     *
     * @param p1
     * @param p2
     * @return
     */
    private static float distance(Point p1, Point p2) {
        final float dx = abs(p1.x - p2.x);
        final float dy = abs(p1.y - p2.y);
        return dx > dy ? (dy * sqrt2 + (dx - dy)) : (dx * sqrt2 + (dy - dx));
    }

    private static int abs(int x) {
        final int m = x >> 31;
        return x + m ^ m;
    }

    /**
     * heuristic for use with A
     * @param p1
     * @param p2
     * @return
     */
    private static float octile(Point p1, Point p2) {
        int dx = abs(p2.x - p1.x);
        int dy = abs(p2.y - p1.y);
        return dx > dy ? (dx + octile_constant * dy) : (dy + octile_constant * dx);
    }

    /**
     * Moves a point one cell along direction d.
     *
     * @param p
     * @param d
     * @return
     */
    private static Point moveTo(Point p, int d) {
        switch (d) {
            case 0:
                return new Point(p.x, p.y - 1);
            case 1:
                return new Point(p.x + 1, p.y - 1);
            case 2:
                return new Point(p.x + 1, p.y);
            case 3:
                return new Point(p.x + 1, p.y + 1);
            case 4:
                return new Point(p.x, p.y + 1);
            case 5:
                return new Point(p.x - 1, p.y + 1);
            case 6:
                return new Point(p.x - 1, p.y);
            default:
                return new Point(p.x - 1, p.y - 1);
        }
    }

    private boolean validMove(Point p) {
        return valid(p) && !map[p.x][p.y];
    }

    private boolean valid(Point p) {
        return (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height);
    }

    /**
     * Adds an obstacle to the map
     *
     * @param p The position of the obstacle.
     */
    public void addObstacle(Point p) {
        map[p.x][p.y] = true;
    }
    
    public void removeObstacle(Point p) {
        map[p.x][p.y] = false;
    }

    public int[] pathAndSerialize(Point start, Point dest) {
        final Point[] path = pathfind(start, dest);
        if (path == null) {
            return null;
        }
        final int[] sd = new int[path.length];
        for (int i = 0; i < path.length; i++) {
            sd[i] = path[i].serialize();
        }
        return sd;
    }

    private void add(Point p, float c) {
        int i = index;
        for (; i != 0 && c > costs[i - 1]; i--) {
            costs[i] = costs[i - 1];
            q[i] = q[i - 1];
        }
        costs[i] = c;
        q[i] = p;
        index++;
    }
    
}
