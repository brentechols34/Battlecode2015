package team163.air;

import battlecode.common.*;

public class B_Missile implements Behavior {

    double ratio;
    RobotInfo[] robots;
    MapLocation loc;
    MapLocation myLoc;

    public void perception() {
        try {
            robots = Missile.rc.senseNearbyRobots(24);
            myLoc = Missile.rc.getLocation();
            //int x = Missile.rc.readBroadcast(Missile.channel);
            //int y = Missile.rc.readBroadcast(Missile.channel + 1);
            //loc = new MapLocation(x, y);
        } catch (Exception e) {
            System.out.println("Error in missile perception");
        }
    }

    public void calculation() {
        double enemy = 0;
        double allie = 0;

        for (RobotInfo x : robots) {
            if (x.team == Missile.team) {
                allie++;
            } else {
                loc = x.location;
                if (loc.isAdjacentTo(myLoc)) {
                    enemy++;
                }
            }
        }

        if (allie == 0 && enemy > 0) {
            ratio = 1;
        } else {
            ratio = enemy / allie;
        }
    }

    public void action() {
        try {
            if (ratio > .5) {
                Missile.rc.explode();
            } else {
                if (loc == null) {
                    loc = Missile.enemyHQ;
                }
                Direction dir = Missile.rc.getLocation().directionTo(loc);
                if (Missile.rc.canMove(dir) && Missile.rc.isCoreReady()) {
                    Missile.rc.move(dir);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in Missile action");
            e.printStackTrace();
        }

    }

    public void panicAlert() {
        // TODO Auto-generated method stub

    }
}
