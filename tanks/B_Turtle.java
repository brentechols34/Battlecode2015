package team163.tanks;

import java.util.Random;

import team163.utils.*;
import battlecode.common.*;

public class B_Turtle implements Behavior {

	RobotController rc = Tank.rc;
	RobotInfo[] allies;
	RobotInfo[] enemies;
	MapLocation nearest;
	MapLocation rally;
	Random rand = new Random();
	static int currentCount = 0;
	public void perception() {
		try {
		int x = rc.readBroadcast(50);
		int y = rc.readBroadcast(51);
		rally = new MapLocation(x,y);
		nearest = rally;
		} catch (Exception e) {
			System.out.println("B_Turtle perception error");
		}
		allies = rc.senseNearbyRobots(Tank.senseRange, Tank.team);
		enemies = rc.senseNearbyRobots(Tank.range, Tank.team.opponent());

	}

	public void calculation() {
		double min = rc.getLocation().distanceSquaredTo(nearest);
		if (allies.length > 0) {
			for (RobotInfo ri : allies) {
				double dis = rc.getLocation().distanceSquaredTo(ri.location);
				if (dis < min) {
					min = dis;
					nearest = ri.location;
				}
			}
		}

	}

	public void action() {
		try {
			MapLocation myLoc= rc.getLocation();
			if (enemies.length > 0 && rc.isWeaponReady()) {
				rc.attackLocation(enemies[0].location);
			} else {
				//next waypoint
				if (currentCount < rc.readBroadcast(77)*2) {
					int gx = rc.readBroadcast(78 + currentCount);
					int gy = rc.readBroadcast(79 + currentCount);
					MapLocation waypoint = new MapLocation(gx, gy);
					System.out.println(gx + " " + gy + " " + myLoc.distanceSquaredTo(waypoint) + " " + currentCount);
					if (myLoc.isAdjacentTo(waypoint)) {
						System.out.println("yay");
						currentCount+=2;
					}
					Move.tryMove(myLoc.directionTo(waypoint));
				} else {
					return;
				}
			}
		} catch (Exception e) {
			System.out.println("Tank Tutle action Error");
		}

	}

	public void panicAlert(MapLocation m) {
		// TODO Auto-generated method stub

	}

}
