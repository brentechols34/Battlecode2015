/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import team163.utils.Move;
import battlecode.common.*;

/**
 *
 * @author sweetness
 */
public class Missile {
	
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    public static void run(RobotController rc) {
        try {
            Team team = rc.getTeam();
            MapLocation loc = findClosestToMe(rc);
            if (loc == null) {
            	RobotInfo[] en = rc.senseNearbyRobots(24, team.opponent());
            	if (en.length > 0) loc = en[0].location;
            	else loc = rc.senseEnemyHQLocation();
            }
            MapLocation myLoc;
            

            //missiles only live 5 rounds
            while (true) {
                /* perform round */
            	Direction dir = rc.getLocation().directionTo(loc);
            	if (rc.canMove(dir)) {
            		rc.move(dir);
            	}
                /* end round */
                rc.yield();
            }
        } catch (Exception e) {
            System.out.println("Missile Exception");
            e.printStackTrace();
        }
    }
    
	public static MapLocation findClosestToMe(RobotController rc) {
		MapLocation[] en = rc.senseEnemyTowerLocations();
		MapLocation me = rc.getLocation();
		MapLocation enHQ = rc.senseEnemyHQLocation();
		MapLocation min = enHQ;
		int dis = me.distanceSquaredTo(enHQ);
		for (MapLocation m : en) {
			if (dis < 30) return min;
			int t_dis = me.distanceSquaredTo(m);
			if (t_dis < dis) {
				dis = t_dis;
				min = m;
			}
		} 
		return null;
	}

}
