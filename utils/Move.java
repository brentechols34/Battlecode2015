package team163.utils;

import java.util.Random;

import battlecode.common.*;

/**
 * 
 * @author jak
 * 
 */
public class Move {

	/* @NOTE first and one time, rc must be SET!!! */
	static RobotController rc;
	static int count = 0;
	static MapLocation store;
	static boolean stored = false;
	static boolean set = false;
	static int persistance = 0;

	/* instantiate movement utils */
	
	static MBugger mb;
	static Random rand = new Random();

	static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST,
			Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
			Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	static public void setRc(RobotController in) {
		Move.rc = in;
		mb = new MBugger(rc);
	}

	// This method will attempt to move in Direction d (or as close to it as
	// possible)
	static public void tryMove(Direction d) {
		try {
			set = false;
			int offsetIndex = 0;
			int[] offsets = { 0, 1, -1, 2, -2 };
			int dirint = directionToInt(d);
			while (offsetIndex < 5
					&& !rc.canMove(directions[(dirint + offsets[offsetIndex] + 8) % 8])) {
				offsetIndex++;
			}
			if (offsetIndex < 5 && rc.isCoreReady()) {
				rc.move(directions[(dirint + offsets[offsetIndex] + 8) % 8]);
			}
		} catch (Exception e) {
			System.out.println("Error in tryMove");
		}
	}

	static public void randMove() {
		tryMove(directions[rand.nextInt(8)]);
		set = false;
	}

	/**
	 * Try moving using the bugger High of 2360 bytecode Low of 560 bytecode
	 * Average of about 1400 bytecode
	 * 
	 * @param m
	 *            end target map location
	 */
	static MapLocation last;
	static public void tryMove(MapLocation m) {
		rc.setIndicatorString(0, "Trying to bug: should not see this.");
		if (m.equals(rc.getLocation())) {
			rc.setIndicatorString(0, "Already at location.");
			return;
		}
		try {
			//if (!rc.isCoreReady()) return;
			MapLocation ml = rc.getLocation();
			if (!set || !m.equals(last) || count > 3) { //  || rand.nextDouble() > .90
				last = m;
				set = true;
				mb.reset();
				mb.setTargetLocation(new Point(m.x, m.y));
				mb.start = new Point(ml.x,ml.y);
				if (count > 3) {
					//robot avoid for like 10 rounds
					mb.avoid = 10;
					count = 0;
				}
				
			}
			if (!rc.isCoreReady()) {
				rc.setIndicatorString(0, "");
				return;
			}
			// try using bugging system
			Point p = mb.nextMove();
			
			if (p!=null) {
				MapLocation loc = new MapLocation(p.x, p.y);
				Direction dir = ml.directionTo(loc);
				if (rc.canMove(dir)) {
					rc.setIndicatorString(0,"Bug success " + dir);
					mb.avoid--;
					rc.move(dir);
				} else {
					rc.setIndicatorString(0,"Tried to move: " + dir);
					mb.avoid++;
					count++;
					mb.closest = null;
				}
			} else {
				rc.setIndicatorString(0,"Bugger returned null.");
			}
			
		} catch (Exception e) {
			System.out.println("Error attempting bugging");
			e.printStackTrace();
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
