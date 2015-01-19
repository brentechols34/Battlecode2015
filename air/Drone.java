package team163.air;

import java.util.Random;

import battlecode.common.*;

public class Drone {

    static RobotController rc;
    static int range;
    static Team team;
    static Team opponent;
    static MapLocation hq;
    static MapLocation enemyHQ;
    static boolean right; //turn right
    static boolean panic = false;

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static void run(RobotController rc) {
        try {
            Drone.rc = rc;
            Drone.range = rc.getType().attackRadiusSquared;
            Drone.team = rc.getTeam();
            Drone.opponent = rc.getTeam().opponent();
            Drone.hq = rc.senseHQLocation();
            Drone.enemyHQ = rc.senseEnemyHQLocation();

            Behavior mood = new B_Scout(); /* starting behavior of turtling */

            while (true) {

                /* get behavior */
                //mood = chooseB();
                MapLocation myLoc = rc.getLocation();
                double best = rc.readBroadcast(1000);
                double here = rc.senseOre(myLoc);
                if (here > best) {
                    rc.broadcast(1000, (int) (.5 + here));
                    rc.broadcast(1001, myLoc.x);
                    rc.broadcast(1002, myLoc.y);
                }
                /* perform round */
                mood.perception();
                mood.calculation();
                mood.action();

                /* end round */
                Drone.rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Drone Exception");
            e.printStackTrace();
        }
    }
}
