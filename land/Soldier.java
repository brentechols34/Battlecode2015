package team163.land;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
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
    static Random rand;
    static Behavior mood; /* current behavior */

    static int range;
    static Team team;
    static int senseRange = 24;
    //static boolean isAttacking = false;

	public static void run(RobotController rc) {
		myTeam = rc.getTeam();
		Soldier.rc = rc;
		enemyTeam = myTeam.opponent();
		boolean attacking = false;
		MapLocation[] towers = rc.senseEnemyTowerLocations();
		int target = findClosestToHQ(towers);
		try {
			//sense every bot in range
			RobotInfo[] robots = rc.senseNearbyRobots(75);
			int allyCount = 0;
			MapLocation myLoc = rc.getLocation();
			int dis = Integer.MAX_VALUE;
			MapLocation closestEnemy = null;
			int t_dis;
			for (RobotInfo ri : robots) {
				if (ri.team == myTeam && (ri.type == RobotType.TANK || ri.type == RobotType.SOLDIER)) allyCount++;
				else if (ri.team == enemyTeam && (t_dis=myLoc.distanceSquaredTo(ri.location)) < dis) {
					dis = t_dis;
					closestEnemy = ri.location;
				}
			}
			if (rc.canSenseLocation(towers[target]) && rc.senseRobotAtLocation(towers[target]).type != RobotType.TOWER) {
				towers[target] = null;
				target = findClosestToHQ(towers);
				rc.broadcast(1111,0);
				attacking = false;
			}
			if (myLoc.distanceSquaredTo(towers[target]) < 35) {
				if (allyCount < 2 && rc.readBroadcast(1111)==0) {
					attacking = false;
					rc.broadcast(1111,0);
				}
				if (allyCount > 14 || attacking || rc.readBroadcast(1111)==1 || rc.readBroadcast(66)==1) {
					attacking = true;
					rc.broadcast(1111, 1);
					if (rc.isCoreReady()) {
						Direction dirToTower = myLoc.directionTo(towers[target]);
						Move.tryMove(towers[target]);
					}
				} else {
					if (rc.isCoreReady() && closestEnemy != null && rc.canAttackLocation(closestEnemy) && rc.getWeaponDelay() < 1) {
						rc.attackLocation(closestEnemy);
					}
				}
			} else {
				//System.out.println(myLoc.distanceSquaredTo(towers[target]));
				if (rc.isCoreReady() && closestEnemy != null && rc.canAttackLocation(closestEnemy) && rc.getWeaponDelay() < 1) {
					rc.attackLocation(closestEnemy);
				} else Move.tryMove(towers[target]);
			}
			
			rc.yield();
		} catch (Exception e) {
			System.out.println("Soldier Exception");
			e.printStackTrace();
		}
	}
	
	public static int findClosestToHQ(MapLocation[] locs) {
		int mindex = 0;
		MapLocation hq = rc.senseHQLocation();
		int minDis = hq.distanceSquaredTo(locs[0]);
		for (int i = 1; i < locs.length; i++) {
			int dis = hq.distanceSquaredTo(locs[i]);
			if (locs[i] != null && dis < minDis) {
				mindex=i;
				minDis = dis;
			}
		}
		return mindex;		
	}
}

