/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.*;

/**
 *
 * @author sweetness
 */
public class Missile {
    public static void run(RobotController rc) {
        try {
            Team team = rc.getTeam();
            double ratio;
            RobotInfo[] robots;
            MapLocation loc = rc.senseEnemyHQLocation();
            MapLocation myLoc;

            //missiles only live 5 rounds
            while (true) {
                /* perform round */
                robots = rc.senseNearbyRobots(14);
                myLoc = rc.getLocation();

                double enemy = 0;
                double allie = 0;

                for (RobotInfo x : robots) {
                    if (x.team == team) {
                        allie++;
                    } else {
                        loc = x.location;
                        if (loc.isAdjacentTo(myLoc)) {
                            enemy++;
                        }
                    }
                }

                if (allie == 0 && enemy > 0) {
                    ratio = 1;
                } else {
                    ratio = enemy / allie;
                }

                if (ratio > .5) {
                    rc.explode();
                } else {
                    Direction dir = rc.getLocation().directionTo(loc);
                    if (rc.canMove(dir) && rc.isCoreReady()) {
                        rc.move(dir);
                    }
                }
                /* end round */
                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Missile Exception");
            e.printStackTrace();
        }
    }

}
