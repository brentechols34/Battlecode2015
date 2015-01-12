/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import java.util.Random;

import team163.utils.Move;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

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
	
	public static void run(RobotController rc) {
        Miner.rc = rc;
        rand = new Random(rc.getID());
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        //rc.yield();
        //rc.yield();
        while (true) {
            try {
            	lifetime++;
            	myLoc = rc.getLocation();
                oreHere = (int) (rc.senseOre(myLoc) +.5);
            	bestVal = rc.readBroadcast(1000);
            	bestLoc = new MapLocation(rc.readBroadcast(1001), rc.readBroadcast(1002));
            	if (rc.isCoreReady()) {
            		if (oreHere > bestVal) {
            			rc.broadcast(1000, oreHere);
            			rc.broadcast(1001, myLoc.x);
            			rc.broadcast(1002, myLoc.y);
            		}
        			defaultMove();
        			rc.yield();
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
        	if (rc.isCoreReady() && rc.canMine() && oreHere > 3) rc.mine();
        	else if (rc.isCoreReady() && rc.canMove(d)) {
    			rc.move(d);
    			return;
    		}
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

    
}
