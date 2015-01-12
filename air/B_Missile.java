package team163.air;

import team163.utils.Move;
import battlecode.common.*;

public class B_Missile implements Behavior {

	double ratio;
	RobotInfo[] robots;
	MapLocation loc;

	public void setRc(RobotController rc) {
		// stub for inheritance
	}

	public void perception() {
		try {
			robots = Missile.rc.senseNearbyRobots(2);
			int x = Missile.rc.readBroadcast(Missile.channel);
			int y = Missile.rc.readBroadcast(Missile.channel + 1);
			loc = new MapLocation(x, y);
		} catch (Exception e) {
			System.out.println("Error in missile perception");
		}
	}

	public void calculation() {
		double enemy = 0;
		double allie = 0;

		for (RobotInfo x : robots) {
			if (x.team == Missile.team) {
				allie++;
			} else {
				enemy++;
			}
		}

		if (allie == 0 && enemy > 0) {
			ratio = 1;
		} else {
			ratio = enemy / allie;
		}
	}

	public void action() {
		try {
			if (ratio > .5) {
				Missile.rc.explode();
			} else {
				Move.tryMove(Missile.rc.getLocation().directionTo(loc));
			}
		} catch (Exception e) {
			System.out.println("Error in Missile action");
		}

	}

	public void panicAlert(MapLocation m) {
		// TODO Auto-generated method stub

	}
}
