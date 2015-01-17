/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import java.util.Random;

import battlecode.common.*;
import team163.utils.Move;
import team163.utils.PathMove;

/**
 *
 * @author sweetness
 */
public class Miner {

	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

	static RobotController rc;
	static Team myTeam;
	static Team enemyTeam;
	static int myRange;
	static Random rand;
	static MapLocation myLoc;
	static int oreHere;
	static int bestVal;
	static MapLocation bestLoc;
	static int lifetime = 0;
	static double myHealth;
	static RobotInfo[] enemies;
    static PathMove pathMove;
    static MapLocation targetLoc;

	static int resupplyChannel = 0;

	static final boolean TESTING_MINING = false;
	static final int ORE_CHANNEL = 10000;
	static final int SUPPLY_THRESHOLD = 500;

	public static void run(RobotController rc) {
		Miner.rc = rc;
		rand = new Random(rc.getID());
		myRange = rc.getType().attackRadiusSquared;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		myHealth = rc.getHealth();
		State state = new Mining();
        pathMove = new PathMove(rc);

		while (true) {
			try {
				lifetime++;
				myLoc = rc.getLocation();
				oreHere = (int) (rc.senseOre(myLoc) +.5);
				bestVal = rc.readBroadcast(1000);
				bestLoc = new MapLocation(rc.readBroadcast(1001), rc.readBroadcast(1002));
				enemies = rc.senseNearbyRobots(24, Miner.enemyTeam);

				// Update the most lucrative position's ore
				if (myLoc.x == rc.readBroadcast(1001) && myLoc.y == rc.readBroadcast(1002)) {
					rc.broadcast(1000, (int) (rc.senseOre(myLoc)+.5));
				}

				// Run the current state
				state = state.run(rc);

				if (TESTING_MINING && (Clock.getRoundNum() == 1000 || Clock.getRoundNum() == 1999)) {
					System.out.println("Ore Extracted: " + rc.readBroadcast(ORE_CHANNEL));
				}

				rc.yield();
			} catch (Exception e) {
				System.out.println("Miner Exception");
				e.printStackTrace();
			}
		}
	}

	static boolean goingToBest;
	static void defaultMove() throws GameActionException {

        // Logic to decongest and remove walls of people
        RobotInfo[] friends = rc.senseNearbyRobots(myRange, myTeam);
        for (int i = 0; i < friends.length; i++) {
            RobotInfo friend = friends[i];

            if (friend.type == RobotType.MINER && rc.senseOre(friend.location) < 3 && rc.senseOre(myLoc.add(friend.location.directionTo(myLoc))) > 3) {
                Move.tryMove(friend.location.directionTo(myLoc));
            }
        }


		if (rc.isCoreReady() && rc.canMine() && oreHere > 1) {
			rc.setIndicatorString(1, "Mining");
			MineHere();
		} else {
			Direction d = findSpot();
			if (d == Direction.NONE) {
                rc.setIndicatorString(1, "Moving to best: " + bestLoc);
                Move.tryMove(bestLoc);
			} else if (rc.isCoreReady() && rc.canMove(d)) {
				rc.setIndicatorString(1, "Moving nearby: " + d);
                Move.tryMove(d);
			}
		}
	}

	public static Direction findSpot() throws GameActionException {
		double bestFound = 3;
		Direction[] counts = new Direction[9];
		counts[0] = Direction.NONE;
        int count = 1;

		for (int i = 0; i < 8; i++) {
			double oreHere = rc.senseOre(myLoc.add(directions[i]));
			if (bestFound < oreHere && rc.canMove(directions[i])) {
                count = 1;

				counts[0] = directions[i];
				bestFound = oreHere;
			} else if (bestFound == oreHere) {
                counts[count++] = directions[i];
            }
		}

		return counts[rand.nextInt(count)];
	}


	static void MineHere () throws GameActionException {
		if (TESTING_MINING) {
			int extracted = (int)(Math.max(Math.min(3, oreHere/4),0.2) * 10);
			rc.broadcast(ORE_CHANNEL, rc.readBroadcast(ORE_CHANNEL) + extracted);
		}

		rc.mine();
	}
}

interface State {
	State run (RobotController rc) throws GameActionException;
}

//State for when the miner is just mining along
class Mining implements State {
	public State run (RobotController rc) throws GameActionException {
		rc.setIndicatorString(0, "Mining");

		// Check our supply level, and put in a request
		if (rc.getSupplyLevel() < Miner.SUPPLY_THRESHOLD && this.requestSupply(rc)) {
			return new Resupplying();
		} else if (rc.getSupplyLevel() > Miner.SUPPLY_THRESHOLD) {
			Miner.resupplyChannel = 0;
		}

		//Check if we need to start retreating
		double curHealth = rc.getHealth();
		if (Miner.enemies.length > 0 || curHealth < Miner.myHealth) {
			Miner.myHealth = curHealth;

			//Broadcast out our position
			rc.broadcast(911, Miner.myLoc.x);
			rc.broadcast(912, Miner.myLoc.y);

			State retreat = new Retreating();
			retreat.run(rc);

			return retreat;
		}

		//Otherwise just default move
		if (rc.isCoreReady()) {
			if (Miner.oreHere > Miner.bestVal) {
				rc.broadcast(1000, Miner.oreHere);
				rc.broadcast(1001, Miner.myLoc.x);
				rc.broadcast(1002, Miner.myLoc.y);
			}
			Miner.defaultMove();
		}

		return this;
	}

	boolean requestSupply (RobotController rc) throws GameActionException {
		Miner.resupplyChannel = SupplyBeaver.requestResupply(rc, rc.getLocation(), Miner.resupplyChannel);

		int head = rc.readBroadcast(196);
		MapLocation beaverLoc = new MapLocation(rc.readBroadcast(198), rc.readBroadcast(199));

		//If we are close, and are the resupply target, than trigger a resupply
		return head == Miner.resupplyChannel && beaverLoc.distanceSquaredTo(rc.getLocation()) < GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED + 5;
	}
}


//State for when the miner is close to a beaver, and awaiting a resupply
class Resupplying implements State {
	int roundsWaited = 0;

	public State run (RobotController rc) throws GameActionException {
		rc.setIndicatorString(0, "Resupplying on channel " + Miner.resupplyChannel);

		//Check if we need to start retreating
		double curHealth = rc.getHealth();
		if (Miner.enemies.length > 0 || curHealth < Miner.myHealth) {
			State retreat = new Retreating();
			retreat.run(rc);

			return retreat;
		}

		//Just relax and mine until we are resupplied
		if (rc.isCoreReady() && rc.canMine() && Miner.oreHere > 0) {
			Miner.MineHere();
		}

		//If the resupply hasn't come yet, it probably isnt' coming so just go back to mining
		roundsWaited++;
		if (roundsWaited == 10) {
			return new Mining();
		}

		//Transition to Mining once we are resupplied
		if (rc.getSupplyLevel() > Miner.SUPPLY_THRESHOLD) {
			return new Mining();
		}

		return this;
	}
}


//State for when we are running away!
class Retreating implements State {
	int roundsSinceEnemy = 0;

	public State run (RobotController rc) throws GameActionException {
		rc.setIndicatorString(0, "Retreating");

		//Move towards the HQ
		Move.tryMove(rc.senseHQLocation());

		//send panic on being attacked
		double curHealth = rc.getHealth();
		if (curHealth < Miner.myHealth) {
			Miner.myHealth = curHealth;

			//Broadcast out our position
			rc.broadcast(911, Miner.myLoc.x);
			rc.broadcast(912, Miner.myLoc.y);
			return this;
		}

		//run from enemies
		if (Miner.enemies.length == 0) {
			roundsSinceEnemy++;
		}

		//We are probably safe now
		if (roundsSinceEnemy == 5) {
			return new Mining();
		}

		return this;
	}
}

