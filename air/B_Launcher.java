package team163.air;

import java.util.Random;

import team163.utils.BasicBugger;
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
    MapLocation myLoc = Launcher.rc.getLocation();
    MapLocation previous = Launcher.rc.getLocation();
    Random rand = new Random();
    boolean madeItToRally = false;
    public boolean attacking;
    boolean nearTower = false;
    PathMove pm = new PathMove(Launcher.rc);
    BasicBugger b = new BasicBugger(Launcher.rc);
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
            myLoc = rc.getLocation();
            xGoal = rc.readBroadcast(67);
            yGoal = rc.readBroadcast(68);
            checkTarget();
            rc.setIndicatorString(0, target.toString());
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
    
	public void checkTarget() throws GameActionException{
		int x = rc.readBroadcast(67);
		int y = rc.readBroadcast(68);
		MapLocation r = new MapLocation(x,y);
		RobotInfo ri;
		if (rc.canSenseLocation(r) && (((ri=rc.senseRobotAtLocation(r)) != null && (ri.type != RobotType.TOWER && ri.type != RobotType.HQ)) || ri == null)) {
			retarget();
		}

	}

	public void retarget() throws GameActionException {
		MapLocation[] towers = rc.senseEnemyTowerLocations();
		if (towers.length == 0) {
			MapLocation enHQ = rc.senseEnemyHQLocation();
			rc.broadcast(67, enHQ.x);
			rc.broadcast(68, enHQ.y);
			nearest = enHQ;
		} else {
			int closest = findClosestToHQ(towers);
			rc.broadcast(67, towers[closest].x);
			rc.broadcast(68, towers[closest].y);
			nearest = towers[closest];
		}

	}
	
	public static int findClosestToHQ(MapLocation[] locs) {
		int mindex = 0;
		MapLocation hq = rc.senseHQLocation();
		int minDis = hq.distanceSquaredTo(locs[0]);
		for (int i = 1; i < locs.length; i++) {
			int dis = hq.distanceSquaredTo(locs[i]);
			if (locs[i] != null && dis < minDis) {
				mindex = i;
				minDis = dis;
			}
		}
		return mindex;
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
                    b.setDestination(target);
                }
            } else {
                if (previous.x != xRally && previous.y != yRally) {
                    target = new MapLocation(xRally, yRally);
                    pm.setDestination(target);
                    b.setDestination(target);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in Launcher calculation " + e);
        }
    }

    private void launchThemAll(MapLocation n) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(n);
        for (int j = 0; j < 5; j++) {
            if (rc.getMissileCount() > 0) {
                for (int i = 0; i < 8; i++) {
                    if (rc.canLaunch(dir)) {
                        if (rc.isWeaponReady()) {
                            rc.launchMissile(dir);
                        }
                        break;
                    } else {
                        dir.rotateLeft();
                    }
                }
            }
        }
    }

    public void action() {
        try {
            if (enemies.length > 0) {
                target();
                if (wasHurt) {
                    Move.tryMove(rc.getLocation().directionTo(nearest)
                            .opposite());
                }
            } else {
                if (!Move.inTowerRange(myLoc, rc.senseEnemyTowerLocations(), 35)) {
                    nearTower = false;
                } else {
                    nearTower = true;
                }
                if (nearTower) {
                    if (isClear(target)) launchThemAll(target);
                    else b.attemptMove();
                } else {
                    previous = target;
                    pm.attemptMove();
                }
            }
        } catch (Exception e) {
            System.out.println("Launcher action Error");
            e.printStackTrace();
        }
    }
    
    public void target() throws GameActionException {
    	for (RobotInfo r : enemies) {
    		if (isClear(r.location)) {
    			launchThemAll(r.location);
    			return;
    		}
    	}
    }
    
    public boolean isClear(MapLocation m) throws GameActionException {
    	Direction d = myLoc.directionTo(m);
    	MapLocation t = new MapLocation(myLoc.x,myLoc.y).add(d);
    	while (!t.equals(m)) {
    		d = t.directionTo(m);
    		if (!rc.canSenseLocation(m)) return true;
    		RobotInfo r = rc.senseRobotAtLocation(t);
    		if (r != null && r.team == rc.getTeam() && r.type != RobotType.MISSILE) {
    			return false;
    		}
    		t = t.add(d);
    	}
    	return true;
    }

    public void panicAlert() {
        // read and see if someone is under attack
    	rc.setIndicatorString(1,"Panic: " + Clock.getRoundNum());
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
                        if (!pm.goal.equals(nearest)) pm.setDestination(nearest);
                    }
                    pm.attemptMove();
                }
            } else {
                Launcher.panic = false; // no alarm
            }
        } catch (Exception e) {
            System.out.println("Error in Launcher panic alert");
        }
    }
}
