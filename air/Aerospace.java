/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.RobotController;
import battlecode.common.RobotType;
import java.util.Random;
import team163.utils.CHANNELS;
import team163.utils.Spawn;

/**
 *
 * @author sweetness
 */
public class Aerospace {

    static RobotController rc;

    public static void run(RobotController rc) {
        try {
            Spawn.rc = rc;
            while (true) {
                int maxLauncher = rc.readBroadcast(CHANNELS.BUILD_NUM_LAUNCHER.getValue());
                int countLauncher = rc.readBroadcast(CHANNELS.NUMBER_LAUNCHER.getValue());
                if (rc.isCoreReady() && rc.getTeamOre() >= 400
                        && countLauncher < maxLauncher) {
                    Spawn.randSpawn(RobotType.LAUNCHER);
                }

                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Aerospace Exception");
            e.printStackTrace();
        }
    }
}
