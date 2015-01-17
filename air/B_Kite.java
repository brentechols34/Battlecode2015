/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.*;
import static team163.utils.Move.*;
import team163.utils.AttackUtils;

/**
 * Kite around stuff. Made specifically for drones
 *
 * @author sweetness
 */
public class B_Kite implements Behavior {

    RobotInfo[] enemies;
    RobotInfo[] allies;
    RobotInfo closestEnemy = null;
    MapLocation[] towers;
    MapLocation target = Drone.enemyHQ;
    MapLocation nearest;
    MapLocation myLoc;
    double health = Drone.rc.getHealth();
    boolean wasHurt = false;
    boolean rush = false;
    int enemyCount = 0;

    public void perception() {
        try {
            enemyCount = 0;
            enemies = Drone.rc.senseNearbyRobots(24, Drone.opponent);
            allies = Drone.rc.senseNearbyRobots(24, Drone.team);
            myLoc = Drone.rc.getLocation();
            towers = Drone.rc.senseEnemyTowerLocations(); // for 6 tower locations
            if (Drone.rc.getHealth() != health) {
                wasHurt = true;
                health = Drone.rc.getHealth();
            } else {
                wasHurt = false;
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
                double dis = x.location.distanceSquaredTo(myLoc);
                if (dis < max) {
                    if (x.type.compareTo(RobotType.MINER) == 0
                            || x.type.compareTo(RobotType.BEAVER) == 0
                            || x.type == RobotType.LAUNCHER
                            || AttackUtils.isStationary(x.type)) {
                        rush = true;
                    } else {
                        enemyCount++;
                        rush = false;
                    }

                    if (closestEnemy == null || (closestEnemy.type == RobotType.MISSILE && x.type == RobotType.MISSILE) || (x.type != RobotType.MISSILE)) {
                        closestEnemy = x;
                        nearest = x.location;
                        max = dis;
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
                    if (enemyCount > allies.length || wasHurt) {
                        tryKite(myLoc.add(nearest.directionTo(myLoc)), towers);
                    } else {
                        if (rush) {//closest is miner so press attack
                            tryKite(nearest, towers);
                        }
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
        //read and see if someone is under attack
        try {
            int panicX = Drone.rc.readBroadcast(911);
            if (panicX != 0) { //try to assist
                if (enemies.length > 0 && Drone.rc.isWeaponReady()
                        && Drone.rc.canAttackLocation(nearest)) {
                    Drone.rc.attackLocation(nearest);
                }
                Drone.panic = true;
                int panicY = Drone.rc.readBroadcast(912);
                MapLocation aid = new MapLocation(panicX, panicY);
                // if greater than 5 enemies be a coward
                if (Drone.rc.getLocation().distanceSquaredTo(aid) < 7
                        && (enemies.length < 1 || enemies.length > 5)) {
                    Drone.rc.broadcast(911, 0); //no enemies so reset alarm
                    Drone.rc.broadcast(912, 0);
                    Drone.panic = false; //give up or no enemies
                } else {
                    tryKite(aid, towers);
                }
            } else {
                Drone.panic = false; //no alarm
            }
        } catch (Exception e) {
            System.out.println("Error in Drone panic alert" + e);
        }
    }
}
