package team163;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Helipad {
	static Random rand;
	static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST,
			Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
			Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	public static void run(RobotController rc) {
		try {
			rand = new Random();
			while (true) {
				if (rc.isCoreReady() && rc.getTeamOre() >= 125
						&& rand.nextBoolean()) {
					RobotPlayer.trySpawn(directions[rand.nextInt(8)],
							RobotType.DRONE);
				}
				
				rc.yield();
			}
		} catch (Exception e) {
			System.out.println("Helipad Exception");
			e.printStackTrace();
		}
	}
}
