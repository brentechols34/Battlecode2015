package team163.land;

import team163.tanks.*;
import java.util.Random;
import team163.utils.*;

import battlecode.common.*;

public class B_Turtle implements Behavior {

    public RobotController rc;
    RobotInfo[] allies;
    RobotInfo[] enemies;
    MapLocation nearest;
    Random rand = new Random();

    public void setRc(RobotController in) {
        this.rc = in;
    }

    public void perception() {
        try {
            int x = rc.readBroadcast(67);
            int y = rc.readBroadcast(68);
            nearest = new MapLocation(x, y);
        } catch (Exception e) {
            System.out.println("B_Turtle perception error");
        }
        allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
        enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());

    }

    public void calculation() {
        double min = rc.getLocation().distanceSquaredTo(nearest);
        if (allies.length > 0) {
            for (RobotInfo ri : allies) {
                double dis = rc.getLocation().distanceSquaredTo(ri.location);
                if (dis < min) {
                    min = dis;
                    nearest = ri.location;
                }
            }
        }

    }

    public void action() {
        try {
            int x = rc.readBroadcast(67);
            int y = rc.readBroadcast(68);
            MapLocation attLoc = new MapLocation(x, y);
            if (enemies.length > 0 && rc.isWeaponReady()) {
                rc.attackLocation(enemies[0].location);
            } else {
                if (rand.nextBoolean()) {
                    Move.tryMove(attLoc);
                } else {
                    /* move towards nearest */
                    Move.tryMove(nearest);
                }
            }
        } catch (Exception e) {
            System.out.println("Tank Tutle action Error");
        }

    }

    public void panicAlert(MapLocation m) {
        // TODO Auto-generated method stub

    }

}
