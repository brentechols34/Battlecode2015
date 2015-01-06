package team163;

import java.util.Random;

import team163.utils.AttackUtils;
import battlecode.common.Direction;
import battlecode.common.RobotController;

public class Drone {
	private static boolean isAttacking;	
	static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };


	public static void run(RobotController rc) {
		try {
			Random rand = new Random();
			while (true) {
				if (rc.isWeaponReady()) {
					AttackUtils.attackSomething(rc,
							rc.getType().attackRadiusSquared, rc.getTeam()
									.opponent());
				}
				if (rc.getCoreDelay() > 0) {
					rc.yield();
				}

				if (rc.readBroadcast(3) > 15) {
					isAttacking = true;
				}
				if (rc.readBroadcast(3) > 10 && isAttacking) {
					RobotPlayer.tryMove(rc.getLocation().directionTo(
							rc.senseEnemyHQLocation()));
				} else {
					/* move random */
					RobotPlayer.tryMove(directions[rand.nextInt(8)]);
				}
				rc.yield();
			}
		} catch (Exception e) {
			System.out.println("Drone Exception");
			e.printStackTrace();
		}
	}
}
