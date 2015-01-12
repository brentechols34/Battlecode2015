package team163.tanks;

import java.util.Random;

import team163.utils.*;
import battlecode.common.*;

public class B_Turtle implements Behavior {

	static RobotController rc = Tank.rc;
	RobotInfo[] allies;
	RobotInfo[] enemies;
	MapLocation nearest;
	MapLocation rally;
	MapLocation goal;
	Random rand = new Random();
	static int currentCount = 0;
	boolean pathSet = false;
	int pathCount = 0;
	boolean madeItToRally = false;
	public boolean attacking;
	public void perception() {
		try {
			if (!pathSet) {
				pathSet = true;
				pathCount = rc.readBroadcast(179);
			}
			int x = rc.readBroadcast(50);
			int y = rc.readBroadcast(51);
			rally = new MapLocation(x,y);
			x = rc.readBroadcast(67);
			y = rc.readBroadcast(68);
			goal = new MapLocation(x,y);
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
			nearest = goal;
		} catch (Exception e) {
			System.out.println("B_Turtle perception error");
		}
		allies = rc.senseNearbyRobots(Tank.senseRange, Tank.team);
		enemies = rc.senseNearbyRobots(Tank.range, Tank.team.opponent());

	}

	public void calculation() {
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
			MapLocation myLoc= rc.getLocation();
			if (enemies.length > 0 && rc.isWeaponReady()) {
				rc.attackLocation(nearest);
			} else {
				if (attacking && madeItToRally) {
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
							Move.tryMove(goal);
						}
					}
				} else {
					if (rc.readBroadcast(179) != pathCount && rc.readBroadcast(77) > 0) {
						pathCount = rc.readBroadcast(179);
						currentCount = 0;
						int gx = rc.readBroadcast(578 + currentCount*2);
						int gy = rc.readBroadcast(579 + currentCount*2);
						MapLocation waypoint = new MapLocation(gx, gy);
						for (int i = 1; i < rc.readBroadcast(77); i++) {
							int gx2 = rc.readBroadcast(578 + i*2);
							int gy2 = rc.readBroadcast(579 + i*2);
							MapLocation waypoint2 = new MapLocation(gx2, gy2);
							if (myLoc.distanceSquaredTo(waypoint) > myLoc.distanceSquaredTo(waypoint2)) {
								currentCount = i;
								gx=gx2;
								gy=gy2;
								waypoint = waypoint2;
							}
						}
					}
					if (currentCount < rc.readBroadcast(77) - 5) {
						int gx = rc.readBroadcast(578 + currentCount*2);
						int gy = rc.readBroadcast(579 + currentCount*2);
						MapLocation waypoint = new MapLocation(gx, gy);
						if (myLoc.isAdjacentTo(waypoint) || myLoc.equals(waypoint)) {
							currentCount+=1;
							Move.tryMove(myLoc.directionTo(waypoint));
						} else {
							Move.tryMove(myLoc.directionTo(waypoint));
						}
					} else {
						madeItToRally = true;
						Move.tryMove(rally);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Tank Tutle action Error");
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
