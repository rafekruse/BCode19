package bc19;

import java.util.ArrayList;


public class Helper {
	public static boolean inMap(boolean[][] map, Position pos) {
		if (pos.y < 0 || pos.y > (map.length - 1) || pos.x < 0 || pos.x > (map[0].length - 1)) {
			return false;
		}
		return true;
	}

	public static ArrayList<Position> AllOpenInRange(MyRobot robot, Position pos, int tileRange, float moveRange) {
		ArrayList<Position> validPositions = new ArrayList<>();
		for (int i = -tileRange; i <= tileRange; i++) {
			for (int j = -tileRange; j <= tileRange; j++) {
				Position relative = new Position(pos.y + i, pos.x + j);
				if(TileEmpty(robot, relative) && Helper.DistanceSquared(pos, relative) <= moveRange){
					validPositions.add(relative);
				}
			}
		}
		return validPositions;
	}

	public static float DistanceSquared(Position pos1, Position pos2) {
		return (pos2.y - pos1.y) * (pos2.y - pos1.y) + (pos2.x - pos1.x) * (pos2.x - pos1.x);
	}

	public static boolean FindSymmetry(boolean[][] map) {
		boolean mapIsHorizontal = true;
		for (int i = 0; i < map.length / 2; i++) {
			for (int j = 0; j < map[i].length; j++) {
				if (map[i][j] != map[(map.length - 1) - i][j]) {
					mapIsHorizontal = false;
					return mapIsHorizontal;
				}
			}
		}
		return mapIsHorizontal;
	}

	public static Position FindEnemyCastle(boolean[][] map, boolean mapIsHorizontal, Position ourCastle) {
		return mapIsHorizontal ? new Position((map.length - 1) - ourCastle.y, ourCastle.x)
				: new Position(ourCastle.y, (map[0].length - 1) - ourCastle.x);
	}

	public static ArrayList<Position> FindEnemyCastles(MyRobot robot, boolean mapIsHorizontal,
			ArrayList<Position> ourCastles) {
		ArrayList<Position> outputs = new ArrayList<>();
		for (int i = 0; i < ourCastles.size(); i++) {
			outputs.add(FindEnemyCastle(robot.map, mapIsHorizontal, ourCastles.get(i)));
		}
		return outputs;
	}

	public static Robot RobotAtPosition(MyRobot robot, Position pos) {
		Robot[] visibleRobots = robot.getVisibleRobots();
		for (int i = 0; i < visibleRobots.length; i++) {
			if (pos.y == visibleRobots[i].y && pos.x == visibleRobots[i].x) {
				return visibleRobots[i];
			}
		}
		return null;
	}

	public static boolean EnemiesAround(MyRobot robot){
		Robot[] robots = robot.getVisibleRobots();
		for (int i = 0; i < robots.length; i++) {
			if (robots[i].team != robot.ourTeam) {
				return true;
			}
		}
		return false;
	}
	public static int abs(int a) {
		return a < 0 ? -a : a;
	}
	public static int sign(int a){
		return a == 0 ? 0 : a / abs(a);
	}

	public static boolean ContainsPosition(ArrayList<Position> list, Position pos){
		for (int i = 0; i < list.size(); i++) {
			if(list.get(i).y == pos.y && list.get(i).x == pos.x){
				return true;
			}
		}
		return false;
	}

	public static Position ClosestPosition(MyRobot robot, ArrayList<Position> positions){
		float dist = Integer.MAX_VALUE;
		Position closest = null;
		for (int i = 0; i < positions.size(); i++) {
			if(Helper.DistanceSquared(positions.get(i), robot.location) < dist){
				dist = Helper.DistanceSquared(positions.get(i), robot.location);
				closest = positions.get(i);
			}
		}
		return closest;
	}

	public static Position closestEnemy(MyRobot robot, ArrayList<Robot> robots){
		float dist = Integer.MAX_VALUE;
		Position closest = null;
		for (int i = 0; i < robots.size(); i++) {
			Position rp = new Position(robots.get(i).y, robots.get(i).x);
			if(Helper.DistanceSquared(rp, robot.location) < dist){
				dist = Helper.DistanceSquared(rp, robot.location);
				closest = rp;
			}
		}
		return closest;
	}

	public static ArrayList<Robot> EnemiesWithin(MyRobot robot, float range){
		ArrayList<Robot> output = new ArrayList<>();
		Robot[] robots = robot.getVisibleRobots();
		for (int i = 0; i < robots.length; i++) {
			if (robots[i].team != robot.ourTeam && Helper.DistanceSquared(new Position(robots[i].y, robots[i].x), robot.location) <= range) {
				output.add(robots[i]);
			}
		}
		return output;
	}

	public static Position UpdateBattleStatus(MyRobot robot, ArrayList<Position> enemies, Position enemy){
		Position battleCry = ListenForBattleCry(robot);
		if(battleCry != null){
			robot.log("Hearing the battle Cry");
			if(Helper.ContainsPosition(enemies, battleCry)){
				robot.log("My Position is " + robot.location.toString() + " I should be a castle boi");
				robot.log("This is the battleCry " + battleCry.toString());
				return battleCry;
			}
			else{
				robot.log("My Position is " + robot.location.toString() + " I should be a church boi");
				robot.log("This is the battleCry " + battleCry.toString());

				enemies.add(battleCry);
				return battleCry;
			}
		}
		return enemy;
	}

	public static Position ListenForBattleCry(MyRobot robot){
		Robot[] robots = robot.getVisibleRobots();
		for (int i = 0; i < robots.length; i++) {
			Position enemyCastle = DecodeBattleCry(robots[i].signal);
			if(enemyCastle != null){
				if(robot.me.unit != robot.SPECS.PROPHET){
					return enemyCastle;
				}
				else if(ProphetBattleCry(robots[i].signal)){
					return enemyCastle;
				}				
			}
		}
		return null;
	}
	public static boolean ProphetBattleCry(int signal){
		if(signal < 61440 && signal >= 45056){// starts with 1011 
			return true;
		}
		return false;
	}
	public static Position DecodeBattleCry(int signal){
		if(signal < 45056){//1011 followed by the cords
			return null;
		}
		int x = signal & 63;
		signal >>= 6;
		int y = signal & 63;
		return new Position(y, x);		
	}

	public static int IsSurroundingsOccupied(MyRobot robot, int[][] map, Position pos, int ourTeam) {
		int highest = 0;
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				int numAllies = -1;
				Position relative = new Position(pos.y + i, pos.x + j);
				Robot[] robots = robot.getVisibleRobots();
				for (int k = -1; k <= 1; k++) {
					for (int l = -1; l <= 1; l++) {
						for (int m = 0; m < robots.length; m++) {
							if (relative.y + k == robots[m].y && relative.x + l == robots[m].x
									&& robots[m].team == ourTeam) {
								numAllies++;
							}
						}
					}
				}
				if (numAllies > highest) {
					highest = numAllies;
				}
			}
		}
		return highest;
	}
	public static Position RandomAdjacentNonResource(MyRobot robot, Position pos) {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				Position adjacent = new Position(pos.y + i, pos.x + j);
				if (TileEmptyNonResource(robot, adjacent)) {
					return new Position(adjacent.y, adjacent.x);
				}
			}
		}
		return null;
	}
	public static Position RandomAdjacentMoveable(MyRobot robot, Position pos, float moveRange) {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				Position adjacent = new Position(pos.y + i, pos.x + j);
				if (TileEmpty(robot, adjacent) && Helper.DistanceSquared(robot.location, adjacent) <= moveRange) {
					return new Position(adjacent.y, adjacent.x);
				}
			}
		}
		return null;
	}

	public static float[][] twoDimensionalArrayClone(float[][] original) {
		float[][] output = new float[original.length][original[0].length];
		for (int i = 0; i < original.length; i++) {
			output[i] = original[i].clone();
		}
		return output;
	}
	public static Position RandomAdjacent(MyRobot robot, Position pos) {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				Position adjacent = new Position(pos.y + i, pos.x + j);
				if (TileEmpty(robot, adjacent)) {
					return new Position(adjacent.y, adjacent.x);
				}
			}
		}
		return null;
	}

	public static boolean TileEmptyNonResource(MyRobot robot, Position pos){
		if(TileEmpty(robot, pos) && !robot.getKarboniteMap()[pos.y][pos.x] && !robot.getFuelMap()[pos.y][pos.x]){
			return true;
		}
		return false;
	}
	public static boolean TileEmpty(MyRobot robot, Position pos) {
		if (Helper.inMap(robot.map, pos) && robot.map[pos.y][pos.x] && robot.getVisibleRobotMap()[pos.y][pos.x] == 0) {
			return true;
		}
		return false;
	}


	public static boolean CanAfford(MyRobot robot, int unit) {
		if (robot.karbonite > robot.SPECS.UNITS[unit].CONSTRUCTION_KARBONITE && robot.fuel > robot.SPECS.UNITS[unit].CONSTRUCTION_FUEL) {
			return true;
		}
		return false;

	}

	public static boolean PositiveOrNegativeMap(MyRobot robot)
	{
		if (robot.mapIsHorizontal)
		{
			return (robot.me.y < ((robot.map.length + 1) / 2)) ? true : false;
		}
		else
		{
			return (robot.me.x > ((robot.map[0].length + 1) / 2)) ? true : false;
		}
	}

	public static boolean[][] ResourcesOnOurHalfMap(MyRobot robot)
	{
		robot.log("Map is Horinzontal : " + robot.mapIsHorizontal + "  " + robot.positiveSide);
		boolean[][] halfResourceMap = robot.mapIsHorizontal ? new boolean[(robot.map.length + 1) / 2][robot.map.length] : new boolean[robot.map.length][(robot.map.length + 1) / 2];
		robot.log(halfResourceMap.length + " " + halfResourceMap[0].length);

		if (robot.positiveSide && robot.mapIsHorizontal)
		{
			for (int i = 0; i < (robot.map.length + 1) / 2; i++)
			{
				for (int j = 0; j < robot.map[0].length; j++)
				{
					halfResourceMap[i][j] = robot.getFuelMap()[i][j] || robot.getKarboniteMap()[i][j] ? true : false;
				}
			}
		}
		else if (!robot.positiveSide && robot.mapIsHorizontal)
		{
			for (int i = (robot.map.length) / 2; i < robot.map.length; i++)
			{
				for (int j = 0; j < robot.map[0].length; j++)
				{
					halfResourceMap[i- (robot.map[0].length) / 2][j] = robot.getFuelMap()[i][j] || robot.getKarboniteMap()[i][j] ? true : false;
				}
			}
		}
		else if (robot.positiveSide && !robot.mapIsHorizontal)
		{
			for (int i = 0; i < robot.map.length; i++)
			{
				for (int j = (robot.map[0].length) / 2; j < robot.map[i].length; j++)
				{
					halfResourceMap[i][j - (robot.map[0].length) / 2] = robot.getFuelMap()[i][j] || robot.getKarboniteMap()[i][j] ? true : false;
				}
			}
		}
		else if (!robot.positiveSide && !robot.mapIsHorizontal)
		{
			for (int i = 0; i < robot.map.length; i++)
			{
				for (int j = 0; j < (robot.map[0].length + 1) / 2; j++)
				{
					halfResourceMap[i][j] = robot.getFuelMap()[i][j] || robot.getKarboniteMap()[i][j] ? true : false;
				}
			}
		}
		
		return halfResourceMap;
	}
	public static ArrayList<ResourceCluster> FindClusters(MyRobot robot, boolean[][] resourceMap){
		ArrayList<ResourceCluster> output = new ArrayList<>();
		int yAdd = 0;
		int xAdd = 0;
		if(robot.mapIsHorizontal && !robot.positiveSide){
			yAdd = (robot.map.length - 1) / 2;
		} else if(!robot.mapIsHorizontal && robot.positiveSide){
			xAdd = (robot.map.length - 1) / 2;
		}
		for (int y = 0; y < resourceMap.length; y++) {
			for (int x = 0; x < resourceMap[y].length; x++) {
				if(resourceMap[y][x]){
					ResourceCluster cluster = new ResourceCluster();
					cluster.resourceLocations.add(new Position(y + yAdd, x + xAdd));
					resourceMap[y][x] = false;
					for(int i = -3; i <= 3; i++){
						for(int j = -3; j <= 3; j++){
							if(inMap(resourceMap, new Position(y + i, x + j)) && resourceMap[y + i][x + j]){
								cluster.resourceLocations.add(new Position(y+yAdd + i, x + xAdd + j));
								resourceMap[y+i][x+ j] = false;
							}
						}
					}
					output.add(cluster);
				}
			}
		}
		robot.log("Size : " + output.size());
		return output;
	}
	public static ArrayList<Position> ChurchLocationsFromClusters(MyRobot robot, ArrayList<ResourceCluster> clusters){
		ArrayList<Position> churchLocations = new ArrayList<>();
		for(int i =0 ; i< clusters.size(); i++){
			int yAvg = 0;
			int xAvg = 0;
			for (int j = 0; j < clusters.get(i).resourceLocations.size(); j++) {
				Position resource = clusters.get(i).resourceLocations.get(j);
				yAvg += resource.y;
				xAvg += resource.x;
			}
			yAvg /= clusters.get(i).resourceLocations.size();
			xAvg /= clusters.get(i).resourceLocations.size();
			Position center = new Position(Math.round(yAvg), Math.round(xAvg));
			float lowestDist = Integer.MAX_VALUE;
			Position nonResourceCenter = center;
			for (int y = -2; y <= 2; y++) {
				for (int x = -2; x <= 2; x++) {
					Position pos = new Position(center.y + y, center.x + x);
					if(!ContainsPosition(clusters.get(i).resourceLocations,pos) && DistanceSquared(center, pos) < lowestDist){
						lowestDist = DistanceSquared(center, pos);
						nonResourceCenter = pos;
					}
				}
			}
			churchLocations.add(nonResourceCenter);
			robot.log("Prior Center : " + center.toString() + " New center : " + nonResourceCenter.toString());
		}
		return churchLocations;
	}

}
class ResourceCluster{
	ArrayList<Position> resourceLocations;

	ResourceCluster(){
		resourceLocations = new ArrayList<>();
	}
}