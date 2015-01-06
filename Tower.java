package team163;

import battlecode.common.RobotController;
import team163.utils.AttackUtils;

/**
 * Created by brentechols on 1/5/15.
 */
public class Tower {

    public static void run (RobotController rc) {
        try {
            if (rc.isWeaponReady()) {
                AttackUtils.attackSomething(rc, rc.getType().attackRadiusSquared, rc.getTeam().opponent());
            }
        } catch (Exception e) {
            System.out.println("Tower Exception");
            e.printStackTrace();
        }
    }
}
