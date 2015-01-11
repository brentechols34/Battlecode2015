package team163.land;

import team163.tanks.*;
import team163.utils.*;
import battlecode.common.*;

public class B_Attack implements Behavior {

    RobotController rc;
    MapLocation nearest; /* nearest enemy */

    MapLocation goal;
    RobotInfo[] enemies;

    public void setRc(RobotController in) {
        this.rc = in;
    }

    public void perception() {
        /* for now simply look for nearest enemy */
        try {
            int x = rc.readBroadcast(67);
            int y = rc.readBroadcast(68);
            nearest = new MapLocation(x, y);
        } catch (Exception e) {
            System.out.println("B_Attack perception error");
        }
        enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());

    }

    public void calculation() {

        /* for now just calculating distance to nearest enemy */
        double max = rc.getLocation().distanceSquaredTo(nearest);
        if (enemies.length > 0) {
            for (RobotInfo ri : enemies) {
                double dis = rc.getLocation().distanceSquaredTo(ri.location);
                if (dis < max) {
                    max = dis;
                    nearest = ri.location;
                }
                if (ri.type == RobotType.TOWER) {
                    MapLocation towerLoc = ri.location;
                    try {
                        rc.broadcast(67, towerLoc.x);
                        rc.broadcast(68, towerLoc.y);
                    } catch (Exception e) {
                        System.out.println("Issue in B_attack, broadcasting");
                    }
                }
            }
        }

    }

    public void action() {
        try {
            int x = rc.readBroadcast(67);
            int y = rc.readBroadcast(68);
            MapLocation tempLoc = new MapLocation(x, y);
            if (!tempLoc.equals(goal)) {
                goal = tempLoc;
            }
            RobotInfo ri;
            if (rc.canSenseLocation(goal) && ((ri = rc.senseRobotAtLocation(goal)) == null || ri.type != RobotType.TOWER)) {
                MapLocation enemyHQ = rc.senseEnemyHQLocation();
                rc.broadcast(67, enemyHQ.x);
                rc.broadcast(68, enemyHQ.y);
                goal = enemyHQ;
            }
            /* try to attack the nearest if unable than move towards it */
            if (rc.isWeaponReady()) {
                if (rc.canAttackLocation(nearest)) {
                    rc.attackLocation(nearest);
                } else {
                    /* move towards nearest */
                    Move.tryMove(nearest);
                }
            } else {
                if (!nearest.equals(goal)) {
                    /* if weapon is not ready run from enemy */
                    Direction away = rc.getLocation().directionTo(nearest)
                            .opposite();
                    Move.tryMove(away);
                } else {
                    Move.tryMove(nearest);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in Tank Attack behavior action");
            e.printStackTrace();
        }
    }

    public void panicAlert(MapLocation m) {
        // TODO Auto-generated method stub

    }

}
