/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.commander;

import battlecode.common.*;

/**
 *
 * @author sweetness
 */
public class B_Harass implements Behavior {

    RobotInfo[] enemies;
    RobotInfo[] allies;
    boolean lowHealth = false;
    boolean surrounded = false;
    boolean building = false; //am attacking a building
    boolean needSupply = false;

    public void perception() {
        enemies = Commander.rc.senseNearbyRobots(24, Commander.opponent);
        allies = Commander.rc.senseNearbyRobots(24, Commander.team);
        if (Commander.rc.getHealth() < 110) {
            lowHealth = true;
        } else {
            if (Commander.rc.getHealth() > 150) {
                lowHealth = false;
            }
        }
        //if (Commander.rc.getSupplyLevel() < 10) {
        //    needSupply = true;
        //} else {
            needSupply = false;
        //}
    }

    public void calculation() {
        surrounded = isSurrounded();
    }

    private boolean isSurrounded() {
        //test if surrounded
        boolean $ = false;
        boolean $$ = false;
        boolean $$$ = false;
        boolean $$$$ = false;
        for (RobotInfo x : enemies) {
            MapLocation loc = x.location;
            if (loc.x > Commander.myLoc.x && loc.y > Commander.myLoc.y) {
                $ = true;
            }
            if (loc.x < Commander.myLoc.x && loc.y < Commander.myLoc.y) {
                $$ = true;
            }
            if (loc.x > Commander.myLoc.x && loc.y < Commander.myLoc.y) {
                $$$ = true;
            }
            if (loc.x < Commander.myLoc.x && loc.y > Commander.myLoc.y) {
                $$$$ = true;
            }
        }
        if ($ && $$ && $$$ && $$$$) {
            return true;
        }
        return false;
    }

    public void action() {
        try {
            if (needSupply) {
                Actions.tryAttack(enemies, false);
                Actions.safeMove(Commander.hq);
            } else if ((enemies.length > 5 && allies.length < 3)
                    || lowHealth || surrounded) {
                retreat();
            } else {
                //attacking a building!!
                if (building) {
                    Commander.rc.setIndicatorString(1, "go bulldog on a building");
                    building = Actions.tryTarget(enemies);
                } else {
                    if (enemies.length > 0) {
                        building = Actions.tryAttack(enemies, true);
                        MapLocation c = Actions.closestRobot(enemies);
                        if (c.distanceSquaredTo(Commander.myLoc) < 7) {
                            Actions.directSafe(c.directionTo(Commander.myLoc));
                        } else if (c.distanceSquaredTo(Commander.myLoc) > 10) {
                            Actions.directSafe(Commander.myLoc.directionTo(c));
                        }
                    } else {
                        Actions.safeMove(Commander.enemyHQ);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("error in harassing action " + e);
        }
    }

    public void panicAlert() {
    }

    //try and use flash also -- fighting retreat
    private void retreat() {
        try {
            Actions.tryAttack(enemies, false);
            //retreat to closest tower
            MapLocation[] towers = Commander.rc.senseTowerLocations();
            int max = Integer.MAX_VALUE;
            MapLocation closest = Commander.hq;
            for (MapLocation t : towers) {
                int dis = t.distanceSquaredTo(Commander.myLoc);
                if (dis < max) {
                    max = dis;
                    closest = t;
                }
            }
            Actions.directSafe(Commander.myLoc.directionTo(closest));
            //Actions.safeMove(closest);
            Commander.rc.setIndicatorString(1, "running away to location " + closest.toString());
        } catch (Exception e) {
            System.out.println("error in retreat " + e);
        }
    }
}
