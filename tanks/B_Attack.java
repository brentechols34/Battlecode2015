package team163.tanks;

import team163.utils.*;
import battlecode.common.*;

public class B_Attack implements Behavior {

	RobotController rc = Tank.rc;
	MapLocation nearest; /* nearest enemy */

	RobotInfo[] enemies;


	public void perception() {
		/* for now simply look for nearest enemy */
		nearest = rc.senseEnemyHQLocation();
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
			/* try to attack the nearest if unable than move towards it */
			if (rc.isWeaponReady()) {
				if (rc.canAttackLocation(nearest)) {
					rc.attackLocation(nearest);
				} else {
					/* move towards nearest */
					Move.tryMove(nearest);
				}
			} else {
				if (nearest != rc.senseEnemyHQLocation()) {
					/* if weapon is not ready run from enemy */
					Direction away = rc.getLocation().directionTo(nearest)
							.opposite();
					Move.tryMove(away);
				} else {
					Move.tryMove(nearest);
				}
			}
		} catch (Exception e) {
			System.out.println("Error in Tank Attack behavior action");
			e.printStackTrace();
		}
	}

	public void panicAlert(MapLocation m) {
		// TODO Auto-generated method stub

	}

}
