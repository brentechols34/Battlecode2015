package team163.logistics;

import team163.utils.SimplePather;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;
import battlecode.common.RobotController;

//Path channels 2000-2010 for request
//Path channel 2001 + MAX_PATHS + 4 * request# e
//Path channel 2000 + MAX_PATHS * 5 + 100 * request# to 100 past that for path
//2048
//

public class PathBeaver {

	public static RobotController rc;
	public static final int PATH_REQUEST_CHANNEL = 2000;
	public static final int MAX_PATHS = 10;
	static MapLocationPair[] paths = new MapLocationPair[MAX_PATHS];
	static int pathCount=0;
	static int pathVersion=0;
	static SimplePather p;

	private static class MapLocationPair {
		MapLocation start;
		MapLocation finish;

		public MapLocationPair(MapLocation start, MapLocation finish) {
			this.start = start;
			this.finish = finish;
		}

	}

	static void run() {
		rc.setIndicatorString(0, "I am the path beaver");
		p = new SimplePather(rc); //pathing unit
		MapLocation myLoc = rc.getLocation();
		//start positions will be rally MapLocation
		//channel 72 will be zero when pathbeaver is done.
		while (true) {
			try {
				//broadcast my location
				rc.broadcast(187, myLoc.x);
				rc.broadcast(188, myLoc.y);

				checkRequests();
				rc.broadcast(179, ++pathVersion);
				for (int i = 0; i < pathCount; i++) {
					loadPath(paths[i], i);
				}
			} catch (Exception e) { }
		}
	}

	private static void checkRequests() throws GameActionException {
		if (rc.readBroadcast(2000 + pathCount) != 0) { //if I got a new path request
			//check for MapLocations
			int sx = rc.readBroadcast(2001 + MAX_PATHS + pathCount * 4);
			int sy = rc.readBroadcast(2001 + MAX_PATHS + pathCount * 4+1);
			int fx = rc.readBroadcast(2001 + MAX_PATHS + pathCount * 4+2);
			int fy = rc.readBroadcast(2001 + MAX_PATHS + pathCount * 4+3);
			MapLocation start = new MapLocation(sx,sy);
			MapLocation finish = new MapLocation(fx,fy);
			paths[pathCount] = new MapLocationPair(start, finish);
			loadPath(paths[pathCount], pathCount);
			pathCount++;
		}

	}

	private static void loadPath(MapLocationPair pr, int count) throws GameActionException {
		MapLocation[] path = p.pathfind(pr.start, pr.finish); //this might take a bit
		if (path != null) {
			int channel = getPathChannel(count);
			int len = path.length;
			rc.broadcast(channel, len);
			channel++;
			for (MapLocation pnt : path) {
				rc.broadcast(channel, pnt.x);
				rc.broadcast(channel + 1, pnt.y);
				channel += 2;
			}
		} else {
			System.out.println("Null path");
		}
	}

	public static int getPathChannel(int count) {
		return PATH_REQUEST_CHANNEL + MAX_PATHS * 5 + 100 * count;
	}
	public static int getPathRequestChannel(int count) {
		return PATH_REQUEST_CHANNEL + MAX_PATHS + count * 4;
	}

	static boolean isStationary(RobotType rt) {
		return (rt != null && rt == RobotType.AEROSPACELAB
				|| rt == RobotType.BARRACKS || rt == RobotType.HELIPAD
				|| rt == RobotType.HQ || rt == RobotType.MINERFACTORY
				|| rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY
				|| rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD);
	}
	

}
