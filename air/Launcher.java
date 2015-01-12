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
	static int channel;

	public static void run(RobotController rc) {
		try {
			Launcher.rc = rc;
			Launcher.range = rc.getType().attackRadiusSquared;
			Launcher.team = rc.getTeam();
			Launcher.hq = rc.senseHQLocation();
			Launcher.channel = (rc.getID()%100) + 600;
			
			mood = new B_Launcher(); /* starting behavior of turtling */

			while (true) {

				/* get behavior */
				mood = chooseB();
				mood.setRc(Launcher.rc);

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
