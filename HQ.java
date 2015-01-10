/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163;

import battlecode.common.*;
import java.util.Random;
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
                int numSoldiers = 0;
                int numBashers = 0;
                int numBeavers = 0;
                int numBarracks = 0;
                int numDrones = 0;
                int numTanks = 0;
                for (RobotInfo r : myRobots) {
                    RobotType type = r.type;
                    if (type == RobotType.SOLDIER) {
                        numSoldiers++;
                    } else if (type == RobotType.BASHER) {
                        numBashers++;
                    } else if (type == RobotType.BEAVER) {
                        numBeavers++;
                    } else if (type == RobotType.BARRACKS) {
                        numBarracks++;
                    } else if (type == RobotType.DRONE) {
                        numDrones++;
                    } else if (type == RobotType.TANK) {
                        numTanks++;
                    }
                }
                rc.broadcast(0, numBeavers);
                rc.broadcast(1, numSoldiers);
                rc.broadcast(2, numBashers);
                rc.broadcast(3, numDrones);
                rc.broadcast(4, numTanks);
                rc.broadcast(100, numBarracks);

                /* if more than 60 units execute order 66 (full out attack) */
                if ((numSoldiers + numDrones + numTanks) > 66) {
                    rc.broadcast(66, 1);
                }
                if ((numSoldiers + numDrones + numTanks) < 30) {
                    rc.broadcast(66, 0);
                }

                if (rc.isWeaponReady()) {
                    attackSomething();
                }

                if (rc.isCoreReady() && rc.getTeamOre() >= 100
                        && fate < Math.pow(1.2, 12 - numBeavers) * 10000) {
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
