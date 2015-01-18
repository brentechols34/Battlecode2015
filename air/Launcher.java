/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

/**
 * 
 * @author sweetness
 */
public class Launcher {

	static RobotController rc;
	static Behavior mood; /* current behavior */

	static int range;
	static Team team;
	static MapLocation hq;
	static MapLocation enemyHQ;
	static int channel;
	static boolean panic = false;

	public static void run(RobotController rc) {
		try {
			Launcher.rc = rc;
			Launcher.range = rc.getType().attackRadiusSquared;
			Launcher.team = rc.getTeam();
			Launcher.hq = rc.senseHQLocation();
			Launcher.enemyHQ = rc.senseEnemyHQLocation();
			
			mood = new B_Launcher(); /* starting behavior of turtling */

			while (true) {

				/* get behavior */
				mood = chooseB();

				/* perform round */
				mood.perception();
				mood.calculation();
				mood.action();
				
				/* end round */
				Launcher.rc.yield();
			}
		} catch (Exception e) {
			System.out.println("Launcher Exception");
			e.printStackTrace();
		}
	}

	private static Behavior chooseB() {
		return mood;
	}
}
