/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

/**
 *
 * @author sweetness
 */
public class SupplyDepot {

    static RobotController rc;
    static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST,
            Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
            Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

    public static void run(RobotController minerFactoryRC) {
        try {
            rc = minerFactoryRC;
            Random rand = new Random();
            Team myTeam = rc.getTeam();

            while (true) {
                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Supply Depot Exception");
            e.printStackTrace();
        }
    }

}
