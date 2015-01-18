package team163.tanks;

import battlecode.common.*;

public interface Behavior {
	
	/* general concepts of an agent */
	void perception();
	void calculation();
	void action();
	
	/* in case of a global panic response */
	void panicAlert();

}
