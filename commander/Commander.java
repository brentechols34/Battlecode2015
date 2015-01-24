/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.commander;

import battlecode.common.*;
import team163.logistics.SupplyDrone;

/**
 *
 * @author sweetness
 */
public class Commander {

    static RobotController rc;
    static int range;
    static int senseRange = 24;
    static Team team;
    static int resupplyChannel = 0;
    static boolean panic = false; //is there a distress signal
    static Team opponent;
    static MapLocation enemyHQ;
    static MapLocation hq;
    static MapLocation myLoc;
    static Actions action;

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    static public Mood mood;

    static enum Mood {

        BEAST,
        SCOUT,
        HARASS
    };

    public static void run(RobotController rc) {
        Commander.rc = rc;
        Commander.hq = rc.senseHQLocation();
        Commander.range = rc.getType().attackRadiusSquared;
        System.out.println("Range was " + Commander.range);
        Commander.team = rc.getTeam();
        Commander.opponent = Commander.team.opponent();
        Commander.enemyHQ = rc.senseEnemyHQLocation();

        action = new Actions();

        //Initialise classes
        beast = new B_BeastMode();
        scout = new B_Scout();
        harass = new B_Harass();

        mood = Mood.HARASS;
        Behavior behave = getBehavior(mood);

        while (true) {
            try {
                requestSupply();
                myLoc = rc.getLocation();
                behave = getBehavior(mood);
                rc.setIndicatorString(1, mood + " mode");
                behave.perception();
                behave.calculation();
                if (!panic) {
                    rc.setIndicatorDot(rc.getLocation(), 5, 5, 5);
                    behave.action();
                }
                behave.panicAlert();
                Commander.rc.yield();
            } catch (Exception e) {
                System.out.println("Commander issue " + e);
            }
        }
    }

    static private Behavior beast;
    static private Behavior scout;
    static private Behavior harass;

    static private Behavior getBehavior(Mood current) {
        Mood in = current;
        if (Clock.getRoundNum() < 500) {
            in = Mood.HARASS;
        }

        switch (in) {
            case BEAST:
                return beast;
            case SCOUT:
                return scout;
            case HARASS:
                return harass;
            default:
                return harass;

        }
    }

    static void requestSupply() throws GameActionException {
        if (rc.getSupplyLevel() < 250) {
            resupplyChannel = SupplyDrone.requestResupply(rc, rc.getLocation(), resupplyChannel);

            int head = rc.readBroadcast(196);
            MapLocation beaverLoc = new MapLocation(rc.readBroadcast(198), rc.readBroadcast(199));
            if (head == resupplyChannel && beaverLoc.distanceSquaredTo(rc.getLocation()) < 20) {
                System.out.println("waiting for refuel");
                Commander.rc.yield();
            }
        } else {
            resupplyChannel = 0;
        }
    }

}
