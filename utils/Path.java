package team163.utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Arrays;

/**
 *
 * @author Alex
 */
public class Path {

    private final boolean[][] map;
    private final int width;
    private final int height;
    private int[][] prev;
    private final float[] costs;
    private final Point[] q;
    private int index;
    private Point dest;
    public static final int MAX_PATH_LENGTH = 100;
    
    final int dx;
    final int dy;
    
    int minX = 0;
    int maxX = 360;
    int minY = 0;
    int maxY = 360;
    
    Point myHQ;
    Point enemyHQ;
    Point offsetMyHQ;
    Point offsetEnemyHQ;

    public Path(Point myHQ, Point enemyHQ) {
        this.map = new boolean[360][360];
        this.myHQ = myHQ;
        this.enemyHQ = enemyHQ;
        dx = enemyHQ.x - myHQ.x;
        dy = enemyHQ.y - myHQ.y;
        offsetMyHQ = new Point(120 - dx, 120 - dy);
        offsetEnemyHQ = new Point(120 + dx, 120 + dx);
        minX = 0; //>=
        maxX = 360; //<
        minY = 0; //>=
        maxY = 360; //<
        width = map.length;
        height = map[0].length;
        prev = new int[map.length][map[0].length];
        q = new Point[5000];
        costs = new float[5000];
    }
    
    public void setLimits(int minX, int maxX, int minY, int maxY) {
    	if (this.minX < minX) {
    		this.minX = minX;
    	}
    	if (this.maxX > maxX) {
    		this.maxX = maxX;
    	}
    	if (this.minY < minY) {
    		this.minY = minY;
    	}
    	if (this.maxY > maxY) {
    		this.maxY = maxY;
    	}
    }
    
    public void setMinX(int minX) {
    	this.minX = minX - myHQ.x + offsetMyHQ.x;
    }
    
    public void setMaxX(int maxX) {
    	this.maxX = maxX - myHQ.x + offsetMyHQ.x;
    }
    
    public void setMinY(int minY) {
    	this.minY = minY - myHQ.x + offsetMyHQ.x;
    }
    
    public void setMaxY(int maxY) {
    	this.maxY = maxY - myHQ.x + offsetMyHQ.x;
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
    	start = offsetPoint(start);
    	finish = offsetPoint(finish);
        prev = new int[map.length][map[0].length];
        index = 0;
        Point current;
        dest = start;
        final int desx = dest.x;
        final int desy = dest.y;
        for (int i = 8; i != 0; i--) check(finish, i);
        int count = 0;
        while (index != 0) {
            current = q[--index];
            if (current.x == desx && current.y == desy) {
                //System.out.println(count);
                return reconstruct();
            }
            expand(current);
            count++;
        }
        return null;
    }
    
    public Point offsetPoint(Point p) {
    	Point t = new Point(p.x - myHQ.x + offsetMyHQ.x, p.y- myHQ.y + offsetMyHQ.y);
    	return t;
    }
    public Point unOffsetPoint(Point p) {
    	return new Point(myHQ.x - offsetMyHQ.x + p.x, myHQ.y - offsetMyHQ.y + p.y);
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
        int dir = 0;
        do {
            int next = ((prev[current.x][current.y]+3)&7)+1;
            //if (dir == 0 || next != dir) {
                path_temp[count++] = unOffsetPoint(current);
            //    dir = next;
            //}
            current = moveTo(current, next);
        } while (prev[current.x][current.y] != 0);
        path_temp[count++] = unOffsetPoint(current);
        final Point[] path = new Point[count];
        System.arraycopy(path_temp, 0, path, 0, count);
        return path;
    }

    private void expand(Point p) {
        final int dir = prev[p.x][p.y];
        if (dir == 0) return;
        if ((dir & 1) == 0) { //is diagonal
            check(p, ((dir + 5) & 7)+1);
            check(p, ((dir + 1) & 7)+1);
        }
        check(p, ((dir + 6) & 7)+1);
        check(p, ((dir + 0) & 7)+1);
        check(p, dir);
    }

    private void check(Point parent, int dir) {
        final Point n = moveTo(parent, dir);
        if (!validMove(n)) return;
        final int nx = n.x;
        final int ny = n.y;
        if (prev[nx][ny] == 0) {
            add(n, distance(n, dest));
            prev[nx][ny] = dir;
        }
    }

    /**
     * Get the distance between 2 points
     */
    private static float distance(Point p1, Point p2) {
        final float dx = abs(p1.x - p2.x);
        final float dy = abs(p1.y - p2.y);
        return dx > dy ? (dy * 20f / 70 + dx) : (dx * 20f / 70 + dy);
    }

    private static int abs(int x) {
        final int m = x >> 31;
        return x + m ^ m;
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
            case 1:
                return new Point(p.x, p.y - 1);
            case 2:
                return new Point(p.x + 1, p.y - 1);
            case 3:
                return new Point(p.x + 1, p.y);
            case 4:
                return new Point(p.x + 1, p.y + 1);
            case 5:
                return new Point(p.x, p.y + 1);
            case 6:
                return new Point(p.x - 1, p.y + 1);
            case 7:
                return new Point(p.x - 1, p.y);
            default:
                return new Point(p.x - 1, p.y - 1);
        }
    }

    private boolean validMove(Point p) {
        return (p.x >= minX && p.x < maxX && p.y >= minY && p.y < maxY) && !map[p.x][p.y];
    }

    /**
     * Adds an obstacle to the map
     *
     * @param p The position of the obstacle.
     */
    public boolean addObstacle(Point p) {
    	Point p2 = offsetPoint(p);
    	boolean oldVal =  map[p2.x][p2.y];
        map[p2.x][p2.y] = true;
    	return !oldVal;
    }

//    public void removeObstacle(Point p) {
//   
//        map[p.x][p.y] = false;
//    }

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
