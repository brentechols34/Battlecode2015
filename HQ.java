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

    public static void run(RobotController rc) {
        Spawn.rc = rc;
        HQ.rc = rc;
        Team myTeam = rc.getTeam();
        myRange = rc.getType().attackRadiusSquared;
        enemyTeam = myTeam.opponent();
        rand = new Random(rc.getID());
        RobotInfo[] myRobots;
        while (true) {
            try {
                int fate = rand.nextInt(10000);
                myRobots = rc.senseNearbyRobots(999999, myTeam);
                int[] counts = new int[21];
                for (RobotInfo r : myRobots) {
                    RobotType type = r.type;
                    switch (type) { //HUEHUEHUE
                    case SOLDIER: counts[0]++; break; //1
                    case BASHER: counts[1]++; break; //2
                    case BARRACKS: counts[2]++; break; //3
                    case DRONE: counts[3]++; break; //4
                    case TANK: counts[4]++; break; //5
                    case HELIPAD: counts[5]++; break; //6
                    case AEROSPACELAB: counts[6]++; break;
                    case BEAVER: counts[7]++; break;
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

                /* if more than 60 units execute order 66 (full out attack) */
                if ((counts[0] + counts[3] + counts[4]) > 20 && rc.readBroadcast(66) == 0) { //soldier + drone + tanks
                    rc.broadcast(66, 1);
                    MapLocation enemyHQ = rc.senseEnemyHQLocation();
                    rc.broadcast(67,enemyHQ.x);
                    rc.broadcast(68, enemyHQ.y);
                }
                if ((counts[0] + counts[3] + counts[4]) < 5) {
                    rc.broadcast(66, 0);
                }

                if (rc.isWeaponReady()) {
                    attackSomething();
                }

                if (rc.isCoreReady() && rc.getTeamOre() >= 100
                        && fate < Math.pow(1.2, 12 - counts[7]) * 10000) { //counts[7] == beaverCount
                    team163.utils.Spawn.trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
                }
            } catch (Exception e) {
                System.out.println("HQ Exception");
                e.printStackTrace();
            }
        }
    }

    // This method will attack an enemy in sight, if there is one
    static void attackSomething() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
        if (enemies.length > 0) {
            rc.attackLocation(enemies[0].location);
        }
    }
}
