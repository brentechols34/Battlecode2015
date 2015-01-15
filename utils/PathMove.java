package team163.utils;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class PathMove {
	private int currentNode;
	private MapLocation currentStep;
	private final int pathBaseChannel;
	private final int pathLen;
	private final RobotController rc;
	public boolean finished;
	public boolean amAFailure;
	
	public PathMove(RobotController rc, int pathBaseChannel, int pathLen, int previousCount) {
		this.pathBaseChannel = pathBaseChannel;
		this.rc = rc;
		this.pathLen = pathLen;
		finished = false;
		amAFailure=false;
		currentNode = 0;
		try {
			int x = rc.readBroadcast(pathBaseChannel);
			int y = rc.readBroadcast(pathBaseChannel + 1);
			currentStep = new MapLocation(x,y);
			findPath(Math.max(1,previousCount));
		} catch (GameActionException e) {
			e.printStackTrace();
			System.out.println("PathMove: failed to find initialize pathing.");
		}
	}
	
	public int getCount() {
		return currentNode;
	}

	public void findPath(int prev) throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		for (int i = prev; i < pathLen; i++) {
			int x = rc.readBroadcast(pathBaseChannel + i * 2);
			int y = rc.readBroadcast(pathBaseChannel + i * 2 + 1);
			MapLocation temp = new MapLocation(x,y);
			if (!isObsBetween(myLoc, temp)) {
				currentNode = i;
				currentStep = temp;
				return;
			}
		}
		if (prev == 1) {
			amAFailure=true;
			return;
		}
		findPath(1);
	}

	public boolean isObsBetween(MapLocation myLoc, MapLocation dest) throws GameActionException {
		Point p1 = new Point(myLoc.x, myLoc.y);
		Point p2 = new Point(dest.x, dest.y);
		if (p1.distance(p2) < 2) {
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
			if (impassable(x1,y1)) {
				return true;
			}
			if (e2 < dx) {
				err = err + dx;
				y1 = y1 + sy;
			}
			if (x1 == x2 && y1 == y2) {
				break;
			}
			if (impassable(x1,y1)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean impassable(int x, int y) throws GameActionException {
		MapLocation m = new MapLocation(x,y);
		if (rc.canSenseLocation(m)) {
			RobotInfo ri = rc.senseRobotAtLocation(m);
			if (ri != null && isStationary(ri.type)) return true;
		}
		TerrainTile tt = rc.senseTerrainTile(m);
		if (tt != TerrainTile.NORMAL) return true;
		
		return false;
	}

	public boolean stillGood() throws GameActionException {
		return !isObsBetween(rc.getLocation(), currentStep);
	}
	
	static boolean isStationary(RobotType rt) {
		return (rt != null && rt == RobotType.AEROSPACELAB
				|| rt == RobotType.BARRACKS || rt == RobotType.HELIPAD
				|| rt == RobotType.HQ || rt == RobotType.MINERFACTORY
				|| rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY
				|| rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD);
	}

	public void attemptMove() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		//am on path or can see it 
		if (currentNode >= pathLen-1) { //if at destination do nothing
			currentNode = pathLen-1;
			int x = rc.readBroadcast(pathBaseChannel + currentNode * 2);
			int y = rc.readBroadcast(pathBaseChannel + currentNode * 2 + 1);       
			currentStep = new MapLocation(x,y);
			finished = true;
		}
		if (!finished && (myLoc.isAdjacentTo(currentStep) || myLoc.equals(currentStep))) { //update node
			currentNode++;
//			int x,y;
//			do {
//				currentNode++;
//				x = rc.readBroadcast(pathBaseChannel + currentNode * 2);
//				y = rc.readBroadcast(pathBaseChannel + currentNode * 2 + 1);       
//				currentStep = new MapLocation(x,y);
//			} while (!isObsBetween(myLoc, currentStep) && currentNode < pathLen);
//			currentNode--;
			int x = rc.readBroadcast(pathBaseChannel + currentNode * 2);
			int y = rc.readBroadcast(pathBaseChannel + currentNode * 2 + 1);       
			currentStep = new MapLocation(x,y);
		}
		if (!amAFailure) Move.tryMove(myLoc.directionTo(currentStep));
		else { Move.tryMove(currentStep); }
	}

}
