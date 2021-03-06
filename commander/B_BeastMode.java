/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.commander;

import battlecode.common.*;
import team163.utils.Move;
import team163.utils.PathMove;

/**
 *
 * @author sweetness
 */
public class B_BeastMode implements Behavior {

    double health = Commander.rc.getHealth();
    double curHealth = health;
    boolean wasHurt = false;
    boolean run = false;
    boolean rush = false; //rush at beavers, miners and launchers
    boolean attacking = false;
    boolean roaming = true; //start by harrassing 
    PathMove pm;
    MapLocation target;
    MapLocation previous;
    MapLocation myLoc;
    MapLocation hq;
    MapLocation nearest;
    MapLocation rally;
    MapLocation goal;
    RobotInfo[] enemies;
    RobotInfo[] allies;

    int countTest = 0;

    public B_BeastMode() {
        try {
            pm = new PathMove(Commander.rc);
            hq = Commander.rc.senseHQLocation();
            previous = hq;
            int xRally = Commander.rc.readBroadcast(50);
            int yRally = Commander.rc.readBroadcast(51);
            rally = new MapLocation(xRally, yRally);
        } catch (Exception e) {
            System.out.println("error in beast mode constroctor " + e);
        }
    }

    public void perception() {
        try {
            myLoc = Commander.rc.getLocation();
            curHealth = Commander.rc.getHealth();

            int xGoal = Commander.rc.readBroadcast(67);
            int yGoal = Commander.rc.readBroadcast(68);
            goal = new MapLocation(xGoal, yGoal);

            enemies = Commander.rc.senseNearbyRobots(24, Commander.opponent);
            allies = Commander.rc.senseNearbyRobots(24, Commander.team);

            if (curHealth < health) {
                wasHurt = true;
            } else {
                wasHurt = false;
            }
            health = curHealth;
        } catch (Exception e) {
            System.out.println("error in commander perception " + e);
        }
    }

    public void calculation() {
        countTest++;
        try {
            if (curHealth < 100) {
                run = true;
            }
            if (wasHurt && curHealth < 110) {
                run = true;
            } else {
                if (curHealth > 130) {
                    run = false;
                }
            }
            if (Commander.rc.readBroadcast(66) == 1) {
                attacking = true;
                roaming = false;
            } else {
                attacking = false;
                roaming = true;
            }

            nearest = Commander.enemyHQ;
            int min = Integer.MAX_VALUE;
            for (RobotInfo ri : enemies) {
                int dis = ri.location.distanceSquaredTo(myLoc);
                if (dis < min) {
                    min = dis;
                    if (ri.type.equals(RobotType.BEAVER)
                            || ri.type.equals(RobotType.MINER)
                            || ri.type.equals(RobotType.LAUNCHER)
                            || ri.type.equals(RobotType.DRONE)) {
                        rush = true;
                    } else {
                        rush = false;
                    }
                    nearest = ri.location;
                }
            }
        } catch (Exception e) {
            System.out.println("error in commander calculation " + e);
        }

    }

    public void action() {
        try {
            if (run) {
                Commander.rc.setIndicatorString(1, "retreating " + " count: " + countTest);
                retreat();
            } else if (attacking) {
                Commander.rc.setIndicatorString(1, "attacking " + goal.toString() + "previous " + previous.toString() + " count: " + countTest);
                if (!previous.equals(goal)) {
                    pm.setDestination(goal);
                    previous = goal;
                }
                attackUnit();
            } else if (roaming) {
                Commander.rc.setIndicatorString(1, "roaming " + rally.toString() + "previous " + previous.toString() + " count: " + countTest);
                if (!previous.equals(rally)) {
                    pm.setDestination(rally);
                    previous = rally;
                }
                roam();
            } else {
                //if for what ever reason nothing else set than defend
                defend(null);
            }
        } catch (Exception e) {
            System.out.println("error in commander action " + e);
        }
    }

    private void attackUnit() {
        try {
            Actions.attack(nearest, true);
            //most units attack is of range 5 so if unit is closer than 7 retreat some
            if (nearest.distanceSquaredTo(myLoc) < 7) {
                Move.tryMove(nearest.directionTo(myLoc));
            }
            MapLocation[] towers = Commander.rc.senseEnemyTowerLocations();
            if (towers.length > 0) {
                MapLocation tow = closest(towers);
                if (tow.distanceSquaredTo(myLoc) < 35) {
                    if (tow.distanceSquaredTo(myLoc) < 34) {
                        Move.tryMove(tow.directionTo(myLoc));
                    }
                }
            } else {
                pm.attemptMove();
            }
        } catch (Exception e) {
            System.out.println("error in attack unit with commander " + e);
        }
    }

    private void roam() {
        try {
            //most units attack is of range 5 so if unit is closer than 7 retreat some
            if (nearest.distanceSquaredTo(myLoc) < 7) {
                Move.tryMove(nearest.directionTo(myLoc));
            }
            Actions.attack(nearest, rush);//if rush true than uses flash
            MapLocation tow = closest(Commander.rc.senseEnemyTowerLocations());
            if (tow.distanceSquaredTo(myLoc) < 35) {
                if (tow.distanceSquaredTo(myLoc) < 34) {
                    Move.tryMove(tow.directionTo(myLoc));
                }
            } else {
                pm.attemptMove();
            }
        } catch (Exception e) {
            System.out.println("error in commander roam " + e);
        }
    }

    private MapLocation closest(MapLocation[] in) {
        MapLocation closest = myLoc;
        int min = Integer.MAX_VALUE;
        for (MapLocation m : in) {
            int dis = m.distanceSquaredTo(myLoc);
            if (dis < min) {
                min = dis;
                closest = m;
            }
        }
        return closest;
    }

    private void defend(MapLocation m) {
        try {
            MapLocation closest;
            if (m == null) {
                MapLocation[] towers = Commander.rc.senseTowerLocations();
                int min = Integer.MAX_VALUE;
                closest = hq;
                for (MapLocation t : towers) {
                    int dis = t.distanceSquaredTo(myLoc);
                    if (dis < min) {
                        min = dis;
                        closest = t;
                    }
                }
            } else {
                closest = m;
            }
            if (enemies.length > 0) {
                Actions.attack(nearest, true);
            } else {
                if (Commander.rc.getFlashCooldown() > 0) {
                    MapLocation tow = closest(Commander.rc.senseEnemyTowerLocations());
                    if (tow.distanceSquaredTo(myLoc) < 35) {
                        if (tow.distanceSquaredTo(myLoc) < 34) {
                            Move.tryMove(tow.directionTo(myLoc));
                        }
                    } else {
                        if (!previous.equals(closest)) {
                            pm.setDestination(closest);
                            previous = closest;
                        }
                        pm.attemptMove();
                    }
                } else {
                    //flash
                    Actions.flash(closest);
                }
            }
        } catch (Exception e) {
            System.out.println("error in commander defend " + e);
        }
    }


    //try and use flash also -- fighting retreat
    private void retreat() {
        try {
            Actions.tryAttack(enemies, false);
            //retreat to closest tower
            MapLocation[] towers = Commander.rc.senseTowerLocations();
            int max = Integer.MAX_VALUE;
            MapLocation closest = hq;
            for (MapLocation t : towers) {
                int dis = t.distanceSquaredTo(myLoc);
                if (dis < max) {
                    max = dis;
                    closest = t;
                }
            }
            Actions.kiteMove(closest, Commander.rc.senseEnemyTowerLocations(), 33);
        } catch (Exception e) {
            System.out.println("error in retreat " + e);
        }
    }

    public void panicAlert() {
        //currently does not respond to panic alerts
    }

}
