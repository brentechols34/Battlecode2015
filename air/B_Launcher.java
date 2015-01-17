package team163.air;

import java.util.Random;

import team163.utils.Move;
import battlecode.common.*;
import team163.utils.PathMove;

/**
 *
 * sweetness rally on 50 and 51 67 68 attacking / goal locations
 *
 */
public class B_Launcher implements Behavior {

    public static RobotController rc = Launcher.rc;
    RobotInfo[] allies;
    RobotInfo[] enemies;
    MapLocation nearest;
    MapLocation target;
    MapLocation previous = Launcher.rc.getLocation();
    Random rand = new Random();
    boolean madeItToRally = false;
    public boolean attacking;
    PathMove pm = new PathMove(Launcher.rc);
    int xRally;
    int yRally;
    int xGoal;
    int yGoal;
    boolean attack = false;
    boolean wasHurt = false;
    double health = Launcher.rc.getHealth();

    {
        try {
            xRally = Launcher.rc.readBroadcast(50);
            yRally = Launcher.rc.readBroadcast(51);
            target = new MapLocation(xRally, yRally);
        } catch (Exception e) {
            System.out.println("error setting rally " + e);
        }
    }

    public void perception() {
        try {
            allies = rc.senseNearbyRobots(24, Launcher.team);
            enemies = rc.senseNearbyRobots(24, Launcher.team.opponent());
            nearest = Launcher.enemyHQ;
            xGoal = rc.readBroadcast(67);
            yGoal = rc.readBroadcast(68);
            if (rc.readBroadcast(66) == 1) {
                attack = true;
            } else {
                attack = false;
            }
            if (Launcher.rc.getHealth() < health) {
                health = Launcher.rc.getHealth();
                wasHurt = true;
            } else {
                wasHurt = false;
            }
        } catch (Exception e) {
            System.out.println("Error in Launcher perception " + e);
        }
    }

    public void calculation() {
        try {
            double max = rc.getLocation().distanceSquaredTo(Launcher.enemyHQ);
            if (enemies.length > 0) {
                for (RobotInfo ri : enemies) {
                    double dis = rc.getLocation()
                            .distanceSquaredTo(ri.location);
                    if (dis < max) {
                        max = dis;
                        nearest = ri.location;
                    }
                }
            }

            if (attack) {
                if (previous.x != xGoal && previous.y != yGoal) {
                    target = new MapLocation(xGoal, yGoal);
                    pm.setDestination(target);
                }
            } else {
                if (previous.x != xRally && previous.y != yRally) {
                    target = new MapLocation(xRally, yRally);
                    pm.setDestination(target);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in Launcher calculation " + e);
        }
    }

    public void action() {
        try {
            if (enemies.length > 0) {
                Direction dir = rc.getLocation().directionTo(nearest);
                while (rc.isWeaponReady() && rc.getMissileCount() > 0) {
                    for (int i = 0; i < 8; i++) {
                        if (rc.canLaunch(dir)) {
                            rc.launchMissile(dir);
                            break;
                        } else {
                            dir.rotateLeft();
                        }
                    }
                }
                if (wasHurt) {
                    Move.tryMove(rc.getLocation().directionTo(nearest)
                            .opposite());
                }
            } else {
                previous = target;
                pm.attemptMove();
            }
        } catch (Exception e) {
            System.out.println("Launcher action Error");
        }
    }

    public void panicAlert() {
        // read and see if someone is under attack
        try {
            int panicX = rc.readBroadcast(911);
            if (panicX != 0) { // try to assist
                rc.setIndicatorDot(rc.getLocation(), 2, 2, 2);
                Direction dir = rc.getLocation().directionTo(nearest);
                if (rc.isWeaponReady() && enemies.length > 0
                        && rc.getMissileCount() > 0) {
                    for (int i = 0; i < 8; i++) {
                        if (rc.canLaunch(dir)) {
                            rc.launchMissile(dir);
                            break;
                        } else {
                            dir.rotateLeft();
                        }
                    }
                }
                Launcher.panic = true;
                int panicY = rc.readBroadcast(912);
                MapLocation aid = new MapLocation(panicX, panicY);
                // if greater than 5 enemies be a coward
                if (rc.getLocation().distanceSquaredTo(aid) < 7
                        && (enemies.length < 1 || enemies.length > 5)) {
                    rc.broadcast(911, 0); // no enemies so reset alarm
                    rc.broadcast(912, 0);
                    Launcher.panic = false; // give up or no enemies
                } else {
                    if (nearest != previous) {
                        previous = nearest;
                        pm.attemptMove();
                    }
                }
            } else {
                Launcher.panic = false; // no alarm
            }
        } catch (Exception e) {
            System.out.println("Error in Launcher panic alert");
        }
    }
}
