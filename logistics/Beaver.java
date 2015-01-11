/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import battlecode.common.*;

import java.util.Random;

import team163.utils.Move;

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
    static boolean amSupplyBeaver = false;

    public static void run(RobotController rc) {
        Beaver.rc = rc;
        rand = new Random(rc.getID());
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        
        while (true) {
            try {
            	lifetime++;
            	MapLocation myLoc = rc.getLocation();
            	if (rc.isWeaponReady()) {
            		attackSomething();
            	}
            	int oreHere = (int) (rc.senseOre(myLoc) +.5);
            	int bestFound = rc.readBroadcast(1000);
            	if (rc.isCoreReady()) {
            		if (oreHere > bestFound) {
            			rc.broadcast(1000, oreHere);
            			rc.broadcast(1001, myLoc.x);
            			rc.broadcast(1002, myLoc.y);
            		}
            		if (!rc.getLocation().isAdjacentTo(rc.senseHQLocation())) {
            			int[] counts = new int[] {
            					rc.readBroadcast(3), //barracks
            					rc.readBroadcast(6), //helipad
            					rc.readBroadcast(15), //minerfactory
            					rc.readBroadcast(18) //tank factory
            			};
            			int[] maxCounts = new int[] {
            					1,0,0,5
            			};
            			int[] priorityOffsets = new int[] {
            					1,1,2,(counts[0] > 0)?1:-1000
            			};
            			boolean oneGood = false;
            			for (int i = 0; i < priorityOffsets.length; i++) {
            				if (counts[i] >= maxCounts[i]) {
            					priorityOffsets[i] = -1000;
            				} else {
            					oneGood = true;
            				}
            			}
            			if (oneGood) {
            				int[] oreCosts = new int[] {300,300,500,500};
            				double oreCount = rc.getTeamOre();
            				for (int i = 0; i < counts.length; i++) {
            					counts[i] -= priorityOffsets[i];
            				}
            				int toMake = mindex(counts);
            				if (counts[toMake]+priorityOffsets[toMake] < maxCounts[toMake] && oreCount >= oreCosts[toMake]) {
            					switch(toMake) {
            					case 0: {
            						if (tryBuild(directions[rand.nextInt(8)],RobotType.BARRACKS)) {
            							rc.broadcast(3, counts[toMake]+1);
            							break;
            						}
            					}

            					case 1: {
            						if (tryBuild(directions[rand.nextInt(8)],RobotType.HELIPAD)) {
            							rc.broadcast(6, counts[toMake]+1);
            							break;
            						}
            					}

            					case 2: {
            						if (tryBuild(directions[rand.nextInt(8)],RobotType.MINERFACTORY)) {
            							rc.broadcast(15, rc.readBroadcast(15)+1);
            							break;
            						}
            					}

            					case 3: {
            						if (tryBuild(directions[rand.nextInt(8)],RobotType.TANKFACTORY)) {
            							rc.broadcast(18, counts[toMake]+1);
            							break;
            						}
            					}
            					}
            				}
            			}
            		}
        			defaultMove();
        			rc.yield();
                }
            } catch (Exception e) {
                System.out.println("Beaver Exception");
                e.printStackTrace();
            }
        }
    }

	static void defaultMove() throws GameActionException {
		Direction d = findSpot();
		MapLocation myLoc = rc.getLocation();
		if (rc.isCoreReady()) {
        	int oreHere = (int) (rc.senseOre(myLoc) +.5);
        	int bestFound = rc.readBroadcast(1000);
        	if (bestFound > 1.75 * oreHere) {
        		Move.tryMove(new MapLocation(rc.readBroadcast(1001),rc.readBroadcast(1002)));
        	}
            if (d != Direction.NONE && (rc.isCoreReady() && rc.canMove(d)) && (rand.nextDouble() > .8  || rc.senseOre(myLoc) < .2)) { 
            	rc.move(d);
            } else {
            	if (rc.isCoreReady() && rc.canMine()) rc.mine();
            }
		}
	}
	
	public static Direction findSpot() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		double bestFound = rc.senseOre(myLoc);
		Direction bestDir = Direction.NONE;
		double tempOre;
		for (Direction d : directions) {
			MapLocation potential = myLoc.add(d);
			tempOre = rc.senseOre(potential);
			RobotInfo atLoc = rc.senseRobotAtLocation(potential);
			if (tempOre > bestFound && (atLoc == null) || (tempOre == bestFound && bestDir == Direction.NONE)) {
				bestFound = tempOre;
				bestDir = d;
			}
		}
		return bestDir;
	}
    
    static int mindex(int[] options) {
    	int dex = 0;
    	int min = options[0];
    	for (int i = 1; i < options.length; i++) {
    		if (options[i] < min) {
    			min = options[i];
    			dex = i;
    		}
    	}
    	return dex;
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
    static boolean tryBuild(Direction d, RobotType type)
            throws GameActionException {
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
