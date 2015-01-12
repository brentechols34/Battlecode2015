/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import battlecode.common.*;

import java.util.Random;

import team163.utils.Move;
import team163.utils.Path;
import team163.utils.Point;
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

    public static void run(RobotController rc) {
        SupplyBeaver.rc = rc;
        rand = new Random(rc.getID());
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();


        while (true) {
            try {
                myLoc = rc.getLocation();
                if (rc.isCoreReady()) {
                    double supply = rc.getSupplyLevel();
                    if (supply > 1000) {
                        goSupplyPeople();
                    } else {
                        goToBase();
                    }
                }

                rc.yield();
            } catch (Exception e) {
                System.out.println("Beaver Exception");
                e.printStackTrace();
            }
        }
    }

    // THIS IS CALLED BY OTHER PEOPLE SO DONT USE CLASS VARIABLES
    public static int requestResupply (RobotController requester, MapLocation loc, int channel) throws GameActionException {
        // Default assign the channel to the tail
        if (channel == 0) {
            channel = requester.readBroadcast(197);

            // Adjust the tail pointer for new requests
            channel = channel == 298 ? 200 : channel + 2;
            requester.broadcast(197, channel + 2);
        }

        // Write out the supply request to the radio
        requester.broadcast(channel, loc.x);
        requester.broadcast(channel, loc.y);

        return channel;
    }

    static void finishResupply () throws GameActionException {
        // Adjust the head of the resupply requests
        int head = rc.readBroadcast(196);
        head = head == 300 ? 200 : head + 2;
        rc.broadcast(196, head);
    }

    static void goToBase () {
        if(myLoc.distanceSquaredTo(rc.senseHQLocation()) < 15) {
            return;
        }

        Direction d = myLoc.directionTo(rc.senseHQLocation());
        if (rc.canMove(d)) {
            Move.tryMove(d);
            return;
        }
    }

    static void goSupplyPeople () throws GameActionException {
        int head = rc.readBroadcast(197);
        MapLocation dest = new MapLocation(rc.readBroadcast(head), rc.readBroadcast(head + 1));

        Direction d = myLoc.directionTo(dest);
        if (rc.canMove(d)) {
            Move.tryMove(d);
        }

        if (myLoc.distanceSquaredTo(dest) < GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
            Supply.supplySomething(rc, myTeam);
        }
    }
}
