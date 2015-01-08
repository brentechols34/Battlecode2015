package team163.tanks;

import battlecode.common.*;
import team163.utils.*;

/**
 * This eventually may be put in utils depending on how 
 * universal movement interface is.
 * @author jak
 *
 */
public class Move {
	
	/* @NOTE now using tanks rc */
	static RobotController rc = Tank.rc;
	
	/* instantiate movement utils */
	Path p = new Path(new boolean[120][120]);
	MBugger mb = new MBugger(rc, p);
	
	static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	// This method will attempt to move in Direction d (or as close to it as
	// possible)
	static void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = { 0, 1, -1, 2, -2 };
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5
				&& !rc.canMove(directions[(dirint + offsets[offsetIndex] + 8) % 8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5 && rc.isCoreReady()) {
			rc.move(directions[(dirint + offsets[offsetIndex] + 8) % 8]);
		}
	}
	
	static int directionToInt(Direction d) {
		switch (d) {
		case NORTH:
			return 0;
		case NORTH_EAST:
			return 1;
		case EAST:
			return 2;
		case SOUTH_EAST:
			return 3;
		case SOUTH:
			return 4;
		case SOUTH_WEST:
			return 5;
		case WEST:
			return 6;
		case NORTH_WEST:
			return 7;
		default:
			return -1;
		}
	}
}
