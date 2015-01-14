package team163.logistics;

import team163.utils.Path;
import team163.utils.Point;
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
	static PointPair[] paths = new PointPair[maxPaths];
	static int pathCount=0;
	static int pathVersion=0;
	static Path p;

	private static class PointPair {
		Point start;
		Point finish;

		public PointPair(Point start, Point finish) {
			this.start = start;
			this.finish = finish;
		}

	}

	static void run() {
		rc.setIndicatorString(0, "I am the path beaver");
		MapLocation hq = rc.senseHQLocation();
		MapLocation enemyhq = rc.senseEnemyHQLocation();
		p = new Path(new Point(hq.x, hq.y), new Point(enemyhq.x, enemyhq.y));
		MapLocation myLoc = rc.getLocation();


		//start positions will be rally point
		//channel 72 will be zero when pathbeaver is done.
		while (true) {
			try {
				//broadcast my location
				rc.broadcast(187, myLoc.x);
				rc.broadcast(188, myLoc.y);

				checkRequests();
				
				if (changed > 4) {
					rc.broadcast(179, ++pathVersion);
					for (int i = 0; i < pathCount; i++) {
						loadPath(paths[i], i);
					}
				} else mapScan();
			} catch (Exception e) {
				changed = 5;
			}
		}
	}

	private static void mapScan() throws GameActionException {
		RobotInfo[] allies = rc.senseNearbyRobots(9999999, rc.getTeam());
		for (RobotInfo r : allies) {
			if (!isStationary(r.type)) {
				for (int i = -3; i <= 3; i++) {
					for (int j = -3; j <= 3; j++) {
						MapLocation ml = new MapLocation(r.location.x + i, r.location.y + j);
						TerrainTile tt = rc.senseTerrainTile(ml);
						RobotInfo sensed = null;
						if (rc.canSenseLocation(ml)) {sensed = rc.senseRobotAtLocation(ml);}
						if ((tt != TerrainTile.NORMAL && tt != TerrainTile.UNKNOWN) || (sensed != null && isStationary(sensed.type) && sensed.type != RobotType.HQ)) {
							if (p.addObstacle(new Point(ml.x, ml.y))) {
								changed++;
							}
						}
					}
				}
			}
		}
	}

	private static void checkRequests() throws GameActionException {
		if (rc.readBroadcast(2000 + pathCount) != 0) { //if I got a new path request
			//check for points
			int sx = rc.readBroadcast(2001 + maxPaths + pathCount * 4);
			int sy = rc.readBroadcast(2001 + maxPaths + pathCount * 4+1);
			int fx = rc.readBroadcast(2001 + maxPaths + pathCount * 4+2);
			int fy = rc.readBroadcast(2001 + maxPaths + pathCount * 4+3);
			Point start = new Point(sx,sy);
			Point finish = new Point(fx,fy);
			paths[pathCount] = new PointPair(start, finish);
			loadPath(paths[pathCount], pathCount);
			pathCount++;
		}

	}

	//
	//		Point start = new Point(rc.readBroadcast(73), rc.readBroadcast(74)); //HQ
	//		Point finish = new Point(rc.readBroadcast(75), rc.readBroadcast(76)); //RALLY
	//first val in channel is 
	private static void loadPath(PointPair pr, int count) throws GameActionException {
		//System.out.println(pr.start + " " + pr.finish);
		Point[] path = p.pathfind(pr.start, pr.finish); //this might take a bit

		if (path != null) {
			int channel = getPathChannel(count);
			int len = path.length;
			rc.broadcast(channel, len);
			channel++;
			for (Point pnt : path) {
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
