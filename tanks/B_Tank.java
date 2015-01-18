package team163.tanks;

import team163.utils.*;
import battlecode.common.*;

public class B_Tank {

	private PathMove p;
	private BasicBugger bb;
	private RobotController rc;

	private MapLocation goal;
	private MapLocation rally;

	private final Team myTeam;
	private final MapLocation myHQ;
	private final Team enemyTeam;
	private final MapLocation enemyHQ;

	private RobotInfo[] localEnemies;
	private MapLocation[] enemyTowers;
	private MapLocation me;

	public B_Tank(RobotController rc) {
		this.rc = rc;
		this.myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		myHQ = rc.senseHQLocation();
		enemyHQ = rc.senseEnemyHQLocation();
		p = new PathMove(rc);
		bb = new BasicBugger(rc);
	}

	public void perception() { //state changing and paths and whatnot
		localEnemies = rc.senseNearbyRobots(RobotType.TANK.sensorRadiusSquared, enemyTeam);
		enemyTowers = rc.senseEnemyTowerLocations(); //don't want to move near these.
		me = rc.getLocation();
		try { //check rally points
			checkTarget();
			int goalX = rc.readBroadcast(67);
			int goalY = rc.readBroadcast(68);
			if (goal == null || goal.x != goalX || goal.y != goalY) {
				goal = new MapLocation(goalX,goalY);
				p.setDestination(goal);
			}
		} catch (GameActionException e) {
			e.printStackTrace();
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
		} else {
			int closest = findClosestToHQ(towers);
			rc.broadcast(67, towers[closest].x);
			rc.broadcast(68, towers[closest].y);
		}

	}

	public void act() {
		if (!rc.isCoreReady()) return;
		if (localEnemies.length > 0) {
			int most=0;
			RobotInfo best= null;
			RobotInfo somebody = null;
			for (RobotInfo r : localEnemies) {
				somebody = r;
				int allyCount = rc.senseNearbyRobots(r.location, RobotType.TANK.sensorRadiusSquared, myTeam).length;
				if (allyCount > 6 && (best == null || most < allyCount)) {
					best = r;
					most = allyCount;
				}
			}
			if (best != null) { //there is a nearby enemy who should get rekt
				kill(best.location);
				return;
			} else { //run away
				Move.tryMove(me.directionTo(somebody.location).opposite());
			}
		} else {
			try {
				p.attemptMove();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}

	public void moveTo(MapLocation loc) {
		Direction d = me.directionTo(loc);

		if (!isInBadRange(me.add(d)) || loc.equals(goal)) { //if it is a safe spot, or I'm aggro-ing a tower/HQ
			if (rc.canMove(d)) { //if I can move
				try {

					rc.move(d);
				} catch (GameActionException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isInBadRange(MapLocation m) {
		for (MapLocation t : enemyTowers) {
			if (m.distanceSquaredTo(t) < RobotType.TOWER.sensorRadiusSquared) return true;
		} 
		return (m.distanceSquaredTo(enemyHQ) < RobotType.HQ.sensorRadiusSquared);
	}

	public void kill(MapLocation loc) {
		try {

			if (rc.senseNearbyRobots(loc, RobotType.TOWER.sensorRadiusSquared +7, myTeam).length > 6) {
				if (!rc.canAttackLocation(loc)) { //if I can't attack
					if (!rc.isWeaponReady()) { //if weapon isn't ready
						rc.setIndicatorString(0,"MOVE FORWARDS");
						moveTo(loc);
					} else return;
				} else rc.attackLocation(loc);
				return;
			} else {
				//retreat!
				rc.setIndicatorString(0,"RETREAT");
				Direction d = me.directionTo(loc).opposite();
				Move.tryMove(d);
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}

	public int findClosestToHQ(MapLocation[] locs) {
		int mindex = 0;
		int minDis = myHQ.distanceSquaredTo(locs[0]);
		for (int i = 1; i < locs.length; i++) {
			int dis = myHQ.distanceSquaredTo(locs[i]);
			if (locs[i] != null && dis < minDis) {
				mindex = i;
				minDis = dis;
			}
		}
		return mindex;
	}




}
