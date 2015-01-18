package team163.utils;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

/**
 * Created by brentechols on 1/5/15.
 */
public class AttackUtils {
    private static float sqrt2 = 1.4f;

    // This method will attack an enemy in sight, if there is one
    public static void attackSomething(RobotController rc, int myRange, Team enemyTeam) throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
        if (enemies.length == 0 || !rc.isWeaponReady()) {
            return;
        }

        RobotInfo weakest = enemies[0];
        RobotInfo weakestMissile = null;

        for (RobotInfo enemy : enemies) {
            if (enemy.type != RobotType.MISSILE && enemy.health < weakest.health) {
                weakest = enemy;
            }

            if (enemy.type == RobotType.MISSILE && (weakestMissile == null || weakestMissile.health > enemy.health)) {
                weakestMissile = enemy;
            }
        }

        if (weakest != null) {
            rc.attackLocation(weakest.location);
            return;
        } else {
            rc.attackLocation(weakestMissile.location);
        }
    }

    //produces a value that is representative of how well a fight might turn out, positive is good, negative is bad.
    public static double fightHeuristic(RobotController rc) {
        Team myTeam = rc.getTeam();
        RobotInfo[] robots = rc.senseNearbyRobots(50);
        double val = 0.0;
        MapLocation myLoc = rc.getLocation();
        RobotType rt;
        for (RobotInfo r : robots) {
            rt = r.type;
            val += r.health *
                    (((double)rt.attackPower) / rt.attackDelay) / distance(myLoc, r.location) *
                    ((r.team == myTeam)?1:-1);
        }
        return val;
    }

    public static boolean isStationary(RobotType rt) {
        return (rt != null && (rt == RobotType.AEROSPACELAB
                || rt == RobotType.BARRACKS || rt == RobotType.HELIPAD
                || rt == RobotType.HQ || rt == RobotType.MINERFACTORY
                || rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY
                || rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD));
    }

    private static int abs(int x) {
        final int m = x >> 31;
        return x + m ^ m;
    }

    //actual distance between two points, better than euclidean and manhattan
    private static float distance(MapLocation p1, MapLocation p2) {
        final float dx = abs(p1.x - p2.x);
        final float dy = abs(p1.y - p2.y);
        return dx > dy ? (dy * sqrt2 + (dx - dy)) : (dx * sqrt2 + (dy - dx));
    }

}
