package team163.utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Arrays;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

/**
 *
 * @author Alex
 */
public class SimplePather {

	private int[][] prev;
	private final float[] costs;
	private final MapLocation[] q;
	private int index;
	private MapLocation dest;
	public static final int MAX_PATH_LENGTH = 100;
	private RobotController rc;
	final int dx;
	final int dy;

	int minX = 0;
	int maxX = 360;
	int minY = 0;
	int maxY = 360;

	MapLocation myHQ;
	MapLocation enemyHQ;
	MapLocation offsetMyHQ;
	MapLocation offsetEnemyHQ;

	public SimplePather(RobotController rc) {
		this.rc = rc;
		this.myHQ = rc.senseHQLocation();
		this.enemyHQ = rc.senseEnemyHQLocation();
		dx = enemyHQ.x - myHQ.x;
		dy = enemyHQ.y - myHQ.y;
		offsetMyHQ = new MapLocation(120 - dx, 120 - dy);
		offsetEnemyHQ = new MapLocation(120 + dx, 120 + dx);
		prev = new int[360][360];
		q = new MapLocation[2000];
		costs = new float[2000];
	}
	

	/**
	 * Finds a path from some start to some finish.
	 * This give absolutely no guarantees about path optimality, in fact, I reasonably 
	 * guarantee that this will tend not to do that.
	 * @param start
	 * @param finish
	 * @return An array of MapLocations representing a path that does not intersect
	 * any obstacles.
	 */
	public MapLocation[] pathfind(MapLocation start, MapLocation finish) throws GameActionException {
		start = offsetMapLocation(start);
		finish = offsetMapLocation(finish);
		prev = new int[360][360];
		index = 0;
		MapLocation current;
		dest = start;
		final int desx = dest.x;
		final int desy = dest.y;
		for (int i = 8; i != 0; i--) check(finish, i);
		while (index != 0) {
			current = q[--index];
			if (current.x == desx && current.y == desy) {
				return reconstruct();
			}
			expand(current);
		}
		return null;
	}

	public MapLocation offsetMapLocation(MapLocation p) { //centers this MapLocation
		MapLocation t = new MapLocation(p.x - myHQ.x + offsetMyHQ.x, p.y- myHQ.y + offsetMyHQ.y);
		return t;
	}
	public MapLocation unOffsetMapLocation(MapLocation p) { //returns this MapLocation to the real coordinates
		return new MapLocation(myHQ.x - offsetMyHQ.x + p.x, myHQ.y - offsetMyHQ.y + p.y);
	}

	/**
	 * Reconstruct the path.
	 *
	 * @return
	 */
	private MapLocation[] reconstruct() {
		MapLocation current = dest;
		final MapLocation[] path_temp = new MapLocation[MAX_PATH_LENGTH];
		int count = 0;
		int dir = 0;
		do {
			int next = ((prev[current.x][current.y]+3)&7)+1;
			//if (dir == 0 || next != dir) { //this minimizes the path, for efficient radio-ing
			path_temp[count++] = unOffsetMapLocation(current);
			//	dir = next;
			//}
			current = moveTo(current, next);
		} while (prev[current.x][current.y] != 0);
		path_temp[count++] = unOffsetMapLocation(current);
		final MapLocation[] path = new MapLocation[count];
		System.arraycopy(path_temp, 0, path, 0, count);
		return path;
	}

	private void expand(MapLocation p) throws GameActionException {
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

	private void check(MapLocation parent, int dir) throws GameActionException {
		final MapLocation n = moveTo(parent, dir);
		if (impassable(unOffsetMapLocation(n))) return;
		final int nx = n.x;
		final int ny = n.y;
        if (prev[nx][ny] == 0) {
            add(n, distance(n, dest) * 1.5f);
            prev[nx][ny] = dir;
        }
	}

	/**
	 * Get the distance between 2 MapLocations
	 */
	private static float distance(MapLocation p1, MapLocation p2) {
		final float dx = abs(p1.x - p2.x);
		final float dy = abs(p1.y - p2.y);
		return dx > dy ? (dy * 20f / 70 + dx) : (dx * 20f / 70 + dy);
	}

	private static int abs(int x) {
		final int m = x >> 31;
		return x + m ^ m;
	}

	/**
	 * Moves a MapLocation one cell along direction d.
	 *
	 * @param p
	 * @param d
	 * @return
	 */
	private static MapLocation moveTo(MapLocation p, int d) {
		switch (d) {
		case 1:
			return new MapLocation(p.x, p.y - 1);
		case 2:
			return new MapLocation(p.x + 1, p.y - 1);
		case 3:
			return new MapLocation(p.x + 1, p.y);
		case 4:
			return new MapLocation(p.x + 1, p.y + 1);
		case 5:
			return new MapLocation(p.x, p.y + 1);
		case 6:
			return new MapLocation(p.x - 1, p.y + 1);
		case 7:
			return new MapLocation(p.x - 1, p.y);
		default:
			return new MapLocation(p.x - 1, p.y - 1);
		}
	}

	public boolean impassable(MapLocation m) {
		try {
			TerrainTile tt = rc.senseTerrainTile(m);
			if (tt == TerrainTile.VOID || tt == TerrainTile.OFF_MAP) return true;
			if (rc.canSenseLocation(m)) {
				RobotInfo ri = rc.senseRobotAtLocation(m);
				if (ri != null && isStationary(ri.type)) return true;
			}
			return false;
		} catch(GameActionException e) {
			return impassable(m);
		}
	}

	public static boolean isStationary(RobotType rt) {
		return (rt != null && (rt == RobotType.AEROSPACELAB
				|| rt == RobotType.BARRACKS || rt == RobotType.HELIPAD
				|| rt == RobotType.HQ || rt == RobotType.MINERFACTORY
				|| rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY
				|| rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD));
	}

	private void add(MapLocation p, float c) {
		int i = index;
		for (; i != 0 && c > costs[i - 1]; i--) {
			costs[i] = costs[i - 1];
			q[i] = q[i - 1];
		}
		costs[i] = c;
		q[i] = p;
		index++;
	}

	/**
	 * This will decompress a path, adding in all the MapLocations between
	 * It's relatively expensive so use only when you think units will be off the path
	 * So that PathMove can get them back on using a raycast
	 * @param compressed
	 * @return
	 */
	public static MapLocation[] decompressPath(MapLocation[] compressed) {
		MapLocation[] buffered = new MapLocation[200];
		MapLocation next = compressed[0];
		buffered[0] = next;
		int count = 1;
		int compressed_MapLocationer = 1;
		while (!next.equals(compressed[compressed.length-1]) && count < buffered.length) {
			next.add(compressed[compressed_MapLocationer-1].directionTo(compressed[compressed_MapLocationer]));
			if (next.equals(compressed[compressed_MapLocationer])) compressed_MapLocationer++;
			buffered[count++] = next;
		}
		MapLocation[] actual = new MapLocation[count];
		System.arraycopy(buffered, 0, actual, 0, count);
		return actual;
	}

}

