/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.logistics;

import battlecode.common.*;

import java.util.Random;

import team163.utils.Move;
import team163.utils.Path;
import team163.utils.Point;

/**
 *
 * @author sweetness
 */
public class Beaver {

    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST,
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    static RobotController rc;
    static Team myTeam;
    static Team enemyTeam;
    static int myRange;
    static Random rand;
    static int lifetime = 0;
    static MapLocation bestLoc;
    static int bestVal;
    static boolean wasBest = false;
    static int wasBest_count = 0;
    static MapLocation myLoc;
    static int oreHere;
    static boolean amPathBeaver;

    public static void run(RobotController rc) {
        Beaver.rc = rc;
        rand = new Random(rc.getID());
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        try {
        	if (rc.readBroadcast(72)==1) {
        		rc.broadcast(72, 0);
        		amPathBeaver = true;
        		doPathBeaverThings();
        	}
        } catch (Exception e) {
        	System.out.println("Tried to be a path beaver, but I failed");
        }
        while (true) {
            try {
            	lifetime++;
            	myLoc = rc.getLocation();
            	if (rc.isWeaponReady()) {
            		attackSomething();
            	}
            	oreHere = (int) (rc.senseOre(myLoc) +.5);
            	bestVal = rc.readBroadcast(1000);
            	bestLoc = new MapLocation(rc.readBroadcast(1001), rc.readBroadcast(1002));
            	if (rc.isCoreReady()) {
            		if (oreHere > bestVal) {
            			rc.broadcast(1000, oreHere);
            			rc.broadcast(1001, myLoc.x);
            			rc.broadcast(1002, myLoc.y);
            		}
            		buildStuff();
        			defaultMove();
        			rc.yield();
                }
            } catch (Exception e) {
                System.out.println("Beaver Exception");
                e.printStackTrace();
            }
        }
    }
	static boolean isStationary(RobotType rt) {
		return (rt!=null && rt == RobotType.AEROSPACELAB || rt == RobotType.BARRACKS || rt == RobotType.HELIPAD || rt == RobotType.HQ ||  rt == RobotType.MINERFACTORY || rt == RobotType.SUPPLYDEPOT || rt == RobotType.TANKFACTORY || rt == RobotType.TECHNOLOGYINSTITUTE || rt == RobotType.TOWER || rt == RobotType.TRAININGFIELD);
	}

	static void defaultMove() throws GameActionException {
		Direction d = findSpot();
		if (myLoc.x == rc.readBroadcast(1001) && myLoc.y == rc.readBroadcast(1002)) {
			rc.broadcast(1000, (int) (rc.senseOre(myLoc)+.5));
		}
		if (rc.isCoreReady()) {
        	if (rand.nextDouble() > .7) {
        		if (rc.canMove(d)) {
        			rc.move(d);
        			return;
        		}
        	}
        	if ((oreHere / 20 < .2 || lifetime < 10)) Move.tryMove(bestLoc);
        	if (rc.isCoreReady() && rc.canMine()) rc.mine();
		}
	}
	
	public static Direction findSpot() throws GameActionException {
		double bestFound = rc.senseOre(myLoc);
		Direction bestDir = Direction.NONE;
		double tempOre;
		for (Direction d : directions) {
			MapLocation potential = myLoc.add(d);
			tempOre = rc.senseOre(potential);
			RobotInfo atLoc = rc.senseRobotAtLocation(potential);
			if (tempOre > bestFound && (atLoc == null) || (tempOre == bestFound && (rand.nextBoolean() || bestDir == Direction.NONE))) {
				bestFound = tempOre;
				bestDir = d;
			}
		}
		return bestDir;
	}
	
	static void buildStuff() throws GameActionException {
		int adj_count = 0;
		for (Direction d : directions) {
			MapLocation ml = myLoc.add(d);
			RobotInfo ri = rc.senseRobotAtLocation(ml);
			if (rc.senseTerrainTile(ml) == TerrainTile.NORMAL && (ri == null || !isStationary(ri.type))) adj_count++;
		}
		if (!rc.getLocation().isAdjacentTo(rc.senseHQLocation()) && adj_count > 5) {
			int[] counts = new int[] {
					rc.readBroadcast(3), //barracks
					rc.readBroadcast(6), //helipad
					rc.readBroadcast(15), //minerfactory
					rc.readBroadcast(18), //tank factory
					rc.readBroadcast(17), //Supply depot
					rc.readBroadcast(7)   //Aerospacelab
			};
			int[] maxCounts = new int[] {
					2,0,1,5,5,0
			};
			int[] priorityOffsets = new int[] {
					1,1,2,(counts[0] > 0)?1:-1000,3,(counts[1] > 0)?1:-1000
			};
			boolean oneGood = false;
			for (int i = 0; i < priorityOffsets.length; i++) {
				if (counts[i] >= maxCounts[i]) {
					priorityOffsets[i] = -1000;
				} else {
					oneGood = true;
				}
			}
			if (oneGood) {
				int[] oreCosts = new int[] {300,300,500,500,100,500};
				double oreCount = rc.getTeamOre();
				for (int i = 0; i < counts.length; i++) {
					counts[i] -= priorityOffsets[i];
				}
				int toMake = mindex(counts);
				if (counts[toMake]+priorityOffsets[toMake] < maxCounts[toMake] && oreCount >= oreCosts[toMake]) {
					switch(toMake) {
					case 0: {
						if (tryBuild(directions[rand.nextInt(8)],RobotType.BARRACKS)) {
							rc.broadcast(3, counts[toMake]+1);
							break;
						}
					}

					case 1: {
						if (tryBuild(directions[rand.nextInt(8)],RobotType.HELIPAD)) {
							rc.broadcast(6, counts[toMake]+1);
							break;
						}
					}

					case 2: {
						if (tryBuild(directions[rand.nextInt(8)],RobotType.MINERFACTORY)) {
							rc.broadcast(15, rc.readBroadcast(15)+1);
							break;
						}
					}

					case 3: {
						if (tryBuild(directions[rand.nextInt(8)],RobotType.TANKFACTORY)) {
							rc.broadcast(18, counts[toMake]+1);
							break;
						}
					}
                    case 4: {
                        if (tryBuild(directions[rand.nextInt(8)],RobotType.SUPPLYDEPOT)) {
                            rc.broadcast(17, counts[toMake]+1);
                            break;
                        }
                    }
					case 5: {
						if (tryBuild(directions[rand.nextInt(8)],RobotType.AEROSPACELAB)) {
							rc.broadcast(7, counts[toMake]+1);
							break;
						}
					}
					}
				}
			}
		}
	}
    
    static int mindex(int[] options) {
    	int dex = 0;
    	int min = options[0];
    	for (int i = 1; i < options.length; i++) {
    		if (options[i] < min) {
    			min = options[i];
    			dex = i;
    		}
    	}
    	return dex;
    }

    static int directionToInt(Direction d) {
        switch (d) {
            case NORTH:
                return 0;
            case NORTH_EAST:
                return 1;
            case EAST:
                return 2;
            case SOUTH_EAST:
                return 3;
            case SOUTH:
                return 4;
            case SOUTH_WEST:
                return 5;
            case WEST:
                return 6;
            case NORTH_WEST:
                return 7;
            default:
                return -1;
        }
    }

    // This method will attack an enemy in sight, if there is one
    static void attackSomething() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
        if (enemies.length > 0) {
            rc.attackLocation(enemies[0].location);
        }
    }

    // This method will attempt to build in the given direction (or as close to
    // it as possible)
    static boolean tryBuild(Direction d, RobotType type)
            throws GameActionException {
        int offsetIndex = 0;
        int[] offsets = {0, 1, -1, 2, -2, 3, -3, 4};
        int dirint = directionToInt(d);
        boolean blocked = false;
        while (offsetIndex < 8
                && !rc.canMove(directions[(dirint + offsets[offsetIndex] + 8) % 8])) {
            offsetIndex++;
        }
        if (offsetIndex < 8 && rc.isCoreReady()) {
            rc.build(directions[(dirint + offsets[offsetIndex] + 8) % 8], type);
            return true;
        }
        return false;
    }
    
    static void doPathBeaverThings() {
    	rc.setIndicatorString(0, "I am the path beaver");
    	MapLocation hq=rc.senseHQLocation();
    	MapLocation enemyhq = rc.senseEnemyHQLocation();
    	Path p = new Path(new Point(hq.x,hq.y),new Point(enemyhq.x,enemyhq.y));
    	MapLocation myLoc = rc.getLocation();
    	int path_count = 0;
    	
		//start positions will be rally point

		//channel 72 will be zero when pathbeaver is done.
    	try {
    		rc.broadcast(179, path_count);
    		boolean changed = false;
    		while (true) {
    			rc.broadcast(187, myLoc.x);
            	rc.broadcast(188,  myLoc.y);
    			if (changed) {
    				Point start = new Point(rc.readBroadcast(73),rc.readBroadcast(74));
    				Point finish = new Point(rc.readBroadcast(75),rc.readBroadcast(76));
    				//System.out.println("Pathing from: " + start + " to " + finish);
    				Point[] path = p.pathfind(start, finish); //this might take a bit
    				if (path != null) {
    					int channel = 578;
    					int len = path.length;
    					System.out.println("Path length: " + len);
    					rc.broadcast(77, len);
    					for (Point pr : path) {
    						rc.broadcast(channel, pr.x);
    						rc.broadcast(channel+1, pr.y);
    						channel+=2;
    						//System.out.println(pr.x + " " + pr.y);
    					}
    					rc.broadcast(72, 0);
    					changed = false;
    					rc.broadcast(179, ++path_count);
    				} else {
    					System.out.println("Null path");
    				}
    			} else {
    				changed = false;
    				RobotInfo[] allies = rc.senseNearbyRobots(9999999, rc.getTeam());
    				for (RobotInfo r : allies) {
    					if (!isStationary(r.type)) {
    						for (int i = -3; i <= 3; i++) {
    							for (int j = -3; j <= 3; j++) {
    								MapLocation ml = new MapLocation(r.location.x + i, r.location.y + j);
    								TerrainTile tt = rc.senseTerrainTile(ml);
    								RobotInfo sensed = null;
    								if (rc.canSenseLocation(ml)) {
    									sensed = rc.senseRobotAtLocation(ml);
    								}
    								if ((tt != TerrainTile.NORMAL && tt!=TerrainTile.UNKNOWN) || (sensed!=null&&isStationary(sensed.type)&&sensed.type!=RobotType.HQ)) {
    									if (p.addObstacle(new Point(ml.x,ml.y))) changed = true;;
    								}
    							}
    						}
    					}
    				}
    			}
    			
    			int minX = rc.readBroadcast(179); 
    	        int maxX = rc.readBroadcast(180);
    	        int minY = rc.readBroadcast(181);
    	        int maxY = rc.readBroadcast(182);
    	        p.setLimits(minX,maxX,minY,maxY);
    		}
    	} catch (Exception e) {
    		System.out.println("PathBeaver Exception");
    		e.printStackTrace();
    	}
    	
    }

}
