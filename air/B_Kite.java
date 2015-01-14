/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.air;

import battlecode.common.*;

/**
 * Kite around stuff. Made specifically for drones
 * 
 * @author sweetness
 */
public class B_Kite implements Behavior {

	RobotInfo[] enemies;
	MapLocation[] towers;
	MapLocation nearest;
	MapLocation myLoc;

	public void perception() {
		try {
			enemies = Drone.rc.senseNearbyRobots(24, Drone.opponent);
			myLoc = Drone.rc.getLocation();
			towers = new MapLocation[6]; // for 6 tower locations

			// read tower locations
			for (int i = 0; i < 6; i++) {
				int chan = (2 * i) + 800;
				int x = Drone.rc.readBroadcast(chan);
				int y = Drone.rc.readBroadcast(chan + 1);
				towers[i] = new MapLocation(x, y);
			}
		} catch (Exception e) {
			System.out.println("Error in perception with Kite by Drone");
			e.printStackTrace();
		}
	}

	public void calculation() {
		try {
			double max = Double.MAX_VALUE;
			for (RobotInfo x : enemies) {
				if (x.type == RobotType.TOWER) {
					MapLocation at = x.location;
					int index = 0; // place in towers array
					boolean newTower = true;
					for (int i = 0; i < 6; i++) {
						if (towers[i].x == at.x && towers[i].y == at.y) {
							newTower = false;
						}
						if (towers[i].x == 0 && towers[i].y == 0) {
							index = i;
						}
					}
					if (newTower) {
						int chan = (2 * index) + 800;
						Drone.rc.broadcast(chan, at.x);
						Drone.rc.broadcast(chan + 1, at.y);
						towers[index] = new MapLocation(at.x, at.y);
					}
				} else {
					double dis = x.location.distanceSquaredTo(myLoc);
					if (dis < max) {
						max = dis;
						nearest = x.location;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error in calculation with da Kite");
			e.printStackTrace();
		}
	}

	public void action() {
		try {
			// try moving toward enemy hq
			if (nearest != null && Drone.rc.isWeaponReady()
					&& Drone.rc.canAttackLocation(nearest)) {
				Drone.rc.attackLocation(nearest);
			}
			Direction dir = myLoc.directionTo(Drone.enemyHQ);
			MapLocation next = myLoc.add(dir);
			boolean check = true;
			int count = 8;
			while (check && count-- > 0) {
				for (MapLocation x : towers) {
					if (x != null && x.x != 0 && x.y != 0) {
						while(x.distanceSquaredTo(next) < 25) {
							dir = (Drone.right) ? dir.rotateRight() : dir
									.rotateLeft(); // turn right or left
							next = myLoc.add(dir);
						}
					}
				}

				if (Drone.rc.canSenseLocation(Drone.enemyHQ)) {
					while (next.distanceSquaredTo(Drone.enemyHQ) < 24
							&& count-- > 0) {
						dir = (Drone.right) ? dir.rotateRight() : dir
								.rotateLeft();
						next = myLoc.add(dir);
					}
				}
				// reached the end to the next location is good
				check = false;
			}

			if (Drone.rc.isCoreReady()) {
				for (int i = 0; i < 8; i++) {
					if (Drone.rc.canMove(dir)) {
						Drone.rc.move(dir);
						break;
					} else {
						dir = (Drone.right) ? dir.rotateRight() : dir
								.rotateLeft();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error in Kite action for Drone");
			e.printStackTrace();
		}

	}

	public void panicAlert() {
	}

}
