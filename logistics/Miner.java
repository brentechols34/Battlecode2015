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
	
	public static void run(RobotController rc) {
        Miner.rc = rc;
        rand = new Random(rc.getID());
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        while (true) {
            try {
            	MapLocation myLoc = rc.getLocation();
                Direction d = findSpot();
              //if the spot I found is better than here, and skipping a turn to move there is worthwhile
                
                if (rc.isCoreReady() && rc.canMove(d) && (rand.nextDouble() > .8 || rc.senseOre(myLoc) / 20  < 5)) { 
                	rc.move(d);
                } else {
                	if (rc.isCoreReady() && rc.canMine()) rc.mine();
                }
                
            } catch (Exception e) {
                System.out.println("Miner Exception");
                e.printStackTrace();
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
			if (tempOre > bestFound && (atLoc == null)) {
				bestFound = tempOre;
				bestDir = d;
			}
		}
		return bestDir;
	}

    
}
