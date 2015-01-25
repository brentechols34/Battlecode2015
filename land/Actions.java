/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.land;

import battlecode.common.*;
import java.util.Arrays;
import team163.utils.BasicBugger;
import team163.utils.PathMove;

/**
 *
 * @author sweetness
 */
public class Actions {

    static RobotController rc;

    static PathMove pm;
    static BasicBugger bug;

    static MapLocation hq;
    static MapLocation myLoc;
    static MapLocation previous;
    static MapLocation bugPrevious;
    static MapLocation rally;
    static MapLocation goal;

    static int attackRange = 10;
    static Direction pre = Direction.NORTH;

    /**
     * rc in constructor needs to be set up for the specific type of robot
     */
    public Actions() {
        try {
            pm = new PathMove(rc);
            bug = new BasicBugger(rc);
            hq = rc.senseHQLocation();
            previous = hq;
            bugPrevious = hq;
            int xRally = rc.readBroadcast(50);
            int yRally = rc.readBroadcast(51);
            rally = new MapLocation(xRally, yRally);
        } catch (Exception e) {
            System.out.println("error in actions constroctor " + e);
        }
    }

    /**
     * Test in range of incoming objects
     *
     * @param m location to check
     * @param obj stuff to avoid
     * @return
     */
    static private boolean inRange(MapLocation m, MapLocation[] obj, int range) {
        for (MapLocation x : obj) {
            if (x != null && x.x != 0 && x.y != 0) {
                if (m.distanceSquaredTo(x) <= range) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     *
     * @param enemies
     * @rerturn true if attacking a building or priority
     */
    static public boolean tryAttack(RobotInfo[] enemies) {
        try {
            MapLocation close = closestRobot(enemies);
            if (myLoc.distanceSquaredTo(close) <= attackRange) {//is in range
                if (rc.isWeaponReady()) {
                    rc.attackLocation(close);
                    RobotType ri = rc.senseRobotAtLocation(close).type;
                    switch (ri) {
                        case AEROSPACELAB:
                        case BARRACKS:
                        case COMPUTER:
                        case HELIPAD:
                        case MINERFACTORY:
                        case SUPPLYDEPOT:
                        case TANKFACTORY:
                        case TECHNOLOGYINSTITUTE:
                        case TRAININGFIELD:
                            return true;
                        default:
                            return false;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("error in commander try attack " + e);
        }
        return false;
    }

    /**
     * f is boolean of wheather to flash the enemy or not
     *
     * @param enemies
     * @param f
     * @rerturn true if attacking a building or priority
     */
    static public boolean tryTarget(RobotInfo[] enemies) {
        try {
            for (RobotInfo ri : enemies) {
                switch (ri.type) {
                    case AEROSPACELAB:
                    case BARRACKS:
                    case COMPUTER:
                    case HELIPAD:
                    case MINERFACTORY:
                    case SUPPLYDEPOT:
                    case TANKFACTORY:
                    case TECHNOLOGYINSTITUTE:
                    case TRAININGFIELD:
                        if (myLoc.distanceSquaredTo(ri.location)
                                <= attackRange) {//is in range
                            if (rc.isWeaponReady()) {
                                rc.attackLocation(ri.location);
                            }
                            return true;
                        }
                    default:
                }
            }
            return tryAttack(enemies);
        } catch (Exception e) {
            System.out.println("error in commander try target " + e);
        }
        return false;
    }

    /**
     * f is boolean of wheather to flash the enemy or not
     *
     * @param m
     * @param f
     */
    static public void attack(MapLocation m) {
        try {
            if (myLoc.distanceSquaredTo(m) <= attackRange) {//is in range
                RobotInfo ri = rc.senseRobotAtLocation(m);
                if (rc.canAttackLocation(m)
                        && ri != null && ri.team != rc.getTeam()) {
                    if (rc.isWeaponReady()) {
                        rc.attackLocation(m);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("error in commander attack " + e);
        }
    }

    static public MapLocation closest(MapLocation[] in) {
        MapLocation closest = myLoc;
        int min = Integer.MAX_VALUE;
        for (MapLocation m : in) {
            int dis = m.distanceSquaredTo(myLoc);
            if (dis < min) {
                min = dis;
                closest = m;
            }
        }
        return closest;
    }

    static public MapLocation closestRobot(RobotInfo[] in) {
        MapLocation closest = myLoc;
        int min = Integer.MAX_VALUE;
        for (RobotInfo x : in) {
            int dis = x.location.distanceSquaredTo(myLoc);
            if (dis < min) {
                min = dis;
                closest = x.location;
            }
        }
        return closest;
    }

    /**
     * Try to make a direct move towards the map location regardless of being
     * shot
     *
     * @param m the Map location to move towards
     */
    static public void directMove(MapLocation m) {
        try {
            if (!previous.equals(m)) {
                pm.setDestination(m);
                previous = m;
            }
            pm.attemptMove();
        } catch (Exception e) {
            System.out.println("direct move error " + e);
        }
    }

    /**
     * Try to make a direct move towards the map location safely
     *
     * @param dir the Map location to move towards
     */
    static public void directSafe(Direction dir) {
        try {
            MapLocation[] towers = rc.senseEnemyTowerLocations();
            MapLocation[] objs = new MapLocation[towers.length + 1];
            System.arraycopy(towers, 0, objs, 0, towers.length);
            objs[towers.length] = rc.senseEnemyHQLocation();
            boolean right = (rc.getID() % 2 == 0);
            Direction c = dir;
            for (int i = 0; i < 8; i++) {
                if (rc.isCoreReady() && rc.canMove(c) && !inRange(myLoc, objs, 35)) {
                    rc.move(c);
                    return;
                } else {
                    c = (right) ? c.rotateRight() : c.rotateLeft();
                }
            }

        } catch (Exception e) {
            System.out.println("direct move error " + e);
        }
    }

    /**
     * Try to make a direct move towards the map location regardless of being
     * shot and not using any path/bugging
     *
     * @param m the Map location to move towards
     */
    static public void directMove(Direction m) {
        try {
            boolean right = (rc.getID() % 2 == 0);
            Direction dir = m;
            for (int i = 0; i < 8; i++) {
                if (rc.isCoreReady() && rc.canMove(dir)) {
                    rc.move(dir);
                    return;
                } else {
                    dir = (right) ? dir.rotateRight() : dir.rotateLeft();
                }
            }
        } catch (Exception e) {
            System.out.println("direct move error (with direction) " + e);
        }
    }

    /**
     * Try to kite around objects while making the move
     *
     * @param m target map location to move to
     * @param objects objects to avoid
     * @param dis location to avoid them by
     */
    static public void kiteMove(MapLocation m, MapLocation[] objects, int dis) {
        try {
            MapLocation close = closest(objects);
            if (myLoc.distanceSquaredTo(close) <= (dis)) {
                rc.setIndicatorString(1, "in range kiting");
                boolean right = (rc.getID() % 2 == 0);
                Direction dir = pre;
                MapLocation next = myLoc.add(dir);
                if (m == null || myLoc.compareTo(m) == 0) {
                    m = rc.senseHQLocation();
                }

                if (right && inRange(myLoc.add(pre), objects, dis)
                        && inRange(myLoc.add(pre.rotateRight()), objects, dis)
                        && inRange(myLoc.add(pre.rotateRight().rotateRight()), objects, dis)) {
                    right = false;
                }

                if (!right && inRange(myLoc.add(pre), objects, dis)
                        && inRange(myLoc.add(pre.rotateLeft()), objects, dis)
                        && inRange(myLoc.add(pre.rotateLeft().rotateLeft()), objects, dis)) {
                    right = true;
                }

                boolean check = true;
                int count = 8;
                while (check && count-- > 0) {
                    for (MapLocation x : objects) {
                        if (x != null && x.x != 0 && x.y != 0) {
                            for (int j = 0; j < 8; j++) {
                                if (x.distanceSquaredTo(next) < dis) {
                                    Direction nDir = (right) ? dir.rotateRight() : dir
                                            .rotateLeft(); // turn right or left
                                    next = myLoc.add(nDir);
                                }
                            }
                        }
                    }
                    // reached the end to the next location is good
                    check = false;
                }

                //curve toward target
                //boolean canChange = true;
                Direction direct = myLoc.directionTo(m);
                Direction d = myLoc.directionTo(next);
                do {
                    if (right) {
                        d = d.rotateRight();
                    } else {
                        d = d.rotateLeft();
                    }
                } while (!inRange(myLoc.add(d), objects, dis) && d.compareTo(direct) != 0);
                dir = d;
                if (rc.senseTerrainTile(next) == TerrainTile.OFF_MAP) {
                    dir = dir.opposite();
                }
                if (rc.isCoreReady()) {
                    for (int i = 0; i < 8; i++) {
                        if (rc.canMove(dir) && !inRange(myLoc.add(dir), objects, dis)) {
                            pre = dir;
                            rc.move(dir);
                            return;
                        } else {
                            dir = (right) ? dir.rotateRight() : dir.rotateLeft();
                        }
                    }
                }
                //if this point is reached than just move away from object
                //directMove(close.directionTo(Commander.myLoc));
            } else {
                if (!previous.equals(m)) {
                    pm.setDestination(m);
                    previous = m;
                }
                pm.attemptMove();
            }
        } catch (Exception e) {
            System.out.println("kites move error (with direction) " + e);
        }
    }

    /**
     * Move that is set up to avoid enemy towers and hq fire range (calls
     * kiteMove and sets up the arguments)
     *
     * @param m map location to move to
     */
    static public void safeMove(MapLocation m) {
        rc.setIndicatorString(1, "making safe move");
        MapLocation[] towers = rc.senseEnemyTowerLocations();
        MapLocation[] objs = new MapLocation[towers.length + 1];
        System.arraycopy(towers, 0, objs, 0, towers.length);
        objs[towers.length] = rc.senseEnemyHQLocation();
        kiteMove(m, objs, 35);
    }
}
