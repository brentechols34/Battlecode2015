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
import team163.utils.BasicBugger;
import team163.utils.Move;
import team163.utils.PathMove;

/**
 * Alex
 * sweetness
 */
public class Soldier {

    static MapLocation rally;
    static MapLocation goal;
    static boolean attacking;

    public static void run(RobotController rc) {
        RobotInfo[] enemies;
        boolean building = false;
        Actions.rc = rc;
        while (true) {
            try {
                enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
                if (!building) {
                    int xGoal = rc.readBroadcast(67);
                    int yGoal = rc.readBroadcast(68);
                    goal = new MapLocation(xGoal, yGoal);
                }
                Actions.myLoc = rc.getLocation();
                building = Actions.tryAttack(enemies);
                if (!building) {
                    Actions.safeMove(goal);
                }
                
                rc.yield();
            } catch (Exception e) {
                System.out.println("error in soldier " + e);
            }
        }
    }

}
