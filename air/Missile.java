/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.*;

/**
 * 
 * @author sweetness
 */
public class Missile {
	static RobotController rc;
	static Behavior mood; /* current behavior */

	static int range;
	static Team team;
	static MapLocation hq;
	static int channel;

	public static void run(RobotController rc) {
		try {
			Missile.rc = rc;
			Missile.range = rc.getType().attackRadiusSquared;
			Missile.team = rc.getTeam();
			Missile.hq = rc.senseHQLocation();
			Missile.channel = (rc.getID() % 100) + 600;

			mood = new B_Missile(); /* starting behavior of turtling */

			//missiles only live 5 rounds
			for(int i = 0; i < 4; i++) {
				/* perform round */
				mood.perception();
				mood.calculation();
				mood.action();
				/* end round */
				Missile.rc.yield();
			}
			
			/* final round if no enemy than disentigrate rather than explode */

			mood.perception();
			mood.calculation();
			mood.action();
			rc.disintegrate();
			
		} catch (Exception e) {
			System.out.println("Missile Exception");
			e.printStackTrace();
		}
	}

}
