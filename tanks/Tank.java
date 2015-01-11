package team163.tanks;

import team163.utils.Move;
import battlecode.common.*;

public class Tank {
	static RobotController rc;
	static Behavior mood; /* current behavior */
	static int range;
    static int senseRange = 24;
	static Team team;

	static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST,
			Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
			Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	public static void run(RobotController rc) {
		try {
			Tank.rc = rc;
			Tank.range = rc.getType().attackRadiusSquared;
			Tank.team = rc.getTeam();

			//Move.setRc(rc);

			mood = new B_Turtle(); /* starting behavior of turtling */
			while (true) {

				/* get behavior */
				mood = chooseB();

				/* perform round */
				mood.perception();
				mood.calculation();
				mood.action();

				/* end round */
				Tank.rc.yield();
			}
		} catch (Exception e) {
			System.out.println("Tank Exception");
			e.printStackTrace();
		}
	}

	/**
	 * Logic to choose which behavior to use -- this could get large and require
	 * refactoring...(source of technical debt)
	 * 
	 * @return Behavior interface
	 */
	private static Behavior chooseB() {
		try {
			/* if more than 10 tanks trigger aggressive behavior */
			if (rc.readBroadcast(66) == 1) {
				mood = new B_Attack();
			} else {
				mood = new B_Turtle();
			}

			/* if mood has not been altered than current mood is kept */
			return mood;
		} catch (Exception e) {
			System.out.println("Error caught in choosing tank behavior");
		}
		return mood; /* if error happens use current mood */
	}
}
