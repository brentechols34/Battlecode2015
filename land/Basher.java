/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.land;

import team163.utils.BasicBugger;
import team163.utils.PathMove;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

/**
 *
 * @author Alex
 */
public class Basher {

	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};


	static PathMove pm;
	static BasicBugger bb;
	static RobotController rc;
	static Team myTeam;
	static Team enemyTeam;

	static MapLocation me;

	static MapLocation goal;
	static MapLocation rally;

	public static void run(RobotController rc) {
		pm = new PathMove(rc);
		bb = new BasicBugger(rc);
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		Basher.rc = rc;
		while (true) {
			try {
				me = rc.getLocation();

				int x = rc.readBroadcast(67);
				int y = rc.readBroadcast(68);
				goal = new MapLocation(x,y);
				x = rc.readBroadcast(50);
				y = rc.readBroadcast(51);
				rally = new MapLocation(x,y);
				if (rc.readBroadcast(66) == 1) {
					if (!pm.goal.equals(goal)) pm.setDestination(goal);
				} else {
					if (!pm.goal.equals(rally)) pm.setDestination(rally);
				}

				yolo();

				rc.yield();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}

	public static void yolo() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.BASHER.sensorRadiusSquared, enemyTeam);
		//maximize enemies adjacent
		int[] counts = new int[8];
		MapLocation closest = null;
		int dis = Integer.MAX_VALUE;
		int maxCount = 0;
		for (RobotInfo r : enemies) {
			if (r.type != RobotType.TOWER && r.type != RobotType.HQ) {
				int t_dis = me.distanceSquaredTo(r.location);
				if (t_dis < dis || closest == null) {
					dis = t_dis;
					closest = r.location;
				}
				Direction d = me.directionTo(r.location);
				if (me.add(d).isAdjacentTo(r.location)) {
					int di = directionToInt(d);
					counts[di]++;
					if (counts[di] > counts[maxCount]) maxCount = di;
				}
			}
		}
		if (counts[maxCount] == 0) { //if the best direction puts me towards enemies
			if (closest != null) {
				Direction toClosest = me.directionTo(closest);
				if (rc.canMove(toClosest)) rc.move(toClosest);
			} else {
				RobotInfo[] allies = rc.senseNearbyRobots(RobotType.BASHER.sensorRadiusSquared, myTeam);
				MapLocation closestLauncher = null;
				dis = Integer.MAX_VALUE;
				for (RobotInfo ally : allies) {
					if (ally.type == RobotType.LAUNCHER) {
						int t_dis = me.distanceSquaredTo(ally.location);
						if (t_dis < dis) {
							dis = t_dis;
							closestLauncher = ally.location;
						}
					}
				}
				if (dis < 10) {
					Direction d = me.directionTo(closestLauncher).opposite();
					if (rc.canMove(d)) {
						rc.move(d);
					}
				} else pm.attemptMove();
			}

		} else {
			if (rc.canMove(directions[maxCount])) {
				rc.move(directions[maxCount]);
			}
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
