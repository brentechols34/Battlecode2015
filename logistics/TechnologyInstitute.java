/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import battlecode.common.*;
import java.util.Random;
import team163.utils.Spawn;

/**
 *
 * @author sweetness
 */
public class TechnologyInstitute {

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
                if (rc.isCoreReady()) {
                    //Spawn.trySpawn(directions[rand.nextInt(8)], RobotType.COMPUTER);
                }
            } catch (Exception e) {
                System.out.println("Tech Inst Exception " + e);
            }
        }
    }
}
