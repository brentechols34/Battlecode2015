package team163.utils;

import java.util.Arrays;
import java.util.Random;

import battlecode.common.*;

/**
 * Basic bugging algorithm for cheap pathfinding.
 * 
 * @author Alex
 */
public class MBugger {

	public MapLocation start;
	public MapLocation finish;
	public MapLocation closest;
	public boolean reverse;
	public int avoid;

	/**
	 * Code in the following range needs to be modified for specific purposes
	 * 
	 */
	// /////////////////////////////////////////////////////////////////////////
	private boolean[][] map;
	RobotController rc;
	int width;
	int height;
	Random rand;
	
	public MBugger(RobotController rc) {
		map = new boolean[120][120];
		for (boolean[] i : map)
			Arrays.fill(i, true);
		rand = new Random(rc.getID());
		this.closest = null;
		reverse = true;
		this.rc = rc;
		this.width = 120;
		this.height = 120;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * 
	 * @return the position that this agent is at
	 */
	private MapLocation getCurrentPosn() {
		try {
			MapLocation m = rc.getLocation();
			return new MapLocation(m.x, m.y);
		} catch (Exception e) {
			System.out.println("Error in getCurrentPosen" + e);
		}
		return null;
	}



	/**
	 * 
	 * @param p
	 * @return true if the inputted MapLocation is traversable, false otherwise.
	 */
	private boolean isTraversable(int x, int y) {
		try {
			

			// check if off the map
			if (isOOB(x, y)) {
				return false;
			}
			
			MapLocation next = new MapLocation(x, y);
			if (rc.canSenseLocation(next)) {
				RobotInfo ri = rc.senseRobotAtLocation(next);
				if (ri!=null && isStationary(ri.type)) { //&& () ||avoid>0
					return false;
				}
			}

			// if it is an obstacle add obstacle and update map
			if (rc.senseTerrainTile(next) != TerrainTile.NORMAL) {
				return false;
			}

			return true;
		} catch (Exception e) {
			System.out.println("Error in isTraversable()" + e);
		}
		return false;
	}

	static boolean isStationary(RobotType rt) {
		return (rt == RobotType.AEROSPACELAB || rt == RobotType.BARRACKS || rt == RobotType.HELIPAD || rt == RobotType.HQ ||  rt == RobotType.MINERFACTORY || rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY || rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD);
	}

	private boolean isOOB(int x, int y) {
		return (rc.senseTerrainTile(new MapLocation(x, y)) == TerrainTile.OFF_MAP);
	}

	/**
	 * Finds the path from current location to target finish location
	 * 
	 * @param finish
	 */
	public void setTargetLocation(MapLocation finish) {
		this.start = getCurrentPosn();
		this.finish = finish;
		closest = start;
	}

	public void softReset() {
		closest = null;
	}

	public void reset() {
		closest = null;
		start = null;
		finish = null;
	}

	private double isOnLine(MapLocation p) {
		try {
			if(start == null) {
				System.out.println("\n\nStart is null\n\n");
			}
			if(finish == null) {
				System.out.println("\n\nFinish is null\n\n");
			}
			double m = ((double) (finish.y - start.y)) / (finish.x - start.x);
			double b = -m * start.x + start.y;
			double ty = m * p.x + b;
			return Math.abs(ty - p.y);
		} catch (Exception e) {
			System.out.println("Error in isOnline " + e);
		}
		return 0.0;
	}

	/**
	 * As long as this thing is called, and the map does not change, you are
	 * either on the line, or hugging a wall.
	 * 
	 * @return the next position to move to.
	 */
	public MapLocation nextMove() {
		try {	
			MapLocation me = getCurrentPosn();
			MapLocation potential;
			if (isOnLine(me) < 3
					&& (closest == null || finish.distanceSquaredTo(me) < finish.distanceSquaredTo(closest))) {
				// find the next spot that is on the line, return it.
				if ((potential = followLine(me)) != null) {
					return potential;
				}
			}
			// wall hug.
			if ((potential = bug(me)) != null) {
				return potential;
			}			
			//try to move directly to goal, I should end up hitting the real obstacle, or the goal which is good enough
			//I think I have to reset closest but idk
//			closest = null;
//			start = getCurrentPosn();
			return closestToGoal(me);
		} catch (Exception e) {
			System.out.println("Error in nextMove()" + e);
		}
		return null;
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
			temp = moveTo(me, d);
			obs1 = moveTo(me, obs_d);
			obs2 = moveTo(me, obs_d2);
			if (isTraversable(temp.x, temp.y)
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

	public boolean isObstacle(MapLocation p) {
		return !isTraversable(p.x, p.y) && !isOOB(p.x, p.y);
	}

	public MapLocation followLine(MapLocation me) {
		MapLocation potential;
		MapLocation backup = null;
		double dis;
		for (int i = 0; i < 8; i++) {
			potential = moveTo(me, i);

			if(potential == null) {
				System.out.println("\n\npotential was null\n\n");
			}
			dis = isOnLine(potential);
			if (dis < 3
					&& finish.distanceSquaredTo(potential) < me.distanceSquaredTo(finish)) {
				if (isTraversable(potential.x, potential.y)) {
					if ((int) dis != 0) {
						backup = potential;
					} else {
						closest = potential;
						return potential;
					}
				}
			}
		}
		return backup;
	}
	
	public MapLocation closestToGoal(MapLocation from) {
		MapLocation p = null;
		double min = Double.MAX_VALUE;
		MapLocation t;
		double td;
		for (int i = 0; i < 1; i++) {
			t = moveTo(from,i);
			td = distance(t,finish);
			if (td < min && isTraversable(t.x, t.y)) {
				min=td;
				p=t;
			}
		}
		return p;
	}
	
    private static float distance(MapLocation p1, MapLocation p2) {
        final float dx = Math.abs(p1.x - p2.x);
        final float dy = Math.abs(p1.y - p2.y);
        return dx > dy ? (dy * 20f / 70 + dx) : (dx * 20f / 70 + dy);
    }


	private MapLocation moveTo(MapLocation p, int d) {
		switch (d) {
		case 0:
			return new MapLocation(p.x, p.y - 1);
		case 1:
			return new MapLocation(p.x + 1, p.y - 1);
		case 2:
			return new MapLocation(p.x + 1, p.y);
		case 3:
			return new MapLocation(p.x + 1, p.y + 1);
		case 4:
			return new MapLocation(p.x, p.y + 1);
		case 5:
			return new MapLocation(p.x - 1, p.y + 1);
		case 6:
			return new MapLocation(p.x - 1, p.y);
		case 7:
			return new MapLocation(p.x - 1, p.y - 1);
		default:
			return null;
		}
	}

}
