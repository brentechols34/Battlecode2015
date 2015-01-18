package team163.utils;
import java.util.Random;

import battlecode.common.*;

public class BasicBugger {

	private RobotController rc;
	private boolean reverse;
	public MapLocation goal;
	private MapLocation closest;
	private MapLocation hugStart;
	private MapLocation hugEnd;
	private boolean hugging;
	private final Random rand;

	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

	public BasicBugger(RobotController rc) {
		this.rc = rc;
		reverse = false;
		rand = new Random(rc.getID());
	}

	public void setDestination(MapLocation m) {
		this.goal = m;
		this.closest = null;
		this.hugging = false;
	}

	public void attemptMove() throws GameActionException {
		MapLocation me = rc.getLocation();
		Direction d = me.directionTo(goal);
		if (!rc.isCoreReady()) return;

		if (hugging  && me.equals(hugEnd)) { //If I'm at the end of my rope
			hugging = false;
			hugStart = null;
			closest = null;
		}
		if (!hugging) { //I've not activated my trap card
			rc.setIndicatorString(0, "Not bugging.");
			if (impassable(me.add(d))) {
				hugging = true; //activate trap card
				hugStart = me;
				closest = me;
			} else {
				if (rc.canMove(d)) {
					rc.move(d);
					return;
				}
			}
		}
		if (hugging) { //my trap card has been activated
			MapLocation dune = bug(me); //let's bug
			if (dune == null) { //If I'm not touching anything, try to run straight towards goal, terrible strat
				hugging = false;
				hugStart = null;
				hugEnd = null;
				closest = null;
				if (rc.canMove(d)) {
					rc.move(d);
					return;
				}
			} else {
				Direction to = me.directionTo(dune);
				if (rc.canMove(to)) {
					if (dune.distanceSquaredTo(goal) < goal.distanceSquaredTo(closest)) { //if I'm closer than before, remember here
						closest = dune;
					}
					if (dune.equals(hugStart)) { //if I'm back to where I started, bug to closest
						hugEnd = closest;
					}
					rc.move(to);
				}
			}
		}
	}

	private boolean recursed;

	public MapLocation bug(MapLocation me) {
		MapLocation temp;
		MapLocation obs1;
		MapLocation obs2;
		for (int i = 0; i != 8; i++) {
			int d = (reverse) ? (i + 4) % 8 : i;
			int obs_d = (d + (((reverse) ? -1 : 1) * (2 - d % 2)) + 8) % 8;
			int obs_d2 = (obs_d + ((reverse) ? 1 : -1) + 8) % 8;
			temp = me.add(directions[d]);
			obs1 = me.add(directions[obs_d]);
			obs2 = me.add(directions[obs_d2]);
			if (!impassable(temp)
					&& (isObstacle(obs1) || (d % 2 == 0 && isObstacle(obs2)))) {
				recursed = false;
				return temp;
			}

		}
		if (recursed) {
			return null;
		}
		recursed = true;
		reverse = !reverse;
		return bug(me);
	}
	
	public boolean impassable(MapLocation m) {
		TerrainTile tt = rc.senseTerrainTile(m);
		return isObstacle(m) || tt == TerrainTile.OFF_MAP;
	}

	public boolean isObstacle(MapLocation m) {
		try {
			TerrainTile tt = rc.senseTerrainTile(m);
			if (tt == TerrainTile.VOID) return true;
			if (rc.canSenseLocation(m)) {
				RobotInfo ri = rc.senseRobotAtLocation(m);
				if (ri != null && (rand.nextDouble() > .8 || isStationary(ri.type))) return true; //
			}
			return false;
		} catch(GameActionException e) {
			return impassable(m);
		}
	}

	public static boolean isStationary(RobotType rt) {
		return (rt != null && (rt == RobotType.AEROSPACELAB
				|| rt == RobotType.BARRACKS || rt == RobotType.HELIPAD
				|| rt == RobotType.HQ || rt == RobotType.MINERFACTORY
				|| rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY
				|| rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD));
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
	
	/**
	 * Bresenham's Line algorithm
	 * @param p1
	 * @param p2
	 * @return True if the two given locations have nothing impassable between them, false otherwise
	 * @throws GameActionException
	 */
	public boolean scan(MapLocation p1, MapLocation p2) throws GameActionException {
		if (p1.isAdjacentTo(p2)) {
			return false;
		}
		int x1 = p1.x;
		int y1 = p1.y;
		int x2 = p2.x;
		int y2 = p2.y;
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int sx = (x1 < x2) ? 1 : -1;
		int sy = (y1 < y2) ? 1 : -1;
		int err = dx - dy;
		while (true) {
			int e2 = err << 1;
			if (e2 > -dy) {
				err = err - dy;
				x1 = x1 + sx;
			}
			if (x1 == x2 && y1 == y2) {
				break;
			}
			if (impassable(new MapLocation(x1,y1))) {
				return true;
			}
			if (e2 < dx) {
				err = err + dx;
				y1 = y1 + sy;
			}
			if (x1 == x2 && y1 == y2) {
				break;
			}
			if (impassable(new MapLocation(x1,y1))) {
				return true;
			}
		}
		return false;
	}





}
