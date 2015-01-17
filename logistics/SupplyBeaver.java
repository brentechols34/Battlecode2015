/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import battlecode.common.*;

import java.util.Random;

import team163.utils.Move;
import team163.utils.Supply;

import javax.xml.stream.Location;

/**
 *
 * @author sweetness
 */
public class SupplyBeaver {

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
            Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
            Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    static RobotController rc;
    static Team myTeam;
    static Team enemyTeam;
    static int myRange;
    static Random rand;
    static MapLocation myLoc;

    public static void run(RobotController con) {
        rc = con;
        rand = new Random(rc.getID());
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        //System.out.println("new supply beaver!");

        while (true) {
            try {
                myLoc = rc.getLocation();
                rc.setIndicatorString(0, "I am supply beaver | head: " + rc.readBroadcast(196) + " tail: " + rc.readBroadcast(197));

                //broadcast our updated position
                rc.broadcast(198, myLoc.x);
                rc.broadcast(199, myLoc.y);

                if (rc.isCoreReady()) {
                    double supply = rc.getSupplyLevel();
                    if (supply > 500) {
                        //System.out.println("Supplying people!");
                        goSupplyPeople();
                    } else {
                        //System.out.println("Going back to base!");
                        goToBase();
                    }
                }

                rc.yield();
            } catch (Exception e) {
                System.out.println("Supply Beaver Exception");
                e.printStackTrace();
            }
        }
    }

    // THIS IS CALLED BY OTHER PEOPLE SO DONT USE CLASS VARIABLES
    public static int requestResupply (RobotController requester, MapLocation loc, int channel) throws GameActionException {
        // Default assign the channel to the tail
        if (channel == 0) {
            channel = requester.readBroadcast(197);
            System.out.println("Requested on channel " + channel);

            // Adjust the tail pointer for new requests
            channel = (channel == 300) ? 200 : channel;
            requester.broadcast(197, channel + 2);
        }

        // Write out the supply request to the radio
        requester.broadcast(channel, loc.x);
        requester.broadcast(channel + 1, loc.y);

        return channel;
    }

    static void goToBase () {
        if(myLoc.distanceSquaredTo(rc.senseHQLocation()) < 10) {
            //System.out.println("Resupplying myself!");
            return;
        }

        Direction d = myLoc.directionTo(rc.senseHQLocation());
        if (rc.canMove(d)) {
            Move.tryMove(d);
        }
    }

    static void goSupplyPeople () throws GameActionException {
        int head = rc.readBroadcast(196);
        int tail = rc.readBroadcast(197);

        // If there is no one to supply, go collect more supplies
        if (head == tail) {
            //System.out.println("Queue is empty, no work to do!");
            goToBase();
        }

        MapLocation dest = new MapLocation(rc.readBroadcast(head), rc.readBroadcast(head + 1));
        if (myLoc.distanceSquaredTo(dest) < GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
            RobotInfo info = rc.senseRobotAtLocation(dest);
            if (info == null) {
                //System.out.println("Supply request invalid, moving on");
                rc.broadcast(196, (head == 298) ? 200 : (head + 2));
            }

            //System.out.println("Supplied someone! head now at " + ((head == 298) ? 200 : (head + 2)) + " and tail at " + rc.readBroadcast(197));
            rc.transferSupplies((int) Math.min(rc.getSupplyLevel() - 500, 2000), dest);

            rc.broadcast(196, (head == 298) ? 200 : (head + 2));
        }

        if (rc.isCoreReady()) {
            Move.tryMove(myLoc.directionTo(dest));
        }
    }
}
