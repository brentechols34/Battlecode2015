/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.land;

import battlecode.common.*;
import java.util.Random;
import team163.utils.CHANNELS;
import team163.utils.Spawn;

/**
 *
 * @author sweetness
 */
public class Barracks {

    static RobotController rc;
    static Team myTeam;
    static Team enemyTeam;
    static int myRange;
    static Random rand;
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static void run(RobotController rc) {
        rand = new Random(rc.getID());
        Spawn.rc = rc;
        while (true) {
            try {
                int maxBasher = rc.readBroadcast(CHANNELS.BUILD_NUM_BASHER.getValue());
                int countBasher = rc.readBroadcast(CHANNELS.NUMBER_BASHER.getValue());
                int maxSoldier = rc.readBroadcast(CHANNELS.BUILD_NUM_SOLDIER.getValue());
                int countSoldier = rc.readBroadcast(CHANNELS.NUMBER_SOLDIER.getValue());

                if (rc.isCoreReady() && rc.getTeamOre() >= 100
                        && countBasher < maxBasher) {
                    Spawn.trySpawn(directions[rand.nextInt(8)], RobotType.BASHER);
                }
                if (rc.isCoreReady() && rc.getTeamOre() >= 60
                        && countSoldier < maxSoldier) {
                    Spawn.trySpawn(directions[rand.nextInt(8)], RobotType.SOLDIER);
                }
            } catch (Exception e) {
                System.out.println("Barracks Exception");
                e.printStackTrace();
            }
        }
    }
}
