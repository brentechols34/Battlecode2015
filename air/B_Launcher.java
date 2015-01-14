package team163.air;

import team163.utils.Move;
import battlecode.common.*;

public class B_Launcher implements Behavior {
	public static RobotController rc = Launcher.rc;
	short toBrodcast[];
	int senseRange = 24;
	int index = 0;

	RobotInfo[] nearRobots;
	RobotInfo[] enemies;

	public void perception() {
		/* sense everything within range */
		nearRobots = rc.senseNearbyRobots(senseRange);
		enemies = rc.senseNearbyRobots(senseRange, Launcher.team.opponent());
	}


	public void calculation() {
		try {

		} catch (Exception e) {
			System.out.println("Error in Launcher calculation");
		}
	}


	public void action() {
		try {
			if (enemies.length > 1) {
				Direction dir = rc.getLocation().directionTo(
						enemies[0].location);
				if (rc.getMissileCount() > 0 && rc.canLaunch(dir)) {
					rc.launchMissile(dir);
				} else {
					/* run away if can not fire */
					Move.tryMove(enemies[0].location.directionTo(rc
							.getLocation()));
				}
			} else {
				if (rc.isCoreReady()) {
					Move.tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
				}
			}
		} catch (Exception e) {
			System.out.println("Error in Launcher action");
			e.printStackTrace();
		}
	}

	public void panicAlert(MapLocation m) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
