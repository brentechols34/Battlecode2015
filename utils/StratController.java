package team163.utils;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class StratController {

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static boolean shouldBuildHere(RobotController rc, MapLocation m) {
        int count = 0;
        for (Direction d : directions) {
            MapLocation m2 = m.add(d);
            try {
                if (rc.senseTerrainTile(m2) == TerrainTile.NORMAL) {
                    if (rc.canSenseLocation(m2)) {
                        RobotInfo ri = rc.senseRobotAtLocation(m2);
                        if (ri == null) {
                            count++;
                        } else if (ri.buildingLocation != null) {
                            count--;
                        }
                    } else {
                        count++;
                    }
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        if (count < 6) {
            return false;
        }
        try {
            if (rc.canSenseLocation(m) && rc.senseRobotAtLocation(m) != null) {
                return false;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return isSafe(rc, m) && !m.isAdjacentTo(rc.senseHQLocation());
    }

    public static boolean isSafe(RobotController rc, MapLocation m) {
        MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
        for (MapLocation m2 : enemyTowers) {
            if (m.distanceSquaredTo(m2) < 40) {
                return false;
            }
        }
        if (m.distanceSquaredTo(rc.senseEnemyHQLocation()) < 40) {
            return false;
        }
        return true;
    }

    public static MapLocation findBuildLocation(RobotController rc) {
        MapLocation m = rc.getLocation();
        //find a reasonably close location that is a safe location
        //start close to me, work outwards, and return first valid location
        while (true) {
            int dis = 2;
            MapLocation a;
            for (int i = -dis; i <= dis; i++) {
                a = new MapLocation(m.x + i, m.y + dis);
                if (shouldBuildHere(rc, a)) {
                    return a;
                }
                a = new MapLocation(m.x + i, m.y - dis);
                if (shouldBuildHere(rc, a)) {
                    return a;
                }
                a = new MapLocation(m.x + dis, m.y + i);
                if (shouldBuildHere(rc, a)) {
                    return a;
                }
                a = new MapLocation(m.x - dis, m.y + i);
                if (shouldBuildHere(rc, a)) {
                    return a;
                }
            }
            dis++;
            if (dis > 100) {
                System.out.println("OH NOES");
            }
        }
    }

    public static RobotType toMake(RobotController rc) throws GameActionException {

        //read in brodcast channels for make number to build
        int maxBarracks = rc.readBroadcast(CHANNELS.BUILD_NUM_BARRACKS.getValue());
        int maxHelipad = rc.readBroadcast(CHANNELS.BUILD_NUM_HELIPAD.getValue());
        int maxMinerfactory = rc.readBroadcast(CHANNELS.BUILD_NUM_MINEFACTORY.getValue());
        int maxTankfactory = rc.readBroadcast(CHANNELS.BUILD_NUM_TANKFACTORY.getValue());
        int maxSupply = rc.readBroadcast(CHANNELS.BUILD_NUM_SUPPLY.getValue());
        int maxAerospace = rc.readBroadcast(CHANNELS.BUILD_NUM_AEROSPACELAB.getValue());
        int maxTechnologyInstitute;
        int maxTrainingField;
        if (rc.hasCommander()) {
            maxTechnologyInstitute = 0;
            maxTrainingField = 0;
        } else {
            maxTechnologyInstitute = 1;
            maxTrainingField = 1;
        }
        if (rc.getTeamMemory()[0] == 1) {
            maxHelipad = 5;
            maxTankfactory = 0;
            maxBarracks = 0;
        }
        int[] counts = new int[]{rc.readBroadcast(3), // barracks
            rc.readBroadcast(6), // helipad
            rc.readBroadcast(15), // minerfactory
            rc.readBroadcast(18), // tank factory
            rc.readBroadcast(17), // Supply depot
            rc.readBroadcast(7), // Aerospacelab
            rc.readBroadcast(19), //tech institute
            rc.readBroadcast(21) //training field
    };
        int[] maxCounts = new int[]{maxBarracks, maxHelipad,
            maxMinerfactory, maxTankfactory, maxSupply, maxAerospace,
            maxTechnologyInstitute, maxTrainingField};
        int[] priorityOffsets = new int[]{1, 2, 3,
            (counts[0] > 0) ? 1 : -10000, 1, (counts[1] > 0) ? 1 : -10000,
            1, (counts[6] > 0 && maxAerospace > 0) ? 1 : -10000};
        for (int i = 0; i < priorityOffsets.length; i++) {
            if (counts[i] >= maxCounts[i]) {
                priorityOffsets[i] = -10000;
            }
        }
        int[] oreCosts = new int[]{300, 300, 500, 500, 100, 500, 200, 200};
        double oreCount = rc.getTeamOre();
        for (int i = 0; i < counts.length; i++) {
            counts[i] -= priorityOffsets[i];
        }
        int toMake = mindex(counts);
        rc.setIndicatorString(1, counts[toMake] + " " + maxCounts[toMake] + " " + Clock.getRoundNum() + " " + (counts[toMake] < maxCounts[toMake]
                && oreCount >= oreCosts[toMake]));
        if (counts[toMake] < maxCounts[toMake]
                && oreCount >= oreCosts[toMake]) {
            switch (toMake) {
                case 0:
                    return RobotType.BARRACKS;
                case 1:
                    return RobotType.HELIPAD;
                case 2:
                    return RobotType.MINERFACTORY;
                case 3:
                    return RobotType.TANKFACTORY;
                case 4:
                    return RobotType.SUPPLYDEPOT;
                case 5:
                    return RobotType.AEROSPACELAB;
                case 6:
                    return RobotType.TECHNOLOGYINSTITUTE;
                case 7:
                    return RobotType.TRAININGFIELD;
            }
        }
        return null;
    }

    static int mindex(int[] options) {
        int dex = 0;
        int min = options[0];
        for (int i = 1; i < options.length; i++) {
            if (options[i] < min) {
                min = options[i];
                dex = i;
            }
        }
        return dex;
    }

    public static void calculateRatios(RobotController rc) {
        try {
            int maxBarracks = 1;
            int maxHelipad = 10;
            int maxMinerfactory = 7;
            int maxTankfactory;
            int maxSupply = 5;
            int maxAerospace = 0;
            int maxTechnologyInstitute = 1;
            int maxTrainingField = 1;
            int roundNum = Clock.getRoundNum();
            int mapSize = 3600; //60 * 60 assuming average to begin

            int maxSoldiers = 0;//soldiers seem weak
            int maxDrones = 100;
            int maxMiner = 100;
            int maxTank = 50;
            int maxLauncher;
            int maxBasher = 20;

            int countSoldiers = rc.readBroadcast(CHANNELS.NUMBER_SOLDIER.getValue());
            int countDrones = rc.readBroadcast(CHANNELS.NUMBER_DRONE.getValue());
            int countMiner = rc.readBroadcast(CHANNELS.NUMBER_MINER.getValue());
            int countTank = rc.readBroadcast(CHANNELS.NUMBER_TANK.getValue());
            int countLauncher = rc.readBroadcast(CHANNELS.NUMBER_LAUNCHER.getValue());
            int countBasher = rc.readBroadcast(CHANNELS.NUMBER_BASHER.getValue());

            int countBarracks = rc.readBroadcast(CHANNELS.NUMBER_BARRACKS.getValue());
            int countHelipad = rc.readBroadcast(CHANNELS.NUMBER_HELIPAD.getValue());
            int countMineFact = rc.readBroadcast(CHANNELS.NUMBER_MINERFACTORY.getValue());
            int countTankFact = rc.readBroadcast(CHANNELS.NUMBER_TANKFACTORY.getValue());
            int countSupply = rc.readBroadcast(CHANNELS.NUMBER_SUPPLYDEPOT.getValue());
            int countAeroSpace = rc.readBroadcast(CHANNELS.NUMBER_AEROSPACELAB.getValue());

            //set launchers to 10% of tanks
            maxLauncher = (int) ((double) countTank * 0.1);

            //max tank factory small to begin round
            if (roundNum < 500) {
                maxTankfactory = 2;
            } else {
                maxTankfactory = 5;
            }

            //if no tank factorys than no aerospace is needed
            if (countTankFact < 1 || countTank < 20) {
                maxAerospace = 0;
            } else {
                maxAerospace = (int) ((double) countTankFact / 2) + 1;
            }

            //set drones depending on tanks
            //if (countDrones > 10 && countTank < 10) {
            //    maxDrones = 10;
            //}
            //set max basher as percentage of tanks
            maxBasher = (int) ((double) countTank * 0.3);

            //building max values
            rc.broadcast(CHANNELS.BUILD_NUM_BARRACKS.getValue(), maxBarracks);
            rc.broadcast(CHANNELS.BUILD_NUM_HELIPAD.getValue(), maxHelipad);
            rc.broadcast(CHANNELS.BUILD_NUM_MINEFACTORY.getValue(), maxMinerfactory);
            rc.broadcast(CHANNELS.BUILD_NUM_TANKFACTORY.getValue(), maxTankfactory);
            rc.broadcast(CHANNELS.BUILD_NUM_SUPPLY.getValue(), maxSupply);
            rc.broadcast(CHANNELS.BUILD_NUM_AEROSPACELAB.getValue(), maxAerospace);

            //unit max values
            rc.broadcast(CHANNELS.BUILD_NUM_SOLDIER.getValue(), maxSoldiers);
            rc.broadcast(CHANNELS.BUILD_NUM_DRONE.getValue(), maxDrones);
            rc.broadcast(CHANNELS.BUILD_NUM_MINER.getValue(), maxMiner);
            rc.broadcast(CHANNELS.BUILD_NUM_TANK.getValue(), maxTank);
            rc.broadcast(CHANNELS.BUILD_NUM_LAUNCHER.getValue(), maxLauncher);
            rc.broadcast(CHANNELS.BUILD_NUM_BASHER.getValue(), maxBasher);

        } catch (Exception e) {
            System.out.println("Error in claculating ratios " + e);
        }
    }
}
