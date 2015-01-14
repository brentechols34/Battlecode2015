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
	static Team team;
	static MapLocation enemyHQ;

	public static void run(RobotController rc) {
		try {
			Missile.rc = rc;
			Missile.team = rc.getTeam();
			Missile.enemyHQ = rc.senseEnemyHQLocation();

			mood = new B_Missile(); /* starting behavior of turtling */

			//missiles only live 5 rounds
			while(true) {
				/* perform round */
				mood.perception();
				mood.calculation();
				mood.action();
				/* end round */
				Missile.rc.yield();
			}		
		} catch (Exception e) {
			System.out.println("Missile Exception");
			e.printStackTrace();
		}
	}

}
