package team163.utils;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class StratController {

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

	public static boolean shouldBuildHere(RobotController rc) {
		MapLocation m = rc.getLocation();
		int count = 0;
		for (Direction d : directions) {
			MapLocation m2 = m.add(d);
			try {
				if (rc.senseTerrainTile(m2) == TerrainTile.NORMAL && rc.senseRobotAtLocation(m2) == null) count++;
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
		if (count < 4) return false;
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		for (MapLocation m2 : enemyTowers) {
			if (m.distanceSquaredTo(m2) < 40) return false;
		}
		if (m.distanceSquaredTo(rc.senseEnemyHQLocation()) < 40) return false;
		return true;	
	}
	
	
	
	
}
