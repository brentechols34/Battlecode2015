package team163.air;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import team163.utils.CHANNELS;
import team163.utils.Spawn;

public class Helipad {

    static RobotController rc;

    public static void run(RobotController rc) {
        try {
            Spawn.rc = rc;
            Helipad.rc = rc;
            while (true) {
                int maxDrone = rc.readBroadcast(CHANNELS.BUILD_NUM_DRONE.getValue());
                int droneCount = rc.readBroadcast(CHANNELS.NUMBER_DRONE.getValue());
                if (rc.isCoreReady() && rc.getTeamOre() >= 125
                        && droneCount < maxDrone) {
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
