package team163.logistics;

import team163.utils.SimplePather;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;
import battlecode.common.RobotController;

//Path channels 2000-2010 for request
//Path channel 2001 + maxPaths + 4 * request# e
//Path channel 2000 + maxPaths * 5 + 100 * request# to 100 past that for path
//2048
//

public class PathBeaver {

	public static RobotController rc;
	static int changed = 0;
	static final int maxPaths = 10;
	static MapLocationPair[] paths = new MapLocationPair[maxPaths];
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
		MapLocation hq = rc.senseHQLocation();
		MapLocation enemyhq = rc.senseEnemyHQLocation();
		p = new SimplePather(rc);
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
			} catch (Exception e) {
				changed = 5;
			}
		}
	}

	private static void checkRequests() throws GameActionException {
		if (rc.readBroadcast(2000 + pathCount) != 0) { //if I got a new path request
			//check for MapLocations
			int sx = rc.readBroadcast(2001 + maxPaths + pathCount * 4);
			int sy = rc.readBroadcast(2001 + maxPaths + pathCount * 4+1);
			int fx = rc.readBroadcast(2001 + maxPaths + pathCount * 4+2);
			int fy = rc.readBroadcast(2001 + maxPaths + pathCount * 4+3);
			MapLocation start = new MapLocation(sx,sy);
			MapLocation finish = new MapLocation(fx,fy);
			paths[pathCount] = new MapLocationPair(start, finish);
			loadPath(paths[pathCount], pathCount);
			pathCount++;
		}

	}

	//
	//		MapLocation start = new MapLocation(rc.readBroadcast(73), rc.readBroadcast(74)); //HQ
	//		MapLocation finish = new MapLocation(rc.readBroadcast(75), rc.readBroadcast(76)); //RALLY
	//first val in channel is 
	private static void loadPath(MapLocationPair pr, int count) throws GameActionException {
		//System.out.println(pr.start + " " + pr.finish);
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
			changed = 0;
		} else {
			System.out.println("Null path");
		}
	}

	public static int getPathChannel(int count) {
		return 2000 + maxPaths * 5 + 100 * count;
	}
	public static int getPathRequestChannel(int count) {
		return 2001 + maxPaths + count * 4;
	}

	static boolean isStationary(RobotType rt) {
		return (rt != null && rt == RobotType.AEROSPACELAB
				|| rt == RobotType.BARRACKS || rt == RobotType.HELIPAD
				|| rt == RobotType.HQ || rt == RobotType.MINERFACTORY
				|| rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY
				|| rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD);
	}

}
