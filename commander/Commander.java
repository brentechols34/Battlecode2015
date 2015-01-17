/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.commander;

import battlecode.common.*;

/**
 *
 * @author sweetness
 */
public class Commander {

    static RobotController rc;
    static int range;
    static int senseRange = 24;
    static Team team;
    static int resupplyChannel = 0;
    static boolean panic = false; //is there a distress signal

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static void run(RobotController rc) {
        Behavior mood = new B_BeastMode();
        Commander.rc = rc;
        Commander.range = rc.getType().attackRadiusSquared;
        Commander.team = rc.getTeam();

        while (true) {
            try {

            } catch (Exception e) {
                System.out.println("Commander issue " + e);
            }
        }
    }

}
