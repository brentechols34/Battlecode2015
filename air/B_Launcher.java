package team163.air;

import java.util.Random;

import team163.utils.Move;
import battlecode.common.*;

/**
 * 
 * sweetness
 * 
 */
public class B_Launcher implements Behavior {
	public static RobotController rc = Launcher.rc;
	RobotInfo[] allies;
	RobotInfo[] enemies;
	MapLocation nearest;
	Random rand = new Random();
	boolean madeItToRally = false;
	public boolean attacking;

	public void perception() {
		allies = rc.senseNearbyRobots(24, Launcher.team);
		enemies = rc.senseNearbyRobots(24, Launcher.team.opponent());
		nearest = Launcher.enemyHQ;

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
		} catch (Exception e) {
			System.out.println("Error in Launcher calculation");
		}
	}

	public void action() {
		try {
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
			} else {
				if (rc.isWeaponReady() && rc.getMissileCount() > 0) {
					Move.tryMove(nearest);
				} else {
					Move.tryMove(rc.getLocation().directionTo(nearest)
							.opposite());
				}
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
					Move.tryMove(aid);
				}
			} else {
				Launcher.panic = false; // no alarm
			}
		} catch (Exception e) {
			System.out.println("Error in Launcher panic alert");
		}
	}
}
