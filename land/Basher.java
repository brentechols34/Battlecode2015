/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.land;

import team163.air.Launcher;
import team163.logistics.SupplyDrone;
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

	static int resupplyChannel = 0;
	static PathMove pm;
	static BasicBugger bb;
	static RobotController rc;
	static Team myTeam;
	static Team enemyTeam;

	static MapLocation me;

	static MapLocation goal;
	static MapLocation rally;

	static boolean attacking;

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
				attacking = (rc.readBroadcast(66) == 1);
				if (attacking) {
					if (!goal.equals(pm.goal)) pm.setDestination(goal);
				} else {
					if (!rally.equals(pm.goal)) pm.setDestination(rally);
				}

				if (rc.isCoreReady()) yolo();
				requestSupply();

				rc.yield();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}


	static int backCount = 0;
	public static void yolo() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.BASHER.sensorRadiusSquared, enemyTeam);
		//maximize enemies adjacent
		int[] counts = new int[8];
		MapLocation closest = null;
		int dis = Integer.MAX_VALUE;
		int maxCount = 0;
		if (!attacking) {
			if (me.distanceSquaredTo(rally) > 50) { //keep kinda close to rally until we attack
				backCount=5;
			}
			if (backCount > 0 && rc.getHealth() > RobotType.BASHER.maxHealth/2) {
				pm.attemptMove();
				backCount--;
			}
		}
		for (RobotInfo r : enemies) {
			//if (r.type != RobotType.TOWER && r.type != RobotType.HQ && r.location.distanceSquaredTo(goal) > 24) {
				int t_dis = me.distanceSquaredTo(r.location);
				if ((t_dis < dis || closest == null)) {
					dis = t_dis;
					closest = r.location;
				}
				Direction d = me.directionTo(r.location);
				if (me.add(d).isAdjacentTo(r.location)) {
					int di = directionToInt(d);
					counts[di]++;
					if (counts[di] > counts[maxCount]) maxCount = di;
				}
			//}
		}
		if (counts[maxCount] == 0) { //if the best direction puts me towards no one
			if (closest != null) {
				Direction toClosest = me.directionTo(closest);
				if (rc.canMove(toClosest) && rc.isCoreReady()) rc.move(toClosest);
			} else {
				if (!attacking || me.distanceSquaredTo(goal) > 35) pm.attemptMove();
				else { //I'm attacking and I'm close to the goal


				}
			}

		} else {
			if (rc.canMove(directions[maxCount]) && rc.isCoreReady()) {
				rc.move(directions[maxCount]);
			}
		}
	}

	static void requestSupply() throws GameActionException {
		if (rc.getSupplyLevel() < 100) {
			resupplyChannel = SupplyDrone.requestResupply(rc, rc.getLocation(), resupplyChannel);
		} else {
			resupplyChannel = 0;
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
