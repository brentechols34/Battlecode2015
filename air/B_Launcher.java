package team163.air;

import team163.utils.Move;
import battlecode.common.*;

public class B_Launcher implements Behavior {
	public static RobotController rc;
	boolean map[][] = new boolean[1000][1000]; // HQ in the center
	boolean explored[][] = new boolean[1000][1000]; // HQ in the center
	short toBrodcast[];
	int senseRange = 24;
	int index = 0;

	RobotInfo[] nearRobots;
	RobotInfo[] enemies;

	public void setRc(RobotController in) {
		B_Launcher.rc = in;
	}


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
			// for (int i = 0; i < index; i++) {
			// rc.broadcast(((chan++) % 100), toBrodcast[i]);
			// }

			if (enemies.length > 1) {
				Direction dir = rc.getLocation().directionTo(
						enemies[0].location);
				if (rc.getMissileCount() > 0 && rc.canLaunch(dir)) {
					rc.launchMissile(dir);
					/* get the missiles channel and broadcast target */
					rc.yield();
					RobotInfo missle = rc.senseRobotAtLocation(rc.getLocation().add(dir));
					if(missle == null) {
						System.out.println("missile was null");
						return;
					}
					int channel = (missle.ID %100) + 600;
					rc.broadcast(channel, enemies[0].location.x);
					rc.broadcast(channel+1, enemies[0].location.y);
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
