package team163.air;

import java.util.Random;

import battlecode.common.*;
import team163.logistics.SupplyDrone;
import team163.utils.AttackUtils;
import team163.utils.CHANNELS;
import team163.utils.Move;
import team163.utils.Supply;

public class AlertDrone {

    static RobotController rc;
    static Behavior mood; /* current behavior */

    static int range;
    static Team team;
    static Team opponent;
    static MapLocation hq;
    static MapLocation enemyHQ;
    static boolean right; //turn right
    static boolean panic = false;
    static RobotInfo[] enemies;
    static RobotInfo[] allies;
    static Random random;

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static void run(RobotController rc) {
        try {
            int supplierAlive = rc.readBroadcast(CHANNELS.SUPPLY_DRONE1.getValue());
            int round = Clock.getRoundNum();
            int lastRound = round - 1;

            if (supplierAlive != round && supplierAlive != lastRound) {
                SupplyDrone.run(rc, 1);
            }
            supplierAlive = rc.readBroadcast(CHANNELS.SUPPLY_DRONE2.getValue());
            if (supplierAlive != round && supplierAlive != lastRound) {
                SupplyDrone.run(rc, 2);
            }
            supplierAlive = rc.readBroadcast(CHANNELS.SUPPLY_DRONE3.getValue());
            if (supplierAlive != round && supplierAlive != lastRound) {
                SupplyDrone.run(rc, 3);
            }

            AlertDrone.rc = rc;
            AlertDrone.range = rc.getType().attackRadiusSquared;
            AlertDrone.team = rc.getTeam();
            AlertDrone.opponent = rc.getTeam().opponent();
            AlertDrone.hq = rc.senseHQLocation();
            AlertDrone.enemyHQ = rc.senseEnemyHQLocation();
            AlertDrone.random = new Random(rc.getID());

            State state = new Patrolling();
            while (true) {
                AlertDrone.enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, AlertDrone.opponent);
                AlertDrone.allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, AlertDrone.team);
                state = state.run(rc);

                AlertDrone.rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Alert Drone Exception");
            e.printStackTrace();
        }
    }
}

interface State {

    State run(RobotController rc) throws GameActionException;
}

class Patrolling implements State {

    MapLocation nextTower = null;

    public State run(RobotController rc) throws GameActionException {
        if (nextTower != null) {
            rc.setIndicatorString(0, "Patrolling to " + nextTower.toString());
        }

        int x = rc.readBroadcast(CHANNELS.PANIC_X.getValue());
        int y = rc.readBroadcast(CHANNELS.PANIC_Y.getValue());

        if (x != 0 || y != 0) {
            nextTower = new MapLocation(x, y);
        }

        //add goal location to patrol if attacking
        if (rc.readBroadcast(CHANNELS.ORDER66.getValue()) == 1
                && nextTower == null) {
            x = rc.readBroadcast(CHANNELS.GOAL_X.getValue());
            y = rc.readBroadcast(CHANNELS.GOAL_Y.getValue());
            nextTower = new MapLocation(x, y);
        }

        if (AlertDrone.enemies.length > 0
                && ((double) AlertDrone.allies.length + 1.0) * 1.3 > AlertDrone.enemies.length) {
            Chasing state = new Chasing();
            //state.run(rc);

            return state;
        }

        if (nextTower == null || rc.getLocation().distanceSquaredTo(nextTower) <= 3) {
            if (nextTower != null && nextTower.x == x && nextTower.y == y) {
                rc.broadcast(CHANNELS.PANIC_X.getValue(), 0);
                rc.broadcast(CHANNELS.PANIC_Y.getValue(), 0);
            }

            MapLocation[] towers = rc.senseTowerLocations();
            int index = (int) (Math.random() * towers.length + 1);

            //add chance to patroll ore location
            if (Math.random() > 0.8) {
                int xOre = rc.readBroadcast(CHANNELS.BEST_ORE_X.getValue());
                int yOre = rc.readBroadcast(CHANNELS.BEST_ORE_Y.getValue());
                nextTower = new MapLocation(xOre, yOre);
            } else {
                if (index == towers.length) {
                    nextTower = AlertDrone.hq;
                } else {
                    if (index > towers.length) {
                        nextTower = AlertDrone.hq;
                    } else {
                        nextTower = towers[index];
                    }
                }
            }
        }

        Move.tryKite(nextTower, rc.senseEnemyTowerLocations());

        if (Clock.getBytecodesLeft() > 500) {
            Supply.supplyConservatively(rc, AlertDrone.team);
        }

        return this;
    }
}

class Responding implements State {

    public State run(RobotController rc) throws GameActionException {
        return this;
    }
}

class Chasing implements State {

    public State run(RobotController rc) throws GameActionException {
        rc.setIndicatorString(0, "Chasing");
        if (AlertDrone.enemies.length == 0) {
            Patrolling state = new Patrolling();
            //state.run(rc);

            return state;
        }

        AttackUtils.attackSomething(rc, AlertDrone.range, AlertDrone.opponent);
        MapLocation[] towers = rc.senseEnemyTowerLocations();
        if (AlertDrone.enemies.length > 0
                && Move.inTowerRange(AlertDrone.enemies[0].location, towers)) {
            //enemy moved into tower range
            if (AlertDrone.allies.length < 4) {
                //call off chase
                Patrolling state = new Patrolling();
                //state.run(rc);

                return state;
            }
        }

        //test if outnumbered
        if (((double) AlertDrone.allies.length + 1.0) * 1.3 < AlertDrone.enemies.length) {
            Patrolling state = new Patrolling();
            //state.run(rc);
            return state;
        }

        if (AlertDrone.enemies.length > 0) {
            if (AlertDrone.allies.length > 4) { //charge
                Move.tryFly(AlertDrone.enemies[0].location);
            } else {
                Move.tryKite(AlertDrone.enemies[0].location, rc.senseEnemyTowerLocations());
            }
        }
        return this;
    }
}
