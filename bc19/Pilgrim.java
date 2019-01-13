package bc19;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class Pilgrim extends MovingRobot implements Machine{
  
    MyRobot robot;
    int ourTeam; //red:0 blue:1
    int turn = 0;
    Position location;
    boolean mapIsHorizontal;
    HashMap<Position, int[][]> karbRoutes;
    HashMap<Position, int[][]> fuelRoutes;
    HashMap<Position, int[][]> ourDropOffRoutes;
    Position dropOff;
    int[][] occupiedResources;
    //method to get fuel cost from current tile value
	
	public Pilgrim(MyRobot robot){
		this.robot = robot;
	}

	public Action Execute(){
		UpdateOccupiedResources();
		return robot.move(0, 1);

    }

    void InitializeVariables(){
        ourTeam = robot.me.team == robot.SPECS.RED ? 0 : 1;
        mapIsHorizontal = Helper.FindSymmetry(robot.map);
        karbRoutes = new HashMap<>();
        fuelRoutes = new HashMap<>();
        ourDropOffRoutes = new HashMap<>();
        location = new Position(robot.me.y, robot.me.x);
        occupiedResources = new int[robot.map.length][robot.map[0].length];
        for (int i = 0; i < robot.map.length; i++)
        {
            for (int j = 0; j < robot.map[0].length; j++)
            {
                if (robot.getKarboniteMap()[i][j] == true || robot.getFuelMap()[i][j] == true)
                {
                    occupiedResources[i][j] = 0;
                }
                else
                {
                    occupiedResources[i][j] = -1;
                }
            }
        }
    }

    public Action ReturnToDropOff(){
        if ((dropOff.x - location.x) * (dropOff.x - location.x) > 1 || (dropOff.y - location.y) * (dropOff.y - location.y) > 1)
        {
            //move to dropOff
        }
        
        return robot.give(dropOff.x - location.x, dropOff.y - location.y, robot.me.karbonite, robot.me.fuel); 
    }

    public float FuelToReturn(int[][] path)
    {
        int tilesFromTarget = path[location.y][location.x];
        float amountOfMoves = (float)(tilesFromTarget / Math.sqrt(robot.SPECS.UNITS[robot.SPECS.PILGRIM].SPEED));
        return (float)(amountOfMoves * robot.SPECS.UNITS[robot.SPECS.PILGRIM].FUEL_PER_MOVE);
    }

    public Position getNearestResource(boolean karbResource)
    {
        HashMap<Position, int[][]> chosenRoute = karbResource ? karbRoutes : fuelRoutes;
        int lowest = Integer.MAX_VALUE;
        Position closest = null;
        for (Map.Entry<Position, int[][]> pair : chosenRoute.entrySet())
        {
            int distance = pair.getValue()[location.y][location.x];
            if (occupiedResources[pair.getKey().y][pair.getKey().x] != 1 && distance < lowest)
            {
                lowest = distance;
                closest = pair.getKey();
            }
        }
        return closest;
    }

    void UpdateOccupiedResources()
    {
        int visionRadius = (int)Math.sqrt(robot.SPECS.UNITS[robot.me.unit].VISION_RADIUS);
        for (int i = -visionRadius; i < visionRadius; i++)
        {
            for (int j = -visionRadius; j < visionRadius; j++)
            {
                int yNew = robot.me.y + i, xNew = robot.me.x + i;
                Position tile = new Position(yNew, xNew);
                if (Helper.DistanceSquared(tile, location) > robot.SPECS.UNITS[robot.SPECS.PILGRIM].VISION_RADIUS)
                {
                    continue;
                }

                if (Helper.RobotAtPosition(robot, tile) == null)
                {
                    occupiedResources[yNew][xNew] = 0;
                }
                else if (Helper.RobotAtPosition(robot, tile).unit == robot.SPECS.PILGRIM)
                {
                    occupiedResources[yNew][xNew] = 1;
                }
                else
                {
                    occupiedResources[yNew][xNew] = 2;
                }

            }
        }
    }
/*
    public Action goToMine(boolean karbResource)
    {
        /*
        get nearest position of resource
            if far, move to it
            if close, stand on it
        if on position
            start mining
        
        
}*/

    //flee if enemy
    //if find enemy church or castle relay info
    
    //go to nearest resource, if occupied go to next
    //return when full
        //if to far to castle build church or wait to build

}