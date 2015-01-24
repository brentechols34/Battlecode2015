/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.commander;

import battlecode.common.*;
import team163.utils.AttackUtils;
import team163.utils.Move;

/**
 *
 * @author sweetness
 */
public class B_Scout implements Behavior{

    public static RobotController rc = Commander.rc;
    int senseRange = 24;
    int index = 0;
    MapLocation myLoc;

    //order of exploring
    boolean south = false;
    boolean northEast = false;
    boolean north = false;
    boolean east = false;
    boolean west = false;
    int count = 10;
    int count2 = 10;

    RobotInfo[] nearRobots;
    RobotInfo[] enemies;

    public void setRc(RobotController in) {
        B_Scout.rc = in;
    }

    /**
     * Gather scouting information
     */
    public void perception() {
        myLoc = rc.getLocation();

        /* sense everything within range */
        nearRobots = rc.senseNearbyRobots(senseRange);
        enemies = rc.senseNearbyRobots(Commander.range, Commander.team.opponent());
    }

    /**
     * Test terrain and look at robot info
     */
    public void calculation() {
        try {

        } catch (Exception e) {
            System.out.println("Error in Drone calculation");
        }
    }

    /**
     * Report on information
     */
    public void action() {
        try {
            if (enemies.length > 1) {
                if (rc.isWeaponReady()) {
                    AttackUtils.attackSomething(rc, Commander.range, Commander.opponent);
                } else {
                    /* run away if can not fire */
                    Move.tryMove(enemies[0].location.directionTo(myLoc));
                }
            } else {
                if (rc.isCoreReady()) {
                    if (!south) {
                        if (rc.senseTerrainTile(myLoc.add(Direction.SOUTH)).equals(TerrainTile.OFF_MAP)) {
                            south = true;
                        }
                        Move.tryMove(Direction.SOUTH);
                    } else if (!northEast) {
                        if (rc.senseTerrainTile(myLoc.add(Direction.NORTH)).equals(TerrainTile.OFF_MAP)) {
                            northEast = true;
                            north = true;
                        }
                        if (rc.senseTerrainTile(myLoc.add(Direction.EAST)).equals(TerrainTile.OFF_MAP)) {
                            northEast = true;
                            east = true;
                        }
                        Move.tryMove(Direction.NORTH_EAST);
                    } else if ((count--) > 0) {
                        if (rc.senseTerrainTile(myLoc.add(Direction.WEST)).equals(TerrainTile.OFF_MAP)) {
                            north = true;
                        }
                        Move.tryMove(Direction.WEST);
                    } else if (!north) {
                        if (rc.senseTerrainTile(myLoc.add(Direction.NORTH)).equals(TerrainTile.OFF_MAP)) {
                            north = true;
                        }
                        Move.tryMove(Direction.NORTH);
                    } else if ((count2--) > 0) {
                        Move.tryMove(Direction.SOUTH);
                    } else if (!east) {
                        if (rc.senseTerrainTile(myLoc.add(Direction.EAST)).equals(TerrainTile.OFF_MAP)) {
                            east = true;
                        }
                        Move.tryMove(Direction.EAST);
                    } else if (!west) {
                        if (rc.senseTerrainTile(myLoc.add(Direction.WEST)).equals(TerrainTile.OFF_MAP)) {
                            west = true;
                        }
                        Move.tryMove(Direction.WEST);
                    } else {
                        //explored the map
                        Commander.mood = Commander.Mood.HARASS;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in Drone scout action");
        }
    }

    public void panicAlert() {
    }
}
