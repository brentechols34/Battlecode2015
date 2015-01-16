package team163.tanks;

import team163.logistics.SupplyBeaver;
import team163.utils.Move;
import battlecode.common.*;

public class Tank {

    static RobotController rc;
    static B_Turtle mood; /* current behavior */

    static int range;
    static int senseRange = 24;
    static Team team;
    static int resupplyChannel = 0;
    static boolean panic = false; //is there a distress signal

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static void run(RobotController rc) {
        try {
            Tank.rc = rc;
            Tank.range = rc.getType().attackRadiusSquared;
            Tank.team = rc.getTeam();

			//Move.setRc(rc);
            mood = new B_Turtle(); /* starting behavior of turtling */

            while (true) {
            	MapLocation myLoc = rc.getLocation();
            	double best = rc.readBroadcast(1000);
            	double here = rc.senseOre(myLoc);
            	if (here < best) {
            		rc.broadcast(1000, (int) (.5+here));
            		rc.broadcast(1001, myLoc.x);
            		rc.broadcast(1002, myLoc.y);
            	}

                /* get behavior */
                update();

                /* perform round */
                mood.perception();
                mood.calculation();
                if (!panic) {
                    mood.action();
                }
                mood.panicAlert();

                /* end round */
                Tank.rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Tank Exception");
            e.printStackTrace();
        }
    }

    /**
     * Logic to choose which behavior to use -- this could get large and require
     * refactoring...(source of technical debt)
     *
     * @return Behavior interface
     */
    private static void update() {
        try {
            if (rc.readBroadcast(66) == 1) {
                mood.attacking = true;
            } else {
                mood.attacking = false;
            }

//            requestSupply();
        } catch (Exception e) {
            System.out.println("Error caught in choosing tank behavior");
        }
    }

    static void requestSupply() throws GameActionException {
        if (rc.getSupplyLevel() < 250) {
            resupplyChannel = SupplyBeaver.requestResupply(rc, rc.getLocation(), resupplyChannel);

            int head = rc.readBroadcast(196);
            MapLocation beaverLoc = new MapLocation(rc.readBroadcast(198), rc.readBroadcast(199));
            if (head == resupplyChannel && beaverLoc.distanceSquaredTo(rc.getLocation()) < 20) {
                System.out.println("waiting for refuel");
                Tank.rc.yield();
            }
        } else {
            resupplyChannel = 0;
        }
    }
}
