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

    public static void run(RobotController rc) {
        Beaver.rc = rc;
        rand = new Random(rc.getID());
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();

        while (true) {
            try {
                if (rc.isWeaponReady()) {
                    attackSomething();
                }
                if (rc.isCoreReady()) {
                    int fate = rand.nextInt(1000);

                    /*
                     * @TODO for testing build tanks factories after first
                     * barracks
                     */
                    if (rc.readBroadcast(100) > 0) {
                        if (fate < 8 && rc.getTeamOre() > 500) {
                            tryBuild(directions[rand.nextInt(8)],
                                    RobotType.TANKFACTORY);
                        }
                    }

                    if (fate < 100 && rc.getTeamOre() >= 300
                            && rc.readBroadcast(100) < 1) {
                        tryBuild(directions[rand.nextInt(8)],
                                RobotType.BARRACKS);
                    } else if (fate < 600 && rc.isCoreReady()) {
                        rc.mine();
                    } else if (fate < 900) {
                        Move.tryMove(directions[rand.nextInt(8)]);
                    } else if (fate < 900 && rc.getTeamOre() >= 300) {
                        tryBuild(directions[rand.nextInt(8)],
                                RobotType.HELIPAD);
                    } else {
                        Move.tryMove(rc.senseHQLocation().directionTo(
                                rc.getLocation()));
                    }
                }
            } catch (Exception e) {
                System.out.println("Beaver Exception");
                e.printStackTrace();
            }
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
    static void tryBuild(Direction d, RobotType type)
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
        }
    }

}
