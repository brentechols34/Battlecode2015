package team163.utils;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class PathMove2 {
	private int currentNode;
	private final RobotController rc;
	public boolean finished;
	public boolean amAFailure;

	public final SimplePather sp;
	public MapLocation[] path;
	public MapLocation goal;



	public PathMove2(RobotController rc) {
		this.rc = rc;
		sp = new SimplePather(rc);
		finished = false;
		amAFailure=false;
		currentNode = 0;
	}

	public int getCount() {
		return currentNode;
	}

	public void findPath(int prev) {
		try {
			MapLocation myLoc = rc.getLocation();
			boolean good = false;
			for (int i = prev; i < path.length; i++) {
				MapLocation temp = path[i];
				if (!isObsBetween(myLoc, temp)) {
					currentNode = i;
					good = true;
				}
			}
			if (!good) refindPath();
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}

	public void refindPath() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		for (int i = 0; i < path.length; i++) {
			MapLocation temp = path[i];
			if (!isObsBetween(myLoc, temp)) {
				currentNode = i;
				amAFailure = false;
				return;
			}
		}
		currentNode = 0;
		amAFailure = true;
	}
	
	public void givePath(MapLocation[] points) throws GameActionException {
		this.path = points;
		refindPath();
	}

	public boolean isObsBetween(MapLocation p1, MapLocation p2) throws GameActionException {
		if (p1.isAdjacentTo(p2)) {
			return false;
		}
		int x1 = p1.x;
		int y1 = p1.y;
		int x2 = p2.x;
		int y2 = p2.y;
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int sx = (x1 < x2) ? 1 : -1;
		int sy = (y1 < y2) ? 1 : -1;
		int err = dx - dy;
		while (true) {
			int e2 = err << 1;
			if (e2 > -dy) {
				err = err - dy;
				x1 = x1 + sx;
			}
			if (x1 == x2 && y1 == y2) {
				break;
			}
			if (impassable(new MapLocation(x1,y1))) {
				return true;
			}
			if (e2 < dx) {
				err = err + dx;
				y1 = y1 + sy;
			}
			if (x1 == x2 && y1 == y2) {
				break;
			}
			if (impassable(new MapLocation(x1,y1))) {
				return true;
			}
		}
		return false;
	}

	public boolean impassable(MapLocation m) throws GameActionException {
		if (rc.canSenseLocation(m)) {
			RobotInfo ri = rc.senseRobotAtLocation(m);
			if (ri != null && isStationary(ri.type)) return true;
		}
		TerrainTile tt = rc.senseTerrainTile(m);
		if (tt != TerrainTile.NORMAL) return true;
		return false;
	}

	static boolean isStationary(RobotType rt) {
		return (rt != null && rt == RobotType.AEROSPACELAB
				|| rt == RobotType.BARRACKS || rt == RobotType.HELIPAD
				|| rt == RobotType.HQ || rt == RobotType.MINERFACTORY
				|| rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY
				|| rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD);
	}

	public void setDestination(MapLocation m) throws GameActionException {
		path = sp.pathfind(rc.getLocation(), m);
		this.goal = m;
		refindPath();
		if (path == null)  System.out.println("Please why.");
	}

	public void attemptMove() throws GameActionException {
		if (path==null) return;
		MapLocation myLoc = rc.getLocation();
		if (currentNode>=path.length) {
			Move.tryMove(myLoc.directionTo(path[path.length - 1]));
			return;
		}
		if (impassable(path[currentNode])) {
			rc.setIndicatorString(0, "My path had an obstacle: repathing.");
			path = sp.pathfind(myLoc, goal);
			findPath(currentNode);

		}
		rc.setIndicatorString(0, "Normal:" + path[currentNode].toString());
		//Couldn't see path last time
		//try to find it
		if (amAFailure) {
			refindPath();
			if (amAFailure) { //if I still can't see path
				Move.tryMove(path[currentNode]); //try to bug to the start
				return;
			}
		}
		if (myLoc.equals(path[currentNode])) { //update node
			currentNode++;
		}
		if (currentNode < path.length) Move.tryMove(myLoc.directionTo(path[currentNode]));
	}

}
