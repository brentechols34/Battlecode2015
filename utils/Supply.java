package team163.utils;

import battlecode.common.*;

/**
 * Created by brentechols on 1/11/15.
 */
public class Supply {

    public static void supplySomething(RobotController rc, Team team) throws GameActionException {
        RobotInfo[] friends = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, team);
        int friendLength = friends.length;

        if (friendLength == 0) {
            return;
        }

        double min = Double.MAX_VALUE;
        RobotInfo supplied = null;

        outer: for (int i = friendLength - 1; i >= 0; i--) {
            switch (friends[i].type) {
                case SUPPLYDEPOT:
                case MINERFACTORY:
                case TECHNOLOGYINSTITUTE:
                case BARRACKS:
                case HELIPAD:
                case TRAININGFIELD:
                case TANKFACTORY:
                case AEROSPACELAB:
                case HANDWASHSTATION:
                case TOWER:
                    continue outer;
                default:
                    break;
            }


            if (friends[i].supplyLevel < min) {
                min = friends[i].supplyLevel;
                supplied = friends[i];
            }
        }

        if (min < 20000) {
            int supply = (int)rc.getSupplyLevel();
            rc.transferSupplies((int)Math.min(supply < 20000 ? supply / 2 : supply, 1000), supplied.location);
        }
    }

    public static void supplyConservatively(RobotController rc, Team team) throws GameActionException {
        RobotInfo[] friends = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, team);
        int friendLength = friends.length;

        if (friendLength == 0) {
            return;
        }

        double min = Double.MAX_VALUE;
        RobotInfo supplied = null;

        outer: for (int i = friendLength - 1; i >= 0; i--) {
            switch (friends[i].type) {
                case SUPPLYDEPOT:
                case MINERFACTORY:
                case TECHNOLOGYINSTITUTE:
                case BARRACKS:
                case HELIPAD:
                case TRAININGFIELD:
                case TANKFACTORY:
                case AEROSPACELAB:
                case HANDWASHSTATION:
                case TOWER:
                    continue outer;
                default:
                    break;
            }


            if (friends[i].supplyLevel < min) {
                min = friends[i].supplyLevel;
                supplied = friends[i];
            }
        }

        if (min < 20000) {
            int supply = (int)rc.getSupplyLevel();
            rc.transferSupplies((int)Math.min(supply - 500, 500), supplied.location);
        }
    }
}
