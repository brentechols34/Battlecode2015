package team163.tanks;

import battlecode.common.*;

import java.util.*;

import team163.utils.CHANNELS;
import team163.utils.Spawn;
import static team163.utils.Spawn.trySpawn;

public class TankFactory {

    static RobotController rc;
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static void run(RobotController TankFactoryRC) {

        rc = TankFactoryRC;
        Random rand = new Random();
        Spawn.rc = rc;
        while (true) {
            try {
                // get information broadcasted by the HQ
                int numTanks = rc.readBroadcast(CHANNELS.NUMBER_TANK.getValue());
                int maxTanks = rc.readBroadcast(CHANNELS.BUILD_NUM_TANK.getValue());

                if (rc.isCoreReady() && rc.getTeamOre() >= 250
                        && numTanks < maxTanks) {
                    trySpawn(directions[rand.nextInt(8)], RobotType.TANK);
                }

                rc.yield();

            } catch (Exception e) {
                System.out.println("Tank Factory Exception");
                e.printStackTrace();
            }
        }
    }
}
