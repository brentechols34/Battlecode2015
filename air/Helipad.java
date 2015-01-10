package team163.air;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Helipad {

    static RobotController rc;
    static Random rand;
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static void run(RobotController rc) {
        try {
            Helipad.rc = rc;
            rand = new Random();
            while (true) {
                if (rc.isCoreReady() && rc.getTeamOre() >= 125
                        && rand.nextBoolean()) {
                    trySpawn(directions[rand.nextInt(8)],
                            RobotType.DRONE);
                }

                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Helipad Exception");
            e.printStackTrace();
        }
    }

    // This method will attempt to spawn in the given direction (or as close to
    // it as possible)
    static void trySpawn(Direction d, RobotType type)
            throws GameActionException {
        int offsetIndex = 0;
        int[] offsets = {0, 1, -1, 2, -2, 3, -3, 4};
        int dirint = directionToInt(d);
        boolean blocked = false;
        while (offsetIndex < 8
                && !rc.canSpawn(
                        directions[(dirint + offsets[offsetIndex] + 8) % 8],
                        type)) {
            offsetIndex++;
        }
        if (offsetIndex < 8) {
            rc.spawn(directions[(dirint + offsets[offsetIndex] + 8) % 8], type);
        }
    }

    static int directionToInt(Direction d) {
        switch (d) {
            case NORTH:
                return 0;
            case NORTH_EAST:
                return 1;
            case EAST:
                return 2;
            case SOUTH_EAST:
                return 3;
            case SOUTH:
                return 4;
            case SOUTH_WEST:
                return 5;
            case WEST:
                return 6;
            case NORTH_WEST:
                return 7;
            default:
                return -1;
        }
    }

}
