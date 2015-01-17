package team163.air;

import java.util.Random;

import battlecode.common.*;

public class Drone {

    static RobotController rc;
    static Behavior mood; /* current behavior */

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
            Drone.right = (rc.getID() % 2 == 0); //50% chance of right

            mood = new B_Kite(); /* starting behavior of turtling */

            while (true) {

                /* get behavior */
                mood = chooseB();

                /* perform round */
                mood.perception();
                mood.calculation();
                if (!panic) {
                    mood.action();
                }
                mood.panicAlert();

                /* end round */
                Drone.rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Drone Exception");
            e.printStackTrace();
        }
    }

    private static Behavior chooseB() {
        return mood;
    }

}
