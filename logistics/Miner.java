/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import java.util.Random;

import team163.utils.Move;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
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
        Beaver.rc = rc;
        rand = new Random(rc.getID());
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        while (true) {
            try {
            	MapLocation myLoc = rc.getLocation();
                Direction d = findSpot();
                double oreHere = rc.senseOre(myLoc);
                double oreThere = rc.senseOre(myLoc.add(d));
                if (oreThere > oreHere + oreHere/20 && rc.canMove(d)) {
                	rc.move(d);
                } else {
                	rc.mine();
                }
                
            } catch (Exception e) {
                System.out.println("Beaver Exception");
                e.printStackTrace();
            }
        }
    }
	
	public static Direction findSpot() {
		MapLocation myLoc = rc.getLocation();
		double bestFound = rc.senseOre(myLoc);
		Direction bestDir = Direction.NONE;
		double tempOre;
		for (Direction d : directions) {
			tempOre = rc.senseOre(myLoc.add(d));
			if (tempOre > bestFound) {
				bestFound = tempOre;
				bestDir = d;
			}
		}
		return bestDir;
	}

    
}
