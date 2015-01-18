/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.RobotController;
import battlecode.common.RobotType;
import java.util.Random;
import team163.utils.Spawn;

/**
 * 
 * @author sweetness
 */
public class Aerospace {

	static RobotController rc;
	static Random rand;

	public static void run(RobotController rc) {
		try {
			Spawn.rc = rc;
			rand = new Random(rc.getID());
			while (true) {
				if (rc.isCoreReady() && rc.getTeamOre() >= 400
						&& rand.nextBoolean()) {
					Spawn.randSpawn(RobotType.LAUNCHER);
				}

				rc.yield();
			}
		} catch (Exception e) {
			System.out.println("Aerospace Exception");
			e.printStackTrace();
		}
	}
}
