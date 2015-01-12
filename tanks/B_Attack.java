package team163.tanks;

import team163.utils.*;
import battlecode.common.*;

public class B_Attack implements Behavior {

	static RobotController rc = Tank.rc;
	MapLocation nearest; /* nearest enemy */
	MapLocation goal;
	RobotInfo[] enemies;
	static int currentCount = 0;
	public static boolean madeItToRally = false;
	boolean pathSet = false;
	int pathCount = 0;
	MapLocation rally;
	public void perception() {
		/* for now simply look for nearest enemy */
		try {
			if (!pathSet) {
				pathSet = true;
				pathCount = rc.readBroadcast(179);
			}
		int x = rc.readBroadcast(67);
		int y = rc.readBroadcast(68);
		nearest = new MapLocation(x,y);
		rally = nearest;
		} catch (Exception e) {
			System.out.println("B_Attack perception error");
		}
		enemies = rc.senseNearbyRobots(Tank.range, Tank.team.opponent());

	}

	public void calculation() {

		/* for now just calculating distance to nearest enemy */
		double max = rc.getLocation().distanceSquaredTo(nearest);
		if (enemies.length > 0) {
			for (RobotInfo ri : enemies) {
				double dis = rc.getLocation().distanceSquaredTo(ri.location);
				if (dis < max) {
					max = dis;
					nearest = ri.location;
				}
			}
		}

	}

	public void action() {
		try {
			MapLocation myLoc = rc.getLocation();
			int x = rc.readBroadcast(67);
			int y = rc.readBroadcast(68);
			MapLocation tempLoc = new MapLocation(x,y);
			if (!tempLoc.equals(goal)) {
				goal = tempLoc;
			}
			RobotInfo ri;
			if (rc.canSenseLocation(goal) && ((ri=rc.senseRobotAtLocation(goal))==null || ri.type != RobotType.TOWER)) {
	            MapLocation[] towers = rc.senseEnemyTowerLocations();
	            if (towers.length == 0) {
	            	MapLocation enHQ = rc.senseEnemyHQLocation();
	            	rc.broadcast(67,enHQ.x);
		            rc.broadcast(68,enHQ.y);
		            goal = enHQ;
	            } else {
	            	int closest =  findClosestToHQ(towers);
	            	rc.broadcast(67,towers[closest].x);
	            	rc.broadcast(68,towers[closest].y);
	            	goal = towers[closest];
	            }
			}
			/* try to attack the nearest if unable than move towards it */
			if (rc.isWeaponReady() && enemies.length > 0) {
				if (rc.canAttackLocation(nearest)) {
					rc.attackLocation(nearest);
				} else {
					/* move towards nearest */
					Move.tryMove(nearest);
				}
			} else {
				if (!nearest.equals(goal)) {
					/* if weapon is not ready run from enemy */
					Direction away = rc.getLocation().directionTo(nearest)
							.opposite();
					Move.tryMove(away);
				} else {
					if (!madeItToRally) {
						if (rc.readBroadcast(179) != pathCount) {
							pathCount = rc.readBroadcast(179);
							currentCount = 0;
						}
						if (currentCount < rc.readBroadcast(77)-3) {
							int gx = rc.readBroadcast(578 + currentCount*2);
							int gy = rc.readBroadcast(579 + currentCount*2);
							MapLocation waypoint = new MapLocation(gx, gy);
							for (int i = currentCount; i < rc.readBroadcast(77); i++) {
								int gx2 = rc.readBroadcast(578 + currentCount*2);
								int gy2 = rc.readBroadcast(579 + currentCount*2);
								MapLocation waypoint2 = new MapLocation(gx2, gy2);
								if (myLoc.distanceSquaredTo(waypoint2) < myLoc.distanceSquaredTo(waypoint)) {
									currentCount = i;
									gx = rc.readBroadcast(578 + currentCount*2);
									gy = rc.readBroadcast(579 + currentCount*2);
									waypoint = waypoint2;
								}
							}

							//System.out.println(gx + " " + gy + " " + myLoc.distanceSquaredTo(waypoint) + " " + currentCount);
							if (myLoc.isAdjacentTo(waypoint)) {
								currentCount+=1;
							}
							//System.out.println("Trying to get to " + waypoint);
							Move.tryMove(waypoint);
						} else {
							madeItToRally = true;
							Move.tryMove(rally);
						}
					} else Move.tryMove(goal);
				}
			}
		} catch (Exception e) {
			System.out.println("Error in Tank Attack behavior action");
			e.printStackTrace();
		}
	}
	
	public static int findClosestToHQ(MapLocation[] locs) {
		int mindex = 0;
		MapLocation hq = rc.senseHQLocation();
		int minDis = hq.distanceSquaredTo(locs[0]);
		for (int i = 1; i < locs.length; i++) {
			int dis = hq.distanceSquaredTo(locs[i]);
			if (locs[i] != null && dis < minDis) {
				mindex=i;
				minDis = dis;
			}
		}
		return mindex;		
	}

	public void panicAlert(MapLocation m) {
		// TODO Auto-generated method stub

	}

}
