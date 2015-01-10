/*
 * Behavior for commander since he has special abilities
 */
package team163.land;

import battlecode.common.*;

public interface C_Behavior {
	
	/* general concepts of an agent */
	void perception();
	void calculation();
	void action();
	
	/* in case of a global panic response */
	void panicAlert(MapLocation m);
}
