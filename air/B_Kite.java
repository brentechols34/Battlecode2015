/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.*;

/**
 * Kite around stuff. Made specifically for drones
 *
 * @author sweetness
 */
public class B_Kite implements Behavior {

    RobotInfo[] enemies;
    MapLocation[] towers;
    MapLocation target = Drone.enemyHQ;
    MapLocation nearest;
    MapLocation myLoc;
    Direction pre = Drone.rc.getLocation().directionTo(Drone.enemyHQ);

    public void perception() {
        try {
            enemies = Drone.rc.senseNearbyRobots(24, Drone.opponent);
            myLoc = Drone.rc.getLocation();
            towers = new MapLocation[6]; // for 6 tower locations

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

    private boolean inTowerRange(MapLocation m) {
        for (MapLocation x : towers) {
            if (x != null && x.x != 0 && x.y != 0) {
                if (m.distanceSquaredTo(x) < 26) {
                    return true;
                }
            }
        }
        return false;
    }

    public void action() {
        try {
            // try moving toward target
            if (enemies != null && enemies.length > 0) {
                if (nearest == null) {
                    nearest = enemies[0].location;
                }
                if (Drone.rc.isWeaponReady()
                        && Drone.rc.canAttackLocation(nearest)) {
                    Drone.rc.attackLocation(nearest);
                }
                if (inTowerRange(myLoc)) {
                    team163.utils.Move.tryMove(enemies[0].location.directionTo(myLoc));
                }
            } else {
                Direction dir = pre;
                MapLocation next = myLoc.add(dir);
                boolean check = true;
                int count = 8;
                while (check && count-- > 0) {
                    for (MapLocation x : towers) {
                        if (x != null && x.x != 0 && x.y != 0) {
                            for (int j = 0; j < 8; j++) {
                                if (x.distanceSquaredTo(next) < 26) {
                                    Direction nDir = (Drone.right) ? dir.rotateRight() : dir
                                            .rotateLeft(); // turn right or left
                                    next = myLoc.add(nDir);
                                }
                            }
                        }
                    }

                    if (Drone.rc.canSenseLocation(Drone.enemyHQ)) {
                        for (int j = 0; j < 8; j++) {
                            if (next.distanceSquaredTo(Drone.enemyHQ) < 30
                                    && count-- > 0) {
                                dir = (Drone.right) ? dir.rotateRight() : dir
                                        .rotateLeft();
                                next = myLoc.add(dir);
                            }
                        }
                    }
                    // reached the end to the next location is good
                    check = false;
                }

                //curve toward target
                boolean canChange = true;
                while (target.y > next.y && canChange) {
                    Drone.rc.setIndicatorString(1, "can change y +");
                    MapLocation newLoc = new MapLocation(next.x, next.y + 1);
                    double dis = newLoc.y - myLoc.y;
                    if (dis > 1 || dis < -1) {
                        break;
                    }
                    if (newLoc.compareTo(myLoc) == 0) {
                        Direction d = myLoc.directionTo(target).opposite();
                        newLoc = myLoc.add((Drone.right) ? d.rotateRight() : d
                                .rotateLeft());
                    }

                    canChange = !inTowerRange(newLoc);
                    if (newLoc.distanceSquaredTo(Drone.enemyHQ) < 30) {
                        canChange = false;
                    }
                    if (canChange) {
                        next = newLoc;
                    }
                }

                canChange = true;
                while (target.y < next.y && canChange) {
                    Drone.rc.setIndicatorString(1, "can change y -");
                    MapLocation newLoc = new MapLocation(next.x, next.y - 1);
                    double dis = newLoc.y - myLoc.y;
                    if (dis > 1 || dis < -1) {
                        break;
                    }
                    if (newLoc.compareTo(myLoc) == 0) {
                        Direction d = myLoc.directionTo(target).opposite();
                        newLoc = myLoc.add((Drone.right) ? d.rotateRight() : d
                                .rotateLeft());
                    }

                    canChange = !inTowerRange(newLoc);
                    if (newLoc.distanceSquaredTo(Drone.enemyHQ) < 30) {
                        canChange = false;
                    }
                    if (canChange) {
                        next = newLoc;
                    }
                }

                canChange = true;
                while (target.x < next.x && canChange) {
                    Drone.rc.setIndicatorString(1, "can change x -");
                    MapLocation newLoc = new MapLocation(next.x - 1, next.y);
                    double dis = newLoc.x - myLoc.x;
                    if (dis > 1 || dis < -1) {
                        break;
                    }
                    if (newLoc.compareTo(myLoc) == 0) {
                        Direction d = myLoc.directionTo(target).opposite();
                        newLoc = myLoc.add((Drone.right) ? d.rotateRight() : d
                                .rotateLeft());
                    }

                    canChange = !inTowerRange(newLoc);
                    if (newLoc.distanceSquaredTo(Drone.enemyHQ) < 30) {
                        canChange = false;
                    }
                    if (canChange) {
                        next = newLoc;
                    }
                }

                canChange = true;
                while (target.x > next.x && canChange) {
                    Drone.rc.setIndicatorString(1, "can change x +" + (next.x + 1));
                    MapLocation newLoc = new MapLocation(next.x + 1, next.y);
                    double dis = newLoc.x - myLoc.x;
                    if (dis > 1 || dis < -1) {
                        break;
                    }
                    if (newLoc.compareTo(myLoc) == 0) {
                        Direction d = myLoc.directionTo(target).opposite();
                        newLoc = myLoc.add((Drone.right) ? d.rotateRight() : d
                                .rotateLeft());
                    }

                    canChange = !inTowerRange(newLoc);
                    if (newLoc.distanceSquaredTo(Drone.enemyHQ) < 30) {
                        canChange = false;
                    }
                    if (canChange) {
                        next = newLoc;
                    }
                }

                dir = myLoc.directionTo(next);

                if (Drone.rc.senseTerrainTile(next) == TerrainTile.OFF_MAP) {
                    dir = dir.opposite();
                }

                if (Drone.rc.isCoreReady()) {
                    for (int i = 0; i < 8; i++) {
                        if (Drone.rc.canMove(dir) && !inTowerRange(myLoc.add(dir))) {
                            pre = dir;
                            Drone.rc.move(dir);
                            break;
                        } else {
                            dir = (Drone.right) ? dir.rotateRight() : dir
                                    .rotateLeft();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in Kite action for Drone" + e);
        }

    }

    public void panicAlert() {
    }

}
