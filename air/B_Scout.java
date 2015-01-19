/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import team163.utils.*;
import battlecode.common.*;

/**
 *
 * @author sweetness
 */
public class B_Scout implements Behavior {

    public static RobotController rc = Drone.rc;
    boolean map[][] = new boolean[1000][1000]; // HQ in the center
    boolean explored[][] = new boolean[1000][1000]; // HQ in the center
    short toBrodcast[];
    int senseRange = 24;
    int chan = 500;
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
        myLoc = Drone.rc.getLocation();

        /* sense everything within range */
        nearRobots = rc.senseNearbyRobots(senseRange);
        enemies = rc.senseNearbyRobots(Drone.range, Drone.team.opponent());
    }

    /**
     * Test terrain and look at robot info
     */
    public void calculation() {
        try {
            MapLocation loc = rc.getLocation();
            /* adjust loc to be upper left extreem of sense range */
            loc = new MapLocation(loc.x - (senseRange / 2), loc.y
                    + (senseRange / 2));
            toBrodcast = new short[senseRange * senseRange];

            /* scan terains not explered withing range */
            index = 0;
            for (int i = 0; i < senseRange; i++) {
                for (int j = 0; j < senseRange; j++) {
                    int hqx = (loc.x - Drone.hq.x);
                    int hqy = (loc.y - Drone.hq.y);
                    int x = 500 + hqx;
                    int y = 500 + hqy;
                    if (!explored[x][y]) {
                        if (rc.senseTerrainTile(loc) != TerrainTile.NORMAL) {
                            toBrodcast[index++] = (short) ((hqx << 8) | (hqy & 0x00ff));
                        }
                        explored[x][y] = true;
                    }
                }
            }
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
                    AttackUtils.attackSomething(Drone.rc, Drone.range, Drone.opponent);
                } else {
                    /* run away if can not fire */
                    Move.tryKite(enemies[0].location, Drone.rc.senseEnemyTowerLocations());
                }
            } else {
                if (rc.isCoreReady()) {
                    if (!south) {
                        if (Drone.rc.senseTerrainTile(myLoc.add(Direction.SOUTH)).equals(TerrainTile.OFF_MAP)) {
                            south = true;
                        }
                        Move.tryMove(Direction.SOUTH);
                    } else if (!northEast) {
                        if (Drone.rc.senseTerrainTile(myLoc.add(Direction.NORTH)).equals(TerrainTile.OFF_MAP)) {
                            northEast = true;
                            north = true;
                        }
                        if (Drone.rc.senseTerrainTile(myLoc.add(Direction.EAST)).equals(TerrainTile.OFF_MAP)) {
                            northEast = true;
                            east = true;
                        }
                        Move.tryMove(Direction.NORTH_EAST);
                    } else if ((count--) > 0) {
                        if (Drone.rc.senseTerrainTile(myLoc.add(Direction.WEST)).equals(TerrainTile.OFF_MAP)) {
                            north = true;
                        }
                        Move.tryMove(Direction.WEST);
                    } else if (!north) {
                        if (Drone.rc.senseTerrainTile(myLoc.add(Direction.NORTH)).equals(TerrainTile.OFF_MAP)) {
                            north = true;
                        }
                        Move.tryMove(Direction.NORTH);
                    } else if ((count2--) > 0) {
                        Move.tryMove(Direction.SOUTH);
                    } else if (!east) {
                        if (Drone.rc.senseTerrainTile(myLoc.add(Direction.EAST)).equals(TerrainTile.OFF_MAP)) {
                            east = true;
                        }
                        Move.tryMove(Direction.EAST);
                    } else if (!west) {
                        if (Drone.rc.senseTerrainTile(myLoc.add(Direction.WEST)).equals(TerrainTile.OFF_MAP)) {
                            west = true;
                        }
                        Move.tryMove(Direction.WEST);
                    } else {
                        //explored the map
                        AlertDrone.run(rc);
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
