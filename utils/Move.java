package team163.utils;

import java.util.Random;

import battlecode.common.*;

/**
 *
 * @author jak
 *
 */
public class Move {

    /* @NOTE first and one time, rc must be SET!!! */
    static RobotController rc;
    static int count = 0;
    static MapLocation store;
    static boolean stored = false;
    static boolean set = false;
    static int persistance = 0;
    static Direction pre = Direction.NORTH;
    static boolean right = true;

    /* instantiate movement utils */
    static MBugger mb;
    static Random rand = new Random();

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    static public void setRc(RobotController in) {
        Move.rc = in;
        mb = new MBugger(rc);
    }

    // This method will attempt to move in Direction d (or as close to it as
    // possible)
    static public void tryMove(Direction d) {
        try {
            set = false;
            int offsetIndex = 0;
            int[] offsets = {0, 1, -1, 2, -2};
            int dirint = directionToInt(d);
            while (offsetIndex < 5
                    && !rc.canMove(directions[(dirint + offsets[offsetIndex] + 8) % 8])) {
                offsetIndex++;
            }
            if (offsetIndex < 5 && rc.isCoreReady()) {
                rc.move(directions[(dirint + offsets[offsetIndex] + 8) % 8]);
            }
        } catch (Exception e) {
            System.out.println("Error in tryMove");
        }
    }

    static public void randMove() {
        tryMove(directions[rand.nextInt(8)]);
        set = false;
    }

    /**
     * Try moving using the bugger High of 2360 bytecode Low of 560 bytecode
     * Average of about 1400 bytecode
     *
     * @param m end target map location
     */
    static MapLocation last;

    static public void tryMove(MapLocation m) {
        if (m.equals(rc.getLocation())) {
            return;
        }
        try {
            if (!rc.isCoreReady()) {
                return;
            }
            MapLocation ml = rc.getLocation();
            if (!set || !m.equals(last)) { //  || rand.nextDouble() > .90
                last = m;
                set = true;
                mb.reset();
                mb.setTargetLocation(m);
                mb.start = ml;
            }
            // try using bugging system
            MapLocation p = mb.nextMove();
            if (p != null) {
                Direction dir = ml.directionTo(p);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                } else {
                    mb.closest = null;
                }
            }
        } catch (Exception e) {
            System.out.println("Error attempting bugging");
            e.printStackTrace();
        }
    }

    static public void tryFly (MapLocation m) throws GameActionException {
        if (!rc.isCoreReady()) {
            return;
        }

        Direction d = rc.getLocation().directionTo(m);
//        int dirint = directionToInt(d);
//        int[] offsets = {0, 1, -1};
//        for (int i = 0; i < offsets.length; i++) {
//
//        }

        rc.move(d);
    }

    /**
     * Test in range of incoming objects (uses hard set 27 sq at the moment)
     *
     * @param m location to check
     * @param obj stuff to avoid
     * @return
     */
    public static boolean inTowerRange(MapLocation m, MapLocation[] obj) {
        for (MapLocation x : obj) {
            if (x != null && x.x != 0 && x.y != 0) {
                if (m.distanceSquaredTo(x) < 27) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Test in range of incoming objects (uses hard set 27 sq at the moment)
     *
     * @param m location to check
     * @param obj stuff to avoid
     * @return
     */
    public static boolean inTowerRange(MapLocation m, MapLocation[] obj, int range) {
        for (MapLocation x : obj) {
            if (x != null && x.x != 0 && x.y != 0) {
                if (m.distanceSquaredTo(x) < range) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Does not take into account walls and trys to kite around towers and stuff
     *
     * @param target map location to go to
     * @param objects stuff to kite around such as tower locations
     */
    public static void tryKite(MapLocation target, MapLocation[] objects) {
        try {
            Direction dir = pre;
            MapLocation myLoc = rc.getLocation();
            MapLocation next = myLoc.add(dir);
            if (myLoc.compareTo(target) == 0) {
                target = rc.senseHQLocation();
            }

            if (right && inTowerRange(myLoc.add(pre), objects)
                    && inTowerRange(myLoc.add(pre.rotateRight()), objects)
                    && inTowerRange(myLoc.add(pre.rotateRight().rotateRight()), objects)) {
                right = false;
            }

            if (!right && inTowerRange(myLoc.add(pre), objects)
                    && inTowerRange(myLoc.add(pre.rotateLeft()), objects)
                    && inTowerRange(myLoc.add(pre.rotateLeft().rotateLeft()), objects)) {
                right = true;
            }

            boolean check = true;
            int count = 8;
            while (check && count-- > 0) {
                for (MapLocation x : objects) {
                    if (x != null && x.x != 0 && x.y != 0) {
                        for (int j = 0; j < 8; j++) {
                            if (x.distanceSquaredTo(next) < 27) {
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
            Direction direct = myLoc.directionTo(target);
            Direction d = myLoc.directionTo(next);
            do {
                rc.setIndicatorDot(myLoc, 4, 4, 4);
                if (right) {
                    d = d.rotateRight();
                } else {
                    d = d.rotateLeft();
                }
            } while (!inTowerRange(myLoc.add(d), objects) && d.compareTo(direct) != 0);
            dir = d;
            if (rc.senseTerrainTile(next) == TerrainTile.OFF_MAP) {
                dir = dir.opposite();
            }
            if (rc.isCoreReady()) {
                for (int i = 0; i < 8; i++) {
                    if (rc.canMove(dir) && !inTowerRange(myLoc.add(dir), objects)) {
                        pre = dir;
                        rc.move(dir);
                        break;
                    } else {
                        dir = (right) ? dir.rotateRight() : dir.rotateLeft();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("kiting pooped out" + e);
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
