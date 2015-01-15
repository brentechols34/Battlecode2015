package team163.tanks;

import team163.utils.PathMove2;
import team163.utils.SimplePather;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class TankBrain {
	
	private RobotController rc;
	private MapLocation rally;
	private PathMove2 sp;
	
	public TankBrain(RobotController rc) {
		this.rc = rc;
		this.sp = new PathMove2(rc);
		try {
			int x = rc.readBroadcast(50);
			int y = rc.readBroadcast(51);
			rally = new MapLocation(x, y);
			sp.setDestination(rally);
		} catch (GameActionException e) {
			System.out.println("B_Test perception issue");
		}
	}
	
	public void act() throws GameActionException {
		sp.attemptMove();
	}


}
