/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import battlecode.common.*;

import java.util.Random;

import team163.utils.BasicBugger;
import team163.utils.Move;
import team163.utils.PathMove;
import team163.utils.StratController;

/**
 *
 * @author sweetness
 */
public class Beaver {

	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

	static RobotController rc;
	static Team myTeam;
	static Team enemyTeam;
	static int myRange;
	static Random rand;
	static int lifetime = 0;
	static MapLocation bestLoc;
	static int bestVal;
	static boolean wasBest = false;
	static int wasBest_count = 0;
	static MapLocation myLoc;
	static int oreHere;
	static double myHealth;
	static RobotInfo[] enemies;
	static BasicBugger bb;

	//pathfinding for building
	static PathMove panther;

	public static void run(RobotController rc) {
		Beaver.rc = rc;
		rand = new Random(rc.getID());
		myRange = rc.getType().attackRadiusSquared;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		myHealth = rc.getHealth();
		// check if saved in memory to go with air attack
		panther = new PathMove(rc);
		bb = new BasicBugger(rc);
		try {
			if (rc.readBroadcast(8) > 2) {
				SupplyBeaver.run(rc);
			}
		} catch (Exception e) {
			System.out.println("Tried to be a path or supply beaver, but I failed");
		}

		while (true) {
			try {
				lifetime++;
				myLoc = rc.getLocation();
				if (rc.isWeaponReady()) {
					attackSomething();
				}

//				//send panic on being attacked
//				enemies = rc.senseNearbyRobots(24, enemyTeam);
//				double curHealth = rc.getHealth();
//				if (curHealth < myHealth) {
//					myHealth = curHealth;
//					rc.broadcast(911, myLoc.x);
//					rc.broadcast(912, myLoc.y);
//					Move.tryMove(rc.senseHQLocation());
//					continue;
//				}
//
//				//run from enemies
//				if (enemies.length > 0) {
//					Move.tryMove(rc.senseHQLocation());
//					continue;
//				}

				oreHere = (int) (rc.senseOre(myLoc) + .5);
				bestVal = rc.readBroadcast(1000);
				bestLoc = new MapLocation(rc.readBroadcast(1001),
						rc.readBroadcast(1002));

				buildStuff();
				if (rc.isCoreReady()) {
					if (oreHere > bestVal) {
						rc.broadcast(1000, oreHere);
						rc.broadcast(1001, myLoc.x);
						rc.broadcast(1002, myLoc.y);
					}
					defaultMove();
				}
				rc.yield();
			} catch (Exception e) {
				System.out.println("Beaver Exception");
				e.printStackTrace();
			}
		}
	}

	static boolean isStationary(RobotType rt) {
		return (rt != null && rt == RobotType.AEROSPACELAB
				|| rt == RobotType.BARRACKS || rt == RobotType.HELIPAD
				|| rt == RobotType.HQ || rt == RobotType.MINERFACTORY
				|| rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY
				|| rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD);
	}

	static void defaultMove() throws GameActionException {
		if (panther.goal.isAdjacentTo(myLoc)) {
			RobotType toMake = StratController.toMake(rc);
			Direction dir = myLoc.directionTo(panther.goal);
			if (toMake!=null && rc.canBuild(dir, toMake)) {
				rc.build(myLoc.directionTo(panther.goal),toMake);
			}
		} else panther.attemptMove();
	}

	static void buildStuff() throws GameActionException {
		if (panther.goal==null || !StratController.shouldBuildHere(rc, panther.goal)) {
			panther.setDestination(StratController.findBuildLocation(rc));
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

	// This method will attack an enemy in sight, if there is one
	static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
		if (enemies.length > 0) {
			rc.attackLocation(enemies[0].location);
		}
	}

	// This method will attempt to build in the given direction (or as close to
	// it as possible)
	static boolean tryBuild(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0, 1, -1, 2, -2, 3, -3, 4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8
				&& !rc.canMove(directions[(dirint + offsets[offsetIndex] + 8) % 8])) {
			offsetIndex++;
		}
		if (offsetIndex < 8 && rc.isCoreReady()) {
			rc.build(directions[(dirint + offsets[offsetIndex] + 8) % 8], type);
			return true;
		}
		return false;
	}

}
