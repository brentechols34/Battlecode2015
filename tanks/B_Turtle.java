package team163.tanks;

import java.util.Random;

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
	//    static int currentCount = 0;
	boolean pathSet = false;
	int pathCount = 0;
	boolean madeItToRally = false;
	public boolean attacking;
	boolean offPath = false;
	PathMove panther = null;

	public void perception() {
		try {
			if (!pathSet) {
				pathSet = true;
				pathCount = rc.readBroadcast(179);
			}
			int x = rc.readBroadcast(50);
			int y = rc.readBroadcast(51);
			rally = new MapLocation(x, y);
			x = rc.readBroadcast(67);
			y = rc.readBroadcast(68);
			goal = new MapLocation(x, y);
			RobotInfo ri;
			if (rc.canSenseLocation(goal) && ((ri = rc.senseRobotAtLocation(goal)) == null || ri.type != RobotType.TOWER)) {
				MapLocation[] towers = rc.senseEnemyTowerLocations();
				if (towers.length == 0) {
					MapLocation enHQ = rc.senseEnemyHQLocation();
					rc.broadcast(67, enHQ.x);
					rc.broadcast(68, enHQ.y);
					goal = enHQ;
				} else {
					int closest = findClosestToHQ(towers);
					rc.broadcast(67, towers[closest].x);
					rc.broadcast(68, towers[closest].y);
					goal = towers[closest];
				}
			}
			nearest = goal;
		} catch (Exception e) {
			System.out.println("B_Turtle perception error");
		}
		allies = rc.senseNearbyRobots(Tank.senseRange, Tank.team);
		enemies = rc.senseNearbyRobots(Tank.range, Tank.team.opponent());

	}

	public void calculation() {
		double max = rc.getLocation().distanceSquaredTo(nearest);
		if (enemies.length > 0) {
			for (RobotInfo ri : enemies) {
				double dis = rc.getLocation().distanceSquaredTo(ri.location);
				if (dis < max) {
					max = dis;
					nearest = ri.location;
				}
			}
		}

	}

	public void action() {
		try {
			if (enemies.length > 0 && rc.isWeaponReady()) {
				rc.attackLocation(nearest);
			} else {
				if (attacking && madeItToRally) {
					attackMove();
				} else {
					rallyMove();
				}
			}
		} catch (Exception e) {
			System.out.println("Tank Tutle action Error");
		}
		rc.yield();
	}

	//    public boolean isObsInBetween(MapLocation myLoc, MapLocation dest) {
	//        Point p1 = new Point(myLoc.x, myLoc.y);
	//        Point p2 = new Point(dest.x, dest.y);
	//        if (p1.distance(p2) < 2) {
	//            return false;
	//        }
	//        int x1 = p1.x;
	//        int y1 = p1.y;
	//        int x2 = p2.x;
	//        int y2 = p2.y;
	//        int dx = Math.abs(x2 - x1);
	//        int dy = Math.abs(y2 - y1);
	//        int sx = (x1 < x2) ? 1 : -1;
	//        int sy = (y1 < y2) ? 1 : -1;
	//        int err = dx - dy;
	//        while (true) {
	//            int e2 = err << 1;
	//            if (e2 > -dy) {
	//                err = err - dy;
	//                x1 = x1 + sx;
	//            }
	//            if (x1 == x2 && y1 == y2) {
	//                break;
	//            }
	//            if (rc.senseTerrainTile(new MapLocation(x1, y1)) == TerrainTile.VOID) {
	//                return true;
	//            }
	//
	//            if (e2 < dx) {
	//                err = err + dx;
	//                y1 = y1 + sy;
	//            }
	//            if (x1 == x2 && y1 == y2) {
	//                break;
	//            }
	//            if (rc.senseTerrainTile(new MapLocation(x1, y1)) == TerrainTile.VOID) {
	//                return true;
	//            }
	//
	//        }
	//        return false;
	//    }

	public void rallyMove() throws GameActionException {
		//Constants, should abstract to some constant class TODO
		int rallyBaseChannel = 578;
		int rallyVersionChannel = 179;
		int rallyLength = rc.readBroadcast(77);

		MapLocation myLoc = rc.getLocation();
		if (rc.isCoreReady()) {
			if (enemies.length > 0) {
				if (rc.isWeaponReady()) {
					rc.attackLocation(nearest);
					return;
				} else if (rc.canMove(myLoc.directionTo(nearest))) {
					rc.move(myLoc.directionTo(nearest));
					return;
				}
				return;
			}	


			int currentVersion = rc.readBroadcast(rallyVersionChannel);
			if (currentVersion > pathCount || panther == null) {
				pathCount = currentVersion;
				panther = new PathMove(rc, rallyBaseChannel, rallyLength, (panther==null)?0:panther.getCount());
			}
			panther.attemptMove();
			if (panther.finished) madeItToRally = true;
		}
	}

	public void attackMove() throws GameActionException {
		/* try to attack the nearest if unable than move towards it */
		if (rc.isWeaponReady() && enemies.length > 0) {
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
				Move.tryMove(goal);
			}
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
			if (panicX != 0) { //try to assist
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
