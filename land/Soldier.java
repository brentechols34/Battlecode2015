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
    static boolean isAttacking = false;

    public static void run(RobotController rc) {
        Soldier.rc = rc;
         rand = new Random(rc.getID());
        while (true) {
            try {
                if (rc.isWeaponReady()) {
                    attackSomething();
                }

                RobotInfo[] adjacentEnemies = rc.senseNearbyRobots(
                        rc.getType().attackRadiusSquared, enemyTeam);
                if (rc.isCoreReady() && adjacentEnemies.length == 0) {
                    int numSoldiers = rc.readBroadcast(1);

                    if (numSoldiers > 80) {
                        isAttacking = true;
                    } else if (numSoldiers < 55) {
                        isAttacking = false;
                    }

                    if (rc.readBroadcast(66) == 0) {
                        if (!isAttacking
                                && rc.getLocation().distanceSquaredTo(
                                        rc.senseHQLocation()) > 150) {
                            Move.tryMove(rc.getLocation().directionTo(
                                    rc.senseHQLocation()));
                        } else {
                            Move.tryMove(rc.getLocation().directionTo(
                                    rc.senseEnemyHQLocation()));

                        }
                    } else {
                        Move.tryMove(rc.getLocation().directionTo(
                                rc.senseEnemyHQLocation()));

                    }
                }
            } catch (Exception e) {
                System.out.println("Soldier Exception");
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
