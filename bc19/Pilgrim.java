package bc19;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class Pilgrim extends MovingRobot implements Machine{
  
    MyRobot robot;
    int ourTeam; //red:0 blue:1
    Position location;
    boolean mapIsHorizontal;
    PilgrimState state;
    boolean initialized;
    HashMap<Position, float[][]> karbRoutes;
    HashMap<Position, float[][]> fuelRoutes;
    HashMap<Position, float[][]> ourDropOffRoutes;
    Position dropOff;
    int waitCounter = 0;
    int waitMax = 2;
    int maxKarb, maxFuel;
    int emergencyAmount = 10;
    int karbThreshold = 100, fuelThreshold = 500;
    int returnFuelThreshold = 22;
    boolean miningKarb; //true for karb, false for fuel
    int[][] occupiedResources; //-1 if not resource, 0 unoccupied, 1 occupied by PILGRIM, 2 occupied by any other unit
	
	public Pilgrim(MyRobot robot){
		this.robot = robot;
	}

	public Action Execute(){
        if (!initialized)
        {
            Initialize();
        }
        else
        {
            UpdateOccupiedResources();
            if (state == PilgrimState.GoingToResource)
            {
                GoToMine();
            }
            if (state == PilgrimState.Mining)
            {
                Mining();
            }
            if (state == PilgrimState.Returning)
            {
                ReturnToDropOff();
            }  
        }
        
		return robot.move(0, 1);

    }

    void InitializeVariables(){
        ourTeam = robot.me.team == robot.SPECS.RED ? 0 : 1;
        mapIsHorizontal = Helper.FindSymmetry(robot.map);
        karbRoutes = new HashMap<>();
        fuelRoutes = new HashMap<>();
        ourDropOffRoutes = new HashMap<>();
        location = new Position(robot.me.y, robot.me.x);
        state = PilgrimState.Initializing;
        maxKarb = 0;
        maxFuel = robot.SPECS.UNITS[robot.me.unit].FUEL_CAPACITY;
        miningKarb = true;
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

    void Initialize()
    {
        //10 karb or fuel bit sets max karb
    }

    public Action ReturnToDropOff(){
        if ((dropOff.x - location.x) * (dropOff.x - location.x) > 1 || (dropOff.y - location.y) * (dropOff.y - location.y) > 1)
        {
            return FloodPathing(robot, ourDropOffRoutes.get(dropOff));
        }
        state = PilgrimState.Dropoff;
        WhatToMine();
        return robot.give(dropOff.x - location.x, dropOff.y - location.y, robot.me.karbonite, robot.me.fuel); 
    }

    public float FuelToReturn(float[][] path)
    {
        float tilesFromTarget = path[location.y][location.x];
        float amountOfMoves = (float)(tilesFromTarget / Math.sqrt(robot.SPECS.UNITS[robot.SPECS.PILGRIM].SPEED));
        return (float)(amountOfMoves * robot.SPECS.UNITS[robot.SPECS.PILGRIM].FUEL_PER_MOVE);
    }

    public Position GetNearestResource()
    {
        HashMap<Position, float[][]> chosenRoute = miningKarb ? karbRoutes : fuelRoutes;
        float lowest = Integer.MAX_VALUE;
        Position closest = null;
        for (Map.Entry<Position, float[][]> pair : chosenRoute.entrySet())
        {
            float distance = pair.getValue()[location.y][location.x];
            if (occupiedResources[pair.getKey().y][pair.getKey().x] != 1 && distance < lowest)
            {
                lowest = distance;
                closest = pair.getKey();
            }
        }
        return closest;
    }

    public void GetNearestDropOff()
    {
        float lowest = Integer.MAX_VALUE;
        for (Position dropOffPosition : ourDropOffRoutes.keySet())
        {
            float distance = Helper.DistanceSquared(dropOffPosition, location);
            if (distance < lowest)
            {
                dropOff = dropOffPosition;
                lowest = distance;
            }
        }
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

                if (Helper.RobotAtPosition(robot, tile) == null) //0 if position is unoccupied
                {
                    occupiedResources[yNew][xNew] = 0;
                }
                else if (Helper.RobotAtPosition(robot, tile).unit == robot.SPECS.PILGRIM) //1 if occupied by PILGRIM
                {
                    occupiedResources[yNew][xNew] = 1;
                }
                else //2 if occupied by any other unit
                {
                    occupiedResources[yNew][xNew] = 2;
                }

            }
        }
    }

    public Action GoToMine()
    {
        Position nearest = GetNearestResource();
        int movespeed = (int)robot.SPECS.UNITS[robot.me.unit].SPEED;
        if (nearest.y - location.y == 0 && nearest.x - location.x == 0)
        {
            state = PilgrimState.Mining;
            return robot.mine();
        }
        else if (Helper.DistanceSquared(nearest, location) < movespeed)
        {
            if (occupiedResources[nearest.y][nearest.x] == 2)
            {
                waitCounter++;
                if(waitCounter >= waitMax)
                {
                    occupiedResources[nearest.y][nearest.x] = 1;
                    GoToMine();
                }
                return null;
            }

            return robot.move(nearest.x - location.x, nearest.y - location.y);
        }
        else
        {
            state = PilgrimState.GoingToResource;
            if (miningKarb == true)
            {
                return FloodPathing(robot, karbRoutes.get(nearest));
            }
            else
            {
                return FloodPathing(robot, fuelRoutes.get(nearest));
            }
        }
    }

    public void WhatToMine() //Need to change proportions in mining
    {
        if (maxKarb == emergencyAmount)
        {
            miningKarb = true;
        }
        else if (robot.karbonite < karbThreshold)
        {
            miningKarb = true;
        }
        else if (robot.fuel < fuelThreshold)
        {
            miningKarb = false;
        }
    }

    public Action Mining()
    {
        if (occupiedResources[location.y][location.x] == -1)
        {
            state = PilgrimState.GoingToResource;
        }
        
        int max = miningKarb ? maxKarb : maxFuel;
        int current = miningKarb ? robot.me.karbonite : robot.me.fuel;
        if (current >= max)
        {
            state = PilgrimState.Returning;
            Action church = BuildChurch();
            return church == null ? ReturnToDropOff() : church;
        }
        else
        {
            return robot.mine();
        }
    }

    public boolean ShouldBuildChurch()
    {
        if (maxKarb == emergencyAmount)
        {
            return false;
        }

        GetNearestDropOff();
        if (FuelToReturn(ourDropOffRoutes.get(dropOff)) > returnFuelThreshold)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public Action BuildChurch()
    {
        Position buildChurchHere = Helper.RandomNonResourceAdjacentPosition(robot, location);
        int dx = buildChurchHere.x - location.x;
        int dy = buildChurchHere.y - location.y;
        if (ShouldBuildChurch() == true)
        {
            state = PilgrimState.Returning;
            return robot.buildUnit(robot.SPECS.CHURCH, dx, dy);
        }
        return null;
    }
    //flee if enemy
    //if find enemy church or castle relay info
    
    //go to nearest resource, if occupied go to next
    //return when full
        //if to far to castle build church or wait to build

}

enum PilgrimState
{
    Initializing, GoingToResource, Mining, Returning, Dropoff
}