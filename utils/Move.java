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

	/* current target end map location */
	static MapLocation curTarget;

	static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST,
			Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
			Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	static public void setRc(RobotController in) {
		Move.rc = in;
		curTarget = rc.senseHQLocation();
		mb = new MBugger(rc);
		mb.finish = new Point(curTarget.x, curTarget.y);
	}

	// This method will attempt to move in Direction d (or as close to it as
	// possible)
	static public void tryMove(Direction d) {
		try {
			set = false;
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
	static public void tryMove(MapLocation m) {
		try {
			if (m.compareTo(curTarget) == 0 || !set) {
				set = true;
				stored = false;
				curTarget = m;
				mb.reset();
				mb.setTargetLocation(new Point(m.x, m.y));
			}
			// If ran into another robot or bugger returned null try just moving
			if (stored) {
				mb.closest = null;
				mb.start = null;
				if ((count++) > rand.nextInt(2)+2) {
					// stuck in a loop reset
					stored = false;
					mb.reset();
					mb.setTargetLocation(new Point(m.x, m.y));
					count = 0;
				} else {
					MapLocation cur = rc.getLocation();
					if (cur.compareTo(store) != 0) {
						tryMove(cur.directionTo(store));
						stored = false;
						return;
					} else {
						mb.reset();
						mb.setTargetLocation(new Point(m.x, m.y));
						stored = false;
					}
				}
				return;
			}

			// try using bugging system
			MapLocation ml = rc.getLocation();
			if (mb.start == null) mb.start = new Point(ml.x,ml.y);
			Point p = mb.nextMove();
			if (p == null) { //idk really how this happens, but whatever
				return;
			}
			MapLocation loc = new MapLocation(p.x, p.y);
			RobotInfo info = rc.senseRobotAtLocation(loc);
			if (info == null) {
				Direction dir = rc.getLocation().directionTo(loc);
				if (rc.isCoreReady() && rc.canMove(dir)) {
					count = 0;
					rc.move(dir);
				} else {
					mb.closest = null;
				}
			} else {
				mb.closest = null;
				mb.start = null;
				stored = true;
				store = loc;
				tryMove(rc.getLocation().directionTo(store));
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
