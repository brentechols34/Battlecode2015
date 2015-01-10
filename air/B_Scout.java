/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.*;

/**
 * Drone that scouts out enviornment Channels 500 to 600 are set for scouting
 * reports
 *
 * @author sweetness
 */
public class B_Scout implements Behavior {
    
    RobotController rc = Drone.rc;
    boolean map[][] = new boolean[1000][1000]; // HQ in the center
    boolean explored[][] = new boolean[1000][1000]; // HQ in the center
    short toBrodcast[];
    int senseRange = 24;
    int chan = 500;
    int index = 0;
    
    RobotInfo[] nearRobots;
    RobotInfo[] enemies;

    /**
     * Gather scouting information
     */
    public void perception() {
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
            loc = new MapLocation(loc.x - (senseRange / 2), loc.y + (senseRange / 2));
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
            for (int i = 0; i < index; i++) {
                rc.broadcast(((chan++) % 100), toBrodcast[i]);
            }
            
            if (enemies.length > 1) {
                if (rc.isWeaponReady()) {
                    rc.attackLocation(enemies[0].location);
                } else {
                    /* run away if can not fire */
                    Move.tryMove(enemies[0].location.directionTo(rc.getLocation()));
                }
            } else {
                if (rc.isCoreReady()) {
                    Move.ranMove();
                }
            }
        } catch (Exception e) {
            System.out.println("Error in Drone scout action");
        }
    }
    
    public void panicAlert(MapLocation m) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
