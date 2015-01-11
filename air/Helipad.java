package team163.air;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import team163.utils.Spawn;

public class Helipad {
    
    static RobotController rc;
    static Random rand;

    public static void run(RobotController rc) {
        try {
            Spawn.rc = rc;
            Helipad.rc = rc;
            rand = new Random(rc.getID());
            while (true) {
            	int droneCount = rc.readBroadcast(4);
            	int minerCount = rc.readBroadcast(14);
                if (rc.isCoreReady() && rc.getTeamOre() >= 125
                        && rand.nextBoolean() && droneCount < 10 && minerCount > 5) {
                    Spawn.randSpawn(
                            RobotType.DRONE);
                }

                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Helipad Exception");
            e.printStackTrace();
        }
    }
}
