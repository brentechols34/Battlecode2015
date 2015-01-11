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
				/* end round */
				rc.yield();
		} catch (Exception e) {
			System.out.println("Soldier Exception");
			e.printStackTrace();
		}
	}
}

