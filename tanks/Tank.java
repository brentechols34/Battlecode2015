package team163.tanks;

import team163.logistics.SupplyDrone;
import battlecode.common.*;

public class Tank {

    static RobotController rc;
    static B_Tank mood; /* current behavior */

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
            mood = new B_Tank(rc); /* starting behavior of turtling */

            while (true) {
                MapLocation myLoc = rc.getLocation();
                double best = rc.readBroadcast(1000);
                double here = rc.senseOre(myLoc);
                if (here > best) {
                    rc.broadcast(1000, (int) (.5 + here));
                    rc.broadcast(1001, myLoc.x);
                    rc.broadcast(1002, myLoc.y);
                }

                /* get behavior */
                //update();

                /* perform round */
                requestSupply();
                mood.perception();
                mood.act();

                /* end round */
                Tank.rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Tank Exception");
            e.printStackTrace();
        }
    }

    
    static void requestSupply() throws GameActionException {
        if (rc.getSupplyLevel() < 250) {
            resupplyChannel = SupplyDrone.requestResupply(rc, rc.getLocation(), resupplyChannel);

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
