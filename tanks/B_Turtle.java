package team163.tanks;

import java.util.Random;
import team163.utils.*;

import battlecode.common.*;

public class B_Turtle implements Behavior {

	RobotController rc = Tank.rc;
	RobotInfo[] allies;
	RobotInfo[] enemies;
	MapLocation nearest;
	Random rand = new Random();

	public void perception() {
		nearest = rc.senseHQLocation();
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
			if (enemies.length > 0 && rc.isWeaponReady()) {
				rc.attackLocation(enemies[0].location);
			} else {
				if (rand.nextBoolean()) {
					Move.tryMove(rc.senseHQLocation());
				} else {
					/* move towards nearest */
					Move.tryMove(nearest);
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
