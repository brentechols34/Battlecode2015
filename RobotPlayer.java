package team163;

import team163.air.Aerospace;
import team163.air.Drone;
import team163.air.Helipad;
import team163.air.Launcher;
import team163.air.Missile;
import battlecode.common.*;
import team163.logistics.SupplyDepot;
import team163.tanks.Tank;
import team163.tanks.TankFactory;
import team163.utils.Move;

import java.util.*;

import team163.land.Barracks;
import team163.land.Basher;
import team163.land.Soldier;
import team163.logistics.Beaver;
import team163.logistics.Miner;
import team163.logistics.MinerFactory;

public class RobotPlayer {

	public static void run(RobotController tomatojuice) {
		team163.utils.Move.setRc(tomatojuice); // set rc in utils/move
		while (true) {
			switch (tomatojuice.getType()) {

			case MISSILE:
				Missile.run(tomatojuice);
				break;

			case HQ:
				HQ.run(tomatojuice);
				break;

			case TOWER:
				Tower.run(tomatojuice);
				break;

			case HELIPAD:
				Helipad.run(tomatojuice);
				break;

			case AEROSPACELAB:
				Aerospace.run(tomatojuice);
				break;

			case DRONE:
				Drone.run(tomatojuice);
				break;

			case LAUNCHER:
				Launcher.run(tomatojuice);
				break;

			case TANK:
				Tank.run(tomatojuice);
				break;

			case TANKFACTORY:
				TankFactory.run(tomatojuice);
				break;

			case BASHER:
				Basher.run(tomatojuice);
				break;

			case SOLDIER:
				Tank.run(tomatojuice);
				break;

			case BEAVER:
				Beaver.run(tomatojuice);
				break;

			case BARRACKS:
				Barracks.run(tomatojuice);
				break;

			case MINERFACTORY:
				MinerFactory.run(tomatojuice);
				break;

			case MINER:
				Miner.run(tomatojuice);
				break;

			case SUPPLYDEPOT:
				SupplyDepot.run(tomatojuice);
				break;

			default:
				System.out.println("Unhandeled robot type");
				tomatojuice.yield();
			}
		}
	}
}
