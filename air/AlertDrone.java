package team163.air;

import java.awt.*;
import java.util.Random;

import battlecode.common.*;
import team163.logistics.SupplyBeaver;
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
    static Random random;

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
            Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
            Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static void run(RobotController rc) {
        try {
            if (rc.readBroadcast(4) == 1) {
                SupplyBeaver.run(rc);
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
                state = state.run(rc);

                AlertDrone.rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Drone Exception");
            e.printStackTrace();
        }
    }
}

interface State {
    State run (RobotController rc) throws GameActionException;
}

class Patrolling implements State {
    MapLocation nextTower = null;

    public State run (RobotController rc) throws GameActionException {
        if (nextTower != null) {
            rc.setIndicatorString(0, "Patrolling to " + nextTower.toString());
        }

        int x = rc.readBroadcast(CHANNELS.PANIC_X.getValue());
        int y = rc.readBroadcast(CHANNELS.PANIC_Y.getValue());

        if (x != 0 || y != 0) {
            nextTower = new MapLocation(x, y);
        }

        if (AlertDrone.enemies.length > 0) {
            Chasing state = new Chasing();
            state.run(rc);

            return state;
        }

        if (nextTower == null || rc.getLocation().distanceSquaredTo(nextTower) <= 3) {
            if (nextTower != null && nextTower.x == x && nextTower.y == y) {
                rc.broadcast(CHANNELS.PANIC_X.getValue(), 0);
                rc.broadcast(CHANNELS.PANIC_Y.getValue(), 0);
            }

            MapLocation[] towers = rc.senseTowerLocations();
            int index = (int)(Math.random() * towers.length + 1);

            if (index == towers.length) {
                nextTower = AlertDrone.hq;
            } else {
                nextTower = towers[index];
            }
        }

        Move.tryFly(nextTower);

        if (Clock.getBytecodesLeft() > 500) {
            Supply.supplyConservatively(rc, AlertDrone.team);
        }

        return this;
    }
}

class Responding implements State {
    public State run (RobotController rc) throws GameActionException {
        return this;
    }
}

class Chasing implements State {
    public State run (RobotController rc) throws GameActionException {
        rc.setIndicatorString(0, "Chasing");
        if (AlertDrone.enemies.length == 0) {
            Patrolling state = new Patrolling();
            state.run(rc);

            return state;
        }

        AttackUtils.attackSomething(rc, AlertDrone.range, AlertDrone.opponent);
        Move.tryFly(AlertDrone.enemies[0].location);

        return this;
    }
}