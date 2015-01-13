/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import java.util.Random;

import battlecode.common.*;
import team163.utils.Move;

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

    static int resupplyChannel = 0;

    static final boolean TESTING_MINING = false;
    static final int ORE_CHANNEL = 10000;
    static final int SUPPLY_THRESHOLD = 500;
    static final boolean IS_SUPPLYING = true;
	
	public static void run(RobotController rc) {
        Miner.rc = rc;
        rand = new Random(rc.getID());
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myHealth = rc.getHealth();

        while (true) {
            try {
            	lifetime++;
            	myLoc = rc.getLocation();
                oreHere = (int) (rc.senseOre(myLoc) +.5);
            	bestVal = rc.readBroadcast(1000);
            	bestLoc = new MapLocation(rc.readBroadcast(1001), rc.readBroadcast(1002));

                 //send panic on being attacked
                enemies = rc.senseNearbyRobots(24, enemyTeam);
                double curHealth = rc.getHealth();
                if (curHealth < myHealth) {
                    myHealth = curHealth;
                    rc.broadcast(911, myLoc.x);
                    rc.broadcast(912, myLoc.y);
                    Move.tryMove(rc.senseHQLocation());
                    continue;
                }
                
                //run from enemies
                if (enemies.length > 0) {
                    Move.tryMove(rc.senseHQLocation());
                    continue;
                }
                
                requestSupply();

            	if (rc.isCoreReady()) {
            		if (oreHere > bestVal) {
            			rc.broadcast(1000, oreHere);
            			rc.broadcast(1001, myLoc.x);
            			rc.broadcast(1002, myLoc.y);
            		}
        			defaultMove();
        			rc.yield();
                }

                if (TESTING_MINING && Clock.getRoundNum() == 1000) {
                    System.out.println("Ore Extracted: " + rc.readBroadcast(ORE_CHANNEL));
                }
                if (TESTING_MINING && Clock.getRoundNum() == 1999) {
                    System.out.println("Ore Extracted: " + rc.readBroadcast(ORE_CHANNEL));
                }
            } catch (Exception e) {
                System.out.println("Miner Exception");
                e.printStackTrace();
            }
        }
    }
	
	static void defaultMove() throws GameActionException {
		Direction d = findSpot();
		if (myLoc.x == rc.readBroadcast(1001) && myLoc.y == rc.readBroadcast(1002)) {
			rc.broadcast(1000, (int) (rc.senseOre(myLoc)+.5));
		}
		if (rc.isCoreReady()) {
        	if (oreHere < 3 && bestVal > 3) Move.tryMove(bestLoc);
        	if (rc.isCoreReady() && rc.canMine() && oreHere > 3) {
                if (TESTING_MINING) {
                    int extracted = (int)(Math.max(Math.min(3, oreHere/4),0.2) * 10);
                    rc.broadcast(ORE_CHANNEL, rc.readBroadcast(ORE_CHANNEL) + extracted);
                }

                rc.mine();
            }
        	else if (rc.isCoreReady() && rc.canMove(d)) {
    			rc.move(d);
    			return;
    		}
		}
	}

    static void goSupply() throws GameActionException {
        if(myLoc.distanceSquaredTo(rc.senseHQLocation()) < 15) {
            if (rc.canMine() && oreHere > 0) {
                if (TESTING_MINING) {
                    int extracted = (int)(Math.max(Math.min(3, oreHere/4),0.2) * 10);
                    rc.broadcast(ORE_CHANNEL, rc.readBroadcast(ORE_CHANNEL) + extracted);
                }

                rc.mine();
            }

            return;
        }

        Direction d = myLoc.directionTo(rc.senseHQLocation());
        if (rc.canMove(d)) {
            Move.tryMove(d);
            return;
        }
    }
	
	public static Direction findSpot() throws GameActionException {
		double bestFound = rc.senseOre(myLoc);
		Direction bestDir = Direction.NONE;
		double tempOre;
		for (Direction d : directions) {
			MapLocation potential = myLoc.add(d);
			tempOre = rc.senseOre(potential);
			RobotInfo atLoc = rc.senseRobotAtLocation(potential);
			if (tempOre > bestFound && (atLoc == null) || (tempOre == bestFound && (rand.nextBoolean() || bestDir == Direction.NONE))) {
				bestFound = tempOre;
				bestDir = d;
			}
		}
		return bestDir;
	}

    static void requestSupply () throws GameActionException {
        if (rc.getSupplyLevel() < SUPPLY_THRESHOLD && IS_SUPPLYING) {
            resupplyChannel = SupplyBeaver.requestResupply(rc, rc.getLocation(), resupplyChannel);

            int head = rc.readBroadcast(196);
            MapLocation beaverLoc = new MapLocation(rc.readBroadcast(198), rc.readBroadcast(199));
            if (head == resupplyChannel && beaverLoc.distanceSquaredTo(rc.getLocation()) < 20) {
                //System.out.println("waiting for refuel");
                while (rc.getSupplyLevel() < SUPPLY_THRESHOLD) {
                    oreHere = (int) (rc.senseOre(myLoc) +.5);
                    if (rc.isCoreReady() && rc.canMine() && oreHere > 0) {
                        if (TESTING_MINING) {
                            int extracted = (int)(Math.max(Math.min(3, oreHere/4),0.2) * 10);
                            rc.broadcast(ORE_CHANNEL, rc.readBroadcast(ORE_CHANNEL) + extracted);
                        }

                        rc.mine();
                    }

                    rc.yield();
                }
            }
        } else {
            resupplyChannel = 0;
        }
    }
}
