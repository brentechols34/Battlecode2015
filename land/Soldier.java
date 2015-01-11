package team163.land;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

import java.util.Random;

import team163.utils.AttackUtils;
import team163.utils.Move;

/**
 * Created by brentechols on 1/5/15. sweetness
 */
public class Soldier {

    static RobotController rc;
    static Team myTeam;
    static Team enemyTeam;
    static int myRange;
    static Random rand;
    static Behavior mood; /* current behavior */

    static int range;
    static Team team;
    static int senseRange = 24;
    //static boolean isAttacking = false;

    public static void run(RobotController rc) {
        try {
            Soldier.rc = rc;
            Soldier.range = rc.getType().attackRadiusSquared;
            Soldier.team = rc.getTeam();

			//Move.setRc(rc);
            mood = new B_Turtle(); /* starting behavior of turtling */

            mood.setRc(Soldier.rc);

            while (true) {

                /* get behavior */
                mood = chooseB();

                /* perform round */
                mood.perception();
                mood.calculation();
                mood.action();

                /* end round */
                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Soldier Exception");
            e.printStackTrace();
        }
    }

    // This method will attack an enemy in sight, if there is one
    static void attackSomething() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
        if (enemies.length > 0) {
            rc.attackLocation(enemies[0].location);
        }
    }

    private static Behavior chooseB() {
        try {
            /* if more than 10 tanks trigger aggresive behavior */
            if (rc.readBroadcast(4) > 10 || rc.readBroadcast(66) == 1) {
                mood = new B_Attack();
            }

            /* if less than 5 tanks trigger turtling */
            if (rc.readBroadcast(4) < 5) {
                mood = new B_Turtle();
            }

            /* if mood has not been altered than current mood is kept */
            return mood;
        } catch (Exception e) {
            System.out.println("Error caught in choosing tank behavior");
        }
        return mood; /* if error happens use current mood */

    }
}
