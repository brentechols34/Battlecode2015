/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.*;
import team163.logistics.SupplyDrone;

/**
 *
 * @author sweetness
 */
public class Launcher {

    static RobotController rc;
    static Behavior mood; /* current behavior */

    static int range;
    static Team team;
    static MapLocation hq;
    static MapLocation enemyHQ;
    static int channel;
    static boolean panic = false;
    static int resupplyChannel = 0;

    public static void run(RobotController rc) {
        try {
            Launcher.rc = rc;
            Launcher.range = rc.getType().attackRadiusSquared;
            Launcher.team = rc.getTeam();
            Launcher.hq = rc.senseHQLocation();
            Launcher.enemyHQ = rc.senseEnemyHQLocation();

            mood = new B_Launcher(); /* starting behavior of turtling */

            while (true) {

                /* get behavior */
                mood = chooseB();

                /* perform round */
                requestSupply();
                mood.perception();
                mood.calculation();
                mood.action();

                /* end round */
                Launcher.rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Launcher Exception");
            e.printStackTrace();
        }
    }

    static void requestSupply() throws GameActionException {
        if (rc.getSupplyLevel() < 250) {
            resupplyChannel = SupplyDrone.requestResupply(rc, rc.getLocation(), resupplyChannel);

            int head = rc.readBroadcast(196);
            MapLocation beaverLoc = new MapLocation(rc.readBroadcast(198), rc.readBroadcast(199));
            if (head == resupplyChannel && beaverLoc.distanceSquaredTo(rc.getLocation()) < 20) {
                System.out.println("waiting for refuel");
                Launcher.rc.yield();
            }
        } else {
            resupplyChannel = 0;
        }
    }

    private static Behavior chooseB() {
        return mood;
    }
}
