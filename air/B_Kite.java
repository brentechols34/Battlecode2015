/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.*;
import static team163.utils.Move.*;

/**
 * Kite around stuff. Made specifically for drones
 *
 * @author sweetness
 */
public class B_Kite implements Behavior {

    RobotInfo[] enemies;
    RobotInfo[] allies;
    MapLocation[] towers;
    MapLocation target = Drone.enemyHQ;
    MapLocation nearest;
    MapLocation myLoc;
    double health = Drone.rc.getHealth();
    boolean wasHurt = false;

    public void perception() {
        try {
            enemies = Drone.rc.senseNearbyRobots(24, Drone.opponent);
            allies = Drone.rc.senseNearbyRobots(24, Drone.team);
            myLoc = Drone.rc.getLocation();
            towers = new MapLocation[6]; // for 6 tower locations
            if (Drone.rc.getHealth() != health) {
                wasHurt = true;
                health = Drone.rc.getHealth();
            } else {
                wasHurt = false;
            }

            // read tower locations
            for (int i = 0; i < 6; i++) {
                int chan = (2 * i) + 800;
                int x = Drone.rc.readBroadcast(chan);
                int y = Drone.rc.readBroadcast(chan + 1);
                towers[i] = new MapLocation(x, y);
            }
        } catch (Exception e) {
            System.out.println("Error in perception with Kite by Drone");
            e.printStackTrace();
        }
    }

    public void calculation() {
        try {
            double max = Double.MAX_VALUE;
            for (RobotInfo x : enemies) {
                if (x.type == RobotType.TOWER) {
                    MapLocation at = x.location;
                    int index = 0; // place in towers array
                    boolean newTower = true;
                    for (int i = 0; i < 6; i++) {
                        if (towers[i].x == at.x && towers[i].y == at.y) {
                            newTower = false;
                        }
                        if (towers[i].x == 0 && towers[i].y == 0) {
                            index = i;
                        }
                    }
                    if (newTower) {
                        int chan = (2 * index) + 800;
                        Drone.rc.broadcast(chan, at.x);
                        Drone.rc.broadcast(chan + 1, at.y);
                        towers[index] = new MapLocation(at.x, at.y);
                    }
                } else {
                    double dis = x.location.distanceSquaredTo(myLoc);
                    if (dis < max) {
                        max = dis;
                        nearest = x.location;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in calculation with da Kite");
            e.printStackTrace();
        }
    }

    public void action() {
        try {
            // try moving toward target
            MapLocation toAttack = new MapLocation(Drone.rc.
                    readBroadcast(825), Drone.rc.readBroadcast(826));
            if (myLoc.distanceSquaredTo(toAttack) < 30 && toAttack.x != 0
                    && toAttack.y != 0) {
                if (Drone.rc.canSenseLocation(toAttack)) {
                    RobotInfo ri = Drone.rc.senseRobotAtLocation(toAttack);
                    if (ri != null && ri.team != Drone.team) {
                        if (Drone.rc.canAttackLocation(toAttack)
                                && Drone.rc.isWeaponReady()) {
                            Drone.rc.attackLocation(toAttack);
                        } else {
                            //no drone there reset
                            Drone.rc.broadcast(825, 0);
                            Drone.rc.broadcast(826, 0);

                        }
                    }
                } else {
                    tryKite(toAttack, towers);
                }
            }

            if (enemies.length > 0) {
                if (nearest == null) { //case of a tower
                    nearest = enemies[0].location;
                }
                if (Drone.rc.isWeaponReady()
                        && Drone.rc.canAttackLocation(nearest)) {
                    //check if near attack point

                    Drone.rc.attackLocation(nearest);
                    Drone.rc.broadcast(825, nearest.x);
                    Drone.rc.broadcast(826, nearest.y);
                } else {
                    if (enemies.length > allies.length && wasHurt) {
                        tryKite(myLoc.add(nearest.directionTo(myLoc)), towers);
                    }

                }
                if (inTowerRange(myLoc, towers)) { //case of tower
                    team163.utils.Move.tryMove(enemies[0].location.directionTo(myLoc));
                }
            } else {
                tryKite(target, towers);
            }
        } catch (Exception e) {
            System.out.println("Error in Kite action for Drone" + e);
        }

    }

    public void panicAlert() {
    }

}
