package team163.land;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

import java.util.Random;

import team163.utils.AttackUtils;
import team163.utils.BasicBugger;
import team163.utils.Move;
import team163.utils.PathMove;

/**
 * Alex
 */
public class Soldier {

	static PathMove panther;
	static MapLocation rally;
	static MapLocation goal;
	static boolean attacking;
	static BasicBugger bb;

	public static void run(RobotController rc) {
		//panther = new PathMove(rc);
		bb = new BasicBugger(rc);
		panther = new PathMove(rc);
		while (true) {
			try {
				bb.attemptMove();
				
				
				rc.yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	

}

