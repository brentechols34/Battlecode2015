/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import battlecode.common.*;
import static team163.utils.AttackUtils.attackSomething;
import team163.utils.PathMove;

/**
 *
 * @author sweetness
 */
public class Miner_Rush {

    static MapLocation previous;
    static MapLocation closest;
    static MapLocation myLoc;
    static MapLocation enemyHQ;
    static PathMove pm;
    static int myRange;
    static Team enemyTeam;

    public static void run(RobotController rc) {
        myLoc = rc.getLocation();
        previous = rc.getLocation();
        enemyHQ = rc.senseEnemyHQLocation();
        pm = new PathMove(rc);
        myRange = rc.getType().attackRadiusSquared;
        enemyTeam = rc.getTeam().opponent();

        while (true) {
            try {
                myLoc = rc.getLocation();
                attackSomething(rc, myRange, enemyTeam);

                //find closest tower or enemy hq
                closest = closest(rc.senseEnemyTowerLocations());
                if (!previous.equals(closest)) {
                    pm.setDestination(closest);
                    previous = closest;
                }
                pm.attemptMove();
                rc.yield();
            } catch (Exception e) {
                System.out.println("error in miner rush " + e);
            }
        }
    }

    private static MapLocation closest(MapLocation[] in) {
        MapLocation c = enemyHQ;
        int min = myLoc.distanceSquaredTo(enemyHQ);
        if (in == null) {
            return c;
        }
        for (MapLocation m : in) {
            int dis = m.distanceSquaredTo(myLoc);
            if (dis < min) {
                min = dis;
                c = m;
            }
        }
        return c;
    }
}
