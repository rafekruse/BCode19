package bc19;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MovingRobot{

	public static int[][] CreateFloodPath(boolean[][] map, Position pos){
        //overlay multiple flood path of different step size keep lowest value per square
		int[][] outputPath = new int[map.length][map[0].length];
		Queue<Position> toBeVisited = new LinkedList<>();
		toBeVisited.add(pos);
		ArrayList<Position> visited = new ArrayList<>();
		int currentMapValue = 1;
		int currentCount = 0;
		int needForRing = 0;
		visited.add(pos);

		while(toBeVisited.size() > 0){
			Position removed = toBeVisited.poll();
			if(map[removed.y][removed.x]){
				outputPath[removed.y][removed.x] = currentMapValue;
			}
			else{
				outputPath[removed.y][removed.x] = -1;
			}

			currentCount += 1;
			if(needForRing <= currentCount){
				currentCount = 0;
				needForRing += 4;
				currentMapValue++;
			}
			Position top = new Position(removed.y - 1, removed.x);
			if(Helper.inMap(map, top) && outputPath[top.y][top.x] == 0){
				toBeVisited.add(top);
				outputPath[top.y][top.x] = -2;
            }
			Position right = new Position(removed.y, removed.x + 1);
			if(Helper.inMap(map, right) && outputPath[right.y][right.x] == 0){
				toBeVisited.add(right);
				outputPath[right.y][right.x] = -2;
			}
			Position bottom = new Position(removed.y + 1, removed.x);
			if(Helper.inMap(map, bottom) && outputPath[bottom.y][bottom.x] == 0){
				toBeVisited.add(bottom);
				outputPath[bottom.y][bottom.x] = -2;
			}
			Position left = new Position(removed.y, removed.x - 1);
			if(Helper.inMap(map, left) && outputPath[left.y][left.x] == 0){
				toBeVisited.add(left);
				outputPath[left.y][left.x] = -2;
			}
		}
		return outputPath;
	}
	int PathingDistance(MyRobot robot, int[][] path)
	{
		return path[robot.me.y][robot.me.x];
	}
	
    Position FloodPathing(MyRobot robot, int[][] path)
    
	{//needs to include use closest to goal of lowest number
		if (path == null)
		{
			return null;	
		}

		Position[] validPositions = Helper.AllPassableInRange(robot.map, new Position(robot.me.y, robot.me.x), robot.SPECS.UNITS[robot.me.unit].ATTACK_RADIUS);
		int lowest = Integer.MAX_VALUE;
		Position lowestPos = null;

		for (int i = 0; i < validPositions.length ; i++)
		{
			if (path[validPositions[i].y][validPositions[i].x] < lowest && path[validPositions[i].y][validPositions[i].x] > 0)
			{
				lowest = path[validPositions[i].y][validPositions[i].x];
				lowestPos = validPositions[i];
			}
		}
		return lowestPos;
	}

	boolean ReadInitialSignals(MyRobot robot, ArrayList<Position> castleLocations){
		Robot spawnCastle = new Robot();	
		for(Robot r : robot.getVisibleRobots()){
			if(r.unit == robot.SPECS.CASTLE && Helper.DistanceSquared(new Position(robot.me.y, robot.me.x), new Position(r.y, r.x)) < 2){
				spawnCastle = r;
			}
		}
		Position spawnCastlePos = new Position(spawnCastle.y, spawnCastle.x);	
		if(castleLocations.size() == 0){
			castleLocations.add(spawnCastlePos);
		}
		int signal = spawnCastle.signal;
		if(signal == -1){
			return true;
		}
		int x = signal & 63;
		signal -= x;
		signal = signal 
	}


}