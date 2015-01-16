package team163.tanks;

import java.util.Random;

import team163.logistics.PathBeaver;
import team163.utils.*;
import battlecode.common.*;

public class B_Turtle implements Behavior {

	static RobotController rc = Tank.rc;
	RobotInfo[] allies;
	RobotInfo[] enemies;
	MapLocation nearest;
	MapLocation rally;
	MapLocation goal;
	Random rand = new Random();
	boolean pathSet = false;
	int pathCount = 0;
	public boolean madeItToRally = false;
	public boolean attacking;
	boolean offPath = false;
	PathMove panther = new PathMove(rc);

	public void perception() {
		try {
			int x = rc.readBroadcast(50);
			int y = rc.readBroadcast(51);
			rally = new MapLocation(x, y);
			checkTarget();
			x = rc.readBroadcast(67);
			y = rc.readBroadcast(68);
			goal = new MapLocation(x, y);
			nearest = goal;
			try{
				if (rc.readBroadcast(66) == 0) {
					if (!rally.equals(panther.goal)) panther.setDestination(rally);
				} else {
					if (!goal.equals(panther.goal)) panther.setDestination(goal);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.out.println("B_Turtle perception error");
			e.printStackTrace();
		}
		allies = rc.senseNearbyRobots(Tank.senseRange, Tank.team);
		enemies = rc.senseNearbyRobots(Tank.range, Tank.team.opponent());

	}
	
	/**
	 * Loads the path specified by pathCount from the radio
	 * @param pathCount
	 * @throws GameActionException
	 */
	public void loadPath(int pathCount) throws GameActionException {
		int channel = PathBeaver.getPathChannel(pathCount);
		int length = rc.readBroadcast(channel);
		MapLocation[] path = new MapLocation[length];
		channel++;
		for (int i = 0; i < length * 2; i++) {
			int x = rc.readBroadcast(channel + i*2);
			int y = rc.readBroadcast(channel + i*2+1);
			path[i]=new MapLocation(x,y);
		}
		panther.givePath(path);
	}

	public void calculation() {
		double max = rc.getLocation().distanceSquaredTo(nearest);
		if (enemies.length > 0) {
			for (RobotInfo ri : enemies) {
				MapLocation loc = ri.location;
				double dis = rc.getLocation().distanceSquaredTo(ri.location);
				if (dis < max && ri.type != RobotType.TOWER) {
					max = dis;
					nearest = loc;
				}
				try {
					int retarget_cooldown = rc.readBroadcast(69);
					if (ri.type == RobotType.TOWER && retarget_cooldown == 0) {
						rc.broadcast(67, loc.x);
						rc.broadcast(68, loc.y);
						rc.broadcast(69, 4);
						goal = loc;
					}
				} catch (GameActionException e) {
					System.out.println("Calc error");
					e.printStackTrace();
				}
			}
		}
	}

	public void action() {
		try {
			if (rc.readBroadcast(66) == 1) {
				attackMove();
			} else {
				rallyMove();
			}
		} catch (Exception e) {
			System.out.println("Tank Tutle action Error");
			e.printStackTrace();
		}
	}

	public void rallyMove() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		rc.setIndicatorString(1, "");
		if (rc.isCoreReady()) {
			if (enemies.length > 0) {
				if (rc.isWeaponReady()) {
					rc.attackLocation(nearest);
					return;
				} else {
					Move.tryMove(myLoc.directionTo(nearest));
					return;
				}
			} else {
				panther.attemptMove();
			}
		}
	}

	public void checkTarget() throws GameActionException{
		int x = rc.readBroadcast(67);
		int y = rc.readBroadcast(68);
		MapLocation r = new MapLocation(x,y);
		RobotInfo ri;
		if (rc.canSenseLocation(r) && (ri=rc.senseRobotAtLocation(r)) != null && (ri.type != RobotType.TOWER && ri.type != RobotType.HQ)) {
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

	public void attackMove() throws GameActionException {
		RobotInfo[] allies = rc.senseNearbyRobots(nearest, 40, rc.getTeam());
		MapLocation myLoc = rc.getLocation();
		if (enemies.length > 0){
			if (rc.isWeaponReady() && rc.canAttackLocation(nearest)) {
				rc.attackLocation(nearest);
			} else {
				Move.tryMove(myLoc.directionTo(nearest));
			}
		} else {
			if (allies.length > 7 || goal.distanceSquaredTo(myLoc) > 37) panther.attemptMove();
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

	public void panicAlert() {
		//read and see if someone is under attack
		try {
			int panicX = rc.readBroadcast(911);
			if (panicX != 0 && rc.readBroadcast(66) == 0) { //try to assist
				rc.setIndicatorDot(rc.getLocation(), 2, 2, 2);
				if(enemies.length > 0 && rc.isWeaponReady()) {
					rc.attackLocation(nearest);
				}
				Tank.panic = true;
				int panicY = rc.readBroadcast(912);
				MapLocation aid = new MapLocation(panicX, panicY);
				// if greater than 5 enemies be a coward
				if (rc.getLocation().distanceSquaredTo(aid) < 7 && (enemies.length < 1 || enemies.length > 5)) {
					rc.broadcast(911, 0); //no enemies so reset alarm
					rc.broadcast(912, 0);
					Tank.panic = false; //give up or no enemies
				} else {
					Move.tryMove(aid);
				}
			} else {
				Tank.panic = false; //no alarm
			}
		} catch (Exception e) {
			System.out.println("Error in tank turtle panic alert");
		}
	}
}
