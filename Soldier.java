package team163;

import battlecode.common.RobotController;
import team163.utils.AttackUtils;

/**
 * Created by brentechols on 1/5/15.
 */
public class Soldier {

    public static void run (RobotController rc) {
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

                if (!isAttacking
                        && rc.getLocation().distanceSquaredTo(
                        rc.senseHQLocation()) > 150) {
                    tryMove(rc.getLocation().directionTo(
                            rc.senseHQLocation()));
                }

                if (isAttacking) {
                    tryMove(rc.getLocation().directionTo(
                            rc.senseEnemyHQLocation()));
                } else {
                    tryMove(directions[rand.nextInt(8)]);
                }
            }
        } catch (Exception e) {
            System.out.println("Soldier Exception");
            e.printStackTrace();
        }
    }
}