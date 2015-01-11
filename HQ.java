/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163;

import battlecode.common.*;

import java.util.Random;

import team163.utils.Point;
import team163.utils.Spawn;

/**
 *
 * @author sweetness
 */
public class HQ {

    static Random rand;
    static RobotController rc;
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    static Team enemyTeam;
    static int myRange;
    static RobotInfo[] myRobots;
    static int[] counts;
    static Team myTeam;
    
    static boolean[] walls_found; //NORTH, EAST, SOUTH, WEST is the order
    static int minX;
    static int minY;
    static int maxX;
    static int maxY;

    public static void run(RobotController rc) {
        Spawn.rc = rc;
        HQ.rc = rc;
        myTeam = rc.getTeam();
        myRange = rc.getType().attackRadiusSquared;
        enemyTeam = myTeam.opponent();
        rand = new Random(rc.getID());
        try {
        	setRallyLocation();
        } catch (Exception e) {
        	System.out.println("Broke setting rally location");
        }
        try {
        rc.broadcast(72, 1);
        rc.broadcast(179, 0); //init vals
        rc.broadcast(180, Integer.MAX_VALUE);
        rc.broadcast(181, 0);
        rc.broadcast(182, Integer.MAX_VALUE);
        } catch (Exception e) {System.out.println("Couldn't request path beaver");};
        while (true) {
            try {
            	//Channel 1-21 for unit counts
                //Channel 200 for supply beavers
                //Channel 201 for supply beaver requests
            	//Channel 666 for move-bitch strat
            	//667 for timeout for 666
            	//Channel 1000 for ore-best
            	//Channel 1001 for ore-best.x
            	//Channel 1001 for ore-best.y
            	//Channel 30, 31 for x, y offsets
            	//Channel 32, 33 for map width, height
            	//Channel 34,35,36,37 booleans for asking for explorer beavers
            	//Channel 72 for path planning beaver request
            	//Channel 73,74 for start x,y
            	//Channel 75,76 for finish x,y
            	//channel 77 for path length
            	//Channel 78-178 for path
            	//Channel 179 for minX
            	//Channel 180 for maxX 
            	//Channel 181 for minY
            	//channel 182 for maxY
            	//Channel 187,188 for supply beaver loc
            
            	eachTurn();
            	performUnitCount();
            	decrees();
            	

                if (rc.isWeaponReady()) {
                    attackSomething();
                }

                if (rc.isCoreReady() && rc.getTeamOre() >= 100 && counts[8] < 20) { //counts[7] == beaverCount
                    team163.utils.Spawn.trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
                }
                int pbX = rc.readBroadcast(187);
                int pbY = rc.readBroadcast(188);
                MapLocation pathBeaverLoc = new MapLocation(pbX,pbY);
                if (pathBeaverLoc.distanceSquaredTo(rc.getLocation()) < 15) rc.transferSupplies((int) rc.getSupplyLevel(), pathBeaverLoc); //give pathbeaver everything               
                rc.yield();
            } catch (Exception e) {
                System.out.println("HQ Exception");
                e.printStackTrace();
            }
        }
    }
    
    static void eachTurn() throws GameActionException {
    	int timeout = rc.readBroadcast(667);
    	if (timeout > 0) {
    		rc.broadcast(667, timeout-1);
    	}
    }
    
    static void performUnitCount() throws GameActionException {
    	myRobots = rc.senseNearbyRobots(999999, myTeam);
        counts = new int[21];
        for (RobotInfo r : myRobots) {
            RobotType type = r.type;
            switch (type) {
            case SOLDIER: counts[0]++; break; //1
            case BASHER: counts[1]++; break; //2
            case BARRACKS: counts[2]++; break; //3
            case DRONE: counts[3]++; break; //4
            case TANK: counts[4]++; break; //5
            case HELIPAD: counts[5]++; break; //6
            case AEROSPACELAB: counts[6]++; break;
            case BEAVER: counts[7]++; break; //8
            case COMMANDER: counts[8]++; break;
            case COMPUTER: counts[9]++; break;
            case HANDWASHSTATION: counts[10]++; break;
            case HQ: counts[11]++; break;
            case LAUNCHER: counts[12]++; break;
            case MINER: counts[13]++; break;
            case MINERFACTORY: counts[14]++; break;
            case MISSILE: counts[15]++; break;
            case SUPPLYDEPOT: counts[16]++; break;
            case TANKFACTORY: counts[17]++; break;
            case TECHNOLOGYINSTITUTE: counts[18]++; break;
            case TOWER: counts[19]++; break;
            case TRAININGFIELD: counts[20]++; break;
            }

        }
        for (int i = 1; i < 22; i++) {
        	rc.broadcast(i, counts[i-1]);
        }
    }
    
    static void decrees() throws GameActionException {
        /* if more than 60 units execute order 66 (full out attack) */
        if ((counts[0] + counts[3] + counts[4]) > 10 && rc.readBroadcast(66) == 0) { //soldier + drone + tanks
            rc.broadcast(66, 1);
            MapLocation enemyHQ = rc.senseEnemyHQLocation();
            rc.broadcast(67,enemyHQ.x);
            rc.broadcast(68, enemyHQ.y);
        }
        
        if ((counts[0] + counts[3] + counts[4]) < 5) {
            rc.broadcast(66, 0);
        }
    }
    

    // This method will attack an enemy in sight, if there is one
    static void attackSomething() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
        if (enemies.length > 0) {
            rc.attackLocation(enemies[0].location);
        }
    }
    
    static void setRallyLocation() throws GameActionException {
    	MapLocation enemy = rc.senseEnemyHQLocation();
    	MapLocation me = rc.senseHQLocation();
    	//this is a simple way that should tend to work, hopefully
    	int x = me.x - (me.x - enemy.x)/5;
    	int y =  me.y - (me.y - enemy.y)/5;
    	System.out.println(x + " " + y);
    	rc.broadcast(50,x);
    	rc.broadcast(51,y);
    }
}
