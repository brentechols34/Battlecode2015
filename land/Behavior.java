/*
 * Behavior interface for soldiers and bashers
 */
package team163.land;

import battlecode.common.*;

public interface Behavior {
	
	/* general concepts of an agent */
	void perception();
	void calculation();
	void action();
	
	/* in case of a global panic response */
	void panicAlert(MapLocation m);
    
}
