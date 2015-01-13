package team163;

import battlecode.common.*;
import team163.utils.AttackUtils;

/**
 * Created by brentechols on 1/5/15.
 * sweetness
 */
public class Tower {

    static double myHealth;

    public static void run(RobotController rc) {
        myHealth = rc.getHealth();
        MapLocation myLoc = rc.getLocation();
        while (true) {
            try {
                double curHealth = rc.getHealth();
                if (curHealth < myHealth) {
                    myHealth = curHealth;
                    rc.broadcast(911, myLoc.x);
                    rc.broadcast(912, myLoc.y);
                    continue;
                }
                if (rc.isWeaponReady()) {
                    AttackUtils.attackSomething(rc, rc.getType().attackRadiusSquared, rc.getTeam().opponent());
                }
            } catch (Exception e) {
                System.out.println("Tower Exception");
                e.printStackTrace();
            }
        }
    }
}
