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

	public Point start;
	public Point finish;
	private int moveCount;
	public Point closest;
	public boolean reverse;
	private Random rand;

	/**
	 * Code in the following range needs to be modified for specific purposes
	 * 
	 */
	// /////////////////////////////////////////////////////////////////////////
	private boolean[][] map;
	RobotController rc;
	int width;
	int height;

	public MBugger(RobotController rc) {
		map = new boolean[120][120];
		for (boolean[] i : map)
			Arrays.fill(i, true);

		this.closest = null;
		reverse = true;
		moveCount = 0;
		this.rc = rc;
		this.width = 120;
		this.height = 120;
		rand = new Random(rc.getID());
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
	private Point getCurrentPosn() {
		try {
			MapLocation m = rc.getLocation();
			return new Point(m.x, m.y);
		} catch (Exception e) {
			System.out.println("Error in getCurrentPosen" + e);
		}
		return null;
	}
	


	/**
	 * 
	 * @param p
	 * @return true if the inputted point is traversable, false otherwise.
	 */
	private boolean isTraversable(int x, int y) {
		try {
			boolean traversable = true;
			Boolean temp = null;
			MapLocation next = new MapLocation(x, y);

			// check if off the map
			if (isOOB(x, y)) {
				return false;
			}
			
			RobotInfo ri = rc.senseRobotAtLocation(next);
			if (ri!=null&& (rand.nextDouble() > .5 || isStationary(ri.type))) {
				return false;
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
	public void setTargetLocation(Point finish) {
		this.start = getCurrentPosn();
		this.finish = finish;
		closest = start;
	}

	public void reset() {
		closest = null;
		start = null;
		finish = null;
		moveCount = 0;
	}

	private double isOnLine(Point p) {
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
	public Point nextMove() {
		try {
			moveCount++;
			Point me = getCurrentPosn();
			Point potential;
			if (isOnLine(me) < 3
					&& (closest == null || me.distance(finish) <= finish.distance(closest))) {
				// find the next spot that is on the line, return it.
				if ((potential = followLine(me)) != null) {
					return potential;
				}
			}
			// wall hug.
			if ((potential = bug(me)) != null) {
				return potential;
			}
		} catch (Exception e) {
			System.out.println("Error in nextMove()" + e);
		}
		// if it gets here, there are major problems.
		// just in case, we'll modify the move count.
		reverse = !reverse;
		moveCount--;
		closest=null;
		return null;
	}

	private boolean recursed;

	public Point bug(Point me) {
		Point temp;
		Point obs1;
		Point obs2;
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

	public boolean isObstacle(Point p) {
		return !isTraversable(p.x, p.y) && !isOOB(p.x, p.y);
	}

	public Point followLine(Point me) {
		Point potential;
		Point backup = null;
		double dis;
		for (int i = 0; i < 8; i++) {
			potential = moveTo(me, i);
			
			if(potential == null) {
				System.out.println("\n\npotential was null\n\n");
			}
			dis = isOnLine(potential);
			if (dis < 2
					&& finish.distance(potential) < me.distance(finish)) {
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

	private Point moveTo(Point p, int d) {
		switch (d) {
		case 0:
			return new Point(p.x, p.y - 1);
		case 1:
			return new Point(p.x + 1, p.y - 1);
		case 2:
			return new Point(p.x + 1, p.y);
		case 3:
			return new Point(p.x + 1, p.y + 1);
		case 4:
			return new Point(p.x, p.y + 1);
		case 5:
			return new Point(p.x - 1, p.y + 1);
		case 6:
			return new Point(p.x - 1, p.y);
		case 7:
			return new Point(p.x - 1, p.y - 1);
		default:
			return null;
		}
	}

	public int getMoveCount() {
		return moveCount;
	}

	public double pathRatio() {
		if (moveCount == 0) {
			return 0;
		} else {
			return ((double) moveCount) / Point.manhattan(start, finish);
		}
	}

}
